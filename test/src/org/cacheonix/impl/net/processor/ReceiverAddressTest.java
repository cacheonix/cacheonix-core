package org.cacheonix.impl.net.processor;

import java.net.InetAddress;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

import static org.cacheonix.TestConstants.PORT_7676;
import static org.cacheonix.impl.net.ClusterNodeAddress.createAddress;

/**
 * A tester for {@link ReceiverAddress}.
 */
public final class ReceiverAddressTest extends CacheonixTestCase {


   private ReceiverAddress receiverAddress;

   private InetAddress tcpAddress;


   public void testGetTcpPort() {

      assertEquals(PORT_7676, receiverAddress.getTcpPort());
   }


   public void testGetAddresses() {

      assertEquals(new InetAddress[]{tcpAddress}, receiverAddress.getAddresses());
   }


   public void testWriteReadWire() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(Serializer.TYPE_JAVA);
      assertEquals(receiverAddress, ser.deserialize(ser.serialize(receiverAddress)));
   }


   public void testGetWireableType() {

      assertWireableTypeEquals(291, receiverAddress);
   }


   public void testEquals() {

      assertEquals(receiverAddress, new ReceiverAddress(tcpAddress, PORT_7676));
   }


   public void testHashCode() {

      assertEquals(2130944420, receiverAddress.hashCode());

   }


   public void testIsAddressOf() throws Exception {

      final ClusterNodeAddress address = createAddress("localhost", PORT_7676);
      assertTrue("Unexpected address: " + address, receiverAddress.isAddressOf(address));
   }


   public void testToString() {

      assertNotNull(receiverAddress.toString());
   }


   public void setUp() throws Exception {

      super.setUp();

      tcpAddress = InetAddress.getByName("localhost");
      receiverAddress = new ReceiverAddress(tcpAddress, PORT_7676);
   }


   protected void tearDown() throws Exception {

      receiverAddress = null;
      tcpAddress = null;

      super.tearDown();
   }


   public String toString() {

      return "ReceiverAddressTest{" +
              "receiverAddress=" + receiverAddress +
              ", tcpAddress=" + tcpAddress +
              '}';
   }
}