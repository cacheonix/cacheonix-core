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
package org.cacheonix.cache;

import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 * A an exception that indicates conditions that a reasonable application might want to catch.
 *
 * @noinspection ClassWithoutToString
 */
public class CacheException extends Exception {

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new exception with the specified detail message.  The cause is not initialized, and may subsequently
    * be initialized by a call to {@link #initCause}.
    *
    * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
    *                method.
    */
   public CacheException(final String message) {

      super(ExceptionUtils.prefixWithVersion(message));
   }


   /**
    * Constructs a new exception with the specified detail message and cause.  <p>Note that the detail message
    * associated with <code>cause</code> is <i>not</i> automatically incorporated in this exception's detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
    * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
    *                value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public CacheException(final String message, final Throwable cause) {

      super(ExceptionUtils.prefixWithVersion(message), cause);
   }


   /**
    * Constructs a new exception with the specified cause and a detail message of <tt>cause.toString())</tt> or null if
    * cause is not set (which typically contains the class and detail message of <tt>cause</tt>). This constructor is
    * useful for exceptions that are little more than wrappers for other throwables.
    *
    * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
    *              value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public CacheException(final Throwable cause) {

      super(ExceptionUtils.prefixWithVersion(cause.toString()), cause);
   }
}
