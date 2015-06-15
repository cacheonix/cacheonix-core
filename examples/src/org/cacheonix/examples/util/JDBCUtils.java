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
package org.cacheonix.examples.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * A set of utility methods to support JDBC operations.
 */
public final class JDBCUtils {

   /**
    * Utility class constructor.
    */
   private JDBCUtils() {

   }


   public static void close(final Connection conn) {

      if (conn == null) {
         return;
      }
      try {
         conn.close();
      } catch (final Exception e) {
         // Intentionally ignored
      }
   }


   public static void close(final ResultSet rs) {

      if (rs == null) {
         return;
      }
      try {
         rs.close();
      } catch (final Exception e) {
         // Intentionally ignored
      }
   }


   public static void close(final Statement stmt) {

      if (stmt == null) {
         return;
      }
      try {
         stmt.close();
      } catch (final Exception e) {
         // Intentionally ignored
      }
   }
}
