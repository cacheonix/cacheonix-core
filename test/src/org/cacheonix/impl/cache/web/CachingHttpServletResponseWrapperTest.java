package org.cacheonix.impl.cache.web;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.apache.catalina.ssi.ByteArrayServletOutputStream;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A tester for {@link CachingHttpServletResponseWrapper}.
 */
public final class CachingHttpServletResponseWrapperTest extends TestCase {


   private static final String TEST_ERROR_MESSAGE = "Test error message";

   private static final String TEST_REDIRECT_URL = "test://redirect/url";

   private static final String DATE_HEADER = "Date header";

   private static final long DATE_HEADER_VALUE = 12345L;

   private static final String STRING_HEADER = "Test header";

   private static final String STRING_HEADER_VALUE = "Test header value";

   private static final String INT_HEADER = "Test int header";

   private static final int INT_HEADER_VALUE = 456;

   private static final String STATUS_MESSAGE = "Test status message";

   private static final byte[] TEST_OUTPUT_BYTES = "Test outpuit".getBytes();

   private static final int CONTENT_LENGTH = 789;

   private static final String CONTENT_TYPE = "test/content/type";

   private static final Locale LOCALE = Locale.US;

   private CachingHttpServletResponseWrapper cachingHttpServletResponseWrapper;

   private HttpServletResponse httpServletResponse;


   public void testAddCookie() {

      final String test_cookie_name = "test_cookie_name";
      final Cookie cookie = new Cookie(test_cookie_name, "test_cookie_value");
      cachingHttpServletResponseWrapper.addCookie(cookie);

      final Map<String, Cookie> cookies = cachingHttpServletResponseWrapper.getCookies();
      assertEquals(cookie, cookies.get(test_cookie_name));
   }


   public void testSendError() throws Exception {

      cachingHttpServletResponseWrapper.sendError(SC_INTERNAL_SERVER_ERROR);
      assertEquals(SC_INTERNAL_SERVER_ERROR, cachingHttpServletResponseWrapper.getError().intValue());
   }


   public void testSendErrorWithMessage() throws Exception {

      cachingHttpServletResponseWrapper.sendError(SC_INTERNAL_SERVER_ERROR, TEST_ERROR_MESSAGE);
      assertEquals(SC_INTERNAL_SERVER_ERROR, cachingHttpServletResponseWrapper.getError().intValue());
      assertEquals(TEST_ERROR_MESSAGE, cachingHttpServletResponseWrapper.getErrorMessage());
   }


   public void testSendRedirect() throws Exception {

      cachingHttpServletResponseWrapper.sendRedirect(TEST_REDIRECT_URL);
      assertEquals(TEST_REDIRECT_URL, cachingHttpServletResponseWrapper.getRedirectUrl());
   }


