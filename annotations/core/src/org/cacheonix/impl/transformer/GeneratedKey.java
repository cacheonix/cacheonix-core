/**
 *
 */
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
package org.cacheonix.impl.transformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Used to create Cacheonix aggregate key
 */
public class GeneratedKey implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = -4199830171158115772L;

   private final Object[] keyElements;


   /**
    * Class constructor
    *
    * @param args variable array containing the arguments, each argument will be wrapped as an Object
    */
   public GeneratedKey(final Object... args) {

      final ArrayList<Object> tmpKey = new ArrayList<Object>(args.length);

      Collections.addAll(tmpKey, args);

      keyElements = tmpKey.toArray();
   }


   /*
     * (non-Javadoc)
     *
     * @see java.lang.Objectr#equals(java.lang.Object Object)
     */
   @Override
   public boolean equals(final Object obj) {

      if (obj instanceof GeneratedKey) {
         final GeneratedKey other = (GeneratedKey) obj;

         if (keyElements == null || other.keyElements == null
                 || (keyElements.length != other.keyElements.length)) {
            return false;
         }

         for (int i = 0; i < keyElements.length; ++i) {
            if (!keyElements[i].equals(other.keyElements[i])) {
               return false;
            }
         }
         return true;
      }
      return false;
   }


   /*
     * (non-Javadoc)
     *
     * @see java.lang.Objectr#hashCode()
     */
   @Override
   public int hashCode() {

      int hash = 1;

      if (keyElements != null) {
         for (int i = 0; i < keyElements.length; ++i) {
            hash = 31
                    * hash
                    + ((null == keyElements[i]) ? 0 : keyElements[i]
                    .hashCode());
         }
      }
      return hash;
   }


   /**
    * Returns a Boolean object
    *
    * @param Z boolean value that needs to be wrapped as an object
    * @return Object of type Boolean
    */
   public static Object addKeyElement(final boolean Z) {

      return Boolean.valueOf(Z); // NOPMD
   }


   /**
    * Returns a Character object
    *
    * @param c char value that needs to be wrapped as an object
    * @return Object of type Character
    */
   public static Object addKeyElement(final char c) {

      return Character.valueOf(c);
   }


   /**
    * Returns a Byte object
    *
    * @param B byte value that needs to be wrapped as an object
    * @return Object of type Byte
    */
   public static Object addKeyElement(final byte B) {

      return Byte.valueOf(B); // NOPMD
   }


   /**
    * Returns a Short object
    *
    * @param S byte value that needs to be wrapped as an object
    * @return Object of type Short
    */
   public static Object addKeyElement(final short S) // NOPMD
   {

      return Short.valueOf(S); // NOPMD
   }


   /**
    * Returns a Integer object
    *
    * @param I int value that needs to be wrapped as an object
    * @return Object of type Integer
    */
   public static Object addKeyElement(final int I) {

      return Integer.valueOf(I);// NOPMD
   }


   /**
    * Returns a Float object
    *
    * @param F float value that needs to be wrapped as an object
    * @return Object of type Float
    */
   public static Object addKeyElement(final float F) {

      return new Float(F);
   }


   /**
    * Returns a Long object
    *
    * @param J long value that needs to be wrapped as an object
    * @return Object of type Long
    */
   public static Object addKeyElement(final long J) {

      return Long.valueOf(J);
   }


   /**
    * Returns a Double object
    *
    * @param D double value that needs to be wrapped as an object
    * @return Object of type Double
    */
   public static Object addKeyElement(final double D) {

      return new Double(D);
   }


   /**
    * Returns a Object
    *
    * @param Oo Object value
    * @return Same Object that is received as parameter, if object is not null. In case, when object is null, returns
    *         String "???NULL#"
    */
   public static Object addKeyElement(final Object Oo) {

      if (Oo == null) {
         return "???NULL#";
      }
      return Oo;
   }

}
