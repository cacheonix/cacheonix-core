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

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

/**
 *
 */
public final class UpdateKeyRequestTest extends CacheonixTestCase {


   private static final int EXPECTED_ELEMENT_UPDATE_COUNTER = 777;

   private static final String TEST_CACHE_NAME = "test.cache.name";

   private static final Binary VALUE = toBinary("test.value");

   private static final Binary KEY = toBinary("test.key");

   private static final TimeImpl TIME_TO_READ = new TimeImpl(10, 0);


   private UpdateKeyRequest request;


   public void testCreate() {

      assertEquals(EXPECTED_ELEMENT_UPDATE_COUNTER, request.getExpectedElementUpdateCounter());
      assertEquals(TIME_TO_READ, request.getTimeToRead());
      assertEquals(VALUE, request.getValue());
   }


   public void testCreateRequest() {

      final KeyRequest copy = request.createRequest();
      assertTrue(copy instanceof UpdateKeyRequest);
      final UpdateKeyRequest updateKeyRequestCopy = (UpdateKeyRequest) copy;
      assertEquals(request.getExpectedElementUpdateCounter(), updateKeyRequestCopy.getExpectedElementUpdateCounter());
      assertEquals(request.getTimeToRead(), updateKeyRequestCopy.getTimeToRead());
      assertEquals(request.getValue(), updateKeyRequestCopy.getValue());
      assertEquals(request.getKey(), updateKeyRequestCopy.getKey());
   }


   public void testSerializeDeserialize() throws IOException {

      request.setSender(TestUtils.createTestAddress());
      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(request, ser.deserialize(ser.serialize(request)));
   }


   public void setUp() throws Exception {

      super.setUp();

      request = new UpdateKeyRequest(TEST_CACHE_NAME, KEY, VALUE, TIME_TO_READ, EXPECTED_ELEMENT_UPDATE_COUNTER);
   }


   public void tearDown() throws Exception {

      super.tearDown();

      request = null;
   }
}
