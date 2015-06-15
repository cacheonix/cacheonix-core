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

import org.cacheonix.impl.util.StringUtils;
import junit.framework.TestCase;

/**
 * Tests {@link Version}
 */
public final class VersionTest extends TestCase {

   private Version version;


   public void testCompanyName() {

      assertFalse(StringUtils.isBlank(version.companyName()));
   }


   public void testFullProductVersion() {

      assertFalse(StringUtils.isBlank(version.fullProductVersion(true)));
      assertFalse(StringUtils.isBlank(version.fullProductVersion(false)));
   }


   public void testMajorVersion() {

      assertTrue(version.majorVersion() > 0);
   }


   public void testMinorVersion() {

      assertTrue(version.minorVersion() >= 0);
   }


   public void testPatchVersion() {

      assertTrue(version.patchVersion().length() > 0);
   }


   public void testProductName() {

      assertFalse(StringUtils.isBlank(version.productName()));
   }


   public void testProductVersion() {

      assertFalse(StringUtils.isBlank(version.productVersion()));
   }


   public void testReleaseBuild() {

      assertNotNull(version.releaseBuild());
   }


   public void testReleaseChange() {

      assertNotNull(version.releaseChange());
   }


   public void testReleaseDate() {

      assertNotNull(version.releaseDate());
   }


   public void testToString() {

      assertFalse(StringUtils.isBlank(version.toString()));
   }


   /**
    * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
    */
   protected void setUp() throws Exception {

      super.setUp();
      version = Version.getVersion();
   }


   public String toString() {

      return "VersionTest{" +
              "version=" + version +
              '}';
   }
}
