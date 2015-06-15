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
package org.cacheonix.impl.cache.store;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.AssertionFailedError;

/**
 * Tests CacheStatisticsImpl
 */
public final class CacheStatisticsImplTest extends CacheonixTestCase {

   /**
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheStatisticsImplTest.class); // NOPMD

   private CacheStatisticsImpl statistics;

   private static final float FLOAT_ZERO = 0.0f;

   private static final float FLOAT_ONE = 1.0f;

   private static final float FLOAT_ZERO_DOT_FIVE = 0.5f;


   public void testCreate() {

      assertEquals(0L, statistics.getReadHitCount());
      assertEquals(0L, statistics.getReadMissCount());
      assertEquals(FLOAT_ZERO, statistics.getReadMissRatio());
      assertEquals(FLOAT_ZERO, statistics.getReadHitRatio());
   }


   public void testIncrementHitCount() {

      statistics.incrementReadHitCount();
      assertEquals(1L, statistics.getReadHitCount());
      statistics.incrementReadHitCount();
      assertEquals(2L, statistics.getReadHitCount());
      assertEquals(FLOAT_ONE, statistics.getReadHitRatio());
   }


   public void testIncrementMissCount() {

      statistics.incrementReadMissCount();
      assertEquals(1L, statistics.getReadMissCount());
      statistics.incrementReadMissCount();
      assertEquals(2L, statistics.getReadMissCount());
      assertEquals(FLOAT_ONE, statistics.getReadMissRatio());
   }


   public void testGetWriteHitCount() {

      statistics.incrementWriteHitCount();
      assertEquals(1L, statistics.getWriteHitCount());
   }


   public void testGetWriteHitRatio() {

      assertEquals(0.0F, statistics.getWriteHitRatio());

      statistics.incrementWriteHitCount();
      assertEquals(1.0F, statistics.getWriteHitRatio());

      statistics.incrementWriteMissCount();
      assertEquals(FLOAT_ZERO_DOT_FIVE, statistics.getWriteHitRatio());
   }


   public void testGetWriteMissCount() {

      statistics.incrementWriteMissCount();
      assertEquals(1L, statistics.getWriteMissCount());
   }


   public void testGetWriteMissRatio() {

      assertEquals(0.0F, statistics.getWriteMissRatio());

      statistics.incrementWriteMissCount();
      assertEquals(1.0F, statistics.getWriteMissRatio());

      statistics.incrementWriteHitCount();
      assertEquals(FLOAT_ZERO_DOT_FIVE, statistics.getWriteMissRatio());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      statistics.incrementReadHitCount();

      statistics.incrementReadMissCount();
      statistics.incrementReadMissCount();

      statistics.incrementWriteHitCount();
      statistics.incrementWriteHitCount();
      statistics.incrementWriteHitCount();

      statistics.incrementWriteMissCount();
      statistics.incrementWriteMissCount();
      statistics.incrementWriteMissCount();
      statistics.incrementWriteMissCount();

      statistics.incrementElementsOnDiskCount();
      statistics.incrementElementsOnDiskCount();
      statistics.incrementElementsOnDiskCount();
      statistics.incrementElementsOnDiskCount();
      statistics.incrementElementsOnDiskCount();

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(statistics, ser.deserialize(ser.serialize(statistics)));
   }


   /**
    * @noinspection MethodOverridesStaticMethodOfSuperclass, FloatingPointEquality
    */
   private static void assertEquals(final float expected, final float received) {

      if (expected != received) {
         throw new AssertionFailedError("Expected: " + expected + " but received: " + received);
      }
   }


   protected void setUp() throws Exception {

      super.setUp();
      statistics = new CacheStatisticsImpl();
   }


   public String toString() {

      return "CacheStatisticsImplTest{" +
              "statistics=" + statistics +
              '}';
   }
}
