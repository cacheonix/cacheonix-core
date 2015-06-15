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

import org.cacheonix.impl.util.logging.Logger;

/**
 * An event that is sent to subscribers when a member leaves a group.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 */
public final class GroupMemberLeftEvent {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupMemberLeftEvent.class); // NOPMD

   private GroupMember groupMember = null;


   /**
    * Constructor.
    *
    * @param groupMember group member.
    */
   public GroupMemberLeftEvent(final GroupMember groupMember) {

      this.groupMember = groupMember;
   }


   /**
    * Returns group member that joined a group.
    *
    * @return group member that joined a group.
    */
   public GroupMember getGroupMember() {

      return groupMember;
   }


   public String toString() {

      return "GroupMemberLeftEvent{" +
              "groupMember=" + groupMember +
              '}';
   }
}