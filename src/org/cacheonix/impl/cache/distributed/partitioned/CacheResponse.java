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
package org.cacheonix.impl.cache.distributed.partitioned;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.Response;
import org.cacheonix.impl.net.serializer.SerializerUtils;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;
import org.cacheonix.impl.util.logging.Logger;

/**
 * CacheResponse
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @noinspection NonFinalFieldReferenceInEquals, NonFinalFieldReferencedInHashCode, RedundantIfStatement
 * @since Jan 17, 2009 9:48:37 PM
 */
public class CacheResponse extends Response {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(CacheResponse.class); // NOPMD


   private String cacheName = null;


   public CacheResponse() {

      super(TYPE_CACHE_RESPONSE);
   }


   public CacheResponse(final String cacheName) {

      this(TYPE_CACHE_RESPONSE, cacheName);
   }


   public CacheResponse(final int wireableType, final String cacheName) {

      super(wireableType);
      this.cacheName = cacheName;
   }


   public String getCacheName() {

      return cacheName;
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return new CacheProcessorKey(cacheName);
   }


   public void readWire(final DataInputStream in) throws IOException, ClassNotFoundException {

      super.readWire(in);
      cacheName = SerializerUtils.readString(in);
   }


   public void writeWire(final DataOutputStream out) throws IOException {

      super.writeWire(out);
      SerializerUtils.writeString(cacheName, out);
   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new CacheResponse();
      }
   }
}
