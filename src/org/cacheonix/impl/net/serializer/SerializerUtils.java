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
package org.cacheonix.impl.net.serializer;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cacheonix.impl.RuntimeIOException;
import org.cacheonix.impl.cache.distributed.partitioned.Bucket;
import org.cacheonix.impl.cache.distributed.partitioned.CacheableValue;
import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.clock.TimeImpl;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.net.cluster.JoiningNode;
import org.cacheonix.impl.net.processor.ReceiverAddress;
import org.cacheonix.impl.net.processor.UUID;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.IntegerUtils;
import org.cacheonix.impl.util.array.HashMap;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.array.IntArrayList;
import org.cacheonix.impl.util.array.IntHashSet;
import org.cacheonix.impl.util.array.IntProcedure;
import org.cacheonix.impl.util.array.ObjectProcedure;
import org.cacheonix.impl.util.logging.Logger;

/**
 * SerializationUtils
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection UnusedDeclaration, NumericCastThatLosesPrecision @since Mar 28, 2008 3:55:39 PM
 */
public final class SerializerUtils {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SerializerUtils.class); // NOPMD

   private static WireableFactory WIREABLE_FACTORY = WireableFactory.getInstance();


   /**
    * Utility constructor.
    */
   private SerializerUtils() {

   }


   public static void writeByteArray(final DataOutputStream oos, final byte[] bytes) throws IOException {

      if (bytes == null) {
         oos.writeInt(-1);
      } else {
         oos.writeInt(bytes.length);
         oos.write(bytes);
      }
   }


   public static void writeByteArray(final ObjectOutput oos, final byte[] bytes) throws IOException {

      if (bytes == null) {
         oos.writeInt(-1);
      } else {
         oos.writeInt(bytes.length);
         oos.write(bytes);
      }
   }


   public static byte[] readByteArray(final DataInputStream oi) throws IOException {

      final int length = oi.readInt();
      if (length == -1) {
         return null;
      } else {
         final byte[] result = new byte[length];
         oi.readFully(result);
         return result;
      }
   }


   public static byte[] readByteArray(final ObjectInput oi) throws IOException {

      final int length = oi.readInt();
      if (length == -1) {
         return null;
      } else {
         final byte[] result = new byte[length];
         oi.readFully(result);
         return result;
      }
   }


   public static void writeExternalizableList(final List<Externalizable> list, final ObjectOutput out)
           throws IOException {

      if (list == null) {
         out.writeInt(-1);
      } else {
         final int listSize = list.size();
         out.writeInt(listSize);
         for (final Externalizable aList : list) {
            aList.writeExternal(out);
         }
      }
   }


   public static List<Externalizable> readExternalizableList(final Class<Externalizable> clazz, final ObjectInput in)
           throws IOException, ClassNotFoundException {

      try {
         final int size = in.readInt();
         final List<Externalizable> result = new ArrayList<Externalizable>(size);
         for (int i = 0; i < size; i++) {
            final Externalizable externalizable = clazz.getConstructor().newInstance();
            externalizable.readExternal(in);
            result.add(externalizable);
         }
         return result;
      } catch (final InstantiationException e) {
         throw IOUtils.createIOException(e);
      } catch (final IllegalAccessException e) {
         throw IOUtils.createIOException(e);
      } catch (final NoSuchMethodException e) {
         throw IOUtils.createIOException(e);
      } catch (final InvocationTargetException e) {
         throw IOUtils.createIOException(e);
      }
   }


   public static void writeList(final List<Object> list, final ObjectOutput out)
           throws IOException {

      if (list == null) {
         out.writeInt(-1);
      } else {
         final int listSize = list.size();
         out.writeInt(listSize);
         for (int i = 0; i < listSize; i++) {
            out.writeObject(list.get(0));
         }
      }
   }


   /**
    * Returns an instance of ArrayList or null if a null list was written.
    *
    * @param in input
    * @return an instance of ArrayList or null if a null list was written.
    * @throws IOException            if IO exception occurs while reading the list
    * @throws ClassNotFoundException if a class in the list cannot be found
    */
   public static List<Object> readList(final ObjectInput in) throws IOException, ClassNotFoundException {

      final int listSize = in.readInt();
      if (listSize == -1) {
         return null;
      }
      final List<Object> result = new ArrayList<Object>(listSize);
      for (int i = 0; i < listSize; i++) {
         result.add(in.readObject());
      }
      return result;
   }


   public static void writeCollection(final Collection<Object> collection, final ObjectOutput out)
           throws IOException {

      if (collection == null) {
         out.writeInt(-1);
      } else {
         final int collectionSize = collection.size();
         out.writeInt(collectionSize);
         for (final Object obj : collection) {
            out.writeObject(obj);
         }
      }
   }


   public static Collection<Object> readCollection(final ObjectInput in)
           throws IOException, ClassNotFoundException {

      final int collectionSize = in.readInt();
      if (collectionSize == -1) {
         return null;
      }
      final List<Object> result = new ArrayList<Object>(collectionSize);
      for (int i = 0; i < collectionSize; i++) {
         result.add(in.readObject());
      }
      return result;
   }


   public static void writeUuid(final UUID uuid, final DataOutputStream out) throws IOException {

      if (uuid == null) {
         out.writeBoolean(true); // is null
         // Required by Frame to keep the same size
         out.writeLong(0L);
         out.writeLong(0L);
      } else {
         out.writeBoolean(false);
         out.writeLong(uuid.getMostSignificantBits());
         out.writeLong(uuid.getLeastSignificantBits());
      }
   }


   public static UUID readUuid(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         // Required by Frame to keep the same size
         in.readLong();
         in.readLong();
         return null;
      } else {
         final long mostSignificantBits = in.readLong();
         final long leastSignificantBits = in.readLong();
         return new UUID(mostSignificantBits, leastSignificantBits);
      }
   }


   public static IOException createShouldNotBeSerializedException(final Externalizable object) {

      return new IOException("Class " + object.getClass().getName() + " should never be serialized but it was");
   }


   public static IOException createShouldNotBeDeserializedException(final Object object) {

      return new IOException("Class " + object.getClass().getName() + " should never be deserialized but it was");
   }


   public static ClusterNodeAddress readAddress(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      } else {
         final ClusterNodeAddress result = new ClusterNodeAddress();
         result.readWire(in);
         return result.inline();
      }
   }


   public static void writeAddress(final ClusterNodeAddress address, final DataOutputStream out) throws IOException {

      if (address == null) {
         out.writeBoolean(true); // is null
      } else {
         out.writeBoolean(false);
         address.writeWire(out);
      }
   }


   public static Binary readBinary(final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      } else {
         final int type = in.readInt();
         final WireableFactory wf = WireableFactory.getInstance();
         final Binary binary = (Binary) wf.createWireable(type);
         binary.readWire(in);
         return binary;
      }
   }


   public static void writeBinary(final DataOutputStream out, final Binary binary) throws IOException {

      if (binary == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         out.writeInt(binary.getWireableType());
         binary.writeWire(out);
      }
   }


   /**
    * Creates an instance of this class.
    *
    * @param clazz class to instantiate
    * @return new instance of this class
    * @throws IOException if am error occurs while reading the list
    * @noinspection OverlyBroadCatchBlock
    */
   public static Object newInstance(final Class clazz) throws IOException {

      try {
         return clazz.getConstructor().newInstance();
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw IOUtils.createIOException(e);
      }
   }


   public static Object readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
      // REVIEWME: simeshev@cacheonix.org - 2009-08-06 -> handling nulls, ints and exceptions
      return in.readObject();
   }


   public static void writeObject(final ObjectOutput out, final Object result) throws IOException {
      // REVIEWME: simeshev@cacheonix.org - 2009-08-06 -> handling nulls, ints and exceptions
      out.writeObject(result);
   }


   public static void writeObject(DataOutputStream out, Serializable object) throws IOException {

      if (object == null) {
         out.writeBoolean(true); // is null
      } else {
         out.writeBoolean(false);
         final ObjectOutputStream oos = new ObjectOutputStream(out);
         oos.writeObject(object);
         oos.flush();
         oos.close();
      }
   }


   @SuppressWarnings("unchecked")
   public static <T extends Serializable> T readObject(
           final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      } else {
         final ObjectInputStream ois = new ObjectInputStream(in);
         final T result = (T) ois.readObject();
         ois.close();
         return result;
      }
   }




   public static void writeBucket(final DataOutputStream out, final Bucket bucket) throws IOException {

      if (bucket == null) {
         out.writeBoolean(true); // is null
      } else {
         out.writeBoolean(false);
         bucket.writeWire(out);
      }
   }


   public static Bucket readBucket(final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      } else {
         final Bucket result = new Bucket();
         result.readWire(in);
         return result;
      }
   }


   public static int[] readIntArray(final DataInputStream in) throws IOException {

      final int length = in.readInt();
      if (length == -1) {
         return null;
      } else {
         final int[] result = new int[length];
         for (int i = 0; i < length; i++) {
            result[i] = in.readInt();
         }
         return result;
      }
   }


   public static void writeIntArray(final DataOutputStream output, final int[] ints) throws IOException {

      if (ints == null) {
         output.writeInt(-1);
      } else {
         output.writeInt(ints.length);
         for (final int anInt : ints) {
            output.writeInt(anInt);
         }
      }
   }


   public static int[] readShortArray(final DataInputStream in) throws IOException {

      final int length = in.readInt();
      if (length == -1) {
         return null;
      } else {
         final int[] result = new int[length];
         for (int i = 0; i < length; i++) {
            result[i] = in.readShort();
         }
         return result;
      }
   }


   public static void writeShortArray(final DataOutputStream output, final int[] shorts) throws IOException {

      if (shorts == null) {
         output.writeInt(-1);
      } else {
         output.writeInt(shorts.length);
         for (final int anInt : shorts) {
            output.writeShort(anInt);
         }
      }
   }


   public static Map<Binary, Binary> readBinaryMap(final DataInputStream in) throws IOException,
           ClassNotFoundException {

      final int size = in.readInt();
      final Map<Binary, Binary> result = new HashMap<Binary, Binary>(size);
      for (int i = 0; i < size; i++) {
         final Binary key = readBinary(in);
         final Binary value = readBinary(in);
         result.put(key, value);
      }
      return result;
   }


   public static void writeBinaryMap(final DataOutputStream out, final Map map) throws IOException {

      out.writeInt(map.size());
      for (final Object obj : map.entrySet()) {
         final Entry entry = (Entry) obj;
         writeBinary(out, (Binary) entry.getKey());
         writeBinary(out, (Binary) entry.getValue());
      }
   }


   public static InetAddress readInetAddress(final DataInput in, final boolean fixedLength) throws IOException {


      // Read boolean null value marker
      if (in.readBoolean()) {

         // The address is null
         if (fixedLength) {

            // Read padded bytes
            in.readByte();
            in.readLong();
            in.readLong();
         }

         return null;
      }

      // Read the address
      final byte addrLength = in.readByte();
      switch (addrLength) {
         case 4:

            final int address = in.readInt();
            if (fixedLength) {

               // Pad 12 bytes
               in.readInt();
               in.readLong();
            }
            final byte[] addr = new byte[4];
            addr[0] = (byte) (address >>> 24 & 0xFF);
            addr[1] = (byte) (address >>> 16 & 0xFF);
            addr[2] = (byte) (address >>> 8 & 0xFF);
            addr[3] = (byte) (address & 0xFF);
            return InetAddress.getByAddress(addr);
         case 16:

            final byte[] result = new byte[16];
            in.readFully(result);
            return InetAddress.getByAddress(result);
         default:

            throw new IOException("Unknown address format, length: " + addrLength);
      }
   }


   public static void writeInetAddress(final InetAddress inetAddress, final DataOutput out,
           final boolean fixedLength) throws IOException {

      if (inetAddress == null) {

         // Write null 16 byte address
         out.writeBoolean(true);

         if (fixedLength) {

            out.writeByte(0);
            out.writeLong(0);
            out.writeLong(0);
         }
      } else {

         // Write not null marker
         out.writeBoolean(false);

         final byte[] addr = inetAddress.getAddress();
         out.writeByte(addr.length);
         if (addr.length == 4) {

            int address = addr[3] & 0xFF;
            address |= addr[2] << 8 & 0xFF00;
            address |= addr[1] << 16 & 0xFF0000;
            address |= addr[0] << 24 & 0xFF000000;

            out.writeInt(address);

            if (fixedLength) {
               out.writeInt(0);
               out.writeLong(0);
            }
         } else if (addr.length == 16) {

            out.write(addr);
         } else {

            throw new IOException("Unknown address format: " + inetAddress);
         }
      }
   }


   public static void writeInteger(final DataOutputStream out, final Integer integer) throws IOException {

      if (integer == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         out.writeInt(integer.intValue());
      }
   }


   public static Integer readInteger(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }
      return in.readInt();
   }


   public static void writeIntArrayList(final DataOutputStream out, final IntArrayList list) throws IOException {

      if (list == null) {
         out.writeBoolean(true);
      } else {
         final int size = list.size();
         out.writeBoolean(false);
         out.writeInt(size);
         for (int i = 0; i < size; i++) {
            out.writeInt(list.get(i));
         }
      }
   }


   public static IntArrayList readIntArrayList(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }
      final int size = in.readInt();
      final IntArrayList result = new IntArrayList(size);
      for (int i = 0; i < size; i++) {
         result.add(in.readInt());
      }
      return result;
   }


   public static void writeIntHashSet(final DataOutputStream out, final IntHashSet set) throws IOException {

      if (set == null) {
         out.writeBoolean(true);
      } else {
         final int size = set.size();
         out.writeBoolean(false);
         out.writeInt(size);
         set.forEach(new IntProcedure() {

            public boolean execute(final int value) {

               try {
                  out.writeInt(value);
               } catch (final IOException e) {
                  throw new RuntimeIOException(e);
               }
               return true;
            }
         });
      }
   }


   public static IntHashSet readIntHashSet(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }
      final int size = in.readInt();
      final IntHashSet result = new IntHashSet(size);
      for (int i = 0; i < size; i++) {
         result.add(in.readInt());
      }
      return result;
   }


   public static HashSet<Binary> readBinaryHashSet(final DataInputStream in) throws IOException, // NOPMD
           ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      }
      final int size = in.readInt();
      final HashSet<Binary> result = new HashSet<Binary>(size);
      for (int i = 0; i < size; i++) {
         result.add(readBinary(in));
      }
      return result;
   }


   public static void writeBinaryHashSet(final DataOutputStream out, final HashSet<Binary> set) // NOPMD
           throws IOException {

      if (set == null) {
         out.writeBoolean(true);
      } else {
         final int size = set.size();
         out.writeBoolean(false);
         out.writeInt(size);
         set.forEach(new ObjectProcedure<Binary>() {

            public boolean execute(final Binary object) {

               try {
                  writeBinary(out, object);
               } catch (final IOException e) {
                  throw new RuntimeIOException(e);
               }
               return true;
            }
         });
      }
   }


   public static void writeLong(final DataOutputStream out, final Long aLong) throws IOException {

      if (aLong == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         out.writeLong(aLong.longValue());
      }
   }


   public static Long readLong(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }
      return in.readLong();
   }


   public static void writeString(final String string, final DataOutput out) throws IOException {

      if (string == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         out.writeUTF(string);
      }
   }


   public static String readString(final DataInput in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }

      return in.readUTF().intern();
   }


   public static void writeTime(final Time timestamp, final DataOutputStream out) throws IOException {

      if (timestamp == null) {
         out.writeBoolean(true);

      } else {

         out.writeBoolean(false);
         timestamp.writeWire(out);
      }
   }


   public static Time readTime(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {

         return null;
      } else {

         final TimeImpl time = new TimeImpl();
         time.readWire(in);
         return time;
      }
   }


   public static void writeCacheableValue(final DataOutputStream out, final CacheableValue value) throws IOException {

      if (value == null) {
         out.writeBoolean(true);
      } else {
         out.writeBoolean(false);
         value.writeWire(out);
      }
   }


   public static CacheableValue readCacheableValue(
           final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      } else {
         final CacheableValue cacheableValue = new CacheableValue();
         cacheableValue.readWire(in);
         return cacheableValue;
      }
   }


   public static List<Integer> readIntegerList(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {

         return null;
      } else {
         final int size = in.readInt();
         final List<Integer> result = new ArrayList<Integer>(size);
         for (int i = 0; i < size; i++) {

            result.add(IntegerUtils.valueOf(in.readInt()));
         }
         return result;
      }
   }


   @SuppressWarnings("ForLoopReplaceableByForEach")
   public static void writeIntegerList(final DataOutputStream out, final List<Integer> list) throws IOException {

      if (list == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         out.writeInt(list.size());
         for (int i = 0; i < list.size(); i++) {

            out.writeInt(list.get(i).intValue());
         }
      }
   }


   public static List<Integer> readShortList(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {

         return null;
      } else {
         final int size = in.readInt();
         final List<Integer> result = new ArrayList<Integer>(size);
         for (int i = 0; i < size; i++) {

            result.add(IntegerUtils.valueOf(in.readShort()));
         }
         return result;
      }
   }


   @SuppressWarnings("ForLoopReplaceableByForEach")
   public static void writeShortList(final DataOutputStream out, final List<Integer> list) throws IOException {

      if (list == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         out.writeInt(list.size());
         for (int i = 0; i < list.size(); i++) {

            out.writeShort(list.get(i).intValue());
         }
      }
   }


   public static void writeDate(final DataOutputStream out, final Date date) throws IOException {

      if (date == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         out.writeLong(date.getTime());
      }
   }


   public static Date readDate(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }

      return new Date(in.readLong());
   }


   public static void writeBoolean(final DataOutputStream out, final Boolean aBoolean) throws IOException {

      if (aBoolean == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         out.writeBoolean(aBoolean.booleanValue());
      }
   }


   public static JoiningNode readJoiningNode(final DataInputStream in) throws IOException {

      if (in.readBoolean()) {
         return null;
      }

      final JoiningNode joiningNode = new JoiningNode();
      joiningNode.readWire(in);

      return joiningNode;
   }


   public static void writeJoiningNode(final JoiningNode joiningNode, final DataOutputStream out) throws IOException {

      if (joiningNode == null) {

         out.writeBoolean(true);
      } else {

         out.writeBoolean(false);
         joiningNode.writeWire(out);
      }
   }


   public static void writeReceiverAddress(final ReceiverAddress address,
           final DataOutputStream out) throws IOException {

      if (address == null) {
         out.writeBoolean(true); // is null
      } else {
         out.writeBoolean(false);
         address.writeWire(out);
      }
   }


   public static ReceiverAddress readReceiverAddress(final DataInputStream in) throws IOException {


      if (in.readBoolean()) {
         return null;
      } else {
         final ReceiverAddress result = new ReceiverAddress();
         result.readWire(in);
         return result;
      }
   }


   public static void writeWireable(final DataOutputStream out, final Wireable wireable) throws IOException {

      if (wireable == null) {
         out.writeBoolean(true); // is null
      } else {
         out.writeBoolean(false);
         out.writeInt(wireable.getWireableType());
         wireable.writeWire(out);
      }
   }


   @SuppressWarnings("unchecked")
   public static <T extends Wireable> T readWireable(
           final DataInputStream in) throws IOException, ClassNotFoundException {

      if (in.readBoolean()) {
         return null;
      } else {

         final Wireable result = WIREABLE_FACTORY.createWireable(in.readInt());
         result.readWire(in);
         return (T) result;
      }

   }
}
