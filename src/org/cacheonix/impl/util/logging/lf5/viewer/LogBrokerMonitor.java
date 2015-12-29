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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;

import org.cacheonix.impl.util.logging.lf5.LogLevel;
import org.cacheonix.impl.util.logging.lf5.LogRecord;
import org.cacheonix.impl.util.logging.lf5.LogRecordFilter;
import org.cacheonix.impl.util.logging.lf5.util.DateFormatManager;
import org.cacheonix.impl.util.logging.lf5.util.LogFileParser;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryExplorerTree;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryPath;
import org.cacheonix.impl.util.logging.lf5.viewer.configure.ConfigurationManager;
import org.cacheonix.impl.util.logging.lf5.viewer.configure.MRUFileManager;

/**
 * LogBrokerMonitor .
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brad Marlborough
 * @author Richard Wan
 * @author Brent Sprecher
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

@SuppressWarnings("deprecation")
public final class LogBrokerMonitor {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   public static final String DETAILED_VIEW = "Detailed";

   //    public static final String STANDARD_VIEW = "Standard";
   //    public static final String COMPACT_VIEW = "Compact";
   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected JFrame _logMonitorFrame = null;

   protected int _logMonitorFrameWidth = 550;

   protected int _logMonitorFrameHeight = 500;

   protected LogTable _table = null;

   protected CategoryExplorerTree _categoryExplorerTree = null;

   protected String _searchText = null;

   protected String _NDCTextFilter = "";

   protected LogLevel _leastSevereDisplayedLogLevel = LogLevel.DEBUG;

   protected JScrollPane _logTableScrollPane = null;

   protected JLabel _statusLabel = null;

   protected Object _lock = new Object();

   protected JComboBox _fontSizeCombo = null;

   protected int _fontSize = 10;

   protected String _fontName = "Dialog";

   protected String _currentView = DETAILED_VIEW;

   protected final boolean _loadSystemFonts = false;

   protected final boolean _trackTableScrollPane = true;

   protected Dimension _lastTableViewportSize = null;

   protected boolean _callSystemExitOnClose = false;

   protected final List _displayedLogBrokerProperties = new Vector(3);

   protected final Map _logLevelMenuItems = new HashMap(11);

   protected final Map _logTableColumnMenuItems = new HashMap(11);

   protected List _levels = null;

   protected List _columns = null;

   protected boolean _isDisposed = false;

   protected ConfigurationManager _configurationManager = null;

   protected MRUFileManager _mruFileManager = null;

   protected File _fileLocation = null;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   /**
    * Construct a LogBrokerMonitor.
    */
   @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
   public LogBrokerMonitor(final List logLevels) {

      _levels = logLevels;
      _columns = LogTableColumn.getLogTableColumns();
      // This allows us to use the LogBroker in command line tools and
      // have the option for it to shutdown.

      String callSystemExitOnClose =
              System.getProperty("monitor.exit");
      if (callSystemExitOnClose == null) {
         callSystemExitOnClose = "false";
      }
      callSystemExitOnClose = callSystemExitOnClose.trim().toLowerCase();

      if ("true".equals(callSystemExitOnClose)) {
         _callSystemExitOnClose = true;
      }

      initComponents();


      _logMonitorFrame.addWindowListener(
              new LogBrokerMonitorWindowAdaptor(this));

   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * Show the frame for the LogBrokerMonitor. Dispatched to the swing thread.
    */
   public final void show(final int delay) {

      if (_logMonitorFrame.isVisible()) {
         return;
      }
      // This request is very low priority, let other threads execute first.
      SwingUtilities.invokeLater(new Runnable() {

         public void run() {

            pause(delay);
            _logMonitorFrame.setVisible(true);
         }
      });
   }


   public final void show() {

      show(0);
   }


   /**
    * Dispose of the frame for the LogBrokerMonitor.
    */
   public final void dispose() {

      _logMonitorFrame.dispose();
      _isDisposed = true;

      if (_callSystemExitOnClose) {
         System.exit(0);
      }
   }


   /**
    * Hide the frame for the LogBrokerMonitor.
    */
   public void hide() {

      _logMonitorFrame.setVisible(false);
   }


   /**
    * Get the DateFormatManager for formatting dates.
    */
   public DateFormatManager getDateFormatManager() {

      return _table.getDateFormatManager();
   }


   /**
    * Set the date format manager for formatting dates.
    */
   public void setDateFormatManager(final DateFormatManager dfm) {

      _table.setDateFormatManager(dfm);
   }


   /**
    * Get the value of whether or not System.exit() will be called when the LogBrokerMonitor is closed.
    */
   public boolean getCallSystemExitOnClose() {

      return _callSystemExitOnClose;
   }


   /**
    * Set the value of whether or not System.exit() will be called when the LogBrokerMonitor is closed.
    */
   public final void setCallSystemExitOnClose(final boolean callSystemExitOnClose) {

      _callSystemExitOnClose = callSystemExitOnClose;
   }


   /**
    * Add a log record message to be displayed in the LogTable. This method is thread-safe as it posts requests to the
    * SwingThread rather than processing directly.
    */
   public final void addMessage(final LogRecord lr) {

      if (_isDisposed) {
         // If the frame has been disposed of, do not log any more
         // messages.
         return;
      }

      SwingUtilities.invokeLater(new Runnable() {

         public void run() {

            _categoryExplorerTree.getExplorerModel().addLogRecord(lr);
            _table.getFilteredLogTableModel().addLogRecord(lr); // update table
            updateStatusLabel(); // show updated counts
         }
      });
   }


   public final void setMaxNumberOfLogRecords(final int maxNumberOfLogRecords) {

      _table.getFilteredLogTableModel().setMaxNumberOfLogRecords(maxNumberOfLogRecords);
   }


   public final JFrame getBaseFrame() {

      return _logMonitorFrame;
   }


   public void setTitle(final String title) {

      _logMonitorFrame.setTitle(title + " - LogFactor5");
   }


   public final void setFrameSize(final int width, final int height) {

      final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      if (width > 0 && width < screen.width) {
         _logMonitorFrameWidth = width;
      }
      if (height > 0 && height < screen.height) {
         _logMonitorFrameHeight = height;
      }
      updateFrameSize();
   }


   public final void setFontSize(final int fontSize) {

      changeFontSizeCombo(_fontSizeCombo, fontSize);
      // setFontSizeSilently(actualFontSize); - changeFontSizeCombo fires event
      // refreshDetailTextArea();
   }


   public final void addDisplayedProperty(final Object messageLine) {

      _displayedLogBrokerProperties.add(messageLine);
   }


   public final Map getLogLevelMenuItems() {

      return _logLevelMenuItems;
   }


   public final Map getLogTableColumnMenuItems() {

      return _logTableColumnMenuItems;
   }


   public final JCheckBoxMenuItem getTableColumnMenuItem(final LogTableColumn column) {

      return getLogTableColumnMenuItem(column);
   }


   public final CategoryExplorerTree getCategoryExplorerTree() {

      return _categoryExplorerTree;
   }


   // Added in version 1.2 - gets the value of the NDC text filter
   // This value is set back to null each time the Monitor is initialized.
   public final String getNDCTextFilter() {

      return _NDCTextFilter;
   }


   // Added in version 1.2 - sets the NDC Filter based on
   // a String passed in by the user.  This value is persisted
   // in the XML Configuration file.
   public final void setNDCLogRecordFilter(final String textFilter) {

      _table.getFilteredLogTableModel().
              setLogRecordFilter(createNDCLogRecordFilter(textFilter));
   }
   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final void setSearchText(final String text) {

      _searchText = text;
   }


   // Added in version 1.2 - Sets the text filter for the NDC
   protected final void setNDCTextFilter(final String text) {
      // if no value is set, set it to a blank string
      // otherwise use the value provided
      if (text == null) {
         _NDCTextFilter = "";
      } else {
         _NDCTextFilter = text;
      }
   }


   // Added in version 1.2 - Uses a different filter that sorts
   // based on an NDC string passed in by the user.  If the string
   // is null or is an empty string, we do nothing.
   protected final void sortByNDC() {

      final String text = _NDCTextFilter;
      if (text == null || text.isEmpty()) {
         return;
      }

      // Use new NDC filter
      _table.getFilteredLogTableModel().
              setLogRecordFilter(createNDCLogRecordFilter(text));
   }


   protected final void findSearchText() {

      final String text = _searchText;
      if (text == null || text.isEmpty()) {
         return;
      }
      final int startRow = getFirstSelectedRow();
      final int foundRow = findRecord(
              startRow,
              text,
              _table.getFilteredLogTableModel().getFilteredRecords()
      );
      selectRow(foundRow);
   }


   protected final int getFirstSelectedRow() {

      return _table.getSelectionModel().getMinSelectionIndex();
   }


   protected final void selectRow(final int foundRow) {

      if (foundRow == -1) {
         final String message = _searchText + " not found.";
         JOptionPane.showMessageDialog(
                 _logMonitorFrame,
                 message,
                 "Text not found",
                 JOptionPane.INFORMATION_MESSAGE
         );
         return;
      }
      LF5SwingUtils.selectRow(foundRow, _table, _logTableScrollPane);
   }


   protected final int findRecord(
           int startRow,
           final String searchText,
           final List records
   ) {

      if (startRow < 0) {
         startRow = 0; // start at first element if no rows are selected
      } else {
         startRow++; // start after the first selected row
      }
      int len = records.size();

      for (int i = startRow; i < len; i++) {
         if (matches((LogRecord) records.get(i), searchText)) {
            return i; // found a record
         }
      }
      // wrap around to beginning if when we reach the end with no match
      len = startRow;
      for (int i = 0; i < len; i++) {
         if (matches((LogRecord) records.get(i), searchText)) {
            return i; // found a record
         }
      }
      // nothing found
      return -1;
   }


   /**
    * Check to see if the any records contain the search string. Searching now supports NDC messages and date.
    */
   protected final boolean matches(final LogRecord record, final String text) {

      final String message = record.getMessage();
      final String NDC = record.getNDC();

      if (message == null && NDC == null || text == null) {
         return false;
      }
      return !(!message.toLowerCase().contains(text.toLowerCase()) &&
              !NDC.toLowerCase().contains(text.toLowerCase()));

   }


   /**
    * When the fontsize of a JTextArea is changed, the word-wrapped lines may become garbled.  This method clears and
    * resets the text of the text area.
    */
   protected final void refresh(final JTextArea textArea) {

      final String text = textArea.getText();
      textArea.setText("");
      textArea.setText(text);
   }


   protected final void refreshDetailTextArea() {

      refresh(_table._detailTextArea);
   }


   protected final void clearDetailTextArea() {

      _table._detailTextArea.setText("");
   }


   /**
    * Changes the font selection in the combo box and returns the size actually selected.
    *
    * @return -1 if unable to select an appropriate font
    */
   protected final int changeFontSizeCombo(final JComboBox box, final int requestedSize) {

      final int len = box.getItemCount();
      Object selectedObject = box.getItemAt(0);
      int selectedValue = Integer.parseInt(String.valueOf(selectedObject));
      for (int i = 0; i < len; i++) {
         final Object currentObject = box.getItemAt(i);
         final int currentValue = Integer.parseInt(String.valueOf(currentObject));
         if (selectedValue < currentValue && currentValue <= requestedSize) {
            selectedValue = currentValue;
            selectedObject = currentObject;
         }
      }
      box.setSelectedItem(selectedObject);
      return selectedValue;
   }


   /**
    * Does not update gui or cause any events to be fired.
    */
   protected final void setFontSizeSilently(final int fontSize) {

      _fontSize = fontSize;
      setFontSize(_table._detailTextArea, fontSize);
      selectRow(0);
      setFontSize(_table, fontSize);
   }


   protected final void setFontSize(final Component component, final int fontSize) {

      final Font oldFont = component.getFont();
      final Font newFont =
              new Font(oldFont.getFontName(), oldFont.getStyle(), fontSize);
      component.setFont(newFont);
   }


   protected final void updateFrameSize() {

      _logMonitorFrame.setSize(_logMonitorFrameWidth, _logMonitorFrameHeight);
      centerFrame(_logMonitorFrame);
   }


   protected final void pause(final int millis) {

      try {
         Thread.sleep((long) millis);
      } catch (final InterruptedException ignored) {

      }
   }


   protected final void initComponents() {
      //
      // Configure the Frame.
      //
      _logMonitorFrame = new JFrame("LogFactor5");

      _logMonitorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      final String resource =
              "/org/apache/log4j/lf5/viewer/images/lf5_small_icon.gif";
      final URL lf5IconURL = getClass().getResource(resource);

      if (lf5IconURL != null) {
         _logMonitorFrame.setIconImage(new ImageIcon(lf5IconURL).getImage());
      }
      updateFrameSize();

      //
      // Configure the LogTable.
      //
      final JTextArea detailTA = createDetailTextArea();
      final JScrollPane detailTAScrollPane = new JScrollPane(detailTA);
      _table = new LogTable(detailTA);
      setView(_currentView, _table);
      _table.setFont(new Font(_fontName, Font.PLAIN, _fontSize));
      _logTableScrollPane = new JScrollPane(_table);

      if (_trackTableScrollPane) {
         _logTableScrollPane.getVerticalScrollBar().addAdjustmentListener(
                 new TrackingAdjustmentListener()
         );
      }

      // Configure the SplitPane between the LogTable & DetailTextArea
      //

      final JSplitPane tableViewerSplitPane = new JSplitPane();
      tableViewerSplitPane.setOneTouchExpandable(true);
      tableViewerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      tableViewerSplitPane.setLeftComponent(_logTableScrollPane);
      tableViewerSplitPane.setRightComponent(detailTAScrollPane);
      // Make sure to do this last..
      //tableViewerSplitPane.setDividerLocation(1.0); Doesn't work
      //the same under 1.2.x & 1.3
      // "350" is a magic number that provides the correct default
      // behaviour under 1.2.x & 1.3.  For example, bumping this
      // number to 400, causes the pane to be completely open in 1.2.x
      // and closed in 1.3
      tableViewerSplitPane.setDividerLocation(350);

      //
      // Configure the CategoryExplorer
      //

      _categoryExplorerTree = new CategoryExplorerTree();

      _table.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());

      final JScrollPane categoryExplorerTreeScrollPane =
              new JScrollPane(_categoryExplorerTree);
      categoryExplorerTreeScrollPane.setPreferredSize(new Dimension(130, 400));

      // Load most recently used file list
      _mruFileManager = new MRUFileManager();

      //
      // Configure the SplitPane between the CategoryExplorer & (LogTable/Detail)
      //

      final JSplitPane splitPane = new JSplitPane();
      splitPane.setOneTouchExpandable(true);
      splitPane.setRightComponent(tableViewerSplitPane);
      splitPane.setLeftComponent(categoryExplorerTreeScrollPane);
      // Do this last.
      splitPane.setDividerLocation(130);
      //
      // Add the MenuBar, StatusArea, CategoryExplorer|LogTable to the
      // LogMonitorFrame.
      //
      _logMonitorFrame.getRootPane().setJMenuBar(createMenuBar());
      _logMonitorFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
      _logMonitorFrame.getContentPane().add(createToolBar(),
              BorderLayout.NORTH);
      _logMonitorFrame.getContentPane().add(createStatusArea(),
              BorderLayout.SOUTH);

      makeLogTableListenToCategoryExplorer();
      addTableModelProperties();

      //
      // Configure ConfigurationManager
      //
      _configurationManager = new ConfigurationManager(this, _table);

   }


   protected final LogRecordFilter createLogRecordFilter() {

      return new LogRecordFilter() {

         public boolean passes(final LogRecord record) {

            final CategoryPath path = new CategoryPath(record.getCategory());
            return
                    getMenuItem(record.getLevel()).isSelected() &&
                            _categoryExplorerTree.getExplorerModel().isCategoryPathActive(path);
         }
      };
   }


   // Added in version 1.2 - Creates a new filter that sorts records based on
   // an NDC string passed in by the user.
   protected final LogRecordFilter createNDCLogRecordFilter(final String text) {

      _NDCTextFilter = text;

      return new LogRecordFilter() {

         public boolean passes(final LogRecord record) {

            final String NDC = record.getNDC();
            final CategoryPath path = new CategoryPath(record.getCategory());
            if (NDC == null || _NDCTextFilter == null) {
               return false;
            } else if (!NDC.toLowerCase().contains(_NDCTextFilter.toLowerCase())) {
               return false;
            } else {
               return getMenuItem(record.getLevel()).isSelected() &&
                       _categoryExplorerTree.getExplorerModel().isCategoryPathActive(path);
            }
         }
      };
   }


   protected final void updateStatusLabel() {

      _statusLabel.setText(getRecordsDisplayedMessage());
   }


   protected final String getRecordsDisplayedMessage() {

      final FilteredLogTableModel model = _table.getFilteredLogTableModel();
      return getStatusText(model.getRowCount(), model.getTotalRowCount());
   }


   protected final void addTableModelProperties() {

      final FilteredLogTableModel model = _table.getFilteredLogTableModel();

      addDisplayedProperty(new Object() {

         public String toString() {

            return getRecordsDisplayedMessage();
         }
      });
      addDisplayedProperty(new Object() {

         public String toString() {

            return "Maximum number of displayed LogRecords: "
                    + model._maxNumberOfLogRecords;
         }
      });
   }


   protected final String getStatusText(final int displayedRows, final int totalRows) {

      return "Displaying: " + displayedRows + " records out of a total of: " + totalRows + " records.";
   }


   protected final void makeLogTableListenToCategoryExplorer() {

      final ActionListener listener = new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            _table.getFilteredLogTableModel().refresh();
            updateStatusLabel();
         }
      };
      _categoryExplorerTree.getExplorerModel().addActionListener(listener);
   }


   protected final JPanel createStatusArea() {

      final JPanel statusArea = new JPanel();
      final JLabel status =
              new JLabel("No log records to display.");
      _statusLabel = status;
      status.setHorizontalAlignment(JLabel.LEFT);

      statusArea.setBorder(BorderFactory.createEtchedBorder());
      statusArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      statusArea.add(status);

      return statusArea;
   }


   protected final JTextArea createDetailTextArea() {

      final JTextArea detailTA = new JTextArea();
      detailTA.setFont(new Font("Monospaced", Font.PLAIN, 14));
      detailTA.setTabSize(3);
      detailTA.setLineWrap(true);
      detailTA.setWrapStyleWord(false);
      return detailTA;
   }


   protected final JMenuBar createMenuBar() {

      final JMenuBar menuBar = new JMenuBar();
      menuBar.add(createFileMenu());
      menuBar.add(createEditMenu());
      menuBar.add(createLogLevelMenu());
      menuBar.add(createViewMenu());
      menuBar.add(createConfigureMenu());
      menuBar.add(createHelpMenu());

      return menuBar;
   }


   protected final JMenu createLogLevelMenu() {

      final JMenu result = new JMenu("Log Level");
      result.setMnemonic('l');
      final Iterator levels = getLogLevels();
      while (levels.hasNext()) {
         result.add(getMenuItem((LogLevel) levels.next()));
      }

      result.addSeparator();
      result.add(createAllLogLevelsMenuItem());
      result.add(createNoLogLevelsMenuItem());
      result.addSeparator();
      result.add(createLogLevelColorMenu());
      result.add(createResetLogLevelColorMenuItem());

      return result;
   }


   protected final JMenuItem createAllLogLevelsMenuItem() {

      final JMenuItem result = new JMenuItem("Show all LogLevels");
      result.setMnemonic('s');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            selectAllLogLevels(true);
            _table.getFilteredLogTableModel().refresh();
            updateStatusLabel();
         }
      });
      return result;
   }


   protected final JMenuItem createNoLogLevelsMenuItem() {

      final JMenuItem result = new JMenuItem("Hide all LogLevels");
      result.setMnemonic('h');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            selectAllLogLevels(false);
            _table.getFilteredLogTableModel().refresh();
            updateStatusLabel();
         }
      });
      return result;
   }


   protected final JMenu createLogLevelColorMenu() {

      final JMenu colorMenu = new JMenu("Configure LogLevel Colors");
      colorMenu.setMnemonic('c');
      final Iterator levels = getLogLevels();
      while (levels.hasNext()) {
         colorMenu.add(createSubMenuItem((LogLevel) levels.next()));
      }

      return colorMenu;
   }


   protected final JMenuItem createResetLogLevelColorMenuItem() {

      final JMenuItem result = new JMenuItem("Reset LogLevel Colors");
      result.setMnemonic('r');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            // reset the level colors in the map
            LogLevel.resetLogLevelColorMap();

            // refresh the table
            _table.getFilteredLogTableModel().refresh();
         }
      });
      return result;
   }


   protected final void selectAllLogLevels(final boolean selected) {

      final Iterator levels = getLogLevels();
      while (levels.hasNext()) {
         getMenuItem((LogLevel) levels.next()).setSelected(selected);
      }
   }


   protected final JCheckBoxMenuItem getMenuItem(final LogLevel level) {

      JCheckBoxMenuItem result = (JCheckBoxMenuItem) _logLevelMenuItems.get(level);
      if (result == null) {
         result = createMenuItem(level);
         _logLevelMenuItems.put(level, result);
      }
      return result;
   }


   protected final JMenuItem createSubMenuItem(final LogLevel level) {

      final JMenuItem result = new JMenuItem(level.toString());
      result.setMnemonic(level.toString().charAt(0));
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            showLogLevelColorChangeDialog(result, level);
         }
      });

      return result;

   }


   protected final void showLogLevelColorChangeDialog(final JMenuItem result, final LogLevel level) {

      final Color newColor = JColorChooser.showDialog(
              _logMonitorFrame,
              "Choose LogLevel Color",
              result.getForeground());

      if (newColor != null) {
         // set the color for the record
         level.setLogLevelColorMap(level, newColor);
         _table.getFilteredLogTableModel().refresh();
      }

   }


   protected final JCheckBoxMenuItem createMenuItem(final LogLevel level) {

      final JCheckBoxMenuItem result = new JCheckBoxMenuItem(level.toString());
      result.setSelected(true);
      result.setMnemonic(level.toString().charAt(0));
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            _table.getFilteredLogTableModel().refresh();
            updateStatusLabel();
         }
      });
      return result;
   }


   // view menu
   protected final JMenu createViewMenu() {

      final JMenu result = new JMenu("View");
      result.setMnemonic('v');
      final Iterator columns = getLogTableColumns();
      while (columns.hasNext()) {
         result.add(getLogTableColumnMenuItem((LogTableColumn) columns.next()));
      }

      result.addSeparator();
      result.add(createAllLogTableColumnsMenuItem());
      result.add(createNoLogTableColumnsMenuItem());
      return result;
   }


   protected final JCheckBoxMenuItem getLogTableColumnMenuItem(final LogTableColumn column) {

      JCheckBoxMenuItem result = (JCheckBoxMenuItem) _logTableColumnMenuItems.get(column);
      if (result == null) {
         result = createLogTableColumnMenuItem(column);
         _logTableColumnMenuItems.put(column, result);
      }
      return result;
   }


   protected final JCheckBoxMenuItem createLogTableColumnMenuItem(final LogTableColumn column) {

      final JCheckBoxMenuItem result = new JCheckBoxMenuItem(column.toString());

      result.setSelected(true);
      result.setMnemonic(column.toString().charAt(0));
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {
            // update list of columns and reset the view
            final List selectedColumns = updateView();
            _table.setView(selectedColumns);
         }
      });
      return result;
   }


   protected final List updateView() {

      final ArrayList updatedList = new ArrayList(3);
      for (final Object _column : _columns) {
         final LogTableColumn column = (LogTableColumn) _column;
         final JCheckBoxMenuItem result = getLogTableColumnMenuItem(column);
         // check and see if the checkbox is checked
         if (result.isSelected()) {
            updatedList.add(column);
         }
      }

      return updatedList;
   }


   protected final JMenuItem createAllLogTableColumnsMenuItem() {

      final JMenuItem result = new JMenuItem("Show all Columns");
      result.setMnemonic('s');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            selectAllLogTableColumns(true);
            // update list of columns and reset the view
            final List selectedColumns = updateView();
            _table.setView(selectedColumns);
         }
      });
      return result;
   }


   protected final JMenuItem createNoLogTableColumnsMenuItem() {

      final JMenuItem result = new JMenuItem("Hide all Columns");
      result.setMnemonic('h');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            selectAllLogTableColumns(false);
            // update list of columns and reset the view
            final List selectedColumns = updateView();
            _table.setView(selectedColumns);
         }
      });
      return result;
   }


   protected final void selectAllLogTableColumns(final boolean selected) {

      final Iterator columns = getLogTableColumns();
      while (columns.hasNext()) {
         getLogTableColumnMenuItem((LogTableColumn) columns.next()).setSelected(selected);
      }
   }


   protected final JMenu createFileMenu() {

      final JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('f');
      fileMenu.add(createOpenMI());
      fileMenu.add(createOpenURLMI());
      fileMenu.addSeparator();
      fileMenu.add(createCloseMI());
      createMRUFileListMI(fileMenu);
      fileMenu.addSeparator();
      fileMenu.add(createExitMI());
      return fileMenu;
   }


   /**
    * Menu item added to allow log files to be opened with the LF5 GUI.
    */
   protected final JMenuItem createOpenMI() {

      final JMenuItem result = new JMenuItem("Open...");
      result.setMnemonic('o');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            requestOpen();
         }
      });
      return result;
   }


   /**
    * Menu item added to allow log files loaded from a URL to be opened by the LF5 GUI.
    */
   protected final JMenuItem createOpenURLMI() {

      final JMenuItem result = new JMenuItem("Open URL...");
      result.setMnemonic('u');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            requestOpenURL();
         }
      });
      return result;
   }


   protected final JMenuItem createCloseMI() {

      final JMenuItem result = new JMenuItem("Close");
      result.setMnemonic('c');
      result.setAccelerator(KeyStroke.getKeyStroke("control Q"));
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            requestClose();
         }
      });
      return result;
   }


   /**
    * Creates a Most Recently Used file list to be displayed in the File menu
    */
   protected final void createMRUFileListMI(final JMenu menu) {

      final String[] files = _mruFileManager.getMRUFileList();

      if (files != null) {
         menu.addSeparator();
         for (int i = 0; i < files.length; i++) {
            final JMenuItem result = new JMenuItem(i + 1 + " " + files[i]);
            result.setMnemonic(i + 1);
            result.addActionListener(new ActionListener() {

               public void actionPerformed(final ActionEvent e) {

                  requestOpenMRU(e);
               }
            });
            menu.add(result);
         }
      }
   }


   protected final JMenuItem createExitMI() {

      final JMenuItem result = new JMenuItem("Exit");
      result.setMnemonic('x');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            requestExit();
         }
      });
      return result;
   }


   protected final JMenu createConfigureMenu() {

      final JMenu configureMenu = new JMenu("Configure");
      configureMenu.setMnemonic('c');
      configureMenu.add(createConfigureSave());
      configureMenu.add(createConfigureReset());
      configureMenu.add(createConfigureMaxRecords());

      return configureMenu;
   }


   protected final JMenuItem createConfigureSave() {

      final JMenuItem result = new JMenuItem("Save");
      result.setMnemonic('s');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            saveConfiguration();
         }
      });

      return result;
   }


   protected final JMenuItem createConfigureReset() {

      final JMenuItem result = new JMenuItem("Reset");
      result.setMnemonic('r');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            resetConfiguration();
         }
      });

      return result;
   }


   protected final JMenuItem createConfigureMaxRecords() {

      final JMenuItem result = new JMenuItem("Set Max Number of Records");
      result.setMnemonic('m');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            setMaxRecordConfiguration();
         }
      });

      return result;
   }


   protected final void saveConfiguration() {

      _configurationManager.save();
   }


   protected final void resetConfiguration() {

      _configurationManager.reset();
   }


   protected final void setMaxRecordConfiguration() {

      final LogFactor5InputDialog inputDialog = new LogFactor5InputDialog(
              _logMonitorFrame, "Set Max Number of Records", "", 10);

      final String temp = inputDialog.getText();

      if (temp != null) {
         try {
            setMaxNumberOfLogRecords(Integer.parseInt(temp));
         } catch (final NumberFormatException e) {
            new LogFactor5ErrorDialog(_logMonitorFrame,
                    '\'' + temp + "' is an invalid parameter.\nPlease try again.");
            setMaxRecordConfiguration();
         }
      }
   }


   protected final JMenu createHelpMenu() {

      final JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic('h');
      helpMenu.add(createHelpProperties());
      return helpMenu;
   }


   protected final JMenuItem createHelpProperties() {

      final String title = "LogFactor5 Properties";
      final JMenuItem result = new JMenuItem(title);
      result.setMnemonic('l');
      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            showPropertiesDialog(title);
         }
      });
      return result;
   }


   protected final void showPropertiesDialog(final String title) {

      JOptionPane.showMessageDialog(
              _logMonitorFrame,
              _displayedLogBrokerProperties.toArray(),
              title,
              JOptionPane.PLAIN_MESSAGE
      );
   }


   protected final JMenu createEditMenu() {

      final JMenu editMenu = new JMenu("Edit");
      editMenu.setMnemonic('e');
      editMenu.add(createEditFindMI());
      editMenu.add(createEditFindNextMI());
      editMenu.addSeparator();
      editMenu.add(createEditSortNDCMI());
      editMenu.add(createEditRestoreAllNDCMI());
      return editMenu;
   }


   protected final JMenuItem createEditFindNextMI() {

      final JMenuItem editFindNextMI = new JMenuItem("Find Next");
      editFindNextMI.setMnemonic('n');
      editFindNextMI.setAccelerator(KeyStroke.getKeyStroke("F3"));
      editFindNextMI.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            findSearchText();
         }
      });
      return editFindNextMI;
   }


   protected final JMenuItem createEditFindMI() {

      final JMenuItem editFindMI = new JMenuItem("Find");
      editFindMI.setMnemonic('f');
      editFindMI.setAccelerator(KeyStroke.getKeyStroke("control F"));

      editFindMI.addActionListener(
              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    final String inputValue =
                            JOptionPane.showInputDialog(
                                    _logMonitorFrame,
                                    "Find text: ",
                                    "Search Record Messages",
                                    JOptionPane.QUESTION_MESSAGE
                            );
                    setSearchText(inputValue);
                    findSearchText();
                 }
              }

      );
      return editFindMI;
   }


   // Added version 1.2 - Allows users to Sort Log Records by an
   // NDC text filter. A new LogRecordFilter was created to
   // sort the records.
   protected final JMenuItem createEditSortNDCMI() {

      final JMenuItem editSortNDCMI = new JMenuItem("Sort by NDC");
      editSortNDCMI.setMnemonic('s');
      editSortNDCMI.addActionListener(
              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    final String inputValue =
                            JOptionPane.showInputDialog(
                                    _logMonitorFrame,
                                    "Sort by this NDC: ",
                                    "Sort Log Records by NDC",
                                    JOptionPane.QUESTION_MESSAGE
                            );
                    setNDCTextFilter(inputValue);
                    sortByNDC();
                    _table.getFilteredLogTableModel().refresh();
                    updateStatusLabel();
                 }
              }

      );
      return editSortNDCMI;
   }


   // Added in version 1.2 - Resets the LogRecordFilter back to default
   // filter.
   protected final JMenuItem createEditRestoreAllNDCMI() {

      final JMenuItem editRestoreAllNDCMI = new JMenuItem("Restore all NDCs");
      editRestoreAllNDCMI.setMnemonic('r');
      editRestoreAllNDCMI.addActionListener(
              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    _table.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());
                    // reset the text filter
                    setNDCTextFilter("");
                    _table.getFilteredLogTableModel().refresh();
                    updateStatusLabel();
                 }
              }
      );
      return editRestoreAllNDCMI;
   }


   protected final JToolBar createToolBar() {

      final JToolBar tb = new JToolBar();
      tb.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
      final JComboBox fontCombo = new JComboBox();
      final JComboBox fontSizeCombo = new JComboBox();
      _fontSizeCombo = fontSizeCombo;

      ClassLoader cl = this.getClass().getClassLoader();
      if (cl == null) {
         cl = ClassLoader.getSystemClassLoader();
      }
      final URL newIconURL = cl.getResource("org/apache/log4j/lf5/viewer/" +
              "images/channelexplorer_new.gif");

      ImageIcon newIcon = null;

      if (newIconURL != null) {
         newIcon = new ImageIcon(newIconURL);
      }

      final JButton newButton = new JButton("Clear Log Table");

      if (newIcon != null) {
         newButton.setIcon(newIcon);
      }

      newButton.setToolTipText("Clear Log Table.");
      //newButton.setBorder(BorderFactory.createEtchedBorder());

      newButton.addActionListener(
              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    _table.clearLogRecords();
                    _categoryExplorerTree.getExplorerModel().resetAllNodeCounts();
                    updateStatusLabel();
                    clearDetailTextArea();
                    LogRecord.resetSequenceNumber();
                 }
              }
      );

      final Toolkit tk = Toolkit.getDefaultToolkit();
      // This will actually grab all the fonts

      final String[] fonts;

      if (_loadSystemFonts) {
         fonts = GraphicsEnvironment.
                 getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      } else {
         fonts = tk.getFontList();
      }

      for (final String font : fonts) {
         fontCombo.addItem(font);
      }

      fontCombo.setSelectedItem(_fontName);

      fontCombo.addActionListener(

              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    final JComboBox box = (JComboBox) e.getSource();
                    final String font = (String) box.getSelectedItem();
                    _table.setFont(new Font(font, Font.PLAIN, _fontSize));
                    _fontName = font;
                 }
              }
      );

      fontSizeCombo.addItem("8");
      fontSizeCombo.addItem("9");
      fontSizeCombo.addItem("10");
      fontSizeCombo.addItem("12");
      fontSizeCombo.addItem("14");
      fontSizeCombo.addItem("16");
      fontSizeCombo.addItem("18");
      fontSizeCombo.addItem("24");

      fontSizeCombo.setSelectedItem(String.valueOf(_fontSize));
      fontSizeCombo.addActionListener(
              new ActionListener() {

                 public void actionPerformed(final ActionEvent e) {

                    final JComboBox box = (JComboBox) e.getSource();
                    final String size = (String) box.getSelectedItem();
                    final int s = Integer.valueOf(size);

                    setFontSizeSilently(s);
                    refreshDetailTextArea();
                    _fontSize = s;
                 }
              }
      );

      tb.add(new JLabel(" Font: "));
      tb.add(fontCombo);
      tb.add(fontSizeCombo);
      tb.addSeparator();
      tb.addSeparator();
      tb.add(newButton);

      newButton.setAlignmentY(0.5f);
      newButton.setAlignmentX(0.5f);

      fontCombo.setMaximumSize(fontCombo.getPreferredSize());
      fontSizeCombo.setMaximumSize(
              fontSizeCombo.getPreferredSize());

      return tb;
   }

