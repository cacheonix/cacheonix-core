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
package org.cacheonix.impl.util.exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.cacheonix.Version;
import org.cacheonix.impl.config.SystemProperty;
import org.cacheonix.impl.util.StringUtils;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Utility methods for exception handling.
 */
public final class ExceptionUtils {

   private static final Logger LOG = Logger.getLogger(ExceptionUtils.class);

   /**
    * A prefix added to all messages that contains version of the system that have thrown the exception.
    */
   private static final String VERSION_PREFIX = Version.getVersion().fullProductVersion(true) + ": ";


   /**
    * Ignores exception with leaving a trace.
    *
    * @param throwable
    * @param explanation
    */
   public static void ignoreException(final Throwable throwable, final String explanation) {

      if (SystemProperty.CACHEONIX_PRINT_IGNORED_EXCEPTIONS) {
         LOG.warn(new StringBuffer(100).append("Ignored exception ").append(throwable)
                 .append(". Reason for ignoring: ").append(explanation.toLowerCase()), throwable);
      }
   }


   /**
    * Utility class constructor.
    */
   private ExceptionUtils() {

   }


   /**
    * Helper method.
    *
    * @param e
    * @return
    */
   public static IllegalStateException createIllegalStateException(final Throwable e) {

      final IllegalStateException ise = new IllegalStateException(StringUtils.toString(e));
      ise.initCause(e);
      return ise;
   }


   /**
    * Helper method.
    *
    * @param e
    * @return
    */
   public static IllegalStateException createIllegalStateException(final String message, final Throwable e) {

      final IllegalStateException ise = new IllegalStateException(message);
      ise.initCause(e);
      return ise;
   }


   /**
    * Creates IllegalArgumentException with initialized cause.
    *
    * @param e cause exception
    * @return created IllegalArgumentException
    */
   public static IllegalArgumentException createIllegalArgumentException(final Throwable e) {

      return createIllegalArgumentException(StringUtils.toString(e), e);
   }


   /**
    * Creates IllegalArgumentException with initialized cause.
    *
    * @param message message
    * @param e       cause exception
    * @return created IllegalArgumentException
    */
   public static IllegalArgumentException createIllegalArgumentException(final String message, final Throwable e) {

      final IllegalArgumentException iae = new IllegalArgumentException(message);
      // Init clause because constructor for IllegalArgumentException does not support cause
      // exception
      iae.initCause(e);
      return iae;
   }


   /**
    * Creates an IOException.
    *
    * @param e cause exception
    * @return new IOException
    */
   public static IOException createIOException(final Throwable e) {

      final IOException result = new IOException(StringUtils.toString(e));
      result.initCause(e);
      return result;
   }


   /**
    * Added a socket address to a message of an IOException.
    *
    * @param channel channel for that the error occurred.
    * @param e       the I/O exception to enhance
    * @return the enhanced IOException
    */
   public static IOException enhanceExceptionWithAddress(final SocketChannel channel, final IOException e) {

      try {

         if (channel == null) {
            return e;
         }

         final SocketAddress socketAddress = channel.socket().getRemoteSocketAddress();
         if (socketAddress == null) {

            return e;
         }
         @SuppressWarnings("ObjectToString") final String socketAddressAsString = socketAddress.toString();
         final IOException result = new IOException(e.getMessage() == null ? socketAddressAsString : socketAddressAsString + ':' + e.getMessage());
         result.setStackTrace(e.getStackTrace());
         return result;
      } catch (final Throwable ignored) {

         // If anything wrong happens, just return the original error
         return e;
      }
   }


   /**
    * Converts a throwable to a stack trace.
    *
    * @param throwable a throwable to get a String stack trace from.
    * @return a String stack trace.
    */
   public static String toStackTrace(final Throwable throwable) {


      final ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
      final PrintStream ps = new PrintStream(baos);
      throwable.printStackTrace(ps);
      ps.flush();
      ps.close();

      return new String(baos.toByteArray());
   }


   /**
    * Added version prefix to a message.
    *
    * @param message the message to add a prefix to.
    * @return the prefixed message.
    */
   public static String prefixWithVersion(final String message) {

      return VERSION_PREFIX + message;
   }
}
