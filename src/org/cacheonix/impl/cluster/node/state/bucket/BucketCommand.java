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
package org.cacheonix.impl.cluster.node.state.bucket;

import org.cacheonix.impl.util.logging.Logger;

/**
 * BucketCommand
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @since Feb 5, 2010 3:50:41 PM
 */
class BucketCommand {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(BucketCommand.class); // NOPMD

   private String cacheName = null;


   protected BucketCommand(final String cacheName) {

      this.cacheName = cacheName;
   }


   public String getCacheName() {

      return cacheName;
   }


   @Override
   public String toString() {

      return "BucketCommand{" +
              "cacheName='" + cacheName + '\'' +
              '}';
   }
}
