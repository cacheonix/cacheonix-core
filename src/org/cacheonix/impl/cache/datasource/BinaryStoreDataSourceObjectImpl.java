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
package org.cacheonix.impl.cache.datasource;

import java.io.Serializable;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;

/**
 * An object that is returned by {@link BinaryStoreDataSource#get(Binary)}. <code>BinaryStoreDataSourceObject</code>
 * implements the Value Object Pattern.
 */
public final class BinaryStoreDataSourceObjectImpl implements BinaryStoreDataSourceObject {

   /**
    * Time it took to read from the data source.
    */
   private final Time timeToRead;

   /**
    * Object.
    */
   private final Serializable object;


   public BinaryStoreDataSourceObjectImpl(final Serializable object, final Time timeToRead) {

      this.timeToRead = timeToRead;
      this.object = object;
   }


   /**
    * @return a serializable object or null.
    * @see BinaryStoreDataSource#get(Binary)
    * @see Serializable
    */
   public Serializable getObject() {

      return object;
   }


   /**
    * {@inheritDoc}
    */
   public Time getTimeToRead() {

      return timeToRead;
   }


   public String toString() {

      return "BinaryStoreDataSourceObjectImpl{" +
              ", timeToRead=" + timeToRead +
              '}';
   }
}
