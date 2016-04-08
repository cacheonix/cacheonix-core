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
package org.cacheonix.impl.net.cluster;

import junit.framework.TestCase;
import org.cacheonix.impl.cache.distributed.partitioned.BucketTransferCompletedAnnouncement;

/**
 * A tester for {@link AssembledMessageImpl}.
 */
public final class AssembledMessageImplTest extends TestCase {


   private static final long START_FRAME = 7777L;

   private BucketTransferCompletedAnnouncement message;

   private AssembledMessageImpl assembledMessage;


   public void testGetMessage() throws Exception {

      assertEquals(message, assembledMessage.getMessage());
   }


   public void testGetStartFrameNumber() throws Exception {

      assertEquals(START_FRAME, assembledMessage.getStartFrameNumber());
   }


   public void testToString() throws Exception {

      assertNotNull(assembledMessage.toString());
   }


   protected void setUp() throws Exception {

      super.setUp();

      message = new BucketTransferCompletedAnnouncement("cache.name");
      assembledMessage = new AssembledMessageImpl(message, START_FRAME);
   }


   public void tearDown() throws Exception {

      assembledMessage = null;
      message = null;

      super.tearDown();
   }


   public String toString() {

      return "AssembledMessageImplTest{" +
              "message=" + message +
              ", assembledMessage=" + assembledMessage +
              '}';
   }
}
