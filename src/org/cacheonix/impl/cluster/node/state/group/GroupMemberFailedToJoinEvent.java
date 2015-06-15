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
 * An event that is sent to subsctibers when a member joins a group.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Jan 21, 2009 2:03:27 AM
 */
public final class GroupMemberFailedToJoinEvent {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupMemberFailedToJoinEvent.class); // NOPMD

   private final JoinGroupMessage joinMessage;

   private final String error;


   /**
    * Constructor.
    *
    * @param error
    * @param joinMessage
    */
   public GroupMemberFailedToJoinEvent(final String error, final JoinGroupMessage joinMessage) {

      this.error = error;
      this.joinMessage = joinMessage;
   }


   public String getError() {

      return error;
   }


   public JoinGroupMessage getJoinMessage() {

      return joinMessage;
   }


   public String toString() {

      return "GroupMemberFailedToJoinEvent{" +
              "error='" + error + '\'' +
              ", joinMessage=" + joinMessage +
              '}';
   }
}