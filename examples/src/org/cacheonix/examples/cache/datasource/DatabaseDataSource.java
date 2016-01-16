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
package org.cacheonix.examples.cache.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.cacheonix.CacheonixException;
import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.cache.datasource.DataSourceContext;
import org.cacheonix.cache.datasource.DataSourceObject;
import org.cacheonix.cache.datasource.SimpleDataSourceObject;
import org.cacheonix.examples.util.JDBCUtils;

/**
 * An object that implements <code>DataSource</code> by executing queries to the database on the backend.
 *
 * @see DataSource
 */
final class DatabaseDataSource implements DataSource {

   private static final String DRIVER_NAME_PROPERTY = "driver.name";

   private Driver driver = null;

   private String connectionURL = null;


   /**
    * Sets cache datasource context. Cacheonix will call this method immediately after creating an instance of the class
    * implementing <code>DataSource</code>.
    *
    * @param context an instance of {@link DataSourceContext}
    */
   public void setContext(final DataSourceContext context) {

      try {
         final Properties properties = context.getProperties();

         // Set up database driver
         final String driverName = properties.getProperty(DRIVER_NAME_PROPERTY);
         final Class driverClass = Class.forName(driverName);
         driver = (Driver) driverClass.newInstance();

         // Set up connection URL
         connectionURL = properties.getProperty("connection.url");
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      }
   }


   /**
    * Returns an <code>DataSourceObject</code> corresponding the given key. Returns null if the key cannot be found.
    * Cacheonix calls this method in case of a cache miss.
    *
    * @param key a key this data source should use to look up an object.
    * @return an object corresponding the given key or null if the key cannot be found.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public DataSourceObject get(final Object key) {

      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
         final QueryKey queryKey = (QueryKey) key;
         final String queryText = queryKey.getQueryText();
         final List queryParameters = queryKey.getQueryParameters();

         // Connect
         conn = driver.connect(connectionURL, new Properties());
         ps = conn.prepareStatement(queryText);

         // Set parameters
         for (int i = 1; i <= queryParameters.size(); i++) {
            final Object parameter = queryParameters.get(i - 1);
            ps.setObject(i, parameter);
         }

         // Execute the statement and retrieve the result
         final List<Object[]> rows = new ArrayList<Object[]>(3);
         rs = ps.executeQuery();
         final int columnCount = rs.getMetaData().getColumnCount();
         while (rs.next()) {
            final Object[] row = new Object[columnCount];
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
               row[columnIndex - 1] = rs.getObject(columnIndex);
            }
            rows.add(row);
         }

         // Create query result
         final QueryResult queryResult = new QueryResult(columnCount, rows);

         // Return result
         return new SimpleDataSourceObject(queryResult);
      } catch (final RuntimeException e) {
         throw e;
      } catch (final Exception e) {
         throw new CacheonixException(e);
      } finally {
         JDBCUtils.close(rs);
         JDBCUtils.close(ps);
         JDBCUtils.close(conn);
      }
   }


   /**
    * Returns a collection of <code>DataSourceObject</code> corresponding the given collection of keys. The returned
    * collection contains null if the corresponding key cannot be found. Cacheonix calls this method in case of a cache
    * miss.
    * <p/>
    * This default implementation simply calls <code>get()</code> of this class for each key.
    *
    * @param keys a collection of keys this data source should use to look up objects.
    * @return an collection of <code>DataSourceObject</code> corresponding the given collection of keys.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public Collection<DataSourceObject> get(final Collection keys) {

      final List<DataSourceObject> result = new ArrayList<DataSourceObject>(keys.size());
      for (final Object key : keys) {
         result.add(get(key));
      }
      return result;
   }


   public String toString() {

      return "DatabaseDataSource{" +
              "driver=" + driver +
              ", connectionURL='" + connectionURL + '\'' +
              '}';
   }
}
