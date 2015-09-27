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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Serializer
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 30, 2008 6:21:02 PM
 */
@SuppressWarnings("ConstantDeclaredInInterface")
public interface Serializer {

   /**
    *
    */
   byte TYPE_UNKNOWN = (byte) 0;
   /**
    *
    */
   byte TYPE_JAVA = (byte) 1;
   /**
    *
    */
   byte TYPE_PORTABLE = (byte) 2;


   /**
    * @return a numeric type.
    */
   byte getType();


   /**
    * Deserializes the bytes into an object.
    *
    * @param bytes
    * @return object
    */
   Object deserialize(byte[] bytes) throws IOException;


   byte[] serialize(Object obj) throws IOException;

   /**
    * Serializes an object to a data output stream.
    *
    * @param obj the object to serialize.
    * @param dos the data output stream.
    * @throws IOException if an I/O error occurred.
    */
   void serialize(Object obj, DataOutputStream dos) throws IOException;

   Object deserialize(DataInputStream in) throws IOException;
}
