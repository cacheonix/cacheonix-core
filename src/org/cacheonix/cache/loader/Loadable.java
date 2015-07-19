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
package org.cacheonix.cache.loader;

import java.io.IOException;
import java.io.Serializable;

import org.cacheonix.impl.storage.disk.StorageException;

/**
 * Loadable is a class that is passed to {@link CacheLoader#load(Loadable)} in order to load data into Cacheonix at
 * startup.
 */
public interface Loadable {

   /**
    * Loads a value associated with a key into Cacheonix.
    *
    * @param key   the cache key.
    * @param value the value to load.
    */
   void load(Serializable key, Serializable value) throws StorageException, IOException;
}
