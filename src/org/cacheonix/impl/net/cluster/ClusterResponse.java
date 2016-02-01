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

import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Response to cluster requests.
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClusterResponse extends Response {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterResponse.class); // NOPMD


   public ClusterResponse() {

      super(TYPE_CLUSTER_RESPONSE);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return ClusterProcessorKey.getInstance();
   }


   public String toString() {

      return "ClusterResponse{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ClusterResponse();
      }
   }
}
