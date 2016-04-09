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

import org.cacheonix.impl.clock.Time;

/**
 * A prefetch status maintainer used both to schedule prefetch and to cancel it in mid-air.
 */
public interface PrefetchCommand extends Runnable {

   /**
    * Sets the current prefetch stage.
    *
    * @param currentStage the stage.
    */
   void setCurrentStage(PrefetchStage currentStage);

   /**
    * Sets an opaque stage context. Normally is used by the stage to store information needed for cancelling the
    * object.
    *
    * @param stageContext the stage context.
    */
   void setStageContext(Object stageContext);

   /**
    * Returns the time the prefetch should be launched.
    *
    * @return the time the prefetch should be launched.
    */
   Time getPrefetchTime();

   /**
    * Cancels the prefetch. This <strong>method</strong> will be called concurrently by the CacheProcessor.
    */
   void cancelPrefetch();

   /**
    * The action to be performed by the prefetch command when it is executed by a stage.
    */
   void run();

   /**
    * Returns the opaque stage context. Normally is used by the stage to store information needed for cancelling the
    * object.
    *
    * @return the opaque stage context. Normally is used by the stage to store information needed for cancelling the
    *         object.
    */
   Object getStageContext();
}
