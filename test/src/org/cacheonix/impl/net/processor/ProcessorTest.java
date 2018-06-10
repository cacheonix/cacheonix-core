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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Tester for Processor
 */
public final class ProcessorTest extends TestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ProcessorTest.class); // NOPMD

   private static final String TEST_PROCESSOR = "TestProcessor";

   private Processor processor;


   public void testEnqueue() throws Exception {

      // Enqueue
      final CountDownLatch latch = new CountDownLatch(1);
      processor.enqueue(new Command() {

         public void execute() {

            latch.countDown();
         }
      });

      // Await for command execution
      assertTrue(latch.await(50L, TimeUnit.MILLISECONDS));
   }


   public void testStartup() {

      // Assert
      assertTrue(processor.isAlive());
   }


   public void testShutdown() {

      // Shutdown
      processor.shutdown();

      // Wait and assert
      assertTrue(processor.isShutdown());
   }


   public void testIsProcessorThread() {

      assertFalse(processor.isProcessorThread());
   }


   public void testWaitToShutdown() throws Exception {

      // Enqueue command to shutdown
      processor.enqueue(new Command() {

         public void execute() {

            processor.shutdown();
         }
      });

      // Await for command execution
      assertTrue(processor.waitForShutdown(50L));
   }


   public void testToString() {

      assertNotNull(processor.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      // Create the processor
      processor = new SimpleProcessor(TEST_PROCESSOR);

      // Startup
      processor.startup();
      while (!processor.isAlive()) {
         Thread.sleep(1L);
      }
   }
}
