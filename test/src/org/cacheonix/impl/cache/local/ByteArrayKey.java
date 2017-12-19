package org.cacheonix.impl.cache.local;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.util.ArrayUtils;

/**
 * A byte array key used in tests.
 */
@SuppressWarnings("RedundantIfStatement")
final class ByteArrayKey implements Externalizable {

   private static final long serialVersionUID = 3595907779192315311L;

   private byte[] content = null;


   ByteArrayKey(final byte[] content) {

      this.content = ArrayUtils.copy(content);
   }


   public ByteArrayKey() {

   }


   public void writeExternal(final ObjectOutput out) throws IOException {

      SerializerUtils.writeByteArray(out, content);
   }


   public void readExternal(final ObjectInput in) throws IOException {

      content = SerializerUtils.readByteArray(in);
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || !o.getClass().equals(getClass())) {
         return false;
      }

      final ByteArrayKey testKey = (ByteArrayKey) o;

      if (!Arrays.equals(content, testKey.content)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return Arrays.hashCode(content);
   }


   public String toString() {

      return "ByteArrayKey{" +
              "content=" + Arrays.toString(content) +
              '}';
   }
}
