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
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import junit.framework.TestCase;

/**
 * RevivalMarker Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>04/26/2008</pre>
 */
public final class RevivalMarkerTest extends TestCase {

   private RevivalMarker marker = null;


   public void testSetGetOriginator() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      marker.setOriginator(clusterNodeAddress);
      assertEquals(clusterNodeAddress, marker.getOriginator());
   }


   public void testGetNewList() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      final List list = new ArrayList(1);
      list.add(clusterNodeAddress);
      marker.setNewList(list);
      assertEquals(list, marker.getNewList());
   }


   public void testGetVisitedList() throws Exception {

      final ClusterNodeAddress clusterNodeAddress = TestUtils.createTestAddress();
      final List list = new ArrayList(1);
      list.add(clusterNodeAddress);
      marker.setVisitedList(list);
      assertEquals(list, marker.getVisitedList());
   }


   public void testToString() {

      assertNotNull(marker.toString());
   }


   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {

      marker.setOriginator(TestUtils.createTestAddress());
      marker.setNewList(new ArrayList(1));
      marker.setVisitedList(new ArrayList(1));
      final Serializer serializer = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      final byte[] bytes = serializer.serialize(marker);
      assertEquals(marker, serializer.deserialize(bytes));
   }


   protected void setUp() throws Exception {

      super.setUp();
      marker = new RevivalMarker();
   }


   public String toString() {

      return "RevivalMarkerTest{" +
              "marker=" + marker +
              "} " + super.toString();
   }
}
