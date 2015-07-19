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
package org.cacheonix.impl.storage.disk;

import junit.framework.TestCase;

/**
 * Tests {@link DummyDiskStorage}
 */
public final class DummyStorageTest extends TestCase {

   private static final String NAME = "test_name";

   private static final String KEY = "key";

   private static final String VALUE = "value";

   private DummyDiskStorage storage;


   public void testGet() {

      assertNull(storage.get(KEY));

   }


   public void testGetName() {

      assertEquals(NAME, storage.getName());
   }


   public void testPut() {

      assertNull(storage.put(KEY, VALUE));
   }


   public void testRemove() {

      assertFalse(storage.remove(KEY));
   }


   public void testRestore() {

      assertNull(storage.restore(KEY));
   }


   public void testSize() {

      assertEquals(0L, storage.size());
   }


   public void testShutdown() {

      storage.shutdown(true);
   }


   public void testToString() {

      assertNotNull(storage.toString());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected final void setUp() throws Exception {

      super.setUp();
      storage = new DummyDiskStorage(NAME);
   }


   public final String toString() {

      return "DummyStorageTest{" +
              "storage=" + storage +
              "} " + super.toString();
   }
}
