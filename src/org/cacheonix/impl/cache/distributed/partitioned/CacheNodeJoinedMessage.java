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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheNodeJoinedMessage
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection RedundantIfStatement
 * @since Jul 13, 2009 10:34:17 PM
 */
public final class CacheNodeJoinedMessage extends LocalCacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheNodeJoinedMessage.class); // NOPMD

   private GroupMember cacheGroupMember = null;


   public CacheNodeJoinedMessage(final String cacheName) {

      super(TYPE_CACHE_MEMBER_JOINED, cacheName);
   }


   /**
    * Required by Externalizable.
    */
   public CacheNodeJoinedMessage() {

   }


   public void setCacheGroupMember(final GroupMember cacheGroupMember) {

      this.cacheGroupMember = cacheGroupMember;
   }


   /**
    * Processes cache member joined notification.
    */
   protected void executeOperational() {
      // REVIEWME: simeshev@cacheonix.org - 2009-11-07 - decide what
      // to do with it or if we need it. One of the uses is an API
      // that notifies about nodes joining and leaving.
   }


   protected void executeBlocked() {

      executeOperational();
   }


   public String toString() {

      return "CacheNodeJoinedMessage{" +
              "cacheGroupMember=" + cacheGroupMember +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheNodeJoinedMessage();
      }
   }
}
