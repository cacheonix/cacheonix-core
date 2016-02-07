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

import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.cluster.ClusterConfiguration;
import org.cacheonix.cluster.ClusterMember;
import org.cacheonix.cluster.ClusterMemberJoinedEvent;

/**
 *
 */
public final class ClusterMemberJoinedEventImpl implements ClusterMemberJoinedEvent {

   /**
    * A collection of joined members.
    */
   private final Collection<ClusterMember> joinedMembers;

   /**
    * A cluster configuration after the members joined.
    */
   private final ClusterConfiguration clusterConfiguration;


   /**
    * Creates ClusterMemberJoinedEventImpl.
    *
    * @param clusterConfiguration a cluster configuration after the members joined.
    * @param joinedMembers        a list of joined members.
    */
   public ClusterMemberJoinedEventImpl(final ClusterConfiguration clusterConfiguration,
           final Collection<ClusterMember> joinedMembers) {

      this.clusterConfiguration = clusterConfiguration;

      this.joinedMembers = new ArrayList<ClusterMember>(joinedMembers);
   }


   /**
    * Returns the cluster configuration after the members joined.
    *
    * @return the cluster configuration after the members joined.
    */
   public ClusterConfiguration getClusterConfiguration() {

      return clusterConfiguration;
   }


   /**
    * Returns an unmodifiable collection containing ClusterMembers that joined the cluster.
    *
    * @return the unmodifiable collection containing ClusterMembers that joined the cluster.
    */
   public Collection<ClusterMember> getJoinedMembers() {

      return new ArrayList<ClusterMember>(joinedMembers);
   }


   public String toString() {

      return "ClusterMemberJoinedEventImpl{" +
              "joinedMembers=" + joinedMembers +
              '}';
   }
}
