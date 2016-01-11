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

import org.cacheonix.CacheonixException;

/**
 * An exception that is thrown when there are runtime problems with Cacheonix configuration.
 *
 * @noinspection ClassWithoutToString
 */
public class ConfigurationException extends CacheonixException {

   private static final long serialVersionUID = 0L;


   /**
    * Constructor.
    *
    * @param message exception message.
    */
   public ConfigurationException(final String message) {

      super(message);
   }


   /**
    * Creates a new ConfigurationException.
    *
    * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
    *              value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public ConfigurationException(final Throwable cause) {

      super(cause);
   }


   /**
    * Creates a new ConfigurationException.
    *
    * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
    * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
    *                value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public ConfigurationException(final String message, final Throwable cause) {

      super(message, cause);
   }
}
