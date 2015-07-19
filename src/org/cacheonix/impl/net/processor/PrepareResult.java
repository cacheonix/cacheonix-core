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
package org.cacheonix.impl.net.processor;

/**
 * Enumeration for the result codes returned by the call that a request processor makes to <code>prepare()</code>.
 */
@SuppressWarnings("RedundantIfStatement")
public final class PrepareResult {

   /**
    * A processor must proceed to executing the message.
    */
   public static final PrepareResult EXECUTE = new PrepareResult((byte) 1, "Execute");

   /**
    * A processor must proceed to routing the message instead of executing.
    */
   public static final PrepareResult ROUTE = new PrepareResult((byte) 2, "Route");

   /**
    * A processor must terminate processing the message right after getting control from <code>prepare()</code>.
    */
   public static final PrepareResult BREAK = new PrepareResult((byte) 3, "Break");

   /**
    * The description of the result code.
    */
   private final String description;

   /**
    * The result code.
    */
   private final byte code;


   /**
    * Creates a new member of the enumeration.
    *
    * @param code        a unique code.
    * @param description a description of the code.
    */
   private PrepareResult(final byte code, final String description) {

      this.code = code;
      this.description = description;
   }


   public boolean equals(final Object o) {

      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final PrepareResult that = (PrepareResult) o;

      if (code != that.code) {
         return false;
      }

      return true;
   }


   public int hashCode() {

      return (int) code;
   }


   public String toString() {

      return description;
   }
}
