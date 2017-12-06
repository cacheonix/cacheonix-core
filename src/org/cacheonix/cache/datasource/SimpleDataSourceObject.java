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
package org.cacheonix.cache.datasource;

import java.io.Serializable;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A convenience implementation for <code>DataSourceObject</code>.
 *
 * @see DataSourceObject
 * @see DataSource
 */
public final class SimpleDataSourceObject implements DataSourceObject {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SimpleDataSourceObject.class); // NOPMD

   private final Serializable object;


   /**
    * Creates <code>SimpleCacheDataSourceObject</code> by wrapping the given object.
    *
    * @param object object to wrap.
    */
   public SimpleDataSourceObject(final Serializable object) {

      this.object = object;
   }


   /**
    * Returns the object wrapped by this SimpleDataSourceObject.
    *
    * @return object object wrapped by this SimpleDataSourceObject.
    */
   public Serializable getObject() {

      return object;
   }


   /**
    * Compares by comparing the given object to the wrapped objects.
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || !obj.getClass().equals(getClass())) {
         return false;
      }

      final SimpleDataSourceObject that = (SimpleDataSourceObject) obj;

      return object != null ? object.equals(that.object) : that.object == null;

   }


   /**
    * Returns the hash code of the wrapped object.
    */
   public int hashCode() {

      return object != null ? object.hashCode() : 0;
   }


   public String toString() {

      return "SimpleDataSourceObject{" +
              "object=" + object +
              '}';
   }
}
