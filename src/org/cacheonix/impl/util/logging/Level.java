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

// Contributors:  Kitching Simon <Simon.Kitching@orange.ch>
//                Nicholas Wolff

package org.cacheonix.impl.util.logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Defines the minimum set of levels recognized by the system, that is <code>OFF</code>, <code>FATAL</code>,
 * <code>ERROR</code>, <code>WARN</code>, <code>INFO</code, <code>DEBUG</code> and <code>ALL</code>.
 * <p/>
 * <p>The <code>Level</code> class may be subclassed to define a larger level set.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class Level extends Priority implements Serializable {

   /**
    * TRACE level integer value.
    *
    * @since 1.2.12
    */
   public static final int TRACE_INT = 5000;

   /**
    * The <code>OFF</code> has the highest possible rank and is intended to turn off logging.
    */
   public static final Level OFF = new Level(OFF_INT, "OFF", 0);

   /**
    * The <code>FATAL</code> level designates very severe error events that will presumably lead the application to
    * abort.
    */
   public static final Level FATAL = new Level(FATAL_INT, "FATAL", 0);

   /**
    * The <code>ERROR</code> level designates error events that might still allow the application to continue running.
    */
   public static final Level ERROR = new Level(ERROR_INT, "ERROR", 3);

   /**
    * The <code>WARN</code> level designates potentially harmful situations.
    */
   public static final Level WARN = new Level(WARN_INT, "WARN", 4);

   /**
    * The <code>INFO</code> level designates informational messages that highlight the progress of the application at
    * coarse-grained level.
    */
   public static final Level INFO = new Level(INFO_INT, "INFO", 6);

   /**
    * The <code>DEBUG</code> Level designates fine-grained informational events that are most useful to debug an
    * application.
    */
   public static final Level DEBUG = new Level(DEBUG_INT, "DEBUG", 7);

   /**
    * The <code>TRACE</code> Level designates finer-grained informational events than the <code>DEBUG</code level.
    *
    * @since 1.2.12
    */
   public static final Level TRACE = new Level(TRACE_INT, "TRACE", 7);


   /**
    * The <code>ALL</code> has the lowest possible rank and is intended to turn on all logging.
    */
   public static final Level ALL = new Level(ALL_INT, "ALL", 7);

   /**
    * Serialization version id.
    */
   private static final long serialVersionUID = 3491141966387921974L;


   /**
    * Instantiate a Level object.
    */
   protected Level(final int level, final String levelStr, final int syslogEquivalent) {

      super(level, levelStr, syslogEquivalent);
   }


   /**
    * Convert the string passed as argument to a level. If the conversion fails, then this method returns {@link
    * #DEBUG}.
    */
   public
   static Level toLevel(final String sArg) {

      return toLevel(sArg, DEBUG);
   }


   /**
    * Convert an integer passed as argument to a level. If the conversion fails, then this method returns {@link
    * #DEBUG}.
    */
   public
   static Level toLevel(final int val) {

      return toLevel(val, DEBUG);
   }


   /**
    * Convert an integer passed as argument to a level. If the conversion fails, then this method returns the specified
    * default.
    */
   public
   static Level toLevel(final int val, final Level defaultLevel) {

      switch (val) {
         case ALL_INT:
            return ALL;
         case DEBUG_INT:
            return DEBUG;
         case INFO_INT:
            return INFO;
         case WARN_INT:
            return WARN;
         case ERROR_INT:
            return ERROR;
         case FATAL_INT:
            return FATAL;
         case OFF_INT:
            return OFF;
         case TRACE_INT:
            return TRACE;
         default:
            return defaultLevel;
      }
   }


   /**
    * Convert the string passed as argument to a level. If the conversion fails, then this method returns the value of
    * <code>defaultLevel</code>.
    */
   public
   static Level toLevel(final String sArg, final Level defaultLevel) {

      if (sArg == null) {
         return defaultLevel;
      }

      final String s = sArg.toUpperCase();

      if ("ALL".equals(s)) {
         return ALL;
      }
      if ("DEBUG".equals(s)) {
         return DEBUG;
      }
      if ("INFO".equals(s)) {
         return INFO;
      }
      if ("WARN".equals(s)) {
         return WARN;
      }
      if ("ERROR".equals(s)) {
         return ERROR;
      }
      if ("FATAL".equals(s)) {
         return FATAL;
      }
      if ("OFF".equals(s)) {
         return OFF;
      }
      if ("TRACE".equals(s)) {
         return TRACE;
      }
      //
      //   For Turkish i problem, see bug 40937
      //
      if ("\u0130NFO".equals(s)) {
         return INFO;
      }
      return defaultLevel;
   }


   /**
    * Custom deserialization of Level.
    *
    * @param s serialization stream.
    * @throws IOException            if IO exception.
    * @throws ClassNotFoundException if class not found.
    */
   private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {

      s.defaultReadObject();
      level = s.readInt();
      syslogEquivalent = s.readInt();
      levelStr = s.readUTF();
      if (levelStr == null) {
         levelStr = "";
      }
   }


   /**
    * Serialize level.
    *
    * @param s serialization stream.
    * @throws IOException if exception during serialization.
    */
   private void writeObject(final ObjectOutputStream s) throws IOException {

      s.defaultWriteObject();
      s.writeInt(level);
      s.writeInt(syslogEquivalent);
      s.writeUTF(levelStr);
   }


   /**
    * Resolved deserialized level to one of the stock instances. May be overridden in classes derived from Level.
    *
    * @return resolved object.
    * @throws ObjectStreamException if exception during resolution.
    */
   private Object readResolve() {
      //
      //  if the deserialized object is exactly an instance of Level
      //
      if (Level.class.equals(getClass())) {
         return toLevel(level);
      }
      //
      //   extension of Level can't substitute stock item
      //
      return this;
   }

}
