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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.Assert;
import org.cacheonix.impl.util.IntegerUtils;
import org.cacheonix.impl.util.array.IntObjectHashMap;
import org.cacheonix.impl.util.array.IntObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;


/**
 * A holder for bucket owner status for a bucket owner belonging to a particular storage.
 *
 * @noinspection ImplicitNumericConversion, RedundantIfStatement, RedundantIfStatement, StandardVariableNames,
 * StandardVariableNames
 */
public final class BucketOwner implements Wireable {

   /**
    * Builder used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * @noinspection UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketOwner.class); // NOPMD

   /**
    * Replica count.
    */
   private byte replicaCount = 0;

   /**
    * This partition contributor address.
    */
   private ClusterNodeAddress address = null;

   /**
    * Flag indicating that the bucket owner is leaving the group.
    */
   private boolean leaving = false;

   /**
    * List of buckets belonging to this partition contributor. A bucket within given storage can belong only to a single
    * partition contributor.
    */
   private final LinkedList<Integer> ownedBuckets = new LinkedList<Integer>(); // NOPMD

   /**
    * @noinspection CollectionDeclaredAsConcreteClass
    */
   private final IntObjectHashMap<BucketTransfer> outboundBuckets = new IntObjectHashMap<BucketTransfer>(11);

   /**
    * @noinspection CollectionDeclaredAsConcreteClass
    */
   private final IntObjectHashMap<BucketTransfer> inboundBuckets = new IntObjectHashMap<BucketTransfer>(11);

   /**
    * An array of maps indexed by a storage number, each contains a list of {@link BucketTransfer} objects describing
    * destination replicas. BucketTransfer's <code>storageNumber</code> defines target replica storage that is greater
    * then zero; <code>bucketNumber</code> defines bucket number being restored; <code>owner</code> defines target
    * bucket owner address.
    * <p/>
    * Outbound replicas can only be a part of a primary bucket owner.
    *
    * @noinspection CollectionDeclaredAsConcreteClass
    */
   private IntObjectHashMap<BucketTransfer>[] outboundReplicas = null;

   /**
    * Contains a list of {@link BucketTransfer} objects describing inbound replicas sent by the primary owner. In other
    * words, <code>inboundReplicas</code> describe transfers from primary bucket owner (storage zero) to the replica
    * owner. This is different from the <code>inboundBuckets</code> that describe load-balancing transfers within the
    * same storage, primary or replica.
    * <p/>
    * BucketTransfer's <code>storageNumber</code> defines a source storage that is always equal zero; The
    * <code>bucketNumber</code> defines a bucket number being restored; <code>owner</code> defines primary bucket owner
    * address.
    * <p/>
    * Inbound replicas can only be a part of a replica bucket owner.
    *
    * @noinspection CollectionDeclaredAsConcreteClass
    */
   private final IntObjectHashMap<BucketTransfer> inboundReplicas = new IntObjectHashMap<BucketTransfer>(11);


   BucketOwner(final byte replicaCount, final ClusterNodeAddress address) {

      this.replicaCount = replicaCount;
      this.address = address;
   }


   /**
    * Default constructor required by <code>Externalizable</code>.
    *
    * @noinspection WeakerAccess
    */
   public BucketOwner() {

   }


   public void addOwnedBucketNumber(final Integer bucketNumber) {

      ownedBuckets.add(bucketNumber);
   }


   public int ownedBucketCount() {

      return ownedBuckets.size();
   }


   private int inboundBucketCount() {

      return inboundBuckets.size();
   }


   private int inboundReplicaCount() {

      return inboundReplicas.size();
   }


   public LinkedList<Integer> getOwnedBuckets() { // NOPMD
      //noinspection ReturnOfCollectionOrArrayField
      return ownedBuckets;
   }


   public ClusterNodeAddress getAddress() {

      return address;
   }


