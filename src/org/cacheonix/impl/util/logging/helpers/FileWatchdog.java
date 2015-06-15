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

// Contributors:  Mathias Bogaert

package org.cacheonix.impl.util.logging.helpers;

import java.io.File;

/**
 * Check every now and then that a certain file has not changed. If it has, then call the {@link #doOnChange} method.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since version 0.9.1
 */
public abstract class FileWatchdog extends Thread {

   /**
    * The default delay between every file modification check, set to 60 seconds.
    */
   public static final long DEFAULT_DELAY = 60000L;

   /**
    * The name of the file to observe  for changes.
    */
   protected final String filename;

   /**
    * The delay to observe between every check. By default set {@link #DEFAULT_DELAY}.
    */
   protected long delay = DEFAULT_DELAY;

   final File file;

   long lastModif = 0L;

   boolean warnedAlready = false;

   volatile boolean interrupted = false;


   protected FileWatchdog(final String filename) {

      this.filename = filename;
      file = new File(filename);
      setDaemon(true);
      checkAndConfigure();
   }


   /**
    * Set the delay to observe between each check of the file changes.
    */
   public final void setDelay(final long delay) {

      this.delay = delay;
   }


   protected abstract void doOnChange();


   protected final void checkAndConfigure() {

      final boolean fileExists;
      try {
         fileExists = file.exists();
      } catch (final SecurityException e) {
         LogLog.warn("Was not allowed to read check file existance, file:[" +
                 filename + "].");
         interrupted = true; // there is no point in continuing
         return;
      }

      if (fileExists) {
         final long l = file.lastModified(); // this can also throw a SecurityException
         if (l > lastModif) {           // however, if we reached this point this
            lastModif = l;              // is very unlikely.
            doOnChange();
            warnedAlready = false;
         }
      } else {
         if (!warnedAlready) {
            LogLog.debug('[' + filename + "] does not exist.");
            warnedAlready = true;
         }
      }
   }


   public final void run() {

      while (!interrupted) {
         try {
            Thread.sleep(delay);
         } catch (final InterruptedException e) {
            // no interruption expected
         }
         checkAndConfigure();
      }
   }
}
