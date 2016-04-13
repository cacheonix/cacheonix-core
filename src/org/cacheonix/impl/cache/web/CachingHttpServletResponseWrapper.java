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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.singletonList;

/**
 * This a wrapper for {@link HttpServletResponse} capable of recording actions performed by the web application while
 * producing a response.
 * <p/>
 * Under normal circumstances the pattern us using <code>HttpServletResponse</code> is simple:
 * <p/>
 * 1. Add Cookies 2. Set status codes 3. Write to <code>HttpServletResponse's</code> OutputStream or print to
 * PrintWriter. 4. Flush the buffer. 4. Close the output stream.
 * <p/>
 * What's special about <code>HttpServletResponse</code> that after a call to flushBuffer(), a call to sendError() or
 * closing or flushing the stream of the writer.
 */
@SuppressWarnings("ParameterHidesMemberVariable")
final class CachingHttpServletResponseWrapper extends HttpServletResponseWrapper {

   private final Map<String, Cookie> cookies = new TreeMap<String, Cookie>(CASE_INSENSITIVE_ORDER);

   private final Map<String, Collection<Header>> headers = new TreeMap<String, Collection<Header>>(
           CASE_INSENSITIVE_ORDER);

   private final ServletOutputWrapperFactory outputWrapperFactory;

   private ServletOutputStreamWrapper outputStreamWrapper;

   private ServletPrintWriterWrapper writerWrapper;

   /**
    * If true this means that the response has been committed. Changes to status codes, headers, redirects and cookies
    * must be ignored.
    */
   private String statusMessage;

   private String errorMessage;

   private String redirectUrl;

   private String contentType;

   private int contentLength;

   private int statusCode;

   private Locale locale;

   private Integer error;


   /**
    * Creates a new instance of caching HttpServletResponseWrapper.
    *
    * @param response      the <code>HttpServletResponse</code> to wrap for caching.
    * @param outputFactory the factory used to generate wrappers capable of caching servlet output.
    */
   CachingHttpServletResponseWrapper(final HttpServletResponse response,
           final ServletOutputWrapperFactory outputFactory) {

      super(response);
      this.outputWrapperFactory = outputFactory;
   }


   public void addCookie(final Cookie cookie) {

      // Call super
      super.addCookie(cookie);

      if (!isCommitted()) {

         // Register the cookie
         cookies.put(cookie.getName(), cookie);
      }
   }


   public void sendError(final int error, final String errorMessage) throws IOException {

      // Call super
      super.sendError(error, errorMessage);

      // Register the error
      this.errorMessage = errorMessage;
      this.error = error;
   }


   public void sendError(final int error) throws IOException {

      // Call super
      super.sendError(error);

      // Register the error
      this.error = error;
   }


   public void sendRedirect(final String url) throws IOException {

      super.sendRedirect(url);

      this.redirectUrl = url;
   }


   public void setDateHeader(final String s, final long l) {

      super.setDateHeader(s, l);

      if (!isCommitted()) {

         setHeader(s, new DateHeader(s, l));
      }
   }


   public void addDateHeader(final String name, final long time) {

      super.addDateHeader(name, time);

      if (!isCommitted()) {

         addHeader(name, new DateHeader(name, time));
      }
   }


   public void setHeader(final String s, final String s1) {

      super.setHeader(s, s1);

      if (!isCommitted()) {

         setHeader(s, new StringHeader(s, s1));
      }
   }


   public void addHeader(final String s, final String s1) {

      super.addHeader(s, s1);

      if (!isCommitted()) {

         addHeader(s, new StringHeader(s, s1));
      }
   }


   public void setIntHeader(final String s, final int i) {

      super.setIntHeader(s, i);

      if (!isCommitted()) {

         setHeader(s, new IntegerHeader(s, i));
      }
   }


   public void addIntHeader(final String s, final int i) {

      super.addIntHeader(s, i);

      if (!isCommitted()) {

         addHeader(s, new IntegerHeader(s, i));
      }
   }


   public void setStatus(final int sc) {

      super.setStatus(sc);

      if (!isCommitted()) {

         this.statusCode = sc;
      }
   }


   /**
    * {@inheritDoc}
    */
   public void setStatus(final int sc, final String sm) {

      super.setStatus(sc, sm);

      if (!isCommitted()) {

         this.statusMessage = sm;
         this.statusCode = sc;
      }
   }


