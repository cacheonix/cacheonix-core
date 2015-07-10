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
package org.cacheonix.impl.cluster.node.state.group;

import java.io.IOException;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * JoinCacheGroupRequestTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 8, 2009 5:52:48 PM
 */
public final class JoinGroupMessageTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JoinGroupMessageTest.class); // NOPMD

   private JoinGroupMessage message;

   private static final String CACHE_NAME = "cache.name";

   private static final int MAX_SIZE = 2000;


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void testDefaultConstructor() {

      assertNotNull(new JoinGroupMessage().toString());
   }


   public void testHashCode() {

      assertTrue(message.hashCode() != 0);
   }


   public void testSerialize() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(message, serializer.deserialize(serializer.serialize(message)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      message = new JoinGroupMessage(TestUtils.createTestAddress(), CACHE_NAME, true, 1000000L, 1000000000L, MAX_SIZE);
      message.setCacheConfigName(CACHE_NAME);
   }


   public String toString() {

      return "JoinGroupMessageTest{" +
              "request=" + message +
              "} " + super.toString();
   }
}
