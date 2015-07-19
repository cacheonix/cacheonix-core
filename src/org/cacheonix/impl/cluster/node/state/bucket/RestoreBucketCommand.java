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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * RestorePrimaryBucketCommand
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Oct 31, 2009 6:34:12 PM
 */
public final class RestoreBucketCommand extends BucketCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RestoreBucketCommand.class); // NOPMD

   private final ClusterNodeAddress address;

   private final byte fromStorageNumber;

   private final Collection<Integer> bucketNumbers;


   public RestoreBucketCommand(final String cacheName, final byte fromStorageNumber, final ClusterNodeAddress address) {

      super(cacheName);
      this.bucketNumbers = new LinkedList<Integer>();
      this.fromStorageNumber = fromStorageNumber;
      this.address = address;
   }


   public void addBucketNumber(final Integer bucketNumber) {

      bucketNumbers.add(bucketNumber);
   }


   /**
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public Collection<Integer> getBucketNumbers() {

      return Collections.unmodifiableCollection(bucketNumbers);
   }


   public byte getFromStorageNumber() {

      return fromStorageNumber;
   }


   public ClusterNodeAddress getAddress() {

      return address;
   }


   public String toString() {

      return "RestoreBucketCommand{" +
              "bucketNumbers=" + bucketNumbers +
              ", fromStorageNumber=" + fromStorageNumber +
              ", fromAddress=" + address +
              '}';
   }
}
