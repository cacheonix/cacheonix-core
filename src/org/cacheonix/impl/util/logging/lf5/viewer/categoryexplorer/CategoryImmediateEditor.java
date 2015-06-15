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
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;

/**
 * CategoryImmediateEditor
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryImmediateEditor extends DefaultTreeCellEditor {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   private final CategoryNodeRenderer renderer;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryImmediateEditor(final JTree tree,
                                  final CategoryNodeRenderer renderer,
                                  final CategoryNodeEditor editor) {
      super(tree, renderer, editor);
      this.renderer = renderer;
      renderer.setIcon(null);
      renderer.setLeafIcon(null);
      renderer.setOpenIcon(null);
      renderer.setClosedIcon(null);

      super.editingIcon = null;
   }


   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------
   public final boolean shouldSelectCell(final EventObject e) {
      boolean rv = false;  // only mouse events

      if (e instanceof MouseEvent) {
         final MouseEvent me = (MouseEvent) e;
         final TreePath path = tree.getPathForLocation(me.getX(),
                 me.getY());
         final CategoryNode node = (CategoryNode)
                 path.getLastPathComponent();

         rv = node.isLeaf() /*|| !inCheckBoxHitRegion(me)*/;
      }
      return rv;
   }


   public final boolean inCheckBoxHitRegion(final MouseEvent e) {
      final TreePath path = tree.getPathForLocation(e.getX(),
              e.getY());
      if (path == null) {
         return false;
      }
      final CategoryNode node = (CategoryNode) path.getLastPathComponent();

      final Rectangle bounds = tree.getRowBounds(lastRow);
      final Dimension checkBoxOffset =
              renderer.getCheckBoxOffset();

      bounds.translate(offset + checkBoxOffset.width,
              checkBoxOffset.height);

      final boolean rv = bounds.contains(e.getPoint());
      return true;
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final boolean canEditImmediately(final EventObject e) {
      boolean rv = false;

      if (e instanceof MouseEvent) {
         final MouseEvent me = (MouseEvent) e;
         rv = inCheckBoxHitRegion(me);
      }

      return rv;
   }


   protected final void determineOffset(final JTree tree, final Object value,
                                        final boolean isSelected, final boolean expanded,
                                        final boolean leaf, final int row) {
      // Very important: means that the tree won't jump around.
      offset = 0;
   }

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






