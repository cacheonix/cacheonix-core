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

import java.util.List;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.cache.datasource.DataSource;

/**
 * An object that is responsible for transparent execution and caching of the results of datasource queries.
 *
 * @see QueryKey
 * @see QueryResult
 */
final class CachingDatabaseQueryExecutor {

   private static final String CACHE_NAME = "org.cacheonix.examples.cache.datasource.CachingDatabaseQueryExecutor";

   /**
    * Query cache.
    */
   private final Cache queryCache;


   /**
    * Creates <code>CachingDatabaseQueryExecutor</code>.
    */
   CachingDatabaseQueryExecutor() throws ConfigurationException {

      queryCache = Cacheonix.getInstance().getCache(CACHE_NAME);
   }


   /**
    * Executed a SQL query with the given text and parameters. The result of the query are returned from the cache. In
    * case of a cache miss, Cacheonix calls <code>DatabaseDataSource</code> to retrieve missing data from the database.
    *
    * @param query      a SQL query to execute.
    * @param parameters a list of parameters.
    * @return the result of the query stored in the <code>QueryResult</code> object.
    * @see QueryResult
    * @see DataSource
    */
   public QueryResult execute(final String query, final List parameters) {

      // Create query key
      final QueryKey queryKey = new QueryKey(query, parameters);

      // Request result. The datasource takes care about getting the result from the database if
      // it is not found in the cache.

      // Return the result
      return (QueryResult) queryCache.get(queryKey);
   }


   public String toString() {

      return "CachingDatabaseQueryExecutor{" +
              "queryCache=" + queryCache +
              '}';
   }
}
