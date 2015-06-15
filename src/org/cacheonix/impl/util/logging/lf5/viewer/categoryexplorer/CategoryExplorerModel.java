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
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.cacheonix.impl.util.logging.lf5.LogRecord;

/**
 * CategoryExplorerModel
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brent Sprecher
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryExplorerModel extends DefaultTreeModel {

   private static final long serialVersionUID = -3413887384316015901L;

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   protected ActionListener _listener = null;

   protected final ActionEvent _event = new ActionEvent(this,
           ActionEvent.ACTION_PERFORMED,
           "Nodes Selection changed");

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryExplorerModel(final CategoryNode node) {

      super(node);
   }
   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   public final void addLogRecord(final LogRecord lr) {

      final CategoryPath path = new CategoryPath(lr.getCategory());
      addCategory(path); // create category path if it is new
      final CategoryNode node = getCategoryNode(path);
      node.addRecord(); // update category node
      if (lr.isFatal()) {
         final TreeNode[] nodes = getPathToRoot(node);
         final int len = nodes.length;

         // i = 0 gives root node
         // skip node and root, loop through "parents" in between
         for (int i = 1; i < len - 1; i++) {
            final CategoryNode parent = (CategoryNode) nodes[i];
            parent.setHasFatalChildren(true);
            nodeChanged(parent);
         }
         node.setHasFatalRecords(true);
         nodeChanged(node);
      }
   }


   public final CategoryNode getRootCategoryNode() {

      return (CategoryNode) getRoot();
   }


   public CategoryNode getCategoryNode(final String category) {

      final CategoryPath path = new CategoryPath(category);
      return getCategoryNode(path);
   }


   /**
    * returns null if no CategoryNode exists.
    */
   public final CategoryNode getCategoryNode(final CategoryPath path) {

      CategoryNode parent = (CategoryNode) getRoot(); // Start condition.

      for (int i = 0; i < path.size(); i++) {
         final CategoryElement element = path.categoryElementAt(i);

         // If the two nodes have matching titles they are considered equal.
         final Enumeration children = parent.children();

         boolean categoryAlreadyExists = false;
         while (children.hasMoreElements()) {
            final CategoryNode node = (CategoryNode) children.nextElement();
            final String title = node.getTitle().toLowerCase();

            final String pathLC = element.getTitle().toLowerCase();
            if (title.equals(pathLC)) {
               categoryAlreadyExists = true;
               // This is now the new parent node.
               parent = node;
               break; // out of the while, and back to the for().
            }
         }

         if (!categoryAlreadyExists) {
            return null; // Didn't find the Node.
         }
      }

      return parent;
   }


   /**
    * @return <code>true</code> if all the nodes in the specified CategoryPath are selected.
    */
   public final boolean isCategoryPathActive(final CategoryPath path) {

      CategoryNode parent = (CategoryNode) getRoot(); // Start condition.
      boolean active = false;

      for (int i = 0; i < path.size(); i++) {
         final CategoryElement element = path.categoryElementAt(i);

         // If the two nodes have matching titles they are considered equal.
         final Enumeration children = parent.children();

         active = false;

         boolean categoryAlreadyExists = false;
         while (children.hasMoreElements()) {
            final CategoryNode node = (CategoryNode) children.nextElement();
            final String title = node.getTitle().toLowerCase();

            final String pathLC = element.getTitle().toLowerCase();
            if (title.equals(pathLC)) {
               categoryAlreadyExists = true;
               // This is now the new parent node.
               parent = node;

               if (parent.isSelected()) {
                  active = true;
               }

               break; // out of the while, and back to the for().
            }
         }

         if (!active || !categoryAlreadyExists) {
            return false;
         }
      }

      return active;
   }


   /**
    * <p>Method altered by Richard Hurst such that it returns the CategoryNode corresponding to the CategoryPath</p>
    *
    * @param path category path.
    * @return CategoryNode
    */
   public final CategoryNode addCategory(final CategoryPath path) {

      CategoryNode parent = (CategoryNode) getRoot(); // Start condition.

      for (int i = 0; i < path.size(); i++) {
         final CategoryElement element = path.categoryElementAt(i);

         // If the two nodes have matching titles they are considered equal.
         final Enumeration children = parent.children();

         boolean categoryAlreadyExists = false;
         while (children.hasMoreElements()) {
            final CategoryNode node = (CategoryNode) children.nextElement();
            final String title = node.getTitle().toLowerCase();

            final String pathLC = element.getTitle().toLowerCase();
            if (title.equals(pathLC)) {
               categoryAlreadyExists = true;
               // This is now the new parent node.
               parent = node;
               break;
            }
         }

         if (!categoryAlreadyExists) {
            // We need to add the node.
            final CategoryNode newNode = new CategoryNode(element.getTitle());

            //This method of adding a new node cause parent roots to be
            // collapsed.
            //parent.add( newNode );
            //reload(parent);

            // This doesn't force the nodes to collapse.
            insertNodeInto(newNode, parent, parent.getChildCount());
            refresh(newNode);

            // The newly added node is now the parent.
            parent = newNode;

         }
      }

      return parent;
   }


   public final void update(final CategoryNode node, final boolean selected) {

      if (node.isSelected() == selected) {
         return; // nothing was changed, nothing to do
      }
      // select parents or deselect children
      if (selected) {
         setParentSelection(node, true);
      } else {
         setDescendantSelection(node, false);
      }
   }


   public final void setDescendantSelection(final CategoryNode node, final boolean selected) {

      final Enumeration descendants = node.depthFirstEnumeration();
      while (descendants.hasMoreElements()) {
         final CategoryNode current = (CategoryNode) descendants.nextElement();
         // does the current node need to be changed?
         if (current.isSelected() != selected) {
            current.setSelected(selected);
            nodeChanged(current);
         }
      }
      notifyActionListeners();
   }


   public final void setParentSelection(final CategoryNode node, final boolean selected) {

      final TreeNode[] nodes = getPathToRoot(node);
      final int len = nodes.length;

      // i = 0 gives root node, i=len-1 gives this node
      // skip the root node
      for (int i = 1; i < len; i++) {
         final CategoryNode parent = (CategoryNode) nodes[i];
         if (parent.isSelected() != selected) {
            parent.setSelected(selected);
            nodeChanged(parent);
         }
      }
      notifyActionListeners();
   }


   public final synchronized void addActionListener(final ActionListener l) {

      _listener = AWTEventMulticaster.add(_listener, l);
   }


   public synchronized void removeActionListener(final ActionListener l) {

      _listener = AWTEventMulticaster.remove(_listener, l);
   }


   public final void resetAllNodeCounts() {

      final Enumeration nodes = getRootCategoryNode().depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
         final CategoryNode current = (CategoryNode) nodes.nextElement();
         current.resetNumberOfContainedRecords();
         nodeChanged(current);
      }
   }


   /**
    * <p>Returns the CategoryPath to the specified CategoryNode</p>
    *
    * @param node The target CategoryNode
    * @return CategoryPath
    */
   public final TreePath getTreePathToRoot(final CategoryNode node) {

      if (node == null) {
         return null;
      }
      return new TreePath(getPathToRoot(node));
   }


   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------
   protected final void notifyActionListeners() {

      if (_listener != null) {
         _listener.actionPerformed(_event);
      }
   }


   /**
    * Fires a nodechanged event on the SwingThread.
    */
   protected final void refresh(final CategoryNode node) {

      SwingUtilities.invokeLater(new Runnable() {

         public void run() {

            nodeChanged(node); // remind the tree to render the new node
         }
      });
   }

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






