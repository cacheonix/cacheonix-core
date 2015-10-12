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
package org.cacheonix.cache.invalidator;

import org.cacheonix.impl.cache.storage.disk.StorageException;

/**
 * An invalidateable cache element. When the back-end invalidation is enabled, Cacheonix passes an instance of
 * <code>Invalidateable</code> as a parameter to {@link CacheInvalidator#process(Invalidateable)}.
 * <p/>
 * To enable the back-end invalidation, pass a name of a class implementing <code>CacheInvalidator</code> in the
 * configuration attribute <code>invalidator</code>:
 * <pre>
 *   &lt;cache name="my.cache" maxSize="1000"
 *          <b>invalidator=</b>"my.project.DataBaseTimeStampInvalidator"/&gt;
 * </pre>
 *
 * @see CacheInvalidator
 */
public interface Invalidateable {

   /**
    * Returns the key of this cache element. Cacheonix always passes the key to <code>Invalidateable</code> by
    * reference.
    *
    * @return the key of this cache element.
    * @see CacheInvalidator#process(Invalidateable)
    */
   Object getKey();


   /**
    * Returns the value of this cache element. Cacheonix always passes the value to <code>Invalidateable</code> by
    * reference.
    *
    * @return the value of this cache element.
    * @see CacheInvalidator#process(Invalidateable)
    */
   Object getValue() throws StorageException;


   /**
    * Marks this cache element as invalid. Cacheonix will evict an invalid element.
    *
    * @see CacheInvalidator#process(Invalidateable)
    */
   void invalidate();
}
