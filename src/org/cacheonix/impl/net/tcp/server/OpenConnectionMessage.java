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
package org.cacheonix.impl.net.tcp.server;

import org.cacheonix.impl.net.processor.Message;
import org.cacheonix.impl.net.processor.ProcessorKey;
import org.cacheonix.impl.net.processor.SimpleProcessorKey;
import org.cacheonix.impl.net.serializer.Wireable;
import org.cacheonix.impl.net.serializer.WireableBuilder;

/**
 * Connection request is a wire-level request that is sent to the server imediately after the connection is established.
 * The connection request is used to establish the following facts:
 * <p/>
 * 1. The caller is Cacheonix
 * <p/>
 * 2. Both sides support the same version of the protocol.
 */
public final class OpenConnectionMessage extends Message {

   /**
    * Maker used by WireableFactory.
    */
   public static final WireableBuilder BUILDER = new Builder();


   public OpenConnectionMessage() {

      super(TYPE_OPEN_CONNECTION);
   }


   /**
    * {@inheritDoc}
    */
   protected final ProcessorKey getProcessorKey() {

      return new SimpleProcessorKey(DESTINATION_CONNECTION);
   }


   /**
    * {@inheritDoc}
    * <p/>
    * This implementation does nothing becuase this is a marker object.
    */
   public void execute() {

   }


   /**
    * A class factory.
    */
   final static class Builder implements WireableBuilder {

      public Wireable create() {

         return new OpenConnectionMessage();
      }
   }

}
