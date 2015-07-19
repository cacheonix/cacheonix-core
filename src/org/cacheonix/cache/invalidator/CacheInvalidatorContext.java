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

import java.util.Properties;

/**
 * Context for <code>CacheInvalidator</code>. <code>CacheInvalidator</code> uses <code>CacheInvalidatorContext</code> to
 * access its context data. Cacheonix provides an instance of <code>CacheInvalidatorContext</code> immediately after
 * creating a <code>CacheInvalidator</code> by calling {@link CacheInvalidator#setContext(CacheInvalidatorContext)}.
 *
 * @see CacheInvalidator#setContext(CacheInvalidatorContext)
 */
public interface CacheInvalidatorContext {

   /**
    * Returns the name of the cache the CacheInvalidator is defined for.
    *
    * @return name of the cache the CacheInvalidator is defined for.
    * @see CacheInvalidator
    */
   String getCacheName();


   /**
    * Returns a copy of the configuration properties for <code>CacheInvalidator</code> instance as defined in
    * <code>cacheonix-config.xml</code>.
    * <p/>
    * <b>Example:</b>
    * <pre>
    *   &lt;cache name="my.cache" maxSize="1000"
    *          invalidator="my.project.DataBaseTimeStampInvalidator"
    *          <b>invalidatorProperties</b>="table.name=MY_TABLE;colum.name=TIMESTAMP"/&gt;
    * </pre>
    *
    * @return a copy of the configuration properties for <code>CacheInvalidator</code> instance.
    * @see CacheInvalidator
    */
   Properties getProperties();
}
