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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.SimpleWaiter;
import org.cacheonix.impl.net.processor.Waiter;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Obtains size of the ClusterView at time time of request.
 */
public final class GetClusterViewSizeRequest extends ClusterRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();


   /**
    * Required by Wireable.
    */
   public GetClusterViewSizeRequest() {

      super(TYPE_CLUSTER_GET_CLUSTER_VIEW_SIZE);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation obtains the cluster view size and posts a response with the result set to integer size.
    */
   public void execute() {

      // Get size
      final ClusterProcessor processor = getClusterProcessor();

      final ClusterView clusterView = processor.getProcessorState().getClusterView();
      final int size = clusterView.getSize();

      // Respond
      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(size);
      processor.post(response);
   }


   /**
    * {@inheritDoc}
    */
   protected void processNormal() {

      // Get size
      final ClusterProcessor processor = getClusterProcessor();

      final ClusterView clusterView = processor.getProcessorState().getClusterView();
      final int size = clusterView.getSize();

      // Respond
      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(size);
      processor.post(response);
   }


   /**
    * {@inheritDoc}
    */
   protected void processBlocked() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    */
   protected void processRecovery() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    */
   protected void processCleanup() {

      processNormal();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns a new instance of <code>SimpleWaiter</code>.
    *
    * @see SimpleWaiter
    */
   protected Waiter createWaiter() {

      return new SimpleWaiter(this);
   }


   public String toString() {

      return "GetClusterViewSizeRequest{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new GetClusterViewSizeRequest();
      }
   }
}
