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

// Contributors: "Luke Blanshard" <Luke@quiq.com>
//              "Mark DONSZELMANN" <Mark.Donszelmann@cern.ch>
//               Anders Kristensen <akristensen@dynamicsoft.com>

package org.cacheonix.impl.util.logging;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import org.cacheonix.impl.util.logging.config.PropertySetter;
import org.cacheonix.impl.util.logging.helpers.FileWatchdog;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.helpers.OptionConverter;
import org.cacheonix.impl.util.logging.or.ObjectRenderer;
import org.cacheonix.impl.util.logging.or.RendererMap;
import org.cacheonix.impl.util.logging.spi.Configurator;
import org.cacheonix.impl.util.logging.spi.ErrorHandler;
import org.cacheonix.impl.util.logging.spi.Filter;
import org.cacheonix.impl.util.logging.spi.LoggerFactory;
import org.cacheonix.impl.util.logging.spi.LoggerRepository;
import org.cacheonix.impl.util.logging.spi.OptionHandler;
import org.cacheonix.impl.util.logging.spi.RendererSupport;
import org.cacheonix.impl.util.logging.xml.DOMConfigurator;

/**
 * Allows the configuration of log4j from an external file.  See <b>{@link #doConfigure(String, LoggerRepository)}</b>
 * for the expected format.
 * <p/>
 * <p>It is sometimes useful to see how log4j is reading configuration files. You can enable log4j internal logging by
 * defining the <b>log4j.debug</b> variable.
 * <p/>
 * <P>As of log4j version 0.8.5, at class initialization time class, the file <b>cacheonix.logging.properties</b> will
 * be searched from the search path used to load classes. If the file can be found, then it will be fed to the {@link
 * PropertyConfigurator#configure(URL)} method.
 * <p/>
 * <p>The <code>PropertyConfigurator</code> does not handle the advanced configuration features supported by the {@link
 * DOMConfigurator DOMConfigurator} such as support for {@link Filter Filters}, custom {@link ErrorHandler
 * ErrorHandlers}, nested appenders such as the {@link AsyncAppender AsyncAppender}, etc.
 * <p/>
 * <p>All option <em>values</em> admit variable substitution. The syntax of variable substitution is similar to that of
 * Unix shells. The string between an opening <b>&quot;${&quot;</b> and closing <b>&quot;}&quot;</b> is interpreted as a
 * key. The value of the substituted variable can be defined as a system property or in the configuration file itself.
 * The value of the key is first searched in the system properties, and if not found there, it is then searched in the
 * configuration file being parsed.  The corresponding value replaces the ${variableName} sequence. For example, if
 * <code>java.home</code> system property is set to <code>/home/xyz</code>, then every occurrence of the sequence
 * <code>${java.home}</code> will be interpreted as <code>/home/xyz</code>.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 * @since 0.8.1
 */
public final class PropertyConfigurator implements Configurator {

   /**
    * Used internally to keep track of configured appenders.
    */
   private final Hashtable registry = new Hashtable(11);
   private LoggerFactory loggerFactory = new DefaultCategoryFactory();

   static final String CATEGORY_PREFIX = "log4j.category.";
   static final String LOGGER_PREFIX = "log4j.logger.";
   static final String FACTORY_PREFIX = "log4j.factory";
   static final String ADDITIVITY_PREFIX = "log4j.additivity.";
   static final String ROOT_CATEGORY_PREFIX = "log4j.rootCategory";
   static final String ROOT_LOGGER_PREFIX = "log4j.rootLogger";
   static final String APPENDER_PREFIX = "log4j.appender.";
   static final String RENDERER_PREFIX = "log4j.renderer.";
   static final String THRESHOLD_PREFIX = "log4j.threshold";

   /**
    * Key for specifying the {@link LoggerFactory LoggerFactory}.  Currently set to "<code>log4j.loggerFactory</code>".
    */
   public static final String LOGGER_FACTORY_KEY = "log4j.loggerFactory";

   /**
    * If property set to true, then hierarchy will be reset before configuration.
    */
   private static final String RESET_KEY = "log4j.reset";

   private static final String INTERNAL_ROOT_NAME = "root";


