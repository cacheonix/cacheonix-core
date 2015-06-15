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

import java.util.Enumeration;
import java.util.Vector;

import org.cacheonix.impl.util.logging.Appender;
import org.cacheonix.impl.util.logging.Category;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.Logger;

/**
 * No-operation implementation of LoggerRepository which is used when LogManager.repositorySelector is erroneously
 * nulled during class reloading.
 *
 * @since 1.2.15
 */
public final class NOPLoggerRepository implements LoggerRepository {

   /**
    * {@inheritDoc}
    */
   public void addHierarchyEventListener(final HierarchyEventListener listener) {

   }


   /**
    * {@inheritDoc}
    */
   public boolean isDisabled(final int level) {

      return true;
   }


   /**
    * {@inheritDoc}
    */
   public void setThreshold(final Level level) {

   }


   /**
    * {@inheritDoc}
    */
   public void setThreshold(final String val) {

   }


   /**
    * {@inheritDoc}
    */
   public void emitNoAppenderWarning(final Category cat) {

   }


   /**
    * {@inheritDoc}
    */
   public Level getThreshold() {

      return Level.OFF;
   }


   /**
    * {@inheritDoc}
    */
   public Logger getLogger(final String name) {

      return new NOPLogger(this, name);
   }


   /**
    * {@inheritDoc}
    */
   public Logger getLogger(final String name, final LoggerFactory factory) {

      return new NOPLogger(this, name);
   }


   /**
    * {@inheritDoc}
    */
   public Logger getRootLogger() {

      return new NOPLogger(this, "root");
   }


   /**
    * {@inheritDoc}
    */
   public Logger exists(final String name) {

      return null;
   }


   /**
    * {@inheritDoc}
    */
   public void shutdown() {

   }


   /**
    * {@inheritDoc}
    */
   public Enumeration getCurrentLoggers() {

      return new Vector(0).elements();
   }


   /**
    * {@inheritDoc}
    */
   public Enumeration getCurrentCategories() {

      return getCurrentLoggers();
   }


   /**
    * {@inheritDoc}
    */
   public void fireAddAppenderEvent(final Category logger, final Appender appender) {

   }


   /**
    * {@inheritDoc}
    */
   public void resetConfiguration() {

   }
}
