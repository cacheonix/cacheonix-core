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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.bucket.BucketOwnershipAssignment;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupMessage;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An request to the bucket ownership assignment to run repartitioning.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Oct 27, 2009 12:04:56 PM
 */
public final class RepartitionAnnouncement extends GroupMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RepartitionAnnouncement.class); // NOPMD


   public RepartitionAnnouncement() {

   }


   public RepartitionAnnouncement(final String cacheName) {

      super(TYPE_GROUP_ANNOUNCE_REPARTITIONING, Group.GROUP_TYPE_CACHE, cacheName);
   }


   public void execute() {

      final ReplicatedState replicatedState = getReplicatedState();
      final Group group = replicatedState.getGroup(getGroupType(), getGroupName());
      final BucketOwnershipAssignment bucketOwnershipAssignment = group.getBucketOwnershipAssignment();
      bucketOwnershipAssignment.repartition();
   }


   public String toString() {

      return "RepartitionAnnouncement{" +
              super.toString() +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RepartitionAnnouncement();
      }
   }
}