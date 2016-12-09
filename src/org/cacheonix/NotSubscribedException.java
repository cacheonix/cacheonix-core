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
package org.cacheonix;

import java.util.Set;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;

/**
 * An exception thrown when attempting to un-subscribe a subscriber that it not subscribed.
 *
 * @see Cache#removeEventSubscriber(Set, EntryModifiedSubscriber)
 */
public final class NotSubscribedException extends CacheonixException {

   private static final long serialVersionUID = 1953567097120431016L;


   /**
    * Creates a new <code>NotSubscribedException</code>.
    *
    * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
    *                method.
    */
   public NotSubscribedException(final String message) {

      super(message);
   }
}