   /**
    * Read configuration from a file. <b>The existing configuration is not cleared nor reset.</b> If you require a
    * different behavior, then call {@link  LogManager#resetConfiguration resetConfiguration} method before calling
    * <code>doConfigure</code>. <p/> <p>The configuration file consists of statements in the format
    * <code>key=value</code>. The syntax of different configuration elements are discussed below. <p/>
    * <h3>Repository-wide threshold</h3> <p/> <p>The repository-wide threshold filters logging requests by level
    * regardless of logger. The syntax is: <p/>
    * <pre>
    * log4j.threshold=[level]
    * </pre>
    * <p/> <p>The level value can consist of the string values OFF, FATAL, ERROR, WARN, INFO, DEBUG, ALL or a <em>custom
    * level</em> value. A custom level value can be specified in the form level#classname. By default the
    * repository-wide threshold is set to the lowest possible value, namely the level <code>ALL</code>. </p>
    * <p/>
    * <p/>
    * <h3>Appender configuration</h3>
    * <p/>
    * <p>Appender configuration syntax is:
    * <pre>
    * # For appender named <i>appenderName</i>, set its class.
    * # Note: The appender name can contain dots.
    * log4j.appender.appenderName=fully.qualified.name.of.appender.class
    * <p/>
    * # Set appender specific options.
    * log4j.appender.appenderName.option1=value1
    * ...
    * log4j.appender.appenderName.optionN=valueN
    * </pre>
    * <p/>
    * For each named appender you can configure its {@link Layout}. The syntax for configuring an appender's layout is:
    * <pre>
    * log4j.appender.appenderName.layout=fully.qualified.name.of.layout.class
    * log4j.appender.appenderName.layout.option1=value1
    * ....
    * log4j.appender.appenderName.layout.optionN=valueN
    * </pre>
    * <p/>
    * <h3>Configuring loggers</h3>
    * <p/>
    * <p>The syntax for configuring the root logger is:
    * <pre>
    * log4j.rootLogger=[level], appenderName, appenderName, ...
    * </pre>
    * <p/>
    * <p>This syntax means that an optional <em>level</em> can be supplied followed by appender names separated by
    * commas.
    * <p/>
    * <p>The level value can consist of the string values OFF, FATAL, ERROR, WARN, INFO, DEBUG, ALL or a <em>custom
    * level</em> value. A custom level value can be specified in the form <code>level#classname</code>.
    * <p/>
    * <p>If a level value is specified, then the root level is set to the corresponding level.  If no level value is
    * specified, then the root level remains untouched.
    * <p/>
    * <p>The root logger can be assigned multiple appenders.
    * <p/>
    * <p>Each <i>appenderName</i> (separated by commas) will be added to the root logger. The named appender is defined
    * using the appender syntax defined above.
    * <p/>
    * <p>For non-root categories the syntax is almost the same:
    * <pre>
    * log4j.logger.logger_name=[level|INHERITED|NULL], appenderName, appenderName, ...
    * </pre>
    * <p/>
    * <p>The meaning of the optional level value is discussed above in relation to the root logger. In addition however,
    * the value INHERITED can be specified meaning that the named logger should inherit its level from the logger
    * hierarchy.
    * <p/>
    * <p>If no level value is supplied, then the level of the named logger remains untouched.
    * <p/>
    * <p>By default categories inherit their level from the hierarchy. However, if you set the level of a logger and
    * later decide that that logger should inherit its level, then you should specify INHERITED as the value for the
    * level value. NULL is a synonym for INHERITED.
    * <p/>
    * <p>Similar to the root logger syntax, each <i>appenderName</i> (separated by commas) will be attached to the named
    * logger.
    * <p/>
    * <p>See the <a href="../../../../manual.html#additivity">appender additivity rule</a> in the user manual for the
    * meaning of the <code>additivity</code> flag.
    * <p/>
    * <h3>ObjectRenderers</h3>
    * <p/>
    * You can customize the way message objects of a given type are converted to String before being logged. This is
    * done by specifying an {@link ObjectRenderer ObjectRenderer} for the object type would like to customize.
    * <p/>
    * <p>The syntax is:
    * <p/>
    * <pre>
    * log4j.renderer.fully.qualified.name.of.rendered.class=fully.qualified.name.of.rendering.class
    * </pre>
    * <p/>
    * As in,
    * <pre>
    * log4j.renderer.my.Fruit=my.FruitRenderer
    * </pre>
    * <p/>
    * <h3>Logger Factories</h3>
    * <p/>
    * The usage of custom logger factories is discouraged and no longer documented.
    * <p/>
    * <h3>Resetting Hierarchy</h3>
    * <p/>
    * The hierarchy will be reset before configuration when log4j.reset=true is present in the properties file.
    * <p/>
    * <h3>Example</h3>
    * <p/>
    * <p>An example configuration is given below. Other configuration file examples are given in the
    * <code>examples</code> folder.
    * <p/>
    * <pre>
    * <p/>
    * # Set options for appender named "A1".
    * # Appender "A1" will be a SyslogAppender
    * log4j.appender.A1=org.cacheonix.impl.util.logging.net.SyslogAppender
    * <p/>
    * # The syslog daemon resides on www.abc.net
    * log4j.appender.A1.SyslogHost=www.abc.net
    * <p/>
    * # A1's layout is a PatternLayout, using the conversion pattern
    * # <b>%r %-5p %c{2} %M.%L %x - %m\n</b>. Thus, the log output will
    * # include # the relative time since the start of the application in
    * # milliseconds, followed by the level of the log request,
    * # followed by the two rightmost components of the logger name,
    * # followed by the callers method name, followed by the line number,
    * # the nested diagnostic context and finally the message itself.
    * # Refer to the documentation of {@link PatternLayout} for further information
    * # on the syntax of the ConversionPattern key.
    * log4j.appender.A1.layout=org.cacheonix.impl.util.logging.PatternLayout
    * log4j.appender.A1.layout.ConversionPattern=%-4r %-5p %c{2} %M.%L %x - %m\n
    * <p/>
    * # Set options for appender named "A2"
    * # A2 should be a RollingFileAppender, with maximum file size of 10 MB
    * # using at most one backup file. A2's layout is TTCC, using the
    * # ISO8061 date format with context printing enabled.
    * log4j.appender.A2=org.cacheonix.impl.util.logging.RollingFileAppender
    * log4j.appender.A2.MaxFileSize=10MB
    * log4j.appender.A2.MaxBackupIndex=1
    * log4j.appender.A2.layout=org.cacheonix.impl.util.logging.TTCCLayout
    * log4j.appender.A2.layout.ContextPrinting=enabled
    * log4j.appender.A2.layout.DateFormat=ISO8601
    * <p/>
    * # Root logger set to DEBUG using the A2 appender defined above.
    * log4j.rootLogger=DEBUG, A2
    * <p/>
    * # Logger definitions:
    * # The SECURITY logger inherits is level from root. However, it's output
    * # will go to A1 appender defined above. It's additivity is non-cumulative.
    * log4j.logger.SECURITY=INHERIT, A1
    * log4j.additivity.SECURITY=false
    * <p/>
    * # Only warnings or above will be logged for the logger "SECURITY.access".
    * # Output will go to A1.
    * log4j.logger.SECURITY.access=WARN
    * <p/>
    * <p/>
    * # The logger "class.of.the.day" inherits its level from the
    * # logger hierarchy.  Output will go to the appender's of the root
    * # logger, A2 in this case.
    * log4j.logger.class.of.the.day=INHERIT
    * </pre>
    * <p/>
    * <p>Refer to the <b>setOption</b> method in each Appender and Layout for class specific options.
    * <p/>
    * <p>Use the <code>#</code> or <code>!</code> characters at the beginning of a line for comments.
    *
    * @param configFileName The name of the configuration file where the configuration information is stored.
    */
   public final void doConfigure(final String configFileName, final LoggerRepository hierarchy) {
      final Properties props = new Properties();
      FileInputStream istream = null;
      try {
         istream = new FileInputStream(configFileName);
         props.load(istream);
         istream.close();
      }
      catch (final Exception e) {
         LogLog.error("Could not read configuration file [" + configFileName + "].", e);
         LogLog.error("Ignoring configuration file [" + configFileName + "].");
         return;
      } finally {
         if (istream != null) {
            try {
               istream.close();
            } catch (final Throwable ignored) {
            }

         }
      }
      // If we reach here, then the config file is alright.
      doConfigure(props, hierarchy);
   }


