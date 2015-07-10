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

/**
 * An interface that objects interested in subscribing to group events should implement.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface GroupEventSubscriber {

   /**
    * Notifies implementor that a member joined a group. If this method is used for on-boarding a group member, it
    * should be performed synronously.
    * <p/>
    * <b>Important:</b> This method should never block and perform as fast is possible because it is called
    * synchronously.
    *
    * @param event event containing details.
    */
   void notifyGroupMemberJoined(GroupMemberJoinedEvent event);

   /**
    * Notifies implementor that a member left a group.
    * <p/>
    * <b>Important:</b> This method should never block and perform as fast is possible because it is called
    * synchronously.
    *
    * @param event event containing details.
    */
   void notifyGroupMemberLeft(GroupMemberLeftEvent event);

   /**
    * Notifies implementor that a member failved to jong a group.
    * <p/>
    * <b>Important:</b> This method should never block and perform as fast is possible because it is called
    * synchronously.
    *
    * @param event event containing details.
    */
   void notifyGroupMemberFailedToJoin(GroupMemberFailedToJoinEvent event);
}
