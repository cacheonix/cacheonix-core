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
package org.cacheonix.impl.cluster.node.state.group;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A list of subscribers to group events.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see GroupEventSubscriber
 * @see GroupMemberJoinedEvent
 * @see GroupMemberLeftEvent
 * @since Jan 21, 2009 1:58:28 AM
 */
public final class GroupEventSubscriberList {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupEventSubscriberList.class); // NOPMD

   private final Collection<GroupEventSubscriber> subscribers = new CopyOnWriteArrayList<GroupEventSubscriber>();


   public void add(final GroupEventSubscriber subscriber) {

      subscribers.add(subscriber);
   }


   public void notifyMemberJoined(final GroupMemberJoinedEvent event) {

      for (final GroupEventSubscriber subscriber : subscribers) {
         subscriber.notifyGroupMemberJoined(event);
      }
   }


   public void notifyMemberLeft(final GroupMemberLeftEvent event) {

      for (final GroupEventSubscriber subscriber : subscribers) {
         subscriber.notifyGroupMemberLeft(event);
      }
   }


   public void notifyMemberFailedToJoin(final GroupMemberFailedToJoinEvent event) {

      for (final GroupEventSubscriber subscriber : subscribers) {
         subscriber.notifyGroupMemberFailedToJoin(event);
      }
   }


   /**
    * Returns number of subscribers.
    *
    * @return number of subscribers.
    */
   public int size() {

      return subscribers.size();
   }


   public String toString() {

      return "GroupEventSubscriberList{" +
              "subscribers.size()" + subscribers.size() +
              '}';
   }
}
