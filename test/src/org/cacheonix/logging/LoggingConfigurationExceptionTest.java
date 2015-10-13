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
package org.cacheonix.logging;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.config.LoggingConfigurationException;
import org.cacheonix.impl.util.logging.Logger;

/**
 * LoggingConfigurationExceptionTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jan 9, 2010 5:11:50 PM
 */
public final class LoggingConfigurationExceptionTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LoggingConfigurationExceptionTest.class); // NOPMD


   /**
    * @noinspection ThrowableInstanceNeverThrown
    */
   public void testGetCause() {

      final Throwable cause = new Throwable();
      assertEquals(cause, new LoggingConfigurationException(cause).getCause());
   }
}
