/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.util.logging.lf5.viewer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LogTableColumn
 *
 * @author Michael J. Sikorsky
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public final class LogTableColumn implements Serializable {

   private static final long serialVersionUID = -4275827753626456547L;

   // log4j table columns.
   public static final LogTableColumn DATE = new LogTableColumn("Date");

   public static final LogTableColumn THREAD = new LogTableColumn("Thread");

   public static final LogTableColumn MESSAGE_NUM = new LogTableColumn("Message #");

   public static final LogTableColumn LEVEL = new LogTableColumn("Level");

   public static final LogTableColumn NDC = new LogTableColumn("NDC");

   public static final LogTableColumn CATEGORY = new LogTableColumn("Category");

   public static final LogTableColumn MESSAGE = new LogTableColumn("Message");

   public static final LogTableColumn LOCATION = new LogTableColumn("Location");

   public static final LogTableColumn THROWN = new LogTableColumn("Thrown");


   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected final String _label;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------
   private static final LogTableColumn[] _log4JColumns;

   private static final Map _logTableColumnMap;


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------
   static {
      _log4JColumns = new LogTableColumn[]{DATE, THREAD, MESSAGE_NUM, LEVEL, NDC, CATEGORY,
              MESSAGE, LOCATION, THROWN};

      _logTableColumnMap = new HashMap(11);

      for (int i = 0; i < _log4JColumns.length; i++) {
         _logTableColumnMap.put(_log4JColumns[i]._label, _log4JColumns[i]);
      }
   }


   public LogTableColumn(final String label) {

      _label = label;
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * Return the Label of the LogLevel.
    */
   public final String getLabel() {

      return _label;
   }


   /**
    * Convert a column label into a LogTableColumn object.
    *
    * @param column The label of a level to be converted into a LogTableColumn.
    * @return LogTableColumn The LogTableColumn with a label equal to column.
    * @throws LogTableColumnFormatException Is thrown when the column can not be converted into a LogTableColumn.
    */
   public static LogTableColumn valueOf(String column)
           throws LogTableColumnFormatException {

      LogTableColumn tableColumn = null;
      if (column != null) {
         column = column.trim();
         tableColumn = (LogTableColumn) _logTableColumnMap.get(column);
      }

      if (tableColumn == null) {
         final StringBuilder buf = new StringBuilder(111);
         buf.append("Error while trying to parse (").append(column).append(") into");
         buf.append(" a LogTableColumn.");
         throw new LogTableColumnFormatException(buf.toString());
      }
      return tableColumn;
   }


   public final boolean equals(final Object value) {

      if (this == value) {
         return true;
      }
      if (value == null || getClass() != value.getClass()) {
         return false;
      }

      final LogTableColumn that = (LogTableColumn) value;

      return _label.equals(that._label);

   }


   public final int hashCode() {

      return _label.hashCode();
   }


   public final String toString() {

      return _label;
   }


   /**
    * @return A <code>List</code> of <code>LogTableColumn/code> objects that map to log4j <code>Column</code> objects.
    */
   public static List getLogTableColumns() {

      return Arrays.asList(_log4JColumns);
   }


   public static LogTableColumn[] getLogTableColumnArray() {

      if (_log4JColumns == null) {
         return null;
      } else {
         final LogTableColumn[] result = new LogTableColumn[_log4JColumns.length];
         System.arraycopy(_log4JColumns, 0, result, 0, _log4JColumns.length);
         return result;
      }
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}