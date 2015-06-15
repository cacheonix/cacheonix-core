/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.exceptions.CacheonixException;
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
    * Creates data source.
    *
    * @param cacheName
    * @param invalidatorClass
    * @param invalidatorProperties
    * @return
    */
   public CacheInvalidator createInvalidator(final String cacheName, final String invalidatorClass,
                                             final Properties invalidatorProperties) {

      final CacheInvalidator invalidator;
      try {
         // Inform
         final boolean invalidatorBlank = StringUtils.isBlank(invalidatorClass);
         if (!invalidatorBlank) {
            LOG.info("Creating invalidator for " + cacheName + ": " + invalidatorClass);
         }
         // Create invalidator
         final Class clazz = Class.forName(invalidatorBlank ? DUMMY_INVALIDATOR_NAME : invalidatorClass);
         if (!CacheInvalidator.class.isAssignableFrom(clazz)) {
            throw new CacheonixException("Class " + invalidatorClass
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
