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
package org.cacheonix.impl.cache.local;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.impl.cache.datasource.DummyBinaryStoreDataSource;
import org.cacheonix.impl.cache.datastore.DummyDataStore;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.loader.DummyCacheLoader;
import org.cacheonix.impl.configuration.ElementEventNotification;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tests {@link LocalCache#execute(org.cacheonix.cache.executor.Executable, org.cacheonix.cache.executor.Aggregator)} and {@link
 * LocalCache#executeAll(Set, org.cacheonix.cache.executor.Executable, org.cacheonix.cache.executor.Aggregator)}.
 */
public final class LocalCacheExecuteTest extends CacheonixTestCase {

   private static final int KEY_COUNT = 5000;


   private LocalCache<Integer, Integer> cache;


   /**
    *
    */
   public void testExecute() {

      // Create executable and aggregator.
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      final Integer result = (Integer) cache.execute(executable, aggregator);
      assertEquals(KEY_COUNT, result.intValue());
   }


   /**
    *
    */
   public void testExecuteAll() {

      final int keyCount = KEY_COUNT / 2;
      final Set<Integer> ketSet = new HashSet<Integer>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         ketSet.add(i);
      }

      // Create executable and aggregator.
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      final Integer result = (Integer) cache.executeAll(ketSet, executable, aggregator);
      assertEquals(keyCount, result.intValue());
   }


   /**
    *
    */
   public void testExecuteWithFilter() {

      // Put element into cache
      final int keyCount = KEY_COUNT;
      final Map<Integer, Integer> map = new HashMap<Integer, Integer>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(i, 1);
      }

      // Put to cache
      cache.putAll(map);

      // Create executable and aggregator.
      final EntryFilter filter = new EvenEntryFilter();
      final Executable executable = new Executable();
      final Aggregator aggregator = new Aggregator();

      final Integer result = (Integer) cache.execute(filter, executable, aggregator);
      assertEquals(keyCount / 2, result.intValue());
   }


   protected void setUp() throws Exception {

      super.setUp();

      // Create cache
      cache = new LocalCache<Integer, Integer>(TestConstants.LOCAL_TEST_CACHE, KEY_COUNT, 0, 0, 0,
              getClock(), getEventNotificationExecutor(), new DummyDiskStorage(TestConstants.LOCAL_TEST_CACHE),
              new DummyObjectSizeCalculator(), new DummyBinaryStoreDataSource(), new DummyDataStore(),
              new DummyCacheInvalidator(), new DummyCacheLoader(), ElementEventNotification.SYNCHRONOUS);

      // Put element into cache
      final int keyCount = KEY_COUNT;
      final Map<Integer, Integer> map = new HashMap<Integer, Integer>(keyCount);
      for (int i = 0; i < keyCount; i++) {
         map.put(Integer.valueOf(i), 1);
      }

      // Put to cache
      cache.putAll(map);

   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      cache.shutdown();
      super.tearDown();
   }


   public String toString() {

      return "LocalCacheTest{" +
              "cache=" + cache +
              '}';
   }


   @SuppressWarnings("ClassNameSameAsAncestorName")
   private static final class Executable implements org.cacheonix.cache.executor.Executable {

      private static final long serialVersionUID = 0L;


      public Serializable execute(final Collection<CacheEntry> cacheEntries) {

         int partialResult = 0;
         for (final CacheEntry entry : cacheEntries) {
            final Integer value = (Integer) entry.getValue();
            partialResult += value;
         }
         return partialResult;
      }
   }

   @SuppressWarnings("ClassNameSameAsAncestorName")
   private static final class Aggregator implements org.cacheonix.cache.executor.Aggregator {

      public Serializable aggregate(final Collection partialResults) {

         int result = 0;
         for (final Object partialResult : partialResults) {
            result += (Integer) partialResult;
         }
         return result;
      }
   }

   /**
    * EvenFilter
    * <p/>
    *
    * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
    * @since May 20, 2010 4:38:33 PM
    */
   private static final class EvenEntryFilter implements EntryFilter {

      /**
       * Logger.
       *
       * @noinspection UNUSED_SYMBOL, UnusedDeclaration
       */
      private static final Logger LOG = Logger.getLogger(EvenEntryFilter.class); // NOPMD

      private static final long serialVersionUID = -7019390425271991967L;


      public boolean matches(final CacheEntry cacheEntry) {

         final Integer key = (Integer) cacheEntry.getKey();

         return key % 2 == 0;
      }
   }
}