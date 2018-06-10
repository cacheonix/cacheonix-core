/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.cache.local;

import java.util.Collection;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.datastore.DataStoreContext;
import org.cacheonix.cache.datastore.Storable;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.cache.util.DummyObjectSizeCalculator;
import org.cacheonix.impl.config.ElementEventNotification;

/**
 * Tests DataStore.
 */
public final class LocalDataStoreTest extends CacheonixTestCase {


   /**
    * A test cache size.
    */
   private static final int MAX_SIZE = 10;

   /**
    * Test value.
    */
   private static final String VALUE = "value";

   /**
    * Test keys.
    */
   private static final String KEY = "key";

   /**
    * A test data source
    */
   private TestDataStore dataStore;


   /**
    * A local cache.
    */
   private LocalCache<String, String> cache = null;


   /**
    * Tests that a key and a value are passed to a data store.
    */
   public void testStoresKeyAndValue() {

      cache.put(KEY, VALUE);
      assertEquals(KEY, dataStore.getStoredKey());
      assertEquals(VALUE, dataStore.getStoredValue());
   }


   /**
    * Tests that a null key and a value are passed to a data store.
    */
   public void testStoresNullKeyAndValue() {

      cache.put(null, VALUE);
      assertNull(dataStore.getStoredKey());
      assertEquals(VALUE, dataStore.getStoredValue());
   }


   /**
    * Tests that a key and a null value are passed to a data store.
    */
   public void testStoresKeyAndNullValue() {

      cache.put(KEY, null);
      assertEquals(KEY, dataStore.getStoredKey());
      assertNull(dataStore.getStoredValue());
   }


   /**
    * Sets up a test case.
    *
    * @throws Exception if an error occurred.
    */
   protected void setUp() throws Exception {

      super.setUp();
      dataStore = new TestDataStore();
      cache = new LocalCache<String, String>(TestConstants.LOCAL_TEST_CACHE, MAX_SIZE, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), dataStore, new DummyCacheInvalidator(),
              new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);

   }


   /**
    * Test data store. It just records the last saved key and value.
    */
   private static class TestDataStore implements DataStore {

      public DataStoreContext context = null;

      private Object storedKey = null;

      private Object storedValue = null;


      /**
       * {@inheritDoc}.
       * <p/>
       * This test implementation simply sets the fields for future assertion.
       *
       * @see #getStoredKey()
       * @see #getStoredValue()
       */
      public void store(final Storable storable) {

         storedKey = storable.getKey();
         storedValue = storable.getValue();
      }


      /**
       * {@inheritDoc}
       */
      public void store(final Collection storables) {

         for (final Object storable : storables) {

            store((Storable) storable);
         }
      }


      /**
       * Returns a key set by {@link #store(Storable)}
       *
       * @return the key set by {@link #store(Storable)}.
       */
      public Object getStoredKey() {

         return storedKey;
      }


      /**
       * Returns a value set by {@link #store(Storable)}
       *
       * @return the value set by {@link #store(Storable)}.
       */
      public Object getStoredValue() {

         return storedValue;
      }


      public DataStoreContext getContext() {

         return context;
      }


      /**
       * {@inheritDoc}
       */
      public void setContext(final DataStoreContext context) {

         this.context = context;
      }


      public String toString() {

         return "TestDataStore{" +
                 "context=" + context +
                 ", storedKey=" + storedKey +
                 ", storedValue=" + storedValue +
                 '}';
      }
   }
}
