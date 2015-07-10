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
package org.cacheonix.cache.configuration;

import org.cacheonix.impl.configuration.LoggingLevel;
import junit.framework.TestCase;

/**
 * LoggingLevel Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>06/01/2008</pre>
 */
public final class LoggingLevelTest extends TestCase {

   private static final String DEBUG = "debug";

   private static final String INFO = "info";

   private static final String ERROR = "error";

   private static final String NEVER_EXISTED = "never-existed";


   public void testConvert() {

      assertEquals(LoggingLevel.DEBUG, LoggingLevel.convert(DEBUG));
      assertEquals(LoggingLevel.DEBUG, LoggingLevel.convert(DEBUG.toUpperCase()));

      assertEquals(LoggingLevel.INFO, LoggingLevel.convert(INFO));
      assertEquals(LoggingLevel.INFO, LoggingLevel.convert(INFO.toUpperCase()));

      assertEquals(LoggingLevel.ERROR, LoggingLevel.convert(ERROR));
      assertEquals(LoggingLevel.ERROR, LoggingLevel.convert(ERROR.toUpperCase()));

      assertEquals(LoggingLevel.WARN, LoggingLevel.convert(NEVER_EXISTED));
      assertEquals(LoggingLevel.WARN, LoggingLevel.convert(NEVER_EXISTED.toUpperCase()));
   }


   public void testToString() {

      assertNotNull(LoggingLevel.DEBUG.toString());
   }


   public void testHashCode() {

      assertTrue(LoggingLevel.DEBUG.hashCode() != 0);
   }
}
