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

import org.cacheonix.impl.net.serializer.Wireable;

/**
 * An immutable reading of a physical clock.
 */
public interface Time extends Comparable<Time>, Wireable {

   long getMillis();

   /**
    * Returns an event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    *
    * @return the event count used to advance the monotonic clock in cases repeated reads of the wall clock return the
    * same value.
    */
   long getCount();

   /**
    * Subtracts time from this time and returns result.
    *
    * @param time time to subtract.
    * @return a new Time object representing time differential.
    */
   Time subtract(Time time);

   /**
    * Adds time in milliseconds.
    *
    * @param timeMillis millisecond interval.
    * @return a new Time object representing the result of the addition.
    */
   Time add(long timeMillis);

   /**
    * Adds time in milliseconds.
    *
    * @param time time interval.
    * @return a new Time object representing the result of the addition.
    */
   Time add(Time time);

   Time divide(int size);
}
