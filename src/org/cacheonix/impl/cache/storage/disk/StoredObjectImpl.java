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
package org.cacheonix.impl.cache.storage.disk;

/**
 * Implementation of the StoredObject
 */
public final class StoredObjectImpl implements StoredObject {

   /**
    * Constructor.
    *
    * @param valueOffset offest of the value in the data storage
    * @param valueLength legth of the serialized value stored at offset.
    */
   public StoredObjectImpl(final long valueOffset, final long valueLength) {

      this.valueOffset = valueOffset;
      this.valueLength = valueLength;
   }


   /**
    * Offest of the value in the data storage.
    */
   private long valueOffset = 0L;

   /**
    * Legth of the serialized value stored at offset.
    */
   private long valueLength = 0L;


   /**
    * @return returns an offest in data file that serialized objected is stored at.
    */
   public long getValueOffset() {

      return valueOffset;
   }


   /**
    * @return legth of serialized value.
    */
   public long getValueLength() {

      return valueLength;
   }


   public String toString() {

      return "StoredObjectImpl{" +
              "valueOffset=" + valueOffset +
              ", valueLength=" + valueLength +
              '}';
   }
}
