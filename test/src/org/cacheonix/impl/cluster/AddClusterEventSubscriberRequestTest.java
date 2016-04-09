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
package org.cacheonix.impl.cluster;

import java.io.IOException;
import java.io.NotSerializableException;

import junit.framework.TestCase;
import org.cacheonix.cluster.ClusterEventSubscriber;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableFactory;

import static org.mockito.Mockito.mock;

/**
 * Tester for {@link AddClusterEventSubscriberRequest}.
 */
public final class AddClusterEventSubscriberRequestTest extends TestCase {


   private AddClusterEventSubscriberRequest request;


   public void testWriteReadWire() {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      try {
         ser.serialize(request);
      } catch (final IOException e) {
         assertTrue(e instanceof NotSerializableException); // NOPMD
      }
   }


   public void testWireableType() {

      assertEquals(Wireable.TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER, request.getWireableType());
   }


   public void testCreateWireable() {

      final WireableFactory wireableFactory = WireableFactory.getInstance();
      final Wireable wireable = wireableFactory.createWireable(Wireable.TYPE_ADD_USER_CLUSTER_EVENT_SUBSCRIBER);
      assertTrue(wireable instanceof AddClusterEventSubscriberRequest);
   }


   public void testToString() throws Exception {

      assertNotNull(request.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      final ClusterEventSubscriber subscriber = mock(ClusterEventSubscriber.class);
      request = new AddClusterEventSubscriberRequest(subscriber);
   }


   public void tearDown() throws Exception {

      request = null;
      super.tearDown();
   }


   public String toString() {

      return "AddClusterEventSubscriberRequestTest{" +
              "request=" + request +
              "} " + super.toString();
   }
}
