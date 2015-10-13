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
package org.cacheonix.impl.util.thread;

import org.cacheonix.impl.config.SystemProperty;

/**
 * @noinspection ClassWithoutToString
 */
public final class DaemonThreadFactory extends AbstractThreadFactory {


   public DaemonThreadFactory(final String name) {

      super(name);
   }


   public Thread newThread(final Runnable r) {

      final Thread th = new Thread(r, (SystemProperty.isShowThreadType() ? "[D]" : "") + super.createNextName());
      th.setDaemon(true);
      return th;
   }


   public String toString() {

      return "DaemonThreadFactory{" +
              "name='" + getName() + '\'' +
              '}';
   }
}
