/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
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
package org.cacheonix.impl.cluster.node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.cacheonix.ShutdownException;
import org.cacheonix.ShutdownMode;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.CacheExistsException;
import org.cacheonix.cluster.Cluster;
import org.cacheonix.exceptions.RuntimeInterruptedException;
import org.cacheonix.exceptions.RuntimeTimeoutException;
import org.cacheonix.impl.AbstractCacheonix;
import org.cacheonix.impl.cache.CacheonixCache;
import org.cacheonix.impl.cache.distributed.partitioned.CacheNodeJoinedMessage;
import org.cacheonix.impl.cache.distributed.partitioned.CacheNodeLeftMessage;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessor;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessorImpl;
import org.cacheonix.impl.cache.distributed.partitioned.CacheProcessorKey;
import org.cacheonix.impl.cache.distributed.partitioned.LeaveCacheGroupAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.PartitionedCache;
import org.cacheonix.impl.cache.distributed.partitioned.RepartitionAnnouncement;
import org.cacheonix.impl.cache.distributed.partitioned.SetCacheNodeStateMessage;
import org.cacheonix.impl.cache.distributed.partitioned.subscriber.EntryEventSubscriptionConfigurationSubscriber;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.cluster.node.state.group.Group;
import org.cacheonix.impl.cluster.node.state.group.GroupEventSubscriber;
import org.cacheonix.impl.cluster.node.state.group.GroupMember;
import org.cacheonix.impl.cluster.node.state.group.GroupMemberFailedToJoinEvent;
import org.cacheonix.impl.cluster.node.state.group.GroupMemberJoinedEvent;
import org.cacheonix.impl.cluster.node.state.group.GroupMemberLeftEvent;
import org.cacheonix.impl.cluster.node.state.group.JoinGroupMessage;
import org.cacheonix.impl.configuration.BroadcastConfiguration;
import org.cacheonix.impl.configuration.CacheStoreConfiguration;
import org.cacheonix.impl.configuration.CacheonixConfiguration;
import org.cacheonix.impl.configuration.ClusterConfiguration;
import org.cacheonix.impl.configuration.FixedSizeConfiguration;
import org.cacheonix.impl.configuration.KnownAddressBroadcastConfiguration;
import org.cacheonix.impl.configuration.LRUConfiguration;
import org.cacheonix.impl.configuration.MulticastBroadcastConfiguration;
import org.cacheonix.impl.configuration.PartitionedCacheConfiguration;
import org.cacheonix.impl.configuration.PartitionedCacheStoreConfiguration;
import org.cacheonix.impl.configuration.ServerConfiguration;
import org.cacheonix.impl.configuration.SystemProperty;
import org.cacheonix.impl.configuration.TCPListenerConfiguration;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.ClusterNodeJoinedEvent;
import org.cacheonix.impl.net.cluster.ClusterNodeLeftEvent;
import org.cacheonix.impl.net.cluster.ClusterProcessor;
import org.cacheonix.impl.net.cluster.ClusterProcessorKey;
import org.cacheonix.impl.net.cluster.MulticastClientProcessorKey;
import org.cacheonix.impl.net.cluster.MulticastMessageListener;
import org.cacheonix.impl.net.cluster.ReplicatedStateProcessorKey;
import org.cacheonix.impl.net.multicast.sender.MulticastSender;
import org.cacheonix.impl.net.multicast.sender.PlainMulticastSender;
import org.cacheonix.impl.net.multicast.sender.TCPMulticastSender;
import org.cacheonix.impl.net.multicast.server.DummyMulticastServer;
import org.cacheonix.impl.net.multicast.server.MulticastServer;
import org.cacheonix.impl.net.multicast.server.MulticastServerImpl;
import org.cacheonix.impl.net.processor.Command;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.WireableFactory;
import org.cacheonix.impl.net.tcp.server.TCPServer;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.Shutdownable;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Defines an interface for communicating with Cacheonix cluster node.
 * <p/>
 * The cluster node is backed by the cluster configuration. The cluster node consists of a set of cache members that
 * share the same multicast and IP address. Each cluster node runs its own {@link ClusterProcessor}.
 *
 * @noinspection ConstantConditions
 */
