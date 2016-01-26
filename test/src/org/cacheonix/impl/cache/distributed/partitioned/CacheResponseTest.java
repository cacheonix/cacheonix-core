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
import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.CompressedBinary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.item.PassByCopyBinary;
import org.cacheonix.impl.cache.item.PassObjectByReferenceBinary;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.cache.EntryImpl;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheGetResponse Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>05/03/2008</pre>
 */
public final class CacheResponseTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheResponseTest.class); // NOPMD

   private Binary value = null;

   private CacheResponse response = null;

   private static final String TEST_CACHE = "test.cache";

   private Serializer serializer;


   public void testSetGetValue() throws Exception {

      response.setResult(value);
      assertEquals(value, response.getResult());
   }


   public void testToString() {

      assertNotNull(response.toString());
   }


   public void testHashCode() {

      response.setResult(value);
      assertTrue(response.hashCode() != 0);
   }


   public void testGetCacheName() {

      assertEquals(TEST_CACHE, response.getCacheName());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      response.setResult(value);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeBinaryArrayList() throws IOException, ClassNotFoundException, InvalidObjectException {

      final ArrayList<Binary> result = new ArrayList<Binary>(1);
      result.add(new PassByCopyBinary("value1"));
      result.add(new PassByCopyBinary("value3"));
      response.setResult(result);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeBinaryEntry() throws IOException, ClassNotFoundException, InvalidObjectException {

      final EntryImpl entry = new EntryImpl(new PassByCopyBinary("key"), new PassByCopyBinary("value"));
      response.setResult(entry);
      assertSerializedEquals(response);
   }


   public void testSerializeDeserializeBinarySetAsValue() throws IOException, ClassNotFoundException, InvalidObjectException {

      setBinary(new PassObjectByReferenceBinary("value"));
      setBinary(new PassByCopyBinary("value"));
      setBinary(new PassByCopyBinary("value"));
      setBinary(new CompressedBinary("value"));
      assertSerializedEquals(response);
   }


   private void assertSerializedEquals(final Message response) throws IOException {

      assertEquals(response, serializer.deserialize(serializer.serialize(response)));
   }


   public void testSerializationPerformance() throws IOException {

      final long start = System.currentTimeMillis();
      for (int i = 0; i < 100000; i++) {
         serializer.serialize(response);
      }
      final long duration = System.currentTimeMillis() - start;

      //noinspection ControlFlowStatementWithoutBraces
      if (LOG.isDebugEnabled()) LOG.debug("duration: " + duration); // NOPMD
   }


   private void setBinary(final Binary binary) {

      final Set<Binary> binarySet = new HashSet<Binary>(11);
      binarySet.add(binary);
      response.setResult(binarySet);
   }


   protected void setUp() throws Exception {

      super.setUp();
      serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      value = TestUtils.toBinary("value");
      response = new CacheResponse(TEST_CACHE);
      response.setResult(value);
   }


   public String toString() {

      return "PartitionCacheResponseTest{" +
              "response=" + response +
              '}';
   }
}
