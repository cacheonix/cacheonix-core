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

import org.cacheonix.impl.util.logging.lf5.LogRecord;
import org.cacheonix.impl.util.logging.lf5.LogRecordFilter;

/**
 * An implementation of LogRecordFilter based on a CategoryExplorerModel
 *
 * @author Richard Wan
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryExplorerLogRecordFilter implements LogRecordFilter {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   protected final CategoryExplorerModel _model;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryExplorerLogRecordFilter(final CategoryExplorerModel model) {

      _model = model;
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * @return <code>true</code> if the CategoryExplorer model specified at construction is accepting the category of the
    *         specified LogRecord.  Has a side-effect of adding the CategoryPath of the LogRecord to the explorer model
    *         if the CategoryPath is new.
    */
   public boolean passes(final LogRecord record) {

      final CategoryPath path = new CategoryPath(record.getCategory());
      return _model.isCategoryPathActive(path);
   }


   /**
    * Resets the counters for the contained CategoryNodes to zero.
    */
   public void reset() {

      resetAllNodes();
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final void resetAllNodes() {

      final Enumeration nodes = _model.getRootCategoryNode().depthFirstEnumeration();
      while (nodes.hasMoreElements()) {
         final CategoryNode current = (CategoryNode) nodes.nextElement();
         current.resetNumberOfContainedRecords();
         _model.nodeChanged(current);
      }
   }
   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces
   //--------------------------------------------------------------------------
}

