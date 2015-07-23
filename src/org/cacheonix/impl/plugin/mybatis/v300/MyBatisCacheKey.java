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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;

/**
 * An externalizable adapter for <code>org.apache.ibatis.cache.CacheKey</code>.
 */
@SuppressWarnings("RedundantIfStatement")
public final class MyBatisCacheKey implements Externalizable {

   private static final long serialVersionUID = 5913884318234726314L;

   private static final Class<? extends CacheKey> CACHE_KEY_CLASS = CacheKey.class;

   private static final Field HASH_CODE_FIELD = getField("hashcode");

   private static final Field CHECKSUM_FIELD = getField("checksum");

   private static final Field COUNT_FIELD = getField("count");

   private static final Field UPDATE_LIST_FIELD = getField("updateList");


   private int hashcode;

   private long checksum;

   private int count;

   private List<Object> updateList;


   /**
    * Default constructor required by Externalizable.
    */
   public MyBatisCacheKey() {

   }


   /**
    * Creates a new MyBatisCacheKey.
    *
    * @param cacheKey the key to create MyBatisCacheKey from.
    */
   @SuppressWarnings("unchecked")
   public MyBatisCacheKey(final CacheKey cacheKey) {

      try {
         hashcode = (Integer) HASH_CODE_FIELD.get(cacheKey);
         checksum = (Long) CHECKSUM_FIELD.get(cacheKey);
         count = (Integer) COUNT_FIELD.get(cacheKey);
         updateList = (List<Object>) UPDATE_LIST_FIELD.get(cacheKey);

      } catch (final IllegalAccessException e) {
         throw new IllegalArgumentException(e);
      }
   }


   /**
    * Returns an ID of the select statement.
    *
    * @return the ID of the select statement.
    */
   public final String getSelectID() {

      return (String) updateList.get(0);
   }


   /**
    * Gets a <code>org.apache.ibatis.cache.CacheKey</code> field.
    *
    * @param fieldName the field name.
    * @return the field.
    * @throws IllegalArgumentException if there is no such field.
    */
   private static Field getField(final String fieldName) throws IllegalArgumentException {

      try {
         final Field declaredField = CACHE_KEY_CLASS.getDeclaredField(fieldName);
         declaredField.setAccessible(true);
         return declaredField;
      } catch (final NoSuchFieldException e) {
         throw new IllegalArgumentException(e);
      }
   }


   /**
    * Save this object contents by calling the methods of DataOutput for its primitive values or calling the writeObject
    * method of ObjectOutput for objects, strings, and arrays.
    *
    * @param out the stream to write the object to.
    * @throws IOException Includes any I/O exceptions that may occur.
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      out.writeInt(hashcode);
      out.writeLong(checksum);
      out.writeInt(count);

      for (final Object listObject : updateList) {
         out.writeObject(listObject);
      }
   }


   /**
    * Restores restores this object  contents by calling the methods of DataInput for primitive types and readObject for
    * objects, strings and arrays.  The readExternal method must read the values in the same sequence and with the same
    * types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object.
    * @throws IOException            if I/O errors occur.
    * @throws ClassNotFoundException If the class for an object being restored cannot be found.
    */
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

      hashcode = in.readInt();
      checksum = in.readLong();
      count = in.readInt();

      updateList = new ArrayList<Object>(count);
      for (int i = 0; i < count; i++) {
         updateList.add(in.readObject());
      }
   }


   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final MyBatisCacheKey that = (MyBatisCacheKey) obj;

      if (checksum != that.checksum) {
         return false;
      }
      if (count != that.count) {
         return false;
      }
      if (hashcode != that.hashcode) {
         return false;
      }
      if (updateList != null ? !updateList.equals(that.updateList) : that.updateList != null) {
         return false;
      }

      return true;
   }


   /**
    * Returns a hash code value for the object.
    */
   public int hashCode() {

      return hashcode;
   }


   /**
    * Returns a string representation of the object.
    *
    * @return a string representation of the object.
    */
   public String toString() {

      return "MyBatisCacheKey{" +
              "hashcode=" + hashcode +
              ", checksum=" + checksum +
              ", count=" + count +
              ", updateList=" + updateList +
              '}';
   }
}
