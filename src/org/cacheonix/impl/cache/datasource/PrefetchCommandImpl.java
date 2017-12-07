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
package org.cacheonix.impl.cache.datasource;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A prefetch status maintainer used both to schedule prefetch and to cancel it in mid-air.
 */
public final class PrefetchCommandImpl implements PrefetchCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(PrefetchCommandImpl.class); // NOPMD

   /**
    * Indicates that the command is in scheduler.
    */
   private static final int IN_SCHEDULER = 0;

   /**
    * Indicates that the command is
    */
   private static final int IN_EXECUTOR = 1;

   /**
    * A data source to use to read the data when prefetching.
    */
   private BinaryStoreDataSource binaryStoreDataSource;

   /**
    * An updated of an element.
    */
   private PrefetchElementUpdater prefetchElementUpdater;

   /**
    * Current prefetch stage.
    */
   private PrefetchStage currentStage = null;

   /**
    * A flag indicating that the prefetch was closed early.
    */
   private final AtomicBoolean closed = new AtomicBoolean(false);

   /**
    * An update counter.
    */
   private final long expectedElementUpdateCounter;

   /**
    * A key to prefetch.
    */
   private Binary key;

   /**
    * State machine.
    */
   private byte state = (byte) IN_SCHEDULER;

   /**
    * Scheduled prefetch time.
    */
   private Time prefetchTime;

   /**
    * Current stage's context.
    */
   private Object stageContext = null;


   /**
    * Creates a new prefetch command.
    *
    * @param prefetchElementUpdater       an object responsible for updating or initiating an update.
    * @param binaryStoreDataSource        a binary store data source.
    * @param key                          a key.
    * @param prefetchTime                 the time a prefetch is scheduled to run.
    * @param expectedElementUpdateCounter an update counter at the time the prefetch was initiated.
    */
   public PrefetchCommandImpl(final PrefetchElementUpdater prefetchElementUpdater,
           final BinaryStoreDataSource binaryStoreDataSource, final Binary key, final Time prefetchTime,
           final long expectedElementUpdateCounter) {

      this.expectedElementUpdateCounter = expectedElementUpdateCounter;
      this.prefetchElementUpdater = prefetchElementUpdater;
      this.binaryStoreDataSource = binaryStoreDataSource;
      this.prefetchTime = prefetchTime;
      this.key = key;
   }


   /**
    * Sets the current prefetch stage.
    *
    * @param currentStage the stage.
    */
   public void setCurrentStage(final PrefetchStage currentStage) {

      this.currentStage = currentStage;
   }


   /**
    * Sets an opaque stage context. Normally is used by the stage to store information needed for cancelling the
    * object.
    *
    * @param stageContext the stage context.
    */
   public void setStageContext(final Object stageContext) {

      this.stageContext = stageContext;
   }


   /**
    * Returns the opaque stage context. Normally is used by the stage to store information needed for cancelling the
    * object.
    *
    * @return the opaque stage context. Normally is used by the stage to store information needed for cancelling the
    *         object.
    */
   public Object getStageContext() {

      return stageContext;
   }


   /**
    * Returns the time the prefetch should be launched.
    *
    * @return the time the prefetch should be launched.
    */
   public Time getPrefetchTime() {

      return prefetchTime;
   }


   /**
    * Cancels the prefetch. This <strong>method</strong> will be called concurrently by the CacheProcessor.
    */
   public void cancelPrefetch() {

      // Don't cancel cancelled
      if (!closed.compareAndSet(false, true)) {

         return;
      }

      // Set cancelled flag
      closed.set(true);

      // Let stage thread know it is cancelled
      currentStage.cancel(this);

      //
      destroy();
   }


   /**
    * The action to be performed by the prefetch command when it is executed by a stage.
    */
   public void run() {

      // Create copies of objects that can be nullified as a result of a call to cancelPrefetch();
      final PrefetchElementUpdater prefetchElementUpdater = this.prefetchElementUpdater;
      final BinaryStoreDataSource binaryStoreDataSource = this.binaryStoreDataSource;
      final PrefetchStage currentStage = this.currentStage;
      final Binary key = this.key;

      // Don't run cancelled
      if (closed.get()) {

         // Clear all object references
         destroy();

         // Exit
         return;
      }

      switch (state) {
         case IN_SCHEDULER:

//            //noinspection ControlFlowStatementWithoutBraces
//            if (LOG.isDebugEnabled()) LOG.debug("Enqueueing prefetch"); // NOPMD

            state = IN_EXECUTOR;

            // Place self into the threadpool's queue
            this.currentStage = currentStage.nextStage();
            currentStage.schedule(this);

            // Exit
            return;
         case IN_EXECUTOR:

            // Get new value from the data source
            final BinaryStoreDataSourceObject binaryStoreDataSourceObject = binaryStoreDataSource.get(key);

            // Check if it was cancelled while reading from the data source
            if (!closed.get()) {

               if (binaryStoreDataSourceObject != null) {

                  // Update the store's element
                  final Serializable value = binaryStoreDataSourceObject.getObject();
                  final Time timeToRead = binaryStoreDataSourceObject.getTimeToRead();

//               //noinspection ControlFlowStatementWithoutBraces
//               if (LOG.isDebugEnabled()) LOG.debug("Calling prefetch updater"); // NOPMD

                  prefetchElementUpdater.updateElement(key, value, timeToRead, expectedElementUpdateCounter);
               } else {

                  // Remove element
                  prefetchElementUpdater.removeElement(key);
               }
            }

            // Mark as done
            closed.set(true);

            // Clear all object references
            destroy();

            // Exit
            return;
         default:
            LOG.error("Unknown prefetch order state");

      }
   }


   private void destroy() {

      prefetchElementUpdater = null;
      binaryStoreDataSource = null;
      prefetchTime = null;
      stageContext = null;
      currentStage = null;
      key = null;
   }


   public String toString() {

      return "PrefetchCommandImpl{" +
              "closed=" + closed +
              ", expectedElementUpdateCounter=" + expectedElementUpdateCounter +
              ", key=" + key +
              ", state=" + state +
              ", prefetchTime=" + prefetchTime +
              '}';
   }
}
