/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
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

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.datasource.DataSource;
import junit.framework.TestCase;

/**
 * Tester for CachingQueryExecutor. CachingQueryExecutor demonstrates backend cache population though implementing a
 * database-bound DataSource.
 *
 * @noinspection JavaDoc
 * @see DatabaseDataSource
 * @see DataSource
 */
public final class DataSourceTest extends TestCase {

   /**
    * Caching query executor.
    */
   private CachingDatabaseQueryExecutor queryExecutor;


   /**
    * Tests database query caching.
    *
    * @throws Exception
    * @see CachingDatabaseQueryExecutor
    * @see DatabaseDataSource
    */
   public void testExecuteQuery() throws Exception {

      // Prepare query. Note the positional parameter marker with a standard JDBC syntax, the "?"
      // character.

      // Set the positional parameters.
      final List<Serializable> queryParameters = new ArrayList<Serializable>(2);
      queryParameters.add("my_product");
      queryParameters.add(Integer.valueOf(3));

      // Execute
      final String query = "select distinct(INVOICE.*) from PRODUCT, INVOICE " +
              "                          where PRODUCT.NAME=? " +
              "                                and INVOICE.NUMBER > ? " +
              "                                and INVOICE.PRODUCT_ID = PRODUCT.ID";
      final QueryResult result = queryExecutor.execute(query, queryParameters);

      // Get from the cache
      assertEquals(result, queryExecutor.execute(query, queryParameters));
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();

      queryExecutor = new CachingDatabaseQueryExecutor();
   }


   protected void tearDown() throws Exception {

      // Cache manager has be be shutdown upon application exit.
      // Note that call to shutdown() here uses unregisterSingleton
      // set to true. This is necessary to support clean restart on setUp()
      Cacheonix.getInstance().shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);

      //
      super.tearDown();
   }


   public String toString() {

      return "DataSourceTest{" +
              "queryExecutor=" + queryExecutor +
              '}';
   }
}
