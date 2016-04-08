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
package org.cacheonix.impl.net.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * ClusterNodeLeftEventImpl
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 16, 2009 1:45:06 AM
 */
final class ClusterNodeLeftEventImpl implements ClusterNodeLeftEvent {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterNodeLeftEventImpl.class); // NOPMD

   private final List<ClusterNodeAddress> nodes;


   ClusterNodeLeftEventImpl(final Collection<ClusterNodeAddress> nodes) {

      this.nodes = new ArrayList<ClusterNodeAddress>(nodes);
   }


   @SuppressWarnings("unchecked")
   public Collection<ClusterNodeAddress> getNodes() {

      return Collections.unmodifiableCollection(nodes);
   }


   public String toString() {

      return "ClusterNodeLeftEventImpl{" +
              "nodes=" + nodes +
              '}';
   }
}
