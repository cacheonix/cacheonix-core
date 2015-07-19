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

import org.cacheonix.impl.util.logging.Logger;

/**
 * SavedSystemProperty
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 6, 2009 1:07:58 PM
 */
public final class SavedSystemProperty {


   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(SavedSystemProperty.class); // NOPMD

   private final String propertyName;

   private String savedValue = null;

   private boolean saved = false;


   public SavedSystemProperty(final String propertyName) {

      this.propertyName = propertyName;
   }


   public void save() {

      savedValue = System.getProperty(propertyName);
      saved = true;
   }


   public void restore() {

      if (!saved) {
         throw new IllegalStateException("Property " + propertyName + " has not been saved yet");
      }
      if (savedValue != null) {
         System.setProperty(propertyName, savedValue);
      }
   }


   public String toString() {

      return "SavedSystemProperty{" +
              "propertyName='" + propertyName + '\'' +
              ", savedValue='" + savedValue + '\'' +
              ", saved=" + saved +
              '}';
   }
}
