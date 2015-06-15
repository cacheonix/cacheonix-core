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
package org.cacheonix.impl.lock;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorKey;
import org.cacheonix.impl.net.processor.InvalidMessageException;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Requests a number of times a thread owner entered a lock.
 */
public final class EntryCountRequest extends LockRequest {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();


   /**
    * Required by Wireable.
    */
   public EntryCountRequest() {

   }


   public void validate() throws InvalidMessageException {

      super.validate();

      if (!getReceiver().isAddressOf(getProcessor().getAddress())) {
         throw new InvalidMessageException("Entry count request should be local");
      }
   }


   public EntryCountRequest(final String lockRegionName, final Binary lockKey, final ClusterNodeAddress ownerAddress,
                            final int ownerThreadID,
                            final String ownerThreadName, final boolean readLock) {

      super(TYPE_LOCK_ENTRY_COUNT_REQUEST, lockRegionName, lockKey, ownerAddress, ownerThreadID, ownerThreadName, readLock);
   }


   /**
    * {@inheritDoc}
    */
   protected ProcessorKey getProcessorKey() {

      return ClusterProcessorKey.getInstance();
   }


   public void execute() {

      final ClusterProcessor processor = getClusterProcessor();

      final LockRegistry lockRegistry = processor.getProcessorState().getReplicatedState().getLockRegistry();
      final LockQueue lockQueue = lockRegistry.getLockQueue(getLockRegionName(), getLockKey());
      final int lockEntryCount = lockQueue.getLockEntryCount(this);
      final Response response = createResponse(Response.RESULT_SUCCESS);
      response.setResult(lockEntryCount);
      processor.post(response);
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new EntryCountRequest();
      }
   }
}
