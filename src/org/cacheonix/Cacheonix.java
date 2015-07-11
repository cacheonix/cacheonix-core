/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cacheonix.cache.Cache;
import org.cacheonix.cache.CacheExistsException;
import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.cluster.Cluster;
import org.cacheonix.impl.AbstractCacheonix;
import org.cacheonix.impl.cache.local.LocalCacheonix;
import org.cacheonix.impl.cluster.node.DistributedCacheonix;
import org.cacheonix.impl.configuration.BroadcastConfiguration;
import org.cacheonix.impl.configuration.CacheonixConfiguration;
import org.cacheonix.impl.configuration.ConfigurationConstants;
import org.cacheonix.impl.configuration.ConfigurationReader;
import org.cacheonix.impl.configuration.MulticastBroadcastConfiguration;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.logging.Logger;

/**
 * A singleton that creates and provides access to various Cacheonix features such as caching, parallel computing and
 * distrbuted locks. <h1>Accessing a Cache</h1> Each cache in Cacheonix is uniquely identified by its name within
 * Cacheonix configuration. To get a cache from Cacheonix instance use method <code>getCache()</code>. <p/>
 * <b>Example:</b>
 * <pre>
 * Cache cache = Cacheonix.getInstance().getCache("my.cache");
 * </pre>
 * <h1>Configuring Cacheonix</h1> <p>Visit <a href="http://wiki.cacheonix.com/display/CCHNX20/Configuring+Cacheonix">online
 * Cacheonix documentation</a> for information on configuring Cacheonix.</p> <p/> <h1>Code Examples</h1> <p>Visit <a
 * href="http://wiki.cacheonix.com/display/CCHNX20/Programming+With+Cacheonix">online code examples</a> for examples on
 * working with the cache API.</p>
 *
 * @see #getCache(String)
 * @see #createCache(String)
 * @see #createCache(String, String)
 * @see #cacheExists(String)
 * @see Cache
 */
