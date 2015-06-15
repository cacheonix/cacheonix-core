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

import java.io.IOException;
import java.io.Writer;

import org.cacheonix.impl.util.logging.spi.ErrorCode;
import org.cacheonix.impl.util.logging.spi.ErrorHandler;

/**
 * Counts the number of bytes written.
 *
 * @author Heinz Richter, heinz.richter@frogdot.com
 * @since 0.8.1
 */
public final class CountingQuietWriter extends QuietWriter {

   protected long count = 0L;


   public CountingQuietWriter(final Writer writer, final ErrorHandler eh) {
      super(writer, eh);
   }


   public final void write(final String string) {
      try {
         out.write(string);
         count += (long) string.length();
      }
      catch (final IOException e) {
         errorHandler.error("Write failure.", e, ErrorCode.WRITE_FAILURE);
      }
   }


   public final long getCount() {
      return count;
   }


   public final void setCount(final long count) {
      this.count = count;
   }

}
