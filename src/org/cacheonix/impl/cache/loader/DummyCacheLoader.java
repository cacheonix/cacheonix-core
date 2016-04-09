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
package org.cacheonix.impl.cache.loader;

import org.cacheonix.cache.loader.CacheLoader;
import org.cacheonix.cache.loader.CacheLoaderContext;
import org.cacheonix.cache.loader.Loadable;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Dummy cache loader.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 11, 2008 10:36:24 PM
 */
public final class DummyCacheLoader implements CacheLoader {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DummyCacheLoader.class); // NOPMD


   /**
    * Sets cache loader context. Cacheonix will call this method immediately after creating an instance of the class
    * that implements <code>CacheLoader</code>.
    *
    * @param context an instance of {@link CacheLoaderContext}
    */
   public void setContext(final CacheLoaderContext context) {

   }


   /**
    * {@inheritDoc}
    */
   public void load(final Loadable loadable) {

      // Do nothing
   }


   public String toString() {

      return "DummyCacheLoader{}";
   }
}
