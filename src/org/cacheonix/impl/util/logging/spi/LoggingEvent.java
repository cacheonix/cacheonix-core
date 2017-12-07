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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.cacheonix.impl.util.logging.Category;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.MDC;
import org.cacheonix.impl.util.logging.NDC;
import org.cacheonix.impl.util.logging.Priority;
import org.cacheonix.impl.util.logging.helpers.Loader;
import org.cacheonix.impl.util.logging.helpers.LogLog;

// Contributors:   Nelson Minar <nelson@monkey.org>
//                 Wolf Siberski
//                 Anders Kristensen <akristensen@dynamicsoft.com>

/**
 * The internal representation of logging events. When an affirmative decision is made to log then a
 * <code>LoggingEvent</code> instance is created. This instance is passed around to the different log4j components.
 * <p/>
 * <p>This class is of concern to those wishing to extend log4j.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author James P. Cakalic
 * @since 0.8.2
 */
@SuppressWarnings("deprecation")
public final class LoggingEvent implements Serializable {

   private static final long startTime = System.currentTimeMillis();

   /**
    * Fully qualified name of the calling category class.
    */
   public final transient String fqnOfCategoryClass;

   /**
    * The category of the logging event. This field is not serialized for performance reasons.
    * <p/>
    * <p>It is set by the LoggingEvent constructor or set by a remote entity after deserialization.
    *
    * @deprecated This field will be marked as private or be completely removed in future releases. Please do not use
    *             it.
    */
   private final transient Category logger;

   /**
    * <p>The category (logger) name.
    */
   public final String categoryName;

   /**
    * Level of logging event. Level cannot be serializable because it is a flyweight.  Due to its special serialization
    * it cannot be declared final either.
    * <p/>
    * <p> This field should not be accessed directly. You should use the {@link #getLevel} method instead.
    *
    * @deprecated This field will be marked as private in future releases. Please do not access it directly. Use the
    *             {@link #getLevel} method instead.
    */
   public transient Priority level;

   /**
    * The nested diagnostic context (NDC) of logging event.
    */
   private String ndc = null;

   /**
    * The mapped diagnostic context (MDC) of logging event.
    */
   private Hashtable mdcCopy = null;


   /**
    * Have we tried to do an NDC lookup? If we did, there is no need to do it again.  Note that its value is always
    * <code>false</code> when serialized. Thus, a receiving SocketNode will never use it's own (incorrect) NDC. See also
    * writeObject method.
    */
   private boolean ndcLookupRequired = true;


   /**
    * Have we tried to do an MDC lookup? If we did, there is no need to do it again.  Note that its value is always
    * <code>false</code> when serialized. See also the getMDC and getMDCCopy methods.
    */
   private boolean mdcCopyLookupRequired = true;

   /**
    * The application supplied message of logging event.
    */
   private final transient Object message;

   /**
    * The application supplied message rendered through the log4j object rendering mechanism.
    */
   private String renderedMessage = null;

   /**
    * The name of thread in which this logging event was generated.
    */
   private String threadName = null;


   /**
    * This variable contains information about this event's throwable
    */
   private ThrowableInformation throwableInfo = null;

   /**
    * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
    */
   public final long timeStamp;

   /**
    * Location information for the caller.
    */
   private LocationInfo locationInfo = null;

   // Serialization
   private static final long serialVersionUID = -868428216207166145L;

   static final Integer[] PARAM_ARRAY = new Integer[1];

   static final String TO_LEVEL = "toLevel";

   static final Class[] TO_LEVEL_PARAMS = {int.class};

   static final Hashtable methodCache = new Hashtable(3); // use a tiny table


