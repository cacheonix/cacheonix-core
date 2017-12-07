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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Updates a key with a new value obtained through prefetching.
 */
@SuppressWarnings("RedundantIfStatement")
public final class UpdateKeyRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   private Binary value = null;

   private Time timeToRead = null;

   private long expectedElementUpdateCounter = 0L;


   /**
    * Required to support <code>Wireable</code>.
    */
   private UpdateKeyRequest() {

   }


   /**
    * Creates an UpdateKeyRequest.
    *
    * @param cacheName                    cache name
    * @param key                          the key.
    * @param value                        the new value.
    * @param timeToRead                   the time it took to read the element.
    * @param expectedElementUpdateCounter the update counter the element must have in order to be updated.
    */
   public UpdateKeyRequest(final String cacheName, final Binary key, final Binary value, final Time timeToRead,
                           final long expectedElementUpdateCounter) {

      super(TYPE_CACHE_UPDATE_KEY_REQUEST, cacheName, false, false);

      this.expectedElementUpdateCounter = expectedElementUpdateCounter;
      this.timeToRead = timeToRead;
      this.value = value;
      this.setKey(key);
   }


   Binary getValue() {

      return value;
   }


   Time getTimeToRead() {

      return timeToRead;
   }


   long getExpectedElementUpdateCounter() {

      return expectedElementUpdateCounter;
   }


   /**
    * Processes a key in a bucket. It is guaranteed that this bucket is an owner of the key and that it is locked.
    *
    * @param bucket the bucket that owns the key.
    * @param key    the key to process.
    * @return the result of processing the key.
    */
   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      final ReadableElement previousElement = bucket.update(key, value, timeToRead, expectedElementUpdateCounter);
      if (previousElement == null) {

         return new ProcessingResult(null, null);
      } else {

         return new ProcessingResult(null, key);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new UpdateKeyRequest(getCacheName(), getKey(), value, timeToRead, expectedElementUpdateCounter);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);

      value = SerializerUtils.readBinary(in);
      timeToRead = SerializerUtils.readTime(in);
      expectedElementUpdateCounter = in.readLong();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);

      SerializerUtils.writeBinary(out, value);
      SerializerUtils.writeTime(timeToRead, out);
      out.writeLong(expectedElementUpdateCounter);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final UpdateKeyRequest that = (UpdateKeyRequest) o;

      if (expectedElementUpdateCounter != that.expectedElementUpdateCounter) {
         return false;
      }
      if (timeToRead != null ? !timeToRead.equals(that.timeToRead) : that.timeToRead != null) {
         return false;
      }
      if (value != null ? !value.equals(that.value) : that.value != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      result = 31 * result + (timeToRead != null ? timeToRead.hashCode() : 0);
      result = 31 * result + (int) (expectedElementUpdateCounter ^ expectedElementUpdateCounter >>> 32);
      return result;
   }


   public String toString() {

      return "UpdateKeyRequest{" +
              "value=" + value +
              ", timeToRead=" + timeToRead +
              ", expectedElementUpdateCounter=" + expectedElementUpdateCounter +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new UpdateKeyRequest(); // NOPMD
      }
   }
}
