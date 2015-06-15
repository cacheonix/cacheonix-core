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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BeginBucketTransferCommandTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Nov 18, 2009 7:35:21 PM
 */
public final class RestoreBucketCommandTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RestoreBucketCommandTest.class); // NOPMD

   private static final String CACHE_NAME = "test.cache.name";

   private static final byte STORAGE_NUMBER = 1;

   private static final ClusterNodeAddress ADDRESS = TestUtils.createTestAddress(STORAGE_NUMBER);

   private static final Integer BUCKET_NUMBER = Integer.valueOf(5);

   private RestoreBucketCommand command = null;


   public void testGetStorageNumber() {

      assertEquals(STORAGE_NUMBER, command.getFromStorageNumber());
   }


   public void testGetBucketNumber() {

      assertEquals(BUCKET_NUMBER, command.getBucketNumbers().iterator().next());
   }


   public void testGetCurrentOwner() {

      assertEquals(ADDRESS, command.getAddress());
   }


   protected void setUp() throws Exception {

      super.setUp();
      command = new RestoreBucketCommand(CACHE_NAME, STORAGE_NUMBER, ADDRESS);
      command.addBucketNumber(BUCKET_NUMBER);
   }


   public String toString() {

      return "RestoreBucketCommandTest{" +
              "command=" + command +
              "} " + super.toString();
   }
}