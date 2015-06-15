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
package com.impl.examples.cache.file.cache.data.grid;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;

/**
 * An application demonstrating an application-level file cache.
 */
public class DataGridFileCacheExample {


   public static void main(final String[] args) {

      // Replace the file name with an actual file name
      final String pathName = "test.file.txt";

      final DataGridFileCacheExample fileCacheExample = new DataGridFileCacheExample();
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
    */
   public String getFileFromCache(final String pathName) {

      // Get cache
      final Cacheonix cacheonix = Cacheonix.getInstance();

      final Cache<String, TextFile> cache = cacheonix.getCache("data.grid.file.cache");

      // Get the file from the cache
      final TextFile textFile = cache.get(pathName);

      // Check if the cached file exists
      if (textFile == null) {

         return null;
      } else {

         return textFile.getContent();
      }
   }
}
