/**
 *
 */
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
package org.cacheonix.impl.transformer;

import junit.framework.TestCase;

/**
 *
 */
public class GeneratedKeyTest extends TestCase {

   private static final String key1 = "key1";

   private static final String key2 = "key2";

   private static final String key3 = "key3";

   private static final String key4 = "key4";

   private static final int ikey1 = 1;

   private static final double dkey1 = 34.56D;


   /**
    * Test method for {@link GeneratedKey#hashCode()} .
    */
   public void testHashCode() {

      final GeneratedKey key = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      final int hashCode = key.hashCode();

      final GeneratedKey hk = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      final int hk2 = hk.hashCode();

      assertEquals(hashCode, hk2);

      final GeneratedKey hkf = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1 + 33), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      final int hk3 = hkf.hashCode();

      assertFalse(hk3 == hk2);
   }


   /**
    * Test method for {@link GeneratedKey#GeneratedKey(Object[])} .
    */
   public void testGeneratedKey() {

      final GeneratedKey key = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      assertNotNull(key);
   }


   /**
    * Test method for {@link GeneratedKey#equals(Object)}.
    */
   public void testEqualsObject() {

      final GeneratedKey key = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      final GeneratedKey hk = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      assertEquals(key, hk);

      final GeneratedKey hkf = new GeneratedKey(GeneratedKey
              .addKeyElement(ikey1 + 33), GeneratedKey.addKeyElement(dkey1),
              GeneratedKey.addKeyElement(key1), GeneratedKey
              .addKeyElement(key2), GeneratedKey.addKeyElement(key3),
              GeneratedKey.addKeyElement(key4));

      assertFalse(key.equals(hkf));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(boolean)}.
    */
   public void testAddKeyElementBoolean() {

      assertEquals(GeneratedKey.addKeyElement(ikey1), new Integer(ikey1));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(char)}.
    */
   public void testAddKeyElementChar() {

      assertEquals(GeneratedKey.addKeyElement(true), new Boolean(true)); // NOPMD
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(byte)}.
    */
   public void testAddKeyElementByte() {

      final byte bt = ikey1 & 0xFF; // NOPMD
      assertEquals(GeneratedKey.addKeyElement(bt), new Byte((byte) (ikey1 & 0xFF))); // NOPMD
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(short)}.
    */
   public void testAddKeyElementShort() {

      final short bt = ikey1 & 0xFF; //NOPMD
      assertEquals(GeneratedKey.addKeyElement(bt), new Short((short) (ikey1 & 0xFF))); // NOPMD
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(int)}.
    */
   public void testAddKeyElementInt() {

      assertEquals(GeneratedKey.addKeyElement(ikey1), new Integer(ikey1));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(float)}.
    */
   public void testAddKeyElementFloat() {

      assertEquals(GeneratedKey.addKeyElement((float) ikey1), new Float(
              (float) ikey1));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(long)}.
    */
   public void testAddKeyElementLong() {

      assertEquals(GeneratedKey.addKeyElement((long) ikey1), new Long(
              (long) ikey1));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(double)}.
    */
   public void testAddKeyElementDouble() {

      assertEquals(GeneratedKey.addKeyElement((double) ikey1), new Double(
              (double) ikey1));
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(Object)} .
    */
   public void testAddKeyElementObject() {

      assertEquals(GeneratedKey.addKeyElement(key1), new String(key1)); // NOPMD
   }


   /**
    * Test method for {@link GeneratedKey#addKeyElement(Object)}.
    */
   public void testAddKeyElementNULL() {

      final Object in = GeneratedKey.addKeyElement(null);
      assertNotNull(in);
   }

}
