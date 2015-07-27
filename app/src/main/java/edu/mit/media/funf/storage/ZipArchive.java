package edu.mit.media.funf.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.SecretKey;

import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.util.NameGenerator;
import edu.mit.media.funf.util.NameGenerator.CompositeNameGenerator;
import edu.mit.media.funf.util.NameGenerator.RequiredSuffixNameGenerator;
import edu.mit.media.funf.util.StringUtil;

/**
 * @author Kilho Kim
 * @description Zipped archive class.
 */
public class ZipArchive extends DefaultArchive {

  private static final String SCDC_PREFS = "kr.ac.snu.imlab.scdc";

  @Configurable
  protected String name = "default";

  public ZipArchive() {
  }

  public ZipArchive(Context ctx, String name) {
    super(ctx, name);
  }

  private String getCleanedName() {
    return StringUtil.simpleFilesafe(name);
  }

  private FileArchive delegateArchive; // Cache
  protected FileArchive getDelegateArchive() {
    if (delegateArchive == null) {
      synchronized(this) {
        if (delegateArchive == null) {
          SecretKey key = getSecretKey();
          String rootSdCardPath = getPathOnSDCard();
          FileArchive backupArchive = FileDirectoryArchive.getRollingFileArchive(new File(rootSdCardPath + "backup"));
          FileArchive mainArchive = new CompositeFileArchive(
                  getTimestampedDbFileArchive(new File(rootSdCardPath + "archive"), context, key),
                  getTimestampedDbFileArchive(context.getDir("funf_" + getCleanedName() + "_archive", Context.MODE_PRIVATE), context, key)
          );
          delegateArchive = new BackedUpArchive(mainArchive, backupArchive);
        }
      }
    }
    return delegateArchive;
  }

  static FileDirectoryArchive getTimestampedDbFileArchive(File archiveDir, Context context, SecretKey encryptionKey) {
    SharedPreferences prefs = context.getSharedPreferences(SCDC_PREFS,
            Context.MODE_PRIVATE);
    // NameGenerator nameGenerator = new CompositeNameGenerator(new UsernameNameGenerator(prefs), new IsFemaleNameGenerator(prefs), new ShortDatetimeNameGenerator(), new RequiredSuffixNameGenerator(".db"));
    NameGenerator nameGenerator = new CompositeNameGenerator(new UsernameNameGenerator(prefs), new IsFemaleNameGenerator(prefs), new ShortDatetimeNameGenerator(),
            new RequiredSuffixNameGenerator(".zip"));
    // Use non-encrypting FileCopier
    FileCopier copier = new FileCopier.SimpleFileCopier();
    return new FileDirectoryArchive(archiveDir, nameGenerator, copier, new DirectoryCleaner.KeepAll());
  }

  private File compressFile(File originalFile) {
    final String TAG = "DEBUG";
    final int BUFFER = 2048;

    String ran = String.valueOf(1 + (int)(Math.random() * (100000 - 1)));
    Log.i(TAG, "ZipArchive.compressFile()/ Adding: " +
            originalFile.getAbsolutePath());

    try {
      File compressedFile = new File(originalFile.getAbsolutePath());

      BufferedInputStream origin = null;
      FileOutputStream dest = new FileOutputStream(compressedFile);
      CheckedOutputStream checksum =
              new CheckedOutputStream(dest, new Adler32());
      ZipOutputStream out =
              new ZipOutputStream(new BufferedOutputStream(checksum));

      byte[] data = new byte[BUFFER];

      FileInputStream fis = new FileInputStream(originalFile);
      origin = new BufferedInputStream(fis, BUFFER);

      ZipEntry entry = new ZipEntry(ran + "_" + originalFile.getName() + "" +
              ".sqlite");
      out.putNextEntry(entry);

      int count;
      while ((count = origin.read(data, 0, BUFFER)) != -1) {
        out.write(data, 0, count);
      }
      out.flush();

      origin.close();
      out.close();

      Log.i(TAG, "ZipArchive.compressFile()/ Funf archive: " + compressedFile);
      Log.i(TAG, "ZipArchive.compressFile()/ File Zipped Length: " +
                  compressedFile.length());

      return compressedFile;
    } catch (Exception e) {
      Log.i(TAG, "ZipArchive.getTimestampedDbFileArchive(): " +
              "File compression error:" + e);
      e.printStackTrace();

      return null;
    }
  }

  /**
   * New NameGenerator class: UsernameNameGenerator.
   * Added by Kilho Kim.
   */
  static class UsernameNameGenerator implements NameGenerator {

    private SharedPreferences prefs;

    public UsernameNameGenerator(SharedPreferences prefs) {
      this.prefs = prefs;
    }

    @Override
    public String generateName(final String name) {
      return name == null ? null : prefs.getString("userName", "imlab_user") + "_" + name;
    }
  }

  /**
   * New NameGenerator class: IsFemaleNameGenerator
   * Added by Kilho Kim.
   */
  static class IsFemaleNameGenerator implements NameGenerator {

    private SharedPreferences prefs;

    public IsFemaleNameGenerator(SharedPreferences prefs) {
      this.prefs = prefs;
    }

    @Override
    public String generateName(final String name) {
      return name == null ? null : (prefs.getBoolean("isFemale", false) ? "female" : "male") + "_" + name;
    }
  }


  /**
   * New NameGenerator class: ShortDatetimeNameGenerator.
   * Added by Kilho Kim.
   */
  static class ShortDatetimeNameGenerator implements NameGenerator {
    @Override
    public String generateName(final String name) {
      // String datetime = java.text.DateFormat.getDateTimeInstance().format(new Date());
      SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_HHmmss");
      String datetime = fmt.format(new Date());
      return name == null ? null : datetime + "_" + name;
    }
  }

  @Override
  public boolean add(File item) {
    // Zip file
    File compressedItem = compressFile(item);

    if (compressedItem != null) {
      item = compressedItem;
    }

    return getDelegateArchive().add(item);
  }

  @Override
  public boolean contains(File item) {
    return getDelegateArchive().contains(item);
  }

  @Override
  public File[] getAll() {
    return getDelegateArchive().getAll();
  }

  @Override
  public boolean remove(File item) {
    return getDelegateArchive().remove(item);
  }
}
