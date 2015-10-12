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
package org.cacheonix.impl.cache.storage.disk;

import org.cacheonix.cache.CacheException;

/**
 * <b>StorageException</b> is thrown when errors occur in the storage subsystem.
 *
 * @author sfichel@chacheonix.com
 */
public class StorageException extends CacheException {

   private static final long serialVersionUID = 0L;


   /**
    * @param cause - Throwable
    */
   public StorageException(final Throwable cause) {

      super(cause);
   }


   public StorageException(final String message, final Throwable cause) {

      super(message, cause);
   }


   public StorageException(final String message) {

      super(message);
   }
}
