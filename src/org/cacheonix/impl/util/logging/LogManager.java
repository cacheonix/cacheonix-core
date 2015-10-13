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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.helpers.Loader;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.helpers.OptionConverter;
import org.cacheonix.impl.util.logging.spi.DefaultRepositorySelector;
import org.cacheonix.impl.util.logging.spi.LoggerFactory;
import org.cacheonix.impl.util.logging.spi.LoggerRepository;
import org.cacheonix.impl.util.logging.spi.NOPLoggerRepository;
import org.cacheonix.impl.util.logging.spi.RepositorySelector;
import org.cacheonix.impl.util.logging.spi.RootLogger;

/**
 * Use the <code>LogManager</code> class to retreive {@link Logger} instances or to operate on the current {@link
 * LoggerRepository}. When the <code>LogManager</code> class is loaded into memory the default initalzation procedure is
 * inititated. The default intialization procedure</a> is described in the <a href="../../../../manual.html#defaultInit">short
 * log4j manual</a>.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class LogManager {

   static final String DEFAULT_XML_CONFIGURATION_FILE = "cacheonix.logging.xml";

   /**
    * @deprecated This variable is for internal use only. It will become private in future versions.
    */
   public static final String CONFIGURATOR_CLASS_KEY = "log4j.configuratorClass";

   /**
    * @deprecated This variable is for internal use only. It will become private in future versions.
    */
   public static final String DEFAULT_INIT_OVERRIDE_KEY = "log4j.defaultInitOverride";

   public static final String RESOURCE_DEBUG = "/META-INF/cacheonix-logging-debug.properties";

   public static final String RESOURCE_ERROR = "/META-INF/cacheonix-logging-error.properties";

   public static final String RESOURCE_INFO = "/META-INF/cacheonix-logging-info.properties";

   public static final String RESOURCE_WARN = "/META-INF/cacheonix-logging-warn.properties";

   @SuppressWarnings("StaticNonFinalField")
   private static Object guard = null;

   @SuppressWarnings("StaticNonFinalField")
   private static RepositorySelector repositorySelector;


   private LogManager() {

   }


   static {
      // By default we use a DefaultRepositorySelector which always returns 'h'.
      final Hierarchy h = new Hierarchy(new RootLogger(Level.DEBUG));
      repositorySelector = new DefaultRepositorySelector(h);

      /** Search for the properties file log4j.properties in the CLASSPATH.  */
      final String override = OptionConverter.getSystemProperty(DEFAULT_INIT_OVERRIDE_KEY, null);

      // if there is no default init override, then get the resource
      // specified by the user or the default config file.
      if (override == null || "false".equalsIgnoreCase(override)) {

         final String configurationOptionStr = SystemProperty.CACHEONIX_LOGGING_CONFIGURATION;

         final String configuratorClassName = OptionConverter.getSystemProperty(CONFIGURATOR_CLASS_KEY, null);

         URL url = null;

         // if the user has not specified the cacheonix.logging.configuration
         // property, we search first for the file "log4j.xml" and then
         // "log4j.properties"
         if (StringUtils.isBlank(configurationOptionStr)) {
            url = Loader.getResource(DEFAULT_XML_CONFIGURATION_FILE);
            if (url == null) {
               url = Loader.getResource(RESOURCE_ERROR);

               if (url == null) {
                  url = LogManager.class.getResource(RESOURCE_ERROR);
               }
            }
         } else {
            try {
               url = new URL(configurationOptionStr);
            } catch (final MalformedURLException ex) {
               // so, resource is not a URL:
               // attempt to get the resource from the class path
               url = Loader.getResource(configurationOptionStr);
            }
         }

         // If we have a non-null url, then delegate the rest of the
         // configuration to the OptionConverter.selectAndConfigure
         // method.
         if (url != null) {
            LogLog.debug("Using URL [" + url + "] for automatic log4j configuration.");
            try {
               OptionConverter.selectAndConfigure(url, configuratorClassName,
                       getLoggerRepository());
            } catch (final NoClassDefFoundError e) {
               LogLog.warn("Error during default initialization", e);
            }
         } else {
            LogLog.debug("Could not find resource: [" + configurationOptionStr + "].");
         }
      }
   }


   /**
    * Sets <code>LoggerFactory</code> but only if the correct <em>guard</em> is passed as parameter.
    * <p/>
    * <p>Initally the guard is null.  If the guard is <code>null</code>, then invoking this method sets the logger
    * factory and the guard. Following invocations will throw a {@link IllegalArgumentException}, unless the previously
    * set <code>guard</code> is passed as the second parameter.
    * <p/>
    * <p>This allows a high-level component to set the {@link RepositorySelector} used by the <code>LogManager</code>.
    * <p/>
    * <p>For example, when tomcat starts it will be able to install its own repository selector. However, if and when
    * Tomcat is embedded within JBoss, then JBoss will install its own repository selector and Tomcat will use the
    * repository selector set by its container, JBoss.
    */
   public static void setRepositorySelector(final RepositorySelector selector, final Object guard)
           throws IllegalArgumentException {

      if (LogManager.guard != null && LogManager.guard != guard) {
         throw new IllegalArgumentException(
                 "Attempted to reset the LoggerFactory without possessing the guard.");
      }

      if (selector == null) {
         throw new IllegalArgumentException("RepositorySelector must be non-null.");
      }

      LogManager.guard = guard;
      repositorySelector = selector;
   }


   public static LoggerRepository getLoggerRepository() {

      if (repositorySelector == null) {
         repositorySelector = new DefaultRepositorySelector(new NOPLoggerRepository());
         guard = null;
         LogLog.error("LogMananger.repositorySelector was null likely due to error in class reloading, using NOPLoggerRepository.");
      }
      return repositorySelector.getLoggerRepository();
   }


   /**
    * Retrieve the appropriate root logger.
    */
   public
   static Logger getRootLogger() {
      // Delegate the actual manufacturing of the logger to the logger repository.
      return getLoggerRepository().getRootLogger();
   }


   /**
    * Retrieve the appropriate {@link Logger} instance.
    */
   public
   static Logger getLogger(final String name) {
      // Delegate the actual manufacturing of the logger to the logger repository.
      return getLoggerRepository().getLogger(name);
   }


   /**
    * Retrieve the appropriate {@link Logger} instance.
    */
   public
   static Logger getLogger(final Class clazz) {
      // Delegate the actual manufacturing of the logger to the logger repository.
      return getLoggerRepository().getLogger(clazz.getName());
   }


   /**
    * Retrieve the appropriate {@link Logger} instance.
    */
   public
   static Logger getLogger(final String name, final LoggerFactory factory) {
      // Delegate the actual manufacturing of the logger to the logger repository.
      return getLoggerRepository().getLogger(name, factory);
   }


   public
   static Logger exists(final String name) {

      return getLoggerRepository().exists(name);
   }


   public
   static Enumeration getCurrentLoggers() {

      return getLoggerRepository().getCurrentLoggers();
   }


   public
   static void shutdown() {

      getLoggerRepository().shutdown();
   }


   public
   static void resetConfiguration() {

      getLoggerRepository().resetConfiguration();
   }
}

