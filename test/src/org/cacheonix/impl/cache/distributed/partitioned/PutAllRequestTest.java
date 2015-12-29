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

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryFactory;
import org.cacheonix.impl.cache.item.BinaryFactoryBuilder;
import org.cacheonix.impl.cache.item.BinaryType;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * PutAllRequestTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Dec 20, 2009 7:18:53 PM
 */
public final class PutAllRequestTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PutAllRequestTest.class); // NOPMD

   private static final BinaryFactoryBuilder BINARY_FACTORY_BUILDER = new BinaryFactoryBuilder();

   private static final TimeImpl EXPIRATION_TIME_MILLIS = new TimeImpl(1000L, 0);

   private static final String TEST_CACHE = "test.cache";

   private PutAllRequest request;


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new PutAllRequest().toString());
   }


   public void testGetExpirationTimeMillis() throws Exception {

      assertEquals(EXPIRATION_TIME_MILLIS, request.getExpirationTime());
   }


   public void testSerializeDeserialize() throws IOException, InvalidObjectException {

      final BinaryFactory binaryFactory = BINARY_FACTORY_BUILDER.createFactory(BinaryType.BY_COPY);
      final HashMap<Binary, Binary> map = new HashMap<Binary, Binary>(1);
      map.put(binaryFactory.createBinary("key"), binaryFactory.createBinary("value"));
      request.setEntrySet(map);
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      request = new PutAllRequest(TEST_CACHE);
      request.setSender(TestUtils.createTestAddress(0));
      request.setReceiver(TestUtils.createTestAddress(2));
      request.setExpirationTime(EXPIRATION_TIME_MILLIS);
   }


   public String toString() {

      return "PutAllRequestTest{" +
              "request=" + request +
              "} " + super.toString();
   }
}
