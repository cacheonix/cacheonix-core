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
package org.cacheonix.impl.cluster.node;

import org.cacheonix.exceptions.CacheonixException;

/**
 * Indicates that selector.select() experienced an unrecoverable I/O error. The primary use of this exception is to wrap
 * an IOException thrown by selector.select() in order to distinguish from other IOExceptions.
 */
public final class UnrecoverableSelectException extends CacheonixException {

   public UnrecoverableSelectException() {

   }


   public UnrecoverableSelectException(final String message) {

      super(message);
   }


   public UnrecoverableSelectException(final String message, final Throwable cause) {

      super(message, cause);
   }


   public UnrecoverableSelectException(final Throwable cause) {

      super(cause);
   }
}
