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
package org.cacheonix.impl.cache.datasource;

import java.util.ArrayList;
import java.util.Collection;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.store.BinaryStoreElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.logging.Logger;

/**
 * DummyBinaryStoreDataSource
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 30, 2008 12:15:15 AM
 */
public final class DummyBinaryStoreDataSource implements BinaryStoreDataSource {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DummyBinaryStoreDataSource.class); // NOPMD


   /**
    * {@inheritDoc}
    */
   public BinaryStoreDataSourceObject get(final Binary key) {

      return null;
   }


   /**
    * {@inheritDoc}
    */
   public Collection<BinaryStoreDataSourceObject> get(final Collection keys) {

      final int size = keys.size();
      final ArrayList<BinaryStoreDataSourceObject> result = new ArrayList<BinaryStoreDataSourceObject>(size);

      for (int i = 0; i < size; i++) {
         result.add(null);
      }

      return result;
   }


   public void schedulePrefetch(final BinaryStoreElement newElement, final Time timeTookToReadFromDataSource) {
      // Do nothing.
   }


   public String toString() {

      return "DummyBinaryStoreDataSource{" +
              '}';
   }
}
