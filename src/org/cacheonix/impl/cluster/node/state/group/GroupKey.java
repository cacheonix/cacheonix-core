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
package org.cacheonix.impl.cluster.node.state.group;

import org.cacheonix.impl.util.logging.Logger;

/**
 * GroupKey
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.com">Slava Imeshev</a>
 * @noinspection SimplifiableIfStatement
 * @since Jan 19, 2009 2:49:30 AM
 */
public final class GroupKey {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(GroupKey.class); // NOPMD

   private final int type;

   private final String name;


   public GroupKey(final int type, final String name) {

      this.type = type;
      this.name = name;
   }


   public boolean equals(final Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }

      final GroupKey groupKey = (GroupKey) obj;

      if (type != groupKey.type) {
         return false;
      }
      return name.equals(groupKey.name);

   }


   public String toString() {

      return "GroupKey{" +
              "type=" + type +
              ", name='" + name + '\'' +
              '}';
   }


   public int hashCode() {

      int result = type;
      result = 29 * result + name.hashCode();
      return result;
   }
}
