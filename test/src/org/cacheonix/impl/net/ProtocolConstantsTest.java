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
package org.cacheonix.impl.net;

import org.cacheonix.impl.net.processor.Frame;
import junit.framework.TestCase;

/**
 * ProtocolConstants Tester.
 *
 * @author simeshev@cacheonix.org
 * @version 1.0
 * @since <pre>03/28/2008</pre>
 */
public final class ProtocolConstantsTest extends TestCase {

   public ProtocolConstantsTest(final String name) {

      super(name);
   }


   public void testMaximumMulticastPacketSize() {

      assertEquals("Protocol maximum multicast packet size is not allowed to be changed!", 1468, Frame.MAXIMUM_MULTICAST_PACKET_SIZE);
   }
}