   /**
    */
   public static void configure(final String configFilename) {
      new PropertyConfigurator().doConfigure(configFilename,
              LogManager.getLoggerRepository());
   }


   /**
    * Read configuration options from url <code>configURL</code>.
    *
    * @since 0.8.2
    */
   public
   static void configure(final URL configURL) {
      new PropertyConfigurator().doConfigure(configURL,
              LogManager.getLoggerRepository());
   }


   /**
    * Read configuration options from <code>properties</code>.
    * <p/>
    * See {@link #doConfigure(String, LoggerRepository)} for the expected format.
    */
   public static void configure(final Properties properties) {
      new PropertyConfigurator().doConfigure(properties,
              LogManager.getLoggerRepository());
   }


   /**
    * Like {@link #configureAndWatch(String, long)} except that the default delay as defined by {@link
    * FileWatchdog#DEFAULT_DELAY} is used.
    *
    * @param configFilename A file in key=value format.
    */
   public static void configureAndWatch(final String configFilename) {
      configureAndWatch(configFilename, FileWatchdog.DEFAULT_DELAY);
   }


   /**
    * Read the configuration file <code>configFilename</code> if it exists. Moreover, a thread will be created that will
    * periodically check if <code>configFilename</code> has been created or modified. The period is determined by the
    * <code>delay</code> argument. If a change or file creation is detected, then <code>configFilename</code> is read to
    * configure log4j.
    *
    * @param configFilename A file in key=value format.
    * @param delay          The delay in milliseconds to wait between each check.
    */
   public static void configureAndWatch(final String configFilename, final long delay) {
      final PropertyWatchdog pdog = new PropertyWatchdog(configFilename);
      pdog.setDelay(delay);
      pdog.start();
   }


