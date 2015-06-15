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

package org.cacheonix.impl.util.logging.helpers;

import org.cacheonix.impl.util.logging.Appender;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.spi.ErrorHandler;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * The <code>OnlyOnceErrorHandler</code> implements log4j's default error handling policy which consists of emitting a
 * message for the first error in an appender and ignoring all following errors.
 * <p/>
 * <p>The error message is printed on <code>System.err</code>.
 * <p/>
 * <p>This policy aims at protecting an otherwise working application from being flooded with error messages when
 * logging fails.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 0.9.0
 */
public final class OnlyOnceErrorHandler implements ErrorHandler {


   static final String WARN_PREFIX = "log4j warning: ";
   static final String ERROR_PREFIX = "log4j error: ";

   boolean firstTime = true;


   /**
    * Does not do anything.
    */
   public void setLogger(final Logger logger) {
   }


   /**
    * No options to activate.
    */
   public void activateOptions() {
   }


   /**
    * Prints the message and the stack trace of the exception on <code>System.err</code>.
    */
   public void error(final String message, final Exception e, final int errorCode) {
      error(message, e, errorCode, null);
   }


   /**
    * Prints the message and the stack trace of the exception on <code>System.err</code>.
    */
   public final void error(final String message, final Exception e, final int errorCode,
                           final LoggingEvent event) {
      if (firstTime) {
         LogLog.error(message, e);
         firstTime = false;
      }
   }


   /**
    * Print a the error message passed as parameter on <code>System.err</code>.
    */
   public void error(final String message) {
      if (firstTime) {
         LogLog.error(message);
         firstTime = false;
      }
   }


   /**
    * Does not do anything.
    */
   public void setAppender(final Appender appender) {
   }


   /**
    * Does not do anything.
    */
   public void setBackupAppender(final Appender appender) {
   }
}
