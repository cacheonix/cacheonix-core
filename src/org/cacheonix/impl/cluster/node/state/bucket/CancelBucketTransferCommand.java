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
package org.cacheonix.impl.cluster.node.state.bucket;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.IntegerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Requests listeners to cancel bucket transfer.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Oct 27, 2009 5:29:43 PM
 */
public final class CancelBucketTransferCommand extends BucketCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CancelBucketTransferCommand.class); // NOPMD

   private final List<Integer> bucketNumbers = new LinkedList<Integer>();

   private final ClusterNodeAddress previousOwner;

   private final ClusterNodeAddress newOwner;

   private final byte sourceStorageNumber;

   private final byte destinationStorageNumber;


   /**
    * @param sourceStorageNumber
    * @param destinationStorageNumber
    * @param previousOwner
    * @param newOwner                 @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   CancelBucketTransferCommand(final String cacheName, final byte sourceStorageNumber,
                               final byte destinationStorageNumber,
                               final ClusterNodeAddress previousOwner,
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


   /**
    * Returns an unmodifiable collection of bucket numbers.
    *
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public List<Integer> getBucketNumbers() {

      return bucketNumbers;
   }


   public ClusterNodeAddress getPreviousOwner() {

      return previousOwner;
   }


   public ClusterNodeAddress getNewOwner() {

      return newOwner;
   }


   public void addBucketNumber(final int bucketNumber) {

      bucketNumbers.add(IntegerUtils.valueOf(bucketNumber));
   }


   public void addBucketNumbers(final Collection<Integer> bucketNumbers) {

      this.bucketNumbers.addAll(bucketNumbers);
   }


   public String toString() {

      return "CancelBucketTransferCommand{" +
              "bucketNumbers=" + bucketNumbers +
              ", newOwner=" + newOwner +
              ", previousOwner=" + previousOwner +
              ", sourceStorageNumber=" + sourceStorageNumber +
              ", destinationStorageNumber=" + destinationStorageNumber +
              '}';
   }
}
