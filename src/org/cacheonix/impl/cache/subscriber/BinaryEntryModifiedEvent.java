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
package org.cacheonix.impl.cache.subscriber;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;

import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedEventType;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Implementation of the {@link EntryModifiedEvent}.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see EntryModifiedSubscriber
 * @since Aug 13, 2008 4:20:21 PM
 */
public final class BinaryEntryModifiedEvent implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BinaryEntryModifiedEvent.class); // NOPMD

   /**
    * Key of the updated element or null if the key was not requested.
    */
   private Binary updatedKey = null;

   /**
    * Value associated with the key of the updated element or null if the value was null or if the value was not
    * requested.
    */
   private Binary newValue = null;

   /**
    * Previous value associated with the key of the updated element or null if the value was null, this is a
    * notification about an added element or if the previous value was not requested.
    */
   private Binary previousValue = null;

   /**
    * Type of the update operation.
    */
   private EntryModifiedEventType updateType = null;

   /**
    * Time of the last update of the element according to the element's local clock.
    */
   private Time lastUpdateTime;

   /**
    * Version of the element. The version is equivalent of an update counter.
    */
   private long version;


   /**
    * Updater of the element or null if the updater is not available.
    */
   private CacheMember updater = null;


   /**
    * Creates BinaryEntryModifiedEvent. Required by {@link Externalizable}.
    */
   @SuppressWarnings("WeakerAccess")
   public BinaryEntryModifiedEvent() {

   }


   /**
    * Constructor.
    *
    * @param updateType     update type
    * @param updatedKey     updated key
    * @param newValue       updated value
    * @param previousValue  previous value
    * @param lastUpdateTime lat time the entry was updated, milliseconds
    * @param version        entry version
    * @param updater        cache member that performed an update.
    */
   public BinaryEntryModifiedEvent(final EntryModifiedEventType updateType, final Binary updatedKey,
                                   final Binary newValue, final Binary previousValue, final Time lastUpdateTime,
                                   final long version, final CacheMember updater) {

      this.updatedKey = updatedKey;
      this.newValue = newValue;
      this.previousValue = previousValue;
      this.updateType = updateType;
      this.lastUpdateTime = lastUpdateTime;
      this.version = version;
      this.updater = updater;
   }


   /**
    * {@inheritDoc}
    */
   public EntryModifiedEventType getUpdateType() {

      return updateType;
   }


   /**
    * {@inheritDoc}
    */
   public Binary getUpdatedKey() {

      return updatedKey;
   }


   /**
    * {@inheritDoc}
    */
   public Binary getNewValue() {

      return newValue;
   }


   /**
    * {@inheritDoc}
    */
   public Binary getPreviousValue() {

      return previousValue;
   }


   /**
    * {@inheritDoc}
    */
   public Time getLastUpdateTime() {

      return lastUpdateTime;
   }


   /**
    * {@inheritDoc}
    */
   public long getVersion() {

      return version;
   }


   /**
    * {@inheritDoc}
    */
   public CacheMember getUpdater() {

      return updater;
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      updateType = EntryModifiedEventType.toEntryUpdateType(in.readByte());
      updatedKey = SerializerUtils.readBinary(in);
      newValue = SerializerUtils.readBinary(in);
      previousValue = SerializerUtils.readBinary(in);
      lastUpdateTime = SerializerUtils.readTime(in);
      version = in.readLong();
   }


   public int getWireableType() {

      return TYPE_BINARY_ENTRY_MODIFIED_EVENT;
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("NonSerializableObjectPassedToObjectStream")
   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeByte(updateType.getCode());
      SerializerUtils.writeBinary(out, updatedKey);
      SerializerUtils.writeBinary(out, newValue);
      SerializerUtils.writeBinary(out, previousValue);
      SerializerUtils.writeTime(lastUpdateTime, out);
      out.writeLong(version);
   }


   @SuppressWarnings("RedundantIfStatement")
   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final BinaryEntryModifiedEvent that = (BinaryEntryModifiedEvent) o;

      if (version != that.version) {
         return false;
      }
      if (lastUpdateTime != null ? !lastUpdateTime.equals(that.lastUpdateTime) : that.lastUpdateTime != null) {
         return false;
      }
      if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) {
         return false;
      }
      if (previousValue != null ? !previousValue.equals(that.previousValue) : that.previousValue != null) {
         return false;
      }
      if (updateType != null ? !updateType.equals(that.updateType) : that.updateType != null) {
         return false;
      }
      if (updatedKey != null ? !updatedKey.equals(that.updatedKey) : that.updatedKey != null) {
         return false;
      }
      if (updater != null ? !updater.equals(that.updater) : that.updater != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = updatedKey != null ? updatedKey.hashCode() : 0;
      result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
      result = 31 * result + (previousValue != null ? previousValue.hashCode() : 0);
      result = 31 * result + (updateType != null ? updateType.hashCode() : 0);
      result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
      result = 31 * result + (int) (version ^ version >>> 32);
      result = 31 * result + (updater != null ? updater.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "BinaryEntryModifiedEvent{" +
              "updateType=" + updateType +
              ", updatedKey=" + updatedKey +
              ", newValue=" + newValue +
              ", previousValue=" + previousValue +
              ", lastUpdateTimeMillis=" + lastUpdateTime +
              ", version=" + version +
              ", updater=" + updater +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new BinaryEntryModifiedEvent();
      }
   }
}
