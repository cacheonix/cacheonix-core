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
package org.cacheonix.examples.cache.file.cache.data.grid;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.cacheonix.cache.datasource.DataSource;
import org.cacheonix.cache.datasource.DataSourceContext;
import org.cacheonix.cache.datasource.DataSourceException;
import org.cacheonix.cache.datasource.DataSourceObject;
import org.cacheonix.cache.datasource.SimpleDataSourceObject;

/**
 * A data source responsible for reading a text file.
 */
public final class FileDataSource implements DataSource {

   /**
    * Sets a cache datasource context.
    * <p/>
    * Cacheonix calls <code>setContext()</code> method once in DataSource lifetime, immediately after creating an
    * instance of <code>DataSource</code>.
    *
    * @param context an instance of {@link DataSourceContext}
    */
   public void setContext(final DataSourceContext context) {

      // Do nothing.
   }


   /**
    * Returns an <code>DataSourceObject</code> corresponding the given key. <tt>FileDataSource</tt> expects that the key
    * is a name of the file to read.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the key in the cache (cache miss) thus giving
    * the data source a chance to supply the missing key. This method returns <code>null</code> if the data source
    * cannot supply the key.
    *
    * @param key the key this data source should use to look up an object.
    * @return the object corresponding the given key or <code>null</code> if the data source cannot supply the key.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public DataSourceObject get(final Object key) throws DataSourceException {

      try {

         final String pathName = (String) key;
         final File file = new File(pathName);
         final TextFile textFile = readFile(file);
         return new SimpleDataSourceObject(textFile);

      } catch (final IOException e) {
         throw new DataSourceException(e);
      }
   }


   /**
    * Reads a file into a new TextFile object.
    *
    * @param file the file to read from.
    * @return a new TextFile object.
    * @throws IOException if an I/O error occurred.
    */
   private static TextFile readFile(final File file) throws IOException {

      // Read the file content into a StringBuilder
      final char[] buffer = new char[1000];
      final FileReader fileReader = new FileReader(file);
      final StringBuilder fileContent = new StringBuilder((int) file.length());

      for (final int bytesRead = fileReader.read(buffer); bytesRead != -1; ) {
         fileContent.append(buffer, 0, bytesRead);
      }

      // Close the reader
      fileReader.close();

      // Create CachedTextFile object
      final TextFile textFile = new TextFile();
      textFile.setContent(fileContent.toString());
      textFile.setLastModified(file.lastModified());

      // Return the result
      return textFile;
   }


   /**
    * Returns a collection of <code>DataSourceObject</code> corresponding the given collection of keys.
    * <p/>
    * Cacheonix calls <code>get()</code> every time Cacheonix cannot find the keys in the cache (cache miss) thus giving
    * the data source a chance to supply missing keys.  The returned collection should contain <code>null</code> at the
    * position of the key that the data source cannot supply.
    *
    * @param keys the collection of keys this data source should use to look up objects.
    * @return the collection of <code>DataSourceObject</code> corresponding the given collection of keys. The returned
    *         collection should contain <code>null</code> at the position of the key that the data source cannot
    *         supply.
    * @see DataSourceObject
    * @see SimpleDataSourceObject
    */
   public Collection<DataSourceObject> get(final Collection keys) {

      final ArrayList result = new ArrayList(keys.size());
      for (final Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
         final String fileName = (String) iterator.next();
         try {
            result.add(readFile(new File(fileName)));
         } catch (final IOException e) {
            throw new DataSourceException(e);
         }
      }

      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
}
