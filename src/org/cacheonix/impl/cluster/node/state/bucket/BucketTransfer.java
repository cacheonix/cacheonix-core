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
package org.cacheonix.impl.cluster.node.state.bucket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * @noinspection SimplifiableIfStatement, RedundantIfStatement, ParameterNameDiffersFromOverriddenParameter
 */
public final class BucketTransfer implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * @noinspection UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransfer.class); // NOPMD

   private byte storageNumber = 0x0;

   private ClusterNodeAddress owner = null;


   /**
    * Creates BucketTransfer.
    *
    * @param storageNumber storage number
    * @param owner         the owner.
    */
   BucketTransfer(final byte storageNumber, final ClusterNodeAddress owner) {

      this.storageNumber = storageNumber;
      this.owner = owner;
   }


   /**
    * Default constructor required by <code>Externalizable</code>.
    */
   public BucketTransfer() {

   }


   public ClusterNodeAddress getOwner() {

      return owner;
   }


   public byte getStorageNumber() {

      return storageNumber;
   }


   public void readWire(final DataInputStream in) throws IOException {

      storageNumber = in.readByte();
      owner = SerializerUtils.readAddress(in);
   }


   public int getWireableType() {

      return TYPE_BUCKET_TRANSFER;
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeByte(storageNumber);
      SerializerUtils.writeAddress(owner, out);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (!(o instanceof BucketTransfer)) {
         return false;
      }

      final BucketTransfer that = (BucketTransfer) o;

      if (storageNumber != that.storageNumber) {
         return false;
      }
      if (!owner.equals(that.owner)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = storageNumber;
      result = 31 * result + owner.hashCode();
      return result;
   }


   public String toString() {

      return "BucketTransfer{" +
              "storage=" + storageNumber +
              ", owner=" + owner +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new BucketTransfer();
      }
   }
}