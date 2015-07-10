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
package org.cacheonix.impl.cache.loader;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cacheonix.TestConstants;
import org.cacheonix.cache.loader.Loadable;
import junit.framework.TestCase;

/**
 * DummyCacheLoader Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>09/09/2008</pre>
 */
public final class DummyCacheLoaderTest extends TestCase {

   private DummyCacheLoader loader = null;


   public void testToString() {

      assertNotNull(loader.toString());
   }


   public void testLoad() {

      final AtomicBoolean loadCalled = new AtomicBoolean(false);
      loader.load(new Loadable() {

         public void load(final Serializable key, final Serializable value) {

            loadCalled.set(true);
         }
      });

      assertFalse(loadCalled.get());
   }


   protected void setUp() throws Exception {

      super.setUp();
      final CacheLoaderContextImpl context = new CacheLoaderContextImpl(TestConstants.LOCAL_TEST_CACHE, new Properties());
      loader = new DummyCacheLoader();
      loader.setContext(context);
   }


   public String toString() {

      return "DummyCacheLoaderTest{" +
              "loader=" + loader +
              "} " + super.toString();
   }
}
