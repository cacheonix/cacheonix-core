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
package org.cacheonix.impl.annotations.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cacheonix.cache.annotations.CacheConfiguration;
import org.cacheonix.cache.annotations.CacheDataSource;
import org.cacheonix.cache.annotations.CacheInvalidate;
import org.cacheonix.cache.annotations.CacheKey;

/**
 *
 */

@CacheConfiguration(configurationPath = "test_cacheonix_annotations_config.xml")
public class CacheAnnotatedTest {

   private static final String fileName = "DBSimValues.txt";

   private static final String EOL = System.getProperty("line.separator");

   private final List<String> sudoDB = new ArrayList<String>();


   public CacheAnnotatedTest() {

      final URL url = ClassLoader.getSystemResource(fileName);

      final String fullFileName = url.getFile();

      final File inp = new File(fullFileName);

      // Check current directory

      if (inp.exists() && inp.canRead()) {
         try {
            final BufferedReader reader = new BufferedReader(
                    new FileReader(inp));

            String txt;

            while ((txt = reader.readLine()) != null) {
               sudoDB.add(txt);
            }

            reader.close();

         } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }
   }


   private String find(final String... keys) {

      final StringBuilder buf = new StringBuilder();

      try {
         for (int i = 0; i < keys.length; ++i) {
            final Pattern pt = Pattern.compile(keys[i]);

            final Iterator<String> it = sudoDB.iterator();

            while (it.hasNext()) {
               final String val = it.next();
               final Matcher m = pt.matcher(val);
               if (m.find()) {
                  buf.append(val);
                  buf.append('/');
               }
            }
         }
      } catch (final Exception e) {
         throw new RuntimeException(e);
      }

      String strRes = null;

      if (!buf.toString().isEmpty()) {
         strRes = buf.toString();
      }

      return strRes;
   }

   // ////////////////////////////////////////////////


   @CacheDataSource
   public String getNothing() {

      return "Nothing";
   }


   @CacheDataSource
   public String getItem(final String searchKey) {
      // System.out.println("@@@@@@@@@@  Original 'getItem' is Called");
      return find(searchKey);
   }


   @CacheDataSource(expirationTimeMillis = 12345)
   public String get3Items(final String key1, final String key2,
                           final String key3) {
      // System.out.println("@@@@@@@@@@  Original 'get3Items' is Called");
      return find(key1, key2, key3);
   }


   @CacheDataSource
   public String getItems(final String... keys) {
      // System.out.println("@@@@@@@@@@  Original 'getItems' is Called");
      return find(keys);
   }


   public String toString() {

      final StringBuilder buff = new StringBuilder(64);

      final Field[] fields = this.getClass().getDeclaredFields();

      buff.append("Class '").append(this.getClass().getSimpleName()).append("' has following declared fields: ").append(EOL);

      for (int i = 0; i < fields.length; ++i) {
         Object val = null;

         try {
            val = fields[i].get(this);
         } catch (final Exception e) // NOPMD
         {
            // Do nothing
         }

         buff.append(" --- ").append(Modifier.toString(fields[i].getModifiers())).append(' ').append(fields[i].getGenericType().toString()).append(' ').append(fields[i].getName()).append(' ').append((val != null) ? " = " + val.toString() : "").append(EOL);
      }

      final Method[] methods = this.getClass().getMethods();

      for (int i = 0; i < methods.length; ++i) {

         buff.append(" +++ ").append(Modifier.toString(methods[i].getModifiers())).append(' ').append(methods[i].getGenericReturnType().toString()).append(' ').append(methods[i].getName()).append("( ").append(processParams(methods[i].getParameterTypes())).append(") ").append(EOL);
      }
      return buff.toString();
   }


   private String processParams(final Class<?>[] parameterTypes) {

      final StringBuilder buff = new StringBuilder();

      for (int i = 0; i < parameterTypes.length; ++i) {
         if (i > 0) {
            buff.append(", ");
         }
         buff.append(parameterTypes[i].getSimpleName());
      }
      return buff.toString();
   }


   @CacheDataSource
   public String getMixedItems(final int key1, final double key2,
                               final String key3, final Object key4) {
      // System.out.println("@@@@@@@@@@  Original 'getMixedItems' is Called");
      return find(String.valueOf(key1), String.valueOf(key2), key3, key4
              .toString());
   }


   @CacheDataSource
   public String getItem(final int searchKey) {
      // System.out.println("@@@@@@@@@@  Original 'getItem' is Called");
      return find(String.valueOf(searchKey));
   }


   @CacheDataSource
   public String getItem(final double searchKey) {
      // System.out.println("@@@@@@@@@@  Original 'getItem' is Called");
      return find(String.valueOf(searchKey));
   }


   @CacheDataSource
   public String getMoreThanFiveArgs(final int key1, final double key2,
                                     final String key3, final Object key4, final double ke5,
                                     final float ke6, final Object key7) {
      // System.out.println("@@@@@@@@@@  Original 'getMoreThanFiveArgs' is Called");
      return find(String.valueOf(key1), String.valueOf(key2), key3, String
              .valueOf(key4), String.valueOf(ke5), String.valueOf(ke6),
              String.valueOf(key7));
   }


   @CacheDataSource(expirationTimeMillis = 12345)
   public String getFuncWithArgsParams(@CacheKey final int key1,
                                       @CacheKey final double key2, final String key3, final Object key4,
                                       final double ke5, final float ke6, final @CacheKey Object key7) {

      return find(String.valueOf(key1), String.valueOf(key2), key3, String
              .valueOf(key4), String.valueOf(ke5), String.valueOf(ke6),
              String.valueOf(key7));
   }


   @CacheInvalidate
   public Object putItem(@CacheKey final String key, final String Item) {
      // System.out.println("@@@@@@@@@@  Original 'putItem' is Called");
      return new String("Object"); // NOPMD
   }

}
