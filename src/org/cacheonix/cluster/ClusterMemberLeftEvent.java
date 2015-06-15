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
package org.cacheonix.cluster;

import java.util.Collection;

/**
 * An event that Cacheonix sends to subscribers when a member leaves a Cacheonix cluster.
 *
 * @see ClusterEventSubscriber#notifyClusterMemberLeft(ClusterMemberLeftEvent)
 */
public interface ClusterMemberLeftEvent {

   /**
    * Returns an unmodifiable collection containing ClusterMembers that left the cluster.
    *
    * @return the unmodifiable collection containing ClusterMembers that left the cluster.
    */
   Collection<ClusterMember> getLeftMembers();
}
