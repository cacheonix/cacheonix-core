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
package org.cacheonix.impl.clock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * An immutable reading of a physical clock.
 */
@SuppressWarnings("RedundantIfStatement")
public final class TimeImpl implements Time {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Zero time.
    */
   public static final Time ZERO = new TimeImpl(0L, 0L);

   private long millis = 0L;

   private long count = 0L;


   /**
    * Default constructor required by Wireable.
    */
   public TimeImpl() {

   }


   public TimeImpl(final long millis, final long count) {

      this.millis = millis;
      this.count = count;
   }


   public long getMillis() {

      return millis;
   }


   public long getCount() {

      return count;
   }


   public Time subtract(final Time time) {

      return new TimeImpl(millis - time.getMillis(), count - time.getMillis());
   }


   public Time add(final long timeMillis) {

      if (timeMillis == 0) {

         return this;
      } else {

         return new TimeImpl(millis + timeMillis, count);
      }
   }


   public Time add(final Time time) {


      if (time.getMillis() == 0 && time.getCount() == 0) {

         return this;
      } else {

         return new TimeImpl(millis + time.getMillis(), count + time.getCount());
      }
   }


   public Time divide(final int size) {

      return new TimeImpl(millis / size, count / size);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeLong(millis);
      out.writeLong(count);
   }


   public void readWire(final DataInputStream in) throws IOException {

      millis = in.readLong();
      count = in.readLong();
   }


   public int getWireableType() {

      return Wireable.TYPE_TIME;
   }


   public int compareTo(final Object o) {

      if (!(o instanceof TimeImpl)) {

         return 1;
      }

      final TimeImpl other = (TimeImpl) o;
      if (millis > other.millis) {

         return 1;
      }

      if (millis < other.millis) {

         return -1;
      }

      if (count > other.count) {

         return 1;
      }

      if (count < other.count) {

         return -1;
      }

      return 0;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final TimeImpl time = (TimeImpl) o;

      if (count != time.count) {
         return false;
      }
      if (millis != time.millis) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = (int) (millis ^ (millis >>> 32));
      result = 31 * result + (int) (count ^ (count >>> 32));
      return result;
   }


   public String toString() {

      return "Time{" + millis + ", " + count + '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new TimeImpl();
      }
   }
}