   public void testSetDateHeader() {

      cachingHttpServletResponseWrapper.setDateHeader(DATE_HEADER, DATE_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(DATE_HEADER);
      final DateHeader header = (DateHeader) headerValues.iterator().next();
      assertEquals(DATE_HEADER_VALUE, header.getValue());
      assertEquals(DATE_HEADER, header.getName());
   }


   public void testAddDateHeader() {

      cachingHttpServletResponseWrapper.addDateHeader(DATE_HEADER, DATE_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(DATE_HEADER);
      final DateHeader header = (DateHeader) headerValues.iterator().next();
      assertEquals(DATE_HEADER_VALUE, header.getValue());
      assertEquals(DATE_HEADER, header.getName());
   }


   public void testSetStringHeader() {

      cachingHttpServletResponseWrapper.setHeader(STRING_HEADER, STRING_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(STRING_HEADER);
      final StringHeader header = (StringHeader) headerValues.iterator().next();
      assertEquals(STRING_HEADER_VALUE, header.getValue());
      assertEquals(STRING_HEADER, header.getName());
   }


   public void testAddStringHeader() {

      cachingHttpServletResponseWrapper.addHeader(STRING_HEADER, STRING_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(STRING_HEADER);
      final StringHeader header = (StringHeader) headerValues.iterator().next();
      assertEquals(STRING_HEADER_VALUE, header.getValue());
      assertEquals(STRING_HEADER, header.getName());
   }


   public void testSetIntHeader() {

      cachingHttpServletResponseWrapper.setIntHeader(INT_HEADER, INT_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(INT_HEADER);
      final IntegerHeader header = (IntegerHeader) headerValues.iterator().next();
      assertEquals(INT_HEADER_VALUE, header.getValue());
      assertEquals(INT_HEADER, header.getName());
   }


   public void testAddIntHeader() {

      cachingHttpServletResponseWrapper.addIntHeader(INT_HEADER, INT_HEADER_VALUE);

      final Map<String, Collection<Header>> headers = cachingHttpServletResponseWrapper.getHeaders();
      final Collection<Header> headerValues = headers.get(INT_HEADER);
      final IntegerHeader header = (IntegerHeader) headerValues.iterator().next();
      assertEquals(INT_HEADER_VALUE, header.getValue());
      assertEquals(INT_HEADER, header.getName());
   }


   public void testSetStatus() {

      cachingHttpServletResponseWrapper.setStatus(SC_OK);
      assertEquals(SC_OK, cachingHttpServletResponseWrapper.getStatusCode());
   }


   public void testSetStatusAndMessage() {

      cachingHttpServletResponseWrapper.setStatus(SC_OK, STATUS_MESSAGE);
      assertEquals(SC_OK, cachingHttpServletResponseWrapper.getStatusCode());
      assertEquals(STATUS_MESSAGE, cachingHttpServletResponseWrapper.getStatusMessage());
   }


   public void testGetOutputStream() throws Exception {

      final ServletOutputStream outputStream = cachingHttpServletResponseWrapper.getOutputStream();
      outputStream.write(TEST_OUTPUT_BYTES);
      final byte[] byteOutput = cachingHttpServletResponseWrapper.getByteOutput();
      assertTrue(Arrays.equals(TEST_OUTPUT_BYTES, byteOutput));
   }


   public void testGetWriter() throws Exception {

      final PrintWriter writer = cachingHttpServletResponseWrapper.getWriter();
      writer.print(TEST_OUTPUT_BYTES);
   }


   public void testSetContentLength() {

      cachingHttpServletResponseWrapper.setContentLength(CONTENT_LENGTH);
      assertEquals(CONTENT_LENGTH, cachingHttpServletResponseWrapper.getContentLength());
   }


   public void testSetContentType() {

      cachingHttpServletResponseWrapper.setContentType(CONTENT_TYPE);
      assertEquals(CONTENT_TYPE, cachingHttpServletResponseWrapper.getContentType());
   }


   public void testFlushBuffer() throws Exception {

      cachingHttpServletResponseWrapper.flushBuffer();
      verify(httpServletResponse).flushBuffer();
   }


   public void testResetBuffer() {

      cachingHttpServletResponseWrapper.resetBuffer();
      verify(httpServletResponse).resetBuffer();
   }


   public void testReset() throws Exception {

      cachingHttpServletResponseWrapper.getOutputStream();
      cachingHttpServletResponseWrapper.reset();
      verify(httpServletResponse).reset();
   }


   public void testSetLocale() {

      cachingHttpServletResponseWrapper.setLocale(LOCALE);
      assertEquals(LOCALE, cachingHttpServletResponseWrapper.getLocale());
   }


   public void testGetByteOutput() {

   }


   public void testGetCharOutput() {

   }


   public void setUp() throws Exception {

      super.setUp();

      httpServletResponse = mock(HttpServletResponse.class);
      when(httpServletResponse.getOutputStream()).thenReturn(new ByteArrayServletOutputStream());
      when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(new ByteArrayServletOutputStream()));

      final ServletOutputWrapperFactory servletOutputWrapperFactory = new ServletOutputWrapperFactoryImpl();
      cachingHttpServletResponseWrapper = new CachingHttpServletResponseWrapper(httpServletResponse,
              servletOutputWrapperFactory);
   }


   public void tearDown() throws Exception {

      cachingHttpServletResponseWrapper = null;
      super.tearDown();
   }
}