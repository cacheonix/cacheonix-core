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
package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.net.processor.RetryException;
import org.cacheonix.impl.util.StringUtils;


@SuppressWarnings("PublicMethodNotExposedInInterface")
public abstract class Retryable {

   public abstract Object execute() throws RetryException;


   /**
    * Creates {@link Retryable}.
    *
    * @param description a description of the action to be re-tried.
    */
   protected Retryable(final String description) {

      this.description = description;
   }


   /**
    * A description of the action to be re-tried.
    */
   private final String description;


   /**
    * Returns a description of the action to be re-tried.
    *
    * @return description of the action to be re-tried.
    */
   public final String description() {

      return StringUtils.isBlank(description) ? "" : description;
   }


   public String toString() {

      return "Retryable{" +
              "description='" + description() + '\'' +
              '}';
   }
}
