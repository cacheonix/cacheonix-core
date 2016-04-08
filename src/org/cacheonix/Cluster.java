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
package org.cacheonix;

import java.io.Serializable;

import org.cacheonix.locks.ReadWriteLock;

/**
 * An object that represents a cluster that a Cacheonix is a member of.
 *
 * @see Cacheonix#getCluster()
 */
public interface Cluster {

   /**
    * Returns a cluster-wide, distributed lock. This method is equavalent of <code>getReadWriteLock("default")</code>.
    * The created lock is accessible by all members of the cluster.
    *
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock();

   /**
    * Returns a cluster-wide, distributed lock. The created lock is accessible by all members of the cluster.
    *
    * @param lockKey the key that uniquely identifies the lock.
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock(final Serializable lockKey);
}
