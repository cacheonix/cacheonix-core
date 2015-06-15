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
package org.cacheonix.impl.net.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;

import org.cacheonix.impl.net.Protocol;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.ArrayUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Cacheonix's wire-level message. That's what gets written to the OutputStream and read from input stream in one
 * chunk.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @noinspection RedundantIfStatement
 */
public final class Frame {

   /**
    * This is a calculated header length. Do not change it!
    */
   public static final int HEADER_LENGTH = 82;

   /**
    * Maximum multicast datagram size.
    */
   public static final int MAXIMUM_MULTICAST_PACKET_SIZE = 1468;

   /**
    * Maximum length produced by toBytes().
    * <p/>
    * This value is used for self-test and by the payload partitioner.
    */
   public static final int MAXIMUM_MCAST_MESSAGE_LENGTH = MAXIMUM_MULTICAST_PACKET_SIZE;


   /**
    * Maximum part length.
    * <p/>
    * This value is used for self-test and by the payload partitioner.
    */
   public static final int MAXIMUM_MCAST_PAYLOAD_LENGTH = MAXIMUM_MCAST_MESSAGE_LENGTH - HEADER_LENGTH;

   /**
    * Protocol signature as a byte array.
    */
   private static final byte[] PROTOCOL_SIGNATURE_BYTES = Protocol.getProtocolSignature();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Frame.class); // NOPMD

   /**
    * Frame content is not compressed.
    */
   public static final byte NO_COMPRESSION = 1;

   /**
    * Frame content is compressed.
    *
    * @noinspection UnusedDeclaration
    */
   public static final byte COMPRESSION = 2;

   /**
    * Maximum length
    */
   private int maximumMessageLength = 0;

   /**
    * A quick message type look up handler. Message type can be used for custom serialization to avoid writing class
    * names produced by an ObjectOutputStream when writing an Externalizable messages.
    */
   private int messageType = -1;

   /**
    * An IP address of the machine that sent this frame. This field is set only if this is a received frame and it was
    * received from a remote machine.
    */
   private InetAddress senderInetAddress;


   /**
    * A serializer used to create payload.
    */
   private byte serializerType = Serializer.TYPE_UNKNOWN;

   /**
    * Compression used to create payload.
    */
   private byte compressionType = NO_COMPRESSION;

   /**
    * Sequence number.
    */
   private long sequenceNumber = -1L;

   /**
    * Number of parts.
    */
   private int partCount = 1;

   /**
    * Part index
    */
   private int partIndex = 0;

   /**
    * Payload length
    */
   private int payloadLength = 0;

   /**
    * Payload.
    */
   private byte[] payload = ArrayUtils.EMPTY_BYTE_ARRAY;

   /**
    * Cluster UUID.
    */
   private UUID clusterUUID = null;


   /**
    * Creates an empty frame.
    */
   public Frame() {

   }


   /**
    * Creates an empty frame.
    *
    * @param maximumMessageLength maximum size of payload in bytes.
    */
   public Frame(final int maximumMessageLength) {

      this.maximumMessageLength = maximumMessageLength;
   }


   public Frame(final int maximumMessageLength, final int messageType, final Serializer serializer,
                final byte compressionType, final long sequenceNumber, final Object payloadObject) throws
           IOException {

      this.maximumMessageLength = maximumMessageLength;
      this.messageType = messageType;
      this.serializerType = serializer.getType();
      this.compressionType = compressionType;
      this.sequenceNumber = sequenceNumber;
      this.partCount = 1;
      this.partIndex = 0;
      this.setPayload(serializer.serialize(payloadObject));
   }


   /**
    * @param serializerType
    * @param sequenceNumber
    * @param partCount
    * @param partIndex
    * @param part
    * @throws IllegalArgumentException if part longer then allowed
    */
   public Frame(final int maximumMessageLength, final byte serializerType,
                final long sequenceNumber, final int partCount, final int partIndex,
                final byte[] part) throws IllegalArgumentException {

      this.maximumMessageLength = maximumMessageLength;
      this.serializerType = serializerType;
      this.sequenceNumber = sequenceNumber;
      this.partCount = partCount;
      this.partIndex = partIndex;
      this.setPayload(part);
   }


   /**
    * Returns packet type.
    *
    * @return packet type.
    */
   public int getMessageType() {

      return messageType;
   }


   /**
    * Returns serialization type.
    *
    * @return serialization type.
    */
   public byte getSerializerType() {

      return serializerType;
   }


   /**
    * Returns this message number.
    *
    * @return this message number.
    */
   public long getSequenceNumber() {

      return sequenceNumber;
   }


   /**
    * Sets this message number.
    *
    * @param sequenceNumber this multicast message's unique message number.
    */
   public void setSequenceNumber(final long sequenceNumber) {

      if (this.sequenceNumber == -1) {
         this.sequenceNumber = sequenceNumber;
      } else {
         throw new IllegalStateException("Sequence number can be set only once: " + sequenceNumber);
      }
   }


   /**
    * Sets an IP address of the machine that sent this frame.
    *
    * @param senderInetAddress the IP address of the machine that sent this frame.
    */
   public void setSenderInetAddress(final InetAddress senderInetAddress) {

      this.senderInetAddress = senderInetAddress;
   }


   /**
    * Returns the IP address of the machine that sent this frame. The IP address of the sender is not null only if this
    * is a received frame and it was received from a remote machine.
    *
    * @return the IP address of the machine that sent this frame. The IP address of the sender is not null only if this
    *         is a received frame and it was received from a remote machine.
    */
   public InetAddress getSenderInetAddress() {

      return senderInetAddress;
   }


   /**
    * Number of parts.
    *
    * @return number of parts.
    * @see #getPartCount()
    */
   public int getPartCount() {

      return partCount;
   }


   /**
    * Returns part index.
    *
    * @return part index.
    * @see #getPartCount()
    */
   public int getPartIndex() {

      return partIndex;
   }


   /**
    * @return actual part content.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public byte[] getPayload() {

      return payload; // NOPMD
   }


   public void setMaximumMessageLength(final int maximumMessageLength) {

      this.maximumMessageLength = maximumMessageLength;
   }


   public UUID getClusterUUID() {

      return clusterUUID;
   }


   public void setClusterUUID(final UUID clusterUUID) {

      this.clusterUUID = clusterUUID;
   }


   private int getFrameSize() {

      return 1 + 4 + 4 + 1 + 8 + 4 + 4 + 17 + 4 + 18 + payloadLength;
   }


   /**
    * Writes this frame to the output stream
    *
    * @param out OutputStream to write this frame to
    * @throws IOException if an I/O error occurs
    */
   public void write(final OutputStream out) throws IOException {

      final DataOutputStream dos = new DataOutputStream(out);
      writeWire(dos);
   }


   private void writeWire(final DataOutputStream dos) throws IOException {

      //
      writeHeader(dos);

      //
      writeFrame(dos);
   }


   private void writeHeader(final DataOutputStream dos) throws IOException {

      dos.write(PROTOCOL_SIGNATURE_BYTES);
      dos.writeInt(Protocol.getProtocolMagicNumber());
      dos.writeInt(Protocol.getProtocolVersion());
      dos.writeInt(getFrameSize());
   }


   private void writeFrame(final DataOutputStream dos) throws IOException {

      dos.writeByte(serializerType);
      dos.writeInt(maximumMessageLength);
      dos.writeInt(messageType);
      SerializerUtils.writeInetAddress(senderInetAddress, dos, true);
      dos.writeByte(compressionType);
      dos.writeLong(sequenceNumber);
      dos.writeInt(partCount);
      dos.writeInt(partIndex);
      SerializerUtils.writeUuid(clusterUUID, dos);
      dos.writeInt(payloadLength);
      dos.write(payload);
      if (dos.size() > maximumMessageLength) {
         throw new InvalidObjectException(
                 "Message length exceeded maximum allowed " + maximumMessageLength + ": " + dos.size());
      }
   }


   public Frame read(final InputStream inputStream) throws IOException {

      final DataInputStream dis = new DataInputStream(inputStream);
      readWire(dis);
      return this;
   }


   public void readWire(final DataInputStream dis) throws IOException {

      // Read protocol header
      readHeader(dis);


      // Read frame itself
      readFrame(dis);
   }


   private void readHeader(final DataInputStream dis) throws IOException {
      // Validate protocol signature
      for (final byte signatureByte : PROTOCOL_SIGNATURE_BYTES) {
         if (dis.readByte() != signatureByte) {
            throw new IOException("Invalid protocol signature");
         }
      }
      // Validate protocol magic number
      final int protocolMagicNumber = dis.readInt();
      if (protocolMagicNumber != Protocol.getProtocolMagicNumber()) {
         throw new IOException("Invalid protocol magic number: " + protocolMagicNumber);
      }

      // Protocol version
      final int protocolVersion = dis.readInt();
      if (protocolVersion != Protocol.getProtocolVersion()) {
         throw new IOException("Invalid protocol version: " + protocolVersion + ", expected: " + Protocol.getProtocolVersion());
      }

      // Read frame size
      dis.readInt();
   }


   public void readFrame(final DataInputStream dis) throws IOException {

      serializerType = dis.readByte();
      maximumMessageLength = dis.readInt();
      messageType = dis.readInt();
      senderInetAddress = SerializerUtils.readInetAddress(dis, true);
      compressionType = dis.readByte();
      sequenceNumber = dis.readLong();
      partCount = dis.readInt();
      partIndex = dis.readInt();
      clusterUUID = SerializerUtils.readUuid(dis);
      payloadLength = dis.readInt();
      payload = new byte[payloadLength];
      dis.readFully(payload);
   }


   /**
    * Converts the message to a byte array suitable for further sending through the network.
    *
    * @return a byte array suitable for further sending through the network.
    * @throws IOException
    * @noinspection IOResourceOpenedButNotSafelyClosed
    */
   public byte[] toBytes() throws IOException {

      final ByteArrayOutputStream out = new ByteArrayOutputStream(1600);
      write(out);
      return out.toByteArray();
   }


   /**
    * @noinspection IOResourceOpenedButNotSafelyClosed
    */
   public static Frame fromBytes(final byte[] bytes, final int offset, final int length)
           throws IOException {

      final ByteArrayInputStream bais = new ByteArrayInputStream(bytes, offset, length);
      final Frame frame = new Frame();
      frame.read(bais);
      return frame;
   }


   private byte[] validatePayloadSize(final byte[] p) {

      if (p != null) {
         if (p.length + HEADER_LENGTH > maximumMessageLength) {
            throw new IllegalArgumentException(
                    "Part length cannot be longer then " + maximumMessageLength + " but it was " + p.length);
         }
      }
      return p;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final Frame frame = (Frame) o;

      if (compressionType != frame.compressionType) {
         return false;
      }
      if (maximumMessageLength != frame.maximumMessageLength) {
         return false;
      }
      if (messageType != frame.messageType) {
         return false;
      }
      if (partCount != frame.partCount) {
         return false;
      }
      if (partIndex != frame.partIndex) {
         return false;
      }
      if (payloadLength != frame.payloadLength) {
         return false;
      }
      if (sequenceNumber != frame.sequenceNumber) {
         return false;
      }
      if (serializerType != frame.serializerType) {
         return false;
      }
      if (clusterUUID != null ? !clusterUUID.equals(frame.clusterUUID) : frame.clusterUUID != null) {
         return false;
      }
      if (!Arrays.equals(payload, frame.payload)) {
         return false;
      }
      if (senderInetAddress != null ? !senderInetAddress.equals(frame.senderInetAddress) : frame.senderInetAddress != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = maximumMessageLength;
      result = 31 * result + messageType;
      result = 31 * result + (senderInetAddress != null ? senderInetAddress.hashCode() : 0);
      result = 31 * result + (int) serializerType;
      result = 31 * result + (int) compressionType;
      result = 31 * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
      result = 31 * result + partCount;
      result = 31 * result + partIndex;
      result = 31 * result + payloadLength;
      result = 31 * result + (payload != null ? Arrays.hashCode(payload) : 0);
      result = 31 * result + (clusterUUID != null ? clusterUUID.hashCode() : 0);
      return result;
   }


   public static Object getPayload(final Frame frame)
           throws IOException, ClassNotFoundException {

      if (frame.partCount == 1) {
         final SerializerFactory factory = SerializerFactory.getInstance();
         final Serializer serializer = factory.getSerializer(frame.serializerType);
         return serializer.deserialize(frame.payload);
      } else {
         throw new IOException("Cannot get payload from a partial frame: " + frame);
      }
   }


   public void setPayload(final byte serializerType, final Object obj) throws IOException {

      this.serializerType = serializerType;
      final SerializerFactory instance = SerializerFactory.getInstance();
      final Serializer serializer = instance.getSerializer(this.serializerType);
      setPayload(serializer.serialize(obj));
   }


   private void setPayload(final byte[] payloadBytes) {

      this.payload = validatePayloadSize(payloadBytes);
      this.payloadLength = payloadBytes.length;
   }


   @Override
   public String toString() {

      return "Frame{" +
              "sequenceNumber=" + sequenceNumber +
              ", clusterUUID=" + clusterUUID +
              ", compressionType=" + compressionType +
              ", maximumMessageLength=" + maximumMessageLength +
              ", partCount=" + partCount +
              ", partIndex=" + partIndex +
              ", senderInetAddress=" + senderInetAddress +
              ", payload=" + payload.length +
              ", payloadLength=" + payloadLength +
              ", serializerType=" + serializerType +
              '}';
   }
}
