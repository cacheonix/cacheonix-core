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

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * CategoryPath is a collection of CategoryItems which represent a path of categories.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public final class CategoryPath {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected final LinkedList _categoryElements = new LinkedList();

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public CategoryPath() {

   }


   /**
    * Construct a CategoryPath.  If the category is null, it defaults to "Debug".
    */
   public CategoryPath(final String category) {

      String processedCategory = category;

      if (processedCategory == null) {
         processedCategory = "Debug";
      }

      processedCategory = processedCategory.replace('/', '.').replace('\\', '.');

      final StringTokenizer st = new StringTokenizer(processedCategory, ".");
      while (st.hasMoreTokens()) {
         final String element = st.nextToken();
         addCategoryElement(new CategoryElement(element));
      }
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * returns the number of CategoryElements.
    */
   public final int size() {

      return _categoryElements.size();
   }


   public final boolean isEmpty() {

      boolean empty = false;

      if (_categoryElements.isEmpty()) {
         empty = true;
      }

      return empty;
   }


   /**
    * Removes all categoryElements.
    */
   public void removeAllCategoryElements() {

      _categoryElements.clear();
   }


   /**
    * Adds the specified categoryElement to the end of the categoryElement set.
    */
   public final void addCategoryElement(final CategoryElement categoryElement) {

      _categoryElements.addLast(categoryElement);
   }


   /**
    * Returns the CategoryElement at the specified index.
    */
   public final CategoryElement categoryElementAt(final int index) {

      return (CategoryElement) _categoryElements.get(index);
   }


   public final String toString() {

      final StringBuilder out = new StringBuilder(100);

      out.append('\n');
      out.append("===========================\n");
      out.append("CategoryPath:                   \n");
      out.append("---------------------------\n");

      out.append("\nCategoryPath:\n\t");

      if (this.isEmpty()) {
         out.append("<<NONE>>");
      } else {
         for (int i = 0; i < this.size(); i++) {
            //noinspection ObjectToString
            out.append(this.categoryElementAt(i).toString());
            out.append("\n\t");
         }
      }

      out.append('\n');
      out.append("===========================\n");

      return out.toString();
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}
