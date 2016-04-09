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

/**
 * An exception that Cacheonix throws if Cacheonix was shutdown.
 */
public final class ShutdownException extends CacheonixException {

   private static final long serialVersionUID = 6086455619561429669L;


   /**
    * Creates a new exception with <code>null</code> as its detail message.  The cause is not initialized, and may
    * subsequently be initialized by a call to {@link #initCause}.
    */
   public ShutdownException() { // NOPMD
   }


   /**
    * Creates a new exception with the specified detail message. The cause is not initialized, and may subsequently be
    * initialized by a call to {@link #initCause}.
    *
    * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
    *                method.
    */
   public ShutdownException(final String message) {

      super(message);
   }
}
