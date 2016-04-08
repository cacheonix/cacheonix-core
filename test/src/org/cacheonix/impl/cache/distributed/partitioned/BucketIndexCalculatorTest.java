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
package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketIndexCalculatorTest
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 22, 2009 4:35:20 PM
 */
public final class BucketIndexCalculatorTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketIndexCalculatorTest.class); // NOPMD

   private BucketIndexCalculator calculator;


   public void testCalculateBucketIndex() {

      for (int i = 0; i <= 10000; i++) {
         final int bucketIndex = calculator.calculateBucketIndex(Integer.valueOf(i));
         assertTrue("bucketIndex should be less then bucket count: " + bucketIndex,
                 bucketIndex >= 0 && bucketIndex < ConfigurationConstants.BUCKET_COUNT);
      }
   }


   public void testToString() {

      assertNotNull(calculator.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();
      calculator = new BucketIndexCalculator(ConfigurationConstants.BUCKET_COUNT);
   }


   public String toString() {

      return "BucketIndexCalculatorTest{" +
              "calculator=" + calculator +
              "} " + super.toString();
   }
}
