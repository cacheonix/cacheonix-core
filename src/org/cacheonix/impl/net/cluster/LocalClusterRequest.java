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
package org.cacheonix.impl.net.cluster;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;

/**
 * A cluster request that may be set only to the local cluster processor.
 */
public abstract class LocalClusterRequest extends ClusterRequest {

   public LocalClusterRequest(final int wireableType) {

      super(wireableType);
   }


   /**
    * Required by Wireable.
    */
   public LocalClusterRequest() {

   }


   @Override
   public final void readWire(final DataInputStream in) throws IOException {

      throw new NotSerializableException(this.getClass() + " can be sent only to a local processor");
   }


   @Override
   public final void writeWire(final DataOutputStream out) throws IOException {

      throw new NotSerializableException(this.getClass() + " can be sent only to a local processor");
   }
}