   /**
    * Read configuration options from <code>properties</code>.
    * <p/>
    * See {@link #doConfigure(String, LoggerRepository)} for the expected format.
    */
   public final void doConfigure(final Properties properties, final LoggerRepository hierarchy) {
      String value = properties.getProperty(LogLog.DEBUG_KEY);
      if (value == null) {
         value = properties.getProperty("log4j.configDebug");
         if (value != null) {
            LogLog.warn("[log4j.configDebug] is deprecated. Use [log4j.debug] instead.");
         }
      }

      if (value != null) {
         LogLog.setInternalDebugging(OptionConverter.toBoolean(value, true));
      }

      //
      //   if log4j.reset=true then
      //        reset hierarchy
      final String reset = properties.getProperty(RESET_KEY);
      if (reset != null && OptionConverter.toBoolean(reset, false)) {
         hierarchy.resetConfiguration();
      }

      final String thresholdStr = OptionConverter.findAndSubst(THRESHOLD_PREFIX,
              properties);
      if (thresholdStr != null) {
         hierarchy.setThreshold(OptionConverter.toLevel(thresholdStr,
                 Level.ALL));
         LogLog.debug("Hierarchy threshold set to [" + hierarchy.getThreshold() + "].");
      }

      configureRootCategory(properties, hierarchy);
      configureLoggerFactory(properties);
      parseCatsAndRenderers(properties, hierarchy);

      LogLog.debug("Finished configuring.");
      // We don't want to hold references to appenders preventing their
      // garbage collection.
      registry.clear();
   }


   /**
    * Read configuration options from url <code>configURL</code>.
    */
   public final void doConfigure(final URL configURL, final LoggerRepository hierarchy) {
      final Properties props = new Properties();
      LogLog.debug("Reading configuration from URL " + configURL);
      InputStream istream = null;
      try {
         istream = configURL.openStream();
         props.load(istream);
      }
      catch (final Exception e) {
         LogLog.error("Could not read configuration file from URL [" + configURL
                 + "].", e);
         LogLog.error("Ignoring configuration file [" + configURL + "].");
         return;
      }
      finally {
         if (istream != null) {
            try {
               istream.close();
            } catch (final Exception ignored) {
            }
         }
      }
      doConfigure(props, hierarchy);
   }

   // --------------------------------------------------------------------------
   // Internal stuff
   // --------------------------------------------------------------------------


