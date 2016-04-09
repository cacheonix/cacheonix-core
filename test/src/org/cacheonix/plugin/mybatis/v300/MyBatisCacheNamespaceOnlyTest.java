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
package org.cacheonix.plugin.mybatis.v300;

/**
 * Tests default configuration where only namespace cache is used.
 */
public class MyBatisCacheNamespaceOnlyTest extends MyBatisCacheTestDriver {

   private static final String TRUE_STRING = Boolean.TRUE.toString();

   private static final String FALSE_STRING = Boolean.FALSE.toString();

   private static final String SELECT_CACHE_TEMPLATE_NAME = "SelectCacheTemplateName";


   public void testSetGetEnableNamespaceCaching() throws Exception {

      assertEquals("Default should be 'true'", TRUE_STRING, myBatisCache.getEnableNamespaceCaching());
      myBatisCache.setEnableNamespaceCaching(FALSE_STRING);
      assertEquals(FALSE_STRING, myBatisCache.getEnableNamespaceCaching());
   }


   public void testSetGetNamespaceUpdatesInvalidateSelectCaches() throws Exception {

      assertEquals("Default should be 'true'", TRUE_STRING, myBatisCache.getNamespaceUpdatesInvalidateSelectCaches());
      myBatisCache.setNamespaceUpdatesInvalidateSelectCaches(FALSE_STRING);
      assertEquals(FALSE_STRING, myBatisCache.getNamespaceUpdatesInvalidateSelectCaches());
   }


   public void testSetGetEnableSelectCaching() throws Exception {

      assertEquals("Default should be 'false'", FALSE_STRING, myBatisCache.getEnablePerSelectCaching());
      myBatisCache.setEnablePerSelectCaching(TRUE_STRING);
      assertEquals(TRUE_STRING, myBatisCache.getEnablePerSelectCaching());
   }


   public void testSetGetSelectCacheTemplateName() throws Exception {

      assertNull("Default should be 'null'", myBatisCache.getSelectCacheTemplateName());
      myBatisCache.setSelectCacheTemplateName(SELECT_CACHE_TEMPLATE_NAME);
      assertEquals(SELECT_CACHE_TEMPLATE_NAME, myBatisCache.getSelectCacheTemplateName());
   }
}