   public void completeOutboundTransfer(final Integer bucketNumber) {

      final BucketTransfer bt = outboundBuckets.remove(bucketNumber);
      Assert.assertNotNull(bt, "Outbound transfer should be registered: {0}", bucketNumber);
   }


   public void registerOutboundReplicaRestore(final byte storageNumber, final int bucketNumber,
                                              final BucketTransfer bucketTransfer) {

      final Object bt = getOrCreateOutboundReplicas(storageNumber).put(bucketNumber, bucketTransfer);
      Assert.assertNull(bt, "Outbound replica should not be registered: {0}", bt);
   }


   public void completeOutboundReplicaRestore(final byte storageNumber, final int bucketNumber) {

      final BucketTransfer bt = getOrCreateOutboundReplicas(storageNumber).remove(bucketNumber);
      Assert.assertNotNull(bt, "Replica restore should be registered for storage: {0}, bucket: {1}, owner: {2}", storageNumber, bucketNumber, this);
   }


   public void completeInboundTransfer(final Integer bucketNumber) {

      final BucketTransfer bt = inboundBuckets.remove(bucketNumber);
      Assert.assertNotNull(bt, "Inbound transfer should be registered for bucket {0}", bucketNumber);
      ownedBuckets.add(bucketNumber);
   }


   public void completeInboundReplicaRestore(final int bucketNumber) {

      final BucketTransfer bucketTransfer = inboundReplicas.remove(bucketNumber);
      Assert.assertNotNull(bucketTransfer, "Inbound replica should be registered for bucket number {0}", bucketNumber);
      ownedBuckets.add(bucketNumber);
   }


   public IntObjectHashMap<BucketTransfer> getInboundBuckets() {
      //noinspection ReturnOfCollectionOrArrayField
      return inboundBuckets;
   }


   public boolean hasInboundBuckets() {

      return !inboundBuckets.isEmpty();
   }


   /**
    * @return a reference to the registry of outbound buckets.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   IntObjectHashMap<BucketTransfer> getOutboundBuckets() {

      return outboundBuckets;
   }


   public boolean hasOutboundBuckets() {

      return !outboundBuckets.isEmpty();
   }


   public void cancelOutboundTransfer(final int bucketNumber) {

      final BucketTransfer bt = outboundBuckets.remove(bucketNumber);
      Assert.assertNotNull(bt, "Outbound transfer should be registered for bucket: {0}", bucketNumber);
      ownedBuckets.add(bucketNumber);
   }


   public void cancelOutboundReplica(final byte storageNumber, final int bucketNumber) {

      final BucketTransfer bt = getOrCreateOutboundReplicas(storageNumber).remove(bucketNumber);
      Assert.assertNotNull(bt, "Outbound replica should be registered, storage: {0}, bucket: {1}", storageNumber, bucketNumber);
   }


   /**
    * Lazy init method. Returns a registry of outbound replicas.
    *
    * @param storageNumber the storage number.
    * @return the registry of outbound replicas.
    */
   public IntObjectHashMap<BucketTransfer> getOrCreateOutboundReplicas(final byte storageNumber) {

      if (replicaCount == 0) {
         return new IntObjectHashMap<BucketTransfer>(0);
      }
      if (outboundReplicas == null) {
         //noinspection unchecked
         outboundReplicas = new IntObjectHashMap[replicaCount];
      }
      final int index = storageNumber - 1;
      final IntObjectHashMap<BucketTransfer> map = outboundReplicas[index];
      if (map == null) {
         final IntObjectHashMap<BucketTransfer> newMap = new IntObjectHashMap<BucketTransfer>(1);
         outboundReplicas[index] = newMap;
         return newMap;
      } else {
         return map;
      }
   }


   public boolean hasOutboundReplicas() {


      if (outboundReplicas == null) {
         return false;
      }

      for (final IntObjectHashMap<BucketTransfer> outboundReplica : outboundReplicas) {

         if (outboundReplica != null && !outboundReplica.isEmpty()) {

            return true;
         }
      }

      return false;
   }


