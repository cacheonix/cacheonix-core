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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * TestMessage
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 9:38:26 PM
 */
@SuppressWarnings("RedundantIfStatement")
public final class TestMessage extends Message {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(TestMessage.class); // NOPMD

   private int senderID;

   private int messageNumber;

   private String payload = null;


   /**
    * Required by Externalizable.
    */
   public TestMessage() {

   }


   public TestMessage(final int senderID, final int messageNumber, final String payload) {

      super(TYPE_TEST_MESSAGE);
      this.senderID = senderID;
      this.messageNumber = messageNumber;
      this.payload = payload;
   }


   public int getSenderID() {

      return senderID;
   }


   public int getMessageNumber() {

      return messageNumber;
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      senderID = in.readInt();
      messageNumber = in.readInt();
      payload = SerializerUtils.readString(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      out.writeInt(senderID);
      out.writeInt(messageNumber);
      SerializerUtils.writeString(payload, out);
   }


   protected final ProcessorKey getProcessorKey() {

      return MulticastClientProcessorKey.getInstance();
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing becuase this is a marker object.
    */
   public void execute() {

   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      if (!super.equals(o)) {
         return false;
      }

      final TestMessage that = (TestMessage) o;

      if (messageNumber != that.messageNumber) {
         return false;
      }
      if (senderID != that.senderID) {
         return false;
      }
      if (payload != null ? !payload.equals(that.payload) : that.payload != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + senderID;
      result = 31 * result + messageNumber;
      result = 31 * result + (payload != null ? payload.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "TestMessage{" +
              "sourceIndex=" + senderID +
              ", messageNumber=" + messageNumber +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new TestMessage();
      }
   }
}
