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

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketOwnerTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Nov 3, 2009 12:29:44 AM
 */
public final class BucketOwnerTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketOwnerTest.class); // NOPMD

   private BucketOwner bucketOwner = null;

   private static final int STORAGE_NUMBER = 0;

   private static final byte REPLICA_COUNT = (byte) 1;


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      bucketOwner.markLeaving();

      bucketOwner.addOwnedBucketNumber(Integer.valueOf(4));
      bucketOwner.addOwnedBucketNumber(Integer.valueOf(5));

      final int bucketNumber = 6;
      bucketOwner.registerInboundTransfer(bucketNumber, new BucketTransfer((byte) STORAGE_NUMBER, TestUtils.createTestAddress(1)));

      final int outboundNumber = 4;
      bucketOwner.getOutboundBuckets().put(outboundNumber, new BucketTransfer((byte) STORAGE_NUMBER, TestUtils.createTestAddress(2)));

      // NOPMD
      bucketOwner.getOrCreateOutboundReplicas((byte) 1).put(Integer.valueOf(5), new BucketTransfer((byte) 1, TestUtils.createTestAddress(2)));
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(bucketOwner, ser.deserialize(ser.serialize(bucketOwner)));
      assertTrue(bucketOwner.isLeaving());
   }


   protected void setUp() throws Exception {

      super.setUp();
      bucketOwner = new BucketOwner(REPLICA_COUNT, TestUtils.createTestAddress(STORAGE_NUMBER));
   }


   public String toString() {

      return "BucketOwnerTest{" +
              "bucketOwner=" + bucketOwner +
              "} " + super.toString();
   }
}
