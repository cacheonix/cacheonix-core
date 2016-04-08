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

import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * AbstractThreadFactory
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Jan 12, 2009 8:39:24 PM
 */
abstract class AbstractThreadFactory implements ThreadFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AbstractThreadFactory.class); // NOPMD

   /**
    * @noinspection AnalyzingVariableNaming
    */
   private static final Map<String, ThreadNameCounter> THREAD_NAME_COUNTERS = new HashMap<String, ThreadNameCounter>(11);

   private final String name;


   AbstractThreadFactory(final String name) {

      this.name = name;
   }


   public abstract Thread newThread(Runnable r);


   final String createNextName() {

      final ThreadNameCounter counter = getCounter();

      // Increment thread number
      final int threadNumber = counter.increment();

      // Create name
      return name + (SystemProperty.CACHEONIX_SHOW_THREAD_NUMBER ? ':' + Integer.toString(threadNumber) : "");
   }


   @SuppressWarnings("SynchronizationOnStaticField")
   private ThreadNameCounter getCounter() {

      ThreadNameCounter counter;
      synchronized (THREAD_NAME_COUNTERS) {

         counter = THREAD_NAME_COUNTERS.get(name);
         if (counter == null) {

            counter = new ThreadNameCounter();
            THREAD_NAME_COUNTERS.put(name, counter);
         }
      }
      return counter;
   }


   final String getName() {

      return name;
   }


   public String toString() {

      return "AbstractThreadFactory{" +
              "name='" + name + '\'' +
              '}';
   }
}
