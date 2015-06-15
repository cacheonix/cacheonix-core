/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
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

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterRequest;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.processor.Waiter;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * A request to get key owners.
 */
public class GetKeyOwnerRequest extends ClusterRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private String cacheName;

   private int storageNumber;

   private int bucketNumber;


   /**
    * Required by wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public GetKeyOwnerRequest() {

   }


   public GetKeyOwnerRequest(final String cacheName, final int storageNumber, final int bucketNumber) {

      super(TYPE_CLUSTER_KEY_OWNER);
      this.cacheName = cacheName;
      this.storageNumber = storageNumber;
      this.bucketNumber = bucketNumber;
   }


   public String getCacheName() {

      return cacheName;
   }


   public int getStorageNumber() {

      return storageNumber;
   }


   public int getBucketNumber() {

      return bucketNumber;
   }


   protected void processNormal() throws IOException, InterruptedException {

      final ClusterProcessor processor = getClusterProcessor();

      final ReplicatedState state = processor.getProcessorState().getReplicatedState();
      final Group group = state.getGroup(Group.GROUP_TYPE_CACHE, cacheName);
      if (group == null) {
         postRetry("Cache " + cacheName + " is offline");
      } else {
         final Response response = createResponse(Response.RESULT_SUCCESS);
         response.setResult(group.getBucketOwner(storageNumber, bucketNumber));
         processor.post(response);
      }
   }


   protected void processBlocked() throws IOException, InterruptedException {

      postRetry("Cluster is blocked");
   }


   protected void processRecovery() {

      postRetry("Cluster is reconfiguring");
   }


   protected void processCleanup() throws IOException {

      postRetry("Cluster is reconfiguring");
   }


   private void postRetry(final String message) {

      final Response response = createResponse(Response.RESULT_RETRY);
      response.setResult(message);
      getProcessor().post(response);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeString(cacheName, out);
      out.writeInt(storageNumber);
      out.writeShort(bucketNumber);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      cacheName = SerializerUtils.readString(in);
      storageNumber = in.readInt();
      bucketNumber = in.readShort();
   }


   protected Waiter createWaiter() {

      return new SimpleWaiter(this);
   }


   public String toString() {

      return "GetKeyOwnersRequest{" +
              "cacheName='" + cacheName + '\'' +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetKeyOwnerRequest();
      }
   }
}