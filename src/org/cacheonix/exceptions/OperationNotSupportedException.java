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
 * This exception is thrown when a context implementation does not support the operation being invoked.
 * <p/>
 * Synchronization and serialization issues that apply to RuntimeException apply directly here.
 */
public final class OperationNotSupportedException extends CacheonixException {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(OperationNotSupportedException.class); // NOPMD

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new instance of OperationNotSupportedException using an explanation. All other fields default to
    * null.
    *
    * @param explanation Possibly null additional detail about this exception
    * @see Throwable#getMessage
    */
   public OperationNotSupportedException(final String explanation) {

      super(explanation);
   }


   /**
    * Constructs a new instance of OperationNotSupportedException using cause's message as an explanation. All other
    * fields default to null.
    *
    * @param cause Possibly null additional detail about this exception
    * @see Throwable#getMessage
    */
   public OperationNotSupportedException(final Throwable cause) {

      super(cause);
   }


   public OperationNotSupportedException() {

   }
}
