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

import java.util.Map;

import org.cacheonix.cache.annotations.CacheDataSource;
import junit.framework.TestCase;
import org.objectweb.asm.Type;

/**
 * @author calrxy
 */
public class CacheonixAnnotationCollectorTest extends TestCase {

   private CacheonixMethodVisitor methodVisitor;

   private static final Map<String, CacheonixAnnotation> cacheonixAnnotations = CacheonixAnnotation
           .annotationMapCreator();

   private static final int access = 1;

   private static final String name = "mName";

   private static final String desc = "desc";

   private static final String signature = "signature";

   private CacheonixAnnotationCollector collector = null;


   /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
   protected void setUp() throws Exception {

      super.setUp();

      final CacheonixClassReader classReader = new CacheonixClassReader();
      methodVisitor = new CacheonixMethodVisitor(access, name, desc,
              signature, classReader);
      final Type tp = Type.getType(CacheDataSource.class);
      final CacheonixAnnotation annotationMetaInfo = cacheonixAnnotations
              .get(tp.getDescriptor());

      collector = new CacheonixAnnotationCollector(annotationMetaInfo, tp
              .getDescriptor(), access, name, desc, signature, methodVisitor);
   }


   /**
    * Test method for {@link CacheonixAnnotationCollector#CacheonixAnnotationCollector(CacheonixAnnotation, String, int,
    * String, String, String, CacheonixMethodVisitor)} .
    */
   public void testCacheonixAnnotationCollector() {

      assertNotNull(collector);
   }


   /**
    * Test method for {@link CacheonixAnnotationCollector#visit(String, Object)} .
    */
   public void testVisit() {

      collector.visit(
              CacheonixAnnotation.CACHEDATASOURCE_EXPIRATION_TIME_MILLIS,
              new Integer(1000));
      collector.visitEnd();
      final Type tp = Type.getType(CacheDataSource.class);
      final String desc = tp.getDescriptor();

      assertTrue(methodVisitor.isAnnotationPresent(desc));
   }


   /**
    * Test method for {@link CacheonixAnnotationCollector#visitAnnotation(String, String)} .
    */
   public void testVisitAnnotation() {

      assertNull(collector.visitAnnotation("name", "desc"));
   }


   /**
    * Test method for {@link CacheonixAnnotationCollector#visitArray(String)} .
    */
   public void testVisitArray() {

      assertNull(collector.visitArray("name"));
   }


   /**
    * Test method for {@link CacheonixAnnotationCollector#visitEnd()} .
    */
   public void testVisitEnd() {

      collector.visitEnd();
      assertFalse(methodVisitor.isAnnotationPresent("bogusAnnotation"));
   }

}
