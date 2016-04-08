/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.config;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Holds names of Cacheonix system properties.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Aug 4, 2008 5:45:50 PM
 */
public final class SystemProperty {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SystemProperty.class); // NOPMD


   /**
    *
    */
   public static final int BUFFER_SIZE = 2 * 1024 * 1024;


   /**
    * System property that overrides attribute homeAloneTimeout.
    */
   private static final String NAME_CACHEONIX_HOME_ALONE_TIMEOUT = "cacheonix.home.alone.timeout";

   /**
    * An optional system property that overrides attribute <code>clusterSurveyTimeout</code>.
    * <code>clusterSurveyTimeout</code> is the time that a new Cacheonix node waits for to identify all available
    * Cacheonix nodes before stating a join procedure. The cluster survey timeout should be set to a value that is at
    * least two times higher than the cluster announcement frequency.
    */
   private static final String NAME_CACHEONIX_CLUSTER_SURVEY_TIMEOUT = "cacheonix.cluster.survey.timeout";

   /**
    * An optional system property that overrides attribute <code>clusterAnnouncementTimeoutMillis</code>, the frequency
    * the cluster should announce itself.
    */
   private static final String NAME_CACHEONIX_CLUSTER_ANNOUNCEMENT_TIMEOUT = "cacheonix.cluster.announcement.timeout";

   /**
    * System property that overrides attribute loggingLevel.
    */
   public static final String NAME_CACHEONIX_LOGGING_LEVEL = "cacheonix.logging.level";

   /**
    * Defines a system property for Cacheonix configuration.
    */
   public static final String NAME_CACHEONIX_CONFIGURATION = "cacheonix.configuration";

   /**
    * Defines a system property for Cacheonix configuration. If set to "false", the fallback configuration is not used.
    */
   public static final String NAME_CACHEONIX_FALLBACK_CONFIGURATION = "cacheonix.fallback.configuration";

   /**
    * java.io.temp
    */
   public static final String JAVA_IO_TEMP = "java.io.temp";

   /**
    * Enables automatic creating of a cache from a default template. Default is enabled.
    */
   public static final String NAME_CACHEONIX_AUTO_CREATE_CACHE = "cacheonix.auto.create.cache";

   /**
    * Default lease time property.
    */
   public static final String NAME_CACHEONIX_DEFAULT_LEASE_TIME = "cacheonix.default.lease.time";

   /**
    * Default lock time property.
    */
   public static final String NAME_CACHEONIX_DEFAULT_LOCK_TIMEOUT = "cacheonix.default.lock.timeout";

   /**
    * Default unlock time property.
    */
   public static final String NAME_CACHEONIX_DEFAULT_UNLOCK_TIMEOUT = "cacheonix.default.unlock.timeout";

   /**
    * Overrides configured multicast TTL.
    */
   public static final String NAME_CACHEONIX_MULTICAST_TTL = "cacheonix.multicast.ttl";

   /**
    * Overrides configured multicast port.
    */
   public static final String NAME_CACHEONIX_MULTICAST_PORT = "cacheonix.multicast.port";

   /**
    * Overrides configured multicast address.
    */
   public static final String NAME_CACHEONIX_MULTICAST_ADDRESS = "cacheonix.multicast.address";

   /**
    * Sets machine's name. A machine name is a name that uniquely identifies a machine that Cacheonix is running on. It
    * is possible to run multiple Cacheonix instances on a single machine. While this configuration helps to reduce the
    * latency caused by garbage collection by running more JVMs with smaller heaps, it also creates a possibility
    * Cacheonix assigning one or more backup copies to the same hardware box thus reducing availability of data in
    * presence of the hardware failures. If the machine name is set, Cacheonix uses it to ensure safety of backup copies
    * by trying to assign them to machines with different names. If the machine name is not set with
    * cacheonix.machine.name, Cacheonix uses an IP address of the host as machine name.
    */
   private static final String NAME_CACHEONIX_MACHINE_NAME = "cacheonix.machine.name";

   /**
    * Sets rack's name. In a production environment it is common for machines running Cacheonix to be organized into
    * racks. A rack name is a name that uniquely identifies a rack. All machines in a rack may represent a single point
    * of failure because each rack often has its own power and network feed. If the rack name is set, Cacheonix uses it
    * to ensure safety of partition backup copies by trying to assign them to machines in racks with different names.
    */
   private static final String NAME_CACHEONIX_RACK_NAME = "cacheonix.rack.name";

   /**
    * Sets territory name. For purpose of disaster survival and recovery, Cacheonix infrastructure may consist of a set
    * of geographically distributed data centers  connected by a high speed virtual private network. A territory name is
    * a name that uniquely identifies a territory. If the territory name is set, Cacheonix uses it to ensure safety of
    * partition backup copies by trying to assign them to machines at locations with different names.
    */
   private static final String NAME_CACHEONIX_TERRITORY_NAME = "cacheonix.territory.name";

   /**
    * Overrides configured TCP port.
    */
   public static final String NAME_CACHEONIX_TCP_LISTENER_PORT = "cacheonix.tcp.listener.port";

   /**
    * <code>true</code> if Cacheonix should wait for a cache to appear even if it is not configured in the local
    * <code>cacheonix-config.xml</code>.
    */
   public static final String NAME_CACHEONIX_WAIT_FOR_CACHE = "cacheonix.wait.for.cache";

   /**
    * Name of the property that can be used to override Cacheonix logging configuration. If set, it overrides the
    * logging settings defined by logging level.
    */
   private static final String NAME_CACHEONIX_LOGGING_CONFIGURATION = "cacheonix.logging.configuration";

   /**
    * If true, Cacheonix will print a stacktrace at cache manager shutdown. This may help to identify places where
    * system is getting shutdown at runtime.
    * <p/>
    * Default is false.
    */
   private static final String NAME_CACHEONIX_PRINT_STACKTRACE_AT_CACHEONIX_SHUTDOWN = "cacheonix.print.stacktrace.at.shutdown";

   /**
    * If true, Cacheonix will print a stacktrace at cache shutdown. This may help to identify places where a cache is
    * getting shutdown at runtime.
    * <p/>
    * Default is false.
    */
   private static final String NAME_CACHEONIX_PRINT_STACKTRACE_AT_CACHE_SHUTDOWN = "cacheonix.print.stacktrace.at.cache.shutdown";

   /**
    * If true, Cacheonix will print stacktraces for ignored exceptions.
    * <p/>
    * Default is false.
    */
   private static final String NAME_CACHEONIX_PRINT_IGNORED_EXCEPTIONS = "cacheonix.print.ignored.exceptions";


   /**
    * If true, Cacheonix will add a number of the created thread to the name of the thread.
    * <p/>
    * Default is false.
    */
   private static final String NAME_CACHEONIX_SHOW_THREAD_NUMBER = "cacheonix.show.thread.number";


   /**
    * Show thread type or not.
    */
   private static final String NAME_CACHEONIX_SHOW_TREAD_TYPE = "cacheonix.show.tread.type";

   /**
    * Defines system-wide client request timeout in milliseconds. Default is not set which means no timeout.
    */
   public static final String NAME_CACHEONIX_CLIENT_REQUEST_TIMEOUT = "cacheonix.client.request.timeout";

   /**
    * Command-line override of the time between issuing a request to shutdown the server gracefully and time a forced
    * shutdown is performed. Partition contributors use this timeout to move their to other nodes to try to avoid loss
    * of data or recovery overhead that may result from the forced shutdown.
    */
   private static final String NAME_CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT = "cacheonix.graceful.shutdown.timeout";

   /**
    * SO_TIMEOUT override.
    */
   private static final String NAME_CACHEONIX_SOCKET_TIMEOUT = "cacheonix.socket.timeout";

   /**
    * Selector timeout override.
    */
   private static final String NAME_CACHEONIX_SELECTOR_TIMEOUT = "cacheonix.selector.timeout";

   /**
    * Value of property {@link #NAME_CACHEONIX_MACHINE_NAME}.
    */
   public static final String CACHEONIX_MACHINE_NAME = System.getProperty(NAME_CACHEONIX_MACHINE_NAME);

   /**
    * Value of property {@link #NAME_CACHEONIX_RACK_NAME}.
    */
   public static final String CACHEONIX_RACK_NAME = System.getProperty(NAME_CACHEONIX_RACK_NAME);

   /**
    * Value of property {@link #NAME_CACHEONIX_TERRITORY_NAME}.
    */
   public static final String CACHEONIX_TERRITORY_NAME = System.getProperty(NAME_CACHEONIX_TERRITORY_NAME);

   /**
    * @see #NAME_CACHEONIX_TCP_LISTENER_PORT
    */
   public static final Integer CACHEONIX_TCP_LISTENER_PORT = propertyToInteger(NAME_CACHEONIX_TCP_LISTENER_PORT);

   public static final boolean CACHEONIX_PRINT_IGNORED_EXCEPTIONS = propertyToBoolean(
           System.getProperty(NAME_CACHEONIX_PRINT_IGNORED_EXCEPTIONS, "false"));


   public static final boolean CACHEONIX_SHOW_THREAD_NUMBER = propertyToBoolean(
           System.getProperty(NAME_CACHEONIX_SHOW_THREAD_NUMBER, "false"));

   /**
    * Value of the the property that can be used to override Cacheonix logging configuration. If set, it overrides the
    * logging settings defined by logging level.
    *
    * @see #CACHEONIX_LOGGING_CONFIGURATION
    */
   public static final String CACHEONIX_LOGGING_CONFIGURATION = System.getProperty(
           NAME_CACHEONIX_LOGGING_CONFIGURATION);

   /**
    * Command-line override of the time between issuing a request to shutdown the server gracefully and time a forced
    * shutdown is performed. Partition contributors use this timeout to move their to other nodes to try to avoid loss
    * of data or recovery overhead that may result from the forced shutdown.
    *
    * @see #NAME_CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT
    */
   public static final Long CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_GRACEFUL_SHUTDOWN_TIMEOUT);

   /**
    * Multicast TTL override. May be null if not set.
    */
   public static final Integer CACHEONIX_MULTICAST_TTL = propertyToInteger(NAME_CACHEONIX_MULTICAST_TTL);

   /**
    * Multicast port override. May be null if not set.
    */
   public static final Integer CACHEONIX_MULTICAST_PORT = propertyToInteger(NAME_CACHEONIX_MULTICAST_PORT);


   /**
    * Multicast address override. May be null if not set.
    */
   public static final InetAddress CACHEONIX_MULTICAST_ADDRESS = propertyToAddress(NAME_CACHEONIX_MULTICAST_ADDRESS);


   /**
    * Default lease time in millis or null if not set.
    *
    * @see #NAME_CACHEONIX_DEFAULT_LEASE_TIME
    */
   public static final Long CACHEONIX_DEFAULT_LEASE_TIME_MILLIS = timePropertyToLong(NAME_CACHEONIX_DEFAULT_LEASE_TIME);


   /**
    * Default lock timeout in millis or null if not set.
    *
    * @see #NAME_CACHEONIX_DEFAULT_LOCK_TIMEOUT
    */
   public static final Long CACHEONIX_DEFAULT_LOCK_TIMEOUT_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_DEFAULT_LOCK_TIMEOUT);


   /**
    * Default unlock timeout in millis or null if not set.
    *
    * @see #NAME_CACHEONIX_DEFAULT_UNLOCK_TIMEOUT
    */
   public static final Long CACHEONIX_DEFAULT_UNLOCK_TIMEOUT_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_DEFAULT_UNLOCK_TIMEOUT);


   /**
    * Home alone timeout value
    *
    * @see #NAME_CACHEONIX_HOME_ALONE_TIMEOUT
    */
   public static final Long CACHEONIX_HOME_ALONE_TIMEOUT_VALUE_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_HOME_ALONE_TIMEOUT);


   /**
    * A value of the the optional system property that overrides attribute <code>clusterAnnouncementTimeoutMillis</code>,
    * the frequency the cluster should announce itself.
    *
    * @see #NAME_CACHEONIX_CLUSTER_ANNOUNCEMENT_TIMEOUT
    */
   public static final Long CACHEONIX_CLUSTER_ANNOUNCEMENT_TIMEOUT_VALUE_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_CLUSTER_ANNOUNCEMENT_TIMEOUT);

   /**
    * The value of an optional system property that overrides attribute <code>clusterSurveyTimeout</code>.
    * <code>clusterSurveyTimeout</code> is the time that a new Cacheonix node waits for to identify all available
    * Cacheonix nodes before stating a join procedure. The cluster survey timeout should be set to a value that is at
    * least two times higher than the cluster announcement frequency.
    *
    * @see #NAME_CACHEONIX_CLUSTER_SURVEY_TIMEOUT
    */
   public static final Long CACHEONIX_CLUSTER_SURVEY_TIMEOUT_VALUE_MILLIS = timePropertyToLong(
           NAME_CACHEONIX_CLUSTER_SURVEY_TIMEOUT);


   /**
    * Optional value of SO_TIMEOUT override.
    */
   public static final Long CACHEONIX_SOCKET_TIMEOUT_MILLIS = timePropertyToLong(NAME_CACHEONIX_SOCKET_TIMEOUT);


   /**
    * Optional value of selector timeout override.
    */
   public static final Long CACHEONIX_SELECTOR_TIMEOUT_MILLIS = timePropertyToLong(NAME_CACHEONIX_SELECTOR_TIMEOUT);

   /**
    * Logging level value.
    *
    * @see #NAME_CACHEONIX_LOGGING_LEVEL
    */
   public static final LoggingLevel CACHEONIX_LOGGING_LEVEL = initLoggingLevel();

   /**
    * A system-wide client request timeout in milliseconds. Default is not set which means no timeout.
    */
   private static final AtomicLong CACHEONIX_CLIENT_REQUEST_TIMEOUT = new AtomicLong(
           toClientRequestTimeoutMillis(System.getProperty(NAME_CACHEONIX_CLIENT_REQUEST_TIMEOUT)));


   /**
    * Constructs the constant holder.
    */
   private SystemProperty() {

   }


   /**
    * Returns <code>true</code> if automatic creation of a cache from a template configuration is enabled.
    *
    * @return <code>true</code> if automatic creation of a cache from a template configuration is enabled.
    */
   public static boolean isAutocreateEnabled() {

      return propertyToBoolean(System.getProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE, "true"));
   }


   /**
    * Enables or disables autocreate.
    */
   public static void enableAutocreate() {

      System.setProperty(NAME_CACHEONIX_AUTO_CREATE_CACHE, Boolean.TRUE.toString());
   }


   /**
    * If true, cacheonix will print a stacktrace at cache manager shutdown. This may help to identify places where
    * system is getting shutdown at runtime.
    * <p/>
    * Default is false.
    *
    * @return if true, cacheonix will print a stacktrace at cache manager shutdown. This may help to identify places
    *         where system is getting shutdown at runtime.
    */
   public static boolean isPrintStacktraceAtCacheonixShutdown() {

      return propertyToBoolean(System.getProperty(NAME_CACHEONIX_PRINT_STACKTRACE_AT_CACHEONIX_SHUTDOWN, "false"));
   }


   /**
    * If true, Cacheonix will print a stacktrace at cache shutdown. This may help to identify places where a cache is
    * getting shutdown at runtime.
    * <p/>
    * Default is false.
    *
    * @return If true, Cacheonix will print a stacktrace at cache shutdown. This may help to identify places where a
    *         cache is getting shutdown at runtime.
    */
   public static boolean isPrintStacktraceAtCacheShutdown() {

      return propertyToBoolean(System.getProperty(NAME_CACHEONIX_PRINT_STACKTRACE_AT_CACHE_SHUTDOWN, "false"));
   }


   public static boolean isShowThreadType() {

      return propertyToBoolean(System.getProperty(NAME_CACHEONIX_SHOW_TREAD_TYPE, "false"));
   }


   private static boolean propertyToBoolean(final String property) {

      return "true".equalsIgnoreCase(property) || "yes".equalsIgnoreCase(property);
   }


   /**
    * Returns an immutable client request timeout in milliseconds. A client is a thread that calls public Cacheonix API
    * such as <code>Cache.put()</code>. This timeout is used to enforce liveliness of the client threads. If not set
    * (default), there is no timeout.
    *
    * @return client request timeout in milliseconds
    */
   public static long getClientRequestTimeoutMillis() {

      return CACHEONIX_CLIENT_REQUEST_TIMEOUT.get();
   }


   public static void setClientRequestTimeoutMillis(final long duration) {

      CACHEONIX_CLIENT_REQUEST_TIMEOUT.set(duration);
   }


   private static long toClientRequestTimeoutMillis(final String duration) {

      if (StringUtils.isBlank(duration)) {
         return Long.MAX_VALUE;
      }
      final long result;
      try {
         result = StringUtils.readTime(duration);
      } catch (final IllegalArgumentException ignored) {
         return Long.MAX_VALUE;
      }
      if (result == 0L) {
         return Long.MAX_VALUE;
      }
      return result;
   }


   private static Integer propertyToInteger(final String propertyName) {

      final String property = System.getProperty(propertyName);

      if (StringUtils.isBlank(property)) {

         return null;
      }

      try {

         return Integer.parseInt(property);
      } catch (final NumberFormatException e) {

         LOG.warn("Invalid property \"" + propertyName + "\" (" + property + "), won't use: " + e);
         return null;
      }
   }


   private static Long timePropertyToLong(final String propertyName) {

      final String property = System.getProperty(propertyName);
      if (StringUtils.isBlank(property)) {
         return null;
      }

      try {

         return StringUtils.readTime(property);

      } catch (final Exception e) {

         LOG.warn("Invalid property \"" + propertyName + "\" (" + property + "), won't use: " + e);
         return null;
      }
   }


   /**
    * Returns <code>true</code> if Cacheonix should wait for a cache to appear even if it is not configured in the local
    * <code>cacheonix-config.xml</code>.
    *
    * @return <code>true</code> if Cacheonix should wait for a cache to appear even if it is not configured in the local
    *         <code>cacheonix-config.xml</code>.
    */
   public static boolean isWaitForCacheEnabled() {

      return propertyToBoolean(System.getProperty(NAME_CACHEONIX_WAIT_FOR_CACHE, "false"));
   }


   private static LoggingLevel initLoggingLevel() {

      final String stringLoggingLevel = System.getProperty(NAME_CACHEONIX_LOGGING_LEVEL);
      if (StringUtils.isBlank(stringLoggingLevel)) {

         return null;
      }

      return LoggingLevel.convert(stringLoggingLevel);
   }


   private static InetAddress propertyToAddress(final String propertyName) {

      final String property = System.getProperty(propertyName);
      if (StringUtils.isBlank(property)) {
         return null;
      }

      try {

         return StringUtils.readInetAddress(property);
      } catch (final IllegalArgumentException e) {

         LOG.warn("Invalid property '" + propertyName + "': " + property + ", won't use: " + e);
         return null;
      }
   }
}
