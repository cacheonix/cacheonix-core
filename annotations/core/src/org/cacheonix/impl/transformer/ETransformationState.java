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

/**
 * Enum used for maintaing the state of the reader while reading the bytes in the class file
 */
public enum ETransformationState {
   // States ENUMs
   INITIAL_STATE {
      /*
         * (non-Javadoc)
         *
         * @see java.lang.Objectr#toString()
         */
      public String toString() {

         return "INITIAL_STATE";
      }
   },

   READING_CONFIG_ANNOTATION // Found Configuration Annotation - Class level
           {
              /*
           * (non-Javadoc)
           *
           * @see java.lang.Objectr#toString()
           */
              public String toString() {

                 return "READING_CONFIG_ANNOTATION";
              }
           },

   READING_METHOD_ANNOTATION // Found Method Annotation
           {
              /*
           * (non-Javadoc)
           *
           * @see java.lang.Objectr#toString()
           */
              public String toString() {

                 return "READING_METHOD_ANNOTATION";
              }
           };


   /*
     * (non-Javadoc)
     *
     * @see java.lang.Objectr#toString()
     */
   public String toString() {

      return "UNKNOWN";
   }

}
