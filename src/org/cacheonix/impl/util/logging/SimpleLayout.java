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

import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * SimpleLayout consists of the level of the log statement, followed by " - " and then the log message itself. For
 * example,
 * <p/>
 * <pre>
 * DEBUG - Hello world
 * </pre>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since version 0.7.0
 *        <p/>
 *        <p>{@link PatternLayout} offers a much more powerful alternative.
 */
public final class SimpleLayout extends Layout {

   final StringBuffer sbuf = new StringBuffer(128);


   public void activateOptions() {
   }


   /**
    * Returns the log statement in a format consisting of the <code>level</code>, followed by " - " and then the
    * <code>message</code>. For example, <pre> INFO - "A message"
    * </pre>
    * <p/>
    * <p>The <code>category</code> parameter is ignored.
    *
    * @return A byte array in SimpleLayout format.
    */
   public String format(final LoggingEvent event) {

      sbuf.setLength(0);
      sbuf.append(event.getLevel());
      sbuf.append(" - ");
      sbuf.append(event.getRenderedMessage());
      sbuf.append(LINE_SEP);
      return sbuf.toString();
   }


   /**
    * The SimpleLayout does not handle the throwable contained within {@link LoggingEvent LoggingEvents}. Thus, it
    * returns <code>true</code>.
    *
    * @since version 0.8.4
    */
   public boolean ignoresThrowable() {
      return true;
   }
}
