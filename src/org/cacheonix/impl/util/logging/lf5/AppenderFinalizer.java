/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.util.logging.lf5;

import org.cacheonix.impl.util.logging.lf5.viewer.LogBrokerMonitor;

/**
 * <code>AppenderFinalizer</code> has a single method that will finalize resources associated with a
 * <code>LogBrokerMonitor</code> in the event that the <code>LF5Appender</code> class is destroyed, and the class loader
 * is garbage collected.
 *
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public final class AppenderFinalizer {
   //--------------------------------------------------------------------------
   // Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   // Protected Variables:
   //--------------------------------------------------------------------------

   private LogBrokerMonitor _defaultMonitor = null;

   //--------------------------------------------------------------------------
   // Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   // Constructors:
   //--------------------------------------------------------------------------


   public AppenderFinalizer(final LogBrokerMonitor defaultMonitor) {
      _defaultMonitor = defaultMonitor;
   }
   //--------------------------------------------------------------------------
   // Public Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   // Protected Methods:
   //--------------------------------------------------------------------------


   /**
    * @throws Throwable
    */
   protected final void finalize() throws Throwable {
      System.out.println("Disposing of the default LogBrokerMonitor instance");
      _defaultMonitor.dispose();
      super.finalize();
   }

   //--------------------------------------------------------------------------
   // Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   // Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}