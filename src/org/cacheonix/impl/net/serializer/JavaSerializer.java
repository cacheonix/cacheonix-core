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
package org.cacheonix.impl.net.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.cacheonix.impl.cache.util.EntryImpl;
import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.array.HashSet;
import org.cacheonix.impl.util.exception.ExceptionUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Java serializer. Portable only for exchanging messages between Java VMs.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 6:28:47 PM
 */
final class JavaSerializer implements Serializer {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(JavaSerializer.class); // NOPMD

   private static final int INITIAL_BYTE_ARRAY_OUTPUT_STREAM_SIZE = 1000;

   private static final JavaSerializer INSTANCE = new JavaSerializer();

   private static final int OBJECT_TYPE_WIREABLE = 1;

   private static final int OBJECT_TYPE_STRING = 2;

   private static final int OBJECT_TYPE_INTEGER = 3;

   private static final int OBJECT_TYPE_OBJECT = 4;

   private static final int OBJECT_TYPE_LONG = 5;

   private static final int OBJECT_TYPE_BYTE = 6;

   private static final int OBJECT_TYPE_BOOLEAN = 7;

   private static final int OBJECT_TYPE_SHORT = 8;

   private static final int OBJECT_TYPE_FLOAT = 9;

   private static final int OBJECT_TYPE_DOUBLE = 10;

   private static final int OBJECT_TYPE_NULL = 11;

   private static final int OBJECT_TYPE_KNOWN_COLLECTION = 12;

   private static final int OBJECT_TYPE_MAP_ENTRY = 13;


   private JavaSerializer() {

   }


   public static JavaSerializer getInstance() {

      return INSTANCE;
   }


   /**
    * Returns {@link #TYPE_JAVA}.
    *
    * @return {@link #TYPE_JAVA}.
    */
   public byte getType() {

      return TYPE_JAVA;
   }


   /**
    * Deserializes the bytes into an object.
    *
    * @param bytes
    * @return object
    */
   public Object deserialize(final byte[] bytes) throws IOException {

      final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      final DataInputStream dis = new DataInputStream(bais);
      return deserialize(dis);
   }


   public Object deserialize(final DataInputStream dis) throws IOException {

      try {
         final int objectType = dis.readByte();
         switch (objectType) {
            case OBJECT_TYPE_BYTE:
               return Byte.valueOf(dis.readByte());
            case OBJECT_TYPE_INTEGER:
               return Integer.valueOf(dis.readInt());
            case OBJECT_TYPE_LONG:
               return Long.valueOf(dis.readLong());
            case OBJECT_TYPE_BOOLEAN:
               return Boolean.valueOf(dis.readBoolean());
            case OBJECT_TYPE_SHORT:
               return Short.valueOf(dis.readShort());
            case OBJECT_TYPE_FLOAT:
               return Float.valueOf(dis.readFloat());
            case OBJECT_TYPE_DOUBLE:
               return Double.valueOf(dis.readDouble());
            case OBJECT_TYPE_NULL:
               return null;
            case OBJECT_TYPE_OBJECT:
               return readObject(dis);
            case OBJECT_TYPE_STRING:
               return SerializerUtils.readString(dis);
            case OBJECT_TYPE_MAP_ENTRY:
               final Object key = deserialize(dis);
               final Object value = deserialize(dis);
               return new EntryImpl(key, value);
            case OBJECT_TYPE_KNOWN_COLLECTION:
               final String clazzName = SerializerUtils.readString(dis);
               final int size = dis.readInt();
               final Class aClass = Class.forName(clazzName);
               final Collection collection;
               if (aClass.equals(ArrayList.class) || aClass.equals(HashSet.class)) { // NOPMD
                  final Constructor constructor = aClass.getConstructor(Integer.TYPE);
                  collection = (Collection) constructor.newInstance(size);
               } else {
                  collection = (Collection) aClass.getConstructor().newInstance();
               }
               for (int i = 0; i < size; i++) {
                  collection.add(deserialize(dis));
               }
               return collection;
            case OBJECT_TYPE_WIREABLE:
               final int wireableType = dis.readInt();
               final WireableFactory factory = WireableFactory.getInstance();
               final Wireable wireable = factory.createWireable(wireableType);
               wireable.readWire(dis);
               return wireable;
            default:
               throw new IOException("Unknown object type: " + objectType);
         }
      } catch (final RuntimeException e) {
         throw e;
      } catch (final IOException e) {
         throw e;
      } catch (final Exception e) {
         throw ExceptionUtils.createIOException(e);
      }
   }


