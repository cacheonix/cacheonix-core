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
package org.cacheonix.examples.cache.invalidator;

import java.sql.Date;
import java.sql.SQLException;

import org.cacheonix.Cacheonix;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import junit.framework.TestCase;

/**
 * Example of using CacheInvalidator to invalidate a cache based on a time stamp in a backing database table.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class TimestampCacheInvalidatorTest extends TestCase {

   /**
    * Cache name to use in this test case. The cacheonix-config.xml is stored under the conf directory.
    */
   private static final String CACHE_NAME = "TimestampCacheInvalidatorTest";

   /**
    * Database JDBC driver.
    */
   private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

   /**
    * URL to access the examples database.
    */
   private static final String DATABASE_URL = "REVIEWME:";

   /**
    * Database user.
    */
   private static final String DATABASE_USER = "test";

   /**
    * Database user's password.
    */
   private static final String DATABASE_PASSWORD = "test";

   /**
    * Cache to hold Invoice objects.
    *
    * @see #setUp()
    */
   private Cache<Integer, Invoice> cache = null;

   /**
    * Storage manager to access an invoice stored in the database.
    *
    * @see #setUp()
    */
   private InvoiceStorageManager storageManager = null;

   /**
    * Cacheonix instance.
    */
   private Cacheonix cacheonix;


   /**
    * Tests that the TimestampCacheInvalidator is called.
    *
    * @throws SQLException if there are errors accessing database
    */
   public void testInvalidate() throws SQLException {

      // Get a record from the database.
      final Integer invoiceID = 1;
      final Invoice invoice = storageManager.getInvoiceFromDB(invoiceID);

      // Put invoice to the cache
      cache.put(invoiceID, invoice);

      // Update invoice
      invoice.setDate(new Date(System.currentTimeMillis()));

      // Increment update timestamp that is used by cache invalidator
      invoice.incrementTimeStamp();

      // Save invoice in the DB that in turn updates the time stamp of the record
      storageManager.storeInvoiceInDB(invoice);

      // Observe that the cached object has been invalidated by confirming that it is not in the cache anymore. 
      assertNull(cache.get(invoiceID));
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();

      // Set up the access to database
      storageManager = new InvoiceStorageManager(JDBC_DRIVER, DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

      // Get clean cache
      final String configurationPath = getConfigurationPath();
      cacheonix = Cacheonix.getInstance(configurationPath);
      cache = cacheonix.getCache(CACHE_NAME);
      cache.clear();
   }


   protected void tearDown() throws Exception {

      // Cache manager has be be shutdown upon application exit.
      // Note that call to shutdown() here uses unregisterSingleton
      // set to true. This is necessary to support clean restart on setUp()
      cacheonix.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, true);
      storageManager = null;
      cacheonix = null;
      cache = null;

      super.tearDown();
   }


   /**
    * Returns path to example cacheonix-config.xml.
    * <p/>
    * If running in an IDE, make sure to set property <code>cacheonix.examples.cacheonix.config.xml</code> to point it
    * to the <code>cacheonix-config.xml</code> under the <code>conf</code> directory. Examples' build.xml sets this
    * property automatically.
    * <p/>
    * This may seem to be a bit complicated, but we did to make it possible for you to run Cacheonix examples alongside
    * your application, so that they don't interfere.
    *
    * @return Returns path to example's <code>cacheonix-config.xml</code>.
    * @throws IllegalStateException if <code>cacheonix.examples.cacheonix.config.xml</code> is not set.
    */
   private static String getConfigurationPath() throws IllegalStateException {

      // Read the property containing path to cacheonix-config.xml
      final String configurationPath = System.getProperty("cacheonix.examples.cacheonix.config.xml");

      // Validate
      if (configurationPath == null || configurationPath.length() == 0) {
         throw new IllegalStateException("Property 'cacheonix.examples.cacheonix.config.xml' is not set. " +
                 "Make sure that your IDE provides property 'cacheonix.examples.cacheonix.config.xml' " +
                 "to the JUnit runner and that it is set to the path to 'cacheonix-config.xml' " +
                 "under the examples' 'conf' directory. ");
      }
      return configurationPath;
   }


   public String toString() {

      return "TimestampCacheInvalidatorTest{" +
              "cache=" + cache +
              ", storageManager=" + storageManager +
              '}';
   }
}
