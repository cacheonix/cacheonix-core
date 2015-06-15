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
package org.cacheonix.impl.net.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Revival marker.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Apr 5, 2008 6:36:35 PM
 */
public final class RevivalMarker implements Wireable {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(RevivalMarker.class); // NOPMD

   private ClusterNodeAddress originator = null;

   private List newList = null;

   private List visitedList = null;


   public ClusterNodeAddress getOriginator() {

      return originator;
   }


   /**
    * Sets originator.
    *
    * @param originator
    */
   public void setOriginator(final ClusterNodeAddress originator) {

      this.originator = originator;
   }


   /**
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public List getNewList() {

      return newList;
   }


   /**
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public List getVisitedList() {

      return visitedList;
   }


   /**
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setNewList(final List newList) {

      this.newList = newList;
   }


   /**
    * @noinspection AssignmentToCollectionOrArrayFieldFromParameter
    */
   public void setVisitedList(final List visitedList) {

      this.visitedList = visitedList;
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
   public void readWire(final DataInputStream in) throws IOException {

      // Originator
      originator = SerializerUtils.readAddress(in);

      // new list
      final int newListSize = in.readInt();
      newList = new ArrayList(newListSize + 1); // +1 because we know we will add ourselves
      for (int i = 0; i < newListSize; i++) {
         newList.add(SerializerUtils.readAddress(in));
      }
      // visited list
      final int visitedListSize = in.readInt();
      visitedList = new ArrayList(visitedListSize);
      for (int i = 0; i < visitedListSize; i++) {
         visitedList.add(SerializerUtils.readAddress(in));
      }
   }


   public int getWireableType() {

      return TYPE_REVIVAL_MARKER;
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

      // originator
      SerializerUtils.writeAddress(originator, out);

      // new list
      final int newListSize = newList != null ? newList.size() : 0;
      out.writeInt(newListSize);
      for (int i = 0; i < newListSize; i++) {
         SerializerUtils.writeAddress((ClusterNodeAddress) newList.get(i), out);
      }
      // visited list
      final int visitedListSize = visitedList != null ? visitedList.size() : 0;
      out.writeInt(visitedListSize);
      for (int i = 0; i < visitedListSize; i++) {
         SerializerUtils.writeAddress((ClusterNodeAddress) visitedList.get(i), out);
      }
   }


   /**
    * @noinspection RedundantIfStatement
    */
   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final RevivalMarker that = (RevivalMarker) obj;

      if (newList != null ? !newList.equals(that.newList) : that.newList != null) {
         return false;
      }
      if (originator != null ? !originator.equals(that.originator) : that.originator != null) {
         return false;
      }
      if (visitedList != null ? !visitedList.equals(that.visitedList) : that.visitedList != null) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = originator != null ? originator.hashCode() : 0;
      result = 29 * result + (newList != null ? newList.hashCode() : 0);
      result = 29 * result + (visitedList != null ? visitedList.hashCode() : 0);
      return result;
   }


   public String toString() {

      return "RecoveryMarker{" +
              "originator=" + originator +
              ", newList=" + newList +
              ", visitedList=" + visitedList +
              '}';
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new RevivalMarker();
      }
   }
}
