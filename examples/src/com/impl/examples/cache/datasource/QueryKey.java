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
import java.util.Collections;
import java.util.List;

/**
 * An composite object that uniquely identifies a datasource query. This includes the text of the datasource query and
 * the parameters of the query.
 * <p/>
 * <code>QueryKey</code> implement the <a href="">Immutable Key Pattern</a>.
 *
 * @see DatabaseDataSource#get(Object)
 */
final class QueryKey implements Serializable {

   private final String queryText;

   private final List queryParameters;

   private static final long serialVersionUID = -3503552425911562915L;


   QueryKey(final String queryText, final List queryParameters) {

      this.queryText = queryText;
      this.queryParameters = new ArrayList(queryParameters);
   }


   /**
    * Returns the query text.
    *
    * @return query test.
    * @see DatabaseDataSource#get(Object)
    */
   public String getQueryText() {

      return queryText;
   }


   /**
    * Returns the parameters of the query.
    *
    * @return the parameters of the query.
    * @see DatabaseDataSource#get(Object)
    */
   public List getQueryParameters() {

      return Collections.unmodifiableList(queryParameters);
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }
      final QueryKey query = (QueryKey) obj;
      if (!queryParameters.equals(query.queryParameters)) {
         return false;
      }
      return queryText.equals(query.queryText);
   }


   public int hashCode() {

      int result = queryText.hashCode();
      result = 29 * result + queryParameters.hashCode();
      return result;
   }


   public String toString() {

      return "QueryKey{" +
              "queryText='" + queryText + '\'' +
              ", queryParameters=" + queryParameters +
              '}';
   }
}
