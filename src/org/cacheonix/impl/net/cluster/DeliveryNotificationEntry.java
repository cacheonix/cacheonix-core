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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.impl.net.processor.Request;

/**
 * An entry to the list containing messages waiting for delivery notification.
 */
final class DeliveryNotificationEntry {

   /**
    * The message that want to be notified.
    */
   private final Request request;

   /**
    * The start frame number of the message as it was send while having been partitioned into frames. Valid only if
    * <code>hasFrameNumber</code> set to true.
    */
   private long startFrameNumber;

   /**
    * Indicates that the <code>startFrameNumber</code> contains a meaningful value. This flag is raised when the start
    * frame number is set by calling <code>setStartFrameNumber()</code>.
    */
   private boolean hasStartFrameNumber;


   /**
    * Creates a new DeliveryNotificationEntry.
    *
    * @param request message that wants to be notified.
    */
   DeliveryNotificationEntry(final Request request) {

      this.request = request;
   }


   /**
    * Returns the message that want to be notified.
    *
    * @return message that want to be notified.
    */
   public Request getRequest() {

      return request;
   }


   /**
    * Sets the start frame number of the message as it was send while having been partitioned into frames. Calling this
    * method rises the flag <code>hasFrameNumber</code>.
    *
    * @param startFrameNumber the start frame number to set.
    */
   public void setStartFrameNumber(final long startFrameNumber) {

      this.startFrameNumber = startFrameNumber;
      this.hasStartFrameNumber = true;
   }


   /**
    * Returns the start frame number of the message as it was send while having been partitioned into frames. Callers
    * must always call  <code>hasStartFrameNumber()</code>  to make sure that the <code>startFrameNumber</code> is
    * valid.
    *
    * @return the start frame number of the message as it was send while having been partitioned into frames. Valid only
    *         if <code>hasStartFrameNumber()</code> returns true.
    * @throws IllegalStateException if the entry does not contain a valid start frame number.
    * @see #hasStartFrameNumber()
    */
   public long getStartFrameNumber() throws IllegalStateException {

      if (!hasStartFrameNumber) {

         throw new IllegalStateException("This entry foes not contain valid start frame number");
      }

      return startFrameNumber;
   }


   /**
    * Returns the flag indicating that that the <code>startFrameNumber</code> contains a meaningful value. This flag is
    * raised when the start frame number is set by calling <code>setStartFrameNumber()</code>.
    *
    * @return the flag indicating that that the <code>startFrameNumber</code> contains a meaningful value. This flag is
    *         raised when the start frame number is set by calling <code>setStartFrameNumber()</code>.
    * @see #setStartFrameNumber(long)
    */
   public boolean hasStartFrameNumber() {

      return hasStartFrameNumber;
   }
}
