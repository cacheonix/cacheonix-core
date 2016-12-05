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
package org.cacheonix.cache.invalidator;

/**
 * Cache invalidator.
 * <p/>
 * Cacheonix uses classes implementing <code>CacheInvalidator</code> for custom backend invalidation of cache elements
 * such as invalidation based on a database record's time stamp or a version counter. An implementing class should
 * provide a public, no-argument, constructor.
 * <p/>
 * The name of the class implementing <code>CacheInvalidator</code> is configured using
 * <code>cacheonix-config.xml</code>.
 * <p/>
 * <b>Example:</b>
 * <pre>
 *   &lt;cache name="my.cache" maxSize="1000"
 *          <b>invalidator=</b>"my.project.DataBaseTimeStampInvalidator"
 *          <b>invalidatorProperties</b>="table.name=MY_TABLE;column.name=TIMESTAMP"/&gt;
 * </pre>
 *
 * @see CacheInvalidatorContext
 * @see Invalidateable
 */
public interface CacheInvalidator {

   /**
    * Sets the cache invalidator context. Cacheonix will call this method immediately after creating an instance of this
    * class that was provided to the cache configuration.
    *
    * @param context an instance of {@link CacheInvalidatorContext}
    */
   void setContext(final CacheInvalidatorContext context);


   /**
    * May invalidate the cache element. Cacheonix calls this method after locating a valid element requested by any read
    * methods of the cache.
    * <p/>
    * A class implementing  <code>CacheInvalidator</code>  decide sif the element has to be invalidated and calls {@link
    * Invalidateable#invalidate()} to mark the element as invalid. If the element is marked as invalid, Cacheonix will
    * evict it.
    *
    * @param cacheElement a cache element to invalidate.
    * @see Invalidateable#invalidate()
    */
   void process(final Invalidateable cacheElement);
}
