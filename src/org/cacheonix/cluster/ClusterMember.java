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
package org.cacheonix.cluster;

import java.io.Externalizable;
import java.util.List;

/**
 * A Cacheonix cluster member is an instance of Cacheonix that is a part of the cluster. A cluster consists of a set of
 * <code>ClusterMembers</code>.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface ClusterMember extends Externalizable {

   /**
    * Returns a name of the cluster this cluster member belongs to.
    *
    * @return the name of the cluster this cluster member belongs to.
    */
   String getClusterName();

   /**
    * Returns an list of <code>ClusterMemberAddress</code> objects that this cluster member may be accessed at.
    *
    * @return a list of <code>ClusterMemberAddress</code> objects that this cluster member may be accessed at.
    */
   List<ClusterMemberAddress> getClusterMemberAddresses();
}
