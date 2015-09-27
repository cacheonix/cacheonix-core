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
package org.cacheonix.impl.util.logging.lf5.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.cacheonix.impl.util.logging.lf5.LogLevel;
import org.cacheonix.impl.util.logging.lf5.LogRecord;

/**
 * <p>A LogRecord to be used with the LogMonitorAdapter</p>
 *
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public final class AdapterLogRecord extends LogRecord {

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------
   private static final long serialVersionUID = 0L;

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------
   @SuppressWarnings("StaticNonFinalField")
   private static LogLevel severeLevel = null;

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   public final void setCategory(final String category) {

      super.setCategory(category);
      super.setLocation(getLocationInfo(category));
   }


   public boolean isSevereLevel() {

      if (severeLevel == null) {
         return false;
      }
      return severeLevel.equals(getLevel());
   }


   public static void setSevereLevel(final LogLevel level) {

      severeLevel = level;
   }


   public static LogLevel getSevereLevel() {

      return severeLevel;
   }


   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   @SuppressWarnings("ThrowableInstanceNeverThrown")
   final String getLocationInfo(final String category) {

      final String stackTrace = stackTraceToString(new Throwable());
      return parseLine(stackTrace, category);
   }


   final String stackTraceToString(final Throwable t) {

      final StringWriter sw = new StringWriter(100);
      final PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      pw.flush();
      return sw.toString();
   }


   final String parseLine(String trace, final String category) {

      final int index = trace.indexOf(category);
      if (index == -1) {
         return null;
      }
      trace = trace.substring(index);
      trace = trace.substring(0, trace.indexOf(')') + 1);
      return trace;
   }
   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces
   //--------------------------------------------------------------------------
}

