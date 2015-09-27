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

import java.util.Date;

import org.cacheonix.impl.util.logging.helpers.Transform;
import org.cacheonix.impl.util.logging.net.SMTPAppender;
import org.cacheonix.impl.util.logging.spi.LocationInfo;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;

/**
 * This layout outputs events in a HTML table.
 * <p/>
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class HTMLLayout extends Layout {

   protected static final int BUF_SIZE = 256;

   protected static final int MAX_CAPACITY = 1024;

   static final String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";

   // output buffer appended to when format() is invoked
   private StringBuffer sbuf = new StringBuffer(BUF_SIZE);

   /**
    * A string constant used in naming the option for setting the the location information flag. Current value of this
    * string constant is <b>LocationInfo</b>.
    * <p/>
    * <p>Note that all option keys are case sensitive.
    *
    * @deprecated Options are now handled using the JavaBeans paradigm. This constant is not longer needed and will be
    *             removed in the <em>near</em> term.
    */
   public static final String LOCATION_INFO_OPTION = "LocationInfo";

   /**
    * A string constant used in naming the option for setting the the HTML document title.  Current value of this string
    * constant is <b>Title</b>.
    */
   public static final String TITLE_OPTION = "Title";

   // Print no location info by default
   boolean locationInfo = false;

   String title = "Log4J Log Messages";


   /**
    * The <b>LocationInfo</b> option takes a boolean value. By default, it is set to <code>false</code> which means
    * there will be no location information output by this layout. If the the option is set to true, then the file name
    * and line number of the statement at the origin of the log statement will be output.
    * <p/>
    * <p>If you are embedding this layout within an {@link SMTPAppender} then make sure to set the <b>LocationInfo</b>
    * option of that appender as well.
    */
   public void setLocationInfo(final boolean flag) {

      locationInfo = flag;
   }


   /**
    * Returns the current value of the <b>LocationInfo</b> option.
    */
   public boolean getLocationInfo() {

      return locationInfo;
   }


   /**
    * The <b>Title</b> option takes a String value. This option sets the document title of the generated HTML document.
    * <p/>
    * <p>Defaults to 'Log4J Log Messages'.
    */
   public void setTitle(final String title) {

      this.title = title;
   }


   /**
    * Returns the current value of the <b>Title</b> option.
    */
   public String getTitle() {

      return title;
   }


   /**
    * Returns the content type output by this layout, i.e "text/html".
    */
   public String getContentType() {

      return "text/html";
   }


   /**
    * No options to activate.
    */
   public void activateOptions() {

   }


   public String format(final LoggingEvent event) {

      if (sbuf.capacity() > MAX_CAPACITY) {
         sbuf = new StringBuffer(BUF_SIZE);
      } else {
         sbuf.setLength(0);
      }

      sbuf.append(Layout.LINE_SEP).append("<tr>").append(Layout.LINE_SEP);

      sbuf.append("<td>");
      sbuf.append(event.timeStamp - LoggingEvent.getStartTime());
      sbuf.append("</td>").append(Layout.LINE_SEP);

      final String escapedThread = Transform.escapeTags(event.getThreadName());
      sbuf.append("<td title=\"").append(escapedThread).append(" thread\">");
      sbuf.append(escapedThread);
      sbuf.append("</td>").append(Layout.LINE_SEP);

      sbuf.append("<td title=\"Level\">");
      if (event.getLevel().equals(Level.DEBUG)) {
         sbuf.append("<font color=\"#339933\">");
         sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
         sbuf.append("</font>");
      } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
         sbuf.append("<font color=\"#993300\"><strong>");
         sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
         sbuf.append("</strong></font>");
      } else {
         sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
      }
      sbuf.append("</td>").append(Layout.LINE_SEP);

      final String escapedLogger = Transform.escapeTags(event.getLoggerName());
      sbuf.append("<td title=\"").append(escapedLogger).append(" category\">");
      sbuf.append(escapedLogger);
      sbuf.append("</td>").append(Layout.LINE_SEP);

      if (locationInfo) {
         final LocationInfo locInfo = event.getLocationInformation();
         sbuf.append("<td>");
         sbuf.append(Transform.escapeTags(locInfo.getFileName()));
         sbuf.append(':');
         sbuf.append(locInfo.getLineNumber());
         sbuf.append("</td>").append(Layout.LINE_SEP);
      }

      sbuf.append("<td title=\"Message\">");
      sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
      sbuf.append("</td>").append(Layout.LINE_SEP);
      sbuf.append("</tr>").append(Layout.LINE_SEP);

      if (event.getNDC() != null) {
         sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
         sbuf.append("NDC: ").append(Transform.escapeTags(event.getNDC()));
         sbuf.append("</td></tr>").append(Layout.LINE_SEP);
      }

      final String[] s = event.getThrowableStrRep();
      if (s != null) {
         sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
         appendThrowableAsHTML(s, sbuf);
         sbuf.append("</td></tr>").append(Layout.LINE_SEP);
      }

      return sbuf.toString();
   }


   final void appendThrowableAsHTML(final String[] s, final StringBuffer sbuf) {

      if (s != null) {
         final int len = s.length;
         if (len == 0) {
            return;
         }
         sbuf.append(Transform.escapeTags(s[0]));
         sbuf.append(Layout.LINE_SEP);
         for (int i = 1; i < len; i++) {
            sbuf.append(TRACE_PREFIX);
            sbuf.append(Transform.escapeTags(s[i]));
            sbuf.append(Layout.LINE_SEP);
         }
      }
   }


   /**
    * Returns appropriate HTML headers.
    */
   public String getHeader() {

      final StringBuilder sbuf = new StringBuilder(256);
      sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">").append(Layout.LINE_SEP);
      sbuf.append("<html>").append(Layout.LINE_SEP);
      sbuf.append("<head>").append(Layout.LINE_SEP);
      sbuf.append("<title>").append(title).append("</title>").append(Layout.LINE_SEP);
      sbuf.append("<style type=\"text/css\">").append(Layout.LINE_SEP);
      sbuf.append("<!--").append(Layout.LINE_SEP);
      sbuf.append("body, table {font-family: arial,sans-serif; font-size: x-small;}").append(Layout.LINE_SEP);
      sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}").append(Layout.LINE_SEP);
      sbuf.append("-->").append(Layout.LINE_SEP);
      sbuf.append("</style>").append(Layout.LINE_SEP);
      sbuf.append("</head>").append(Layout.LINE_SEP);
      sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">").append(Layout.LINE_SEP);
      sbuf.append("<hr size=\"1\" noshade>").append(Layout.LINE_SEP);
      sbuf.append("Log session start time ").append(new Date()).append("<br>").append(Layout.LINE_SEP);
      sbuf.append("<br>").append(Layout.LINE_SEP);
      sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">").append(Layout.LINE_SEP);
      sbuf.append("<tr>").append(Layout.LINE_SEP);
      sbuf.append("<th>Time</th>").append(Layout.LINE_SEP);
      sbuf.append("<th>Thread</th>").append(Layout.LINE_SEP);
      sbuf.append("<th>Level</th>").append(Layout.LINE_SEP);
      sbuf.append("<th>Category</th>").append(Layout.LINE_SEP);
      if (locationInfo) {
         sbuf.append("<th>File:Line</th>").append(Layout.LINE_SEP);
      }
      sbuf.append("<th>Message</th>").append(Layout.LINE_SEP);
      sbuf.append("</tr>").append(Layout.LINE_SEP);
      return sbuf.toString();
   }


   /**
    * Returns the appropriate HTML footers.
    */
   public String getFooter() {

      return "</table>" + Layout.LINE_SEP + "<br>" + Layout.LINE_SEP + "</body></html>";
   }


   /**
    * The HTML layout handles the throwable contained in logging events. Hence, this method return <code>false</code>.
    */
   public boolean ignoresThrowable() {

      return false;
   }
}
