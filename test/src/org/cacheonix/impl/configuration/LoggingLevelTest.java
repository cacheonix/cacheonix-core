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
package org.cacheonix.impl.configuration;

import java.io.IOException;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.util.logging.Logger;

/**
 * LoggingLevelTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Jan 9, 2010 5:14:12 PM
 */
public final class LoggingLevelTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(LoggingLevelTest.class); // NOPMD


   public void testLevelsAreDifferent() {

      assertFalse(LoggingLevel.DEBUG.equals(LoggingLevel.ERROR));
      assertFalse(LoggingLevel.DEBUG.equals(LoggingLevel.INFO));
      assertEquals(LoggingLevel.DEBUG, LoggingLevel.DEBUG);
      assertEquals(LoggingLevel.ERROR, LoggingLevel.ERROR);
      assertEquals(LoggingLevel.INFO, LoggingLevel.INFO);
   }


   /**
    * @noinspection JUnitTestMethodWithNoAssertions
    */
   public void testReadResource() {

      readResource(LoggingLevel.DEBUG);
      readResource(LoggingLevel.INFO);
      readResource(LoggingLevel.ERROR);
   }


   /**
    * Fails if resource cannot be read.
    *
    * @param loggingLevel a logging level.
    */
   private static void readResource(final LoggingLevel loggingLevel) {

      try {
         loggingLevel.readResource();
      } catch (final IOException e) {
         LOG.error(e, e);
         fail("Exception should not be thrown: " + e);
      }
   }
}
