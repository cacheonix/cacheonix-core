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

import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

/**
 * CategoryExplorerTree
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brent Sprecher
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryExplorerTree extends JTree {

   private static final long serialVersionUID = 8066257446951323576L;
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected final CategoryExplorerModel _model;
   protected boolean _rootAlreadyExpanded = false;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   /**
    * Construct a CategoryExplorerTree with a specific model.
    */
   public CategoryExplorerTree(final CategoryExplorerModel model) {
      super(model);

      _model = model;
      init();
   }


   /**
    * Construct a CategoryExplorerTree and create a default CategoryExplorerModel.
    */
   public CategoryExplorerTree() {

      final CategoryNode rootNode = new CategoryNode("Categories");

      _model = new CategoryExplorerModel(rootNode);

      setModel(_model);

      init();
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   public final CategoryExplorerModel getExplorerModel() {
      return _model;
   }


   public final String getToolTipText(final MouseEvent e) {

      try {
         return super.getToolTipText(e);
      } catch (final Exception ex) {
         return "";
      }

   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final void init() {
      // Put visible lines on the JTree.
      putClientProperty("JTree.lineStyle", "Angled");

      // Configure the Tree with the appropriate Renderers and Editors.

      final CategoryNodeRenderer renderer = new CategoryNodeRenderer();
      setEditable(true);
      setCellRenderer(renderer);

      final CategoryNodeEditor editor = new CategoryNodeEditor(_model);

      setCellEditor(new CategoryImmediateEditor(this,
              new CategoryNodeRenderer(),
              editor));
      setShowsRootHandles(true);

      setToolTipText("");

      ensureRootExpansion();

   }


   protected final void expandRootNode() {
      if (_rootAlreadyExpanded) {
         return;
      }
      _rootAlreadyExpanded = true;
      final TreePath path = new TreePath(_model.getRootCategoryNode().getPath());
      expandPath(path);
   }


   protected final void ensureRootExpansion() {
      _model.addTreeModelListener(new TreeModelAdapter() {
         public void treeNodesInserted(final TreeModelEvent e) {
            expandRootNode();
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






