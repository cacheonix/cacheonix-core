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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.invalidator.CacheInvalidatorContext;
import org.cacheonix.cache.invalidator.Invalidateable;
import org.cacheonix.examples.util.DatabaseConnector;
import org.cacheonix.examples.util.JDBCUtils;
import org.cacheonix.impl.storage.disk.StorageException;

/**
 * Invalidator of cache based on an incremental update counter (timestamp).
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public final class TimestampCacheInvalidator implements CacheInvalidator {

   private static final String INVALIDATABLE_CACHE_NAME = "my.cache";

   private CacheInvalidatorContext context = null;

   private DatabaseConnector databaseConnector = null;


   /**
    * Sets the cache invalidator context. Cacheonix will call this method immediately after creating an instance of this
    * class that was provided to the cache configuration.
    *
    * @param context an instance of {@link CacheInvalidatorContext}
    */
   public void setContext(final CacheInvalidatorContext context) {

      this.context = context;
      final Properties contextProperties = context.getProperties();
      databaseConnector = new DatabaseConnector(contextProperties.getProperty("driver"),
              contextProperties.getProperty("url"), contextProperties.getProperty("user"),
              contextProperties.getProperty("password"));
   }


   /**
    * May invalidate the cache element. Cacheonix calls this method after locating a valid element requested by any read
    * methods of the cache.
    * <p/>
    * A class implementing  <code>CacheInvalidator</code>  decide sif the element has to be invalidated and calls {@link
    * Invalidateable#invalidate()} to mark the element as invalid. If the element is marked as invalid, Cacheonix will
    * evict it.
    *
    * @param cacheElement a cache element to invalidate.
    * @see Invalidateable#invalidate()
    */
   public void process(final Invalidateable cacheElement) {

      if (context.getCacheName().equals(INVALIDATABLE_CACHE_NAME)) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
            // Connect to the database
            conn = databaseConnector.connect();

            // Execute statement
            ps = conn.prepareStatement("select TIME_STAMP from INVOICE where ID = ?");
            final Integer invoiceID = (Integer) cacheElement.getKey();
            ps.setInt(1, invoiceID);
            rs = ps.executeQuery();

            // Check if the timestamps match
            if (rs.next()) {
               final int databaseTimestamp = rs.getInt(1);
               // Get the time stamp of the cached Invoice
               final Object value = cacheElement.getValue();
               final Invoice cachedInvoice = (Invoice) value;
               final int cachedInvoiceTimestamp = cachedInvoice.getTimeStamp();
               if (databaseTimestamp != cachedInvoiceTimestamp) {
                  // Time stamps do not match, exit
                  cacheElement.invalidate();
               }
            } else {
               // The record is gone. Invalidate the cache.
               cacheElement.invalidate();
            }
         } catch (final StorageException e) {
            // REVIEWME: simeshev@cacheonix.org -> why getValue is throwing an exception?
            throw new IllegalArgumentException(e.toString());
         } catch (final SQLException ignored) {
            cacheElement.invalidate();
         } finally {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
            JDBCUtils.close(conn);
         }
      }
   }


   public String toString() {

      return "TimestampCacheInvalidator{" +
              "context=" + context +
              ", databaseConnector=" + databaseConnector +
              '}';
   }
}
