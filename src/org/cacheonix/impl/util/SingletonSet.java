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
package org.cacheonix.impl.util;


import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SingletonSet<E> extends AbstractSet<E> implements Serializable {

   // use serialVersionUID from JDK 1.2.2 for interoperability
   private static final long serialVersionUID = 3193687207550431679L;

   private final E element;


   public SingletonSet(final E o) {

      element = o;
   }


   public Iterator<E> iterator() {

      return new Iterator<E>() {

         private boolean hasNext = true;


         public boolean hasNext() {

            return hasNext;
         }


         public E next() {

            if (hasNext) {
               hasNext = false;
               return element;
            }
            throw new NoSuchElementException();
         }


         public void remove() {

            throw new UnsupportedOperationException();
         }
      };
   }


   public int size() {

      return 1;
   }


   public E getElement() {

      return element;
   }


   public boolean contains(final Object o) {

      return !(o == null || element == null) && element.equals(o);

   }
}

