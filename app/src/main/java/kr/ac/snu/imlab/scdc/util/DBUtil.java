package kr.ac.snu.imlab.scdc.util;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.funf.util.StringUtil;

/**
  * Created by kilho on 15. 8. 3.
  */
 public interface DBUtil {
  /**
      * Immutable Table Definition
      */
     public static class Table {
       private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s (_id INTEGER primary key autoincrement, %s);";

       public final String name;
       private final List<Column> columns;
       public Table(final String name, final List<Column> columns) {
         this.name = name;
         this.columns = new ArrayList<Column>(columns);
       }
       public List<Column> getColumns() { return new ArrayList<Column>(columns); }
       public String getCreateTableSQL() {
         return String.format(CREATE_TABLE_FORMAT, name, StringUtil.join(columns, ", "));
       }
     }

  /**
      * Immutable Column Definition
      *
      */
     public static class Column {
       public final String name, type;
       public Column(final String name, final String type) {
         this.name = name;
         this.type = type;
       }
       @Override
       public String toString() {
         return name + " " + type;
       }
     }
 }
