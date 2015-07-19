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
package org.cacheonix.examples.cache.invalidator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cacheonix.examples.util.DatabaseConnector;
import org.cacheonix.examples.util.JDBCUtils;

/**
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
class InvoiceStorageManager {

   private final DatabaseConnector databaseConnector;


   public InvoiceStorageManager(final String driverName, final String url, final String user,
                                final String password) {

      databaseConnector = new DatabaseConnector(driverName, url, user, password);
   }


   public void storeInvoiceInDB(final Invoice invoice) throws SQLException {
      // Store
      Connection conn = null;
      PreparedStatement stmt = null;
      try {
         // Connect to the database
         conn = databaseConnector.connect();

         // Execute update
         stmt = conn.prepareStatement("update INVOICE set INVOICE_DATE = ? , TIME_STAMP = ? where ID = ?");
         stmt.setDate(1, invoice.getDate());
         stmt.setInt(2, invoice.getTimeStamp());
         stmt.setInt(3, invoice.getId());
         stmt.executeUpdate();
      } finally {
         JDBCUtils.close(stmt);
         JDBCUtils.close(conn);
      }
   }


   /**
    * Retrieves an invoice from the database.
    *
    * @param invoiceID invoice ID
    * @return invoice
    * @throws SQLException if the invoice cannot be retrieved from the database.
    */
   public Invoice getInvoiceFromDB(final Integer invoiceID) throws SQLException {

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try {
         // Connect to the database
         conn = databaseConnector.connect();

         // Execute query
         stmt = conn.prepareStatement("select ID, INVOICE_DATE, TIME_STAMP from INVOICE where ID = ?");
         rs = stmt.executeQuery();

         // Create result
         if (rs.next()) {
            final int id = rs.getInt(1);
            final Date date = rs.getDate(2);
            final int timeStamp = rs.getInt(3);
            return new Invoice(id, date, timeStamp);
         } else {
            throw new SQLException("Invoice with ID " + invoiceID + " not found");
         }
      } finally {
         JDBCUtils.close(rs);
         JDBCUtils.close(stmt);
         JDBCUtils.close(conn);
      }
   }


   public String toString() {

      return "InvoiceStorageManager{" +
              "databaseConnector=" + databaseConnector +
              '}';
   }
}
