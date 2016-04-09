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

import org.cacheonix.Version;
import org.cacheonix.impl.util.logging.Logger;

/**
 * Stack trace at object costruction. This throwable is used to track impossible resource leaks.
 * <p/>
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 * @since Mar 20, 2008 9:06:53 PM
 */
public final class StackTraceAtCreate extends Throwable {

   /**
    * Logger.
    *
    * @noinspection UNUSED_SYMBOL, UnusedDeclaration
    */
   private static final Logger LOG = Logger.getLogger(StackTraceAtCreate.class); // NOPMD

   private static final long serialVersionUID = 0L;


   /**
    * Constructs a new throwable with <code>null</code> as its detail message. The cause is not initialized, and may
    * subsequently be initialized by a call to {@link #initCause}.
    * <p/>
    * <p>The {@link #fillInStackTrace()} method is called to initialize the stack trace data in the newly created
    * throwable.
    */
   public StackTraceAtCreate() {

      super(createMessagePrefix());
   }


   /**
    * Constructs a new throwable with the specified detail message and cause.  <p>Note that the detail message
    * associated with <code>cause</code> is <i>not</i> automatically incorporated in this throwable's detail message.
    *
    * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
    */
   public StackTraceAtCreate(final String message) {

      super(createMessagePrefix() + ": " + message);
   }


   private static String createMessagePrefix() {

      return Version.getVersion().fullProductVersion(true);
   }
}
