package org.cacheonix.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutput;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;

import static org.cacheonix.cluster.ClusterState.BLOCKED;
import static org.cacheonix.cluster.ClusterState.BLOCKED_CODE;
import static org.cacheonix.cluster.ClusterState.OPERATIONAL;
import static org.cacheonix.cluster.ClusterState.OPERATIONAL_CODE;
import static org.cacheonix.cluster.ClusterState.RECONFIGURING;
import static org.cacheonix.cluster.ClusterState.RECONFIGURING_CODE;
import static org.cacheonix.impl.net.serializer.Serializer.TYPE_JAVA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A Tester for {@link ClusterState}.
 */
public final class ClusterStateTest extends TestCase {

   /**
    * A helper method that serializes, deserializes and asserts that original object and de-serialized object are
    * equal.
    *
    * @param clusterState the ClusterState to process
    */
   private static void writeReadCompare(final ClusterState clusterState) throws IOException {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(clusterState, ser.deserialize(ser.serialize(clusterState)));
   }


   /**
    * Tests that {@link ClusterState#getName()} returns a proper value.
    */
   public void testGetName() {

      assertEquals("Reconfiguring", RECONFIGURING.getName());
      assertEquals("Operational", OPERATIONAL.getName());
      assertEquals("Blocked", BLOCKED.getName());
   }


   /**
    * Tests that {@link ClusterState#equals(Object)} works as expected.
    */
   public void testEquals() {

      assertEquals(RECONFIGURING, RECONFIGURING);
      assertEquals(OPERATIONAL, OPERATIONAL);
      assertEquals(BLOCKED, BLOCKED);
   }


   /**
    * Tests that {@link ClusterState#hashCode()} works as expected.
    */
   public void testHashCode() {

      assertEquals(2, RECONFIGURING.hashCode());
      assertEquals(1, OPERATIONAL.hashCode());
      assertEquals(0, BLOCKED.hashCode());
   }


   /**
    * Tests that {@link ClusterState#readDataInput(DataInput)} works as expected.
    */
   public void testReadDataInputReconfiguring() throws IOException {

      final DataInput dataInput = mock(DataInput.class);
      when(dataInput.readByte()).thenReturn(RECONFIGURING_CODE);
      assertEquals(RECONFIGURING, ClusterState.readDataInput(dataInput));
   }


   /**
    * Tests that {@link ClusterState#readDataInput(DataInput)} works as expected.
    */
   public void testReadDataInputOperational() throws IOException {

      final DataInput dataInput = mock(DataInput.class);
      when(dataInput.readByte()).thenReturn(OPERATIONAL_CODE);
      assertEquals(OPERATIONAL, ClusterState.readDataInput(dataInput));
   }


   /**
    * Tests that {@link ClusterState#readDataInput(DataInput)} works as expected.
    */
   public void testReadDataInputBlocked() throws IOException {

      final DataInput dataInput = mock(DataInput.class);
      when(dataInput.readByte()).thenReturn(BLOCKED_CODE);
      assertEquals(BLOCKED, ClusterState.readDataInput(dataInput));
   }


   /**
    * Tests that {@link ClusterState#writeDataOutput(ClusterState, DataOutput)} works as expected.
    */
   public void testWriteDataOutputOperational() throws IOException {

      final DataOutput dataOutput = mock(DataOutput.class);
      ClusterState.writeDataOutput(OPERATIONAL, dataOutput);
      verify(dataOutput).writeByte(OPERATIONAL_CODE);
   }


   /**
    * Tests that {@link ClusterState#writeDataOutput(ClusterState, DataOutput)} works as expected.
    */
   public void testWriteDataOutputReconfiguring() throws IOException {

      final DataOutput dataOutput = mock(DataOutput.class);
      ClusterState.writeDataOutput(RECONFIGURING, dataOutput);
      verify(dataOutput).writeByte(RECONFIGURING_CODE);
   }


   /**
    * Tests that {@link ClusterState#writeDataOutput(ClusterState, DataOutput)} works as expected.
    */
   public void testWriteDataOutputBlocked() throws IOException {

      final DataOutput dataOutput = mock(DataOutput.class);
      ClusterState.writeDataOutput(BLOCKED, dataOutput);
      verify(dataOutput).writeByte(BLOCKED_CODE);
   }


   /**
    * Tests that {@link ClusterState#writeExternal(ObjectOutput)} works as expected.
    */
   public void testWriteReadExternal() throws IOException {

      writeReadCompare(RECONFIGURING);
      writeReadCompare(OPERATIONAL);
      writeReadCompare(BLOCKED);
   }


   /**
    * Tests that {@link ClusterState#toString()} works as expected.
    */
   public void testToString() {

      assertNotNull(RECONFIGURING.toString());
      assertNotNull(OPERATIONAL.toString());
      assertNotNull(BLOCKED.toString());
   }


   public String toString() {

      return "ClusterStateTest{" +
              "} " + super.toString();
   }
}