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
package org.cacheonix.impl.storage.disk;

import junit.framework.TestCase;

/**
 * Tests {@link StoredObjectImpl}.
 */
public final class StoredObjectImplTest extends TestCase {

   private static final int VALUE_OFFSET = 111;

   private static final int VALUE_LENGTH = 222;


   /**
    * Tests constructor.
    */
   public void testCreate() {

      final StoredObjectImpl storedObject = new StoredObjectImpl((long) VALUE_OFFSET, (long) VALUE_LENGTH);
      assertEquals((long) VALUE_LENGTH, storedObject.getValueLength());
      assertEquals((long) VALUE_OFFSET, storedObject.getValueOffset());
   }
}
