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

/**
 * Messages implementing  Prepareable will be placed into their processor's execution queue and prepare() will be called
 * before calling message's execute().
 */
public interface Prepareable {

   /**
    * This callback method is executed when the processor pulls out this message from its queue and is about to execute
    * it. It's called after the context processor was set, the waiter was created and registered, but before the message
    * is executed.
    *
    * @return <code>PrepareResult.EXECUTE</code> if this message should be placed into the processor's queue.
    * @see PrepareResult#EXECUTE
    * @see PrepareResult#BREAK
    * @see PrepareResult#ROUTE
    */
   PrepareResult prepare();

   /**
    * Marks a message a prepared. The message should rise an internal non-transient wireable flag.
    *
    * @see #prepare()
    */
   void markPrepared();

   /**
    * Returns true if markPrepared() has been called.
    *
    * @return true if markPrepared() has been called.
    * @see #markPrepared()
    */
   boolean isPrepared();
}
