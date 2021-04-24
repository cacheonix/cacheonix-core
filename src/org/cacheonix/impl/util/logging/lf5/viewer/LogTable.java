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

import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.cacheonix.impl.util.logging.lf5.util.DateFormatManager;

/**
 * LogTable.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brad Marlborough
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public final class LogTable extends JTable {

   private static final long serialVersionUID = 4867085140195148458L;
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   private int _rowHeight = 30;

   protected final JTextArea _detailTextArea;

   // For the columns:
   private final int _numCols = 9;

   private final TableColumn[] _tableColumns = new TableColumn[_numCols];

   private final int[] _colWidths = {40, 40, 40, 70, 70, 360, 440, 200, 60};

   private final LogTableColumn[] _colNames = LogTableColumn.getLogTableColumnArray();

   private final int _colDate = 0;

   private final int _colThread = 1;

   private final int _colLevel = 3;

   private final int _colNDC = 4;

   private final int _colMessage = 6;

   private DateFormatManager _dateFormatManager = null;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public LogTable(final JTextArea detailTextArea) {

      init();

      _detailTextArea = detailTextArea;

      setModel(new FilteredLogTableModel());

      final Enumeration columns = getColumnModel().getColumns();
      int i = 0;
      while (columns.hasMoreElements()) {
         final TableColumn col = (TableColumn) columns.nextElement();
         col.setCellRenderer(new LogTableRowRenderer());
         col.setPreferredWidth(_colWidths[i]);

         _tableColumns[i] = col;
         i++;
      }

      final ListSelectionModel rowSM = getSelectionModel();
      rowSM.addListSelectionListener(new LogTableListSelectionListener(this));

      //setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * Get the DateFormatManager for formatting dates.
    */
   public final DateFormatManager getDateFormatManager() {

      return _dateFormatManager;
   }


   /**
    * Set the date format manager for formatting dates.
    */
   public final void setDateFormatManager(final DateFormatManager dfm) {

      _dateFormatManager = dfm;
   }


   public final synchronized void clearLogRecords() {
      //For JDK1.3
      //((DefaultTableModel)getModel()).setRowCount(0);

      // For JDK1.2.x
      getFilteredLogTableModel().clear();
   }


   public final FilteredLogTableModel getFilteredLogTableModel() {

      return (FilteredLogTableModel) getModel();
   }


   // default view if a view is not set and saved
   public final void setDetailedView() {
      //TODO: Definable Views.
      final TableColumnModel model = getColumnModel();
      // Remove all the columns:
      for (int f = 0; f < _numCols; f++) {
         model.removeColumn(_tableColumns[f]);
      }
      // Add them back in the correct order:
      for (int i = 0; i < _numCols; i++) {
         model.addColumn(_tableColumns[i]);
      }
      //SWING BUG:
      sizeColumnsToFit(-1);
   }


   public final void setView(final List columns) {

      final TableColumnModel model = getColumnModel();

      // Remove all the columns:
      for (int f = 0; f < _numCols; f++) {
         model.removeColumn(_tableColumns[f]);
      }
      final Iterator selectedColumns = columns.iterator();
      final Vector columnNameAndNumber = getColumnNameAndNumber();
      while (selectedColumns.hasNext()) {
         // add the column to the view
         model.addColumn(_tableColumns[columnNameAndNumber.indexOf(selectedColumns.next())]);
      }

      //SWING BUG:
      sizeColumnsToFit(-1);
   }


   public final void setFont(final Font font) {

      super.setFont(font);
      final Graphics g = this.getGraphics();
      if (g != null) {
         final FontMetrics fm = g.getFontMetrics(font);
         final int height = fm.getHeight();
         _rowHeight = height + height / 3;
         setRowHeight(_rowHeight);
      }


   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   private final void init() {

      setRowHeight(_rowHeight);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   }


   // assign a column number to a column name
   private final Vector getColumnNameAndNumber() {

      final Vector columnNameAndNumber = new Vector(_colNames.length);
      for (int i = 0; i < _colNames.length; i++) {
         columnNameAndNumber.add(i, _colNames[i]);
      }
      return columnNameAndNumber;
   }

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

   final class LogTableListSelectionListener implements ListSelectionListener {

      private final JTable _table;


      LogTableListSelectionListener(final JTable table) {

         _table = table;
      }


      public final void valueChanged(final ListSelectionEvent e) {
         //Ignore extra messages.
         if (e.getValueIsAdjusting()) {
            return;
         }

         final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
         if (lsm.isSelectionEmpty()) {
            //no rows are selected
         } else {
            final StringBuilder buf = new StringBuilder(100);
            final int selectedRow = lsm.getMinSelectionIndex();

            for (int i = 0; i < _numCols - 1; i++) {
               String value = "";
               final Object obj = _table.getModel().getValueAt(selectedRow, i);
               if (obj != null) {
                  value = obj.toString();
               }

               buf.append(_colNames[i]).append(':');
               buf.append('\t');

               if (i == _colThread || i == _colMessage || i == _colLevel) {
                  buf.append('\t'); // pad out the date.
               }

               if (i == _colDate || i == _colNDC) {
                  buf.append("\t\t"); // pad out the date.
               }

//               if( i == _colSequence)
//               {
//                  buf.append("\t\t\t"); // pad out the Sequence.
//               }

               buf.append(value);
               buf.append('\n');
            }
            buf.append(_colNames[_numCols - 1]).append(":\n");
            final Object obj = _table.getModel().getValueAt(selectedRow, _numCols - 1);
            if (obj != null) {
               buf.append(obj);
            }

            _detailTextArea.setText(buf.toString());
         }
      }
   }
}






