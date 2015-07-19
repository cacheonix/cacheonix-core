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
package org.cacheonix.impl.net.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.NotSerializableException;

import org.cacheonix.impl.cache.distributed.partitioned.ShutdownCacheProcessorMessage;
import junit.framework.TestCase;

/**
 * Tester for ShutdownCacheProcessorMessage.
 */
public final class ShutdownCacheProcessorMessageTest extends TestCase {

   private static final String TEST_CACHE_NAME = "test.cache.name";

   private ShutdownCacheProcessorMessage message;


   public void testReadWire() throws Exception {

      boolean thrown = false;
      try {
         message.writeWire(new DataOutputStream(new ByteArrayOutputStream()));
      } catch (final NotSerializableException e) {
         thrown = true;
      }

      assertTrue(thrown);
   }


   public void testWriteWire() throws Exception {

      boolean thrown = false;
      try {
         message.readWire(new DataInputStream(new ByteArrayInputStream(new byte[]{0})));
      } catch (final NotSerializableException e) {
         thrown = true;
      }

      assertTrue(thrown);
   }


   public void testToString() throws Exception {

      assertNotNull(message.toString());
   }


   public void testGetCacheName() {

      assertEquals(TEST_CACHE_NAME, message.getCacheName());
   }


   protected void setUp() throws Exception {

      super.setUp();
      message = new ShutdownCacheProcessorMessage(TEST_CACHE_NAME, true);
   }
}
