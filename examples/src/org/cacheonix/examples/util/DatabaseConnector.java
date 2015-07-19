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
package org.cacheonix.examples.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connector.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class DatabaseConnector {

   /**
    * Database driver.
    */
   private final Driver driver;

   /**
    * Database connection properties.
    */
   private final Properties connectionProperties;

   /**
    * Database connection URL.
    */
   private final String connectionUrl;


   /**
    * Constructor.
    *
    * @param driverName JDBC driver name.
    * @param url        database URL
    * @param user       database user
    * @param password   database password
    * @throws IllegalArgumentException if the database driver cannot be loaded.
    */
   public DatabaseConnector(final String driverName, final String url, final String user,
                            final String password)
           throws IllegalArgumentException {

      // Load driver
      try {
         final Class driverClass = Class.forName(driverName);
         driver = (Driver) driverClass.newInstance();
      } catch (final Exception e) {
         throw new IllegalArgumentException("Cannot set up database driver: " + e.toString());
      }

      // Set connection properties
      connectionUrl = url;
      connectionProperties = new Properties();
      connectionProperties.setProperty("user", user);
      connectionProperties.setProperty("password", password);
   }


   /**
    * Connects to the database.
    *
    * @return new JDBC connection
    * @throws SQLException if connection cannot be established.
    */
   public Connection connect() throws SQLException {
      // Connect to the database
      return driver.connect(connectionUrl, connectionProperties);
   }


   public String toString() {

      return "DatabaseConnector{" +
              "driver=" + driver +
              ", connectionProperties=" + connectionProperties +
              ", connectionUrl='" + connectionUrl + '\'' +
              '}';
   }
}
