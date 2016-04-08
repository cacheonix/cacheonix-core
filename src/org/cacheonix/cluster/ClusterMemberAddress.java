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
package org.cacheonix.cluster;

import java.io.Externalizable;
import java.net.InetAddress;

/**
 * An address of a Cacheonix cluster member.
 */
public interface ClusterMemberAddress extends Externalizable {

   /**
    * Returns an address of a Cacheonix cluster member.
    *
    * @return the address of a Cacheonix cluster member.
    */
   InetAddress getInetAddress();
}