   void registerInboundTransfer(final int bucketNumber, final BucketTransfer transfer) {

      inboundBuckets.put(bucketNumber, transfer);
   }


   public void cancelInboundTransfer(final int bucketNumber) {

      final BucketTransfer bt = inboundBuckets.remove(bucketNumber);
      Assert.assertNotNull(bt, "Inbound transfer should be registered for bucket {0}", bucketNumber);
   }


   public void cancelInboundReplica(final int bucketNumber) {

      final BucketTransfer bucketTransfer = inboundReplicas.remove(bucketNumber);
      Assert.assertNotNull(bucketTransfer, "Inbound replica should ne not null {0}", bucketTransfer);
   }


   public BucketTransfer getOutboundTransfer(final int bucketNumber) {

      if (outboundBuckets.isEmpty()) {
         return null;
      }
      return outboundBuckets.get(bucketNumber);
   }


   /**
    * @return a registry of inbound replicas.
    * @noinspection ReturnOfCollectionOrArrayField
    */
   public IntObjectHashMap<BucketTransfer> getInboundReplicas() {  // NOPMD
      return inboundReplicas;
   }


   public boolean hasInboundReplicas() {

      return !inboundReplicas.isEmpty();
   }


   /**
    * Returns <code>true</code> if the primary owner is restoring any replica for the given bucket number.
    *
    * @param bucketNumber a bucket number to check.
    * @return <code>true</code> if the primary owner is restoring any replica for the given bucket number.
    */
   public boolean isRestoringReplicas(final int bucketNumber) {

      if (outboundReplicas == null) {

         return false;
      }

      for (byte storageNumber = 1; storageNumber <= replicaCount; storageNumber++) {

         final IntObjectHashMap<BucketTransfer> storageOutboundReplicas = outboundReplicas[storageNumber - 1];

         if (storageOutboundReplicas == null) {

            continue;
         }

         if (storageOutboundReplicas.containsKey(bucketNumber)) {

            return true;
         }
      }
      return false;
   }


   /**
    * Returns <code>true</code> if this bucket owner is in process of leaving the group.
    *
    * @return <code>true</code> if this bucket owner is in process of leaving the group.
    */
   public boolean isLeaving() {

      return leaving;
   }


   /**
    * Sets a flag indicating that this bucket owner is in process of leaving the group.
    */
   public void markLeaving() {

      this.leaving = true;
   }


   /**
    * Returns <code>true</code> if owns a bucket, receives buckets or sends buckets out. Returns <code>false</code> if
    * does not have any bucket obligations.
    *
    * @return <code>true</code> if owns a bucket, receives buckets or sends buckets out. Returns <code>false</code> if
    *         does not have any bucket obligations.
    */
   public boolean hasBucketResponsibilities() {

      //
      if (!ownedBuckets.isEmpty()) {
         return true;
      }

      //
      if (!outboundBuckets.isEmpty()) {
         return true;
      }

      //
      if (!inboundBuckets.isEmpty()) {
         return true;
      }

      //
      if (outboundReplicas != null) {

         for (final IntObjectHashMap<BucketTransfer> outboundReplica : outboundReplicas) {

            if (outboundReplica != null && !outboundReplica.isEmpty()) {

               return true;
            }
         }
      }

      //
      if (!inboundReplicas.isEmpty()) {

         return true;
      }

      return false;
   }


   /**
    * Returns current bucket responsibility.
    *
    * @return current bucket responsibility including owned buckets, inbound buckets and inbound replicas.
    */
   public int load() {

      return ownedBucketCount() + inboundBucketCount() + inboundReplicaCount();
   }


   /**
    * Calculates overload of a bucket owner. Overload is a excessive number of bucket that an owner should get rid of to
    * match the given fair buckets per node.
    *
    * @param fairBucketsPerNode fair buckets per node.
    * @return overload of a bucket owner.
    */
   public int overload(final int fairBucketsPerNode) {

      if (leaving) {

         // return the number of buckets this bucket owner should get rid of.
         return ownedBucketCount();

      } else {
         int overload = load() - fairBucketsPerNode;
         if (overload > ownedBucketCount()) {

            overload = ownedBucketCount();
         }
         return overload;
      }
   }


