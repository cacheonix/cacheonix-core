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
package org.cacheonix.impl.cache.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.config.CacheonixConfiguration;
import org.cacheonix.impl.config.ConfigurationReader;
import org.cacheonix.impl.util.IOUtils;

/**
 */
public class StandardObjectSizeCalculatorTest extends TestCase {

   private static final StandardObjectSizeCalculator CALCULATOR = new StandardObjectSizeCalculator();


   @SuppressWarnings("RedundantStringConstructorCall")
   public void testString() {

      assertEquals(72, CALCULATOR.sizeOf(new String("Hello World!"))); // NOPMD Avoid instantiating String objects; this is usually unnecessary.
   }


   @SuppressWarnings("ConcatenationWithEmptyString")
   public void testIntegerToString() {

      for (int i = 0; i < 1; i++) {
         assertEquals(50, CALCULATOR.sizeOf("" + i)); // NOPMD Do not add empty strings
      }
   }


   static class Entry implements Map.Entry {

      final Object key;

      final Object value;

      final int hash;

      final Entry next;


      Entry(final int h, final Object k, final Object v, final Entry n) {

         value = v;
         next = n;
         key = k;
         hash = h;
      }


      public Object getKey() {

         return key;
      }


      public Object getValue() {

         return value;
      }


      public Object setValue(final Object value) {

         return value;
      }
   }


   @SuppressWarnings({"unchecked", "CachedNumberConstructorCall"})
   public void testHashMap() {

      assertEquals(200, CALCULATOR.sizeOf(new HashMap(16)));

      final Byte[] all = new Byte[256];
      for (int i = -128; i < 128; i++) {
         all[i + 128] = Byte.valueOf((byte) i);
      }
      assertEquals(6160, CALCULATOR.sizeOf(all));

      final HashMap hm = new HashMap(16);
      for (int i = -128; i < 128; i++) {
         hm.put(String.valueOf(i), new Byte((byte) i)); // NOPMD Avoid instantiating Byte objects. Call Byte.valueOf() instead
      }
      assertEquals(32148, CALCULATOR.sizeOf(hm));
   }


   public void testVector() {

      assertEquals(128, CALCULATOR.sizeOf(new Vector(10))); // NOPMD Use ArrayList instead of Vector
   }


   public void testArrayList() {

      assertEquals(120, CALCULATOR.sizeOf(new ArrayList(10)));
   }


   public void testObject() {

      assertEquals(8, CALCULATOR.sizeOf(new Object()));
   }


   public void testInteger() {

      assertEquals(16, CALCULATOR.sizeOf(Integer.valueOf(1)));
   }


   public void testCharArray() {

      assertEquals(40, CALCULATOR.sizeOf("Hello World!".toCharArray()));
   }


   public void testByte() {

      assertEquals(16, CALCULATOR.sizeOf(Byte.valueOf((byte) 10)));
   }


   public void testThreeBytes() {

      assertEquals(16, CALCULATOR.sizeOf(new ThreeBytes()));
   }


   public void testSixtyFourBoolean() {

      assertEquals(72, CALCULATOR.sizeOf(new SixtyFourBoolean()));
   }


   @SuppressWarnings("BooleanConstructorCall")
   public void testThousandBooleanObjects() {

      final Boolean[] booleanArray = new Boolean[1000];

      for (int i = 0; i < booleanArray.length; i++) {
         booleanArray[i] = new Boolean(true); // NOPMD
      }

      assertEquals(24016, CALCULATOR.sizeOf(booleanArray));
   }


   public void testThousandBytes() {

      assertEquals(1016, CALCULATOR.sizeOf(new byte[1000]));
   }


   @SuppressWarnings("CollectionWithoutInitialCapacity")
   public void testEmptyArrayList() {

      assertEquals(120, CALCULATOR.sizeOf(new ArrayList()));
   }


   @SuppressWarnings("unchecked")
   public void testFullArrayList() {

      final ArrayList arrayList = new ArrayList(10000);

      for (int i = 0; i < 10000; i++) {
         arrayList.add(new Object());
      }

      assertEquals(160040, CALCULATOR.sizeOf(arrayList));
   }


   @SuppressWarnings("unchecked")
   public void testFullLinkedList() {

      final LinkedList linkedList = new LinkedList();

      for (int i = 0; i < 10000; i++) {
         linkedList.add(new Object());
      }

      assertEquals(400056, CALCULATOR.sizeOf(linkedList));
   }


   public void testComplexClass() {

      assertEquals(48, CALCULATOR.sizeOf(new ComplexClass()));
   }


   public void testBooleanArray() {

      assertEquals(27, CALCULATOR.sizeOf(new boolean[11]));
   }


   public void testShortArray() {

      assertEquals(38, CALCULATOR.sizeOf(new short[11]));
   }


   public void testIntArray() {

      assertEquals(60, CALCULATOR.sizeOf(new int[11]));
   }


   public void testFloatArray() {

      assertEquals(60, CALCULATOR.sizeOf(new float[11]));
   }


   public void testLongArray() {

      assertEquals(104, CALCULATOR.sizeOf(new long[11]));
   }


   public void testDoubleArray() {

      assertEquals(104, CALCULATOR.sizeOf(new double[11]));
   }


   public void testSizeOfWithCircularReference() throws IOException {

      final FileInputStream testFileInputStream = TestUtils.getTestFileInputStream(TestConstants.CACHEONIX_CLUSTER_XML);
      try {
         final ConfigurationReader reader = new ConfigurationReader();
         final CacheonixConfiguration config = reader.readConfiguration(testFileInputStream);
         assertEquals(1648, CALCULATOR.sizeOf(config));
      } finally {
         IOUtils.closeHard(testFileInputStream);
      }
   }


   static class ThreeBytes {

      byte b0;

      byte b1;

      byte b2;
   }

   private static class ComplexClass {

      ComplexClass cc = this;

      boolean z;

      byte b;

      char c;

      double d;

      float f;

      int i;

      long l;

      short s;
   }

   private static class SixtyFourBoolean {

      boolean a0;

      boolean a1;

      boolean a2;

      boolean a3;

      boolean a4;

      boolean a5;

      boolean a6;

      boolean a7;

      boolean b0;

      boolean b1;

      boolean b2;

      boolean b3;

      boolean b4;

      boolean b5;

      boolean b6;

      boolean b7;

      boolean c0;

      boolean c1;

      boolean c2;

      boolean c3;

      boolean c4;

      boolean c5;

      boolean c6;

      boolean c7;

      boolean d0;

      boolean d1;

      boolean d2;

      boolean d3;

      boolean d4;

      boolean d5;

      boolean d6;

      boolean d7;

      boolean e0;

      boolean e1;

      boolean e2;

      boolean e3;

      boolean e4;

      boolean e5;

      boolean e6;

      boolean e7;

      boolean f0;

      boolean f1;

      boolean f2;

      boolean f3;

      boolean f4;

      boolean f5;

      boolean f6;

      boolean f7;

      boolean g0;

      boolean g1;

      boolean g2;

      boolean g3;

      boolean g4;

      boolean g5;

      boolean g6;

      boolean g7;

      boolean h0;

      boolean h1;

      boolean h2;

      boolean h3;

      boolean h4;

      boolean h5;

      boolean h6;

      boolean h7;
   }
}


