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
package org.cacheonix.impl.cache.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.cacheonix.impl.util.logging.Logger;

/**
 * A standard implementation of the ObjectSizeCalculator.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jul 26, 2008 6:36:52 PM
 */
public final class StandardObjectSizeCalculator implements ObjectSizeCalculator {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(StandardObjectSizeCalculator.class); // NOPMD

   /**
    * A map of pre-calculated fixed sizes.
    */
   private static final Map<Class, Integer> FIXED_SIZES = makeFixedSizesMap();

   public static final int SIZE_OBJECT_REF = getDataModelBits() / 8;

   public static final int SIZE_OBJECT = SIZE_OBJECT_REF + 4;

   public static final int SIZE_BOOLEAN = SIZE_OBJECT + 1;

   public static final int SIZE_BYTE = SIZE_OBJECT + 1;

   public static final int SIZE_SHORT = SIZE_OBJECT + 2;

   public static final int SIZE_CHARACTER = SIZE_OBJECT + 2;

   public static final int SIZE_INTEGER = SIZE_OBJECT + 4;

   public static final int SIZE_FLOAT = SIZE_OBJECT + 4;

   public static final int SIZE_LONG = SIZE_OBJECT + 8;

   public static final int SIZE_DOUBLE = SIZE_OBJECT + 8;

   public static final int SIZE_STRING = calculateShallowSize(String.class) + 16;

   public static final String SUN_ARCH_DATA_MODEL = "sun.arch.data.model";

   public static final int SIZE_CLASS = 8;

   private final DefinedFieldCache fieldCache = new DefinedFieldCache();


   /**
    * {@inheritDoc}
    */
   public final long sum(final long value1, final long value2, final long value3) {

      return pad(value1 + value2 + value3, 8L);
   }


   public static final long pad(final long lMin, final long lMultiple) {

      return (lMin + lMultiple - 1L) / lMultiple * lMultiple;
   }


   private static Map<Class, Integer> makeFixedSizesMap() {

      final Map<Class, Integer> map = new IdentityHashMap<Class, Integer>(17);
      map.put(Boolean.TYPE, Integer.valueOf(1));
      map.put(Byte.TYPE, Integer.valueOf(1));
      map.put(Short.TYPE, Integer.valueOf(2));
      map.put(Character.TYPE, Integer.valueOf(2));
      map.put(Integer.TYPE, Integer.valueOf(4));
      map.put(Float.TYPE, Integer.valueOf(4));
      map.put(Long.TYPE, Integer.valueOf(8));
      map.put(Double.TYPE, Integer.valueOf(8));
      map.put(Object.class, Integer.valueOf(SIZE_OBJECT));
      map.put(Boolean.class, Integer.valueOf(SIZE_BOOLEAN));
      map.put(Byte.class, Integer.valueOf(SIZE_BYTE));
      map.put(Short.class, Integer.valueOf(SIZE_SHORT));
      map.put(Character.class, Integer.valueOf(SIZE_CHARACTER));
      map.put(Integer.class, Integer.valueOf(SIZE_INTEGER));
      map.put(Float.class, Integer.valueOf(SIZE_FLOAT));
      map.put(Long.class, Integer.valueOf(SIZE_LONG));
      map.put(Double.class, Integer.valueOf(SIZE_DOUBLE));
      return map;
   }


   /**
    * Get data model bit size (32/64)
    *
    * @return data model bit size (32/64)
    */
   private static int getDataModelBits() {

      try {
         return Integer.parseInt(System.getProperty(SUN_ARCH_DATA_MODEL));
      } catch (final Exception ignored) {
         return 32;
      }
   }


   private static int calculateShallowSize(final Class clz) {

      Integer size = FIXED_SIZES.get(clz);
      if (size != null) {

         return size;
      }

      int byteCount = SIZE_OBJECT;
      Class clazz = clz;
      try {

         do {

            final Field[] fields = clazz.getDeclaredFields();
            for (final Field field : fields) {

               if (Modifier.isStatic(field.getModifiers())) {

                  continue;
               }

               size = FIXED_SIZES.get(field.getType());
               if (size != null) {

                  byteCount += size;
               }
            }

            clazz = clazz.getSuperclass();
         } while (clazz != null);
      } catch (final SecurityException e) {

         throw new RuntimeException("Error calculating size of the class: " + clazz, e);
      }
      return byteCount;
   }


   /**
    * {@inheritDoc}
    */
   public long sizeOf(final Object obj) {


      final Map<Object, Object> visited = new IdentityHashMap<Object, Object>(11);

      final LinkedList<Object> stack = new LinkedList<Object>();


      long result = calculate(obj, visited, stack);
      while (!stack.isEmpty()) {

         final Object pop = stack.removeFirst();
         result += calculate(pop, visited, stack);
      }
      visited.clear();
      return result;
   }


   private static boolean skipObject(final Object obj, final Map<Object, Object> visited) {

      if (obj instanceof String) {

         // this will not cause a memory leak since
         // unused interned Strings will be thrown away
         if (obj == ((String) obj).intern()) {
            return true;
         }
      }
      return obj == null || visited.containsKey(obj);
   }


   private long calculate(final Object obj, final Map<Object, Object> visited, final LinkedList<Object> stack) {

      if (skipObject(obj, visited)) {
         return 0;
      }

      visited.put(obj, null);
      Class clazz = obj.getClass();

      if (clazz.isArray()) {

         return sizeOfArray(obj, visited, stack);
      }

      long result = 0;
      while (clazz != null) {

         final Field[] fields = getCachedDeclaredFields(clazz);
         for (final Field field : fields) {

            if (!Modifier.isStatic(field.getModifiers())) {

               if (field.getType().isPrimitive()) {

                  result += FIXED_SIZES.get(field.getType());
               } else {

                  result += SIZE_OBJECT_REF;

                  field.setAccessible(true);
                  try {

                     final Object toBeDone = field.get(obj);
                     if (toBeDone != null) {

                        stack.add(toBeDone);
                     }
                  } catch (final IllegalAccessException ignored) {

                     assert false;
                  }
               }
            }
         }
         clazz = clazz.getSuperclass();
      }

      result += SIZE_CLASS;

      return roundUpToNearestEightBytes(result);
   }


   protected long sizeOfArray(final Object obj, final Map<Object, Object> visited, final LinkedList<Object> stack) {

      long result = 16;
      final int length = Array.getLength(obj);
      if (length != 0) {

         final Class arrayElementClazz = obj.getClass().getComponentType();
         if (arrayElementClazz.isPrimitive()) {

            result += length * FIXED_SIZES.get(arrayElementClazz);
         } else {

            for (int i = 0; i < length; i++) {

               result += SIZE_OBJECT_REF + calculate(Array.get(obj, i), visited, stack);
            }
         }
      }
      return result;
   }


   private static long roundUpToNearestEightBytes(long result) {

      if (result % 8 != 0) {

         result += 8 - result % 8;
      }
      return result;
   }


   private Field[] getCachedDeclaredFields(final Class clazz) {

      final String clazzName = clazz.getName();
      final Field[] existingFields = fieldCache.get(clazzName);
      if (existingFields == null) {

         final Field[] newFields = clazz.getDeclaredFields();
         fieldCache.put(clazzName, newFields);
         return newFields;
      } else {

         return existingFields;
      }
   }
}
