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
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

import static org.cacheonix.impl.net.serializer.SerializerUtils.readBinary;
import static org.cacheonix.impl.net.serializer.SerializerUtils.readTime;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeBinary;
import static org.cacheonix.impl.net.serializer.SerializerUtils.writeTime;

/**
 * A value that can be cached, returned by read requests such as GetRequest.
 */
@SuppressWarnings("RedundantIfStatement")
public final class CacheableValue implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * A value.
    */
   private Binary binaryValue = null;

   /**
    * Expiration time. The value cannot be cache if the expiration time is null.
    */
   private Time timeToLeave = null;

   /**
    * Time the element was created.
    */
   private Time createdTime = null;

   /**
    * Time to expire.
    */
   private Time expirationTime = null;


   public CacheableValue() {

   }


   /**
    * Constructor.
    *
    * @param binaryValue a value.
    * @param timeToLeave expiration time. Set to null if the value cannot be cached.
    */
   public CacheableValue(final Binary binaryValue, final Time timeToLeave, final Time createdTime,
           final Time expirationTime) {

      this.expirationTime = expirationTime;
      this.binaryValue = binaryValue;
      this.timeToLeave = timeToLeave;
      this.createdTime = createdTime;
   }


   /**
    * Returns the value.
    *
    * @return the value.
    */
   public Binary getBinaryValue() {

      return binaryValue;
   }


   /**
    * Returns the expiration time.
    *
    * @return the expiration time. Returns null if the value must not be cached.
    */
   public Time getTimeToLeave() {

      return timeToLeave;
   }


   /**
    * {@inheritDoc}
    */
   public void writeWire(final DataOutputStream out) throws IOException {

      writeBinary(out, binaryValue);
      writeTime(expirationTime, out);
      writeTime(timeToLeave, out);
      writeTime(createdTime, out);
   }


   /**
    * {@inheritDoc}
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      binaryValue = readBinary(in);
      expirationTime = readTime(in);
      timeToLeave = readTime(in);
      createdTime = readTime(in);
   }


   /**
    * Returns time this element expires.
    *
    * @return time this element expires.
    */
   public Time getExpirationTime() {

      return expirationTime;
   }


   /**
    * Returns the time this element was created.
    *
    * @return the time this element was created.
    */
   public Time getCreatedTime() {

      return createdTime;
   }


   /**
    * {@inheritDoc}
    */
   public int getWireableType() {

      return TYPE_CACHEABLE_VALUE;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final CacheableValue that = (CacheableValue) o;

      if (binaryValue != null ? !binaryValue.equals(that.binaryValue) : that.binaryValue != null) {
         return false;
      }

      if (timeToLeave != null ? !timeToLeave.equals(that.timeToLeave) : that.timeToLeave != null) {
         return false;
      }

      if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) {
         return false;
      }

      if (expirationTime != null ? !expirationTime.equals(
              that.expirationTime) : that.expirationTime != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = binaryValue != null ? binaryValue.hashCode() : 0;
      result = 31 * result + (timeToLeave != null ? timeToLeave.hashCode() : 0);
      result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
      result = 31 * result + (expirationTime != null ? expirationTime.hashCode() : 0);
      return result;
   }


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "CacheableValue{" +
              "value=" + binaryValue +
              ", expirationTime=" + timeToLeave +
              '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheableValue();
      }
   }
}
