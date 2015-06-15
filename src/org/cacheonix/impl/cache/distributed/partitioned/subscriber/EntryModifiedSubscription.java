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
package org.cacheonix.impl.cache.distributed.partitioned.subscriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cacheonix.cache.subscriber.EntryModifiedEventContentFlag;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedNotificationMode;
import org.cacheonix.impl.cache.store.BinaryEntryModifiedSubscriber;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.ObjectProcedure;

/**
 * A holder for subscriber information.
 */
@SuppressWarnings("RedundantIfStatement")
public final class EntryModifiedSubscription implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Subscriber's address.
    */
   private ClusterNodeAddress subscriberAddress = null;

   /**
    * An event notification mode.
    */
   private EntryModifiedNotificationMode notificationMode = null;

   /**
    * Event flags.
    */
   private List<EntryModifiedEventContentFlag> eventContentFlags;

   /**
    * Subscriber object's identity as returned by {@link System#identityHashCode(Object)}.
    */
   private int subscriberIdentity = 0;

   private HashSet<EntryModifiedEventType> modificationTypes; // NOPMD


   /**
    * Required by Wireable.
    */
   public EntryModifiedSubscription() {

   }


   /**
    * Creates <code>EntryModifiedSubscription</code>.
    *
    * @param subscriberIdentity subscriber object's identity as returned by {@link System#identityHashCode(Object)}.
    * @param subscriberAddress  subscriber's address.
    * @param notificationMode   event notification mode.
    * @param eventContentFlags  event flags.
    * @param modificationTypes  event operations.
    */
   public EntryModifiedSubscription(final int subscriberIdentity, final ClusterNodeAddress subscriberAddress,
                                    final EntryModifiedNotificationMode notificationMode,
                                    final List<EntryModifiedEventContentFlag> eventContentFlags,
                                    final Set<EntryModifiedEventType> modificationTypes) {

      this.subscriberAddress = subscriberAddress;
      this.notificationMode = notificationMode;
      this.eventContentFlags = new ArrayList<EntryModifiedEventContentFlag>(eventContentFlags);
      this.subscriberIdentity = subscriberIdentity;
      this.modificationTypes = new HashSet<EntryModifiedEventType>(modificationTypes);
   }


   /**
    * Returns subscriber's address.
    *
    * @return the subscriber's address.
    */
   public ClusterNodeAddress getSubscriberAddress() {

      return subscriberAddress;
   }


   /**
    * Returns event notification mode.
    *
    * @return the event notification mode.
    * @see BinaryEntryModifiedSubscriber#getNotificationMode()
    */
   public EntryModifiedNotificationMode getNotificationMode() {

      return notificationMode;
   }


   /**
    * Returns event flags.
    *
    * @return the event flags.
    * @see BinaryEntryModifiedSubscriber#getEventContentFlags()
    */
   public List<EntryModifiedEventContentFlag> getEventContentFlags() {

      return eventContentFlags;
   }


   /**
    * Returns a list of modification types.
    *
    * @return the list of modification types.
    */
   public Set<EntryModifiedEventType> getModificationTypes() {

      return modificationTypes;
   }


   /**
    * Returns subscriber object's identity.
    *
    * @return subscriber object's identity as returned by {@link System#identityHashCode(Object)}.
    */
   public int getSubscriberIdentity() {

      return subscriberIdentity;
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("ForLoopReplaceableByForEach")
   public void writeWire(final DataOutputStream out) throws IOException {

      SerializerUtils.writeAddress(subscriberAddress, out);
      out.writeInt(notificationMode.getCode());

      final int eventFlagsSize = eventContentFlags.size();
      out.writeInt(eventFlagsSize);

      for (int i = 0; i < eventFlagsSize; i++) {
         out.writeInt(eventContentFlags.get(i).getCode());
      }

      final int modificationTypesSize = modificationTypes.size();
      out.writeInt(modificationTypesSize);

      final IOException[] ioException = new IOException[1];
      modificationTypes.forEach(new ObjectProcedure<EntryModifiedEventType>() {

         public boolean execute(final EntryModifiedEventType modifiedEventType) {

            try {

               out.writeInt(modifiedEventType.getCode());
            } catch (final IOException e) {

               ioException[0] = e;
               return false;
            }

            return true;
         }
      });

      if (ioException[0] != null) {

         throw ioException[0];
      }

      out.writeInt(subscriberIdentity);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException {

      subscriberAddress = SerializerUtils.readAddress(in);

      final int notificationModeCode = in.readInt();
      notificationMode = EntryModifiedNotificationMode.toMode(notificationModeCode);

      final int eventFlagsSize = in.readInt();
      eventContentFlags = new ArrayList<EntryModifiedEventContentFlag>(eventFlagsSize);
      for (int i = 0; i < eventFlagsSize; i++) {

         final int eventFlagCode = in.readInt();
         eventContentFlags.add(EntryModifiedEventContentFlag.toFlag(eventFlagCode));
      }

      final int modificationTypesSize = in.readInt();
      modificationTypes = new HashSet<EntryModifiedEventType>(modificationTypesSize);
      for (int i = 0; i < modificationTypesSize; i++) {

         final int modificationTypeCode = in.readInt();
         modificationTypes.add(EntryModifiedEventType.toEntryUpdateType(modificationTypeCode));
      }

      subscriberIdentity = in.readInt();
   }


   /**
    * Returns <code>TYPE_ENTRY_MODIFICATION_SUBSCRIPTION</code>.
    *
    * @return {@link Wireable#TYPE_ENTRY_MODIFICATION_SUBSCRIPTION}.
    */
   public int getWireableType() {

      return TYPE_ENTRY_MODIFICATION_SUBSCRIPTION;
   }


   /**
    * Compares objects' {@link #subscriberIdentity}.
    */
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final EntryModifiedSubscription that = (EntryModifiedSubscription) o;

      if (subscriberIdentity != that.subscriberIdentity) {
         return false;
      }

      return true;
   }


   /**
    * Returns {@link #subscriberIdentity}.
    *
    * @return {@link #subscriberIdentity}.
    */
   public int hashCode() {

      return subscriberIdentity;
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "EntryModifiedSubscription{" +
              "subscriberAddress=" + subscriberAddress +
              ", notificationMode=" + notificationMode +
              ", eventContentFlags=" + eventContentFlags +
              ", subscriberIdentity=" + subscriberIdentity +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new EntryModifiedSubscription();
      }
   }
}
