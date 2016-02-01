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
import java.util.Arrays;

import org.cacheonix.impl.net.cluster.ClusterProcessorKey;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AggregatingCacheResponse
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 4, 2010 11:50:33 PM
 */
@SuppressWarnings({"RedundantIfStatement", "WeakerAccess"})
public final class AggregatingAnnouncementResponse extends Response {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AggregatingAnnouncementResponse.class); // NOPMD

   private int[] processedBuckets = null;


   /**
    * Required by <code>Wireable</code>.
    */
   public AggregatingAnnouncementResponse() {

      super(TYPE_AGGREGATING_ANNOUNCEMENT_RESPONSE);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ClusterProcessorKey.getInstance();
   }


   public int[] handOffProcessedBuckets() {

      final int[] result = processedBuckets;
      processedBuckets = null;

      return result;
   }


   public boolean isProcessedBucketsEmpty() {

      return processedBuckets == null || processedBuckets.length == 0;
   }


   public void setProcessedBuckets(final int[] processedBuckets) {

      this.processedBuckets = ArrayUtils.copy(processedBuckets);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      processedBuckets = SerializerUtils.readIntArray(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeIntArray(out, processedBuckets);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final AggregatingAnnouncementResponse response = (AggregatingAnnouncementResponse) o;

      if (!Arrays.equals(processedBuckets, response.processedBuckets)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (processedBuckets != null ? Arrays.hashCode(processedBuckets) : 0);
      return result;
   }


   @Override
   public String toString() {

      return "AggregatingAnnouncementResponse{" +
              "processed.size()=" + (processedBuckets == null ? "null" : Integer.toString(processedBuckets.length)) +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new AggregatingAnnouncementResponse();
      }
   }
}
