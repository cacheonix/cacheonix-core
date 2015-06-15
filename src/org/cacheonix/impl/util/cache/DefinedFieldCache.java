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
package org.cacheonix.impl.util.cache;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
final class DefinedFieldCache extends LinkedHashMap<String, Field[]> {

   private static final int CACHE_SIZE = 1000;


   DefinedFieldCache() {

      super(CACHE_SIZE, 0.75f, true);
   }


   protected boolean removeEldestEntry(final Map.Entry<String, Field[]> eldest) {

      return size() > CACHE_SIZE;
   }
}
