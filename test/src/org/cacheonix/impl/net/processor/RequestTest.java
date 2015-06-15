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
package org.cacheonix.impl.net.processor;

import org.cacheonix.impl.net.cluster.ClusterProcessorKey;
import org.cacheonix.impl.net.serializer.Wireable;
import junit.framework.TestCase;

/**
 * Message Tester.
 *
 * @author simeshev@cacheonix.com
 * @version 1.0
 * @since <pre>04/04/2008</pre>
 */
public final class RequestTest extends TestCase {

   private Message message;

   private static final int TYPE = Wireable.TYPE_CLUSTER_MULTICAST_MARKER;


   public void testGetType() throws Exception {

      assertEquals(TYPE, message.getWireableType());
   }


   public void testRequiresNoArgConstructorPresent() {

      final Message message = new Message() {

         protected ProcessorKey getProcessorKey() {

            return new SimpleProcessorKey(DESTINATION_NONE);
         }


         public void execute() {

         }
      };
      assertEquals(Wireable.TYPE_UNDEFINED, message.getWireableType());
   }


   /**
    * @noinspection ConstantConditions
    */
   public void testImplementsWireable() {

      assertTrue(Wireable.class.isAssignableFrom(Message.class));
   }


   public void testToString() {

      assertNotNull(message.toString());
   }


   public void testHashCode() {

      assertTrue(message.hashCode() != 0);
   }


   protected void setUp() throws Exception {

      super.setUp();

      message = new Message(TYPE) {

         protected ProcessorKey getProcessorKey() {

            return ClusterProcessorKey.getInstance();
         }


         public void execute() {

         }
      };
   }


   public String toString() {

      return "RequestTest{" +
              "message=" + message +
              "} " + super.toString();
   }
}
