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

import junit.framework.TestCase;

/**
 * Tester for OrphanBucketMessage.
 */
public final class OrphanBucketMessageTest extends TestCase {

   private static final String CACHE_NAME = "test.cache";

   private static final byte STORAGE_NUMBER = (byte) 2;

   private static final Integer BUCKET_NUMBER = 1000;

   private OrphanBucketMessage message;


   public void testGetStorageNumber() throws Exception {

      assertEquals(STORAGE_NUMBER, message.getStorageNumber());
   }


   public void testGetBucketNumber() throws Exception {

      assertEquals(BUCKET_NUMBER, message.getBucketNumber());
   }


   public void testToString() throws Exception {

      assertNotNull(message.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      message = new OrphanBucketMessage(CACHE_NAME, STORAGE_NUMBER, BUCKET_NUMBER);
   }


   public void tearDown() throws Exception {

      message = null;

      super.tearDown();
   }
}
