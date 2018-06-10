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
package org.cacheonix.impl.concept.exception;

import junit.framework.TestCase;
import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * StaticExceptionTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Sep 4, 2009 11:08:28 PM
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public final class StaticExceptionTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(StaticExceptionTest.class); // NOPMD

   private static final RetryException EXCEPTION = new RetryException("Test");


   public void testThrow() {

      try {
         //noinspection ThrowCaughtLocally
         throw EXCEPTION;
      } catch (final RetryException e) {
         assertEquals(EXCEPTION, e);
      }
   }
}
