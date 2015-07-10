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

import org.cacheonix.impl.util.logging.Logger;

/**
 * Dummy object size calculator always return zero for the object size in bytes.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 26, 2008 3:54:09 AM
 */
public final class DummyObjectSizeCalculator implements ObjectSizeCalculator {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DummyObjectSizeCalculator.class); // NOPMD


   public long sizeOf(final Object obj) {

      return 0;
   }


   /**
    * Calculates cached object size.
    *
    * @param value3
    * @param key    object's key
    * @param value  object's value
    * @return object size
    */
   public long sum(final long sizeOfKey, final long value2, final long value3) {

      return 0L;
   }
}
