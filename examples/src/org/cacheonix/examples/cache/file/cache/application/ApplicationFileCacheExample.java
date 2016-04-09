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
package org.cacheonix.examples.cache.file.cache.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.examples.cache.file.cache.data.grid.TextFile;

/**
 * An application demonstrating an application-level file cache.
 */
public class ApplicationFileCacheExample {


   /**
    * Gets a file content from the cache ant prints it in the standard output.
    *
    * @param args arguments
    * @throws IOException if an I/O error occured.
    */
   public static void main(final String[] args) throws IOException {

      // Replace the file name with an actual file name
      final String pathName = "test.file.txt";

      final ApplicationFileCacheExample fileCacheExample = new ApplicationFileCacheExample();
      final String fileFromCache = fileCacheExample.getFileFromCache(pathName);
      System.out.print("File content from cache: " + fileFromCache);
   }


   /**
    * Retrieves a file from a cache. Puts it into the cache if it's not cached yet.
    * <p/>
    * This method demonstrates a typical flow an application must follow to cache a file and to get it from the cache.
    * As you can see, the application is pretty involved in maintaining the cache. It must read the file, check the the
    * timestamps and update the cache if its content is stale.
    * <p/>
    * A better approach that significantly simplifies application programming is to use Cacheonix as an in-memory data
    * grid. When used as an im-memory data grid Cacheonix hides from the application the details of reading and writing
    * the data. All the application needs to do is to get or put the data into Cacheonix.
    *
    * @param pathName a file path name.
    * @return a cached file content or null if file not found
    * @throws IOException if an I/O error occurred.
    */
   public String getFileFromCache(final String pathName) throws IOException {

      // Get cache
      final Cacheonix cacheonix = Cacheonix.getInstance();

      final Cache<String, TextFile> cache = cacheonix.getCache("application.level.file.cache");

      // Check if file exists
      final File file = new File(pathName);
      if (!file.exists()) {

         // Invalidate cache
         cache.remove(pathName);

         // Return null (not found)
         return null;
      }

      // Get the file from the cache
      TextFile textFile = cache.get(pathName);

      // Check if the cached file exists
      if (textFile == null) {

         // Not found in the cache, put in the cache

         textFile = readFile(file);

         cache.put(pathName, textFile);
      } else {

         // Found in cache, check the modification time stamp

         if (textFile.getLastModified() != file.lastModified()) {

            // Update cache

            textFile = readFile(file);

            cache.put(pathName, textFile);
         }
      }

      return textFile.getContent();
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
}
