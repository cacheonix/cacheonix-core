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
package org.cacheonix.impl.cluster;

import org.cacheonix.cluster.ClusterState;
import org.cacheonix.cluster.ClusterStateChangedEvent;

/**
 * An implementation of {@link ClusterStateChangedEvent}.
 */
public final class ClusterStateChangedEventImpl implements ClusterStateChangedEvent {

   private final ClusterState newClusterState;


   /**
    * Creates ClusterStateChangedEventImpl.
    *
    * @param newClusterState a new cluster state.
    */
   public ClusterStateChangedEventImpl(final ClusterState newClusterState) {

      this.newClusterState = newClusterState;
   }


   /**
    * Returns a new cluster state.
    *
    * @return the new cluster state.
    */
   public ClusterState getNewClusterState() {

      return newClusterState;
   }


   public String toString() {

      return "ClusterStateChangedEventImpl{" +
              "newClusterState=" + newClusterState +
              '}';
   }
}
