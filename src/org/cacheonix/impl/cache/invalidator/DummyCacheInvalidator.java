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

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.invalidator.CacheInvalidatorContext;
import org.cacheonix.cache.invalidator.Invalidateable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * DummyCacheInvalidator
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Jul 30, 2008 4:30:32 PM
 */
public final class DummyCacheInvalidator implements CacheInvalidator {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DummyCacheInvalidator.class); // NOPMD


   /**
    * {@inheritDoc}
    */
   public void setContext(final CacheInvalidatorContext context) {

   }


   /**
    * {@inheritDoc}
    */
   public void process(final Invalidateable cacheElement) {

   }


   public String toString() {

      return "DummyCacheInvalidator{" +
              '}';
   }
}
