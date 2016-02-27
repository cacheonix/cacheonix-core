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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.Wireable;
import junit.framework.TestCase;

/**
 * RecoveryMarker Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/26/2008</pre>
 */
public final class RecoveryMarkerTest extends TestCase {

   private RecoveryMarker recoveryMarker = null;

   private List currentList;

   private ClusterNodeAddress originator;

   private List previousList;


   public void testSetGetOriginator() throws Exception {

      assertEquals(originator, recoveryMarker.getOriginator());
      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      recoveryMarker.setOriginator(clusterNodeAddress);
      assertEquals(clusterNodeAddress, recoveryMarker.getOriginator());
   }


   public void testGetCurrentList() throws Exception {

      assertEquals(currentList, recoveryMarker.getCurrentList());
   }


   public void testGetPreviousList() throws Exception {

      assertEquals(previousList, recoveryMarker.getPreviousList());
   }


   public void testToString() {

      assertNotNull(recoveryMarker.toString());
   }


   public void testSerializeDeserialize() throws IOException {

      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(recoveryMarker);
      assertEquals(recoveryMarker, serializer.deserialize(bytes));
   }


   public void testHashCode() {

      assertTrue(recoveryMarker.hashCode() != 0);
   }


   public void testDefaultConstructor() {

      assertEquals(Wireable.TYPE_CLUSTER_RECOVERY_MARKER, new RecoveryMarker().getWireableType());
   }


   protected void setUp() throws Exception {

      super.setUp();
      originator = TestUtils.createTestAddress();
      currentList = new ArrayList(1);
      previousList = new ArrayList(1);
      recoveryMarker = new RecoveryMarker(UUID.randomUUID(), originator);
   }


   public String toString() {

      return "RecoveryMarkerTest{" +
              "currentList=" + currentList +
              ", originator=" + originator +
              ", previousList=" + previousList +
              ", recoveryMarker=" + recoveryMarker +
              "} " + super.toString();
   }
}
