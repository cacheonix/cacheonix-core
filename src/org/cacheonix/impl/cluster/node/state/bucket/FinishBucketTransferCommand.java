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
package org.cacheonix.impl.cluster.node.state.bucket;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Requests listeners to finish bucket transfer.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Oct 27, 2009 3:35:44 PM
 */
public final class FinishBucketTransferCommand extends BucketCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FinishBucketTransferCommand.class); // NOPMD

   private final byte sourceStorageNumber;

   private final byte destinationStorageNumber;

   private final ClusterNodeAddress previousOwner;

   private final ClusterNodeAddress newOwner;

   private List<Integer> bucketNumbers = null;


   FinishBucketTransferCommand(final String cacheName, final byte sourceStorageNumber,
                               final byte destinationStorageNumber, final ClusterNodeAddress previousOwner,
                               final ClusterNodeAddress newOwner) {

      super(cacheName);
      this.sourceStorageNumber = sourceStorageNumber;
      this.destinationStorageNumber = destinationStorageNumber;
      this.previousOwner = previousOwner;
      this.newOwner = newOwner;
   }


   public byte getSourceStorageNumber() {

      return sourceStorageNumber;
   }


   public byte getDestinationStorageNumber() {

      return destinationStorageNumber;
   }


   public ClusterNodeAddress getPreviousOwner() {

      return previousOwner;
   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   public void addBucketNumbers(final List<Integer> bucketNumbersToAdd) {

      getOrCreateBucketNumbers(bucketNumbersToAdd.size()).addAll(bucketNumbersToAdd);
   }


   private List<Integer> getOrCreateBucketNumbers(final int createsSizeIfEmpty) {

      if (bucketNumbers == null) {
         bucketNumbers = new ArrayList<Integer>(createsSizeIfEmpty);
      }
      return bucketNumbers;
   }


   public List<Integer> getBucketNumbers() {

      return getOrCreateBucketNumbers(0);
   }


   @Override
   public String toString() {

      return "FinishBucketTransferCommand{" +
              "sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              ", previousOwner=" + previousOwner +
              ", newOwner=" + newOwner +
              ", bucketNumbers=" + bucketNumbers +
              "} " + super.toString();
   }
}
