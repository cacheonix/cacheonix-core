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
package org.cacheonix.impl.cache.datasource;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.cache.datasource.SimpleDataSourceObject;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.store.BinaryStoreElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.util.cache.DummyObjectSizeCalculator;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tester for BinaryStoreDataSourceImpl.
 */
public final class BinaryStoreDataSourceImplTest extends CacheonixTestCase {


   private static final String TEST_KEY = "test.key";

   private static final String TEST_VALUE = "test.value";

   private BinaryStoreDataSourceImpl binaryStoreDataSource;

   private DataSource userDataSource;

   private PrefetchStage prefetchStage;


   public void testGetFoundKey() throws Exception {

      // Mock user datasource that always returns TEST_VALUE
      when(userDataSource.get(TEST_KEY)).thenReturn(new SimpleDataSourceObject(TEST_VALUE));

      // Test
      final BinaryStoreDataSourceObject binaryStoreDataSourceObject = binaryStoreDataSource.get(toBinary(TEST_KEY));
      assertEquals(TEST_VALUE, binaryStoreDataSourceObject.getObject());
      assertNotNull(binaryStoreDataSourceObject.getTimeToRead());
   }


   public void testGetNotFoundKey() throws Exception {

      // Mock user datasource that always returns TEST_VALUE
      when(userDataSource.get(TEST_KEY)).thenReturn(null);

      // Test
      final BinaryStoreDataSourceObject binaryStoreDataSourceObject = binaryStoreDataSource.get(toBinary(TEST_KEY));
      assertNull(binaryStoreDataSourceObject);
   }


   public void testSchedulePrefetch() throws Exception {

      // Do-nothing objects
      final DummyObjectSizeCalculator sizeCalculator = new DummyObjectSizeCalculator();
      final DummyCacheInvalidator invalidator = new DummyCacheInvalidator();
      final DummyDiskStorage diskStorage = new DummyDiskStorage("test.cache");

      final Time createdTime = getClock().currentTime();
      final Time expirationTime = getClock().currentTime().add(100);
      final Time timeTookToReadFromDataSource = new Time(50, 0);
      final BinaryStoreElement element = new BinaryStoreElement(toBinary(TEST_KEY), toBinary(TEST_VALUE), createdTime,
              expirationTime, null, sizeCalculator, invalidator, diskStorage);

      // Confirm
      binaryStoreDataSource.schedulePrefetch(element, timeTookToReadFromDataSource);

      // Confirm was scheduled
      verify(prefetchStage).schedule(any(PrefetchCommandImpl.class));
   }


   public void testToString() throws Exception {

      assertNotNull(binaryStoreDataSource.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      final PrefetchElementUpdater prefetchElementUpdater = Mockito.mock(PrefetchElementUpdater.class);
      prefetchStage = Mockito.mock(PrefetchStage.class);

      userDataSource = Mockito.mock(DataSource.class);
      binaryStoreDataSource = new BinaryStoreDataSourceImpl(getClock(), prefetchElementUpdater, prefetchStage,
              userDataSource, true);
   }


   public void tearDown() throws Exception {

      binaryStoreDataSource = null;

      super.tearDown();
   }
}
