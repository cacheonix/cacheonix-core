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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.cluster.node.state.group.GroupMemberLeftEvent;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheNodeLeftMessage is send to a cache processor as a result of translation of synchronous {@link
 * GroupMemberLeftEvent}.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
@SuppressWarnings("RedundantIfStatement")
public final class CacheNodeLeftMessage extends LocalCacheMessage {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheNodeLeftMessage.class); // NOPMD

   private GroupMember cacheGroupMember = null;


   public CacheNodeLeftMessage() {

   }


   public CacheNodeLeftMessage(final String cacheName) {

      super(TYPE_CACHE_MEMBER_LEFT, cacheName);
   }


   public void setCacheGroupMember(final GroupMember cacheGroupMember) {

      this.cacheGroupMember = cacheGroupMember;
   }


   /**
    * {@inheritDoc}
    */
   protected void executeOperational() {

      // Notify waiter list
      final ClusterNodeAddress leftAddress = cacheGroupMember.getAddress();
      final CacheProcessor processor = getCacheProcessor();

      // Notify waiters
      processor.notifyNodeLeft(leftAddress);

      // Clear local cache
      final FrontCache frontCache = processor.getFrontCache();
      if (frontCache != null) {

         //noinspection ControlFlowStatementWithoutBraces
         LOG.debug("Clearing front cache '" + getCacheName() + "' at " + processor.getAddress()); // NOPMD

         frontCache.clear();
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      // This notification message should be executed regardless of state
      executeOperational();
   }


   public String toString() {

      return "CacheNodeLeftMessage{" +
              "cacheGroupMember=" + cacheGroupMember +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheNodeLeftMessage();
      }
   }
}