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
package org.cacheonix.impl.util.cache;

/**
 * Object size calculator.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface ObjectSizeCalculator {


   /**
    * Calculates object size.
    *
    * @param obj object
    * @return object size
    */
   long sizeOf(final Object obj);


   /**
    * Sums the given values and pads to the nearest 8 bytes.
    *
    * @param value1 a value number one.
    * @param value2 a value number two.
    * @param value3 a value number tree.
    * @return a sum of three values padded to the nearest 8 bytes.
    */
   long sum(long value1, long value2, final long value3);
}
