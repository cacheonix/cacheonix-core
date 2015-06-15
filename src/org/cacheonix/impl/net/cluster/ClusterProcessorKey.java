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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.SimpleProcessorKey;
import org.cacheonix.impl.net.serializer.Wireable;

/**
 *
 */
public final class ClusterProcessorKey extends SimpleProcessorKey {

   private static final ClusterProcessorKey INSTANCE = new ClusterProcessorKey();


   private ClusterProcessorKey() {

      super(Wireable.DESTINATION_CLUSTER_PROCESSOR);
   }


   public static ClusterProcessorKey getInstance() {

      return INSTANCE;
   }
}
