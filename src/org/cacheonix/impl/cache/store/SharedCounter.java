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
package org.cacheonix.impl.cache.store;

/**
 * Tracks number of elements across a set of buckets.
 */
public final class SharedCounter {

   private long counter = 0L;

   private final long maxValue;


   /**
    * Creates a counter.
    *
    * @param maxValue a maximum value. The maximum value is used for reference and does not affect counting.
    */
   public SharedCounter(final long maxValue) {

      this.maxValue = maxValue;
   }


   /**
    * Increments the counter.
    *
    * @return a new counter value.
    */
   public long increment() {

      ++counter;

      return counter;
   }


   /**
    * Decrement the counter.
    *
    * @return a new counter value.
    */
   public long decrement() {

      --counter;

      return counter;
   }


   /**
    * Adds a value to the counter.
    *
    * @param value the value to add to the counter.
    * @return a new counter value.
    */
   public long add(final long value) {

      counter = counter + value;

      return counter;
   }


   /**
    * Subtracts a value from the counter.
    *
    * @param value the value to subtract from the counter.
    * @return a new counter value.
    */
   public long subtract(final long value) {

      counter = counter - value;

      return counter;
   }


   /**
    * Returns a counter value.
    *
    * @return the counter value.
    */
   public long value() {

      return counter;
   }


   public long getMaxValue() {

      return maxValue;
   }


   /**
    * Returns <code>true</code> if the counter has unlimited size. Otherwise returns <code>false</code>.
    *
    * @return <code>true</code> if the counter has unlimited size. Otherwise returns <code>false</code>.
    */
   public boolean isUnlimitedSize() {

      return maxValue <= 0L;
   }


   public String toString() {

      return "SharedCounter{" +
              "counter=" + counter +
              ", maxValue=" + maxValue +
              '}';
   }
}

