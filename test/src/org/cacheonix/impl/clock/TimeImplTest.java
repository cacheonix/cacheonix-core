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
package org.cacheonix.impl.clock;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for Time.
 */
@SuppressWarnings("EqualsWithItself")
public class TimeImplTest extends TestCase {

   private static final long MILLIS = 1000L;

   private static final long COUNT = 5L;

   private TimeImpl time;


   public void testDefaultConstructor() {

      final Time defaultContructorTime = new TimeImpl();
      assertEquals(0, defaultContructorTime.getMillis());
      assertEquals(0, defaultContructorTime.getCount());
   }


   public void testGetMillis() {

      assertEquals(MILLIS, time.getMillis());
   }


   public void testGetCount() {

      assertEquals(COUNT, time.getCount());
   }


   public void testCompareTo() {

      assertEquals(0, time.compareTo(new TimeImpl(MILLIS, COUNT)));

      assertEquals(1, time.compareTo(new TimeImpl(MILLIS - 1, COUNT)));
      assertEquals(1, time.compareTo(new TimeImpl(MILLIS, COUNT - 1)));
      assertEquals(1, time.compareTo(new TimeImpl(MILLIS - 1, COUNT - 1)));

      assertEquals(-1, time.compareTo(new TimeImpl(MILLIS + 1, COUNT)));
      assertEquals(-1, time.compareTo(new TimeImpl(MILLIS, COUNT + 1)));
      assertEquals(-1, time.compareTo(new TimeImpl(MILLIS + 1, COUNT + 1)));

      assertEquals(1, time.compareTo(TimeImpl.ZERO));
   }


   @SuppressWarnings({"ObjectEqualsNull", "EqualsWithItself"})
   public void testEquals() {

      assertEquals(time, new TimeImpl(MILLIS, COUNT));
      assertFalse(new TimeImpl(MILLIS + 1, COUNT).equals(time));
      assertFalse(new TimeImpl(MILLIS, COUNT + 1).equals(time));
      assertTrue(time.equals(time));
      assertFalse(time.equals(null)); // NOPMD
      assertFalse(time.equals(new Object()));
   }


   public void testSubtract() {

      assertEquals(new TimeImpl(0, 0), time.subtract(new TimeImpl(MILLIS, COUNT)));
      assertEquals(new TimeImpl(-1, -1), time.subtract(new TimeImpl(MILLIS + 1, COUNT + 1)));
      assertNotSame(time, time.subtract(new TimeImpl(MILLIS, COUNT)));
   }


   public void testDivide() {

      assertEquals(new TimeImpl(3, 2), new TimeImpl(6, 4).divide(2));
   }


   public void testZeroTime() {

      assertEquals(TimeImpl.ZERO, new TimeImpl(0L, 0L));
   }


   public void testAddMillis() {

      final long addMillis = 777L;
      final Time timeAdded = time.add(addMillis);
      assertEquals(new TimeImpl(MILLIS + addMillis, COUNT), timeAdded);
   }


   public void testAddZeroMillis() {

      final Time timeAdded = time.add(0L);
      assertSame(time, timeAdded);
   }


   public void testAddTime() {

      final long addMillis = 777L;
      final long addCount = 888L;
      final Time timeAdded = time.add(new TimeImpl(addMillis, addCount));
      assertEquals(new TimeImpl(MILLIS + addMillis, COUNT + addCount), timeAdded);
      assertSame(time.add(new TimeImpl(0, 0)), time);
   }


   public void testHashCode() {

      assertTrue(time.hashCode() != 0);
   }


   public void testToString() {

      assertNotNull(time.toString());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(time, ser.deserialize(ser.serialize(time)));
   }


   public void setUp() throws Exception {

      super.setUp();

      time = new TimeImpl(MILLIS, COUNT);
   }


   public void tearDown() throws Exception {

      time = null;

      super.tearDown();
   }


   public String toString() {

      return "TimeImplTest{" +
              "time=" + time +
              "} " + super.toString();
   }
}
