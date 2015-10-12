package org.cacheonix.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.datasource.PrefetchScheduler;
import org.cacheonix.impl.cache.datasource.PrefetchStageThreadPoolAdapter;
import org.cacheonix.impl.cache.storage.disk.StorageException;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.ClockImpl;
import org.cacheonix.impl.configuration.CacheonixConfiguration;
import org.cacheonix.impl.configuration.ConfigurationConstants;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.UserThreadFactory;

/**
 * AbstractCacheonix
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection ProtectedField, AbstractMethodCallInConstructor
 * @since May 9, 2009 4:07:51 PM
 */
public abstract class AbstractCacheonix extends Cacheonix {

   private static final int INITIAL_CACHE_MAP_CAPACITY = 11;

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(AbstractCacheonix.class); // NOPMD

   /**
    * A single-threaded Executor used to run event notification outside of main processing loop.
    */
   private final ExecutorService eventNotificationExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

   /**
    * A shutdown hook to be called when a JVM shuts down.
    */
   private Thread shutdownHook = null;


   /**
    * Read lock is used for read operations on the mutable internal structures of the cache manager.
    */
   private final Lock readLock;

   /**
    * Read lock is used for write operations on the mutable internal structures of the cache manager.
    */
   protected final Lock writeLock;

   /**
    * Local configuration associated with this Cacheonix instance.
    */
   private final CacheonixConfiguration config;

   /**
    * Holds all caches. Cache name is a key and a {@link Cache} is an object.
    */
   protected final Map<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(INITIAL_CACHE_MAP_CAPACITY);

   /**
    * A look up map that contains cache configurations.
    *
    * @see #startup()
    */
   protected final Map cacheConfigMap;

   /**
    * Set to <code>true</code> if this cache manager was shutdown.
    */
   protected boolean shutdown = false;

   protected final Timer timer;

   protected final Clock clock;

   /**
    * A thread pool to execute asynchronous tasks in parallel. An example of such a task is pre-fetching a data for a
    * cache using a datasource.
    */
   private final ThreadPoolExecutor threadPoolExecutor;

   /**
    * A scheduler responsible for scheduling prefetch orders.
    */
   private final PrefetchScheduler prefetchScheduler;


   /**
    * Constructor.
    *
    * @param config Cacheonix configuration.
    */
   protected AbstractCacheonix(final CacheonixConfiguration config) {

      final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
      this.readLock = reentrantLock.readLock();
      this.writeLock = reentrantLock.writeLock();
      this.cacheConfigMap = createCacheConfigMap(config);
      this.config = config;
      this.timer = new Timer("CacheonixTimer");
      this.clock = new ClockImpl(1000L).attachTo(timer);
      this.threadPoolExecutor = new ThreadPoolExecutor(1, 2, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new UserThreadFactory("CacheonixExecutor"));
      this.prefetchScheduler = new PrefetchScheduler(new PrefetchStageThreadPoolAdapter(threadPoolExecutor));
   }


   /**
    * Timer.
    *
    * @return return system's timer.
    */
   protected final Timer getTimer() {

      return timer;
   }


   /**
    * This Cacheonix instance's system clock.
    *
    * @return return the system clock.
    */
   protected final Clock getClock() {

      return clock;
   }


   protected ThreadPoolExecutor getThreadPoolExecutor() {

      return threadPoolExecutor;
   }


   /**
    * Returns the scheduler responsible for scheduling prefetch orders.
    *
    * @return the scheduler responsible for scheduling prefetch orders.
    */
   protected PrefetchScheduler getPrefetchScheduler() {

      return prefetchScheduler;
   }


   /**
    * Returns the single-threaded Executor used to run event notification outside of main processing loop.
    *
    * @return the single-threaded Executor used to run event notification outside of main processing loop.
    */
   protected final ExecutorService getEventNotificationExecutor() {

      return eventNotificationExecutor;
   }


   /**
    * Performs actual startup. This is a template method that is called by {@link #startup()}. This method is called
    * inside the cache manager's write lock.
    *
    * @throws StorageException
    */
   protected abstract void doStartup();


   /**
    * This method is called once during Cacheonix construction.
    *
    * @param configuration the cacheonix configuration.
    * @return
    */
   protected abstract Map createCacheConfigMap(CacheonixConfiguration configuration);


