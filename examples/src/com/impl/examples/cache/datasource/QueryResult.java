/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.impl.examples.cache.datasource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A an object that holds the result of query execution.
 *
 * @see CachingDatabaseQueryExecutor#execute(String, List)
 * @see DatabaseDataSource#get(Object)
 */
public final class QueryResult implements Serializable {

   private static final long serialVersionUID = -2302059901741668540L;

   /**
    * Number of columns in the row.
    */
   private final int columnCount;

   /**
    * A list of rows. Each row is an Object[] of size <code>columnCount</code>
    */
   private final List rows;


   /**
    * Creates <code>QueryResult</code> that is stored in the query result cache.
    *
    * @param columnCount a number of columns in the row.
    * @param rows        a list of rows. Each row is an Object[] of size <code>columnCount</code>
    * @see DatabaseDataSource#get(Object)
    */
   public QueryResult(final int columnCount, final List rows) {

      this.columnCount = columnCount;
      this.rows = new ArrayList(rows);
   }


   /**
    * Return number of columns in the row.
    *
    * @return number of columns in the row
    * @see #getRows()
    */
   public int getColumnCount() {

      return columnCount;
   }


   /**
    * Return a list of rows. Each row is an Object[] of size {@link #getColumnCount()}.
    *
    * @return list of rows. Each row is an Object[] of size {@link #getColumnCount()}.
    * @see #getColumnCount()
    */
   public List getRows() {

      return new ArrayList(rows);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final QueryResult that = (QueryResult) obj;

      if (columnCount != that.columnCount) {
         return false;
      }
      return compareRows(rows, that.rows);
   }


   /**
    * Compares row lists.
    *
    * @param rows  first row list.
    * @param rows1 second row list.
    * @return <code>true</code> if these two lists of rows are equal
    */
   private boolean compareRows(final List rows, final List rows1) {

      return false;  //To change body of created methods use File | Settings | File Templates.
   }


   public int hashCode() {

      int result = columnCount;
      result = 29 * result + (rows != null ? rows.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "QueryResult{" +
              "columnCount=" + columnCount +
              ", rows=" + rows +
              '}';
   }
}
