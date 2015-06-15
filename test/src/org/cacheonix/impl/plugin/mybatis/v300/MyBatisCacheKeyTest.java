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

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.apache.ibatis.cache.CacheKey;

/**
 *
 */
public class MyBatisCacheKeyTest extends CacheonixTestCase {


   private MyBatisCacheKey myBatisCacheKey;


   public void testWriteReadExternal() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(myBatisCacheKey, ser.deserialize(ser.serialize(myBatisCacheKey)));

   }


   public void testEquals() throws Exception {

      final CacheKey cacheKey = new CacheKey();
      cacheKey.update("Object 1");
      cacheKey.update("Object 2");
      assertEquals(myBatisCacheKey, new MyBatisCacheKey(cacheKey));
   }


   public void testHashCode() throws Exception {

      assertEquals(1755436731, myBatisCacheKey.hashCode());
   }


   public void testToString() throws Exception {

      assertNotNull(myBatisCacheKey.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      final CacheKey cacheKey = new CacheKey();
      cacheKey.update("Object 1");
      cacheKey.update("Object 2");

      myBatisCacheKey = new MyBatisCacheKey(cacheKey);
   }


   public void tearDown() throws Exception {

      myBatisCacheKey = null;

      super.tearDown();
   }


}
