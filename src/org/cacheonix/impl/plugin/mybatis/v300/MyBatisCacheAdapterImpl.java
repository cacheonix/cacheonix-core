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
package org.cacheonix.impl.plugin.mybatis.v300;

import java.io.Serializable;

import org.cacheonix.cache.Cache;

/**
 * A cache adapter for myBatis.
 */
public final class MyBatisCacheAdapterImpl implements MyBatisCacheAdapter {

   private final Cache<Serializable, Serializable> cache;


   public MyBatisCacheAdapterImpl(final Cache<Serializable, Serializable> cache) {

      //noinspection AssignmentToCollectionOrArrayFieldFromParameter
      this.cache = cache;
   }


   public void put(final Serializable key, final Serializable value) {

      cache.put(key, value);
   }


   public Object remove(final Serializable key) {

      return cache.remove(key);
   }


   public Object get(final Serializable key) {

      return cache.get(key);
   }


   public void clear() {

      cache.clear();
   }


   public int size() {

      return cache.size();
   }


   public String toString() {

      return "MyBatisCacheAdapterImpl{" +
              "cache=" + cache +
              '}';
   }
}
