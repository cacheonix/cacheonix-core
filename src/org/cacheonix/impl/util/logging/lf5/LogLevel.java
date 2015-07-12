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
package org.cacheonix.impl.util.logging.lf5;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The LogLevel class defines a set of standard logging levels.
 * <p/>
 * The logging Level objects are ordered and are specified by ordered integers. Enabling logging at a given level also
 * enables logging at all higher levels.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brent Sprecher
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public final class LogLevel implements Serializable {

   private static final long serialVersionUID = 0;

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   // log4j log levels.
   public static final LogLevel FATAL = new LogLevel("FATAL", 0);

   public static final LogLevel ERROR = new LogLevel("ERROR", 1);

   public static final LogLevel WARN = new LogLevel("WARN", 2);

   public static final LogLevel INFO = new LogLevel("INFO", 3);

   public static final LogLevel DEBUG = new LogLevel("DEBUG", 4);

   // jdk1.4 log levels NOTE: also includes INFO
   public static final LogLevel SEVERE = new LogLevel("SEVERE", 1);

   public static final LogLevel WARNING = new LogLevel("WARNING", 2);

   public static final LogLevel CONFIG = new LogLevel("CONFIG", 4);

   public static final LogLevel FINE = new LogLevel("FINE", 5);

   public static final LogLevel FINER = new LogLevel("FINER", 6);

   public static final LogLevel FINEST = new LogLevel("FINEST", 7);

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   protected final String _label;

   protected final int _precedence;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------
   private static final LogLevel[] _log4JLevels;

   private static final LogLevel[] _jdk14Levels;

   private static final LogLevel[] _allDefaultLevels;

   private static final Map _logLevelMap;

   private static final Map _logLevelColorMap;

   private static final Map _registeredLogLevelMap = new HashMap(11);


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------
   static {
      _log4JLevels = new LogLevel[]{FATAL, ERROR, WARN, INFO, DEBUG};
      _jdk14Levels = new LogLevel[]{SEVERE, WARNING, INFO,
              CONFIG, FINE, FINER, FINEST};
      _allDefaultLevels = new LogLevel[]{FATAL, ERROR, WARN, INFO, DEBUG,
              SEVERE, WARNING, CONFIG, FINE, FINER, FINEST};

      _logLevelMap = new HashMap(11);
      for (final LogLevel _allDefaultLevel1 : _allDefaultLevels) {
         _logLevelMap.put(_allDefaultLevel1._label, _allDefaultLevel1);
      }

      // prepopulate map with levels and text color of black
      _logLevelColorMap = new HashMap(11);
      for (final LogLevel _allDefaultLevel : _allDefaultLevels) {
         _logLevelColorMap.put(_allDefaultLevel, Color.black);
      }
   }


   public LogLevel(final String label, final int precedence) {

      _label = label;
      _precedence = precedence;
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * Return the Label of the LogLevel.
    */
   public final String getLabel() {

      return _label;
   }


   /**
    * Returns <code>true</code> if the level supplied is encompassed by this level. For example, LogLevel.SEVERE
    * encompasses no other LogLevels and LogLevel.FINE encompasses all other LogLevels.  By definition, a LogLevel
    * encompasses itself.
    */
   public boolean encompasses(final LogLevel level) {

      return level._precedence <= _precedence;

   }


   /**
    * Convert a log level label into a LogLevel object.
    *
    * @param level The label of a level to be converted into a LogLevel.
    * @return LogLevel The LogLevel with a label equal to level.
    * @throws LogLevelFormatException Is thrown when the level can not be converted into a LogLevel.
    */
   public static LogLevel valueOf(String level)
           throws LogLevelFormatException {

      LogLevel logLevel = null;
      if (level != null) {
         level = level.trim().toUpperCase();
         logLevel = (LogLevel) _logLevelMap.get(level);
      }

      // Didn't match, Check for registered LogLevels
      if (logLevel == null && !_registeredLogLevelMap.isEmpty()) {
         logLevel = (LogLevel) _registeredLogLevelMap.get(level);
      }

      if (logLevel == null) {
         final StringBuilder buf = new StringBuilder(60);
         buf.append("Error while trying to parse (").append(level).append(") into");
         buf.append(" a LogLevel.");
         throw new LogLevelFormatException(buf.toString());
      }
      return logLevel;
   }


   /**
    * Registers a used defined LogLevel.
    *
    * @param logLevel The log level to be registered. Cannot be a default LogLevel
    * @return LogLevel The replaced log level.
    */
   public static LogLevel register(final LogLevel logLevel) {

      if (logLevel == null) {
         return null;
      }

      // ensure that this is not a default log level
      if (_logLevelMap.get(logLevel._label) == null) {
         return (LogLevel) _registeredLogLevelMap.put(logLevel._label, logLevel);
      }

      return null;
   }


   public static void register(final LogLevel[] logLevels) {

      if (logLevels != null) {
         for (final LogLevel logLevel : logLevels) {
            register(logLevel);
         }
      }
   }


   public static void register(final List logLevels) {

      if (logLevels != null) {
         final Iterator it = logLevels.iterator();
         while (it.hasNext()) {
            register((LogLevel) it.next());
         }
      }
   }


   public final boolean equals(final Object o) {

      boolean equals = false;

      if (o instanceof LogLevel) {
         if (this._precedence ==
                 ((LogLevel) o)._precedence) {
            equals = true;
         }

      }

      return equals;
   }


   public final int hashCode() {

      return _label.hashCode();
   }


   public final String toString() {

      return _label;
   }


   // set a text color for a specific log level
   public final void setLogLevelColorMap(final LogLevel level, Color color) {
      // remove the old entry
      _logLevelColorMap.remove(level);
      // add the new color entry
      if (color == null) {
         color = Color.black;
      }
      _logLevelColorMap.put(level, color);
   }


   public static void resetLogLevelColorMap() {
      // empty the map
      _logLevelColorMap.clear();

      // repopulate map and reset text color black
      for (final LogLevel _allDefaultLevel : _allDefaultLevels) {
         _logLevelColorMap.put(_allDefaultLevel, Color.black);
      }
   }


   /**
    * @return A <code>List</code> of <code>LogLevel</code> objects that map to log4j <code>Priority</code> objects.
    */
   public static List getLog4JLevels() {

      return Arrays.asList(_log4JLevels);
   }


   public static List getJdk14Levels() {

      return Arrays.asList(_jdk14Levels);
   }


   public static List getAllDefaultLevels() {

      return Arrays.asList(_allDefaultLevels);
   }


   public static Map getLogLevelColorMap() {

      return _logLevelColorMap;
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   protected final int getPrecedence() {

      return _precedence;
   }

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






