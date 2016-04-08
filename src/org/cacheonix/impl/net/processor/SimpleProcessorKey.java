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
package org.cacheonix.impl.net.processor;

/**
 * A simple processor key that IDs a processor by a destination type.
 */
@SuppressWarnings("RedundantIfStatement")
public class SimpleProcessorKey implements ProcessorKey {

   private final int destinationType;


   public SimpleProcessorKey(final int destinationType) {

      this.destinationType = destinationType;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final SimpleProcessorKey that = (SimpleProcessorKey) o;

      if (destinationType != that.destinationType) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return destinationType;
   }


   public String toString() {

      return Integer.toString(destinationType);
   }
}
