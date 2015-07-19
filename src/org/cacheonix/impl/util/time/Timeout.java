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
package org.cacheonix.impl.util.time;

/**
 * A tracker for timeouts.
 */
public final class Timeout {

   private long duration = 0L;

   private boolean set = false;

   private long expires = 0L;


   /**
    * Creates a timeout.
    *
    * @param duration the timeout in milliseconds.
    */
   public Timeout(final long duration) {

      this.duration = duration;
      this.set = false;
   }


   /**
    * Returns the intended duration of the Timeout in milliseconds.
    */
   public long getDuration() {

      return duration;
   }


   /**
    * Returns <code>true</code> if the timeout has expired.
    */
   public boolean isExpired() {

      return set && duration > 0L && duration < Long.MAX_VALUE && System.currentTimeMillis() >= expires;
   }


   /**
    * Begins a new waiting cycle. This method starts a new waiting cycle regardless of whether this Timeout is already
    * waiting.
    */
   public Timeout reset() {

      this.set = true;
      this.expires = System.currentTimeMillis() + duration;
      return this;
   }


   /**
    * Cancels current waiting cycle. This method may be called whether or not this Timeout is actually waiting or not.
    */
   public void cancel() {

      this.set = false;
   }


   public String toString() {

      return "Timeout{" +
              "duration=" + duration +
              ", set=" + set +
              ", expires=" + expires +
              '}';
   }
}
