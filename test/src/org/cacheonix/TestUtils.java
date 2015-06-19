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
package org.cacheonix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.BinaryFactory;
import org.cacheonix.impl.cache.item.BinaryFactoryBuilder;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.CollectionUtils;
import org.cacheonix.impl.util.exception.ExceptionUtils;

import static org.cacheonix.impl.cache.item.BinaryType.BY_REFERERENCE;

/**
 * Test helper.
 */
public final class TestUtils {


   private static final BinaryFactoryBuilder BINARY_FACTORY_BUILDER = new BinaryFactoryBuilder();

   private static final BinaryFactory BINARY_FACTORY = BINARY_FACTORY_BUILDER.createFactory(BY_REFERERENCE);


   /**
    * Utility class constructor.
    */
   private TestUtils() {

   }


   /**
    * Returns a test file. The test file is located under the test/data directory.
    *
    * @param file
    * @return test file.
    */
   public static File getTestFile(final String file) throws IOException {

      return new File("test" + File.separator + "data", file).getCanonicalFile();
   }


   /**
    * Returns an InputStream based on the test file. The test file is located under the test/data directory.
    *
    * @param file
    * @return test file.
    * @throws FileNotFoundException if the test file cannot be found.
    * @noinspection IOResourceOpenedButNotSafelyClosed
    */
   public static FileInputStream getTestFileInputStream(
           final String file) throws IOException {

      return new FileInputStream(getTestFile(file));
   }


   public static File getTempFile(final String name) throws IOException {

      return new File("temp" + File.separator + "test", name).getCanonicalFile();
   }


   public static InetAddress getInetAddress(final String host) {

      try {
         return InetAddress.getByName(host);
      } catch (final UnknownHostException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public static String makeTestObject(final int size) {

      final StringBuilder sb = new StringBuilder(size);
      int j = 0;
      for (int i = 0; i < size; i++) {
         sb.append(Integer.toString(j));
         j++;
         if (j > 9) {
            j = 0;
         }
      }
      return sb.toString();
   }


   public static ClusterNodeAddress createTestAddress(final int port) {

      try {
         return ClusterNodeAddress.createAddress(TestConstants.LOCALHOST, port);
      } catch (final IOException e) {
         throw ExceptionUtils.createIllegalStateException(e);
      }
   }


   public static ClusterNodeAddress createTestAddress() {

      return createTestAddress(TestConstants.PORT);
   }


   public static boolean equals(final ClusterNodeAddress[] listOne, final ClusterNodeAddress[] listTwo) {

      if (Arrays.equals(listOne, listTwo)) {
         return true;
      }

      if (listOne == null || listTwo == null) {
         return false;
      }

      if (listOne.length != listTwo.length) {
         return false;
      }

      for (int i = 0; i < listOne.length; i++) {
         if (!listOne[i].equals(listTwo[i])) {
            return false;
         }
      }

      return true;
   }


   public static boolean equals(final List listOne, final List listTwo) {

      return CollectionUtils.same(listOne, listTwo);
   }


   public static Binary toBinary(final Object obj) throws InvalidObjectException {

      return BINARY_FACTORY.createBinary(obj);
   }
}
