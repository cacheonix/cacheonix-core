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
package org.cacheonix.locks;

import org.cacheonix.exceptions.CacheonixException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * An runtime exception thrown when a lock's <code>unlock()</code> method was called after the lock was forcibly
 * released.
 * <p/>
 * It can happen when lock was not released in time (unlock timeout), when number of calls to <code>unlock()</code> does
 * not match the number of calls to <code>lock()</code>, or when the cluster node joined another cluster while holding a
 * lock. because it was unlock timed out.
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 */
public final class BrokenLockException extends CacheonixException {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BrokenLockException.class); // NOPMD   

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new exception with the specified detail message.  The cause is not initialized, and may subsequently
    * be initialized by a call to {@link #initCause}.
    */
   public BrokenLockException() {

      super("Lock has already been unlocked, a number unlocks is greater than a number of locks or cluster re-configuration has occurred");
   }
}
