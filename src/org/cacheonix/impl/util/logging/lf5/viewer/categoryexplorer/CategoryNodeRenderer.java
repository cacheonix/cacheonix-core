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
package org.cacheonix.impl.util.logging.lf5.viewer.categoryexplorer;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * CategoryNodeRenderer
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class CategoryNodeRenderer extends DefaultTreeCellRenderer {

   private static final long serialVersionUID = -6046702673278595048L;

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   public static final Color FATAL_CHILDREN = new Color(189, 113, 0);

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected final JCheckBox _checkBox = new JCheckBox();

   private final JPanel _panel = new JPanel();

   @SuppressWarnings("StaticNonFinalField")
   private static ImageIcon _sat = null;
//   protected JLabel              _label  = new JLabel();

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryNodeRenderer() {

      _panel.setBackground(UIManager.getColor("Tree.textBackground"));

      if (_sat == null) {
         // Load the satellite image.
         final String resource =
                 "/org/apache/log4j/lf5/viewer/images/channelexplorer_satellite.gif";
         final URL satURL = getClass().getResource(resource);

         _sat = new ImageIcon(satURL);
      }

      setOpaque(false);
      _checkBox.setOpaque(false);
      _panel.setOpaque(false);

      // The flowlayout set to LEFT is very important so that the editor
      // doesn't jump around.
      _panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      _panel.add(_checkBox);
      _panel.add(this);

      setOpenIcon(_sat);
      setClosedIcon(_sat);
      setLeafIcon(_sat);
   }


   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------
   public Component getTreeCellRendererComponent(
           final JTree tree, final Object value,
           final boolean selected, final boolean expanded,
           final boolean leaf, final int row,
           final boolean hasFocus) {

      final CategoryNode node = (CategoryNode) value;
      //FileNode node = (FileNode)value;
      //String s = tree.convertValueToText(value, selected,
      //						   expanded, leaf, row, hasFocus);

      super.getTreeCellRendererComponent(
              tree, value, selected, expanded,
              leaf, row, hasFocus);

      if (row == 0) {
         // Root row -- no check box
         _checkBox.setVisible(false);
      } else {
         _checkBox.setVisible(true);
         _checkBox.setSelected(node.isSelected());
      }
      final String toolTip = buildToolTip(node);
      _panel.setToolTipText(toolTip);
      if (node.hasFatalChildren()) {
         this.setForeground(FATAL_CHILDREN);
      }
      if (node.hasFatalRecords()) {
         this.setForeground(Color.red);
      }

      return _panel;
   }


   public final Dimension getCheckBoxOffset() {

      return new Dimension(0, 0);
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final String buildToolTip(final CategoryNode node) {

      return node.getTitle() + " contains a total of " + node.getTotalNumberOfRecords() + " LogRecords." + " Right-click for more info.";
   }
   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