   /**
    * Check the provided <code>Properties</code> object for a {@link LoggerFactory LoggerFactory} entry specified by
    * {@link #LOGGER_FACTORY_KEY}.  If such an entry exists, an attempt is made to create an instance using the default
    * constructor.  This instance is used for subsequent Category creations within this configurator.
    *
    * @see #parseCatsAndRenderers
    */
   private final void configureLoggerFactory(final Properties props) {
      final String factoryClassName = OptionConverter.findAndSubst(LOGGER_FACTORY_KEY,
              props);
      if (factoryClassName != null) {
         LogLog.debug("Setting category factory to [" + factoryClassName + "].");
         loggerFactory = (LoggerFactory)
                 OptionConverter.instantiateByClassName(factoryClassName,
                         LoggerFactory.class,
                         loggerFactory);
         PropertySetter.setProperties(loggerFactory, props, FACTORY_PREFIX + '.');
      }
   }

   /*
   void configureOptionHandler(OptionHandler oh, String prefix,
                Properties props) {
     String[] options = oh.getOptionStrings();
     if(options == null)
       return;

     String value;
     for(int i = 0; i < options.length; i++) {
       value =  OptionConverter.findAndSubst(prefix + options[i], props);
       LogLog.debug(
          "Option " + options[i] + "=[" + (value == null? "N/A" : value)+"].");
       // Some option handlers assume that null value are not passed to them.
       // So don't remove this check
       if(value != null) {
    oh.setOption(options[i], value);
       }
     }
     oh.activateOptions();
   }
   */


   final void configureRootCategory(final Properties props, final LoggerRepository hierarchy) {
      String effectivePrefix = ROOT_LOGGER_PREFIX;
      String value = OptionConverter.findAndSubst(ROOT_LOGGER_PREFIX, props);

      if (value == null) {
         value = OptionConverter.findAndSubst(ROOT_CATEGORY_PREFIX, props);
         effectivePrefix = ROOT_CATEGORY_PREFIX;
      }

      if (value == null) {
         LogLog.debug("Could not find root logger information. Is this OK?");
      } else {
         final Logger root = hierarchy.getRootLogger();
         //noinspection SynchronizationOnLocalVariableOrMethodParameter
         synchronized (root) {
            parseCategory(props, root, effectivePrefix, INTERNAL_ROOT_NAME, value);
         }
      }
   }


   /**
    * Parse non-root elements, such non-root categories and renderers.
    */
   private final void parseCatsAndRenderers(final Properties props, final LoggerRepository hierarchy) {
      final Enumeration enumeration = props.propertyNames();
      while (enumeration.hasMoreElements()) {
         final String key = (String) enumeration.nextElement();
         if (key.startsWith(CATEGORY_PREFIX) || key.startsWith(LOGGER_PREFIX)) {
            String loggerName = null;
            if (key.startsWith(CATEGORY_PREFIX)) {
               loggerName = key.substring(CATEGORY_PREFIX.length());
            } else if (key.startsWith(LOGGER_PREFIX)) {
               loggerName = key.substring(LOGGER_PREFIX.length());
            }
            final String value = OptionConverter.findAndSubst(key, props);
            final Logger logger = hierarchy.getLogger(loggerName, loggerFactory);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (logger) {
               parseCategory(props, logger, key, loggerName, value);
               parseAdditivityForLogger(props, logger, loggerName);
            }
         } else if (key.startsWith(RENDERER_PREFIX)) {
            if (hierarchy instanceof RendererSupport) {
               final String renderingClass = OptionConverter.findAndSubst(key, props);
               final String renderedClass = key.substring(RENDERER_PREFIX.length());
               RendererMap.addRenderer((RendererSupport) hierarchy, renderedClass,
                       renderingClass);
            }
         }
      }
   }


   /**
    * Parse the additivity option for a non-root category.
    */
   final void parseAdditivityForLogger(final Properties props, final Logger cat,
                                       final String loggerName) {
      final String value = OptionConverter.findAndSubst(ADDITIVITY_PREFIX + loggerName,
              props);
      LogLog.debug("Handling " + ADDITIVITY_PREFIX + loggerName + "=[" + value + ']');
      // touch additivity only if necessary
      if (value != null && !value.isEmpty()) {
         final boolean additivity = OptionConverter.toBoolean(value, true);
         LogLog.debug("Setting additivity for \"" + loggerName + "\" to " +
                 additivity);
         cat.setAdditivity(additivity);
      }
   }


