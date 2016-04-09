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
package org.cacheonix.cache.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * A data source representing a property file.
 */
public final class PropertyCacheDataSource implements DataSource {

   private static final String PROPERTY_FILE = "/CacheDataSourceTest.properties";


   /**
    * Sets cache datasource context. Cacheonix will call this method next after creating an instance of the class
    * implementing <code>DataSource</code>.
    *
    * @param context an instance of {@link DataSourceContext}
    */
   public void setContext(final DataSourceContext context) {

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

      // Read property file
      final Properties properties = readPropertyFile();

      // Get property
      final String property = properties.getProperty((String) key);

      // Return the result
      return new SimpleDataSourceObject(property);
   }


   /**
    * Returns a collection of <code>DataSourceObject</code> corresponding the given collection of keys. The returned
    * collection contains null if the corresponding key cannot be found. Cacheonix calls this method in case of a cache
    * miss.
    *
    * @param keys a collection of keys this data source should use to look up objects.
    * @return an collection of <code>DataSourceObject</code> corresponding the given collection of keys.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public Collection<DataSourceObject> get(final Collection keys) {

      final Collection<DataSourceObject> result = new LinkedList<DataSourceObject>();

      // Read property file
      final Properties properties = readPropertyFile();

      // Iterate keys and get property value for each key
      for (final Object key : properties.keySet()) {

         // Get property
         final String property = properties.getProperty((String) key);

         // Add to the result
         result.add(new SimpleDataSourceObject(property));
      }
      // Return the result
      return result;

   }


   /**
    * Reads the property file as a resource.
    *
    * @return the property file.
    * @throws IllegalStateException if there was an I/O error while reading reading the property file or the property
    *                               file doesn't exist.
    */
   private Properties readPropertyFile() throws IllegalStateException {

      InputStream resourceAsStream = null;
      try {

         // Read the properties
         final Properties properties = new Properties();
         resourceAsStream = getClass().getResourceAsStream(PROPERTY_FILE);
         properties.load(resourceAsStream);
         return properties;
      } catch (final IOException e) {

         throw new IllegalStateException(e);
      } finally {

         // Close input stream
         if (resourceAsStream != null) {

            try {

               resourceAsStream.close();
            } catch (final IOException ignored) { // NOPMD
               // Nothing we can do here - error at closing the resource
            }
         }
      }
   }
}
