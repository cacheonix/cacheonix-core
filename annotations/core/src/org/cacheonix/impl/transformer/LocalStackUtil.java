/**
 *
 */
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
package org.cacheonix.impl.transformer;

import org.objectweb.asm.Type;

/**
 * Utility class used to generate new method body for the methods that are tagged with Cacheonix annotations.
 * <p/>
 * The offsets are used based on the injected implementation for a class. For example if the user method looks like
 * <pre>
 *   &#64;DataSource
 * 	 public String get3Item(String key1, String key2, String key3) {
 * 		return find(searchKey);
 *   }
 * </pre>
 * <p/>
 * then the existing method will be renamed to orig$Cacheonix$get3Items and a new method will be generated
 * <pre>
 *   public String get3Item(String key1, String key2, String key3) {
 *       final Logger LOG = Logger.getLogger(ByteCodeExample1.class);
 *       CacheManager inst;
 *       Object val = null;
 *
 *       GeneratedKey key = new GeneratedKey(GeneratedKey.addKeyElement(key1),
 *                                           GeneratedKey.addKeyElement(key2),
 *                                           GeneratedKey.addKeyElement(key3) );
 *       Cache cache = null;
 *
 *       try
 *       {
 *           inst = CacheManager.getInstance(filePath);
 *       }
 *       catch (Exception e)
 *       {
 *           inst = null;
 *           LOG.e(">>>>> Exception getting CacheManager ", e);
 *       }
 *
 *       if (inst != null)
 *       {
 *           cache = inst.getCache(cacheName);
 *           val = cache.get(key);
 *           if (val == null)
 *           {
 *               val = orig$Cacheonix$get3Items(key1, key2, key3);
 *               cache.put(key, val);
 *           }
 *       }
 *
 *       return (String)val;
 *   }
 * </pre>
 * The local variable offsets are calculated based on the generated method
 */
public class LocalStackUtil {

   // OFFSETS for DataSource
   public static final int OFFSET_CD_LOG_VARIABLE = 1;

   public static final int OFFSET_CD_CACHE_MANAGER_INSTANCE_VARIABLE = OFFSET_CD_LOG_VARIABLE + 1;

   public static final int OFFSET_CD_VALUE_OBJECT_VARIABLE = OFFSET_CD_CACHE_MANAGER_INSTANCE_VARIABLE + 1;

   public static final int OFFSET_CD_GENERATED_KEY_VARIABLE = OFFSET_CD_VALUE_OBJECT_VARIABLE + 1;

   public static final int OFFSET_CD_CACHE_REFERENCE_VARIABLE = OFFSET_CD_GENERATED_KEY_VARIABLE + 1;

   public static final int OFFSET_CD_EXCEPTION_VARIABLE = OFFSET_CD_CACHE_REFERENCE_VARIABLE + 1;

   // OFFSETS for CacheInvalidate
   public static final int OFFSET_CI_LOG_VARIABLE = 1;

   public static final int OFFSET_CI_CACHE_MANAGER_INSTANCE_VARIABLE = OFFSET_CD_LOG_VARIABLE + 1;

   public static final int OFFSET_CI_GENERATED_KEY_VARIABLE = OFFSET_CD_VALUE_OBJECT_VARIABLE + 1;

   public static final int OFFSET_CI_EXCEPTION_VARIABLE = OFFSET_CD_VALUE_OBJECT_VARIABLE + 1;

   public static final int OFFSET_CI_EEXCEPTION_VARIABLE = OFFSET_CD_VALUE_OBJECT_VARIABLE + 1;

   public static final int OFFSET_CI_CACHE_REFERENCE_VARIABLE = OFFSET_CD_GENERATED_KEY_VARIABLE + 1;

   private final int size;


   /**
    * Class constructor
    *
    * @param types Array containing the Type of each argument in the method
    */
   public LocalStackUtil(final Type[] types) {

      this.size = getParametersStackSize(types);
   }


   /**
    * @param types Array containing the Type of each argument in the method
    * @return combined size of the slots for the local variables for all the argument types
    */
   public final int getParametersStackSize(final Type[] types) {

      int tmpSize = 0;
      if (types != null) {
         for (final Type t : types) {
            tmpSize += t.getSize();
         }
      }
      return tmpSize;
   }


   /**
    * Returns Offset for the LOG variable in the generated method
    *
    * @return Offset of the LOG local variable
    */
   public int getLogLocalStackPos() {

      return size + OFFSET_CD_LOG_VARIABLE;
   }


   /**
    * Returns Offset for the CacheManager inst variable in the generated method
    *
    * @return Offset of the CacheManager inst local variable
    */
   public int getCacheManagerLocalStackPos() {

      return size + OFFSET_CD_CACHE_MANAGER_INSTANCE_VARIABLE;
   }


   /**
    * Returns Offset for the Object val variable in the generated method
    *
    * @return Offset of the Object val local variable
    */
   public int getValObjLocalStackPos() {

      return size + OFFSET_CD_VALUE_OBJECT_VARIABLE;
   }


   /**
    * Returns Offset for the GeneratedKey key variable in the generated method
    *
    * @return Offset of the GeneratedKey key local variable
    */
   public int getKeyGenLocalStackPos() {

      return size + OFFSET_CD_GENERATED_KEY_VARIABLE;
   }


   /**
    * Returns Offset for the Cache cache variable in the generated method
    *
    * @return Offset of the Cache cache local variable
    */
   public int getCacheRefLocalStackPos() {

      return size + OFFSET_CD_CACHE_REFERENCE_VARIABLE;
   }


   /**
    * Returns Offset for the Exception e variable in the generated method
    *
    * @return Offset of the Exception e local variable
    */
   public int getExceptionLocalStackPos() {

      return size + OFFSET_CD_EXCEPTION_VARIABLE;
   }

   //////////////////// CacheInvalidate Part


   /**
    * Returns Offset for the LOG variable in the generated method
    *
    * @return Offset of the LOG local variable
    */
   public int getCILogLocalStackPos() {

      return size + OFFSET_CI_LOG_VARIABLE;
   }


   /**
    * Returns Offset for the CacheManager inst variable in the generated method
    *
    * @return Offset of the CacheManager inst local variable
    */
   public int getCICacheManagerLocalStackPos() {

      return size + OFFSET_CI_CACHE_MANAGER_INSTANCE_VARIABLE;
   }


   /**
    * Returns Offset for the GeneratedKey key variable in the generated method
    *
    * @return Offset of the GeneratedKey key local variable
    */
   public int getCIKeyGenLocalStackPos() {

      return size + OFFSET_CI_GENERATED_KEY_VARIABLE;
   }


   /**
    * Returns Offset for the Cache cache variable in the generated method
    *
    * @return Offset of the Cache cache local variable
    */
   public int getCICacheRefLocalStackPos() {

      return size + OFFSET_CI_CACHE_REFERENCE_VARIABLE;
   }


   /**
    * Returns Offset for the Exception e variable in the generated method
    *
    * @return Offset of the Exception e local variable
    */
   public int getCIExceptionLocalStackPos() {

      return size + OFFSET_CI_EXCEPTION_VARIABLE;
   }


   /**
    * Returns Offset for the Exception e variable in the generated method
    *
    * @return Offset of the Exception e local variable
    */
   public int getCIEExceptionLocalStackPos() {

      return size + OFFSET_CI_EEXCEPTION_VARIABLE;
   }

}
