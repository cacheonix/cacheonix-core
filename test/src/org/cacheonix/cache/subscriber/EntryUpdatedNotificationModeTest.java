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
package org.cacheonix.cache.subscriber;

import org.cacheonix.impl.util.logging.Logger;
import junit.framework.TestCase;

/**
 * EntryModifiedEventContentFlag Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>09/08/2008</pre>
 */
public final class EntryUpdatedNotificationModeTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryUpdatedNotificationModeTest.class); // NOPMD


   public void testToString() {

      assertNotNull(EntryModifiedNotificationMode.BATCH.toString());
      assertNotNull(EntryModifiedNotificationMode.SINGLE.toString());
   }


   public void testHashCode() {

      assertTrue(EntryModifiedNotificationMode.BATCH.hashCode() != 0);
      assertTrue(EntryModifiedNotificationMode.SINGLE.hashCode() != 0);
   }


   public void testEquals() {

      assertEquals(EntryModifiedNotificationMode.BATCH, EntryModifiedNotificationMode.BATCH);
      assertEquals(EntryModifiedNotificationMode.SINGLE, EntryModifiedNotificationMode.SINGLE);
      assertTrue(!EntryModifiedNotificationMode.BATCH.equals(EntryModifiedNotificationMode.SINGLE));
   }
}