   /**
    * This method must work for the root category as well.
    */
   final void parseCategory(final Properties props, final Logger logger, final String optionKey,
                            final String loggerName, final String value) {

      LogLog.debug("Parsing for [" + loggerName + "] with value=[" + value + "].");
      // We must skip over ',' but not white space
      final StringTokenizer st = new StringTokenizer(value, ",");

      // If value is not in the form ", appender.." or "", then we should set
      // the level of the loggeregory.

      if (!(value.startsWith(",") || value.isEmpty())) {

         // just to be on the safe side...
         if (!st.hasMoreTokens()) {
            return;
         }

         final String levelStr = st.nextToken();
         LogLog.debug("Level token is [" + levelStr + "].");

         // If the level value is inherited, set category level value to
         // null. We also check that the user has not specified inherited for the
         // root category.
         if (INHERITED.equalsIgnoreCase(levelStr) ||
                 NULL.equalsIgnoreCase(levelStr)) {
            if (loggerName.equals(INTERNAL_ROOT_NAME)) {
               LogLog.warn("The root logger cannot be set to null.");
            } else {
               logger.setLevel(null);
            }
         } else {
            logger.setLevel(OptionConverter.toLevel(levelStr, Level.DEBUG));
         }
         LogLog.debug("Category " + loggerName + " set to " + logger.getLevel());
      }

      // Begin by removing all existing appenders.
      logger.removeAllAppenders();

      while (st.hasMoreTokens()) {
         final String appenderName = st.nextToken().trim();
         if (",".equals(appenderName)) {
            continue;
         }
         LogLog.debug("Parsing appender named \"" + appenderName + "\".");
         final Appender appender = parseAppender(props, appenderName);
         if (appender != null) {
            logger.addAppender(appender);
         }
      }
   }


   final Appender parseAppender(final Properties props, final String appenderName) {
      Appender appender = registryGet(appenderName);
      if (appender != null) {
         LogLog.debug("Appender \"" + appenderName + "\" was already parsed.");
         return appender;
      }
      // Appender was not previously initialized.
      final String prefix = APPENDER_PREFIX + appenderName;
      final String layoutPrefix = prefix + ".layout";

      appender = (Appender) OptionConverter.instantiateByKey(props, prefix,
              Appender.class,
              null);
      if (appender == null) {
         LogLog.error(
                 "Could not instantiate appender named \"" + appenderName + "\".");
         return null;
      }
      appender.setName(appenderName);

      if (appender instanceof OptionHandler) {
         if (appender.requiresLayout()) {
            final Layout layout = (Layout) OptionConverter.instantiateByKey(props,
                    layoutPrefix,
                    Layout.class,
                    null);
            if (layout != null) {
               appender.setLayout(layout);
               LogLog.debug("Parsing layout options for \"" + appenderName + "\".");
               //configureOptionHandler(layout, layoutPrefix + ".", props);
               PropertySetter.setProperties(layout, props, layoutPrefix + '.');
               LogLog.debug("End of parsing for \"" + appenderName + "\".");
            }
         }
         //configureOptionHandler((OptionHandler) appender, prefix + ".", props);
         PropertySetter.setProperties(appender, props, prefix + '.');
         LogLog.debug("Parsed \"" + appenderName + "\" options.");
      }
      registryPut(appender);
      return appender;
   }


   final void registryPut(final Appender appender) {
      registry.put(appender.getName(), appender);
   }


   final Appender registryGet(final String name) {
      return (Appender) registry.get(name);
   }
}

final class PropertyWatchdog extends FileWatchdog {

   PropertyWatchdog(final String filename) {
      super(filename);
   }


   /**
    * Call {@link PropertyConfigurator#configure(String)} with the <code>filename</code> to reconfigure log4j.
    */
   public void doOnChange() {
      new PropertyConfigurator().doConfigure(filename,
              LogManager.getLoggerRepository());
   }
}
