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
package org.cacheonix.impl.cache.store;

import java.io.IOException;
import java.io.Serializable;

import org.cacheonix.cache.loader.Loadable;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.Time;

import static org.cacheonix.impl.cache.item.BinaryUtils.toBinary;

/**
 */
public class LoadableBinaryStoreAdapter implements Loadable {

   private final BinaryStore binaryStore;

   private final Clock clock;


   public LoadableBinaryStoreAdapter(final Clock clock, final BinaryStore binaryStore) {

      this.binaryStore = binaryStore;
      this.clock = clock;
   }


   /**
    * {@inheritDoc}
    *
    * @param key   a cache key.
    * @param value a value to load.
    * @throws StorageException
    * @throws IOException
    */
   public void load(final Serializable key, final Serializable value) throws StorageException, IOException {

      final Time expirationInterval = binaryStore.getExpirationInterval();
      final Time expirationTime = binaryStore.calculateExpirationTime(expirationInterval);
      final Time createdTime = clock.currentTime();
      binaryStore.put(toBinary(key), toBinary(value), createdTime, expirationTime, false, null);
   }
}