   /**
    * Calculates underload of a bucket owner. Overload is a number of bucket that an owner should take in to match the
    * given fair buckets per node.
    *
    * @param fairBucketsPerNode fair buckets per node
    * @return underload of a bucket owner.
    */
   public int underload(final int fairBucketsPerNode) {

      return fairBucketsPerNode - load();
   }


   /**
    * Returns <code>true</code> if this bucket owner sends out a given bucket number. In other words, it returns
    * <code>true</code> if the bucket number is in {@link #outboundBuckets}.
    *
    * @param bucketNumber the bucket number to check.
    * @return <code>true</code> if this bucket owner sends out a given bucket number. In other words, it returns
    *         <code>true</code> if the bucket number is in {@link #outboundBuckets}.
    */
   public boolean isTransferringBucket(final int bucketNumber) {

      return outboundBuckets.contains(bucketNumber);
   }


   public int getWireableType() {

      return TYPE_BUCKET_OWNER;
   }


   public void readWire(final DataInputStream in) throws IOException {


      leaving = in.readBoolean();
      address = SerializerUtils.readAddress(in);

      //
      final int ownedBucketSize = in.readInt();
      for (int i = 0; i < ownedBucketSize; i++) {
         ownedBuckets.add(IntegerUtils.valueOf(in.readShort()));
      }

      //
      final int outboundBucketsSize = in.readInt();
      for (int i = 0; i < outboundBucketsSize; i++) {
         final int bucketNumber = in.readShort();
         final BucketTransfer bt = new BucketTransfer();
         bt.readWire(in);
         outboundBuckets.put(bucketNumber, bt);
      }


      //
      replicaCount = in.readByte();
      if (replicaCount > 0 && !in.readBoolean()) {
         // Not null
         //noinspection unchecked
         outboundReplicas = new IntObjectHashMap[replicaCount];
         for (byte i = 0; i < replicaCount; i++) {
            if (!in.readBoolean()) {
               // Not null or empty
               final int outboundReplicasSize = in.readInt();
               final IntObjectHashMap<BucketTransfer> map = new IntObjectHashMap<BucketTransfer>(outboundReplicasSize);
               outboundReplicas[i] = map;
               for (int j = 0; j < outboundReplicasSize; j++) {
                  final int bucketNumber = in.readShort();
                  final BucketTransfer bt = new BucketTransfer();
                  bt.readWire(in);
                  map.put(bucketNumber, bt);
               }
            }
         }
      }


      //
      final int inboundBucketsSize = in.readInt();
      for (int i = 0; i < inboundBucketsSize; i++) {
         final int bucketNumber = in.readShort();
         final BucketTransfer bt = new BucketTransfer();
         bt.readWire(in);
         inboundBuckets.put(bucketNumber, bt);
      }

      //
      final int inboundReplicasSize = in.readInt();
      for (int i = 0; i < inboundReplicasSize; i++) {
         final int bucketNumber = in.readShort();
         final BucketTransfer bt = new BucketTransfer();
         bt.readWire(in);
         inboundReplicas.put(bucketNumber, bt);
      }
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      out.writeBoolean(leaving);
      SerializerUtils.writeAddress(address, out);

      //
      final int ownedBucketSize = ownedBuckets.size();
      out.writeInt(ownedBucketSize);
      for (final Integer ownedBucket : ownedBuckets) {
         out.writeShort(ownedBucket);
      }

      //
      final int outboundBucketSize = outboundBuckets.size();
      out.writeInt(outboundBucketSize);
      outboundBuckets.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer bucketTransfer) {

            try {
               out.writeShort(bucketNumber);
               bucketTransfer.writeWire(out);
            } catch (final IOException e) {
               throw new IllegalStateException(e.toString(), e);
            }
            return true;
         }
      });

      //
      out.writeByte(replicaCount);
      if (replicaCount > 0) {
         if (outboundReplicas == null) {
            // Null marker
            out.writeBoolean(true);
         } else {
            out.writeBoolean(false);
            for (byte i = 0; i < replicaCount; i++) {
               final IntObjectHashMap<BucketTransfer> outboundReplicaMap = outboundReplicas[i];
               if (outboundReplicaMap == null) {
                  // Null marker
                  out.writeBoolean(true);
               } else {
                  out.writeBoolean(false);
                  out.writeInt(outboundReplicaMap.size());
                  outboundReplicaMap.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

                     public boolean execute(final int bucketNumber, final BucketTransfer bucketTransfer) {

                        try {
                           out.writeShort(bucketNumber);
                           bucketTransfer.writeWire(out);
                           return true;
                        } catch (final IOException e) {
                           throw new IllegalStateException(e.toString(), e);
                        }
                     }
                  });
               }
            }
         }
      }

      //
      final int inboundBucketsSize = inboundBuckets.size();
      out.writeInt(inboundBucketsSize);
      inboundBuckets.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer bucketTransfer) {

            try {
               out.writeShort(bucketNumber);
               bucketTransfer.writeWire(out);
               return true;
            } catch (final IOException e) {
               throw new IllegalStateException(e.toString(), e);
            }
         }
      });

      //
      final int inboundReplicasSize = inboundReplicas.size();
      out.writeInt(inboundReplicasSize);
      inboundReplicas.forEachEntry(new IntObjectProcedure<BucketTransfer>() {

         public boolean execute(final int bucketNumber, final BucketTransfer bucketTransfer) {

            try {
               out.writeShort(bucketNumber);
               bucketTransfer.writeWire(out);
               return true;
            } catch (final IOException e) {
               throw new IllegalStateException(e.toString(), e);
            }
         }
      });
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final BucketOwner that = (BucketOwner) o;

      if (leaving != that.leaving) {
         return false;
      }
      if (replicaCount != that.replicaCount) {
         return false;
      }
      if (address != null ? !address.equals(that.address) : that.address != null) {
         return false;
      }
      if (!inboundBuckets.equals(that.inboundBuckets)) {
         return false;
      }
      if (!inboundReplicas.equals(that.inboundReplicas)) {
         return false;
      }
      if (!outboundBuckets.equals(that.outboundBuckets)) {
         return false;
      }
      if (!Arrays.equals(outboundReplicas, that.outboundReplicas)) {
         return false;
      }
      if (!ownedBuckets.equals(that.ownedBuckets)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = (int) replicaCount;
      result = 31 * result + (address != null ? address.hashCode() : 0);
      result = 31 * result + (leaving ? 1 : 0);
      result = 31 * result + ownedBuckets.hashCode();
      result = 31 * result + outboundBuckets.hashCode();
      result = 31 * result + inboundBuckets.hashCode();
      result = 31 * result + (outboundReplicas != null ? Arrays.hashCode(outboundReplicas) : 0);
      result = 31 * result + inboundReplicas.hashCode();
      return result;
   }


   @Override
   public String toString() {

      return "BucketOwner{" + "leaving=" + leaving + ", address=" + address + ", replicaCount=" + replicaCount + ", inboundBuckets.size()=" + inboundBuckets.size() + ", inboundReplicas.size()=" + inboundReplicas.size() + ", outboundBuckets.size()=" + outboundBuckets.size() + ", outboundReplicas.length =" + (outboundReplicas == null ? "null" : outboundReplicas.length) + ", ownedBuckets.size()=" + ownedBuckets.size() + '}';
   }


   /**
    * A class factory.
    */
   private static final class Builder implements WireableBuilder {

      public Wireable create() {

         return new BucketOwner();
      }
   }
}
