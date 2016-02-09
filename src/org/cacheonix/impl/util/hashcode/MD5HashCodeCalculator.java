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
package org.cacheonix.impl.util.hashcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * MD5HashCodeCalculator
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 13, 2008 6:57:55 PM
 */
public final class MD5HashCodeCalculator implements HashCodeCalculator, Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(MD5HashCodeCalculator.class); // NOPMD

   private static final long serialVersionUID = 0L;

   /**
    * MD5
    */
   private transient MessageDigest md5;

   /**
    * Hash.
    */
   private int hash;

   /**
    * Indicates if the hash code was calculated.
    */
   private boolean hashCodeCalculated = false;


   public MD5HashCodeCalculator() {

      initMD5();
   }


   /**
    * Returns MessageDigest.
    *
    * @return MD5 MessageDigest
    */
   private MessageDigest getMD5() {

      return md5;
   }


   /**
    * Initializes the internal data structures.
    */
   public void init() {

      hash = 0;
   }


   /**
    * Adds value to the hash code.
    *
    * @param value
    */
   public void add(final byte value) {

      md5.update(value);
   }


   /**
    * Adds byte array value to the hash code.
    *
    * @param value
    */
   public void add(final byte[] value) {

      md5.update(value);
   }


   /**
    * Returns accumulated hash code.
    *
    * @return
    */
   public int calculate() {

      if (hashCodeCalculated) {
         return hash;
      } else {
         final byte[] bytes = md5.digest();
         long v = 0;
         int i = 0;
         v |= (long) bytes[i++] << 56 & 0xFF00000000000000L;
         v |= (long) bytes[i++] << 48 & 0xFF000000000000L;
         v |= (long) bytes[i++] << 40 & 0xFF0000000000L;
         v |= (long) bytes[i++] << 32 & 0xFF00000000L;
         v |= (long) bytes[i++] << 24 & 0xFF000000L;
         v |= (long) bytes[i++] << 16 & 0xFF0000L;
         v |= (long) bytes[i++] << 8 & 0xFF00L;
         //noinspection PointlessBitwiseExpression
         v |= (long) bytes[i++] << 0 & 0xFFL;
         hash = (int) (v ^ v >>> 32);
         return hash;
      }
   }


   public void readWire(final DataInputStream in) throws IOException {

      initMD5();
      hashCodeCalculated = in.readBoolean();
      hash = in.readInt();
   }


   public int getWireableType() {

      return TYPE_MD5_HASH_CODE_CALC;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      if (!hashCodeCalculated) {
         calculate();
      }
      out.writeBoolean(hashCodeCalculated);
      out.writeInt(hash);
   }


   private void initMD5() {

      try {
         md5 = MessageDigest.getInstance("MD5");
      } catch (final NoSuchAlgorithmException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public String toString() {

      return "MD5HashCodeCalculator{" +
              "md5=" + md5 +
              ", hash=" + hash +
              ", hashCodeCalculated=" + hashCodeCalculated +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new MD5HashCodeCalculator();
      }
   }
}
