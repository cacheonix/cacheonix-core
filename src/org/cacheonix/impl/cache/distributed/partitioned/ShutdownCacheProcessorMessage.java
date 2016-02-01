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

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * This local message either immediately shutdowns a <code>CacheProcessor</code> or initiates a graceful shutdown
 * process by sending <code>LeaveCacheGroupAnnouncement</code>. The immediate shutdown occurs if the
 * <code>CacheProcessor</code> is in the blocked mode. The graceful shutdown occurs if the <code>CacheProcessor</code>
 * is in the operational mode.
 */
public final class ShutdownCacheProcessorMessage extends LocalCacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private final boolean gracefulShutdown;


   /**
    * Required by wireable.
    */
   private ShutdownCacheProcessorMessage() {

      gracefulShutdown = true;
   }


   /**
    * Creates ShutdownCacheProcessorMessage.
    *
    * @param cacheName        a cache name.
    * @param gracefulShutdown <code>true</code> means that the system will first announce the shutdown. See {@link
    *                         #executeBlocked()} for actual use.
    */
   public ShutdownCacheProcessorMessage(final String cacheName, final boolean gracefulShutdown) {

      super(TYPE_CACHE_SHUTDOWN_MESSAGE, cacheName);

      this.gracefulShutdown = gracefulShutdown;
   }


   /**
    * Operational means that the context CacheProcessor is servicing requests normally. We should initiate a graceful
    * shutdown.
    */
   protected void executeOperational() {

      if (gracefulShutdown) {

         // Initiate graceful shutdown
         getProcessor().post(new LeaveCacheGroupAnnouncement(getCacheName(), getProcessor().getAddress(), true));
      } else {

         // Shutdown now
         getProcessor().shutdown();
      }
   }


   /**
    * In Blocked state the context <code>CacheProcessor</code> is not servicing requests. This means that, according to
    * the context <code>CacheProcessor</code>'s knowledge, the parent <code>ClusterProcessor</code> is not sending
    * reliable mcast messages. This makes graceful shutdown of the context <code>CacheProcessor</code> impossible as it
    * requires announcing and confirming bucket ownership change. We should perform an immediate forced shutdown.
    */
   protected void executeBlocked() {

      if (gracefulShutdown) {

         // Announce forced group leave in case cluster recovers
         getProcessor().post(new LeaveCacheGroupAnnouncement(getCacheName(), getProcessor().getAddress(), false));

         // Request forced shutdown. We do this via a message to avoid a potential
         // side effect of forcibly shutting down the node which may prevent from
         // posting the leave LeaveCacheGroupAnnouncement. Using the message means
         // that LeaveCacheGroupAnnouncement gets a chance to be posted. This is
         // just in case because as of this writing post() dispatched to the cluster.
         getProcessor().post(new ShutdownCacheProcessorMessage(getCacheName(), false));
      } else {

         // Shutdown now
         getProcessor().shutdown();
      }
   }


   public String toString() {

      return "ShutdownCacheProcessorMessage{" +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new ShutdownCacheProcessorMessage(); // NOPMD
      }
   }
}
