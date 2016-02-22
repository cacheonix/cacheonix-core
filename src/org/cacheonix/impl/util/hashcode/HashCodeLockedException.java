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
package org.cacheonix.impl.util.hashcode;

import org.cacheonix.CacheonixException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * This exception is thrown by <code>add()</code> methods of the {@link HashCode} class if the hash code has been
 * already calculated.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection SameParameterValue
 * @since Apr 13, 2008 6:07:26 PM
 */
public final class HashCodeLockedException extends CacheonixException {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(HashCodeLockedException.class); // NOPMD

   private static final long serialVersionUID = 0L;


   public HashCodeLockedException(final String message) {

      super(message);
   }
}
