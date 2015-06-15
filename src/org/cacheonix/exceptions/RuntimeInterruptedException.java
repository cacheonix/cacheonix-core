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
package org.cacheonix.exceptions;

import org.cacheonix.impl.util.logging.Logger;

/**
 * Thrown when a thread is waiting, sleeping, or otherwise paused for a long time and another thread interrupts it using
 * the <code>interrupt</code>  method in class <code>Thread</code>.
 * <p/>
 * RuntimeInterruptedException is used to wrap {@link InterruptedException} when a signature of an implemented API does
 * not declare  InterruptedException. A call to <code>Thread.currentThread().interrupt()</code> always precedes throwing
 * this exception.
 */
public final class RuntimeInterruptedException extends CacheonixException {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RuntimeInterruptedException.class); // NOPMD

   private static final long serialVersionUID = 0L;


   /**
    * Creates a new runtime exception with the specified cause.
    *
    * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
    */
   public RuntimeInterruptedException(final InterruptedException cause) {

      super(cause);
   }


   /**
    * Constructs a new runtime exception with the specified detail message and cause.  <p>Note that the detail message
    * associated with <code>cause</code> is <i>not</i> automatically incorporated in this runtime exception's detail
    * message.
    *
    * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
    * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
    *                value is permitted, and indicates that the cause is nonexistent or unknown.)
    * @since 1.4
    */
   public RuntimeInterruptedException(final String message, final InterruptedException cause) {

      super(message, cause);
   }
}
