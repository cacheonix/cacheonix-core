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
package org.cacheonix.locks;

import org.cacheonix.exceptions.CacheonixException;

/**
 * A runtime exception thrown when Cacheonix detects a deadlock.
 * <p/>
 * A deadlock is an inability to proceed due to two threads both requiring to release a lock held by the other thread.
 * Cacheonix prevents a thread from being locked in a deadlock by throwing a DeadlockException from
 * <code>lock()</code>.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see Lock#lock()
 */
public final class DeadlockException extends CacheonixException {

   /**
    * Creates <code>DeadlockException</code>.
    */
   public DeadlockException() { // NOPMD
   }


   /**
    * Create a new DeadlockException.
    *
    * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
    *                method.
    */
   public DeadlockException(final String message) {

      super(message);
   }
}
