/**
 *
 */
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
package org.cacheonix.impl.transformer;

import java.io.IOException;
import java.math.BigDecimal;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.ConfigurationException;
import org.cacheonix.impl.annotations.tests.CacheAnnotatedTest;
import junit.framework.TestCase;

/**
 *
 */
public class CacheonixAnnotationsCombinedTest extends TestCase {

   private final static String cacheName = "org.cacheonix.impl.annotations.tests.CacheAnnotatedTest";


   public void testGetItem() throws ConfigurationException, IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final String key = "three";
      final String valOne = tst.getItem(key);

      // Cache cache =
      // Cacheonix.getInstance("test_cacheonix_annotations_config.xml").getCache("org.cacheonix.impl.annotations.tests.CacheAnnotatedTest");
      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);
      // org/cacheonix/impl/annotations/tests/CacheAnnotatedTestgetNothing()Ljava/lang/String;

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "getItem" + "(Ljava/lang/String;)Ljava/lang/String;";
      final String strSyntheticKey = cacheName;
      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(strSyntheticKey), GeneratedKey
              .addKeyElement(key));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck.toString());
   }


   public void testGet3Item() throws ConfigurationException, IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final String key1 = "three";
      final String key2 = "three";
      final String key3 = "three";
      final String valOne = tst.get3Items(key1, key2, key3);

      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "get3Items" +
      // "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
      final String strSyntheticKey = cacheName;

      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(strSyntheticKey), GeneratedKey
              .addKeyElement(key1), GeneratedKey.addKeyElement(key2),
              GeneratedKey.addKeyElement(key3));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck.toString());
   }


   public void testGetMoreThanFiveArgsTest() throws ConfigurationException,
           IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final int ikey1 = 252525;
      final double dkey2 = 345678.9876D;
      final String key3 = "teen";
      final BigDecimal ke4 = new BigDecimal(345);
      final double dke5 = 678.999999999D;
      final float ke6 = 2348.9089867F;
      final BigDecimal ke7 = new BigDecimal(9999999999999L);

      final String valOne = tst.getMoreThanFiveArgs(ikey1, dkey2, key3, ke4,
              dke5, ke6, ke7);

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "getMoreThanFiveArgs"
      // +
      // "(IDLjava/lang/String;Ljava/lang/Object;DFLjava/lang/Object;)Ljava/lang/String;";
      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey2),
              GeneratedKey.addKeyElement(key3), GeneratedKey
              .addKeyElement(ke4), GeneratedKey.addKeyElement(dke5),
              GeneratedKey.addKeyElement(ke6), GeneratedKey
              .addKeyElement(ke7));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck);

      final GeneratedKey keyF = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1 + 23), GeneratedKey.addKeyElement(dkey2),
              GeneratedKey.addKeyElement(key3), GeneratedKey
              .addKeyElement(ke4), GeneratedKey.addKeyElement(dke5),
              GeneratedKey.addKeyElement(ke6), GeneratedKey
              .addKeyElement(ke7));

      final Object valCheck2 = cache.get(keyX);

      final Object valCheckF = cache.get(keyF);

      assertFalse(valCheck2.equals(valCheckF));

   }


   public void testGetNothing() throws ConfigurationException, IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final String valOne = tst.getNothing();

      // Cache cache =
      // Cacheonix.getInstance("test_cacheonix_annotations_config.xml").getCache("org.cacheonix.impl.annotations.tests.CacheAnnotatedTest");
      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "getNothing" + "()Ljava/lang/String;";
      final String strSyntheticKey = cacheName;
      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(strSyntheticKey));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck.toString());
   }


   public void testGetMoreThanFiveArgsWithNullTest()
           throws ConfigurationException, IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final int ikey1 = 252525;
      final double dkey2 = 345678.9876D;
      final String key3 = "teen";
      final BigDecimal ke4 = null;
      final double dke5 = 678.999999999D;
      final float ke6 = 2348.9089867F;
      final BigDecimal ke7 = null;

      final String valOne = tst.getMoreThanFiveArgs(ikey1, dkey2, key3, ke4,
              dke5, ke6, ke7);

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "getMoreThanFiveArgs"
      // +
      // "(IDLjava/lang/String;Ljava/lang/Object;DFLjava/lang/Object;)Ljava/lang/String;";
      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey2),
              GeneratedKey.addKeyElement(key3), GeneratedKey
              .addKeyElement(ke4), GeneratedKey.addKeyElement(dke5),
              GeneratedKey.addKeyElement(ke6), GeneratedKey
              .addKeyElement(ke7));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck);

      final GeneratedKey keyF = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1 + 23), GeneratedKey.addKeyElement(dkey2),
              GeneratedKey.addKeyElement(key3), GeneratedKey
              .addKeyElement(ke4), GeneratedKey.addKeyElement(dke5),
              GeneratedKey.addKeyElement(ke6), GeneratedKey
              .addKeyElement(ke7));

      final Object valCheck2 = cache.get(keyX);

      final Object valCheckF = cache.get(keyF);

      assertFalse(valCheck2.equals(valCheckF));

   }


   public void testGetFuncWithArgsParamsTest() throws ConfigurationException,
           IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();

      final int ikey1 = 252525;
      final double dkey2 = 345678.9876D;
      final String key3 = "teen";
      final BigDecimal ke4 = new BigDecimal(345);
      final double dke5 = 678.999999999D;
      final float ke6 = 2348.9089867F;
      final BigDecimal ke7 = new BigDecimal(9999999999999L);

      final String valOne = tst.getFuncWithArgsParams(ikey1, dkey2, key3,
              ke4, dke5, ke6, ke7);

      // String strSyntheticKey = tst.getClass().getName().replace(".", "/") +
      // "getFuncWithArgsParams"
      // +
      // "(IDLjava/lang/String;Ljava/lang/Object;DFLjava/lang/Object;)Ljava/lang/String;";

      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1), GeneratedKey.addKeyElement(dkey2),
              // GeneratedKey.addKeyElement(key3),
              // GeneratedKey.addKeyElement(ke4),
              // GeneratedKey.addKeyElement(dke5),
              // GeneratedKey.addKeyElement(ke6),
              GeneratedKey.addKeyElement(ke7));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck);

      final GeneratedKey keyF = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(ikey1 + 23), GeneratedKey.addKeyElement(dkey2),
              // GeneratedKey.addKeyElement(key3),
              // GeneratedKey.addKeyElement(ke4),
              // GeneratedKey.addKeyElement(dke5),
              // GeneratedKey.addKeyElement(ke6),
              GeneratedKey.addKeyElement(ke7));

      final Object valCheck2 = cache.get(keyX);

      final Object valCheckF = cache.get(keyF);

      assertFalse(valCheck2.equals(valCheckF));

   }


   public void testCacheInvalidateFuncTest() throws ConfigurationException,
           IOException {

      final CacheAnnotatedTest tst = new CacheAnnotatedTest();
      final String key = "teen";

      final String valOne = tst.getItem(key);

      final Cache cache = Cacheonix.getInstance(
              "test_cacheonix_annotations_config.xml").getCache(cacheName);

      assertNotNull(cache);

      final GeneratedKey keyX = new GeneratedKey(GeneratedKey
              .addKeyElement(cacheName), GeneratedKey
              .addKeyElement(key));

      final Object valCheck = cache.get(keyX);

      assertEquals(valOne, valCheck);

      tst.putItem(key, "SomethingImportant");

      final Object valInvalidateCheck = cache.get(keyX);

      assertNull(valInvalidateCheck);

      assertNotSame(valCheck, valInvalidateCheck); // NOPMD

   }

}
