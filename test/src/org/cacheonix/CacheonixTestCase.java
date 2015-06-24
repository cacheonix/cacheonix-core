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
package org.cacheonix;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.ClockImpl;
import org.cacheonix.impl.util.MutableBoolean;
import org.cacheonix.impl.util.exception.ExceptionUtils;

/**
 * @noinspection JUnitTestCaseWithNoTests, JUnitTestClassNamingConvention
 */
public abstract class CacheonixTestCase extends TestCase {

   public static final String TEST_KEY_PREFIX = "test_key";

   protected static final String TEST_OBJECT_PREFIX = "test_object";

   private Timer timer;

   private Clock clock;

   /**
    * A single-threaded Executor used to run event notification outside of main processing loop.
    */
   private ExecutorService eventNotificationExecutor = null;


   /**
    * No-arg constructor to enable serialization. This method is not intended to be used by mere mortals without calling
    * setName().
    */
   protected CacheonixTestCase() {

   }


   /**
    * Constructs a test case with the given name.
    *
    * @param name cache name.
    */
   protected CacheonixTestCase(final String name) {

      super(name);
   }


   public static void assertZero(final int value) {

      assertEquals(null, 0, value);
   }


   public static void assertNotZero(final int value) {

      assertTrue(value != 0);
   }


   protected static String createTestKey(final long i) {

      return TEST_KEY_PREFIX + i;
   }


   protected static String createTestObject(final long i) {

      return TEST_OBJECT_PREFIX + i;
   }


   protected static String createTestKey() {

      return TEST_KEY_PREFIX;
   }


   protected static String createTestObject() {

      return TEST_OBJECT_PREFIX;
   }


   /**
    * @param array1 array 1
    * @param array2 array 2
    * @noinspection MethodOverridesStaticMethodOfSuperclass
    */
   public static void assertEquals(final Object[] array1, final Object[] array2) {

      if (Arrays.equals(array1, array2)) {
         return;
      }
      if (array1 == null || array2 == null) {
         return;
      }

      final int length = array1.length;
      if (array2.length != length) {
         throw new AssertionFailedError("Arrays not equal. length 1 is " + array1.length + ", length 2 is " + array2.length);
      }

      for (int i = 0; i < length; i++) {
         final Object o1 = array1[i];
         final Object o2 = array2[i];
         if (!(o1 == null ? o2 == null : o1.equals(o2))) {
            throw new AssertionFailedError("Arrays not equal. value 1 [" + i + "] " + o1 + ", value 1 [" + i + "] " + array2.length);
         }
      }

   }


   @SuppressWarnings("SameParameterValue")
   protected static void assertEmpty(final String message, final Collection collection) {

      if (!collection.isEmpty()) {
         fail(message);
      }
   }


   protected static Binary toBinary(final Object obj) throws IllegalStateException {

      try {
         return TestUtils.toBinary(obj);
      } catch (final InvalidObjectException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   /**
    * @param array1 array 1
    * @param array2 array 2
    * @noinspection MethodOverridesStaticMethodOfSuperclass
    */
   protected static void assertEquals(final byte[] array1, final byte[] array2) {

      if (!Arrays.equals(array1, array2)) {

         throw new AssertionFailedError("Arrays are not equal");
      }
   }


   /**
    * @param array1 array 1
    * @param array2 array 2
    * @noinspection MethodOverridesStaticMethodOfSuperclass
    */
   protected static void assertEquals(final int[] array1, final int[] array2) {

      if (!Arrays.equals(array1, array2)) {

         throw new AssertionFailedError("Arrays are not equal");
      }
   }


   public static void assertEquals(final InetAddress[] arrayOne, final InetAddress[] arrayTwo) {

      if (!Arrays.equals(arrayOne, arrayTwo)) {

         throw new AssertionFailedError("Arrays are not equal");
      }
   }


   @SuppressWarnings("SameParameterValue")
   public static void assertEquals(final InetAddress addressOne, final InetAddress addressTwo) {

      if (!addressOne.equals(addressTwo)) {
         throw new AssertionFailedError("Addresses are not equal");
      }
   }


   protected static void assertTrue(final MutableBoolean mutableBoolean) {

      assertTrue(mutableBoolean.get());
   }


   protected static void assertFalse(final MutableBoolean mutableBoolean) {

      assertFalse(mutableBoolean.get());
   }


   /**
    * Returns the test clock.
    *
    * @return the test clock.
    */
   protected final Clock getClock() {

      return clock;
   }


   protected final Timer getTimer() {

      return timer;
   }


   public Executor getEventNotificationExecutor() {

      return eventNotificationExecutor;
   }


   protected void setUp() throws Exception {

      super.setUp();

      timer = new Timer("TestTimer", false);
      clock = new ClockImpl(1L).attachTo(timer);

      // Set up executor
      eventNotificationExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
   }


   protected void tearDown() throws Exception {

      if (eventNotificationExecutor != null) {

         eventNotificationExecutor.shutdownNow();
      }

      if (timer != null) {

         timer.cancel();
      }


      timer = null;
      clock = null;

      super.tearDown();
   }
}
