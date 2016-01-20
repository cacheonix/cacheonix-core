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

package org.cacheonix.impl.util.logging.spi;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * ThrowableInformation is log4j's internal representation of throwables. It essentially consists of a string array,
 * called 'rep', where the first element, that is rep[0], represents the string representation of the throwable (i.e.
 * the value you get when you do throwable.toString()) and subsequent elements correspond the stack trace with the top
 * most entry of the stack corresponding to the second entry of the 'rep' array that is rep[1].
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class ThrowableInformation implements Serializable {

   private static final long serialVersionUID = -4748765566864322735L;

   private transient Throwable throwable = null;
   private String[] rep = null;


   public ThrowableInformation(final Throwable throwable) {
      this.throwable = throwable;
   }


   /**
    * Create new instance.
    *
    * @param r String representation of throwable.
    * @since 1.2.15
    */
   public ThrowableInformation(final String[] r) {
      if (r != null) {
         rep = r.clone();
      }
   }


   public Throwable getThrowable() {
      return throwable;
   }


   public final String[] getThrowableStrRep() {
      if (rep != null) {
         return rep.clone();
      }

      final StringWriter sw = new StringWriter(100);
      final PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      pw.flush();
      final LineNumberReader reader = new LineNumberReader(
              new StringReader(sw.toString()));
      final Collection lines = new ArrayList(11);
      try {
         String line = reader.readLine();
         while (line != null) {
            lines.add(line);
            line = reader.readLine();
         }
      } catch (final IOException ex) {
         lines.add(ex.toString());
      }
      rep = new String[lines.size()];
      lines.toArray(rep);
      return rep;
   }
}


