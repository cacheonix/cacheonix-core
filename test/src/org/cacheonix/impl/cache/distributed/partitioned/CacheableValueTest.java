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
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 * Tester for CacheableValue.
 */
public final class CacheableValueTest extends CacheonixTestCase {

   private static final String VALUE = "value";

   private static final long MILLIS = 1000L;

   private static final long COUNT = 2000L;

   private Binary binaryValue;

   private TimeImpl expirationTime;

   private CacheableValue cacheableValue;


   public void testGetValue() throws Exception {

      assertEquals(binaryValue, cacheableValue.getBinaryValue());
   }


   public void testGetExpirationTime() throws Exception {

      assertEquals(expirationTime, cacheableValue.getTimeToLeave());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(cacheableValue, ser.deserialize(ser.serialize(cacheableValue)));
   }


   public void testWriteReadWireNullExpirationTime() throws Exception {

      final CacheableValue cacheableValueWithNullExpirationTime = new CacheableValue(toBinary(VALUE), null);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(cacheableValueWithNullExpirationTime, ser.deserialize(ser.serialize(cacheableValueWithNullExpirationTime)));
   }


   public void testGetWireableType() throws Exception {

      assertEquals(Wireable.TYPE_CACHEABLE_VALUE, cacheableValue.getWireableType());
   }


   public void testEquals() throws Exception {

      assertEquals(new CacheableValue(toBinary(VALUE), new TimeImpl(MILLIS, COUNT)), cacheableValue);
   }


   public void testHashCode() throws Exception {

      assertTrue(cacheableValue.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(cacheableValue.toString());
   }


   public void testHashCodeNullExpirationTime() throws Exception {

      assertTrue(new CacheableValue(toBinary(VALUE), null).hashCode() != 0);
   }


   public void testToStringNullExpirationTime() throws Exception {

      assertNotNull(new CacheableValue(toBinary(VALUE), null).toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      binaryValue = toBinary(VALUE);
      expirationTime = new TimeImpl(MILLIS, COUNT);
      cacheableValue = new CacheableValue(binaryValue, expirationTime);
   }


   public void tearDown() throws Exception {

      cacheableValue = null;
      expirationTime = null;
      binaryValue = null;

      super.tearDown();
   }
}
