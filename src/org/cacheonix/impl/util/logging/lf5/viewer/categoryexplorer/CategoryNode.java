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

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * CategoryNode
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryNode extends DefaultMutableTreeNode {

   private static final long serialVersionUID = 5958994817693177319L;
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected boolean _selected = true;
   protected int _numberOfContainedRecords = 0;
   protected int _numberOfRecordsFromChildren = 0;
   protected boolean _hasFatalChildren = false;
   protected boolean _hasFatalRecords = false;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   /**
    *
    */
   public CategoryNode(final String title) {
      setUserObject(title);
   }


   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------
   public final String getTitle() {
      return (String) getUserObject();
   }


   public final void setSelected(final boolean s) {
      if (s != _selected) {
         _selected = s;
      }
   }


   public final boolean isSelected() {
      return _selected;
   }


   /**
    * @deprecated
    */
   public final void setAllDescendantsSelected() {
      final Enumeration children = children();
      while (children.hasMoreElements()) {
         final CategoryNode node = (CategoryNode) children.nextElement();
         node.setSelected(true);
         node.setAllDescendantsSelected();
      }
   }


   /**
    * @deprecated
    */
   public final void setAllDescendantsDeSelected() {
      final Enumeration children = children();
      while (children.hasMoreElements()) {
         final CategoryNode node = (CategoryNode) children.nextElement();
         node.setSelected(false);
         node.setAllDescendantsDeSelected();
      }
   }


   public final String toString() {
      return getTitle();
   }


   public final boolean equals(final Object obj) {
      if (obj instanceof CategoryNode) {
         final CategoryNode node = (CategoryNode) obj;
         final String tit1 = getTitle().toLowerCase();
         final String tit2 = node.getTitle().toLowerCase();

         if (tit1.equals(tit2)) {
            return true;
         }
      }
      return false;
   }


   public final int hashCode() {
      return getTitle().hashCode();
   }


   public final void addRecord() {
      _numberOfContainedRecords++;
      addRecordToParent();
   }


   public final int getNumberOfContainedRecords() {
      return _numberOfContainedRecords;
   }


   public final void resetNumberOfContainedRecords() {
      _numberOfContainedRecords = 0;
      _numberOfRecordsFromChildren = 0;
      _hasFatalRecords = false;
      _hasFatalChildren = false;
   }


   public final boolean hasFatalRecords() {
      return _hasFatalRecords;
   }


   public final boolean hasFatalChildren() {
      return _hasFatalChildren;
   }


   public final void setHasFatalRecords(final boolean flag) {
      _hasFatalRecords = flag;
   }


   public final void setHasFatalChildren(final boolean flag) {
      _hasFatalChildren = flag;
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final int getTotalNumberOfRecords() {
      return _numberOfRecordsFromChildren + _numberOfContainedRecords;
   }


   /**
    * Passes up the addition from child to parent
    */
   protected final void addRecordFromChild() {
      _numberOfRecordsFromChildren++;
      addRecordToParent();
   }


   protected final int getNumberOfRecordsFromChildren() {
      return _numberOfRecordsFromChildren;
   }


   protected final void addRecordToParent() {
      final TreeNode parent = getParent();
      if (parent == null) {
         return;
      }
      ((CategoryNode) parent).addRecordFromChild();
   }
   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






