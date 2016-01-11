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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cache put request is sent by the distributed cache when the key is stored remotely.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferencedInHashCode, NonFinalFieldReferenceInEquals, RedundantIfStatement,
 * UnnecessaryParentheses
 */
public final class PutRequest extends KeyRequest {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PutRequest.class); // NOPMD

   private Binary value = null;

   private Time expirationTime = null;

   private boolean putIfAbsent;


   /**
    * Required by <code>Wireable<code>.
    *
    * @see Wireable
    */
   public PutRequest() {

   }


   public PutRequest(final ClusterNodeAddress sender, final String cacheName, final Binary key, final Binary value,
                     final Time expirationTime, final boolean putIfAbsent) {

      super(TYPE_CACHE_PUT_REQUEST, cacheName, false, false);
      this.expirationTime = expirationTime;
      this.putIfAbsent = putIfAbsent;
      this.value = value;
      this.setSender(sender);
      this.setKey(key);
   }


   private PutRequest(final String cacheName, final Binary key, final Binary value, final Time expirationTime,
                      final boolean putIfAbsent) {

      super(TYPE_CACHE_PUT_REQUEST, cacheName, false, false);
      this.expirationTime = expirationTime;
      this.putIfAbsent = putIfAbsent;
      this.value = value;
      this.setKey(key);
   }


   /**
    * Returns value.
    *
    * @return value to set.
    */
   public Binary getValue() {

      return value;
   }


   /**
    * Sets value.
    *
    * @param value to set.
    */
   public void setValue(final Binary value) {

      this.value = value;
   }


   public Time getExpirationTime() {

      return expirationTime;
   }


   boolean isPutIfAbsent() {

      return putIfAbsent;
   }


   protected ProcessingResult processKey(final Bucket bucket, final Binary key) {

      try {
         // ... Check if there is an unexpired lease

         final Binary previousValue;
         final Binary modifiedKey;

         if (putIfAbsent) {

            // NOTE: simeshev@cacheonix.org - 2012-07-01 - If putAbsent is set and the previous value
            // is not null, this means that the update *was not* performed. This in turn means that
            // modifiedKey *should not* be set so that it doesn't cause further updates in replicas

            if (bucket.containsKey(key)) {

               // Bucket contains the key

               final ReadableElement currentValue = bucket.get(key);
               previousValue = currentValue == null ? null : currentValue.getValue();

               // Set to null becuase there was no modification made
               modifiedKey = null;
            } else {

               // Bucket didn't contain the key, do put

               previousValue = bucket.put(key, value, expirationTime);

               // Set to key as we made a put
               modifiedKey = key;
            }
         } else {

            // Update
            previousValue = bucket.put(key, value, expirationTime);

            // Set up modified key
            modifiedKey = value == null && previousValue == null ? null : key;
         }

         // Calculate expiration time
         // DELETEME: simeshev@cacheonix.org - 2011-05-18 - Temporarily disabled caching.
         // REVIEWME: simeshev@cacheonix.org - 2011-05-18 -> Implement expiration time as a minimum of cacheUntil and expiration time.
//      final Time resultExpirationTime = isWillCache() && false ? renewLease(bucket, expirationTime) : null;

         // REVIEWME: simeshev@cacheonix.org - 2011-05-18 -> Implement CacheableResult visitor pattern so that it handles
         // passing previousValue as a result and the current value as a new result

         // We need to send the previous value back of only for primary updates
         if (isPrimaryRequest()) {

            // Create result
            final CacheableValue result = new CacheableValue(previousValue, null);

            return new ProcessingResult(result, modifiedKey);
         } else {

            // Replica update request
            return new ProcessingResult(null, null);
         }
      } catch (final InvalidObjectException e) {

         throw new CacheonixException(e);
      } catch (final StorageException e) {

         throw new CacheonixException(e);
      }
   }


   /**
    * {@inheritDoc}
    */
   public KeyRequest createRequest() {

      return new PutRequest(getCacheName(), getKey(), value, expirationTime, putIfAbsent);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      value = SerializerUtils.readBinary(in);
      expirationTime = SerializerUtils.readTime(in);
      putIfAbsent = in.readBoolean();
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeBinary(out, value);
      SerializerUtils.writeTime(expirationTime, out);
      out.writeBoolean(putIfAbsent);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final PutRequest that = (PutRequest) o;

      if (putIfAbsent != that.putIfAbsent) {
         return false;
      }
      if (expirationTime != null ? !expirationTime.equals(that.expirationTime) : that.expirationTime != null) {
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
      result = 31 * result + (expirationTime != null ? expirationTime.hashCode() : 0);
      result = 31 * result + (putIfAbsent ? 1 : 0);
      return result;
   }


   public String toString() {

      return "PutRequest{" +
              "value=" + value +
              ", expirationTime=" + expirationTime +
              ", putIfAbsent=" + putIfAbsent +
              "} " + super.toString();
   }


   /**
    * A class factory.
    */
   private final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new PutRequest();
      }
   }
}
