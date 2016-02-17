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
package org.cacheonix.impl.net.processor;

import org.cacheonix.impl.OperationNotSupportedException;
import org.cacheonix.impl.util.logging.Logger;

import static org.cacheonix.impl.util.StringUtils.isBlank;

/**
 * Waiter's waitForResult throws this exception when the result could not be obtained. Retrying the request may be
 * necessary of the request semantics supports it. Otherwise this exception should be re-thrown as {@link
 * OperationNotSupportedException}.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @see Waiter#waitForResult
 * @since Aug 19, 2009 10:48:41 PM
 */
public final class RetryException extends Exception {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RetryException.class); // NOPMD

   private static final long serialVersionUID = 0L;


   public RetryException(final String result) {

      super(result);
   }


   /**
    * Returns <code>true<code> if the exception message is blank.
    *
    * @return <code>true<code> if the exception message is blank.
    * @see #getMessage()
    */
   public boolean isMessageBlank() {

      return isBlank(getMessage());
   }
}
