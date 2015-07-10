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
package org.cacheonix;

import org.cacheonix.impl.util.logging.Logger;

/**
 * An enumeration that lists shutdown modes. A Cacheonix instance can be shutdown either gracefully or forcibly.
 * <p/>
 * When Cacheonix instance is shutdown gracefully, Cacheonix will notify another nodes in the cluster about a node
 * leaving the cluster thus avoiding delays associated with failure detection. Graceful shutdown may take time.
 * <p/>
 * <p/>
 * When Cacheonix instance is shutdown forcibly, it will immediately stop responding to requests. The cluster detects
 * forced shutdown as a node failure.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @noinspection NumericCastThatLosesPrecision, QuestionableName
 * @see Cacheonix#shutdown()
 * @since Jun 9, 2008 11:09:01 PM
 */
public final class ShutdownMode {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(ShutdownMode.class); // NOPMD

   private static final String DESCRIPTION_GRACEFUL = "graceful";

   private static final String DESCRIPTION_FORCED = "forced";

   private static final byte CODE_GRACEFUL = (byte) 1;

   private static final byte CODE_FORCED = (byte) 2;

   private final byte code;

   private final String description;

   /**
    * When graceful shutdown mode is used, a Cacheonix node ceases to operate only after surrendering data belonging to
    * the cluster node. Graceful shutdown reduces data movement caused by the node leaving the cluster and preserves
    * data in non-replicated configurations. Graceful shutdown may take take time to complete.
    */
   public static final ShutdownMode GRACEFUL_SHUTDOWN = new ShutdownMode(CODE_GRACEFUL, DESCRIPTION_GRACEFUL);

   /**
    * Forced shutdown causes a Cacheonix node to terminate immediately. Forced shutdown leads to data loss in
    * non-replicated configurations. Forced shutdown may result in significant temporarily increase in data exchange in
    * the cluster.
    */
   public static final ShutdownMode FORCED_SHUTDOWN = new ShutdownMode(CODE_FORCED, DESCRIPTION_FORCED);


   /**
    * Enumeration constructor.
    *
    * @param code        numeric code for shutdown mode.
    * @param description shutdown mode description.
    */
   private ShutdownMode(final byte code, final String description) {

      this.code = code;
      this.description = description;
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final ShutdownMode that = (ShutdownMode) obj;

      return code == that.code;

   }


   public int hashCode() {

      return (int) code;
   }


   public String toString() {

      return "ShutdownMode{" +
              "code=" + code +
              ", description='" + description + '\'' +
              '}';
   }
}
