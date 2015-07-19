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
package org.cacheonix.impl.cluster.node.state.group;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.util.logging.Logger;

/**
 * GroupEventSubscriberListTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jan 2, 2010 10:18:14 PM
 */
public final class GroupEventSubscriberListTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupEventSubscriberListTest.class); // NOPMD

   private GroupEventSubscriberList list;


   public void testSize() throws Exception {

      list.add(new GroupEventSubscriber() {

         public void notifyGroupMemberJoined(final GroupMemberJoinedEvent event) {

         }


         public void notifyGroupMemberLeft(final GroupMemberLeftEvent event) {

         }


         public void notifyGroupMemberFailedToJoin(final GroupMemberFailedToJoinEvent event) {

         }
      });

      assertEquals(1, list.size());
   }


   protected void setUp() throws Exception {

      super.setUp();
      list = new GroupEventSubscriberList();
   }


   public String toString() {

      return "GroupEventSubscriberListTest{" +
              "list=" + list +
              "} " + super.toString();
   }
}
