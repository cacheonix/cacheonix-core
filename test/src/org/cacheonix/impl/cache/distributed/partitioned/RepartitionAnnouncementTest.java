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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.IOException;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * RepartitionAnnouncementTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Nov 9, 2009 8:06:51 PM
 */
public final class RepartitionAnnouncementTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RepartitionAnnouncementTest.class); // NOPMD

   private static final ClusterNodeAddress ADDR_1 = TestUtils.createTestAddress(1);

   private static final String TEST_CACHE = "test.cache";

   private RepartitionAnnouncement announcement;


   public void testSerialze() throws IOException, ClassNotFoundException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(announcement, serializer.deserialize(serializer.serialize(announcement)));
   }


   protected void setUp() throws Exception {

      super.setUp();
      announcement = new RepartitionAnnouncement(TEST_CACHE);
      announcement.setSender(ADDR_1);
   }
}
