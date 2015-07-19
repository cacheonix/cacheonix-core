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
import org.cacheonix.impl.net.ClusterNodeAddress;
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
public final class BucketTransferTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketTransferTest.class); // NOPMD

   private static final ClusterNodeAddress OWNER = TestUtils.createTestAddress(0);

   private static final byte STORAGE_NUMBER = 0;

   private BucketTransfer bucketTransfer = null;


   public void testGetOwner() {

      assertEquals(OWNER, bucketTransfer.getOwner());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(bucketTransfer, ser.deserialize(ser.serialize(bucketTransfer)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      bucketTransfer = new BucketTransfer(STORAGE_NUMBER, OWNER);
   }


   public String toString() {

      return "BucketTransferTest{" +
              "bucketTransfer=" + bucketTransfer +
              "} " + super.toString();
   }
}