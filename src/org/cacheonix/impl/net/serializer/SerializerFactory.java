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
package org.cacheonix.impl.net.serializer;

import org.cacheonix.impl.util.logging.Logger;

/**
 * SerializerFactory
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Mar 30, 2008 6:23:07 PM
 */
public final class SerializerFactory {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SerializerFactory.class); // NOPMD

   /**
    * Instance.
    */
   private static final SerializerFactory INSTANCE = new SerializerFactory();


   public Serializer getSerializer(final byte code) {

      if (code == Serializer.TYPE_JAVA) {
         return JavaSerializer.getInstance();
      } else {
         return new UnknownTypeSerializer(code);
      }
   }


   public static SerializerFactory getInstance() {

      return INSTANCE;
   }


   public String toString() {

      return "SerializerFactory{}";
   }
}