   /**
    * Starts up all caches for the given configuration and initializes shutdown hook.
    *
    * @throws IllegalStateException  if a cache with a duplicate name is found in the Cacheonix configuration.
    * @throws ConfigurationException if configuration error occurs.
    */
   public final void startup() throws ConfigurationException {

      // Activate the logging level defined in the configuration
      // file if cacheonix.logging.configuration is not provided.
      // We need this condition to provide specialized logging
      // wo support developer testing.

      if (StringUtils.isBlank(SystemProperty.CACHEONIX_LOGGING_CONFIGURATION)) {
         config.getLoggingConfiguration().getLoggingLevel().activate();
      }

      writeLock.lock();
      try {
         // Set up shutdown thread
         registerShutdownHook();

         // Call template method.
         doStartup();

      } finally {
         writeLock.unlock();
      }
   }


   /**
    * Returns <code>true</code> if this Cacheonix instance has been shutdown.
    *
    * @return <code>true</code> if this Cacheonix instance has been shutdown.
    * @see #shutdown()
    */
   public final boolean isShutdown() {

      return shutdown;
   }


   public <K extends Serializable, V extends Serializable> Cache<K, V> getCache(final String cacheName) {

      verifyOperational();
      readLock.lock();
      try {
         final Cache cache = cacheMap.get(cacheName);
         if (cache != null) {
            return cache;
         }
      } finally {
         readLock.unlock();
      }

      if (SystemProperty.isAutocreateEnabled()) {
         writeLock.lock();
         try {
            final Cache cache = cacheMap.get(cacheName);
            if (cache == null) {
               return createCache(cacheName);
            } else {
               return cache;
            }
         } finally {
            writeLock.unlock();
         }
      } else {

         if (SystemProperty.isWaitForCacheEnabled()) {

            return createWaitCache(cacheName);
         } else {

            return null;
         }
      }
   }


   /**
    * Creates a Cache that waits for an arrival of a configuration if it is not present yet.
    *
    * @param cacheName cache name to create.
    * @return a cache that waits for an arrival of a configuration if it is not present yet.
    */
   protected abstract Cache createWaitCache(final String cacheName);


   public final Collection<Cache> getCaches() {

      readLock.lock();
      try {
         return Collections.unmodifiableCollection(cacheMap.values());
      } finally {
         readLock.unlock();
      }
   }


   public final void deleteCache(final String cacheName) {

      writeLock.lock();
      try {
         final CacheonixCache cache = (CacheonixCache) getCache(cacheName);
         if (cache == null) {
            return;
         }
         cache.shutdown();
         cacheMap.remove(cacheName);
      } finally {
         writeLock.unlock();
      }
   }


   public final boolean cacheExists(final String cacheName) {

      readLock.lock();
      try {
         return cacheMap.containsKey(cacheName);
      } finally {
         readLock.unlock();
      }
   }


   public final Cache createCache(final String cacheName) {

      return createCache(cacheName, ConfigurationConstants.CACHE_TEMPLATE_NAME_DEFAULT);
   }


   public abstract Cache createCache(String cacheName, String templateName) throws IllegalArgumentException;


   /**
    * Registers the shutdown hook.
    * <p/>
    * The registered shutdown hook must be unregistered upon shutdown.
    */
   private void registerShutdownHook() {

      // Create shutdown hook
      final ThreadFactory threadFactory = new UserThreadFactory("ShutdownHook");
      shutdownHook = threadFactory.newThread(new Runnable() {

         public void run() {

            if (!shutdown) {

               LOG.info("Shutting down Cacheonix at JVM exit because it has not been shutdown explicitly");
               shutdown();
            }
         }
      });

      // Register with JVM
      Runtime.getRuntime().addShutdownHook(shutdownHook);
   }


   /**
    * Unregisters the shutdown hook registered by <code>registerShutdownHook()</code>
    *
    * @see #registerShutdownHook()
    */
   protected final void unregisterShutdownHook() {

      if (shutdownHook != null) {

         try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
         } catch (final Exception e) {

            ExceptionUtils.ignoreException(e, "VM must be already shutting down");
         }
      }
   }


   /**
    * Verifies this cache manager is operational.
    */
   private void verifyOperational() {

      if (shutdown) {
         throw new IllegalStateException("This cache manager has been shutdown. ");
      }
   }


   public String toString() {

      return "AbstractCacheonix{" +
              "readLock=" + readLock +
              ", writeLock=" + writeLock +
              ", cacheMap=" + cacheMap +
              ", cacheConfigMap=" + cacheConfigMap +
              ", shutdown=" + shutdown +
              '}';
   }
}
