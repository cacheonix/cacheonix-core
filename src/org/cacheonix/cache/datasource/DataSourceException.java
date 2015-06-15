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
package org.cacheonix.cache.datasource;

import org.cacheonix.exceptions.CacheonixException;

/**
 * A runtime exception that a <tt>DataSource</tt> implementation may throw if an error occured while retrieving data.
 * Cacheonix recommends wrapping checked exceptions produced by the <tt>DataSource</tt> implementations into
 * <tt>DataSourceException<tt>.
 */
public final class DataSourceException extends CacheonixException {

   /**
    * Creates a new DataSourceException  with <code>null</code> as its detail message.  The cause is not initialized,
    * and may subsequently be initialized by a call to {@link #initCause}.
    */
   @SuppressWarnings("UnusedDeclaration")
   public DataSourceException() {

   }


   /**
    * Creates a new DataSourceException  with the specified detail message. The cause is not initialized, and may
    * subsequently be initialized by a call to {@link #initCause}.
    *
    * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
    *                method.
    */
   @SuppressWarnings("UnusedDeclaration")
   public DataSourceException(final String message) {

      super(message);
   }


   /**
    * Creates a new DataSourceException  with the specified detail message and cause.  <p>Note that the detail message
    * associated with <code>cause</code> is <i>not</i> automatically incorporated in this runtime exception's detail
    * message.
    *
    * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
    * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
    *                value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   @SuppressWarnings("UnusedDeclaration")
   public DataSourceException(final String message, final Throwable cause) {

      super(message, cause);
   }


   /**
    * Creates a new DataSourceException  with the specified cause and a detail message of <tt>(cause==null ? null :
    * cause.toString())</tt> (which typically contains the class and detail message of <tt>cause</tt>).  This
    * constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
    *
    * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
    *              value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public DataSourceException(final Throwable cause) {

      super(cause);
   }
}
