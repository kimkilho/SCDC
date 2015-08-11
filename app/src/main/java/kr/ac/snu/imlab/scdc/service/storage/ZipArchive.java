package kr.ac.snu.imlab.scdc.service.storage;

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
import edu.mit.media.funf.storage.BackedUpArchive;
import edu.mit.media.funf.storage.CompositeFileArchive;
import edu.mit.media.funf.storage.DefaultArchive;
import edu.mit.media.funf.storage.DirectoryCleaner;
import edu.mit.media.funf.storage.FileArchive;
import edu.mit.media.funf.storage.FileCopier;
import edu.mit.media.funf.storage.FileDirectoryArchive;
import edu.mit.media.funf.util.NameGenerator;
import edu.mit.media.funf.util.NameGenerator.CompositeNameGenerator;
import edu.mit.media.funf.util.NameGenerator.RequiredSuffixNameGenerator;
import edu.mit.media.funf.util.StringUtil;
import kr.ac.snu.imlab.scdc.service.core.SCDCKeys.Config;
import kr.ac.snu.imlab.scdc.util.SharedPrefsHandler;

/**
 * @author Kilho Kim
 * @description Zipped archive class.
 */
public class ZipArchive extends DefaultArchive {

  private static final String SCDC_PREFS = "kr.ac.snu.imlab.scdc";

  @Configurable
  protected String name;

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
    String prefsName = Config.SCDC_PREFS;
    int mode = Context.MODE_PRIVATE;
    // NameGenerator nameGenerator = new CompositeNameGenerator(new UsernameNameGenerator(prefs), new IsFemaleNameGenerator(prefs), new ShortDatetimeNameGenerator(), new RequiredSuffixNameGenerator(".db"));
    NameGenerator nameGenerator = new CompositeNameGenerator(
      new UsernameNameGenerator(context, prefsName, mode),
      new IsFemaleNameGenerator(context, prefsName, mode),
      new ShortDatetimeNameGenerator());
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
      File compressedFile = new File(originalFile.getAbsolutePath() + "_" +
                                      ran + ".zip");

      BufferedInputStream origin = null;
      FileOutputStream fos = new FileOutputStream(compressedFile);
      CheckedOutputStream checksum =
              new CheckedOutputStream(fos, new Adler32());
      ZipOutputStream zos =
              new ZipOutputStream(new BufferedOutputStream(checksum));

      byte[] data = new byte[BUFFER];

      FileInputStream fis = new FileInputStream(originalFile);
      origin = new BufferedInputStream(fis, BUFFER);

      ZipEntry entry = new ZipEntry(originalFile.getName() + ".db");
      zos.putNextEntry(entry);

      int count;
      while ((count = origin.read(data, 0, BUFFER)) != -1) {
        zos.write(data, 0, count);
      }
      zos.flush();

      fis.close();
      origin.close();
      zos.close();

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

    private SharedPrefsHandler spHandler;

    public UsernameNameGenerator(Context context, String name, int mode) {
      spHandler = SharedPrefsHandler.getInstance(context, name, mode);
    }

    @Override
    public String generateName(final String name) {
      return name == null ? null : spHandler.getUsername() + "_" + name;
    }
  }

  /**
   * New NameGenerator class: IsFemaleNameGenerator
   * Added by Kilho Kim.
   */
  static class IsFemaleNameGenerator implements NameGenerator {

    private SharedPrefsHandler spHandler;

    public IsFemaleNameGenerator(Context context, String name, int mode) {
      spHandler = SharedPrefsHandler.getInstance(context, name, mode);
    }

    @Override
    public String generateName(final String name) {
      return name == null ?
          null : (spHandler.getIsFemale() ? "female" : "male") + "_" + name;
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
