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
package org.cacheonix.impl.plugin.mybatis.v300;

import java.io.Serializable;

/**
 * A cache adapter that doesn't do anything.
 */
public final class DummyMyBatisCacheAdapter implements MyBatisCacheAdapter {

   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing.
    */
   public void put(final Serializable key, final Serializable value) {

   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing.
    */
   public Object remove(final Serializable key) {

      return null;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing.
    */
   public Object get(final Serializable key) {

      return null;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing.
    */
   public void clear() {

   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns 0.
    */
   public int size() {

      return 0;
   }
}
