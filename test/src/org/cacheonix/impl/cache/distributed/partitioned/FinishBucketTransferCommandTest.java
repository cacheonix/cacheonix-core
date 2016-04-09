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

import java.util.Collections;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BeginBucketTransferCommandTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Nov 18, 2009 7:35:21 PM
 */
public final class FinishBucketTransferCommandTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(FinishBucketTransferCommandTest.class); // NOPMD

   private static final String CACHE_NAME = "test.cache.name";

   private static final byte SOURCE_STORAGE_NUMBER = 1;

   private static final byte DESTINATION_STORAGE_NUMBER = 2;

   private static final ClusterNodeAddress CURRENT_OWNER = TestUtils.createTestAddress(SOURCE_STORAGE_NUMBER);

   private static final ClusterNodeAddress NEW_OWNER = TestUtils.createTestAddress(2);

   private static final Integer BUCKET_NUMBER = Integer.valueOf(5);

   private FinishBucketTransferCommand command = null;


   public void testGetSourceStorageNumber() {

      assertEquals(SOURCE_STORAGE_NUMBER, command.getSourceStorageNumber());
   }


   public void testGetDestinationStorageNumber() {

      assertEquals(DESTINATION_STORAGE_NUMBER, command.getDestinationStorageNumber());
   }


   public void testGetNewOwner() {

      assertEquals(NEW_OWNER, command.getNewOwner());
   }


   public void testGetBucketNumber() {

      assertEquals(BUCKET_NUMBER, command.getBucketNumbers().get(0));
   }


   public void testGetCurrentOwner() {

      assertEquals(CURRENT_OWNER, command.getPreviousOwner());
   }


   protected void setUp() throws Exception {

      super.setUp();
      command = new FinishBucketTransferCommand(CACHE_NAME, SOURCE_STORAGE_NUMBER, DESTINATION_STORAGE_NUMBER, CURRENT_OWNER, NEW_OWNER);
      command.addBucketNumbers(Collections.singletonList(BUCKET_NUMBER));
   }


   public String toString() {

      return "FinishBucketTransferCommandTest{" +
              "command=" + command +
              "} " + super.toString();
   }
}