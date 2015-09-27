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

// Contributors: Mathias Rupprecht <mmathias.rupprecht@fja.com>

package org.cacheonix.impl.util.logging.spi;

import java.io.Serializable;

import org.cacheonix.impl.util.logging.helpers.LogLog;

/**
 * The internal representation of caller location information.
 *
 * @since 0.8.3
 */
public final class LocationInfo implements Serializable {

   /**
    * When location information is not available the constant <code>NA</code> is returned. Current value of this string
    * constant is <b>?</b>.
    */
   public static final String NA = "?";

   /**
    * NA_LOCATION_INFO is provided for compatibility with log4j 1.3.
    *
    * @since 1.2.15
    */
   public static final LocationInfo NA_LOCATION_INFO = new LocationInfo(NA, NA, NA, NA);

   /**
    * Caller's line number.
    */
   transient String lineNumber = null;

   /**
    * Caller's file name.
    */
   transient String fileName = null;

   /**
    * Caller's fully qualified class name.
    */
   transient String className = null;

   /**
    * Caller's method name.
    */
   transient String methodName = null;

   /**
    * All available caller information, in the format <code>fully.qualified.classname.of.caller.methodName(Filename.java:line)</code>
    */
   public String fullInfo;

   private static final long serialVersionUID = -1325822038990805636L;

   // Check if we are running in IBM's visual age.
   @SuppressWarnings("StaticNonFinalField")
   static boolean inVisualAge = false;


   static {
      try {

         inVisualAge = Class.forName("com.ibm.uvm.tools.DebugSupport") != null;
         LogLog.debug("Detected IBM VisualAge environment.");
      } catch (final Throwable e) {
         // nothing to do
      }
   }


   /**
    * Instantiate location information based on a Throwable. We expect the Throwable <code>t</code>, to be in the
    * format
    * <p/>
    * <pre>
    * java.lang.Throwable
    * ...
    * at org.cacheonix.impl.util.logging.PatternLayout.format(PatternLayout.java:413)
    * at org.cacheonix.impl.util.logging.FileAppender.doAppend(FileAppender.java:183)
    * at org.cacheonix.impl.util.logging.Category.callAppenders(Category.java:131)
    * at org.cacheonix.impl.util.logging.Category.log(Category.java:512)
    * at callers.fully.qualified.className.methodName(FileName.java:74)
    * ...
    * </pre>
    * <p/>
    * <p>However, we can also deal with JIT compilers that "lose" the location information, especially between the
    * parentheses.
    */
   public LocationInfo(final Throwable t, final String fqnOfCallingClass) {

      if (t == null || fqnOfCallingClass == null) {
         return;
      }

      boolean fqnFound = false;
      final StackTraceElement[] stackTrace = t.getStackTrace();
      for (final StackTraceElement stackTraceElement : stackTrace) {

         if (stackTraceElement.getClassName().equals(fqnOfCallingClass)) {

            fqnFound = true;
         } else {

            if (fqnFound) {

               fileName = stackTraceElement.getFileName();
               lineNumber = stackTraceElement.getLineNumber() >= 0 ? Integer.toString(stackTraceElement.getLineNumber()) : NA;
               methodName = stackTraceElement.getMethodName();
               className = stackTraceElement.getClassName();
               break;
            }
         }
      }

      this.fullInfo = toFullInfo(fileName, className, methodName, lineNumber);
   }


   /**
    * Create new instance.
    *
    * @param file      source file name
    * @param className class name
    * @param method    method
    * @param line      source line number
    * @since 1.2.15
    */
   public LocationInfo(final String file, final String className, final String method, final String line) {

      this.fileName = file;
      this.className = className;
      this.methodName = method;
      this.lineNumber = line;
      this.fullInfo = toFullInfo(file, className, method, line);
   }


   /**
    * Return the fully qualified class name of the caller making the logging request.
    */
   public final String getClassName() {

      return className == null ? NA : className;
   }


   /**
    * Return the file name of the caller.
    * <p/>
    * <p>This information is not always available.
    */
   public final String getFileName() {

      return fileName == null ? NA : fileName;
   }


   /**
    * Returns the line number of the caller.
    * <p/>
    * <p>This information is not always available.
    */
   public final String getLineNumber() {

      return lineNumber == null ? NA : lineNumber;
   }


   /**
    * Returns the method name of the caller.
    */
   public final String getMethodName() {

      return methodName == null ? NA : methodName;
   }


   private static String toFullInfo(final String file, final String classname, final String method, final String line) {

      final StringBuffer buf = new StringBuffer(200);
      appendFragment(buf, classname);
      buf.append('.');
      appendFragment(buf, method);
      buf.append('(');
      appendFragment(buf, file);
      buf.append(':');
      appendFragment(buf, line);
      buf.append(')');
      return buf.toString();
   }


   /**
    * Appends a location fragment to a buffer to build the full location info.
    *
    * @param buf      StringBuffer to receive content.
    * @param fragment fragment of location (class, method, file, line), if null the value of NA will be appended.
    * @since 1.2.15
    */
   private static void appendFragment(final StringBuffer buf,
                                      final String fragment) {

      if (fragment == null) {
         buf.append(NA);
      } else {
         buf.append(fragment);
      }
   }
}
