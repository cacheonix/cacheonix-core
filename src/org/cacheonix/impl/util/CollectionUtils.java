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
package org.cacheonix.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Utility methods for working with collections.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 6, 2008 6:50:50 PM
 */
public final class CollectionUtils {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CollectionUtils.class); // NOPMD


   /**
    * Utility class constructor.
    */
   private CollectionUtils() {

   }


   /**
    * Returns <code>true</code> if both lists are the same?
    *
    * @param list1
    * @param list2
    * @return
    */
   @SuppressWarnings("ObjectEquality")
   public static boolean same(final List list1, final List list2) {

      if (list1 == null || list2 == null) {
         return false;
      }
      if (list1.size() != list2.size()) {
         return false;
      }
      if (list1 == list2) {
         return true;
      }
      if (list1.equals(list2)) {
         return true;
      }
      for (int i = 0; i < list1.size(); i++) {
         if (!list1.get(i).equals(list2.get(i))) {
            return false;
         }
      }
      return true;
   }


   /**
    * Returns <code>true</code> if both lists are the same?
    *
    * @param set1
    * @param set2
    * @return
    */
   @SuppressWarnings("ObjectEquality")
   public static boolean same(final Set set1, final Set set2) {

      if (set1 == null || set2 == null) {
         return false;
      }
      if (set1.size() != set2.size()) {
         return false;
      }
      if (set1 == set2) {
         return true;
      }
      final Iterator iter1 = set1.iterator();
      final Iterator iter2 = set2.iterator();
      while (iter1.hasNext()) {
         final Object o1 = iter1.next();
         final Object o2 = iter2.next();
         if (!o1.equals(o2)) {
            return false;
         }
      }
      return true;
   }


   @SuppressWarnings("UseOfPropertiesAsHashtable")
   public static Properties copyProperties(final Properties source) {

      final Properties copy = new Properties();
      if (source.isEmpty()) {

         return copy;
      }

      // 
      copy.putAll(source);
      return copy;
   }


   /**
    * Creates a list with one object.
    *
    * @param object to add to the list
    * @return list with one object.
    */
   public static <T> List<T> createList(final T object) {

      final List<T> result = new ArrayList<T>(1);
      result.add(object);
      return result;
   }


   /**
    * Creates a list with one object.
    *
    * @param i to add to the list
    * @return list with one object.
    */
   public static IntArrayList createIntArrayList(final int i) {

      final IntArrayList list = new IntArrayList(1);
      list.add(i);
      return list;
   }


   /**
    * Returns <code>true</code> if a map is null or empty.
    *
    * @param map the map to check for emptiness.
    * @return <code>true</code> if the map is null or empty.
    */
   public static boolean isEmpty(final Map map) {

      return !(map != null && !map.isEmpty());
   }


   public static boolean isEmpty(final Collection collection) {

      return collection == null || collection.isEmpty();
   }


   public static boolean isEmpty(final IntArrayList collection) {

      return collection == null || collection.isEmpty();
   }


   public static IntArrayList createIntArrayList(final Collection<Integer> collection) {

      //
      if (isEmpty(collection)) {

         return new IntArrayList(0);
      }

      //
      final IntArrayList result = new IntArrayList(collection.size());
      for (final Integer i : collection) {

         result.add(i);
      }

      return result;
   }


   public static IntHashSet toIntHashSet(final IntArrayList list) {

      if (isEmpty(list)) {

         return new IntHashSet(0);
      }

      final IntHashSet result = new IntHashSet();
      result.addAll(list.toNativeArray());
      return result;
   }


   public static <T> List<T> copy(final List<T> listToCopy) {

      return listToCopy == null ? null : new ArrayList<T>(listToCopy);

   }
}