public abstract class Cacheonix {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Cacheonix.class); // NOPMD

   /**
    * Object to synchronize on when accessing class-wide state.
    */
   private static final Object CLASS_STATE_LOCK = new Object();

   /**
    * Holds cache MANAGERS accessed by a configuration name.
    *
    * @see #getInstance(String)
    */
   private static final Map<String, AbstractCacheonix> INSTANCES = new HashMap<String, AbstractCacheonix>(1);

   /**
    * Default configuration file.
    */
   public static final String CACHEONIX_XML = "cacheonix-config.xml";


   /**
    * Returns a cache with the given name. This method will create a cache using a default cache configuration template
    * if system property <code>cacheonix.auto.create.cache</code> is set to <code>true</code> (default is
    * <code>true</code>).
    * <p/>
    * Named caches are defined in the Cacheonix configuration file <code>cacheonix-config.xml</code>.
    *
    * @param cacheName case-sensitive name of the cache
    * @return named cache or null if a cache with this name cannot be found.
    * @noinspection PublicMethodNotExposedInInterface
    */
   public abstract <K extends Serializable, V extends Serializable> Cache<K, V> getCache(String cacheName);


   /**
    * Shuts down Cacheonix by shutting down its caches and running all necessary cleanups. Calling this shortcut method
    * produces the same results as calling <code>shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, false)</code>. This method
    * should be called once in Cacheonix instance's lifetime, ideally right before the application exits.
    *
    * @throws ShutdownException if this Cacheonix instance has already been shutdown.
    * @see #isShutdown()
    */
   public final void shutdown() {

      shutdown(ShutdownMode.GRACEFUL_SHUTDOWN, false);
   }


   /**
    * Shuts down Cacheonix by shutting down its caches and running all necessary cleanups.
    *
    * @param shutdownMode        a shutdown mode.
    * @param unregisterSingleton if <code>true</code>, the singleton will be unregistered and the next call to
    *                            <code>Cacheonix.getInstance()</code> will perform a lazy init thus returning a new,
    *                            operational instance of Cacheonix. Setting this parameter to <code>true</code> is
    *                            useful when writing unit tests that use Cacheonix where <code>tearDown</code> must
    *                            perform a complete cleanup and <code>setUp</code> must provide a fresh, clean instance
    *                            of Cacheonix. If <code>unregisterSingleton</code> is <code>false</code>, the singleton
    *                            won't be unregistered and subsequent calls to <code>Cacheonix.getInstance()</code> will
    *                            return an instance that has been shutdown. Most of the times
    *                            <code>unregisterSingleton<c/ode> should be set to <code>false</code> to ensure that
    *                            once shutdown Cacheonix stays shutdown.
    * @throws ShutdownException if this Cacheonix instance has already been shutdown.
    * @see ShutdownMode#FORCED_SHUTDOWN
    * @see ShutdownMode#GRACEFUL_SHUTDOWN
    * @see Cacheonix#getInstance()
    */
   public abstract void shutdown(final ShutdownMode shutdownMode, final boolean unregisterSingleton);


   /**
    * Returns an unmodifiable collection of {@link Cache} instances belonging to this Cacheonix instance.
    *
    * @return unmodifiable collection of {@link Cache} instances belonging to this Cacheonix instance.
    * @see Cache
    */
   public abstract Collection<Cache> getCaches();


   /**
    * Shutdowns and deletes the cache.
    *
    * @param cacheName name of the cache to delete.
    * @noinspection NestedTryStatement
    * @see #getCache(String)
    * @see #createCache(String)
    */
   public abstract void deleteCache(String cacheName);


   /**
    * Creates a cache using a template cache configuration. If a cache configuration cannot be not fond,  this method
    * uses the cache configuration template that name is provides as a second argument.
    * <p/>
    * The cache configuration template is a cache configuration that has attribute "template" set to "yes" or "true". A
    * configuration with name "default" is always a template configuration.
    * <p/>
    * The template cache configuration cannot be started.
    * <p/>
    * <b>Example of cacheonix-config.xml:</b>
    * <p/>
    * <pre>
    * &lt;?xml version ="1.0"?&gt;
    * &lt;cacheonix xmlns="http://www.cacheonix.com/schema/configuration"
    *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    *       xsi:schemaLocation="http://www.cacheonix.com/schema/configuration http://www.cacheonix.com/schema/cacheonix-config-2.0.xsd"&gt;
    *
    *    &lt;server&gt;
    *
    *       &lt;listener&gt;
    *          &lt;tcp port="8879" buffer="128k"/&gt;
    *       &lt;/listener&gt;
    *
    *       &lt;broadcast&gt;
    *          &lt;multicast multicastAddress="225.0.1.2" multicastPort="9998" multicastTTL="0"/&gt;
    *       &lt;/broadcast&gt;
    *
    *       &lt;partitionedCache name="customer.cache"&gt;
    *          &lt;store&gt;
    *             &lt;lru maxElements="10000" maxBytes="10mb"/&gt;
    *             &lt;expiration idleTime="120s"/&gt;
    *          &lt;/store&gt;
    *       &lt;/partitionedCache&gt;
    *
    *    &lt;/server&gt;
    * &lt;/cacheonix&gt;
    *
    * </pre>
    *
    * @param cacheName    name of the cache to create.
    * @param templateName name of the template to use when creating the cache.
    * @return a new cache
    * @throws IllegalArgumentException if the cache already exists or if the <code>cacheName</code> is a name of a
    *                                  template configuration.
    * @see #createCache(String)
    * @see #getCache(String)
    * @see #deleteCache(String)
    */
   public abstract Cache createCache(String cacheName, String templateName) throws IllegalArgumentException;


   /**
    * Returns <code>true</code> if a cache with the given name exists. Returns <code>false</code> if a cache with the
    * given name does not exist.
    *
    * @param cacheName name of the cache to check.
    * @return <code>true</code> if a cache with the given name exists or <code>false</code> if does not.
    */
   public abstract boolean cacheExists(String cacheName);


   /**
    * Creates a cache with a given name.
    * <p/>
    * If a cache configuration with this name is not found, a default cache configuration is used. A default
    * configuration is a configuration with a name "default".
    * <p/>
    * <b>Example:</b>
    * <pre>
    * // Create a cache using the default template
    * Cache cache = Cacheonix.getInstance().createCache("my.cache");
    * </pre>
    * <p/>
    * <b>Example of cacheonix-config.xml with default template:</b>
    * <p/>
    * <pre>
    * &lt;?xml version ="1.0"?&gt;
    * &lt;cacheonix xmlns="http://www.cacheonix.com/schema/configuration"
    *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    *       xsi:schemaLocation="http://www.cacheonix.com/schema/configuration http://www.cacheonix.com/schema/cacheonix-config-2.0.xsd"&gt;
    *
    *    &lt;server&gt;
    *
    *       &lt;listener&gt;
    *          &lt;tcp port="8879" buffer="128k"/&gt;
    *       &lt;/listener&gt;
    *
    *       &lt;broadcast&gt;
    *          &lt;multicast multicastAddress="225.0.1.2" multicastPort="9998" multicastTTL="0"/&gt;
    *       &lt;/broadcast&gt;
    *
    *       &lt;partitionedCache <b>name="default" template="true"</b>&gt;
    *          &lt;store&gt;
    *             &lt;lru maxElements="10000" maxBytes="10mb"/&gt;
    *             &lt;expiration idleTime="120s"/&gt;
    *          &lt;/store&gt;
    *       &lt;/partitionedCache&gt;
    *
    *    &lt;/server&gt;
    * &lt;/cacheonix&gt;
    *
    * </pre>
    *
    * @param cacheName name of the cache to create.
    * @return created cache
    * @see #getCache(String)
    * @see #deleteCache(String)
    */
   public abstract Cache createCache(String cacheName);


   /**
    * Returns or creates a Cacheonix instance for the given configuration path. The path can be a fully qualified URL, a
    * file or a resource in the classpath.
    *
    * @param configurationPath defines path to a resource containing Cacheonix configuration file.
    * @return Cacheonix instance.
    * @throws CacheExistsException DuplicateClusterConfigurationException IOException StorageException
    * @noinspection MethodReturnOfConcreteClass
    */
   public static Cacheonix getInstance(final String configurationPath) throws ConfigurationException {

      try {
         if (StringUtils.isBlank(configurationPath)) {
            throw new ConfigurationException("Configuration path cannot be null or empty but it is");
         }

         // Get from class path
         final String normalizedPath = configurationPath.charAt(0) == '/' ? configurationPath : '/' + configurationPath;
         final URL cacheonixXmlResource = Cacheonix.class.getResource(normalizedPath);
         if (cacheonixXmlResource != null) {
            return getInstanceFromExternalPath(cacheonixXmlResource.toExternalForm());
         }

         // Get from current directory
         final File currentDirCacheonixXml = new File(configurationPath);
         if (currentDirCacheonixXml.exists()) {
            return getInstanceFromExternalPath(
                    currentDirCacheonixXml.getCanonicalFile().toURI().toURL().toExternalForm());
         }

         return getInstanceFromExternalPath(configurationPath);
      } catch (final IOException e) {

         throw new ConfigurationException("Unexpected error while getting Cacheonix instance: " + e.toString(), e);
      }
   }


   /**
    * Returns or creates a Cacheonix instance for the given configuration resource name. The resource name defines path
    * to Cacheonix configuration file. The configuration file should be found in the classpath.
    *
    * @param configurationPath defines path to a file containing Cacheonix configuration file.
    * @return Cacheonix instance.
    * @throws ConfigurationException if configuration error occurs
    * @noinspection MethodReturnOfConcreteClass, deprecation
    */
   public static Cacheonix getInstance(final File configurationPath) throws ConfigurationException {

      try {

         return getInstanceFromExternalPath(configurationPath.getCanonicalFile().toURL().toExternalForm());
      } catch (final IOException e) {

         throw new ConfigurationException("Unexpected error while getting Cacheonix instance: " + e.toString(), e);
      }
   }


   private static Cacheonix getInstanceFromExternalPath(final String externalFormPath) throws IOException {

      synchronized (CLASS_STATE_LOCK) {
         final AbstractCacheonix existingInstance = INSTANCES.get(externalFormPath);
         if (existingInstance != null) {
            return existingInstance;
         }
         // Not found, create
         final AbstractCacheonix newInstance = createInstance(externalFormPath);
         // Startup
         newInstance.startup();
         // Register
         INSTANCES.put(externalFormPath, newInstance);
         // Return result
         return newInstance;
      }
   }


   /**
    * Returns a singleton instance of Cacheonix using a default Cacheonix configuration. Cacheonix finds the default
    * configuration by looking for <a href="http://wiki.cacheonix.com/display/CCHNX20/Configuring+Cacheonix">cacheonix-config.xml</a>
    * in the following order. Each step is executed only if the previous step has not defined the configuration: <ol>
    * <li>Use the configuration defined by the URL in the system property <code>cacheonix.configuration</code>. The
    * property is passed to JVM by using command line parameter <code>-Dcacheonix.configuration=&lt;path to
    * configuration&gt;</code></li> <li>Use <code>cacheonix-config.xml</code> from the classpath</li> <li>Use
    * <code>cacheonix-config.xml</code> from the local directory</li> <li>Use fall-back
    * <code>META-INF/cacheonix-config.xml</code> in the cacheonix.jar</li> </ol>
    * <p/>
    * To disable the fall-back configuration, use JVM's command line parameter <code>-Dcacheonix.fallback.configuration=false</code>.
    * <p/>
    * We recommend disabling the fall-back configuration when using Cacheonix in a production environment. The fall-back
    * configuration may hide the fact that the production configuration is missing. Disabling it helps Cacheonix to fail
    * fast thus allowing to discover the fact that the production configuration is missing.
    *
    * @return a singleton instance of Cacheonix.
    * @throws IllegalStateException if a cache with a duplicate name is found in the Cacheonix configuration.
    * @throws IllegalStateException if the cache configuration cannot be found.
    * @noinspection MethodReturnOfConcreteClass, JavaDoc, deprecation
    */
   public static Cacheonix getInstance() throws ConfigurationException {

      try {

         // Find configuration at the system property cacheonix.configuration
         final String systemProperty = System.getProperty(SystemProperty.NAME_CACHEONIX_CONFIGURATION);
         if (!StringUtils.isBlank(systemProperty)) {
            return getInstance(systemProperty);
         }

         //  Find configuration in the classpath
         final URL cacheonixXmlResource = Cacheonix.class.getResource('/' + CACHEONIX_XML);
         if (cacheonixXmlResource != null) {
            return getInstanceFromExternalPath(cacheonixXmlResource.toExternalForm());
         }

         // Get from current directory
         final File currentDirCacheonixXml = new File(CACHEONIX_XML);
         if (currentDirCacheonixXml.exists()) {
            return getInstanceFromExternalPath(currentDirCacheonixXml.toURL().toExternalForm());

         }

         final String fallbackProperty = System.getProperty(SystemProperty.NAME_CACHEONIX_FALLBACK_CONFIGURATION,
                 "true");
         if ("false".equalsIgnoreCase(fallbackProperty)) {
            throw new IllegalStateException("Cacheonix configuration cannot be found");
         } else {
            // Fallback to the packaged configuration
            final URL fallbackCacheonixXmlResource = Cacheonix.class
                    .getResource(ConfigurationConstants.FALLBACK_CACHEONIX_XML_RESOURCE);
            if (fallbackCacheonixXmlResource == null) {
               throw new IllegalStateException("Cacheonix configuration cannot be found");
            } else {
               return getInstanceFromExternalPath(fallbackCacheonixXmlResource.toExternalForm());
            }
         }
      } catch (final IOException e) {
         throw new ConfigurationException("Unexpected error while getting Cacheonix instance: " + e.toString(), e);
      }
   }


   /**
    * Returns a cluster this Cacheonix instance is a member of.
    *
    * @return the cluster this Cacheonix instance is a member of.
    */
   public abstract Cluster getCluster();


   /**
    * Check if this Cacheonix instance has been shutdown.
    *
    * @return <code>true</code> if this Cacheonix instance has been shutdown. Otherwise returns false.
    * @see #shutdown()
    */
   public abstract boolean isShutdown();


   /**
    * Creates a concrete Cacheonix instance based on the configuration.
    *
    * @param externalFormPath configuration path.
    * @return a concrete Cacheonix instance based on the configuration.
    * @throws IOException if a Cacheonix instance cannot be created.
    */
   private static AbstractCacheonix createInstance(final String externalFormPath) throws IOException {

      // Read configuration
      final ConfigurationReader configurationReader = new ConfigurationReader();
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(externalFormPath);
      if (configuration.getServer() != null) {

         // Limit broadcast to the local host if mcast TTL is 0
         final BroadcastConfiguration broadcastConfiguration = configuration.getServer().getBroadcastConfiguration();
         final MulticastBroadcastConfiguration multicastConfiguration = broadcastConfiguration.getMulticast();
         if (multicastConfiguration != null && multicastConfiguration.getMulticastTTL() == 0) {

            configuration.getServer().getListener().getTcp().setAddress(InetAddress.getByName("127.0.0.1"));
         }

         return new DistributedCacheonix(configuration.getServer());
      } else if (configuration.getLocal() != null) {

         return new LocalCacheonix(configuration);
      } else {

         throw new IllegalArgumentException("Unknown configuration type: " + externalFormPath);
      }
   }


   /**
    * Removes a given Cacheonix instance from the cache managers registry.
    *
    * @param instance the Cacheonix instance to unregister.
    */
   @SuppressWarnings("MethodMayBeStatic")
   protected final void unregister(final Cacheonix instance) {

      synchronized (CLASS_STATE_LOCK) {
         final Set<Map.Entry<String, AbstractCacheonix>> entrySet = INSTANCES.entrySet();
         for (final Iterator<Map.Entry<String, AbstractCacheonix>> iter = entrySet.iterator(); iter.hasNext(); ) {
            final Map.Entry<String, AbstractCacheonix> entry = iter.next();
            //noinspection ObjectEquality
            if (entry.getValue() == instance) { // Intended comparison by reference
               //noinspection ControlFlowStatementWithoutBraces
               if (LOG.isDebugEnabled()) LOG.debug("Unregistering: " + instance.getDescription()); // NOPMD
               iter.remove();
               return;
            }
         }
      }
   }


   /**
    * Returns a brief, single-line, free-from description of this instance.
    *
    * @return the description of this instance.
    */
   protected abstract String getDescription();


   /**
    * {@inheritDoc}
    */
   public String toString() {

      return "Cacheonix{" +
              "INSTANCES.size()=" + INSTANCES.size() +
              '}';
   }
}
