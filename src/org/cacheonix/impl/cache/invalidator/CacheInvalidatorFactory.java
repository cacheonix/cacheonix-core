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
package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import org.cacheonix.CacheonixException;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A factory to create a CacheInvalidator based on its class and parameters.
 */
public final class CacheInvalidatorFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheInvalidatorFactory.class); // NOPMD


   /**
    * The name of the dummy invalidator class.
    */
   private static final String DUMMY_INVALIDATOR_NAME = DummyCacheInvalidator.class.getName();


   /**
    * Instantiates a cache invalidator.
    *
    * @param cacheName             the cache name.
    * @param invalidatorClassName  the name of class implementing {@link CacheInvalidator}.
    * @param invalidatorProperties the properties passed to the cache invalidator instance after it has been
    *                              instantiated.
    * @return a new instance of the cache invalidator.
    */
   public CacheInvalidator createInvalidator(final String cacheName, final String invalidatorClassName,
           final Properties invalidatorProperties) {

      final CacheInvalidator invalidator;
      try {
         // Inform
         final boolean invalidatorBlank = StringUtils.isBlank(invalidatorClassName);
         if (!invalidatorBlank) {
            LOG.info("Creating invalidator for " + cacheName + ": " + invalidatorClassName);
         }
         // Create invalidator
         final Class clazz = Class.forName(invalidatorBlank ? DUMMY_INVALIDATOR_NAME : invalidatorClassName);
         if (!CacheInvalidator.class.isAssignableFrom(clazz)) {
            throw new CacheonixException("Class " + invalidatorClassName
                    + " configured as an invalidator for cache "
                    + cacheName + " does not implement interface CacheInvalidator");
         }
         invalidator = (CacheInvalidator) clazz.getConstructor().newInstance();
         // Set Context
         invalidator.setContext(new CacheInvalidatorContextImpl(cacheName, invalidatorProperties));
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
      return invalidator;
   }
}
