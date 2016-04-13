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
package org.cacheonix.impl.cache.web;

import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;

/**
 * An output factory that is called on demand when either {@link CachingHttpServletResponseWrapper#getOutputStream()} or
 * {@link CachingHttpServletResponseWrapper#getWriter()} is called.
 */
final class ServletOutputWrapperFactoryImpl implements ServletOutputWrapperFactory {

   public ServletOutputStreamWrapper createServletOutputStream(final ServletOutputStream servletOutputStream,
           final int bufferSize) {

      return new ServletOutputStreamWrapper(servletOutputStream, bufferSize);
   }


   /**
    * Creates a caching wrapper around the PrintWriter created by HttpServletResponseWrapper#getWriter()}.
    *
    * @param printWriter the PrintWriter to wrap.
    * @return caching wrapper around the PrintWriter.
    */
   public ServletPrintWriterWrapper createServletPrintWriter(final PrintWriter printWriter) {

      return new ServletPrintWriterWrapper(printWriter);
   }
}
