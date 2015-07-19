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
package org.cacheonix;

/**
 * Holds Cacheonix version information. This includes canonical version, build number and time.
 *
 * @noinspection FieldNotUsedInToString, MethodMayBeStatic
 */
public final class Version {

   private static final String STR_COMPANY_NAME = "Cacheonix";

   private static final String STR_PRODUCT_NAME = "Cacheonix";

   private static final String STR_SPACE = " ";

   private static final int MAJOR_VERSION = 2;

   private static final int MINOR_VERSION = 0;

   private static final String RELEASE_DATE = "@release.date@";

   private static final String RELEASE_CHANGE = "@release.change@";

   private static final String RELEASE_BUILD = "@release.build@";

   private static final String RELEASE_PATCH_VERSION = "@release.patch.version@";


   /**
    * Singleton instance.
    */
   private static final Version INSTANCE = new Version();


   private final String shortFullVersion;

   private final String longFullVersion;


   /**
    * Singleton constructor.
    */
   private Version() {

      final StringBuilder result = new StringBuilder(100);

      // static part
      result.append(productName());
      result.append(STR_SPACE).append(productVersion());
      shortFullVersion = result.toString();

      // dynamic part
      if (!releaseDate().isEmpty()) {
         result.append(STR_SPACE).append(releaseDate());
      }
      if (!releaseChange().isEmpty()) {
         result.append(STR_SPACE).append(releaseChange());
      }
      if (!releaseBuild().isEmpty() && !"0".equals(releaseBuild())) {
         result.append(STR_SPACE).append("build ").append(releaseBuild());
      }
      longFullVersion = result.toString();
   }


   /**
    * Returns Version singleton.
    *
    * @return version information.
    * @noinspection MethodReturnOfConcreteClass
    */
   public static Version getVersion() {

      return INSTANCE;
   }


   /**
    * Returns the company name.
    *
    * @return company name
    */
   public String companyName() {

      return STR_COMPANY_NAME;
   }


   /**
    * Returns the product name.
    *
    * @return product name
    */
   public String productName() {

      return STR_PRODUCT_NAME;
   }


   /**
    * Returns the product version.
    *
    * @return product version
    */
   public String productVersion() {

      return Integer.toString(majorVersion()) + '.' + Integer.toString(minorVersion())
              + '.' + patchVersion();
   }


   /**
    * Returns the major version.
    *
    * @return major version.
    */
   public int majorVersion() {

      return MAJOR_VERSION;
   }


   /**
    * Returns the minor version.
    *
    * @return minor version.
    */
   public int minorVersion() {

      return MINOR_VERSION;
   }


   /**
    * Returns the patch version.
    *
    * @return patch version.
    */
   public String patchVersion() {

      String result = RELEASE_PATCH_VERSION;
      if (isBlank(result) || result.charAt(0) == '@') {
         result = "i";
      }
      return result;
   }


   /**
    * Returns the release date.
    *
    * @return release date
    */
   public String releaseDate() {

      String result = RELEASE_DATE;
      if (isBlank(result) || result.charAt(0) == '@') {
         result = "";
      }
      return result;
   }


   /**
    * Returns the release change number.
    *
    * @return release change
    */
   public String releaseChange() {

      String result = RELEASE_CHANGE;
      if (isBlank(result) || result.charAt(0) == '@') {
         result = "";
      }
      return result;
   }


   /**
    * Returns the release build number.
    *
    * @return release build number
    */
   public String releaseBuild() {

      String result = RELEASE_BUILD;
      if (isBlank(result) || result.charAt(0) == '@') {
         result = "";
      }
      return result;
   }


   /**
    * Returns the full product version.
    *
    * @param fullVersion if <code>true</code> returns a full version. If <code>false</code> returns a short version.
    * @return full product version.
    * @noinspection MagicNumber, LiteralAsArgToStringEquals
    */
   public String fullProductVersion(final boolean fullVersion) {

      if (fullVersion) {
         return longFullVersion;
      }
      return shortFullVersion;
   }


   public String toString() {

      return fullProductVersion(true);
   }


   /**
    * Returns the product name.
    *
    * @return product name.
    */
   public String getProductName() {

      return STR_PRODUCT_NAME;
   }


   /**
    * Returns the major and the minor version separated by a dot.
    *
    * @return the major and the minor version separated by a dot.
    */
   public String getMajorMinorVersion() {

      return Integer.toString(majorVersion()) + '.' + Integer.toString(minorVersion());
   }


   /**
    * Prints Cacheonix version to stdout.
    *
    * @param args arguments passed from the command line
    * @noinspection UseOfSystemOutOrSystemErr
    */
   public static void main(final String[] args) {

      System.out.println(new Version().fullProductVersion(true)); // NOPMD
   }


   private boolean isBlank(final String str) {

      if (str == null) {
         return true;
      }
      final int length = str.length();
      if (length == 0) {
         return true;
      }
      for (int i = 0; i < length; i++) {
         if (str.charAt(i) > ' ') {
            return false;
         }
      }
      return true;
   }
}
