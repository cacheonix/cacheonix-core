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
package org.cacheonix.impl.clock;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 * Tester for Time.
 */
public class TimeTest extends TestCase {

   private static final long MILLIS = 1000L;

   private static final long COUNT = 5L;

   private Time time;


   public void testDefaultConstructor() throws Exception {

      final Time defaultContructorTime = new Time();
      assertEquals(0, defaultContructorTime.getMillis());
      assertEquals(0, defaultContructorTime.getCount());
   }


   public void testGetMillis() throws Exception {

      assertEquals(MILLIS, time.getMillis());
   }


   public void testGetCount() throws Exception {

      assertEquals(COUNT, time.getCount());
   }


   public void testCompareTo() throws Exception {

      assertEquals(0, time.compareTo(new Time(MILLIS, COUNT)));

      assertEquals(1, time.compareTo(new Time(MILLIS - 1, COUNT)));
      assertEquals(1, time.compareTo(new Time(MILLIS, COUNT - 1)));
      assertEquals(1, time.compareTo(new Time(MILLIS - 1, COUNT - 1)));

      assertEquals(-1, time.compareTo(new Time(MILLIS + 1, COUNT)));
      assertEquals(-1, time.compareTo(new Time(MILLIS, COUNT + 1)));
      assertEquals(-1, time.compareTo(new Time(MILLIS + 1, COUNT + 1)));

      assertEquals(1, time.compareTo(new Object()));
   }


   public void testEquals() throws Exception {

      assertEquals(time, new Time(MILLIS, COUNT));
      assertFalse(new Time(MILLIS + 1, COUNT).equals(time));
      assertFalse(new Time(MILLIS, COUNT + 1).equals(time));
      assertTrue(time.equals(time));
      assertFalse(time.equals(null));
      assertFalse(time.equals(new Object()));
   }


   public void testSubtract() throws Exception {

      assertEquals(new Time(0, 0), time.subtract(new Time(MILLIS, COUNT)));
      assertEquals(new Time(-1, -1), time.subtract(new Time(MILLIS + 1, COUNT + 1)));
      assertNotSame(time, time.subtract(new Time(MILLIS, COUNT)));
   }


   public void testDivide() throws Exception {

      assertEquals(new Time(3, 2), new Time(6, 4).divide(2));
   }


   public void testZeroTime() {

      assertEquals(Time.ZERO, new Time(0L, 0L));
   }


   public void testAddMillis() {

      final long addMillis = 777L;
      final Time timeAdded = time.add(addMillis);
      assertEquals(new Time(MILLIS + addMillis, COUNT), timeAdded);
   }


   public void testAddZeroMillis() {

      final Time timeAdded = time.add(0L);
      assertSame(time, timeAdded);
   }


   public void testAddTime() {

      final long addMillis = 777L;
      final long addCount = 888L;
      final Time timeAdded = time.add(new Time(addMillis, addCount));
      assertEquals(new Time(MILLIS + addMillis, COUNT + addCount), timeAdded);
      assertSame(time.add(new Time(0, 0)), time);
   }


   public void testHashCode() throws Exception {

      assertTrue(time.hashCode() != 0);
   }


   public void testToString() throws Exception {

      assertNotNull(time.toString());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(time, ser.deserialize(ser.serialize(time)));
   }


   public void setUp() throws Exception {

      super.setUp();

      time = new Time(MILLIS, COUNT);
   }


   public void tearDown() throws Exception {

      time = null;

      super.tearDown();
   }


   public String toString() {

      return "TimeTest{" +
              "time=" + time +
              "} " + super.toString();
   }
}