public final class DistributedCacheonix extends AbstractCacheonix implements MulticastMessageListener, GroupEventSubscriber, Shutdownable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(DistributedCacheonix.class); // NOPMD

   private volatile boolean started = false;


   /**
    * Cluster service.
    */
   private final ClusterProcessor clusterProcessor;

   /**
    * TCP Server.
    */
   private TCPServer tcpServer = null;

   /**
    * Configuration for this cluster member.
    */
   private final ServerConfiguration serverConfig;


   /**
    * Message sender is responsible for sending outbound messages to the network
    */
   private final MessageSender messageSender;

   /**
    * Address.
    */
   private final ClusterNodeAddress address;

   /**
    * Replicated state that holds groups, subscribers and locks.
    */
   private final ReplicatedState replicatedState;


   /**
    * Cache processor map.
    */
   @SuppressWarnings("unchecked")
   private final Map<String, CacheProcessor> cacheProcessorMap = new ConcurrentHashMap<String, CacheProcessor>(11);


   private final ReentrantLock reentrantLock = new ReentrantLock();

   /**
    * This condition is used to notify interested parties about changes in the {@link #cacheMap}.
    */
   private final Condition cacheMapChanged = reentrantLock.newCondition();

   /**
    * Routes messages to local processors or to a message sender
    */
   private final Router router;

   /**
    * Multicast sender.
    */
   private final MulticastSender multicastSender;

   /**
    * A server responsible for receiving multicast frames.
    */
   private final MulticastServer multicastServer;


   /**
    * Constructor.
    *
    * @param serverConfig the cluster config.
    * @throws IOException if I/O error occurred while creating the cluster node.
    */
   public DistributedCacheonix(final ServerConfiguration serverConfig) throws IOException {

      super(serverConfig.getCacheonixConfiguration());
      this.address = createNodeAddress(serverConfig);
      this.replicatedState = new ReplicatedState();
      this.serverConfig = serverConfig;
      this.multicastSender = createMulticastSender(address, serverConfig);
      this.multicastServer = createMulticastServer(serverConfig);
      final UUID initialClusterUUID = UUID.randomUUID();
      this.router = new Router(address);
      this.router.setClusterUUID(initialClusterUUID);
      this.clusterProcessor = createClusterProcessor(clock, timer, router, multicastSender, serverConfig, address,
              initialClusterUUID);
      this.messageSender = new MessageSender(address, serverConfig.getSocketTimeoutMillis(),
              serverConfig.getSelectorTimeoutMillis(), getClock());
      this.router.setOutput(messageSender);
      this.messageSender.setRouter(router);
      this.multicastSender.setRouter(router);
   }


   private static MulticastServer createMulticastServer(final ServerConfiguration serverConfig) {

      final BroadcastConfiguration broadcastConfiguration = serverConfig.getBroadcastConfiguration();
      final MulticastBroadcastConfiguration multicastConfiguration = broadcastConfiguration.getMulticast();
      if (multicastConfiguration == null) {

         return new DummyMulticastServer();
      } else {

         return new MulticastServerImpl(multicastConfiguration.getMulticastAddress(),
                 multicastConfiguration.getMulticastPort(),
                 multicastConfiguration.getMulticastTTL());
      }
   }


   /**
    * Creates a multicast sender based on the server configuration.
    *
    * @param localAddress the local node address.
    * @param serverConfig the server configuration.
    * @return a new multicast sender.
    * @throws IOException if an I/O error occured.
    */
   private static MulticastSender createMulticastSender(final ClusterNodeAddress localAddress,
           final ServerConfiguration serverConfig) throws IOException {

      // Limit broadcast to the local host
      final BroadcastConfiguration broadcastConfiguration = serverConfig.getBroadcastConfiguration();
      final MulticastBroadcastConfiguration multicastConfiguration = broadcastConfiguration.getMulticast();
      if (multicastConfiguration != null) {

         // This is multicast 
         return new PlainMulticastSender(multicastConfiguration.getMulticastAddress(),
                 multicastConfiguration.getMulticastPort(),
                 multicastConfiguration.getMulticastTTL());
      } else {

         // Known addresses must be set
         final List<KnownAddressBroadcastConfiguration> knownAddressConfigs = broadcastConfiguration.getKnownAddresses();
         if (CollectionUtils.isEmpty(knownAddressConfigs)) {
            throw new IOException("Broadcast configuration must contain at least one known address");
         }

         // Gat a list of receivers addresses
         final List<ReceiverAddress> knownReceiverAddresses = new ArrayList<ReceiverAddress>(
                 knownAddressConfigs.size());
         for (final KnownAddressBroadcastConfiguration knownAddressConfiguration : knownAddressConfigs) {

            knownAddressConfiguration.limitToLocalAddresses();
            final TCPListenerConfiguration addressConfiguration = knownAddressConfiguration.getAddressConfiguration();
            final InetAddress receiverTcpAddress = addressConfiguration.getAddress();
            final int receiverTcpPort = addressConfiguration.getPort();
            knownReceiverAddresses.add(new ReceiverAddress(receiverTcpAddress, receiverTcpPort));
         }

         return new TCPMulticastSender(localAddress, knownReceiverAddresses);
      }
   }


   /**
    * Creates a node address.
    *
    * @param serverConfig the server configuration.
    * @return a new node address.
    * @throws IOException if an I/O error occurred.
    */
   private static ClusterNodeAddress createNodeAddress(final ServerConfiguration serverConfig) throws IOException {

      final TCPListenerConfiguration tcpListenerConfiguration = serverConfig.getListener().getTcp();
      final InetAddress tcpInetAddress = tcpListenerConfiguration.getAddress();
      final String tcpAddress = tcpInetAddress == null ? "" : StringUtils.toString(tcpInetAddress);
      final int tcpPort = tcpListenerConfiguration.getPort();
      return ClusterNodeAddress.createAddress(tcpAddress, tcpPort);
   }


   protected void doStartup() {

      try {
         LOG.info("Starting up cluster node: " + getSummary());
         Assert.assertTrue(!started, "Cannot startup cluster member twice: {0} ", getSummary());
         started = true;

         final int wireableFactorySize = WireableFactory.getInstance().size();
         LOG.debug("Wireable factory size: " + wireableFactorySize);

         final BucketEventDispatcher bucketEventDispatcher = new BucketEventDispatcher(address, clusterProcessor);
         replicatedState.addBucketEventListener(Group.GROUP_TYPE_CACHE, bucketEventDispatcher);
         replicatedState.addGroupEventSubscriber(Group.GROUP_TYPE_CACHE, this);

         // Set up cluster processor
         clusterProcessor.subscribeMulticastMessageListener(this);
         clusterProcessor.getProcessorState().setReplicateState(replicatedState);
         router.register(MulticastClientProcessorKey.getInstance(), clusterProcessor);
         router.register(ReplicatedStateProcessorKey.getInstance(), clusterProcessor);
         router.register(ClusterProcessorKey.getInstance(), clusterProcessor);

         // Set up and start the message sender
         messageSender.startup();

         // Startup TCP server.
         final long socketTimeoutMillis = serverConfig.getSocketTimeoutMillis();
         final long selectorTimeoutMillis = serverConfig.getSelectorTimeoutMillis();
         final TCPListenerConfiguration tcpListenerConfiguration = serverConfig.getListener().getTcp();
         final InetAddress tcpInetAddress = tcpListenerConfiguration.getAddress();
         final String tcpAddress = tcpInetAddress == null ? "" : StringUtils.toString(tcpInetAddress);
         final int tcpPort = tcpListenerConfiguration.getPort();

         final DistributedCacheonixTCPRequestDispatcher requestDispatcher = new DistributedCacheonixTCPRequestDispatcher(
                 router);
         tcpServer = new TCPServer(getClock(), tcpAddress, tcpPort, requestDispatcher, socketTimeoutMillis,
                 selectorTimeoutMillis);
         tcpServer.startup();

         // Startup cluster service
         clusterProcessor.startup();

         // Startup multicast server
         multicastServer.addListener(clusterProcessor);
         multicastServer.startup();

         // Init proxies
         for (final PartitionedCacheConfiguration cacheConfig : serverConfig.getNormalPartitionedCacheTypes()) {

            final String cacheName = cacheConfig.getName();
            createAndRegisterCacheProxy(cacheName, cacheConfig);
         }

         LOG.info("Started up cluster node: " + getSummary());
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   private void beginAutocreateCacheProcessor(final Group group, final String cacheName,
           final PartitionedCacheConfiguration createAllTemplate) {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Begin autocreate for cache processor: " + cacheName + ", local address: " + address);
      }

      createAndRegisterCacheProxy(cacheName, createAllTemplate);

      long partitionSizeBytes = group.getPartitionSizeBytes();
      if (partitionSizeBytes == 0) {

         // Node is being created, but no partition size yet. Use our size.
         final CacheStoreConfiguration store = createAllTemplate.getStore();
         final FixedSizeConfiguration fixed = store.getFixed();
         final LRUConfiguration lru = store.getLru();
         partitionSizeBytes = fixed == null ? lru.getMaxBytes() : fixed.getMaxBytes();
      }

      postJoinGroupRequest(partitionSizeBytes, true, group.getReplicaCount(), cacheName,
              createAllTemplate.getName(), group.getMaxElements());
   }


   private void createAndRegisterCacheProxy(final String cacheName,
           final PartitionedCacheConfiguration cacheConfiguration) {

      // Create a cache
      final ServerConfiguration serverConfiguration = cacheConfiguration.getServerConfiguration();
      final long defaultUnlockTimeoutMillis = serverConfiguration.getDefaultUnlockTimeoutMillis();
      final CacheonixCache newCache = new PartitionedCache(clusterProcessor, clock, address, cacheName,
              defaultUnlockTimeoutMillis);

      // Put to map
      reentrantLock.lock();
      try {
         cacheMap.put(cacheName, newCache);
         cacheMapChanged.signalAll();
      } finally {
         reentrantLock.unlock();
      }
   }


   private void postJoinGroupRequest(final long desiredPartitionSize, final boolean partitionContributor,
           final int desiredReplicaCount, final String cacheName,
           final String cacheConfigName,
           final long desiredMaxElements) {
      // Create request
      final long heapSize = Runtime.getRuntime().maxMemory();
      final JoinGroupMessage announcement = new JoinGroupMessage(address, cacheName,
              partitionContributor, desiredPartitionSize, heapSize, desiredMaxElements);

      announcement.setClusterUUID(clusterProcessor.getProcessorState().getClusterView().getClusterUUID());
      announcement.setCacheConfigName(cacheConfigName);
      announcement.setReplicaCount(desiredReplicaCount);

      // Post
      clusterProcessor.post(announcement);
   }


   /**
    * {@inheritDoc}
    */
   final Cache createAndRegisterCache(final String cacheName, final PartitionedCacheConfiguration cacheConfig,
           final boolean useConfigurationAsTemplate) {
      //
      // Validate that cache configuration is not a template if it should not be.
      //
      if (cacheConfig.isTemplate() && !useConfigurationAsTemplate) {
         throw new IllegalArgumentException(
                 "A cache configuration template cannot be instantiated: " + cacheConfig.getName());
      }

      //
      // Validate the cache processor has not already been created
      //
      if (cacheMap.containsKey(cacheName)) {
         throw new IllegalStateException("Cache \"" + cacheName + "\" has already been created");
      }

      //
      // Submit an async command to begin creating a cache to serializer. We do it
      // in a separate command thread to avoid complex serialization and possible
      // conflicts with configuration requests processed by
      //
      try {
         clusterProcessor.enqueue(new Command() {

            public void execute() {

               // By the time this command reached the processor the cache
               // could have been already created, so we need to check.
               if (cacheMap.containsKey(cacheName)) {

                  // Do nothing. Cache has been created
                  // and the requester has been notified.
                  return;
               }

               // Create proxy so that the client can begin using a pass-through
               // cache or begin sending requests
               createAndRegisterCacheProxy(cacheName, cacheConfig);

               // Begin creating a cache
               beginCreateCacheNode(cacheName, cacheConfig);
            }
         });
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeInterruptedException(e);
      }


      // It is OK to do check outside of the lock becuase
      // the cacheMap is a ConcurrentHashMap
      Cache cache = cacheMap.get(cacheName);
      if (cache != null) {
         return cache;
      }

      //
      // Wait until the cache is created
      //
      reentrantLock.lock();
      try {
         // Try again
         cache = cacheMap.get(cacheName);
         if (cache != null) {
            return cache;
         }

         // Wait for condition cacheMapChanged
         final long timeoutMillis = SystemProperty.getClientRequestTimeoutMillis();
         while (cache == null) {
            if (cacheMapChanged.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
               cache = cacheMap.get(cacheName);
            } else {
               throw new RuntimeTimeoutException("Couldn't obtain cache " + cacheName + " in "
                       + timeoutMillis + " milliseconds, local address: " + address);
            }
         }
         return cache;
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeInterruptedException(e);
      } finally {
         reentrantLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation returns string <code>"Cluster node"</code>  suffixed with the summary of the cluster
    * configuration.
    */
   protected String getDescription() {

      return "Cluster node " + getSummary();
   }


   private String getSummary() {

      final TCPListenerConfiguration tcpListenerConfiguration = serverConfig.getListener().getTcp();
      final int tcpPort = tcpListenerConfiguration.getPort();
      final BroadcastConfiguration broadcastConfiguration = serverConfig.getBroadcastConfiguration();
      final MulticastBroadcastConfiguration multicastConfiguration = broadcastConfiguration.getMulticast();
      if (multicastConfiguration == null) {

         final StringBuilder summary = new StringBuilder(200);
         final List<KnownAddressBroadcastConfiguration> knownAddresses = broadcastConfiguration.getKnownAddresses();
         final int knownAddressesSize = knownAddresses.size();
         for (int i = 0; i < knownAddressesSize; i++) {

            final KnownAddressBroadcastConfiguration knownAddressConfiguration = knownAddresses.get(i);
            final TCPListenerConfiguration listenerConfiguration = knownAddressConfiguration.getAddressConfiguration();
            final String knownAddress = StringUtils.toString(listenerConfiguration.getAddress());
            final int knownPort = listenerConfiguration.getPort();
            summary.append(knownAddress).append(':').append(knownPort).append(':').append(tcpPort);
            summary.append(i < knownAddressesSize - 1 ? "," : "");
         }
         return summary.toString();
      } else {

         final String multicastAddress = StringUtils.toString(multicastConfiguration.getMulticastAddress());
         final int multicastPort = multicastConfiguration.getMulticastPort();
         return multicastAddress + ':' + multicastPort + ':' + tcpPort;
      }
   }


   /**
    * Returns a map of {@link PartitionedCacheConfiguration} objects. This method is called once during Cacheonix
    * construction.
    *
    * @param configuration Cacheonix configuration
    * @return a map of {@link PartitionedCacheConfiguration} objects.
    */
   protected final Map createCacheConfigMap(final CacheonixConfiguration configuration) {

      final List<PartitionedCacheConfiguration> cacheConfigs = configuration.getServer().getPartitionedCacheList();
      final Map<String, PartitionedCacheConfiguration> result = new HashMap<String, PartitionedCacheConfiguration>(
              cacheConfigs.size());
      for (final PartitionedCacheConfiguration cacheConfig : cacheConfigs) {

         if (result.containsKey(cacheConfig.getName())) {

            throw new CacheExistsException(cacheConfig.getName());
         }

         result.put(cacheConfig.getName(), cacheConfig);
      }
      return result;
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation simply creates a proxy wrapping a dist cache.
    */
   protected Cache createWaitCache(final String cacheName) {

      // Create a cache
      final CacheonixCache newCache = new PartitionedCache(clusterProcessor, clock, address, cacheName,
              serverConfig.getDefaultUnlockTimeoutMillis());

      // Put to map
      reentrantLock.lock();
      try {

         // Check if cache exist - it could have been created before we entered
         // the lock becuase it is a concurrent operation against cacheMap
         final Cache cache = cacheMap.get(cacheName);
         if (cache != null) {

            return cache;
         }
         cacheMap.put(cacheName, newCache);
         cacheMapChanged.signalAll();
      } finally {
         reentrantLock.unlock();
      }

      return newCache;
   }


   /**
    * Creates a new cluster processor.
    *
    * @param clock              the clock.
    * @param timer              the timer.
    * @param router             the router.
    * @param multicastSender    the multicast sender.
    * @param serverConfig       the server configuration.
    * @param address            the address.
    * @param initialClusterUUID the initial cluster UUID.
    * @return a new cluster processor.
    */
   private static ClusterProcessor createClusterProcessor(final Clock clock, final Timer timer,
           final Router router, final MulticastSender multicastSender,
           final ServerConfiguration serverConfig,
           final ClusterNodeAddress address,
           final UUID initialClusterUUID) {

      final ClusterConfiguration clusterConfiguration = serverConfig.getClusterConfiguration();
      final long homeAloneTimeoutMillis = clusterConfiguration.getHomeAloneTimeoutMillis();
      final long worstCaseLatencyMillis = clusterConfiguration.getWorstCaseLatencyMillis();
      final long clusterSurveyTimeoutMillis = clusterConfiguration.getClusterSurveyTimeoutMillis();
      final long clusterAnnouncementTimeoutMillis = clusterConfiguration.getClusterAnnouncementTimeoutMillis();
      final long gracefulShutdownTimeoutMillis = serverConfig.getGracefulShutdownTimeoutMillis();
      final String clusterName = clusterConfiguration.getName();

      return new ClusterProcessor(clusterName, clock, timer, router, multicastSender, address, homeAloneTimeoutMillis,
              worstCaseLatencyMillis,
              gracefulShutdownTimeoutMillis, clusterSurveyTimeoutMillis, clusterAnnouncementTimeoutMillis,
              initialClusterUUID);
   }


// ================================================================================================================
   //
   // MulticastMessageListener
   //
   // ================================================================================================================


   public void receive(final Message message) {

   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation initiates cache member boarding by submitting replicated state commands to join the cache
    * group.
    */
   public void notifyClusterNodeJoined(final ClusterNodeJoinedEvent event) {

      final List nodes = event.getNodes();
      if (!nodes.contains(address)) {
         return;
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug("JJJJJJJJJJJJJJJJJJJJ Cluster node " + getSummary() + " joined the cluster");
      }

      // Iterate normal cache configurations and send Join announcements. The cacheMap
      // has been populated before, at the startup
      for (final PartitionedCacheConfiguration cacheConfig : serverConfig.getNormalPartitionedCacheTypes()) {

         // Send a request to the group membership to add a member. DistributedCacheonix should
         // receive a notification about change in group membership back when the group
         // state machine has processed it.
         final String cacheName = cacheConfig.getName();
         beginCreateCacheNode(cacheName, cacheConfig);
      }

      // REVIEWME: simeshev@cacheonix.org - 2020-02-11 -> This should be moved to the place
      // where group add is announced when re-sending group member joined announcement to
      // the freshly joined cluster is ready.

      // Process create-all template by announcing local nodes with same parameters
      // as a group and adding the initial proxy to the cacheMap
      final PartitionedCacheConfiguration createAllTemplate = serverConfig.getCreateAllTemplate();
      if (createAllTemplate != null) {

         // Check already existing nodes
         for (final Group group : replicatedState.getGroups()) {

            if (group.getGroupType() == Group.GROUP_TYPE_CACHE) {

               // Check if exists in explicit configurations - it  is being created on its own
               final String cacheName = group.getName();

               // The fact that there is is in the cacheMap means that automatic creating
               // of the cache member has already been announced. We should not attempt to
               // autocreate a node then.
               if (!cacheMap.containsKey(cacheName)) {

                  beginAutocreateCacheProcessor(group, cacheName, createAllTemplate);
               }
            }
         }
      }
   }


   private void beginCreateCacheNode(final String cacheName, final PartitionedCacheConfiguration cacheConfig) {

      // Parameters
      final PartitionedCacheStoreConfiguration store = cacheConfig.getStore();
      final FixedSizeConfiguration fixed = store.getFixed();
      final LRUConfiguration lru = store.getLru();
      final long desiredPartitionSize = fixed == null ? lru.getMaxBytes() : fixed.getMaxBytes();
      final boolean partitionContributor = cacheConfig.isPartitionContributor();

      final int desiredReplicaCount = store.getReplication().getReplicaCount();
      final long desiredMaxElements = lru == null ? 0L : lru.getMaxElements();

      // Post reliable mcast message to the replicate state
      postJoinGroupRequest(desiredPartitionSize, partitionContributor,
              desiredReplicaCount, cacheName, cacheConfig.getName(), desiredMaxElements);
   }


   /**
    * {@inheritDoc}
    */
   public void notifyClusterNodeLeft(final ClusterNodeLeftEvent event) {

      // Submit command to reset cluster state if local
      // Check if we ourselves left
      final Collection nodes = event.getNodes();
      if (!nodes.contains(address)) {
         return;
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug("LLLLLLLLLLLLLLLLLLLLLL Cluster node " + getSummary() + " left the cluster");
      }

      // Destroy cache processors
      for (final Iterator<Map.Entry<String, CacheProcessor>> iterator = cacheProcessorMap.entrySet().iterator(); iterator.hasNext(); ) {
         final Map.Entry<String, CacheProcessor> entry = iterator.next();
         final String cacheName = entry.getKey(); // NOPMD
         final CacheProcessor cacheProcessor = entry.getValue(); // NOPMD

         LOG.debug("Shutting down cache processor: " + cacheName);
         cacheProcessor.shutdown();
         LOG.debug("Cache processor " + cacheName + " has been shutdown");

         // REVIEWME: simeshev@cacheonix.org - 2010-02-05 - Not sure if we should
         // remove it from the map first or remove from destination first.

         // Remove from the message receiver

         clusterProcessor.unregisterCacheProcessor(cacheProcessor.getCacheName());
         router.unregister(new CacheProcessorKey(cacheName));
         cacheProcessorMap.remove(cacheName);

         // Remove from the cache processor registry
         iterator.remove();
      }
   }


   /**
    * {@inheritDoc}
    */
   public void notifyClusterNodeBlocked() {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Notifying all cache processors that they should block");
      }

      // Notify all cache processors that they should block
      for (final CacheProcessor cacheProcessor : cacheProcessorMap.values()) {
         cacheProcessor.post(new SetCacheNodeStateMessage(CacheProcessor.STATE_BLOCKED));
      }
   }


   /**
    * {@inheritDoc}
    */
   public void notifyClusterNodeUnblocked() {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Notifying all cache processors that they should un-block");
      }

      // Notify all cache processors that they should un-block
      for (final CacheProcessor cacheProcessor : cacheProcessorMap.values()) {
         cacheProcessor.post(new SetCacheNodeStateMessage(CacheProcessor.STATE_BLOCKED));
      }
   }


   /**
    * {@inheritDoc}.
    * <p/>
    * This implementation resets this cluster node's state that depends on sending mcast messages. It does shutdown and
    * destroy all cache processors.
    */
   public void notifyReset() {

      reentrantLock.lock();
      try {

         // Shutdown all cache processors.

         final Set<Map.Entry<String, CacheProcessor>> entries = cacheProcessorMap.entrySet();
         for (final Iterator<Map.Entry<String, CacheProcessor>> iterator = entries.iterator(); iterator.hasNext(); ) {

            final Map.Entry<String, CacheProcessor> entry = iterator.next();
            final CacheProcessor cacheProcessor = entry.getValue();

            LOG.debug("Re-setting cache processors: " + entry.getKey());

            // Remove from cluster service

            clusterProcessor.unregisterCacheProcessor(cacheProcessor.getCacheName());
            router.unregister(new CacheProcessorKey(entry.getKey()));

            // Remove from map
            iterator.remove();

            // Shutdown node
            cacheProcessor.shutdown();
         }
      } finally {
         reentrantLock.unlock();
      }
   }


   // ================================================================================================================
   //
   // GroupEventSubscriber
   //
   // ================================================================================================================


   /**
    * {@inheritDoc}
    * <p/>
    * <b>Handling cache processors:</b> <ol> <li>There is no a cache processor. This is a response to our request to add
    * a local node. We simply create a local node.</li> <li>There is no node. This is an announcement of arrival of a
    * remote node.</li> <li>There is node. The request should be forwarded to the node to perform repartitioning.<li>
    * <ol> If this is a response to our request to add a local node we should replace the old node.
    *
    * @see GroupEventSubscriber
    */
   public void notifyGroupMemberJoined(final GroupMemberJoinedEvent event) {

      // Start cache processor
      if (LOG.isDebugEnabled()) {
         LOG.debug("Received: " + event);
      }

      // Check if node exists
      final GroupMember joiningMember = event.getGroupMember();
      final ClusterNodeAddress joiningAddress = joiningMember.getAddress();
      final Group group = joiningMember.getGroup();
      final String cacheName = group.getName();
      final CacheProcessor cacheProcessor = cacheProcessorMap.get(cacheName);

      // Cache processor exists?
      if (cacheProcessor == null) {

         // Check if this a response to our request
         if (address.equals(joiningAddress)) {

            // This is a response to our request to create a local node

            try {

               if (LOG.isDebugEnabled()) {
                  LOG.debug(
                          "Cache member does not exist: " + cacheName + ", Registering cache processor for " + getSummary() + ": " + cacheName);
               }

               final String cacheConfigName = joiningMember.getCacheConfigName();
               final PartitionedCacheConfiguration cacheConfig = serverConfig.getPartitionedCacheType(cacheConfigName);

               // Create cache processor
               final CacheProcessor newCacheProcessor = new CacheProcessorImpl(timer, clock, getPrefetchScheduler(), router,
                       getEventNotificationExecutor(), group, cacheName, address, cacheConfig);
               newCacheProcessor.startup();

               // Register a subscriber to entry modification event subscriber configuration events
               group.setEntryEventSubscriptionConfigurationSubscriber(
                       new EntryEventSubscriptionConfigurationSubscriber(cacheName, clusterProcessor));

               // Register CacheProcessor with the cluster processor
               clusterProcessor.registerCacheProcessor(newCacheProcessor);
               router.register(new CacheProcessorKey(cacheName), newCacheProcessor);

               //  Register cache processor
               reentrantLock.lock();
               try {

                  cacheProcessorMap.put(cacheName, newCacheProcessor);
               } finally {

                  reentrantLock.unlock();
               }

               // Send a repartition announcement to stimulate the RBOAT
               // to send bucket to the new partition contributor.
               if (cacheConfig.isPartitionContributor()) {

                  clusterProcessor.post(new RepartitionAnnouncement(cacheName));
               }

            } catch (final Exception e) {

               // REVIEME: simeshev@cacheonix.org - 2011-07-11 Consider setting up a cache processor that has failed with an error.

               // Report error
               LOG.error(
                       "Error while creating a cache processor '" + cacheName + "', leaving the group: " + StringUtils.toString(
                               e), e);

               // Post an announcement to remove the cache processor
               clusterProcessor.post(new LeaveCacheGroupAnnouncement(cacheName, joiningAddress, false));
            }
         } else {

            // This is not a response to our request.

            // The fact that there is is in the cacheMap means that automatic creating
            // of the cache processor has already been announced. We should not attempt to
            // autocreate a node then.
            if (!cacheMap.containsKey(cacheName)) {

               // A create process for a cache processor has not been started yet.

               // Initiate autocreate of a partition-contributing cache member
               // if there is a create-all template
               final PartitionedCacheConfiguration createAllTemplate = serverConfig.getCreateAllTemplate();
               if (createAllTemplate != null) {

                  beginAutocreateCacheProcessor(group, cacheName, createAllTemplate);
               }
            }
         }
      } else {

         // A cache processor exists
         Assert.assertTrue(!address.equals(joiningAddress), "Member with same address cannot join twice: {0}", event);
         try {

            // Forward the event to the cache processor by converting it to a message
            final CacheNodeJoinedMessage message = new CacheNodeJoinedMessage(cacheName);

            message.setClusterUUID(clusterProcessor.getProcessorState().getClusterView().getClusterUUID());
            message.setCacheGroupMember(event.getGroupMember());
            message.setReceiver(address);

            // Forward it directly to the cache processor
            cacheProcessor.enqueue(message);
         } catch (final InterruptedException ignore) {

            ExceptionUtils.ignoreException(ignore, "Nothing we can do here");
            Thread.currentThread().interrupt();
         }
      }
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This method translates the synchronous GroupMemberLeftEvent to asynchronous CacheNodeLeftMessage or shuts down
    * cache processor.
    */
   public void notifyGroupMemberLeft(final GroupMemberLeftEvent event) {

      try {
         // Check if node exists
         final Group group = event.getGroupMember().getGroup();
         final String cacheName = group.getName();

         if (event.getGroupMember().getAddress().equals(address)) {

            // Stop cache processor if this cluster processor is a home of a local cache processor.

            // Unregister cache processor so that it stops receiving messages.
            final CacheProcessor cacheProcessor = clusterProcessor.unregisterCacheProcessor(cacheName);
            router.unregister(new CacheProcessorKey(cacheName));
            cacheProcessorMap.remove(cacheName);

            // Check if already gone - possible that while this message
            // arrived, the local cache processor has already been killed.
            if (cacheProcessor == null || cacheProcessor.isShutdown()) {
               return;
            }

            // Shutdown cache processor
            try {
               cacheProcessor.shutdown();
            } catch (final Throwable e) { // NOPMD
               ExceptionUtils.ignoreException(e, "Nothing we can do");
            }

            // Unregister a subscriber to entry modification event subscriber configuration events
            group.setEntryEventSubscriptionConfigurationSubscriber(null);


         } else {

            final CacheProcessor cacheProcessor = cacheProcessorMap.get(cacheName);
            if (cacheProcessor == null) {

               // NOTE: simeshev@cacheonix.org - 2010-02-15 - We can do it because
               // the events are processed sequentially. If the node is gone, it may
               // appear only after this event was processed.
               LOG.debug("Ignoring this notification because there is no a cache processor \""
                       + cacheName + "\", yet or already, to process it: " + event);
            } else {

               // Convert the event to the messages and send it to self.
               final CacheNodeLeftMessage message = new CacheNodeLeftMessage(cacheName);

               message.setClusterUUID(clusterProcessor.getProcessorState().getClusterView().getClusterUUID());
               message.setCacheGroupMember(event.getGroupMember());
               message.setReceiver(address);
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Forwarding to the cache processor : " + event);
               }

               // Forward directly to the cache processor queue
               cacheProcessor.enqueue(message);
            }
         }
      } catch (final InterruptedException e) {
         LOG.warn(e, e);
         Thread.currentThread().interrupt();
      }
   }


   /**
    * {@inheritDoc}
    */
   public void notifyGroupMemberFailedToJoin(final GroupMemberFailedToJoinEvent event) {

      if (LOG.isDebugEnabled()) {
         LOG.debug("Received: " + event);
      }
      // TODO: simeshev@cacheonix.org - 2009-11-07 - Decide what to do with it
   }


   public ClusterProcessor getClusterProcessor() {

      return clusterProcessor;
   }


   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   public void shutdown(final ShutdownMode shutdownMode, final boolean unregisterSingleton) {

      LOG.info("Shutting down " + getDescription());
      if (SystemProperty.isPrintStacktraceAtCacheonixShutdown()) {
         LOG.debug("Stack trace at Cacheonix shutdown", new Throwable());
      }

      writeLock.lock();
      try {

         // Check if already shutdown
         if (shutdown) {
            throw new ShutdownException("This Cacheonix instance has already been shutdown");
         }

         // Unregister shutdown hook
         unregisterShutdownHook();

         // Shutdown cluster processor
         if (ShutdownMode.GRACEFUL_SHUTDOWN.equals(shutdownMode)) {

            try {

               clusterProcessor.shutdown(ShutdownMode.GRACEFUL_SHUTDOWN);
            } catch (final Exception e) {

               IOUtils.ignoreExpectedException(e);
            }

            IOUtils.shutdownHard(multicastServer);
            IOUtils.shutdownHard(tcpServer);
            IOUtils.shutdownHard(messageSender);

         } else if (ShutdownMode.FORCED_SHUTDOWN.equals(shutdownMode)) {


            // Immediately shutdown the receivers
            IOUtils.shutdownHard(multicastServer);
            IOUtils.shutdownHard(tcpServer);

            // Shutdown cluster processor
            try {

               clusterProcessor.shutdown(ShutdownMode.FORCED_SHUTDOWN);
            } catch (final Exception e) {
               IOUtils.ignoreExpectedException(e);
            }

            // Finally, the message sender
            IOUtils.shutdownHard(messageSender);

         } else {
            throw new IllegalArgumentException("Unknown shutdown mode: " + shutdownMode);
         }

         // Remove it from the static cache manager registry
         if (unregisterSingleton) {

            unregister(this);
         }

         // Shutdown prefetch scheduler
         getPrefetchScheduler().shutdown();

         // Shutdown thread pool
         getThreadPoolExecutor().shutdownNow();

         // Destroy timer
         getTimer().cancel();

         // Terminate event notification executor
         getEventNotificationExecutor().shutdownNow();

         // Set shutdown flag
         shutdown = true;

         LOG.info(getDescription() + " has been shutdown");
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * @param cacheName    name of the cache to create.
    * @param templateName name of the template to use when creating the cache.
    * @return a new Cache.
    * @throws IllegalArgumentException
    */
   public final Cache createCache(final String cacheName, final String templateName)
           throws IllegalArgumentException {

      writeLock.lock();
      try {

         if (cacheExists(cacheName)) {

            throw new IllegalArgumentException("Cache already exists: " + cacheName);
         }

         // Find existing configuration
         final PartitionedCacheConfiguration cacheConfiguration = (PartitionedCacheConfiguration) cacheConfigMap.get(
                 cacheName);
         if (cacheConfiguration != null) {

            return createAndRegisterCache(cacheName, cacheConfiguration, false);
         }

         // Use template configuration
         final PartitionedCacheConfiguration defaultConfiguration = (PartitionedCacheConfiguration) cacheConfigMap.get(
                 templateName);
         if (defaultConfiguration == null) {
            throw new IllegalArgumentException(
                    "Cannot create cache \"" + cacheName + "\": cache configuration not found and default configuration template is not set");
         }
         return createAndRegisterCache(cacheName, defaultConfiguration, true);
      } finally {
         writeLock.unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public Cluster getCluster() {

      return new ClusterImpl(clusterProcessor, serverConfig.getDefaultUnlockTimeoutMillis());
   }


   /**
    * @noinspection ArithmeticOnVolatileField
    */
   public String toString() {

      return "DistributedCacheonix{" +
              "started=" + started +
              ", clusterProcessor=" + clusterProcessor +
              ", tcpServer=" + tcpServer +
              ", serverConfig=" + serverConfig +
              ", messageSender=" + messageSender +
              ", address=" + address +
              ", replicatedState=" + replicatedState +
              ", router=" + router +
              ", multicastSender=" + multicastSender +
              ", multicastServer=" + multicastServer +
              "} " + super.toString();
   }
}