//    protected void setView(String viewString, LogTable table) {
//        if (STANDARD_VIEW.equals(viewString)) {
//            table.setStandardView();
//        } else if (COMPACT_VIEW.equals(viewString)) {
//            table.setCompactView();
//        } else if (DETAILED_VIEW.equals(viewString)) {
//            table.setDetailedView();
//        } else {
//            String message = viewString + "does not match a supported view.";
//            throw new IllegalArgumentException(message);
//        }
//        _currentView = viewString;
//    }


   protected final void setView(final String viewString, final LogTable table) {

      if (DETAILED_VIEW.equals(viewString)) {
         table.setDetailedView();
      } else {
         final String message = viewString + "does not match a supported view.";
         throw new IllegalArgumentException(message);
      }
      _currentView = viewString;
   }


   protected JComboBox createLogLevelCombo() {

      final JComboBox result = new JComboBox();
      final Iterator levels = getLogLevels();
      while (levels.hasNext()) {
         result.addItem(levels.next());
      }
      result.setSelectedItem(_leastSevereDisplayedLogLevel);

      result.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent e) {

            final JComboBox box = (JComboBox) e.getSource();
            final LogLevel level = (LogLevel) box.getSelectedItem();
            setLeastSevereDisplayedLogLevel(level);
         }
      });
      result.setMaximumSize(result.getPreferredSize());
      return result;
   }


   protected final void setLeastSevereDisplayedLogLevel(final LogLevel level) {

      if (level == null || _leastSevereDisplayedLogLevel == level) {
         return; // nothing to do
      }
      _leastSevereDisplayedLogLevel = level;
      _table.getFilteredLogTableModel().refresh();
      updateStatusLabel();
   }


   /**
    * Ensures that the Table's ScrollPane Viewport will "track" with updates to the Table.  When the vertical scroll bar
    * is at its bottom anchor and tracking is enabled then viewport will stay at the bottom most point of the component.
    * The purpose of this feature is to allow a developer to watch the table as messages arrive and not have to scroll
    * after each new message arrives. When the vertical scroll bar is at any other location, then no tracking will
    * happen.
    *
    * @deprecated tracking is now done automatically.
    */
   protected void trackTableScrollPane() {
      // do nothing
   }


   protected final void centerFrame(final JFrame frame) {

      final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      final Dimension comp = frame.getSize();

      frame.setLocation((screen.width - comp.width) / 2,
              (screen.height - comp.height) / 2);

   }


   /**
    * Uses a JFileChooser to select a file to opened with the LF5 GUI.
    */
   protected final void requestOpen() {

      final JFileChooser chooser;

      if (_fileLocation == null) {
         chooser = new JFileChooser();
      } else {
         chooser = new JFileChooser(_fileLocation);
      }

      final int returnVal = chooser.showOpenDialog(_logMonitorFrame);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final File f = chooser.getSelectedFile();
         if (loadLogFile(f)) {
            _fileLocation = chooser.getSelectedFile();
            _mruFileManager.set(f);
            updateMRUList();
         }
      }
   }


   /**
    * Uses a Dialog box to accept a URL to a file to be opened with the LF5 GUI.
    */
   protected final void requestOpenURL() {

      final LogFactor5InputDialog inputDialog = new LogFactor5InputDialog(
              _logMonitorFrame, "Open URL", "URL:");
      String temp = inputDialog.getText();

      if (temp != null) {
         if (!temp.contains("://")) {
            temp = "http://" + temp;
         }

         try {
            final URL url = new URL(temp);
            if (loadLogFile(url)) {
               _mruFileManager.set(url);
               updateMRUList();
            }
         } catch (final MalformedURLException e) {
            new LogFactor5ErrorDialog(_logMonitorFrame, "Error reading URL.");
         }
      }
   }


   /**
    * Removes old file list and creates a new file list with the updated MRU list.
    */
   protected final void updateMRUList() {

      final JMenu menu = _logMonitorFrame.getJMenuBar().getMenu(0);
      menu.removeAll();
      menu.add(createOpenMI());
      menu.add(createOpenURLMI());
      menu.addSeparator();
      menu.add(createCloseMI());
      createMRUFileListMI(menu);
      menu.addSeparator();
      menu.add(createExitMI());
   }


   protected final void requestClose() {

      _callSystemExitOnClose = false;
      closeAfterConfirm();
   }


   /**
    * Opens a file in the MRU list.
    */
   protected final void requestOpenMRU(final ActionEvent e) {

      String file = e.getActionCommand();
      final StringTokenizer st = new StringTokenizer(file);
      final String num = st.nextToken().trim();
      file = st.nextToken("\n");

      try {
         final int index = Integer.parseInt(num) - 1;

         final InputStream in = _mruFileManager.getInputStream(index);
         final LogFileParser lfp = new LogFileParser(in);
         lfp.parse(this);

         _mruFileManager.moveToTop(index);
         updateMRUList();

      } catch (final Exception me) {
         new LogFactor5ErrorDialog(_logMonitorFrame, "Unable to load file " + file);
      }

   }


   protected final void requestExit() {

      _mruFileManager.save();
      _callSystemExitOnClose = true;
      closeAfterConfirm();
   }


   protected final void closeAfterConfirm() {

      final StringBuilder message = new StringBuilder(150);

      if (_callSystemExitOnClose) {
         message.append("Are you sure you want to exit?\n");
         message.append("This will shut down the Virtual Machine.\n");
      } else {
         message.append("Are you sure you want to close the logging ");
         message.append("console?\n");
         message.append("(Note: This will not shut down the Virtual Machine,\n");
         message.append("or the Swing event thread.)");
      }

      String title =
              "Are you sure you want to dispose of the Logging Console?";

      if (_callSystemExitOnClose) {
         title = "Are you sure you want to exit?";
      }
      final int value = JOptionPane.showConfirmDialog(
              _logMonitorFrame,
              message.toString(),
              title,
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE,
              null
      );

      if (value == JOptionPane.OK_OPTION) {
         dispose();
      }
   }


   protected final Iterator getLogLevels() {

      return _levels.iterator();
   }


   protected final Iterator getLogTableColumns() {

      return _columns.iterator();
   }


   /**
    * Loads and parses a log file.
    */
   protected final boolean loadLogFile(final File file) {

      boolean ok = false;
      try {
         final LogFileParser lfp = new LogFileParser(file);
         lfp.parse(this);
         ok = true;
      } catch (final IOException e) {
         new LogFactor5ErrorDialog(_logMonitorFrame, "Error reading " + file.getName());
      }

      return ok;
   }


   /**
    * Loads a parses a log file running on a server.
    */
   protected final boolean loadLogFile(final URL url) {

      boolean ok = false;
      try {
         final LogFileParser lfp = new LogFileParser(url.openStream());
         lfp.parse(this);
         ok = true;
      } catch (final IOException e) {
         new LogFactor5ErrorDialog(_logMonitorFrame, "Error reading URL:" + url.getFile());
      }
      return ok;
   }
   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

   static final class LogBrokerMonitorWindowAdaptor extends WindowAdapter {

      protected final LogBrokerMonitor _monitor;


      LogBrokerMonitorWindowAdaptor(final LogBrokerMonitor monitor) {

         _monitor = monitor;
      }


      public final void windowClosing(final WindowEvent ev) {

         _monitor.requestClose();
      }
   }
}


