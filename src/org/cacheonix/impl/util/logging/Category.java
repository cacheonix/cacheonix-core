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

// Contributors: Alex Blewitt <Alex.Blewitt@ioshq.com>
//              Markus Oestreicher <oes@zurich.ibm.com>
//              Frank Hoering <fhr@zurich.ibm.com>
//              Nelson Minar <nelson@media.mit.edu>
//              Jim Cakalic <jim_cakalic@na.biomerieux.com>
//              Avy Sharell <asharell@club-internet.fr>
//              Ciaran Treanor <ciaran@xelector.com>
//              Jeff Turner <jeff@socialchange.net.au>
//              Michael Horwitz <MHorwitz@siemens.co.za>
//              Calvin Chan <calvin.chan@hic.gov.au>
//              Aaron Greenhouse <aarong@cs.cmu.edu>
//              Beat Meier <bmeier@infovia.com.ar>
//              Colin Sampaleanu <colinml1@exis.com>

package org.cacheonix.impl.util.logging;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.cacheonix.impl.util.logging.helpers.AppenderAttachableImpl;
import org.cacheonix.impl.util.logging.helpers.NullEnumeration;
import org.cacheonix.impl.util.logging.net.SocketAppender;
import org.cacheonix.impl.util.logging.or.ObjectRenderer;
import org.cacheonix.impl.util.logging.spi.AppenderAttachable;
import org.cacheonix.impl.util.logging.spi.HierarchyEventListener;
import org.cacheonix.impl.util.logging.spi.LoggerRepository;
import org.cacheonix.impl.util.logging.spi.LoggingEvent;


/**
 * <font color="#AA2222"><b>This class has been deprecated and replaced by the {@link Logger}
 * <em>subclass</em></b></font>. It will be kept around to preserve backward compatibility until mid 2003.
 * <p/>
 * <p><code>Logger</code> is a subclass of Category, i.e. it extends Category. In other words, a logger <em>is</em> a
 * category. Thus, all operations that can be performed on a category can be performed on a logger. Internally, whenever
 * log4j is asked to produce a Category object, it will instead produce a Logger object. Log4j 1.2 will <em>never</em>
 * produce Category objects but only <code>Logger</code> instances. In order to preserve backward compatibility, methods
 * that previously accepted category objects still continue to accept category objects.
 * <p/>
 * <p>For example, the following are all legal and will work as expected.
 * <p/>
 * <pre>
 * &nbsp;&nbsp;&nbsp;// Deprecated form:
 * &nbsp;&nbsp;&nbsp;Category cat = Category.getInstance("foo.bar")
 * <p/>
 * &nbsp;&nbsp;&nbsp;// Preferred form for retrieving loggers:
 * &nbsp;&nbsp;&nbsp;Logger logger = Logger.getLogger("foo.bar")
 * </pre>
 * <p/>
 * <p>The first form is deprecated and should be avoided.
 * <p/>
 * <p><b>There is absolutely no need for new client code to use or refer to the <code>Category</code> class.</b>
 * Whenever possible, please avoid referring to it or using it.
 * <p/>
 * <p>See the <a href="../../../../manual.html">short manual</a> for an introduction on this class.
 * <p/>
 * See the document entitled <a href="http://www.qos.ch/logging/preparingFor13.html">preparing for log4j 1.3</a> for a
 * more detailed discussion.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 */
