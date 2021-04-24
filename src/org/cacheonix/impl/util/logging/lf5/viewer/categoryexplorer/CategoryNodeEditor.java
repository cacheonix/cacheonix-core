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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * CategoryNodeEditor
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryNodeEditor extends CategoryAbstractCellEditor {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   private final CategoryNodeEditorRenderer _renderer;
   private CategoryNode _lastEditedNode = null;
   private final JCheckBox _checkBox;
   private final CategoryExplorerModel _categoryModel;
   private JTree _tree = null;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryNodeEditor(final CategoryExplorerModel model) {
      _renderer = new CategoryNodeEditorRenderer();
      _checkBox = _renderer.getCheckBox();
      _categoryModel = model;

      _checkBox.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            _categoryModel.update(_lastEditedNode, _checkBox.isSelected());
            stopCellEditing();
         }
      });

      _renderer.addMouseListener(new MouseAdapter() {
         public void mousePressed(final MouseEvent e) {

            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
               showPopup(_lastEditedNode, e.getX(), e.getY());
            }
            stopCellEditing();
         }
      });
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   public final Component getTreeCellEditorComponent(final JTree tree, final Object value,
                                                     final boolean selected, final boolean expanded,
                                                     final boolean leaf, final int row) {
      _lastEditedNode = (CategoryNode) value;
      _tree = tree;

      return _renderer.getTreeCellRendererComponent(tree,
              value, selected, expanded,
              leaf, row, true);
      // hasFocus ignored
   }


   public final Object getCellEditorValue() {
      return _lastEditedNode.getUserObject();
   }
   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   private final JMenuItem createPropertiesMenuItem(final CategoryNode node) {
      final JMenuItem result = new JMenuItem("Properties");
      result.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            showPropertiesDialog(node);
         }
      });
      return result;
   }


   private final void showPropertiesDialog(final CategoryNode node) {
      JOptionPane.showMessageDialog(
              _tree,
              getDisplayedProperties(node),
              "Category Properties: " + node.getTitle(),
              JOptionPane.PLAIN_MESSAGE
      );
   }


   private final Object getDisplayedProperties(final CategoryNode node) {
      final ArrayList result = new ArrayList(5);
      result.add("Category: " + node.getTitle());
      if (node.hasFatalRecords()) {
         result.add("Contains at least one fatal LogRecord.");
      }
      if (node.hasFatalChildren()) {
         result.add("Contains descendants with a fatal LogRecord.");
      }
      result.add("LogRecords in this category alone: " +
              node.getNumberOfContainedRecords());
      result.add("LogRecords in descendant categories: " +
              node.getNumberOfRecordsFromChildren());
      result.add("LogRecords in this category including descendants: " +
              node.getTotalNumberOfRecords());
      return result.toArray();
   }


   private final void showPopup(final CategoryNode node, final int x, final int y) {
      final JPopupMenu popup = new JPopupMenu();
      popup.setSize(150, 400);
      //
      // Configure the Popup
      //
      if (node.getParent() == null) {
         popup.add(createRemoveMenuItem());
         popup.addSeparator();
      }
      popup.add(createSelectDescendantsMenuItem(node));
      popup.add(createUnselectDescendantsMenuItem(node));
      popup.addSeparator();
      popup.add(createExpandMenuItem(node));
      popup.add(createCollapseMenuItem(node));
      popup.addSeparator();
      popup.add(createPropertiesMenuItem(node));
      popup.show(_renderer, x, y);
   }


   private final JMenuItem createSelectDescendantsMenuItem(final CategoryNode node) {
      final JMenuItem selectDescendants =
              new JMenuItem("Select All Descendant Categories");
      selectDescendants.addActionListener(
              new ActionListener() {
                 public void actionPerformed(final ActionEvent e) {
                    _categoryModel.setDescendantSelection(node, true);
                 }
              }
      );
      return selectDescendants;
   }


   private final JMenuItem createUnselectDescendantsMenuItem(final CategoryNode node) {
      final JMenuItem unselectDescendants =
              new JMenuItem("Deselect All Descendant Categories");
      unselectDescendants.addActionListener(

              new ActionListener() {
                 public void actionPerformed(final ActionEvent e) {
                    _categoryModel.setDescendantSelection(node, false);
                 }
              }

      );
      return unselectDescendants;
   }


   private final JMenuItem createExpandMenuItem(final CategoryNode node) {
      final JMenuItem result = new JMenuItem("Expand All Descendant Categories");
      result.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            expandDescendants(node);
         }
      });
      return result;
   }


   private final JMenuItem createCollapseMenuItem(final CategoryNode node) {
      final JMenuItem result = new JMenuItem("Collapse All Descendant Categories");
      result.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            collapseDescendants(node);
         }
      });
      return result;
   }


   /**
    * This featured was moved from the LogBrokerMonitor class to the CategoryNodeExplorer so that the Category tree
    * could be pruned from the Category Explorer popup menu. This menu option only appears when a user right clicks on
    * the Category parent node.
    * <p/>
    * See removeUnusedNodes()
    */
   private final JMenuItem createRemoveMenuItem() {
      final JMenuItem result = new JMenuItem("Remove All Empty Categories");
      result.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            while (removeUnusedNodes() > 0) { // NOPMD
            }
         }
      });
      return result;
   }


   private final void expandDescendants(final CategoryNode node) {
      final Enumeration descendants = node.depthFirstEnumeration();
      while (descendants.hasMoreElements()) {
         final CategoryNode current = (CategoryNode) descendants.nextElement();
         expand(current);
      }
   }


   private final void collapseDescendants(final CategoryNode node) {
      final Enumeration descendants = node.depthFirstEnumeration();
      while (descendants.hasMoreElements()) {
         final CategoryNode current = (CategoryNode) descendants.nextElement();
         collapse(current);
      }
   }


   /**
    * Removes any inactive nodes from the Category tree.
    */
   private final int removeUnusedNodes() {
      int count = 0;
      final CategoryNode root = _categoryModel.getRootCategoryNode();
      final Enumeration enumeration = root.depthFirstEnumeration();
      while (enumeration.hasMoreElements()) {
         final CategoryNode node = (CategoryNode) enumeration.nextElement();
         if (node.isLeaf() && node.getNumberOfContainedRecords() == 0
                 && node.getParent() != null) {
            _categoryModel.removeNodeFromParent(node);
            count++;
         }
      }

      return count;
   }


   private final void expand(final CategoryNode node) {
      _tree.expandPath(getTreePath(node));
   }


   private final TreePath getTreePath(final CategoryNode node) {
      return new TreePath(node.getPath());
   }


   private final void collapse(final CategoryNode node) {
      _tree.collapsePath(getTreePath(node));
   }

   //-----------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}
