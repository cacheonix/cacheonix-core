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
package org.cacheonix.impl.cache.store;

import junit.framework.TestCase;

/**
 * Tester for SharedCounter.
 */
public class SharedCounterTest extends TestCase {


   private static final long MAX_VALUE = 1000L;

   private SharedCounter counter;


   public void testIncrement() {

      counter.increment();
      counter.increment();

      assertEquals(3, counter.increment());
      assertEquals(3, counter.value());
   }


   public void testDecrement() {

      counter.increment();
      counter.increment();
      counter.increment();

      assertEquals(2, counter.decrement());
      assertEquals(2, counter.value());
   }


   public void testSubtract() {

      counter.add(10L);
      assertEquals(9L, counter.subtract(1L));
   }


   public void testAdd() {

      counter.increment();
      counter.increment();

      assertEquals(7, counter.add(5));
      assertEquals(7, counter.value());
   }


   public void setUp() throws Exception {

      super.setUp();

      counter = new SharedCounter(MAX_VALUE);
   }


   public void tearDown() throws Exception {

      counter = null;

      super.tearDown();
   }
}
