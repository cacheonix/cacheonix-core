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
package org.cacheonix.impl.util.thread;

import junit.framework.TestCase;

/**
 * Tests {@link DaemonThreadFactory}
 */
public final class DaemonThreadFactoryTest extends TestCase {

   private static final String TEST_NAME = "TestName";


   private DaemonThreadFactory threadFactory;


   public void test() {

      final Thread thread = threadFactory.newThread(new Runnable() {

         public void run() {

         }
      });
      assertNotNull(thread);
      assertTrue(thread.getName().contains(TEST_NAME));
      assertTrue(thread.isDaemon());
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      threadFactory = new DaemonThreadFactory(TEST_NAME);
   }


   public String toString() {

      return "DaemonThreadFactoryTest{" +
              "threadFactory=" + threadFactory +
              '}';
   }
}
