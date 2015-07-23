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
package org.cacheonix;

import org.cacheonix.exceptions.CacheonixException;

/**
 * ReconfigurationException is thrown if Cacheonix configuration has changed in way that is not compatible with
 * continued waiting for a response from the method call.
 * <p/>
 * Example: A cluster node leaves a cluster and joins another cluster while waiting for acquiring a lock.
 * <code>Lock.lock()</code> will throw <code>ReconfigurationException</code>.
 */
public final class ReconfigurationException extends CacheonixException {

   private static final long serialVersionUID = -5935676107110854442L;


   /**
    * Creates ReconfigurationException.
    */
   public ReconfigurationException() { // NOPMD
   }


   public ReconfigurationException(final String message) {

      super(message);
   }
}
