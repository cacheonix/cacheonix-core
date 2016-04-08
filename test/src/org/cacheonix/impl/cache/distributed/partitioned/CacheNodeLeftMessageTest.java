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
package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.impl.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CacheNodeLeftMessageTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since May 24, 2010 9:49:23 PM
 */
public final class CacheNodeLeftMessageTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheNodeLeftMessageTest.class); // NOPMD


   public void testClearsFrontCache() throws Exception {

      // Mock front cache
      final FrontCache frontCache = mock(FrontCache.class);

      // Mock Cache processor
      final CacheProcessor cacheProcessor = mock(CacheProcessor.class);
      when(cacheProcessor.getState()).thenReturn(CacheProcessor.STATE_OPERATIONAL);
      when(cacheProcessor.getFrontCache()).thenReturn(frontCache);

      // Execute
      final CacheNodeLeftMessage cacheNodeLeftMessage = new CacheNodeLeftMessage();
      cacheNodeLeftMessage.setProcessor(cacheProcessor);
      cacheNodeLeftMessage.execute();

      // Verify
      verify(frontCache).clear();
   }


   /**
    * Tests that no exceptions occur when creating the object using a default constructor.
    */
   public void testDefaultConstructor() {

      assertNotNull(new CacheNodeLeftMessage().toString());
   }
}
