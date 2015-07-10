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
package org.cacheonix.impl.net.cluster;

import org.cacheonix.TestUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * TimedMulticastMessageSender
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 5, 2008 10:52:47 PM
 */
final class TimedMulticastMessageSender implements Runnable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TimedMulticastMessageSender.class); // NOPMD

   private final long timeoutMillis;

   private final int senderID;

   private int messagesSent = 0;

   private final ClusterProcessor clusterProcessor;


   public TimedMulticastMessageSender(final ClusterProcessor connection, final int senderID,
                                      final int timeoutSecs) {

      this.timeoutMillis = (long) (timeoutSecs * 1000);
      this.senderID = senderID;
      this.clusterProcessor = connection;
   }


   public void run() {

      try {

         final long endTime = System.currentTimeMillis() + timeoutMillis;
         while (System.currentTimeMillis() < endTime) {

            clusterProcessor.post(new TestMessage(senderID, messagesSent, TestUtils.makeTestObject(0)));

            messagesSent++;
         }
      } catch (final Exception e) {

         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public String toString() {

      return "TimedMulticastMessageSender{" +
              "clusterProcessor=" + clusterProcessor +
              ", senderID=" + senderID +
              ", messagesSent=" + messagesSent +
              ", timeoutMillis=" + timeoutMillis +
              '}';
   }
}
