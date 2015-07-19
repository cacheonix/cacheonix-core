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
package org.cacheonix.impl.util;

import java.text.MessageFormat;

import org.cacheonix.impl.net.ClusterNodeAddress;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Assert
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Apr 4, 2008 2:15:27 AM
 */
public final class Assert {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(Assert.class); // NOPMD


   private Assert() {

   }


   public static void assertTrue(final boolean value, final String description) {

      if (!value) {
         throw new AssertionException(description);
      }
   }


   public static void assertFalse(final boolean value, final String description) {

      if (value) {
         throw new AssertionException(description);
      }
   }


   public static void assertFalse(final boolean value, final String description, final Object parameter) {

      if (value) {
         throw new AssertionException(MessageFormat.format(description, safeToString(parameter)));
      }
   }


   public static void assertTrue(final boolean value, final String description, final Object parameter) {

      if (!value) {
         throw new AssertionException(MessageFormat.format(description, safeToString(parameter)));
      }
   }


   public static void assertNull(final Object value, final String description, final Object parameter) {

      if (value != null) {
         throw new AssertionException(MessageFormat.format(description, safeToString(parameter)));
      }
   }


   public static void assertNull(final Object value, final String description, final Object[] parameters) {

      if (value != null) {

         final String[] stringParameters = new String[parameters.length];
         for (int i = 0; i < parameters.length; i++) {
            stringParameters[i] = safeToString(parameters[i]);

         }
         throw new AssertionException(MessageFormat.format(description, stringParameters));
      }
   }


   public static void assertNull(final Object value, final String description) {

      if (value != null) {
         throw new AssertionException(description);
      }
   }


   public static void assertNotNull(final Object value, final String description) {

      if (value == null) {
         throw new AssertionException(description);
      }
   }


   public static void assertNotNull(final Object value, final String description, final Object parameter) {

      if (value == null) {
         throw new AssertionException(MessageFormat.format(description, safeToString(parameter)));
      }
   }


   public static void assertNotNull(final Object value, final String description, final int parameter) {

      if (value == null) {
         throw new AssertionException(MessageFormat.format(description, parameter));
      }
   }


   public static void assertNotNull(final Object value, final String description, final Object param1,
           final Object param2, final int param3) {

      if (value == null) {
         throw new AssertionException(
                 MessageFormat.format(description, safeToString(param1), safeToString(param2), Integer.valueOf(
                         param3)));
      }
   }


   public static void assertNotNull(final Object value, final String description, final int param1, final int param2) {

      if (value == null) {
         throw new AssertionException(
                 MessageFormat.format(description, Integer.valueOf(param1), Integer.valueOf(param2)));
      }
   }


   public static void assertNotNull(final Object value, final String description, final int param1, final int param2,
           final Object param3) {

      if (value == null) {
         throw new AssertionException(MessageFormat.format(description, Integer.valueOf(param1),
                 Integer.valueOf(param2), safeToString(param3)));
      }
   }


   public static void assertNotNull(final Object value, final String description, final int param1,
           final Object param2) {

      if (value == null) {
         throw new AssertionException(MessageFormat.format(description, Integer.valueOf(param1), safeToString(param2)));
      }
   }


   public static void assertTrue(final boolean value, final String description, final int parameter) {

      if (!value) {
         throw new AssertionException(MessageFormat.format(description, Integer.valueOf(parameter)));
      }
   }


   public static void assertTrue(final boolean value, final String description, final Object param1,
           final Object param2) {

      if (!value) {
         throw new AssertionException(MessageFormat.format(description, safeToString(param1), safeToString(param2)));
      }
   }


   public static void assertNull(final Object object, final String description, final Object param1,
           final Object param2) {

      if (object != null) {
         throw new AssertionException(MessageFormat.format(description, safeToString(param1), safeToString(param2)));
      }
   }


   public static void assertEquals(final byte expected, final byte actual) {

      if (expected != actual) {
         throw new AssertionException(MessageFormat.format("Expected: {0}, actual {1}",
                 Byte.toString(expected), Byte.toString(actual)));
      }
   }


   public static void assertEquals(final ClusterNodeAddress a1, final ClusterNodeAddress a2,
           final String description) throws AssertionException {

      if (a1 == null) {
         throw new AssertionException("First address is null");
      }

      if (a2 == null) {
         throw new AssertionException("Second address is null");
      }

      if (!a1.equals(a2)) {
         throw new AssertionException(MessageFormat.format(description, a1, a2));
      }
   }


   public static void assertEquals(final Object o1, final Object o2, final String description, final Object param1,
           final Object param2) {

      if (o1 == o2) {
         return;
      }

      if (o1 != null && o2 != null) {

         if (o1.equals(o2)) {
            return;
         } else {

            throw new AssertionException(MessageFormat.format(description, safeToString(param1), safeToString(param2)));
         }
      }

      throw new AssertionException(MessageFormat.format(description, param1, param2));
   }


   public static void assertNull(final Object object, final String message, final Object m1, final Object m2,
           final Object m3) {

      if (object != null) {
         throw new AssertionException(MessageFormat.format(message, safeToString(m1), safeToString(m2), safeToString(m3)
         ));
      }
   }


   private static String safeToString(final Object parameter) {

      if (parameter == null) {
         return "null";
      }

      try {
         return parameter.toString();
      } catch (final Throwable e) {
         return "Error converting to String: " + e.toString();
      }
   }
}
