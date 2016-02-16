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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.IntegerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Requests listeners to begin bucket transfer.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Oct 23, 2009 1:41:20 PM
 */
public final class BeginBucketTransferCommand extends BucketCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BeginBucketTransferCommand.class); // NOPMD

   private final byte sourceStorageNumber;

   private final byte destinationStorageNumber;

   private final ClusterNodeAddress currentOwner;

   private final ClusterNodeAddress newOwner;

   private final List<Integer> bucketNumbers;


   BeginBucketTransferCommand(final String cacheName, final byte sourceStorageNumber,
           final byte destinationStorageNumber, final ClusterNodeAddress currentOwner,
           final ClusterNodeAddress newOwners) {

      super(cacheName);
      this.destinationStorageNumber = destinationStorageNumber;
      this.sourceStorageNumber = sourceStorageNumber;
      this.bucketNumbers = new LinkedList<Integer>();
      this.currentOwner = currentOwner;
      this.newOwner = newOwners;
   }


   public byte getSourceStorageNumber() {

      return sourceStorageNumber;
   }


   public byte getDestinationStorageNumber() {

      return destinationStorageNumber;
   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   /**
    * Returns an unmodifiable collection of integers bucket numbers.
    *
    * @return an unmodifiable list of bucket numbers
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public List<Integer> getBucketNumbers() {

      return Collections.unmodifiableList(bucketNumbers);
   }


   public ClusterNodeAddress getCurrentOwner() {

      return currentOwner;
   }


   public void addBucketNumber(final int bucketNumber) {

      bucketNumbers.add(IntegerUtils.valueOf(bucketNumber));
   }


   public String toString() {

      return "BeginBucketTransferCommand{" +
              "currentOwner=" + currentOwner +
              ", newOwner=" + newOwner +
              ", sourceStorage=" + sourceStorageNumber +
              ", destinationStorage=" + destinationStorageNumber +
              ", bucketNumbers=" + bucketNumbers +
              '}';
   }
}
