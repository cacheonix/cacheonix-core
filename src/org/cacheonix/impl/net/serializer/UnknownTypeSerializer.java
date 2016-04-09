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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.util.IOUtils;
import org.cacheonix.impl.util.exception.StackTraceAtCreate;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Serilizer to handle unknown serilization types. This serilizer throws an expection with an expalation of the problem
 * and a stack trace showing when it was created.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 6:32:42 PM
 */
final class UnknownTypeSerializer implements Serializer {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(UnknownTypeSerializer.class); // NOPMD

   /**
    * Stack trace at create.
    */
   @SuppressWarnings("ThrowableInstanceNeverThrown")
   private final StackTraceAtCreate stackTraceAtCreate = new StackTraceAtCreate();

   /**
    * Type at create.
    */
   private final byte typeAtCreate;


   UnknownTypeSerializer(final byte type) {

      this.typeAtCreate = type;
   }


   public byte getType() {

      return TYPE_UNKNOWN;
   }


   public Object deserialize(final byte[] bytes) throws IOException {

      throw IOUtils.createIOException("Unknown serialization type: " + typeAtCreate, stackTraceAtCreate);
   }


   public byte[] serialize(final Object obj) throws IOException {

      throw IOUtils.createIOException("Unknown serialization type: " + typeAtCreate, stackTraceAtCreate);
   }


   public void serialize(final Object obj, final DataOutputStream dos) throws IOException {

      throw IOUtils.createIOException("Unknown serialization type: " + typeAtCreate, stackTraceAtCreate);
   }


   public Object deserialize(final DataInputStream in) throws IOException {

      throw IOUtils.createIOException("Unknown serialization type: " + typeAtCreate, stackTraceAtCreate);
   }


   public String toString() {

      return "UnknowTypeSerializer{" +
              "stackTraceAtCreate=" + stackTraceAtCreate +
              ", typeAtCreate=" + typeAtCreate +
              '}';
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

      final UnknownTypeSerializer that = (UnknownTypeSerializer) obj;

      if (typeAtCreate != that.typeAtCreate) {
         return false;
      }
      if (!stackTraceAtCreate.equals(that.stackTraceAtCreate)) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      int result = stackTraceAtCreate.hashCode();
      result = 29 * result + typeAtCreate;
      return result;
   }
}
