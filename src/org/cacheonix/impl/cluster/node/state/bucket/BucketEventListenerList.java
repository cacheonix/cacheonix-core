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
package org.cacheonix.impl.cluster.node.state.bucket;

import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketEventSubscriberList
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Nov 6, 2009 5:46:51 PM
 */
public final class BucketEventListenerList {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketEventListenerList.class); // NOPMD

   private final List<BucketEventListener> listeners = new ArrayList<BucketEventListener>(1);


   public synchronized int size() {

      return listeners.size();
   }


   public synchronized void add(final BucketEventListener listener) {

      listeners.add(listener);
   }


   public synchronized void execute(final BeginBucketTransferCommand command) {

      for (final BucketEventListener listener : listeners) {
         listener.execute(command);
      }
   }


   public synchronized void execute(final FinishBucketTransferCommand command) {

      for (final BucketEventListener listener : listeners) {
         listener.execute(command);
      }
   }


   public synchronized void execute(final CancelBucketTransferCommand command) {

      for (final BucketEventListener listener : listeners) {
         listener.execute(command);
      }

   }


   public synchronized void execute(final RestoreBucketCommand command) {

      for (final BucketEventListener listener : listeners) {
         listener.execute(command);
      }
   }


   public void execute(final OrphanBucketCommand command) {

      for (final BucketEventListener listener : listeners) {

         listener.execute(command);
      }
   }


   public void execute(final AssignBucketCommand command) {

      for (final BucketEventListener listener : listeners) {

         listener.execute(command);
      }
   }


   public String toString() {

      return "BucketEventListenerList{" +
              "listeners=" + listeners +
              '}';
   }
}
