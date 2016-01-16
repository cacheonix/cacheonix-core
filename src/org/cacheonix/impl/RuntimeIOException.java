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
package org.cacheonix.impl;

import java.io.IOException;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Thrown when an IO error occurs.
 * <p/>
 * RuntimeIOException is used to wrap {@link IOException} when a signature of an implemented API does not declare
 * Exception.
 */
public final class RuntimeIOException extends CacheonixException {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RuntimeIOException.class); // NOPMD

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new runtime exception with the specified cause.
    *
    * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
    */
   public RuntimeIOException(final Exception cause) {

      super(StringUtils.toString(cause), cause);
   }
}
