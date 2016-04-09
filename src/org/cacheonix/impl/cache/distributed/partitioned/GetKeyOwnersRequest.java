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
public final class GetKeyOwnersRequest extends ClusterRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private String cacheName;


   /**
    * Required by wireable.
    */
   @SuppressWarnings("UnusedDeclaration")
   public GetKeyOwnersRequest() {

   }


   public GetKeyOwnersRequest(final String cacheName) {

      super(TYPE_CLUSTER_KEY_OWNERS);
      this.cacheName = cacheName;
   }


   public String getCacheName() {

      return cacheName;
   }


   protected void processNormal() {

      final ClusterProcessor processor = getClusterProcessor();

      final ReplicatedState state = processor.getProcessorState().getReplicatedState();
      final Group group = state.getGroup(Group.GROUP_TYPE_CACHE, cacheName);
      if (group == null) {
         postRetry("Cache " + cacheName + " is offline");
      } else {
         final Response response = createResponse(Response.RESULT_SUCCESS);
         response.setResult(group.getPartitionContributorsAddresses());
         processor.post(response);
      }
   }


   protected void processBlocked() {

      postRetry("Cluster is blocked");
   }


   protected void processRecovery() {

      postRetry("Cluster is reconfiguring");
   }


   protected void processCleanup() {

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
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      cacheName = SerializerUtils.readString(in);
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
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetKeyOwnersRequest();
      }
   }
}
