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
package org.cacheonix.impl.util;

import java.util.Collections;
import java.util.Map;

import org.cacheonix.impl.util.array.HashMap;
import junit.framework.TestCase;

/**
 * Tester for CollectionUtils.
 */
public final class CollectionUtilsTest extends TestCase {

   @SuppressWarnings("RedundantCast")
   public void testIsEmptyDetectsEmptyMaps() throws Exception {

      assertTrue(CollectionUtils.isEmpty((Map) null));
      assertTrue(CollectionUtils.isEmpty(Collections.emptyMap()));
   }


   public void testIsEmptyDetectsNonEmptyMaps() throws Exception {

      final Map<Integer, Integer> nonEmptyMap = new HashMap<Integer, Integer>(1);
      nonEmptyMap.put(1, 1);
      assertFalse(CollectionUtils.isEmpty(nonEmptyMap));
   }
}
