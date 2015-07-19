/**
 *
 */
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
package org.cacheonix.impl.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cacheonix.cache.annotations.CacheConfiguration;
import org.cacheonix.cache.annotations.CacheDataSource;
import junit.framework.TestCase;
import org.objectweb.asm.Type;

/**
 *
 */
public class CacheonixAnnotationTest extends TestCase {

   /**
    * Test method for {@link CacheonixAnnotation#CacheonixAnnotation(Type, ETransformationState, List)} .
    */
   public void testCacheonixAnnotation() {

      final List<AnnotationParameter> parameters = new ArrayList<AnnotationParameter>();
      final CacheonixAnnotation obj = new CacheonixAnnotation(Type
              .getType(CacheDataSource.class),
              ETransformationState.INITIAL_STATE, parameters);

      assertNotNull(obj);

   }


   /**
    * Test method for {@link CacheonixAnnotation#annotationMapCreator()} .
    */
   public void testAnnotationMapCreator() {

      final Map<String, CacheonixAnnotation> iniMap = CacheonixAnnotation
              .annotationMapCreator();

      final Type tp = Type.getType(CacheConfiguration.class);
      final CacheonixAnnotation an = iniMap.get(tp.getDescriptor());

      assertEquals(2, an.parameters.size());

      AnnotationParameter info = an.parameters.get(0);

      assertEquals(CacheonixAnnotation.CACHECONFIGURATION_CONFIGURATION_PATH,
              info.name);
      assertEquals("cacheonix-config.xml", info.oVal);

      info = an.parameters.get(1);

      assertEquals(CacheonixAnnotation.CACHECONFIGURATION_CACHE_NAME,
              info.name);
      assertEquals("CacheonixAnnotatedCache", info.oVal);

   }

}
