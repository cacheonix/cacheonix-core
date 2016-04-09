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

import java.util.Collection;
import java.util.Collections;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A a local message that restores a primary bucket from a local replica.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Sep 1, 2009 4:19:13 PM
 */
public final class RestoreBucketMessage extends LocalCacheMessage {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RestoreBucketMessage.class); // NOPMD

   private Collection<Integer> bucketNumbers = null;

   private int fromStorageNumber = 0;


   /**
    * Required by wireable.
    */
   public RestoreBucketMessage() {

   }


   /**
    * Creates new <code>RestoreBucketMessage</code>.
    *
    * @param cacheName         a cache name.
    * @param bucketNumber      a bucket number to restore
    * @param fromStorageNumber a storage number from that to restore a primary bucket.
    * @param receiver          address of the receiver.
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public RestoreBucketMessage(final String cacheName, final Collection<Integer> bucketNumber,
                               final int fromStorageNumber, final ClusterNodeAddress receiver) {

      super(TYPE_CACHE_RESTORE_BUCKET_MESSAGE, cacheName);
      this.fromStorageNumber = validateIsReplicaStorage(fromStorageNumber);
      this.bucketNumbers = bucketNumber;
      setReceiver(receiver);
      setSender(receiver);
   }


   /**
    * Returns a direct access list well-suited for index iteration.
    *
    * @return a direct access list well-suited for index iteration.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   private Collection<Integer> getBucketNumbers() {

      return bucketNumbers == null ? Collections.<Integer>emptyList() : bucketNumbers;
   }


   /**
    * {@inheritDoc}
    */
   protected void executeOperational() {

      final CacheProcessor cacheProcessor = getCacheProcessor();
      for (final Integer bucketNumber : getBucketNumbers()) {

         // REVIEWME: slava@cacheonix.org - 2009-12-17 -> What about us thinking that we already
         // own the bucket and serving PutRequest even though we have not restored the bucket
         // yet. We way need a direct AssignBucketOwnershipCommand to explicitly set up
         // non-null buckets and treat all null buckets as not ours *yet*.

         cacheProcessor.restorePrimaryBucket(bucketNumber, fromStorageNumber);
      }
   }


   /**
    * {@inheritDoc}
    */
   protected void executeBlocked() {

      // This is a purely local notification related only 
      // to the restoring a bucket from the local replica.
      // As such it can be processed even in the blocked state.
      executeOperational();
   }


   /**
    * Validates that storage number is replica storage.
    *
    * @param fromStorageNumber a storage number from that to restore a bucket.
    * @return a valid replica storage number.
    */
   private static int validateIsReplicaStorage(final int fromStorageNumber) {

      if (fromStorageNumber <= 0) {
         throw new IllegalArgumentException("Can restore bucket only from replica: " + fromStorageNumber);
      }

      return fromStorageNumber;
   }


   public String toString() {

      return "RestoreBucketMessage{" +
              "bucketNumbers=" + bucketNumbers +
              ", fromStorageNumber=" + fromStorageNumber +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new RestoreBucketMessage();
      }
   }
}
