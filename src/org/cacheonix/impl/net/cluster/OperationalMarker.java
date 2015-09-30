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

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Multicast marker. The multicast marker circulates in the ring in normal mode.
 *
 * @noinspection SimplifiableIfStatement, NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode,
 * RedundantIfStatement
 */
abstract class OperationalMarker extends MarkerRequest {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(OperationalMarker.class); // NOPMD

   private Time nextAnnouncementTime = null;

   private JoiningNode joiningNode = null;

   private ClusterNodeAddress leave = null;

   private ClusterNodeAddress predecessor = null;


   /**
    * Required by Externalizable.
    *
    * @param wireableType the wireable type.
    * @param clusterUUID  UUID of the cluster
    */
   protected OperationalMarker(final int wireableType, final UUID clusterUUID) {

      super(wireableType);
      setClusterUUID(clusterUUID);
   }


   public OperationalMarker(final int wireableType) {

      super(wireableType);
   }


   public final JoiningNode getJoiningNode() {

      return joiningNode;
   }


   public final void setJoiningNode(final JoiningNode joiningNode) {

      this.joiningNode = joiningNode;
   }


   /**
    * Clears fields responsible for handling join, <code>join</code> and <code>predecessor</code>.
    *
    * @see #joiningNode
    * @see #predecessor
    */
   public void clearJoin() {

      this.joiningNode = null;
      this.predecessor = null;
   }


   public final ClusterNodeAddress getLeave() {

      return leave;
   }


   public final void setLeave(final ClusterNodeAddress leave) {

      this.leave = leave;
   }


   public final void setPredecessor(final ClusterNodeAddress predecessor) {

      this.predecessor = predecessor;
   }


   public final ClusterNodeAddress getPredecessor() {

      return predecessor;
   }


   public Time getNextAnnouncementTime() {

      return nextAnnouncementTime;
   }


   public void setNextAnnouncementTime(final Time nextAnnouncementTime) {

      this.nextAnnouncementTime = nextAnnouncementTime;
   }


   public final boolean isLeaveSet() {

      return leave != null;
   }


   public final boolean isJoiningNodeSet() {

      return joiningNode != null;
   }


   /**
    * Requests a marker held by MarkerListRequest.Waiter to perform actions associated with completing the join if any.
    * <p/>
    * This method is called right before forwarding the first operational marker to the joined node.
    * <p/>
    * The context cluster processor is already set when this method is called.
    *
    * @see MarkerListRequest.Waiter#notifyResponseReceived(Response)
    */
   public abstract void finishJoin();


   /**
    * Forwards the marker taking in account leaving status of the context cluster processor.
    *
    * @throws InterruptedException if the context processor is shutting down.
    */
   public abstract void forward() throws InterruptedException;

   /**
    * Reverts changes to fields associated with a join. This method is used when sending a marker list to the joining
    * process fails. See {@link MarkerListRequest.Waiter#rollbackJoin()} for more information.
    */
   public abstract void rollbackJoin();


   /**
    *
    */
   protected abstract void processClusterAnnouncements();


   /**
    * Initiates join process to the member in the cluster announcement.
    *
    * @param member address of the member to join
    */
   protected final void initiateJoinTo(final ClusterNodeAddress member) {

      final ClusterProcessor processor = getClusterProcessor();

      final JoinStatus joinStatus = processor.getProcessorState().getJoinStatus();

      // It is possible that cluster announcement was received, and join initiated *after*
      // we joined a blocked cluster and cleaned the join status. We have to check if the
      // member is not in the cluster view. We have to check for this.

      if (processor.getProcessorState().getClusterView().contains(member)) {
         return;
      }


      LOG.debug("Initiating join to: " + member);

      // Reset timers
      joinStatus.clear();
      joinStatus.setJoiningTo(member);
      joinStatus.getTimeout().reset();

      // Post join request

      final JoinRequest joinRequest = new JoinRequest(member);
      processor.post(joinRequest);

      processor.getProcessorState().getHomeAloneTimeout().reset();
   }


   /**
    * The object implements the readExternal method to restore its contents by calling the methods of DataInput for
    * primitive types and readObject for objects, strings and arrays.  The readExternal method must read the values in
    * the same sequence and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @throws IOException            if I/O errors occur
    * @throws ClassNotFoundException If the class for an object being restored cannot be found.
    */
   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      nextAnnouncementTime = SerializerUtils.readTime(in);
      joiningNode = SerializerUtils.readJoiningNode(in);
      leave = SerializerUtils.readAddress(in);
      predecessor = SerializerUtils.readAddress(in);
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
   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeTime(nextAnnouncementTime, out);
      SerializerUtils.writeJoiningNode(joiningNode, out);
      SerializerUtils.writeAddress(leave, out);
      SerializerUtils.writeAddress(predecessor, out);
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

      final OperationalMarker that = (OperationalMarker) o;

      if (joiningNode != null ? !joiningNode.equals(that.joiningNode) : that.joiningNode != null) {
         return false;
      }
      if (leave != null ? !leave.equals(that.leave) : that.leave != null) {
         return false;
      }
      if (nextAnnouncementTime != null ? !nextAnnouncementTime.equals(that.nextAnnouncementTime) : that.nextAnnouncementTime != null) {
         return false;
      }
      if (predecessor != null ? !predecessor.equals(that.predecessor) : that.predecessor != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = super.hashCode();
      result = 31 * result + (nextAnnouncementTime != null ? nextAnnouncementTime.hashCode() : 0);
      result = 31 * result + (joiningNode != null ? joiningNode.hashCode() : 0);
      result = 31 * result + (leave != null ? leave.hashCode() : 0);
      result = 31 * result + (predecessor != null ? predecessor.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "OperationalMarker{" +
              "predecessor=" + predecessor +
              ", join=" + joiningNode +
              ", leave=" + leave +
              ", nextAnnouncementTime=" + nextAnnouncementTime +
              '}';
   }
}