   /**
    * Reads an object from a <tt>DataInputStream</tt>
    *
    * @param dis the <tt>DataInputStream</tt> to read the object from.
    * @return the object that was read from the <tt>DataInputStream</tt>.
    * @throws IOException            if an error occurred while reading the object.
    * @throws ClassNotFoundException if one of the object classes was not present in the JVM.
    */
   private static Object readObject(final DataInputStream dis) throws IOException, ClassNotFoundException {

      final ObjectInputStream ois = new ObjectInputStream(dis);
      try {
         return ois.readObject();
      } finally {
         IOUtils.closeHard(ois);
      }
   }


   /**
    * Serializes an object to a byte array.
    *
    * @param obj the object to serialize
    * @return resulting object
    * @throws IOException
    */
   @SuppressWarnings("ChainOfInstanceofChecks")
   public byte[] serialize(final Object obj) throws IOException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(INITIAL_BYTE_ARRAY_OUTPUT_STREAM_SIZE);
      final DataOutputStream dos = new DataOutputStream(baos);
      try {
         serialize(obj, dos);
         return baos.toByteArray();
      } finally {
         IOUtils.closeHard(dos);
      }
   }


   /**
    * Serializes an object to a data output stream.
    *
    * @param obj the object to serialize.
    * @param dos the data output stream.
    * @throws IOException if an I/O error occurred.
    */
   public void serialize(final Object obj, final DataOutputStream dos) throws IOException {

      if (obj == null) {
         dos.writeByte(OBJECT_TYPE_NULL);
      } else if (obj instanceof Wireable) {
         final Wireable wireable = (Wireable) obj;
         dos.writeByte(OBJECT_TYPE_WIREABLE);
         dos.writeInt(wireable.getWireableType());
         wireable.writeWire(dos);
      } else if (obj instanceof String) {
         dos.writeByte(OBJECT_TYPE_STRING);
         SerializerUtils.writeString((String) obj, dos);
      } else if (obj instanceof Integer) {
         dos.writeByte(OBJECT_TYPE_INTEGER);
         dos.writeInt((Integer) obj);
      } else if (obj instanceof Long) {
         dos.writeByte(OBJECT_TYPE_LONG);
         dos.writeLong((Long) obj);
      } else if (obj instanceof Boolean) {
         dos.writeByte(OBJECT_TYPE_BOOLEAN);
         dos.writeBoolean((Boolean) obj);
      } else if (obj instanceof Short) {
         dos.writeByte(OBJECT_TYPE_SHORT);
         dos.writeShort((Short) obj);
      } else if (obj instanceof Float) {
         dos.writeByte(OBJECT_TYPE_FLOAT);
         dos.writeFloat((Float) obj);
      } else if (obj instanceof Double) {
         dos.writeByte(OBJECT_TYPE_DOUBLE);
         dos.writeDouble((Double) obj);
      } else if (obj instanceof Byte) {
         dos.writeByte(OBJECT_TYPE_BYTE);
         dos.writeByte((Byte) obj);
      } else if (obj instanceof Entry) {
         dos.writeByte(OBJECT_TYPE_MAP_ENTRY);
         final Entry entry = (Entry) obj;
         serialize(entry.getKey(), dos);
         serialize(entry.getValue(), dos);
      } else if (obj instanceof ArrayList || obj instanceof LinkedList || obj instanceof HashSet) {
         final Collection collection = (Collection) obj;
         final String name = collection.getClass().getName();
         dos.writeByte(OBJECT_TYPE_KNOWN_COLLECTION);
         SerializerUtils.writeString(name, dos);
         dos.writeInt(collection.size());
         for (final Object o : collection) {
            serialize(o, dos);
         }
      } else {
         dos.writeByte(OBJECT_TYPE_OBJECT);
         final ObjectOutputStream oos = new ObjectOutputStream(dos);
         oos.writeObject(obj);
         oos.flush();
      }
      dos.flush();
   }


   public String toString() {

      return "JavaSerializer{" +
              '}';
   }
}
