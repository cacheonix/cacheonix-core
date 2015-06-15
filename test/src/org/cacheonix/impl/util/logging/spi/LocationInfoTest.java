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
package org.cacheonix.impl.util.logging.spi;

import junit.framework.TestCase;

/**
 * Tester for LocationInfo
 */
public final class LocationInfoTest extends TestCase {


   private LocationInfo locationInfo;


   public void testGetClassName() throws Exception {

      assertEquals("junit.framework.TestCase", locationInfo.getClassName());
   }


   public void testGetFileName() throws Exception {

      assertEquals("TestCase.java", locationInfo.getFileName());
   }


   public void testGetLineNumber() throws Exception {

      assertEquals("128", locationInfo.getLineNumber());
   }


   public void testGetMethodName() throws Exception {

      assertEquals("runBare", locationInfo.getMethodName());
   }


   public void setUp() throws Exception {

      locationInfo = new LocationInfo(new Throwable(), this.getClass().getCanonicalName());

      super.setUp();
   }


   public void tearDown() throws Exception {

      super.tearDown();

      locationInfo = null;
   }
}