   /**
    * Instantiate a LoggingEvent from the supplied parameters.
    * <p/>
    * <p>Except {@link #timeStamp} all the other fields of <code>LoggingEvent</code> are filled when actually needed.
    *
    * @param logger    The logger generating this event.
    * @param level     The level of this event.
    * @param message   The message of this event.
    * @param throwable The throwable of this event.
    */
   public LoggingEvent(final String fqnOfCategoryClass, final Category logger,
                       final Priority level, final Object message, final Throwable throwable) {

      this.fqnOfCategoryClass = fqnOfCategoryClass;
      this.logger = logger;
      this.categoryName = logger.getName();
      this.level = level;
      this.message = message;
      if (throwable != null) {
         this.throwableInfo = new ThrowableInformation(throwable);
      }
      timeStamp = System.currentTimeMillis();
   }


   /**
    * Instantiate a LoggingEvent from the supplied parameters.
    * <p/>
    * <p>Except {@link #timeStamp} all the other fields of <code>LoggingEvent</code> are filled when actually needed.
    *
    * @param logger    The logger generating this event.
    * @param timeStamp the timestamp of this logging event
    * @param level     The level of this event.
    * @param message   The message of this event.
    * @param throwable The throwable of this event.
    */
   public LoggingEvent(final String fqnOfCategoryClass, final Category logger,
                       final long timeStamp, final Priority level, final Object message,
                       final Throwable throwable) {

      this.fqnOfCategoryClass = fqnOfCategoryClass;
      this.logger = logger;
      this.categoryName = logger.getName();
      this.level = level;
      this.message = message;
      if (throwable != null) {
         this.throwableInfo = new ThrowableInformation(throwable);
      }

      this.timeStamp = timeStamp;
   }


   /**
    * Create new instance.
    *
    * @param fqnOfCategoryClass Fully qualified class name of Logger implementation.
    * @param logger             The logger generating this event.
    * @param timeStamp          the timestamp of this logging event
    * @param level              The level of this event.
    * @param message            The message of this event.
    * @param threadName         thread name
    * @param throwable          The throwable of this event.
    * @param ndc                Nested diagnostic context
    * @param info               Location info
    * @param properties         MDC properties
    * @since 1.2.15
    */
   public LoggingEvent(final String fqnOfCategoryClass,
                       final Category logger,
                       final long timeStamp,
                       final Level level,
                       final Object message,
                       final String threadName,
                       final ThrowableInformation throwable,
                       final String ndc,
                       final LocationInfo info,
                       final Map properties) {

      this.fqnOfCategoryClass = fqnOfCategoryClass;
      this.logger = logger;
      if (logger != null) {
         categoryName = logger.getName();
      } else {
         categoryName = null;
      }
      this.level = level;
      this.message = message;
      if (throwable != null) {
         this.throwableInfo = throwable;
      }

      this.timeStamp = timeStamp;
      this.threadName = threadName;
      ndcLookupRequired = false;
      this.ndc = ndc;
      this.locationInfo = info;
      mdcCopyLookupRequired = false;
      if (properties != null) {
         mdcCopy = new Hashtable(properties);
      }
   }


   /**
    * Set the location information for this logging event. The collected information is cached for future use.
    */
   public final LocationInfo getLocationInformation() {

      if (locationInfo == null) {
         locationInfo = new LocationInfo(new Throwable(), fqnOfCategoryClass);
      }
      return locationInfo;
   }


   /**
    * Return the level of this event. Use this form instead of directly accessing the <code>level</code> field.
    */
   public final Level getLevel() {

      return (Level) level;
   }


   /**
    * Return the name of the logger. Use this form instead of directly accessing the <code>categoryName</code> field.
    */
   public final String getLoggerName() {

      return categoryName;
   }


   /**
    * Gets the logger of the event. Use should be restricted to cloning events.
    *
    * @since 1.2.15
    */
   public Category getLogger() {

      return logger;
   }


   /**
    * Return the message for this logging event.
    * <p/>
    * <p>Before serialization, the returned object is the message passed by the user to generate the logging event.
    * After serialization, the returned value equals the String form of the message possibly after object rendering.
    *
    * @since 1.1
    */
   public final Object getMessage() {

      if (message != null) {
         return message;
      } else {
         return getRenderedMessage();
      }
   }


