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

package org.cacheonix.impl.util.logging.config;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.cacheonix.impl.util.logging.Appender;
import org.cacheonix.impl.util.logging.Category;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.config.PropertyGetter.PropertyCallback;

/**
 * Prints the configuration of the log4j default hierarchy (which needs to be auto-initialized) as a propoperties file
 * on a {@link PrintWriter}.
 *
 * @author Anders Kristensen
 */
public final class PropertyPrinter implements PropertyCallback {

   private int numAppenders = 0;

   private final Hashtable appenderNames = new Hashtable(3);

   private final PrintWriter out;


   public PropertyPrinter(final PrintWriter out) {

      this.out = out;
   }


   private final String genAppName() {

      return "A" + numAppenders++;
   }


   /**
    * Returns <code>true</code> if the specified appender name is considered to have been generated, that is, if it is
    * of the form A[0-9]+.
    */
   private final boolean isGenAppName(final String name) {

      if (name.length() < 2 || name.charAt(0) != 'A') {
         return false;
      }

      for (int i = 0; i < name.length(); i++) {
         if (name.charAt(i) < '0' || name.charAt(i) > '9') {
            return false;
         }
      }
      return true;
   }


   /**
    * Prints the configuration of the default log4j hierarchy as a Java properties file on the specified Writer.
    * <p/>
    * <p>N.B. print() can be invoked only once!
    */
   public final void print() {

      printOptions(out, Logger.getRootLogger());

      final Enumeration cats = LogManager.getCurrentLoggers();
      while (cats.hasMoreElements()) {
         printOptions(out, (Logger) cats.nextElement());
      }
   }


   private final void printOptions(final PrintWriter out, final Category cat) {

      final Enumeration appenders = cat.getAllAppenders();
      final Level prio = cat.getLevel();
      final StringBuilder appenderString = new StringBuilder(prio == null ? "" : prio.toString());


      while (appenders.hasMoreElements()) {
         final Appender app = (Appender) appenders.nextElement();
         String name;

         if ((name = (String) appenderNames.get(app)) == null) {

            // first assign name to the appender
            if ((name = app.getName()) == null || isGenAppName(name)) {
               name = genAppName();
            }
            appenderNames.put(app, name);

            printOptions(out, app, "log4j.appender." + name);
            if (app.getLayout() != null) {
               printOptions(out, app.getLayout(), "log4j.appender." + name + ".layout");
            }
         }
         appenderString.append(", ").append(name);
      }
      final String catKey = cat.equals(Logger.getRootLogger())
              ? "log4j.rootLogger"
              : "log4j.logger." + cat.getName();
      if (appenderString.length() != 0) {
         out.println(catKey + '=' + appenderString);
      }
      if (!cat.getAdditivity() && !cat.equals(Logger.getRootLogger())) {
         out.println("log4j.additivity." + cat.getName() + "=false");
      }
   }


   private final void printOptions(final PrintWriter out, final Logger cat) {

      printOptions(out, (Category) cat);
   }


   private final void printOptions(final PrintWriter out, final Object obj, final String fullname) {

      out.println(fullname + '=' + obj.getClass().getName());
      PropertyGetter.getProperties(obj, this, fullname + '.');
   }


   public void foundProperty(final Object obj, final String prefix, final String name,
           final Object value) {
      // XXX: Properties encode value.toString()
      if (obj instanceof Appender && "name".equals(name)) {
         return;
      }
      out.println(prefix + name + '=' + value);
   }


   // for testing
   public static void main(final String[] args) {

      final PrintWriter out = new PrintWriter(System.out);
      final PropertyPrinter propertyPrinter = new PropertyPrinter(out);
      propertyPrinter.print();
      out.flush();
   }
}
