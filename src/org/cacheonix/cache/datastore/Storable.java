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
package org.cacheonix.cache.datastore;

/**
 * An object to store using <code>DataStore</code>.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @see DataStore#store(Storable)
 */
public interface Storable {


   /**
    * Returns a key to save. Cacheonix always passes the key by reference. Special care should be taken not to modify
    * the key during the store operation.
    *
    * @return key to save.
    * @see DataStore#store(Storable)
    */
   Object getKey();


   /**
    * Returns an value to save. Cacheonix always passes the value object by reference. Special care should be taken not
    * to modify the value object during the store operation.
    *
    * @return object to save.
    * @see DataStore#store(Storable)
    */
   Object getValue();
}