   /**
    * This method returns the NDC for this event. It will return the correct content even if the event was generated in
    * a different thread or even on a different machine. The {@link NDC#get} method should <em>never</em> be called
    * directly.
    */
   public final String getNDC() {

      if (ndcLookupRequired) {
         ndcLookupRequired = false;
         ndc = NDC.get();
      }
      return ndc;
   }


   /**
    * Returns the the context corresponding to the <code>key</code> parameter. If there is a local MDC copy, possibly
    * because we are in a logging server or running inside AsyncAppender, then we search for the key in MDC copy, if a
    * value is found it is returned. Otherwise, if the search in MDC copy returns a null result, then the current
    * thread's <code>MDC</code> is used.
    * <p/>
    * <p>Note that <em>both</em> the local MDC copy and the current thread's MDC are searched.
    */
   public final Object getMDC(final String key) {
      // Note the mdcCopy is used if it exists. Otherwise we use the MDC
      // that is associated with the thread.
      if (mdcCopy != null) {
         final Object r = mdcCopy.get(key);
         if (r != null) {
            return r;
         }
      }
      return MDC.get(key);
   }


   /**
    * Obtain a copy of this thread's MDC prior to serialization or asynchronous logging.
    */
   public final void getMDCCopy() {

      if (mdcCopyLookupRequired) {
         mdcCopyLookupRequired = false;
         // the clone call is required for asynchronous logging.
         // See also bug #5932.
         final Hashtable t = MDC.getContext();
         if (t != null) {
            mdcCopy = (Hashtable) t.clone();
         }
      }
   }


   public final String getRenderedMessage() {

      if (renderedMessage == null && message != null) {
         if (message instanceof String) {
            renderedMessage = (String) message;
         } else {
            final LoggerRepository repository = logger.getLoggerRepository();

            if (repository instanceof RendererSupport) {
               final RendererSupport rs = (RendererSupport) repository;
               renderedMessage = rs.getRendererMap().findAndRender(message);
            } else {
               renderedMessage = message.toString();
            }
         }
      }
      return renderedMessage;
   }


   /**
    * Returns the time when the application started, in milliseconds elapsed since 01.01.1970.
    */
   public static long getStartTime() {

      return startTime;
   }


   public final String getThreadName() {

      if (threadName == null) {
         threadName = Thread.currentThread().getName();
      }
      return threadName;
   }


   /**
    * Returns the throwable information contained within this event. May be <code>null</code> if there is no such
    * information.
    * <p/>
    * <p>Note that the {@link Throwable} object contained within a {@link ThrowableInformation} does not survive
    * serialization.
    *
    * @since 1.1
    */
   public final ThrowableInformation getThrowableInformation() {

      return throwableInfo;
   }


   /**
    * Return this event's throwable's string[] representation.
    */
   public final String[] getThrowableStrRep() {

      if (throwableInfo == null) {
         return null;
      } else {
         return throwableInfo.getThrowableStrRep();
      }
   }


   private void readLevel(final ObjectInputStream ois)
           throws IOException {

      final int p = ois.readInt();
      try {
         final String className = (String) ois.readObject();
         if (className == null) {
            level = Level.toLevel(p);
         } else {
            Method m = (Method) methodCache.get(className);
            if (m == null) {
               final Class clazz = Loader.loadClass(className);
               // Note that we use Class.getDeclaredMethod instead of
               // Class.getMethod. This assumes that the Level subclass
               // implements the toLevel(int) method which is a
               // requirement. Actually, it does not make sense for Level
               // subclasses NOT to implement this method. Also note that
               // only Level can be subclassed and not Priority.
               m = clazz.getDeclaredMethod(TO_LEVEL, TO_LEVEL_PARAMS);
               methodCache.put(className, m);
            }
            PARAM_ARRAY[0] = Integer.valueOf(p);
            level = (Priority) m.invoke(null, (Object[]) PARAM_ARRAY);
         }
      } catch (final Exception e) {
         LogLog.warn("Level deserialization failed, reverting to default.", e);
         level = Level.toLevel(p);
      }
   }


