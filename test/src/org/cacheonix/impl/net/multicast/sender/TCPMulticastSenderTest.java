package org.cacheonix.impl.net.multicast.sender;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.cacheonix.TestConstants;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.MulticastFrameMessage;
import org.cacheonix.impl.net.processor.Frame;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.Router;
import org.cacheonix.impl.net.serializer.Serializer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.cacheonix.TestConstants.PORT_7676;
import static org.cacheonix.TestUtils.createTestAddress;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A tester for {@link TCPMulticastSender}.
 */
public final class TCPMulticastSenderTest extends TestCase {


   /**
    * Object under test.
    */
   private TCPMulticastSender sender;

   /**
    * Mock router.
    */
   private Router router;


   public void testSendFrame() throws IOException {

      final ArgumentCaptor<MulticastFrameMessage> routeArgumentCaptor = ArgumentCaptor.forClass(
              MulticastFrameMessage.class);

      final Frame frame = new Frame(Frame.MAXIMUM_MCAST_MESSAGE_LENGTH);
      frame.setPayload(Serializer.TYPE_JAVA, TestConstants.OBJECT_TO_MULTICAST);
      sender.sendFrame(frame);


      // Verify that routed once to the address, and another one to self.
      verify(router, times(2)).route(routeArgumentCaptor.capture());

      // Verify correct messages sent
      final List<MulticastFrameMessage> allRouteArguments = routeArgumentCaptor.getAllValues();
      final MulticastFrameMessage firstMulticastFrameMessage = allRouteArguments.get(0);
      final MulticastFrameMessage secondMulticastFrameMessage = allRouteArguments.get(1);
      assertTrue(firstMulticastFrameMessage.isSendToKnownAddresses());
      assertFalse(secondMulticastFrameMessage.isSendToKnownAddresses());
   }


   public void testToString() throws Exception {

      assertNotNull(sender.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      // Create a list of receiver addresses.
      final ClusterNodeAddress clusterNodeAddress = createTestAddress(PORT_7676);
      final int tcpPort = clusterNodeAddress.getTcpPort();
      final InetAddress[] addresses = clusterNodeAddress.getAddresses();
      final ReceiverAddress receiverAddress = new ReceiverAddress(addresses, tcpPort);
      final List<ReceiverAddress> receiverAddresses = new ArrayList<ReceiverAddress>(1);
      receiverAddresses.add(receiverAddress);

      // Create sender
      router = Mockito.mock(Router.class);
      final ClusterNodeAddress localAddress = createTestAddress(TestConstants.PORT_7677);
      sender = new TCPMulticastSender(router, localAddress, receiverAddresses);
   }


   public void tearDown() throws Exception {

      super.tearDown();
      sender = null;
   }
}