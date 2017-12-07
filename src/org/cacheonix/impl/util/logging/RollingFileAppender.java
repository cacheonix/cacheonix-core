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


package org.cacheonix.impl.util.logging;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.cacheonix.impl.util.logging.helpers.CountingQuietWriter;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.helpers.OptionConverter;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * RollingFileAppender extends FileAppender to backup the log files when they reach a certain size.
 *
 * @author Heinz Richter
 * @author Ceki G&uuml;lc&uuml;
 */
public class RollingFileAppender extends FileAppender {

   /**
    * The default maximum file size is 10MB.
    */
   protected long maxFileSize = (long) (10 << 10 << 10);

   /**
    * There is one backup file by default.
    */
   protected int maxBackupIndex = 1;

   private long nextRollover = 0L;


   /**
    * The default constructor simply calls its {@link FileAppender#FileAppender parents constructor}.
    */
   public RollingFileAppender() {
   }


   /**
    * Instantiate a RollingFileAppender and open the file designated by <code>filename</code>. The opened filename will
    * become the output destination for this appender.
    * <p/>
    * <p>If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file designated by
    * <code>filename</code> will be truncated before being opened.
    */
   public RollingFileAppender(final Layout layout, final String filename, final boolean append)
           throws IOException {
      super(layout, filename, append);
   }


   /**
    * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened filename will become
    * the output destination for this appender.
    * <p/>
    * <p>The file will be appended to.
    */
   public RollingFileAppender(final Layout layout, final String filename) throws IOException {
      super(layout, filename);
   }


   /**
    * Returns the value of the <b>MaxBackupIndex</b> option.
    */
   public int getMaxBackupIndex() {
      return maxBackupIndex;
   }


   /**
    * Get the maximum size that the output file is allowed to reach before being rolled over to backup files.
    *
    * @since 1.1
    */
   public long getMaximumFileSize() {
      return maxFileSize;
   }


   /**
    * Implements the usual roll over behaviour.
    * <p/>
    * <p>If <code>MaxBackupIndex</code> is positive, then files {<code>File.1</code>, ..., <code>File.MaxBackupIndex
    * -1</code>} are renamed to {<code>File.2</code>, ..., <code>File.MaxBackupIndex</code>}. Moreover,
    * <code>File</code> is renamed <code>File.1</code> and closed. A new <code>File</code> is created to receive further
    * log output.
    * <p/>
    * <p>If <code>MaxBackupIndex</code> is equal to zero, then the <code>File</code> is truncated with no backup files
    * created.
    */
   public final // synchronization not necessary since doAppend is already synced
   void rollOver() {

      if (qw != null) {
         final long size = ((CountingQuietWriter) qw).getCount();
         LogLog.debug("rolling over count=" + size);
         //   if operation fails, do not roll again until
         //      maxFileSize more bytes are written
         nextRollover = size + maxFileSize;
      }
      LogLog.debug("maxBackupIndex=" + maxBackupIndex);

      boolean renameSucceeded = true;
      // If maxBackups <= 0, then there is no file renaming to be done.
      if (maxBackupIndex > 0) {
         // Delete the oldest file, to keep Windows happy.
         File file = new File(fileName + '.' + maxBackupIndex);
         if (file.exists()) {
            renameSucceeded = file.delete();
         }

         // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
         File target;
         for (int i = maxBackupIndex - 1; i >= 1 && renameSucceeded; i--) {
            file = new File(fileName + '.' + i);
            if (file.exists()) {
               target = new File(fileName + '.' + (i + 1));
               LogLog.debug("Renaming file " + file + " to " + target);
               renameSucceeded = file.renameTo(target);
            }
         }

         if (renameSucceeded) {
            // Rename fileName to fileName.1
            target = new File(fileName + '.' + 1);

            this.closeFile(); // keep windows happy.

            file = new File(fileName);
            LogLog.debug("Renaming file " + file + " to " + target);
            renameSucceeded = file.renameTo(target);
            //
            //   if file rename failed, reopen file with append = true
            //
            if (!renameSucceeded) {
               try {
                  this.setFile(fileName, true, bufferedIO, bufferSize);
               }
               catch (final IOException e) {
                  LogLog.error("setFile(" + fileName + ", true) call failed.", e);
               }
            }
         }
      }

      //
      //   if all renames were successful, then
      //
      if (renameSucceeded) {
         try {
            // This will also close the file. This is OK since multiple
            // close operations are safe.
            this.setFile(fileName, false, bufferedIO, bufferSize);
            nextRollover = 0L;
         }
         catch (final IOException e) {
            LogLog.error("setFile(" + fileName + ", false) call failed.", e);
         }
      }
   }


   public
   final synchronized void setFile(final String fileName, final boolean append, final boolean bufferedIO,
                                   final int bufferSize)
           throws IOException {
      super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
      if (append) {
         final File f = new File(fileName);
         ((CountingQuietWriter) qw).setCount(f.length());
      }
   }


   /**
    * Set the maximum number of backup files to keep around.
    * <p/>
    * <p>The <b>MaxBackupIndex</b> option determines how many backup files are kept before the oldest is erased. This
    * option takes a positive integer value. If set to zero, then there will be no backup files and the log file will be
    * truncated when it reaches <code>MaxFileSize</code>.
    */
   public void setMaxBackupIndex(final int maxBackups) {
      this.maxBackupIndex = maxBackups;
   }


   /**
    * Set the maximum size that the output file is allowed to reach before being rolled over to backup files.
    * <p/>
    * <p>This method is equivalent to {@link #setMaxFileSize} except that it is required for differentiating the setter
    * taking a <code>long</code> argument from the setter taking a <code>String</code> argument by the JavaBeans {@link
    * Introspector Introspector}.
    *
    * @see #setMaxFileSize(String)
    */
   public void setMaximumFileSize(final long maxFileSize) {
      this.maxFileSize = maxFileSize;
   }


   /**
    * Set the maximum size that the output file is allowed to reach before being rolled over to backup files.
    * <p/>
    * <p>In configuration files, the <b>MaxFileSize</b> option takes an long integer in the range 0 - 2^63. You can
    * specify the value with the suffixes "KB", "MB" or "GB" so that the integer is interpreted being expressed
    * respectively in kilobytes, megabytes or gigabytes. For example, the value "10KB" will be interpreted as 10240.
    */
   public void setMaxFileSize(final String value) {
      maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1L);
   }


   protected void setQWForFiles(final Writer writer) {
      this.qw = new CountingQuietWriter(writer, errorHandler);
   }


   /**
    * This method differentiates RollingFileAppender from its super class.
    *
    * @since 0.9.0
    */
   protected void subAppend(final LoggingEvent event) {
      super.subAppend(event);
      if (fileName != null && qw != null) {
         final long size = ((CountingQuietWriter) qw).getCount();
         if (size >= maxFileSize && size >= nextRollover) {
            rollOver();
         }
      }
   }
}
