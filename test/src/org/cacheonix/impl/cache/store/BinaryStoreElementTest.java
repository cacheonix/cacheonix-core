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
package org.cacheonix.impl.cache.store;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.cache.invalidator.DummyCacheInvalidator;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.storage.disk.DummyDiskStorage;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;
import org.cacheonix.impl.util.cache.ObjectSizeCalculatorFactory;
import org.cacheonix.impl.util.cache.StandardObjectSizeCalculator;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tester for BinaryStoreElement.
 */
public final class BinaryStoreElementTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BinaryStoreElementTest.class); // NOPMD

   private static final String TEST_CACHE_NAME = "test.cache.name";

   private static final long EXPIRATION_TIME_MILLIS = 1L;

   private static final TimeImpl EXPIRATION_TIME = new TimeImpl(EXPIRATION_TIME_MILLIS, 0);

   private static final TimeImpl IDLE_TIME_MILLIS = new TimeImpl(2L, 0);

   private static final Binary KEY = toBinary("key");

   private static final Binary VALUE = toBinary("value");

   private BinaryStoreElement element;

   private Binary value;

   public void testGetValue() throws Exception {

      assertEquals(value, element.getValue());
   }


   public void testGetCreatedTimeMillis() throws Exception {

      assertTrue(element.getCreatedTime().compareTo(getClock().currentTime()) < 0);
   }


   public void testGetLastTimeAccessedMillis() throws Exception {

      assertTrue(element.getIdleTime().compareTo(getClock().currentTime()) < 0);
   }


   public void testSetLastTimeAccessedMillis() throws Exception {

      element.setIdleTime(new TimeImpl(EXPIRATION_TIME_MILLIS, 0));

      assertEquals(new TimeImpl(EXPIRATION_TIME_MILLIS, 0), element.getIdleTime());
   }


   public void testIsExpired() throws Exception {

      Thread.sleep(2 * EXPIRATION_TIME_MILLIS);

      assertTrue(element.isExpired(getClock()));
   }


   public void testInitialBeforeIsNull() throws Exception {

      assertNull(element.getBefore());
   }


   public void testSetBefore() throws Exception {

      final BinaryStoreElement before = new BinaryStoreElement();
      element.setBefore(before);
      assertEquals(before, element.getBefore());
   }


   public void testInitialAfterIsNull() throws Exception {


      assertNull(element.getAfter());
   }


   public void testSetAfter() throws Exception {

      final BinaryStoreElement after = new BinaryStoreElement();
      element.setAfter(after);
      assertEquals(after, element.getAfter());
   }


   public void testInvalidate() throws Exception {

      element.invalidate();
      assertTrue(element.isInvalid());
   }


   public void testIsInvalid() throws Exception {

      assertFalse(element.isInvalid());
   }


   public void testWriteReadWireEmpty() throws Exception {

      final BinaryStoreElement expected = new BinaryStoreElement();
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] serialize = ser.serialize(expected);
      final long size = new StandardObjectSizeCalculator().sizeOf(expected);
      assertEquals("Update BinaryStoreElement.SIZE_CACHE_ELEMENT_OVERHEAD if this size changes", size, BinaryStoreElement.SIZE_CACHE_ELEMENT_OVERHEAD);
      assertEquals(expected, ser.deserialize(serialize));
   }


   protected void setUp() throws Exception {

      super.setUp();

      final ObjectSizeCalculatorFactory calculatorFactory = new ObjectSizeCalculatorFactory();
      final ObjectSizeCalculator sizeCalculator = calculatorFactory.createSizeCalculator(1000L);
      final DummyCacheInvalidator invalidator = new DummyCacheInvalidator();
      final DummyDiskStorage diskStorage = new DummyDiskStorage(TEST_CACHE_NAME);
      value = VALUE;
      element = new BinaryStoreElement(KEY, value, getClock().currentTime(), EXPIRATION_TIME, IDLE_TIME_MILLIS, sizeCalculator, invalidator, diskStorage);
   }


   public void tearDown() throws Exception {

      element = null;

      value = null;

      super.tearDown();
   }
}
