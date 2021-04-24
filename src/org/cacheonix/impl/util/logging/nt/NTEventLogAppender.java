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

package org.cacheonix.impl.util.logging.nt;

import org.cacheonix.impl.util.logging.AppenderSkeleton;
import org.cacheonix.impl.util.logging.Layout;
import org.cacheonix.impl.util.logging.TTCCLayout;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;


/**
 * Append to the NT event log system.
 * <p/>
 * <p><b>WARNING</b> This appender can only be installed and used on a Windows system.
 * <p/>
 * <p>Do not forget to place the file NTEventLogAppender.dll in a directory that is on the PATH of the Windows system.
 * Otherwise, you will get a java.lang.UnsatisfiedLinkError.
 *
 * @author <a href="mailto:cstaylor@pacbell.net">Chris Taylor</a>
 * @author <a href="mailto:jim_cakalic@na.biomerieux.com">Jim Cakalic</a>
 */
public final class NTEventLogAppender extends AppenderSkeleton {

   private int _handle = 0;

   private String source = null;

   private final String server = null;


   public NTEventLogAppender() {

      this(null, null, null);
   }


   public NTEventLogAppender(final String source) {

      this(null, source, null);
   }


   public NTEventLogAppender(final String server, final String source) {

      this(server, source, null);
   }


   public NTEventLogAppender(final Layout layout) {

      this(null, null, layout);
   }


   public NTEventLogAppender(final String source, final Layout layout) {

      this(null, source, layout);
   }


   public NTEventLogAppender(final String server, String source, final Layout layout) {

      if (source == null) {
         source = "Log4j";
      }
      if (layout == null) {
         this.layout = new TTCCLayout();
      } else {
         this.layout = layout;
      }

      try {
         _handle = registerEventSource(server, source);
      } catch (final Exception e) {
         e.printStackTrace();
         _handle = 0;
      }
   }


   public void close() {

      deregisterEventSource(_handle);
      _handle = 0;
   }


   public void activateOptions() {

      if (source != null) {
         try {
            _handle = registerEventSource(server, source);
         } catch (final Exception e) {
            LogLog.error("Could not register event source.", e);
            _handle = 0;
         }
      }
   }


   public void append(final LoggingEvent event) {

      final StringBuilder sbuf = new StringBuilder(11);

      sbuf.append(layout.format(event));
      if (layout.ignoresThrowable()) {
         final String[] s = event.getThrowableStrRep();
         if (s != null) {
            for (final String value : s) {
               sbuf.append(value);
            }
         }
      }
      // Normalize the log message level into the supported categories
      final int nt_category = event.getLevel().toInt();

      // Anything above FATAL or below DEBUG is labeled as INFO.
      //if (nt_category > FATAL || nt_category < DEBUG) {
      //  nt_category = INFO;
      //}
      reportEvent(_handle, sbuf.toString(), nt_category);
   }


   /**
    * The <b>Source</b> option which names the source of the event. The current value of this constant is
    * <b>Source</b>.
    */
   public void setSource(final String source) {

      this.source = source.trim();
   }


   public String getSource() {

      return source;
   }


   /**
    * The <code>NTEventLogAppender</code> requires a layout. Hence, this method always returns <code>true</code>.
    */
   public boolean requiresLayout() {

      return true;
   }


   private native int registerEventSource(String server, String source);


   private native void reportEvent(int handle, String message, int level);


   private native void deregisterEventSource(int handle);


   static {
      System.loadLibrary("NTEventLogAppender");
   }
}