   private void readObject(final ObjectInputStream ois)
           throws IOException, ClassNotFoundException {

      ois.defaultReadObject();
      readLevel(ois);

      // Make sure that no location info is available to Layouts
      if (locationInfo == null) {
         locationInfo = new LocationInfo(null, null);
      }
   }


   private void writeObject(final ObjectOutputStream oos) throws IOException {
      // Aside from returning the current thread name the wgetThreadName
      // method sets the threadName variable.
      this.getThreadName();

      // This sets the renders the message in case it wasn't up to now.
      this.getRenderedMessage();

      // This call has a side effect of setting this.ndc and
      // setting ndcLookupRequired to <code>false</code> if not already false.
      this.getNDC();

      // This call has a side effect of setting this.mdcCopy and
      // setting mdcLookupRequired to <code>false</code> if not already false.
      this.getMDCCopy();

      // This sets the throwable sting representation of the event throwable.
      this.getThrowableStrRep();

      oos.defaultWriteObject();

      // serialize this event's level
      writeLevel(oos);
   }


   private void writeLevel(final ObjectOutputStream oos) throws IOException {

      oos.writeInt(level.toInt());

      final Class clazz = level.getClass();
      if (clazz.equals(Level.class)) {
         oos.writeObject(null);
      } else {
         // writing directly the Class object would be nicer, except that
         // serialized a Class object can not be read back by JDK
         // 1.1.x. We have to resort to this hack instead.
         oos.writeObject(clazz.getName());
      }
   }


   /**
    * Set value for MDC property. This adds the specified MDC property to the event. Access to the MDC is not
    * synchronized, so this method should only be called when it is known that no other threads are accessing the MDC.
    *
    * @param propName
    * @param propValue
    * @since 1.2.15
    */
   public final void setProperty(final String propName,
                                 final String propValue) {

      if (mdcCopy == null) {
         getMDCCopy();
      }
      if (mdcCopy == null) {
         mdcCopy = new Hashtable(3);
      }
      mdcCopy.put(propName, propValue);
   }


   /**
    * Return a property for this event. The return value can be null.
    * <p/>
    * Equivalent to getMDC(String) in log4j 1.2.  Provided for compatibility with log4j 1.3.
    *
    * @param key property name
    * @return property value or null if property not set
    * @since 1.2.15
    */
   public final String getProperty(final String key) {

      final Object value = getMDC(key);
      String result = null;
      if (value != null) {
         result = value.toString();
      }
      return result;
   }


   /**
    * Check for the existence of location information without creating it (a byproduct of calling
    * getLocationInformation).
    *
    * @return <code>true</code> if location information has been extracted.
    * @since 1.2.15
    */
   public final boolean locationInformationExists() {

      return locationInfo != null;
   }


   /**
    * Getter for the event's time stamp. The time stamp is calculated starting from 1970-01-01 GMT.
    *
    * @return timestamp
    * @since 1.2.15
    */
   public final long getTimeStamp() {

      return timeStamp;
   }


   /**
    * Returns the set of the key values in the properties for the event.
    * <p/>
    * The returned set is unmodifiable by the caller.
    * <p/>
    * Provided for compatibility with log4j 1.3
    *
    * @return Set an unmodifiable set of the property keys.
    * @since 1.2.15
    */
   public final Set getPropertyKeySet() {

      return getProperties().keySet();
   }


   /**
    * Returns the set of properties for the event.
    * <p/>
    * The returned set is unmodifiable by the caller.
    * <p/>
    * Provided for compatibility with log4j 1.3
    *
    * @return Set an unmodifiable map of the properties.
    * @since 1.2.15
    */
   public final Map getProperties() {

      getMDCCopy();
      final Map properties;
      if (mdcCopy == null) {
         properties = new HashMap(3);
      } else {
         properties = mdcCopy;
      }
      return Collections.unmodifiableMap(properties);
   }


   /**
    * Get the fully qualified name of the calling logger sub-class/wrapper. Provided for compatibility with log4j 1.3
    *
    * @return fully qualified class name, may be null.
    * @since 1.2.15
    */
   public String getFQNOfLoggerClass() {

      return fqnOfCategoryClass;
   }


}
