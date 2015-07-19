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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.StringUtils;

/**
 * A specialised response to <code>TransferBucketRequest</code>
 */
@SuppressWarnings("RedundantIfStatement")
public final class TransferBucketResult implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private List<Integer> rejectedBucketNumbers = null;

   private List<Integer> transferredBucketNumbers = null;


   /**
    * Required by Wireable.
    */
   @SuppressWarnings("RedundantNoArgConstructor")
   public TransferBucketResult() { // NOPMD

   }


   public void setRejectedBucketNumbers(final List<Integer> rejectedBucketNumbers) {

      this.rejectedBucketNumbers = CollectionUtils.copy(rejectedBucketNumbers);
   }


   public void setTransferredBucketNumbers(final List<Integer> transferredBucketNumbers) {

      this.transferredBucketNumbers = CollectionUtils.copy(transferredBucketNumbers);
   }


   /**
    * Returns an unmodifiable list with rejected buckets.
    *
    * @return the unmodifiable list with rejected buckets.
    */
   public List<Integer> getRejectedBucketNumbers() {

      return rejectedBucketNumbers;
   }


   public boolean hasTransferredBuckets() {

      return !CollectionUtils.isEmpty(transferredBucketNumbers);
   }


   public boolean hasRejectedBuckets() {

      return !CollectionUtils.isEmpty(rejectedBucketNumbers);
   }


   public List<Integer> getTransferredBucketNumbers() {

      return transferredBucketNumbers;
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException {

      rejectedBucketNumbers = SerializerUtils.readShortList(in);
      transferredBucketNumbers = SerializerUtils.readShortList(in);
   }


   public int getWireableType() {

      return Wireable.TYPE_TRANSFER_BUCKET_RESULT;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeShortList(out, rejectedBucketNumbers);
      SerializerUtils.writeShortList(out, transferredBucketNumbers);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final TransferBucketResult result = (TransferBucketResult) o;

      if (transferredBucketNumbers != null ? !transferredBucketNumbers.equals(result.transferredBucketNumbers) : result.transferredBucketNumbers != null) {
         return false;
      }
      if (rejectedBucketNumbers != null ? !rejectedBucketNumbers.equals(result.rejectedBucketNumbers) : result.rejectedBucketNumbers != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = rejectedBucketNumbers != null ? rejectedBucketNumbers.hashCode() : 0;
      result = 31 * result + (transferredBucketNumbers != null ? transferredBucketNumbers.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "TransferBucketResponse{" +
              "completedBucketNumbers=" + StringUtils.sizeToString(transferredBucketNumbers) +
              "rejectedBuckets=" + StringUtils.sizeToString(rejectedBucketNumbers) +
              "} ";
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new TransferBucketResult();
      }
   }
}