@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class Category implements AppenderAttachable {

   /**
    The hierarchy where categories are attached to by default.
    */
   //static
   //public
   //final Hierarchy defaultHierarchy = new Hierarchy(new
   //					   RootCategory(Level.DEBUG));

   /**
    * The name of this category.
    */
   protected final String name;

   /**
    * The assigned level of this category.  The <code>level</code> variable need not be assigned a value in which case
    * it is inherited form the hierarchy.
    */
   protected volatile Level level = null;

   /**
    * The parent of this category. All categories have at least one ancestor which is the root category.
    */
   protected volatile Category parent = null;

   /**
    * The fully qualified name of the Category class. See also the getFQCN method.
    */
   private static final String FQCN = Category.class.getName();

   protected ResourceBundle resourceBundle = null;

   // Categories need to know what Hierarchy they are in
   protected LoggerRepository repository = null;


   AppenderAttachableImpl aai = null;

   /**
    * Additivity is set to <code>true</code> by default, that is children inherit the appenders of their ancestors by
    * default. If this variable is set to <code>false</code> then the appenders found in the ancestors of this category
    * are not used. However, the children of this category will inherit its appenders, unless the children have their
    * additivity flag set to <code>false</code> too. See the user manual for more details.
    */
   protected boolean additive = true;


   /**
    * This constructor created a new <code>Category</code> instance and sets its name.
    * <p/>
    * <p>It is intended to be used by sub-classes only. You should not create categories directly.
    *
    * @param name The name of the category.
    */
   protected Category(final String name) {

      this.name = name;
   }


   /**
    * Add <code>newAppender</code> to the list of appenders of this Category instance.
    * <p/>
    * <p>If <code>newAppender</code> is already in the list of appenders, then it won't be added again.
    */
   public synchronized void addAppender(final Appender newAppender) {

      if (aai == null) {
         aai = new AppenderAttachableImpl();
      }
      aai.addAppender(newAppender);
      repository.fireAddAppenderEvent(this, newAppender);
   }


   /**
    * If <code>assertion</code> parameter is <code>false</code>, then logs <code>msg</code> as an {@link #error(Object)
    * error} statement.
    * <p/>
    * <p>The <code>assert</code> method has been renamed to <code>assertLog</code> because <code>assert</code> is a
    * language reserved word in JDK 1.4.
    *
    * @param assertion
    * @param msg       The message to print if <code>assertion</code> is false.
    * @since 1.2
    */
   public void assertLog(final boolean assertion, final String msg) {

      if (!assertion) {
         this.error(msg);
      }
   }


   /**
    * Call the appenders in the hierarchy starting at <code>this</code>.  If no appenders could be found, emit a
    * warning.
    * <p/>
    * <p>This method calls all the appenders inherited from the hierarchy circumventing any evaluation of whether to log
    * or not to log the particular log request.
    *
    * @param event the event to log.
    * @noinspection SynchronizationOnLocalVariableOrMethodParameter
    */
   public void callAppenders(final LoggingEvent event) {

      int writes = 0;

      for (Category c = this; c != null; c = c.parent) {
         // Protected against simultaneous call to addAppender, removeAppender,...
         synchronized (c) {
            if (c.aai != null) {
               writes += c.aai.appendLoopOnAppenders(event);
            }
            if (!c.additive) {
               break;
            }
         }
      }

      if (writes == 0) {
         repository.emitNoAppenderWarning(this);
      }
   }


   /**
    * Close all attached appenders implementing the AppenderAttachable interface.
    *
    * @since 1.0
    */
   final synchronized void closeNestedAppenders() {

      final Enumeration enumeration = this.getAllAppenders();
      if (enumeration != null) {
         while (enumeration.hasMoreElements()) {
            final Appender a = (Appender) enumeration.nextElement();
            if (a instanceof AppenderAttachable) {
               a.close();
            }
         }
      }
   }


   /**
    * Log a message object with the {@link Level#DEBUG DEBUG} level.
    * <p/>
    * <p>This method first checks if this category is <code>DEBUG</code> enabled by comparing the level of this category
    * with the {@link Level#DEBUG DEBUG} level. If this category is <code>DEBUG</code> enabled, then it converts the
    * message object (passed as parameter) to a string by invoking the appropriate {@link ObjectRenderer}. It then
    * proceeds to call all the registered appenders in this category and also higher in the hierarchy depending on the
    * value of the additivity flag.
    * <p/>
    * <p><b>WARNING</b> Note that passing a {@link Throwable} to this method will print the name of the
    * <code>Throwable</code> but no stack trace. To print a stack trace use the {@link #debug(Object, Throwable)} form
    * instead.
    *
    * @param message the message object to log.
    */
   public void debug(final Object message) {

      if (repository.isDisabled(Priority.DEBUG_INT)) {
         return;
      }
      if (Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.DEBUG, message, null);
      }
   }


   /**
    * Log a message object with the <code>DEBUG</code> level including the stack trace of the {@link Throwable}
    * <code>t</code> passed as parameter.
    * <p/>
    * <p>See {@link #debug(Object)} form for more detailed information.
    *
    * @param message the message object to log.
    * @param t       the exception to log, including its stack trace.
    */
   public void debug(final Object message, final Throwable t) {

      if (repository.isDisabled(Priority.DEBUG_INT)) {
         return;
      }
      if (Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.DEBUG, message, t);
      }
   }


   /**
    * Log a message object with the {@link Level#ERROR ERROR} Level.
    * <p/>
    * <p>This method first checks if this category is <code>ERROR</code> enabled by comparing the level of this category
    * with {@link Level#ERROR ERROR} Level. If this category is <code>ERROR</code> enabled, then it converts the message
    * object passed as parameter to a string by invoking the appropriate {@link ObjectRenderer}. It proceeds to call all
    * the registered appenders in this category and also higher in the hierarchy depending on the value of the
    * additivity flag.
    * <p/>
    * <p><b>WARNING</b> Note that passing a {@link Throwable} to this method will print the name of the
    * <code>Throwable</code> but no stack trace. To print a stack trace use the {@link #error(Object, Throwable)} form
    * instead.
    *
    * @param message the message object to log
    */
   public void error(final Object message) {

      if (repository.isDisabled(Priority.ERROR_INT)) {
         return;
      }
      if (Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.ERROR, message, null);
      }
   }


   /**
    * Log a message object with the <code>ERROR</code> level including the stack trace of the {@link Throwable}
    * <code>t</code> passed as parameter.
    * <p/>
    * <p>See {@link #error(Object)} form for more detailed information.
    *
    * @param message the message object to log.
    * @param t       the exception to log, including its stack trace.
    */
   public void error(final Object message, final Throwable t) {

      if (repository.isDisabled(Priority.ERROR_INT)) {
         return;
      }
      if (Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.ERROR, message, t);
      }

   }


   /**
    * If the named category exists (in the default hierarchy) then it returns a reference to the category, otherwise it
    * returns <code>null</code>.
    *
    * @since 0.8.5
    * @deprecated Please use {@link LogManager#exists} instead.
    */
   public
   static Logger exists(final String name) {

      return LogManager.exists(name);
   }


   /**
    * Log a message object with the {@link Level#FATAL FATAL} Level.
    * <p/>
    * <p>This method first checks if this category is <code>FATAL</code> enabled by comparing the level of this category
    * with {@link Level#FATAL FATAL} Level. If the category is <code>FATAL</code> enabled, then it converts the message
    * object passed as parameter to a string by invoking the appropriate {@link ObjectRenderer}. It proceeds to call all
    * the registered appenders in this category and also higher in the hierarchy depending on the value of the
    * additivity flag.
    * <p/>
    * <p><b>WARNING</b> Note that passing a {@link Throwable} to this method will print the name of the Throwable but no
    * stack trace. To print a stack trace use the {@link #fatal(Object, Throwable)} form instead.
    *
    * @param message the message object to log
    */
   public void fatal(final Object message) {

      if (repository.isDisabled(Priority.FATAL_INT)) {
         return;
      }
      if (Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.FATAL, message, null);
      }
   }


   /**
    * Log a message object with the <code>FATAL</code> level including the stack trace of the {@link Throwable}
    * <code>t</code> passed as parameter.
    * <p/>
    * <p>See {@link #fatal(Object)} for more detailed information.
    *
    * @param message the message object to log.
    * @param t       the exception to log, including its stack trace.
    */
   public void fatal(final Object message, final Throwable t) {

      if (repository.isDisabled(Priority.FATAL_INT)) {
         return;
      }
      if (Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.FATAL, message, t);
      }
   }


   /**
    * This method creates a new logging event and logs the event without further checks.
    */
   protected final void forcedLog(final String fqcn, final Priority level, final Object message,
                                  final Throwable t) {

      callAppenders(new LoggingEvent(fqcn, this, level, message, t));
   }


   /**
    * Get the additivity flag for this Category instance.
    */
   public final boolean getAdditivity() {

      return additive;
   }


   /**
    * Get the appenders contained in this category as an {@link Enumeration}. If no appenders can be found, then a
    * {@link NullEnumeration} is returned.
    *
    * @return Enumeration An enumeration of the appenders in this category.
    */
   public synchronized Enumeration getAllAppenders() {

      if (aai == null) {
         return NullEnumeration.getInstance();
      } else {
         return aai.getAllAppenders();
      }
   }


   /**
    * Look for the appender named as <code>name</code>.
    * <p/>
    * <p>Return the appender with that name if in the list. Return <code>null</code> otherwise.
    */
   public synchronized Appender getAppender(final String name) {

      if (aai == null || name == null) {
         return null;
      }

      return aai.getAppender(name);
   }


   /**
    * Starting from this category, search the category hierarchy for a non-null level and return it. Otherwise, return
    * the level of the root category.
    * <p/>
    * <p>The Category class is designed so that this method executes as quickly as possible.
    */
   public Level getEffectiveLevel() {

      for (Category c = this; c != null; c = c.parent) {
         if (c.level != null) {
            return c.level;
         }
      }
      return null; // If reached will cause an NullPointerException.
   }


   /**
    * @deprecated Please use the the {@link #getEffectiveLevel} method instead.
    */
   public Priority getChainedPriority() {

      for (Category c = this; c != null; c = c.parent) {
         if (c.level != null) {
            return c.level;
         }
      }
      return null; // If reached will cause an NullPointerException.
   }


   /**
    * Returns all the currently defined categories in the default hierarchy as an {@link Enumeration Enumeration}.
    * <p/>
    * <p>The root category is <em>not</em> included in the returned {@link Enumeration}.
    *
    * @deprecated Please use {@link LogManager#getCurrentLoggers()} instead.
    */
   public
   static Enumeration getCurrentCategories() {

      return LogManager.getCurrentLoggers();
   }


   /**
    * Return the default Hierarchy instance.
    *
    * @since 1.0
    * @deprecated Please use {@link LogManager#getLoggerRepository()} instead.
    */
   public
   static LoggerRepository getDefaultHierarchy() {

      return LogManager.getLoggerRepository();
   }


   /**
    * Return the the {@link Hierarchy} where this <code>Category</code> instance is attached.
    *
    * @since 1.1
    * @deprecated Please use {@link #getLoggerRepository} instead.
    */
   public LoggerRepository getHierarchy() {

      return repository;
   }


   /**
    * Return the the {@link LoggerRepository} where this <code>Category</code> is attached.
    *
    * @since 1.2
    */
   public final LoggerRepository getLoggerRepository() {

      return repository;
   }


   /**
    * @deprecated Make sure to use {@link Logger#getLogger(String)} instead.
    */
   public
   static Category getInstance(final String name) {

      return LogManager.getLogger(name);
   }


   /**
    * @deprecated Please make sure to use {@link Logger#getLogger(Class)} instead.
    */
   public
   static Category getInstance(final Class clazz) {

      return LogManager.getLogger(clazz);
   }


   /**
    * Return the category name.
    */
   public
   final String getName() {

      return name;
   }


   /**
    * Returns the parent of this category. Note that the parent of a given category may change during the lifetime of
    * the category.
    * <p/>
    * <p>The root category will return <code>null</code>.
    *
    * @since 1.2
    */
   public final Category getParent() {

      return this.parent;
   }


   /**
    * Returns the assigned {@link Level}, if any, for this Category.
    *
    * @return Level - the assigned Level, can be <code>null</code>.
    */
   public final Level getLevel() {

      return this.level;
   }


   /**
    * @deprecated Please use {@link #getLevel} instead.
    */
   public final Level getPriority() {

      return this.level;
   }


   /**
    * @deprecated Please use {@link Logger#getRootLogger()} instead.
    */
   public static Category getRoot() {

      return LogManager.getRootLogger();
   }


   /**
    * Return the <em>inherited</em> {@link ResourceBundle} for this category.
    * <p/>
    * <p>This method walks the hierarchy to find the appropriate resource bundle. It will return the resource bundle
    * attached to the closest ancestor of this category, much like the way priorities are searched. In case there is no
    * bundle in the hierarchy then <code>null</code> is returned.
    *
    * @since 0.9.0
    */
   public ResourceBundle getResourceBundle() {

      for (Category c = this; c != null; c = c.parent) {
         if (c.resourceBundle != null) {
            return c.resourceBundle;
         }
      }
      // It might be the case that there is no resource bundle
      return null;
   }


   /**
    * Returns the string resource corresponding to <code>key</code> in this category's inherited resource bundle. See
    * also {@link #getResourceBundle}.
    * <p/>
    * <p>If the resource cannot be found, then an {@link #error error} message will be logged complaining about the
    * missing resource.
    */
   protected final String getResourceBundleString(final String key) {

      final ResourceBundle rb = getResourceBundle();
      // This is one of the rare cases where we can use logging in order
      // to report errors from within log4j.
      if (rb == null) {
         //if(!hierarchy.emittedNoResourceBundleWarning) {
         //error("No resource bundle has been set for category "+name);
         //hierarchy.emittedNoResourceBundleWarning = true;
         //}
         return null;
      } else {
         try {
            return rb.getString(key);
         } catch (final MissingResourceException ignored) {
            error("No resource is associated with key \"" + key + "\".");
            return null;
         }
      }
   }


   /**
    * Log a message object with the {@link Level#INFO INFO} Level.
    * <p/>
    * <p>This method first checks if this category is <code>INFO</code> enabled by comparing the level of this category
    * with {@link Level#INFO INFO} Level. If the category is <code>INFO</code> enabled, then it converts the message
    * object passed as parameter to a string by invoking the appropriate {@link ObjectRenderer}. It proceeds to call all
    * the registered appenders in this category and also higher in the hierarchy depending on the value of the
    * additivity flag.
    * <p/>
    * <p><b>WARNING</b> Note that passing a {@link Throwable} to this method will print the name of the Throwable but no
    * stack trace. To print a stack trace use the {@link #info(Object, Throwable)} form instead.
    *
    * @param message the message object to log
    */
   public void info(final Object message) {

      if (repository.isDisabled(Priority.INFO_INT)) {
         return;
      }
      if (Level.INFO.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.INFO, message, null);
      }
   }


   /**
    * Log a message object with the <code>INFO</code> level including the stack trace of the {@link Throwable}
    * <code>t</code> passed as parameter.
    * <p/>
    * <p>See {@link #info(Object)} for more detailed information.
    *
    * @param message the message object to log.
    * @param t       the exception to log, including its stack trace.
    */
   public void info(final Object message, final Throwable t) {

      if (repository.isDisabled(Priority.INFO_INT)) {
         return;
      }
      if (Level.INFO.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.INFO, message, t);
      }
   }


   /**
    * Is the appender passed as parameter attached to this category?
    */
   public boolean isAttached(final Appender appender) {

      if (appender == null) {
         return false;
      }

      // Protect access to aai
      synchronized (this) {
         return aai != null && aai.isAttached(appender);
      }
   }


   /**
    * Check whether this category is enabled for the <code>DEBUG</code> Level.
    * <p/>
    * <p> This function is intended to lessen the computational cost of disabled log debug statements.
    * <p/>
    * <p> For some <code>cat</code> Category object, when you write,
    * <pre>
    *      cat.debug("This is entry number: " + i );
    *  </pre>
    * <p/>
    * <p>You incur the cost constructing the message, concatenation in this case, regardless of whether the message is
    * logged or not.
    * <p/>
    * <p>If you are worried about speed, then you should write
    * <pre>
    * 	 if(cat.isDebugEnabled()) {
    * 	   cat.debug("This is entry number: " + i );
    *     }
    *  </pre>
    * <p/>
    * <p>This way you will not incur the cost of parameter construction if debugging is disabled for <code>cat</code>.
    * On the other hand, if the <code>cat</code> is debug enabled, you will incur the cost of evaluating whether the
    * category is debug enabled twice. Once in <code>isDebugEnabled</code> and once in the <code>debug</code>.  This is
    * an insignificant overhead since evaluating a category takes about 1%% of the time it takes to actually log.
    *
    * @return boolean - <code>true</code> if this category is debug enabled, <code>false</code> otherwise.
    */
   public boolean isDebugEnabled() {

      return !repository.isDisabled(Priority.DEBUG_INT) && Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel());
   }


   /**
    * Check whether this category is enabled for a given {@link Level} passed as parameter.
    * <p/>
    * See also {@link #isDebugEnabled}.
    *
    * @return boolean True if this category is enabled for <code>level</code>.
    */
   public boolean isEnabledFor(final Priority level) {

      return !repository.isDisabled(level.level) && level.isGreaterOrEqual(this.getEffectiveLevel());
   }


   /**
    * Check whether this category is enabled for the info Level. See also {@link #isDebugEnabled}.
    *
    * @return boolean - <code>true</code> if this category is enabled for level info, <code>false</code> otherwise.
    */
   public boolean isInfoEnabled() {

      return !repository.isDisabled(Priority.INFO_INT) && Level.INFO.isGreaterOrEqual(this.getEffectiveLevel());
   }


   /**
    * Log a localized message. The user supplied parameter <code>key</code> is replaced by its localized version from
    * the resource bundle.
    *
    * @see #setResourceBundle
    * @since 0.8.4
    */
   public void l7dlog(final Priority priority, final String key, final Throwable t) {

      if (repository.isDisabled(priority.level)) {
         return;
      }
      if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
         String msg = getResourceBundleString(key);
         // if message corresponding to 'key' could not be found in the
         // resource bundle, then default to 'key'.
         if (msg == null) {
            msg = key;
         }
         forcedLog(FQCN, priority, msg, t);
      }
   }


   /**
    * Log a localized and parameterized message. First, the user supplied <code>key</code> is searched in the resource
    * bundle. Next, the resulting pattern is formatted using {@link MessageFormat#format(String, Object[])} method with
    * the user supplied object array <code>params</code>.
    *
    * @since 0.8.4
    */
   public void l7dlog(final Priority priority, final String key, final Object[] params,
                      final Throwable t) {

      if (repository.isDisabled(priority.level)) {
         return;
      }
      if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
         final String pattern = getResourceBundleString(key);
         final String msg;
         if (pattern == null) {
            msg = key;
         } else {
            msg = MessageFormat.format(pattern, params);
         }
         forcedLog(FQCN, priority, msg, t);
      }
   }


   /**
    * This generic form is intended to be used by wrappers.
    */
   public void log(final Priority priority, final Object message, final Throwable t) {

      if (repository.isDisabled(priority.level)) {
         return;
      }
      if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, priority, message, t);
      }
   }


   /**
    * This generic form is intended to be used by wrappers.
    */
   public void log(final Priority priority, final Object message) {

      if (repository.isDisabled(priority.level)) {
         return;
      }
      if (priority.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, priority, message, null);
      }
   }


   /**
    * This is the most generic printing method. It is intended to be invoked by <b>wrapper</b> classes.
    *
    * @param callerFQCN The wrapper class' fully qualified class name.
    * @param level      The level of the logging request.
    * @param message    The message of the logging request.
    * @param t          The throwable of the logging request, may be null.
    */
   public void log(final String callerFQCN, final Priority level, final Object message,
                   final Throwable t) {

      if (repository.isDisabled(level.level)) {
         return;
      }
      if (level.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(callerFQCN, level, message, t);
      }
   }


   /**
    * LoggerRepository forgot the fireRemoveAppenderEvent method, if using the stock Hierarchy implementation, then call
    * its fireRemove. Custom repositories can implement HierarchyEventListener if they want remove notifications.
    *
    * @param appender appender, may be null.
    */
   private void fireRemoveAppenderEvent(final Appender appender) {

      if (appender != null) {
         if (repository instanceof Hierarchy) {
            ((Hierarchy) repository).fireRemoveAppenderEvent(this, appender);
         } else if (repository instanceof HierarchyEventListener) {
            ((HierarchyEventListener) repository).removeAppenderEvent(this, appender);
         }
      }
   }


   /**
    * Remove all previously added appenders from this Category instance.
    * <p/>
    * <p>This is useful when re-reading configuration information.
    */
   public synchronized void removeAllAppenders() {

      if (aai != null) {
         final Vector appenders = new Vector(11);
         for (final Enumeration iter = aai.getAllAppenders(); iter != null && iter.hasMoreElements();) {
            appenders.add(iter.nextElement());
         }
         aai.removeAllAppenders();
         for (final Enumeration iter = appenders.elements(); iter.hasMoreElements();) {
            fireRemoveAppenderEvent((Appender) iter.nextElement());
         }
         aai = null;
      }
   }


   /**
    * Remove the appender passed as parameter form the list of appenders.
    *
    * @since 0.8.2
    */
   public synchronized void removeAppender(final Appender appender) {

      if (appender == null || aai == null) {
         return;
      }
      final boolean wasAttached = aai.isAttached(appender);
      aai.removeAppender(appender);
      if (wasAttached) {
         fireRemoveAppenderEvent(appender);
      }
   }


   /**
    * Remove the appender with the name passed as parameter form the list of appenders.
    *
    * @since 0.8.2
    */
   public synchronized void removeAppender(final String name) {

      if (name == null || aai == null) {
         return;
      }
      final Appender appender = aai.getAppender(name);
      aai.removeAppender(name);
      if (appender != null) {
         fireRemoveAppenderEvent(appender);
      }
   }


   /**
    * Set the additivity flag for this Category instance.
    *
    * @since 0.8.1
    */
   public final void setAdditivity(final boolean additive) {

      this.additive = additive;
   }


   /**
    * Only the hierarchy class can set the hiearchy of a category. Default package access is MANDATORY here.
    */
   final void setHierarchy(final LoggerRepository repository) {

      this.repository = repository;
   }


   /**
    * Set the level of this Category. If you are passing any of <code>Level.DEBUG</code>, <code>Level.INFO</code>,
    * <code>Level.WARN</code>, <code>Level.ERROR</code>, <code>Level.FATAL</code> as a parameter, you need to case them
    * as Level.
    * <p/>
    * <p>As in <pre> &nbsp;&nbsp;&nbsp;logger.setLevel((Level) Level.DEBUG); </pre>
    * <p/>
    * <p/>
    * <p>Null values are admitted.
    */
   public void setLevel(final Level level) {

      this.level = level;
   }


   /**
    * Set the level of this Category.
    * <p/>
    * <p>Null values are admitted.
    *
    * @deprecated Please use {@link #setLevel} instead.
    */
   public void setPriority(final Priority priority) {

      this.level = (Level) priority;
   }


   /**
    * Set the resource bundle to be used with localized logging methods {@link #l7dlog(Priority, String, Throwable)} and
    * {@link #l7dlog(Priority, String, Object[], Throwable)}.
    *
    * @since 0.8.4
    */
   public void setResourceBundle(final ResourceBundle bundle) {

      resourceBundle = bundle;
   }


   /**
    * Calling this method will <em>safely</em> close and remove all appenders in all the categories including root
    * contained in the default hierarchy.
    * <p/>
    * <p>Some appenders such as {@link SocketAppender} and {@link AsyncAppender} need to be closed before the
    * application exists. Otherwise, pending logging events might be lost.
    * <p/>
    * <p>The <code>shutdown</code> method is careful to close nested appenders before closing regular appenders. This is
    * allows configurations where a regular appender is attached to a category and again to a nested appender.
    *
    * @since 1.0
    * @deprecated Please use {@link LogManager#shutdown()} instead.
    */
   public
   static void shutdown() {

      LogManager.shutdown();
   }


   /**
    * Log a message object with the {@link Level#WARN WARN} Level.
    * <p/>
    * <p>This method first checks if this category is <code>WARN</code> enabled by comparing the level of this category
    * with {@link Level#WARN WARN} Level. If the category is <code>WARN</code> enabled, then it converts the message
    * object passed as parameter to a string by invoking the appropriate {@link ObjectRenderer}. It proceeds to call all
    * the registered appenders in this category and also higher in the hierarchy depending on the value of the
    * additivity flag.
    * <p/>
    * <p><b>WARNING</b> Note that passing a {@link Throwable} to this method will print the name of the Throwable but no
    * stack trace. To print a stack trace use the {@link #warn(Object, Throwable)} form instead.  <p>
    *
    * @param message the message object to log.
    */
   public void warn(final Object message) {

      if (repository.isDisabled(Priority.WARN_INT)) {
         return;
      }

      if (Level.WARN.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.WARN, message, null);
      }
   }


   /**
    * Log a message with the <code>WARN</code> level including the stack trace of the {@link Throwable} <code>t</code>
    * passed as parameter.
    * <p/>
    * <p>See {@link #warn(Object)} for more detailed information.
    *
    * @param message the message object to log.
    * @param t       the exception to log, including its stack trace.
    */
   public void warn(final Object message, final Throwable t) {

      if (repository.isDisabled(Priority.WARN_INT)) {
         return;
      }
      if (Level.WARN.isGreaterOrEqual(this.getEffectiveLevel())) {
         forcedLog(FQCN, Level.WARN, message, t);
      }
   }
}
