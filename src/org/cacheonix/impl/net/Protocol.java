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
package org.cacheonix.impl.net;

/**
 * A container for constants defining the Cacheonix protocol constants.
 */
public final class Protocol {

   /**
    * Protocol signature.
    */
   private static final String PROTOCOL_SIGNATURE = "cchnx";

   /**
    * Protocol magic number.
    */
   private static final int PROTOCOL_MAGIC_NUMBER = 65973751;

   /**
    * Protocol version.
    */
   public static final int PROTOCOL_VERSION = (byte) 5;


   private Protocol() {

   }


   /**
    * Returns a new array containing protocol signature bytes.
    *
    * @return the new array containing protocol signature bytes.
    */
   public static byte[] getProtocolSignature() {

      return PROTOCOL_SIGNATURE.getBytes();
   }


   /**
    * Returns a length of the protocol signature.
    *
    * @return the length of the protocol signature.
    */
   public static int getProtocolSignatureLength() {

      return PROTOCOL_SIGNATURE.length();
   }


   /**
    * Returns protocol magic number.
    *
    * @return protocol magic number.
    */
   public static int getProtocolMagicNumber() {

      return PROTOCOL_MAGIC_NUMBER;
   }


   /**
    * Returns protocol version.
    *
    * @return protocol version.
    */
   public static int getProtocolVersion() {

      return PROTOCOL_VERSION;
   }
}
