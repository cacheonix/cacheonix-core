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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * A tester for CacheableEntry.
 */
public class CacheableEntryTest extends CacheonixTestCase {


   private CacheableEntry cacheableEntry = null;

   private Binary key = null;

   private CacheableValue value = null;


   public void testGetKey() {

      assertEquals(key, cacheableEntry.getKey());
   }


   public void testGetValue() {

      assertEquals(value, cacheableEntry.getValue());
   }


   public void testGetWireableType() {

      assertEquals(Wireable.TYPE_CACHEABLE_ENTRY, cacheableEntry.getWireableType());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(cacheableEntry, ser.deserialize(ser.serialize(cacheableEntry)));
   }


   public void testToString() {

      assertNotNull(cacheableEntry.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      key = toBinary("key");
      value = new CacheableValue(toBinary("value"), getClock().currentTime(),
              getClock().currentTime(), getClock().currentTime());
      cacheableEntry = new CacheableEntry(key, value);
   }


   public void tearDown() throws Exception {

      cacheableEntry = null;
      key = null;
      value = null;

      super.tearDown();
   }
}
