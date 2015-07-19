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

import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.thread.ActionableTimeout;
import org.cacheonix.impl.util.thread.TimeoutAction;

/**
 * ObtainMulticastMarkerTimeout
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 6, 2008 12:16:02 AM
 */
final class ObtainMulticastMarkerTimeout extends ActionableTimeout {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ObtainMulticastMarkerTimeout.class); // NOPMD

   /**
    * The cluster node this time out is set for.
    *
    * @see MarkerTimeoutAction
    */
   private final ClusterProcessor processor;


   /**
    * Constructor.
    *
    * @param processor the context.
    */
   ObtainMulticastMarkerTimeout(final ClusterProcessor processor) {

      super(processor.getTimer());

      this.processor = processor;
   }


   /**
    * {@inheritDoc}
    */
   protected TimeoutAction createTimeoutAction() {

      return new MarkerTimeoutAction(processor);
   }
}
