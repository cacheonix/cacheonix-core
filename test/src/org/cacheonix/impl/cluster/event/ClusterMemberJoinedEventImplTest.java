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
package org.cacheonix.impl.cluster.event;

import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.cluster.ClusterMember;
import junit.framework.TestCase;

/**
 * Tester for ClusterMemberJoinedEventImpl.
 */
public class ClusterMemberJoinedEventImplTest extends TestCase {


   private ClusterMemberImpl clusterMember;

   private ClusterMemberJoinedEventImpl clusterMemberJoinedEvent;


   public void testGetJoinedMembers() throws Exception {

      final Collection<ClusterMember> joinedMembers = clusterMemberJoinedEvent.getJoinedMembers();
      assertEquals(clusterMember, joinedMembers.iterator().next());
   }


   public void testToString() throws Exception {

      assertNotNull(clusterMemberJoinedEvent.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      clusterMember = EventTestUtil.clusterMember("TestClusterName", "1.1.1.1", 7777);
      final ArrayList<ClusterMember> joinedMembers = new ArrayList<ClusterMember>(1);
      joinedMembers.add(clusterMember);

      clusterMemberJoinedEvent = new ClusterMemberJoinedEventImpl(joinedMembers);
   }


   public void tearDown() throws Exception {


      clusterMemberJoinedEvent = null;
      clusterMember = null;

      super.tearDown();
   }


   public String toString() {

      return "ClusterMemberJoinedEventImplTest{" +
              "clusterMember=" + clusterMember +
              ", clusterMemberJoinedEvent=" + clusterMemberJoinedEvent +
              "} " + super.toString();
   }
}
