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
public final class EntryUpdatedEventFlagTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(EntryUpdatedEventFlagTest.class); // NOPMD


   public void testToString() {

      assertNotNull(EntryModifiedEventContentFlag.NEED_KEY.toString());
      assertNotNull(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE.toString());
      assertNotNull(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE.toString());
   }


   public void testHashCode() {

      assertTrue(EntryModifiedEventContentFlag.NEED_KEY.hashCode() != 0);
      assertTrue(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE.hashCode() != 0);
      assertTrue(EntryModifiedEventContentFlag.NEED_NEW_VALUE.hashCode() != 0);
   }


   public void testEquals() {

      assertEquals(EntryModifiedEventContentFlag.NEED_KEY, EntryModifiedEventContentFlag.NEED_KEY);
      assertTrue(!EntryModifiedEventContentFlag.NEED_KEY.equals(EntryModifiedEventContentFlag.NEED_PREVIOUS_VALUE));
      assertTrue(!EntryModifiedEventContentFlag.NEED_KEY.equals(EntryModifiedEventContentFlag.NEED_NEW_VALUE));
   }
}
