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
package org.cacheonix.impl.net.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ThreadFactory;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.ShutdownMode;
import org.cacheonix.TestConstants;
import org.cacheonix.TestUtils;
import org.cacheonix.impl.clock.Clock;
import org.cacheonix.impl.clock.ClockImpl;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.cluster.node.state.ReplicatedState;
import org.cacheonix.impl.config.ConfigurationConstants;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.multicast.sender.PlainMulticastSender;
import org.cacheonix.impl.net.multicast.server.MulticastServer;
import org.cacheonix.impl.net.multicast.server.MulticastServerImpl;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.tcp.Receiver;
import org.cacheonix.impl.net.tcp.Sender;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.DaemonThreadFactory;

/**
 * MulticastConnectionImpl Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @noinspection ProhibitedExceptionDeclared, JUnitTestMethodWithNoAssertions @since <pre>03/21/2008</pre>
 */
public final class ClusterProcessorImplTest extends CacheonixTestCase {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ClusterProcessorImplTest.class); // NOPMD

   private static final int BASE_TCP_PORT = TestConstants.PORT_7676;

   private static final int MULTICAST_PORT = TestConstants.MULTICAST_PORT;

   private static final int MULTICAST_TTL = TestConstants.MULTICAST_TTL;

   private static final long HOME_ALONE_TIMEOUT_MILLIS = 2000L;

   private static final long GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS = 30000L;

   private static final long NETWORK_TIMEOUT_MILLIS = GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS;

   private static final int PROCESS_COUNT = 3;

   private static final String LOCALHOST = TestConstants.LOCALHOST;

   private static final String MULTICAST_ADDRESS = TestConstants.MULTICAST_ADDRESS;

   private static final int SINGLE_PART_PACKET_SIZE = Frame.MAXIMUM_MULTICAST_PACKET_SIZE / 3;

   private static final long WORST_CASE_LATENCY_MILLIS = GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS;

   private static final long SELECTOR_TIMEOUT_MILLIS = ConfigurationConstants.DEFAULT_SELECTOR_TIMEOUT_MILLIS;

   private static final long CLUSTER_ANNOUNCEMENT_TIMEOUT_MILLS = 100L;

   private static final long CLUSTER_SURVEY_TIMEOUT_MILLS = 500L;

   private static final String TEST_CLUSTER_NAME = "Test Cluster Name";

   private final List<ClusterProcessor> clusterProcessors = new ArrayList<ClusterProcessor>(PROCESS_COUNT);

   private final List<ClusterNodeAddress> processes = createProcesses();

   private final List<Receiver> servers = new ArrayList<Receiver>(PROCESS_COUNT);

   private final List<TestMarkerCountingRequestDispatcher> messageHandlers = new ArrayList<TestMarkerCountingRequestDispatcher>(
           PROCESS_COUNT);

   private final List<TestMulticastMessageListener> mcastMessageListeners = new ArrayList<TestMulticastMessageListener>(
           PROCESS_COUNT);

   private final List<Sender> senders = new ArrayList<Sender>(PROCESS_COUNT);

   private final List<Clock> clocks = new ArrayList<Clock>(PROCESS_COUNT);

   private final List<MulticastServer> multicastServers = new ArrayList<MulticastServer>(PROCESS_COUNT);


   public void testMessagesGetSentInParallelSent() throws InterruptedException, IOException {

      // Send in threads
      final Thread[] mcastClientThreads = new Thread[PROCESS_COUNT];
      final int timeTimeToRunSecs = 2;
      final ThreadFactory threadFactory = new DaemonThreadFactory("Sender");
      for (int i = 0; i < PROCESS_COUNT; i++) {

         final Runnable sender = new TimedMulticastMessageSender(getClusterProcessor(i), i, timeTimeToRunSecs);
         final Thread mcastClientThread = threadFactory.newThread(sender);
         mcastClientThreads[i] = mcastClientThread;
         mcastClientThread.start();
      }

      // Wait for completion of sender threads
      for (final Thread mcastClientThread : mcastClientThreads) {

         mcastClientThread.join();
      }

      Thread.sleep(3000);

      for (int i = 0; i < PROCESS_COUNT; i++) {
         final int tokenCount = getHandler(i).getMarkerCount();
         if (LOG.isDebugEnabled()) {
            LOG.debug("tokenCount: " + tokenCount + " for process index: " + i);
         }
      }

      // Assert
      final int minMessageCount = 10;
      for (int i = 0; i < PROCESS_COUNT; i++) {
         // Sender connection does not receive its messages
         final TestMulticastMessageListener mcastMessageListener = getMcastMessageListener(i);
         assertTrue("Message listener # " + i + " should receive min "
                         + minMessageCount + " messages, received: "
                         + mcastMessageListener.getMessageCount(),
                 mcastMessageListener.getMessageCount() >= minMessageCount);

         LOG.debug("Process " + i + " received " + mcastMessageListener.getMessageCount() + " messages");
      }
   }


   public void testNewMemberJoins() throws InterruptedException, IOException {

      // Create process
      final int joinProcessIndex = PROCESS_COUNT;
      final ClusterNodeAddress newAddress = createProcess(joinProcessIndex);
      processes.add(newAddress);

      //
      final UUID initialClusterUUID = UUID.randomUUID();

      // Create router
      final Router router = new Router(newAddress);
      router.setClusterUUID(initialClusterUUID);

      // Create connection objects
      final ClusterProcessor newProcessor = createClusterProcessor(newAddress, InetAddress.getByName(MULTICAST_ADDRESS),
              MULTICAST_PORT, router, initialClusterUUID);
      final ReplicatedState replicatedState = new ReplicatedState();


      newProcessor.getProcessorState().setReplicateState(replicatedState);
      router.register(ClusterProcessorKey.getInstance(), newProcessor);
      router.register(MulticastClientProcessorKey.getInstance(), newProcessor);
      router.register(ReplicatedStateProcessorKey.getInstance(), newProcessor);

      clusterProcessors.add(newProcessor);

      // Create TCP server that handles TCP communications for multicast connection
      final TestMarkerCountingRequestDispatcher messageHandler = new TestMarkerCountingRequestDispatcher(
              joinProcessIndex, getClusterProcessor(joinProcessIndex));
      messageHandlers.add(messageHandler);

      // Create clock
      final Timer timer = new Timer();
      final Clock clock = new ClockImpl(1000L).attachTo(timer);

      // Create TCP Server
      final Receiver receiver = new Receiver(clock, LOCALHOST, getAddress(joinProcessIndex).getTcpPort(),
              messageHandler, NETWORK_TIMEOUT_MILLIS, SELECTOR_TIMEOUT_MILLIS);
      receiver.startup();
      servers.add(receiver);


      // Create listener
      final TestMulticastMessageListener mcastMessageListener = new TestMulticastMessageListener(joinProcessIndex);
      mcastMessageListeners.add(mcastMessageListener);
      getClusterProcessor(joinProcessIndex).subscribeMulticastMessageListener(mcastMessageListener);

      // Create
      final ClusterNodeAddress nodeAddress = getClusterProcessor(joinProcessIndex).getAddress();
      final Sender sender = new Sender(nodeAddress, NETWORK_TIMEOUT_MILLIS,
              SELECTOR_TIMEOUT_MILLIS, clock);
      senders.add(sender);
      router.setOutput(sender);
      sender.setRouter(router);
      sender.startup();

      // Join
      if (LOG.isDebugEnabled()) {
         LOG.debug("================================== Begin Join ===================================");
      }
      getClusterProcessor(joinProcessIndex).startup();

      // Create mcast server
      final MulticastServer multicastServer = new MulticastServerImpl(InetAddress.getByName(MULTICAST_ADDRESS),
              MULTICAST_PORT, newAddress.getTcpPort());
      multicastServer.addListener(newProcessor);
      multicastServers.add(multicastServer);
      multicastServer.startup();

      // Let it run for 2 secs
      Thread.sleep(10000L);
      if (LOG.isDebugEnabled()) {
         LOG.debug("================================== Finished waiting ===================================");
      }

      // Assert all are in normal state because we have seen that it can finish successfully even
      // if they are all in recovery state
      for (final ClusterProcessor clusterProcessor : clusterProcessors) {

         assertEquals("Normal", clusterProcessor.getProcessorState().getStateName());
      }

      // Assert token list size
      for (int i = 0; i < clusterProcessors.size(); i++) {

         assertEquals(clusterProcessors.size(),
                 (clusterProcessors.get(i)).getProcessorState().getClusterView().getSize());
      }

      // Assert token counter
      final TestMarkerCountingRequestDispatcher handler = getHandler(joinProcessIndex);
      final int tokenCount = handler.getMarkerCount();
      LOG.debug(
              "tokenCount: " + tokenCount + " for joining process index: " + joinProcessIndex + ", handler index: " + handler
                      .getConnectionIndex());
      assertTrue(tokenCount > 0);
   }


   public void testRingGetsReformedUponFailure() throws InterruptedException {

      Thread.sleep(100L);
      getMulticastServer(1).shutdown();
      getTCPServer(1).shutdown();
      senders.get(1).shutdown();
      getClusterProcessor(1).shutdown(ShutdownMode.FORCED_SHUTDOWN);

      final long timeoutToStabilize = 3000L;
      if (LOG.isDebugEnabled()) {
         LOG.debug("Begin to wait for " + timeoutToStabilize + " milliseconds for cluster to stabilize"); // NOPMD
      }
      Thread.sleep(timeoutToStabilize);
      if (LOG.isDebugEnabled()) {
         LOG.debug("Finished waiting for " + timeoutToStabilize + " milliseconds for cluster to stabilize"); // NOPMD
      }
   }


   public void testMarkerGetsPassed() throws InterruptedException {

      Thread.sleep(1000L);
      for (int i = 0; i < PROCESS_COUNT; i++) {
         final int tokenCount = getHandler(i).getMarkerCount();
         if (LOG.isDebugEnabled()) {
            LOG.debug("tokenCount: " + tokenCount + " for process index: " + i);
         }
         assertTrue(tokenCount > 0);
      }

      // Check times are synchronized
      final List<Time> times = new ArrayList<Time>(PROCESS_COUNT);
      for (int i = 0; i < PROCESS_COUNT; i++) {

         times.add(clocks.get(0).currentTime());
      }

      for (final Time time1 : times) {

         for (final Time time2 : times) {

            final Time diff = time1.subtract(time2);

            assertEquals(0, diff.getMillis());
            assertTrue(Math.abs(diff.getCount()) <= 2);
         }
      }
   }


   public void testMessagesGetSent() throws InterruptedException, IOException {

      // Send
      final int messageCount = 10;
      final int senderConnection = 1;
      for (int messageNumber = 0; messageNumber < messageCount; messageNumber++) {

         final String payload = TestUtils.makeTestObject(SINGLE_PART_PACKET_SIZE);
         final TestMessage message = new TestMessage(senderConnection, messageNumber, payload);
         getClusterProcessor(senderConnection).post(message);
      }

      // Cool-down
      Thread.sleep(1000L);

      // Assert
      for (int i = 0; i < PROCESS_COUNT; i++) {
         // Sender connection does not receive its messages
         if (i != senderConnection) {
            assertEquals("Message listener # " + i
                            + " should receive " + messageCount + " messages",
                    messageCount, getMcastMessageListener(i).getMessageCount());
         }
      }
   }


   protected void setUp() throws Exception {

      super.setUp();

      // Create connection objects
      for (int i = 0; i < PROCESS_COUNT; i++) {

         final InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
         final ClusterNodeAddress nodeAddress = getAddress(i);
         final int tcpPort = nodeAddress.getTcpPort();

         //
         final UUID initialClusterUUID = UUID.randomUUID();

         // Create router
         final Router router = new Router(nodeAddress);
         router.setClusterUUID(initialClusterUUID);

         // Marker list with process # 0 as originator
         final int multicastPort = MULTICAST_PORT;
         final ClusterProcessor clusterProcessor = createClusterProcessor(nodeAddress, multicastAddress, multicastPort,
                 router, initialClusterUUID);


         clusterProcessor.getProcessorState().setReplicateState(new ReplicatedState());
         clusterProcessors.add(clusterProcessor);
         router.register(ClusterProcessorKey.getInstance(), clusterProcessor);
         router.register(MulticastClientProcessorKey.getInstance(), clusterProcessor);
         router.register(ReplicatedStateProcessorKey.getInstance(), clusterProcessor);

         //
         final TestMulticastMessageListener mcastMessageListener = new TestMulticastMessageListener(PROCESS_COUNT);
         mcastMessageListeners.add(mcastMessageListener);
         getClusterProcessor(i).subscribeMulticastMessageListener(mcastMessageListener);

         // Create clock
         final Timer timer = new Timer();
         final Clock clock = new ClockImpl(1000L).attachTo(timer);
         clocks.add(clock);

         final Sender sender = new Sender(nodeAddress, NETWORK_TIMEOUT_MILLIS,
                 SELECTOR_TIMEOUT_MILLIS, clock);
         sender.setRouter(router);
         router.setOutput(sender);
         senders.add(sender);
         sender.startup();

         // Create TCP server that handle TCP communications
         final TestMarkerCountingRequestDispatcher messageHandler = new TestMarkerCountingRequestDispatcher(i,
                 clusterProcessors.get(i));
         messageHandlers.add(messageHandler);
         final Receiver receiver = new Receiver(clock, LOCALHOST, nodeAddress.getTcpPort(), messageHandler,
                 NETWORK_TIMEOUT_MILLIS, SELECTOR_TIMEOUT_MILLIS);
         servers.add(receiver);
         receiver.startup();

         getClusterProcessor(i).startup();

         // Create mcast server
         final MulticastServer multicastServer = new MulticastServerImpl(multicastAddress, multicastPort, tcpPort);
         multicastServers.add(multicastServer);
         multicastServer.addListener(clusterProcessor);
         multicastServer.startup();
      }

      // Let cluster form
      Thread.sleep(2000L);
   }


   private Receiver getTCPServer(final int i) {

      return servers.get(i);
   }


   private MulticastServer getMulticastServer(final int i) {

      return multicastServers.get(i);
   }


   private ClusterNodeAddress getAddress(final int i) {

      return processes.get(i);
   }


   /**
    * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
    */
   protected void tearDown() throws Exception {

      shutdownClusterServices();
      shutdownMulticastServers();
      shutdownTCPServers();
      for (final Sender sender : senders) {
         sender.shutdown();
      }
      super.tearDown();
   }


   private void shutdownTCPServers() {

      for (int i = 0; i < servers.size(); i++) {
         final Receiver server = getTCPServer(i);
         if (!server.isShutDown()) {
            server.shutdown();
         }
      }
   }


   private void shutdownMulticastServers() {

      for (int i = 0; i < servers.size(); i++) {
         IOUtils.shutdownHard(getMulticastServer(i));
      }
   }


   private void shutdownClusterServices() {

      for (int i = 0; i < clusterProcessors.size(); i++) {
         final ClusterProcessor clusterProcessor = getClusterProcessor(i);
         if (!clusterProcessor.isShutdown()) {
            clusterProcessor.shutdown();
         }
      }
   }


   private static List<ClusterNodeAddress> createProcesses() {

      final List<ClusterNodeAddress> result = new ArrayList<ClusterNodeAddress>(PROCESS_COUNT);
      for (int i = 0; i < PROCESS_COUNT; i++) {
         try {
            result.add(createProcess(i));
         } catch (final IOException e) {
            throw ExceptionUtils.createIllegalStateException(e);
         }
      }
      return result;
   }


   private static ClusterNodeAddress createProcess(final int i) throws IOException {

      return ClusterNodeAddress.createAddress(LOCALHOST, BASE_TCP_PORT + i
      );
   }


   private TestMarkerCountingRequestDispatcher getHandler(final int i) {

      return messageHandlers.get(i);
   }


   private ClusterProcessor getClusterProcessor(final int senderConnection) {

      return clusterProcessors.get(senderConnection);
   }


   private TestMulticastMessageListener getMcastMessageListener(final int i) {

      return mcastMessageListeners.get(i);
   }


   private ClusterProcessor createClusterProcessor(final ClusterNodeAddress newAddress,
           final InetAddress multicastAddress, final int multicastPort,
           final Router router, final UUID initialClusterUUID) throws IOException {

      final PlainMulticastSender multicastSender = new PlainMulticastSender(multicastAddress, multicastPort,
              MULTICAST_TTL);

      return new ClusterProcessorImpl(TEST_CLUSTER_NAME, getClock(), getTimer(), router, multicastSender, newAddress,
              HOME_ALONE_TIMEOUT_MILLIS, WORST_CASE_LATENCY_MILLIS, GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS,
              CLUSTER_SURVEY_TIMEOUT_MILLS, CLUSTER_ANNOUNCEMENT_TIMEOUT_MILLS, initialClusterUUID);
   }


   /**
    * This listener is called when a message is received by a multicast connection.
    */
   @SuppressWarnings("UnusedDeclaration")
   private static final class TestMulticastMessageListener implements MulticastMessageListener {

      /**
       * Contains message counters indexed by a message sender process ID.
       */
      private final int[] messageSequenceNumbers;

      private int messageCount = 0;

      private int resetCount = 0;

      private IOException error = null;


      TestMulticastMessageListener(final int messageSenderCount) {

         this.messageSequenceNumbers = new int[messageSenderCount];

         Arrays.fill(messageSequenceNumbers, 0);
      }


      public final int getMessageCount() throws IOException {

         if (error != null) {
            throw error;
         }
         return messageCount;
      }


      public int getResetCount() {

         return resetCount;
      }


      public final void receive(final Message message) {

         // doesn't make sense to continue;
         if (error != null) {

            return;
         }

         final TestMessage testMessage = (TestMessage) message;
         final int senderID = testMessage.getSenderID();
         final int messageIndex = testMessage.getMessageNumber();

         // Check sequence
         final int expectedMessageIndex = messageSequenceNumbers[senderID];
         if (messageIndex == expectedMessageIndex) {

            messageSequenceNumbers[senderID] = messageIndex + 1;
         } else {

            error = new IOException("Received message out of order, message source: " + senderID
                    + ", expected message index: " + expectedMessageIndex
                    + ", actual message index: " + messageIndex);
         }
         messageCount++;
      }


      public void notifyClusterNodeJoined(final ClusterNodeJoinedEvent event) {
         // Do nothing
      }


      public void notifyClusterNodeLeft(final ClusterNodeLeftEvent event) {
         // Do nothing
      }


      public void notifyClusterNodeBlocked() {
         // Do nothing
      }


      public void notifyClusterNodeUnblocked() {
         // Do nothing
      }


      public void notifyReset() {

         resetCount++;
      }


      public String toString() {

         return "TestMulticastMessageListener{" +
                 "messageSequenceNumbers=" + Arrays.toString(messageSequenceNumbers) +
                 ", messageCount=" + messageCount +
                 ", resetCount=" + resetCount +
                 ", error=" + error +
                 '}';
      }
   }


   public String toString() {

      return "ClusterProcessorImplTest{" +
              "clusterProcessors=" + clusterProcessors +
              ", processes=" + processes +
              ", servers=" + servers +
              ", messageHandlers=" + messageHandlers +
              ", mcastMessageListeners=" + mcastMessageListeners +
              "} " + super.toString();
   }
}
