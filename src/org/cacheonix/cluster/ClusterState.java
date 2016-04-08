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
package org.cacheonix.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An enumeration of possible cluster states.  Throughout the life of a Cacheonix cluster, the cluster state may change
 * as the following:
 * <p/>
 * BLOCKED -> RECONFIGURING -> OPERATIONAL
 * <p/>
 * or
 * <p/>
 * OPERATIONAL -> RECONFIGURING -> BLOCKED
 * <p/>
 * or
 * <p/>
 * BLOCKED -> RECONFIGURING -> BLOCKED
 * <p/>
 * or
 * <p/>
 * OPERATIONAL -> RECONFIGURING -> OPERATIONAL.
 *
 * @see #BLOCKED
 * @see #OPERATIONAL
 * @see #RECONFIGURING
 */
@SuppressWarnings("RedundantIfStatement")
public final class ClusterState implements Externalizable {

   private static final long serialVersionUID = -7719006305712515961L;

   static final byte BLOCKED_CODE = (byte) 0;

   /**
    * A state describing a blocked cluster. A blocked cluster does not send multicast messages. A blocked cluster
    * unblocks when the its member count exceeds a limit defined by the configuration or a timeout passes.
    */
   public static final ClusterState BLOCKED = new ClusterState(BLOCKED_CODE, "Blocked");

   static final byte OPERATIONAL_CODE = (byte) 1;

   /**
    * A state defining an operational cluster. As the name implies, an operational cluster functions normally.
    */
   public static final ClusterState OPERATIONAL = new ClusterState(OPERATIONAL_CODE, "Operational");

   static final byte RECONFIGURING_CODE = (byte) 2;

   /**
    * A state defining a cluster that has lost one of its members and that is now restoring its operational state.
    */
   public static final ClusterState RECONFIGURING = new ClusterState(RECONFIGURING_CODE, "Reconfiguring");

   private byte code;

   private String name;


   private ClusterState(final byte code, final String name) {

      this.code = code;
      this.name = name;
   }


   /**
    * A public constructor required by {@link Externalizable}. Do not use this constructor to create instances of
    * <code>ClusterState</code>. Use enumeration constants defined in <code>ClusterState</code> instead.
    *
    * @see #BLOCKED
    * @see #OPERATIONAL
    * @see #RECONFIGURING
    */
   @SuppressWarnings("UnusedDeclaration")
   public ClusterState() {

   }


   /**
    * Reads a cluster state from data input.
    *
    * @param input a data input to read from.
    * @return a data input stream.
    * @throws IOException if there was an error reading from the data input.
    */
   public static ClusterState readDataInput(final DataInput input) throws IOException {

      final byte code = input.readByte();
      switch (code) {
         case BLOCKED_CODE:
            return BLOCKED;
         case OPERATIONAL_CODE:
            return OPERATIONAL;
         case RECONFIGURING_CODE:
            return RECONFIGURING;
         default:
            throw new IOException("Invalid ClusterState code in the input stream: " + code);
      }
   }


   /**
    * Writes a cluster state to data output.
    *
    * @param clusterState a cluster state to write.
    * @param output       an output to write to.
    * @throws IOException if there was an error reading from the data input.
    */
   public static void writeDataOutput(final ClusterState clusterState, final DataOutput output) throws IOException {

      output.writeByte(clusterState.code);
   }


   public String getName() {

      return name;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ClusterState that = (ClusterState) o;

      if (code != that.code) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return (int) code;
   }


   /**
    * The object implements the writeExternal method to save its contents by calling the methods of DataOutput for its
    * primitive values or calling the writeObject method of ObjectOutput for objects, strings, and arrays.
    *
    * @param out the stream to write the object to
    * @throws IOException Includes any I/O exceptions that may occur
    * @serialData Overriding methods should use this tag to describe the data layout of this Externalizable object. List
    * the sequence of element types and, if possible, relate the element to a public/protected field and/or method of
    * this Externalizable class.
    */
   public void writeExternal(final ObjectOutput out) throws IOException {

      writeDataOutput(this, out);
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException if I/O errors occur
    */
   public void readExternal(final ObjectInput in) throws IOException {

      code = in.readByte();
      switch (code) {
         case BLOCKED_CODE:
            name = BLOCKED.name;
            break;
         case OPERATIONAL_CODE:
            name = OPERATIONAL.name;
            break;
         case RECONFIGURING_CODE:
            name = RECONFIGURING.name;
            break;
         default:
            throw new IOException("Invalid ClusterState code in the input stream: " + code);
      }
   }


   public String toString() {

      return "ClusterState{" +
              "code=" + code +
              ", name='" + name + '\'' +
              '}';
   }
}
