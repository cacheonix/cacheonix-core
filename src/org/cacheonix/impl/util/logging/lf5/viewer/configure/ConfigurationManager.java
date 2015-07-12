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
package org.cacheonix.impl.util.logging.lf5.viewer.configure;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cacheonix.impl.util.logging.lf5.LogLevel;
import org.cacheonix.impl.util.logging.lf5.LogLevelFormatException;
import org.cacheonix.impl.util.logging.lf5.viewer.LogBrokerMonitor;
import org.cacheonix.impl.util.logging.lf5.viewer.LogTable;
import org.cacheonix.impl.util.logging.lf5.viewer.LogTableColumn;
import org.cacheonix.impl.util.logging.lf5.viewer.LogTableColumnFormatException;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryExplorerModel;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryExplorerTree;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryNode;
import org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer.CategoryPath;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>ConfigurationManager handles the storage and retrival of the state of the CategoryExplorer
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public final class ConfigurationManager {

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------
   private static final String CONFIG_FILE_NAME = "lf5_configuration.xml";

   private static final String NAME = "name";

   private static final String PATH = "path";

   private static final String SELECTED = "selected";

   private static final String EXPANDED = "expanded";

   private static final String CATEGORY = "category";

   private static final String FIRST_CATEGORY_NAME = "Categories";

   private static final String LEVEL = "level";

   private static final String COLORLEVEL = "colorlevel";

   private static final String RED = "red";

   private static final String GREEN = "green";

   private static final String BLUE = "blue";

   private static final String COLUMN = "column";

   private static final String NDCTEXTFILTER = "searchtext";
   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------
   private LogBrokerMonitor _monitor = null;

   private LogTable _table = null;


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------
   public ConfigurationManager(final LogBrokerMonitor monitor, final LogTable table) {

      _monitor = monitor;
      _table = table;
      load();
   }
   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   public final void save() {

      final CategoryExplorerModel model = _monitor.getCategoryExplorerTree().getExplorerModel();
      final CategoryNode root = model.getRootCategoryNode();

      final StringBuffer xml = new StringBuffer(2048);
      openXMLDocument(xml);
      openConfigurationXML(xml);
      processLogRecordFilter(_monitor.getNDCTextFilter(), xml);
      processLogLevels(_monitor.getLogLevelMenuItems(), xml);
      processLogLevelColors(_monitor.getLogLevelMenuItems(),
              LogLevel.getLogLevelColorMap(), xml);
      processLogTableColumns(LogTableColumn.getLogTableColumns(), xml);
      processConfigurationNode(root, xml);
      closeConfigurationXML(xml);
      store(xml.toString());
   }


   public final void reset() {

      deleteConfigurationFile();
      collapseTree();
      selectAllNodes();
   }


   public static String treePathToString(final TreePath path) {
      // count begins at one so as to not include the 'Categories' - root category
      final StringBuilder sb = new StringBuilder(16);
      CategoryNode n = null;
      final Object[] objects = path.getPath();
      for (int i = 1; i < objects.length; i++) {
         n = (CategoryNode) objects[i];
         if (i > 1) {
            sb.append('.');
         }
         sb.append(n.getTitle());
      }
      return sb.toString();
   }


   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------
   protected final void load() {

      final File file = new File(getFilename());
      if (file.exists()) {
         try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.
                    newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document doc = docBuilder.parse(file);
            processRecordFilter(doc);
            processCategories(doc);
            processLogLevels(doc);
            processLogLevelColors(doc);
            processLogTableColumns(doc);
         } catch (final Exception e) {
            // ignore all error and just continue as if there was no
            // configuration xml file but do report a message
            System.err.println("Unable process configuration file at " +
                    getFilename() + ". Error Message=" + e.getMessage());
         }
      }

   }


   // Added in version 1.2 - reads in the NDC text filter from the
   // xml configuration file.  If the value of the filter is not null
   // or an empty string ("") then the manager will set the LogBrokerMonitor's
   // LogRecordFilter to use the NDC LogRecordFilter.  Otherwise, the
   // LogBrokerMonitor will use the default LogRecordFilter.
   protected final void processRecordFilter(final Document doc) {

      final NodeList nodeList = doc.getElementsByTagName(NDCTEXTFILTER);

      // there is only one value stored
      final Node n = nodeList.item(0);
      // add check for backwards compatibility  as this feature was added in
      // version 1.2
      if (n == null) {
         return;
      }

      final NamedNodeMap map = n.getAttributes();
      final String text = getValue(map, NAME);

      if (text == null || text.isEmpty()) {
         return;
      }
      _monitor.setNDCLogRecordFilter(text);
   }


   protected final void processCategories(final Document doc) {

      final CategoryExplorerTree tree = _monitor.getCategoryExplorerTree();
      final CategoryExplorerModel model = tree.getExplorerModel();
      final NodeList nodeList = doc.getElementsByTagName(CATEGORY);

      // determine where the starting node is
      NamedNodeMap map = nodeList.item(0).getAttributes();
      final int j = getValue(map, NAME).equalsIgnoreCase(FIRST_CATEGORY_NAME) ? 1 : 0;
      // iterate backwards throught the nodeList so that expansion of the
      // list can occur
      for (int i = nodeList.getLength() - 1; i >= j; i--) {
         final Node n = nodeList.item(i);
         map = n.getAttributes();
         final CategoryNode chnode = model.addCategory(new CategoryPath(getValue(map, PATH)));
         chnode.setSelected("true".equalsIgnoreCase(getValue(map, SELECTED)));
         if ("true".equalsIgnoreCase(getValue(map, EXPANDED))) {
            tree.expandPath(model.getTreePathToRoot(chnode));
         }
      }
   }


   protected final void processLogLevels(final Document doc) {

      final NodeList nodeList = doc.getElementsByTagName(LEVEL);
      final Map menuItems = _monitor.getLogLevelMenuItems();

      for (int i = 0; i < nodeList.getLength(); i++) {
         final Node n = nodeList.item(i);
         final NamedNodeMap map = n.getAttributes();
         final String name = getValue(map, NAME);
         try {
            final JCheckBoxMenuItem item =
                    (JCheckBoxMenuItem) menuItems.get(LogLevel.valueOf(name));
            item.setSelected("true".equalsIgnoreCase(getValue(map, SELECTED)));
         } catch (final LogLevelFormatException ignored) {
            // ignore it will be on by default.
         }
      }
   }


   protected final void processLogLevelColors(final Document doc) {

      final NodeList nodeList = doc.getElementsByTagName(COLORLEVEL);
      LogLevel.getLogLevelColorMap();

      for (int i = 0; i < nodeList.getLength(); i++) {
         final Node n = nodeList.item(i);
         // check for backwards compatibility since this feature was added
         // in version 1.3
         if (n == null) {
            return;
         }

         final NamedNodeMap map = n.getAttributes();
         final String name = getValue(map, NAME);
         try {
            final LogLevel level = LogLevel.valueOf(name);
            final int red = Integer.parseInt(getValue(map, RED));
            final int green = Integer.parseInt(getValue(map, GREEN));
            final int blue = Integer.parseInt(getValue(map, BLUE));
            final Color c = new Color(red, green, blue);
            if (level != null) {
               level.setLogLevelColorMap(level, c);
            }

         } catch (final LogLevelFormatException ignored) {
            // ignore it will be on by default.
         }
      }
   }


   protected final void processLogTableColumns(final Document doc) {

      final NodeList nodeList = doc.getElementsByTagName(COLUMN);
      final Map menuItems = _monitor.getLogTableColumnMenuItems();
      final List selectedColumns = new ArrayList(10);
      for (int i = 0; i < nodeList.getLength(); i++) {
         final Node n = nodeList.item(i);
         // check for backwards compatibility since this feature was added
         // in version 1.3
         if (n == null) {
            return;
         }
         final NamedNodeMap map = n.getAttributes();
         final String name = getValue(map, NAME);
         try {
            final LogTableColumn column = LogTableColumn.valueOf(name);
            final JCheckBoxMenuItem item =
                    (JCheckBoxMenuItem) menuItems.get(column);
            item.setSelected("true".equalsIgnoreCase(getValue(map, SELECTED)));

            if (item.isSelected()) {
               selectedColumns.add(column);
            }
         } catch (final LogTableColumnFormatException ignored) {
            // ignore it will be on by default.
         }

         if (selectedColumns.isEmpty()) {
            _table.setDetailedView();
         } else {
            _table.setView(selectedColumns);
         }

      }
   }


   protected final String getValue(final NamedNodeMap map, final String attr) {

      final Node n = map.getNamedItem(attr);
      return n.getNodeValue();
   }


   protected final void collapseTree() {
      // collapse everything except the first category
      final CategoryExplorerTree tree = _monitor.getCategoryExplorerTree();
      for (int i = tree.getRowCount() - 1; i > 0; i--) {
         tree.collapseRow(i);
      }
   }


   protected final void selectAllNodes() {

      final CategoryExplorerModel model = _monitor.getCategoryExplorerTree().getExplorerModel();
      final CategoryNode root = model.getRootCategoryNode();
      final Enumeration all = root.breadthFirstEnumeration();
      CategoryNode n = null;
      while (all.hasMoreElements()) {
         n = (CategoryNode) all.nextElement();
         n.setSelected(true);
      }
   }


   protected final void store(final String s) {

      try {
         final PrintWriter writer = new PrintWriter(new FileWriter(getFilename()));
         writer.print(s);
         writer.close();
      } catch (final IOException e) {
         // do something with this error.
         e.printStackTrace();
      }

   }


   protected final void deleteConfigurationFile() {

      try {
         final File f = new File(getFilename());
         if (f.exists()) {
            f.delete();
         }
      } catch (final SecurityException e) {
         System.err.println("Cannot delete " + getFilename() +
                 " because a security violation occurred: " + e.toString());
      }
   }


   protected final String getFilename() {

      final String home = System.getProperty("user.home");
      final String sep = System.getProperty("file.separator");

      return home + sep + "lf5" + sep + CONFIG_FILE_NAME;
   }


   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------
   private void processConfigurationNode(final CategoryNode node, final StringBuffer xml) {

      final CategoryExplorerModel model = _monitor.getCategoryExplorerTree().getExplorerModel();

      final Enumeration all = node.breadthFirstEnumeration();
      CategoryNode n = null;
      while (all.hasMoreElements()) {
         n = (CategoryNode) all.nextElement();
         exportXMLElement(n, model.getTreePathToRoot(n), xml);
      }

   }


   private void processLogLevels(final Map logLevelMenuItems, final StringBuffer xml) {

      xml.append("\t<loglevels>\r\n");
      for (final Object o : logLevelMenuItems.entrySet()) {
         final Map.Entry entry = (Map.Entry) o;
         final LogLevel level = (LogLevel) entry.getKey();
         final JCheckBoxMenuItem item = (JCheckBoxMenuItem) entry.getValue();
         exportLogLevelXMLElement(level.getLabel(), item.isSelected(), xml);
      }

      xml.append("\t</loglevels>\r\n");
   }


   private void processLogLevelColors(final Map logLevelMenuItems, final Map logLevelColors,
                                      final StringBuffer xml) {

      xml.append("\t<loglevelcolors>\r\n");
      // iterate through the list of log levels being used (log4j, jdk1.4, custom levels)
      for (final Object o : logLevelMenuItems.keySet()) {
         final LogLevel level = (LogLevel) o;
         // for each level, get the associated color from the log level color map
         final Color color = (Color) logLevelColors.get(level);
         exportLogLevelColorXMLElement(level.getLabel(), color, xml);
      }

      xml.append("\t</loglevelcolors>\r\n");
   }


   private void processLogTableColumns(final List logTableColumnMenuItems, final StringBuffer xml) {

      xml.append("\t<logtablecolumns>\r\n");
      for (final Object logTableColumnMenuItem : logTableColumnMenuItems) {
         final LogTableColumn column = (LogTableColumn) logTableColumnMenuItem;
         final JCheckBoxMenuItem item = _monitor.getTableColumnMenuItem(column);
         exportLogTableColumnXMLElement(column.getLabel(), item.isSelected(), xml);
      }

      xml.append("\t</logtablecolumns>\r\n");
   }


   // Added in version 1.2 - stores the NDC text filter in the xml file
   // for future use.
   private void processLogRecordFilter(final String text, final StringBuffer xml) {

      xml.append("\t<").append(NDCTEXTFILTER).append(' ');
      xml.append(NAME).append("=\"").append(text).append('\"');
      xml.append("/>\r\n");
   }


   private void openXMLDocument(final StringBuffer xml) {

      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
   }


   private void openConfigurationXML(final StringBuffer xml) {

      xml.append("<configuration>\r\n");
   }


   private void closeConfigurationXML(final StringBuffer xml) {

      xml.append("</configuration>\r\n");
   }


   private void exportXMLElement(final CategoryNode node, final TreePath path,
                                 final StringBuffer xml) {

      final CategoryExplorerTree tree = _monitor.getCategoryExplorerTree();

      xml.append("\t<").append(CATEGORY).append(' ');
      xml.append(NAME).append("=\"").append(node.getTitle()).append("\" ");
      xml.append(PATH).append("=\"").append(treePathToString(path)).append("\" ");
      xml.append(EXPANDED).append("=\"").append(tree.isExpanded(path)).append("\" ");
      xml.append(SELECTED).append("=\"").append(node.isSelected()).append("\"/>\r\n");
   }


   private void exportLogLevelXMLElement(final String label, final boolean selected,
                                         final StringBuffer xml) {

      xml.append("\t\t<").append(LEVEL).append(' ').append(NAME);
      xml.append("=\"").append(label).append("\" ");
      xml.append(SELECTED).append("=\"").append(selected);
      xml.append("\"/>\r\n");
   }


   private void exportLogLevelColorXMLElement(final String label, final Color color,
                                              final StringBuffer xml) {

      xml.append("\t\t<").append(COLORLEVEL).append(' ').append(NAME);
      xml.append("=\"").append(label).append("\" ");
      xml.append(RED).append("=\"").append(color.getRed()).append("\" ");
      xml.append(GREEN).append("=\"").append(color.getGreen()).append("\" ");
      xml.append(BLUE).append("=\"").append(color.getBlue());
      xml.append("\"/>\r\n");
   }


   private void exportLogTableColumnXMLElement(final String label, final boolean selected,
                                               final StringBuffer xml) {

      xml.append("\t\t<").append(COLUMN).append(' ').append(NAME);
      xml.append("=\"").append(label).append("\" ");
      xml.append(SELECTED).append("=\"").append(selected);
      xml.append("\"/>\r\n");
   }
   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






