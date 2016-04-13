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
import javax.servlet.http.HttpServletResponse;

/**
 * A factory used to create wrappers for {@link ServletOutputStream} or {@link PrintWriter} used by {@link
 * CachingHttpServletResponseWrapper}.
 *
 * @see CachingHttpServletResponseWrapper#getOutputStream()
 * @see CachingHttpServletResponseWrapper#getWriter()
 */
interface ServletOutputWrapperFactory {

   /**
    * Creates a new wrapper for <code>ServletOutputStream</code> produced by {@link
    * HttpServletResponse#getOutputStream()}. The wrapper records writes made to {@link ServletOutputStream}.
    *
    * @param servletOutputStream the ServletOutputStream to wrap.
    * @param bufferSize          the buffer size used by the ServletOutputStream.
    * @return a new <code>ServletOutputStreamWrapper</code> wrapping the ServletOutputStream
    * @see HttpServletResponse#getOutputStream()
    */
   ServletOutputStreamWrapper createServletOutputStream(ServletOutputStream servletOutputStream, int bufferSize);

   ServletPrintWriterWrapper createServletPrintWriter(PrintWriter printWriter);
}