   public ServletOutputStream getOutputStream() throws IOException {

      if (outputStreamWrapper == null) {

         final ServletOutputStream outputStream = super.getOutputStream();
         final int bufferSize = getBufferSize();
         outputStreamWrapper = outputWrapperFactory.createServletOutputStream(outputStream, bufferSize);
      }

      return outputStreamWrapper;
   }


   public PrintWriter getWriter() throws IOException {

      if (writerWrapper == null) {

         final PrintWriter writer = super.getWriter();
         writerWrapper = outputWrapperFactory.createServletPrintWriter(writer);
      }

      return writerWrapper;
   }


   public void setContentLength(final int len) {


      super.setContentLength(len);

      if (!isCommitted()) {

         this.contentLength = len;
      }
   }


   public void setContentType(final String type) {

      super.setContentType(type);

      if (!isCommitted()) {

         this.contentType = type;
      }
   }


   public void flushBuffer() throws IOException {

      super.flushBuffer();

      if (outputStreamWrapper != null) {
         outputStreamWrapper.flush();
      }

      if (writerWrapper != null) {
         writerWrapper.flush();
      }
   }


   public void resetBuffer() {

      super.resetBuffer();

      reset();
   }


   public void reset() {

      try {
         super.reset();

         // Clear status codes, headers and cookies
         statusMessage = null;
         errorMessage = null;
         redirectUrl = null;
         statusCode = SC_OK;
         contentLength = 0;
         headers.clear();
         cookies.clear();
         locale = null;
         error = null;

         if (outputStreamWrapper != null) {
            outputStreamWrapper = outputWrapperFactory.createServletOutputStream(getOutputStream(), getBufferSize());
         }

         if (writerWrapper != null) {
            writerWrapper = outputWrapperFactory.createServletPrintWriter(getWriter());
         }
      } catch (final IOException e) {

         throw new IllegalStateException(e);
      }
   }


   public void setLocale(final Locale locale) {

      super.setLocale(locale);

      if (!isCommitted()) {

         this.locale = locale;
      }
   }


   /**
    * Returns accumulated cookies.
    *
    * @return accumulated cookies.
    */
   public Map<String, Cookie> getCookies() {

      return cookies;
   }


   /**
    * Returns accumulated headers.
    *
    * @return accumulated headers.
    */
   public Map<String, Collection<Header>> getHeaders() {

      return headers;
   }


   /**
    * Returns accumulated status message.
    *
    * @return accumulated status message.
    */
   public String getStatusMessage() {

      return statusMessage;
   }


   /**
    * Returns accumulated error message.
    *
    * @return accumulated error message.
    */
   public String getErrorMessage() {

      return errorMessage;
   }


   /**
    * Returns accumulated redirect URL.
    *
    * @return accumulated redirect URL.
    */
   public String getRedirectUrl() {

      return redirectUrl;
   }


   /**
    * Returns accumulated content type.
    *
    * @return accumulated content type.
    */
   public String getContentType() {

      return contentType;
   }


   /**
    * Returns accumulated content length.
    *
    * @return accumulated content length.
    */
   public int getContentLength() {

      return contentLength;
   }


   /**
    * Returns accumulated status code.
    *
    * @return accumulated status code.
    */
   public int getStatusCode() {

      return statusCode;
   }


   /**
    * Returns accumulated locale.
    *
    * @return accumulated locale.
    */
   public Locale getLocale() {

      return locale;
   }


   /**
    * Returns accumulated error code or null if not set.
    *
    * @return accumulated error code or null if not set.
    */
   public Integer getError() {

      return error;
   }


   /**
    * Returns accumulated output to the OutputStream or null if output is not available.
    *
    * @return accumulated output to the OutputStream or null if output is not available.
    * @see HttpServletResponse#getOutputStream()
    */
   public byte[] getByteOutput() {

      if (outputStreamWrapper != null) {

         return outputStreamWrapper.getByteOutput();
      }


      if (writerWrapper != null) {

         writerWrapper.getByteOutput();
      }

      return null;
   }


   /**
    * A helper method used to add a header to the {@link #headers} map.
    *
    * @param name   the header name.
    * @param header the header.
    */
   private void addHeader(final String name, final Header header) {

      Collection<Header> headerCollection = headers.get(name);
      if (headerCollection == null) {

         headerCollection = new LinkedList<Header>();
         headers.put(name, headerCollection);
      }
      headerCollection.add(header);
   }


   /**
    * A helper method used to set a header.
    *
    * @param name   the header name.
    * @param header the header.
    */
   private void setHeader(final String name, final Header header) {

      headers.put(name, new LinkedList<Header>(singletonList(header)));
   }
}
