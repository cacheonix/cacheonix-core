package org.cacheonix.impl.cache.web;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.cacheonix.impl.util.array.HashMap;

import static org.cacheonix.impl.cache.web.RequestCacheFilterImpl.CONFIGURATION_PATH;
import static org.cacheonix.impl.cache.web.RequestCacheFilterImpl.GZIP;
import static org.cacheonix.impl.cache.web.RequestCacheFilterImpl.HEADER_ACCEPT_ENCODING;
import static org.cacheonix.impl.cache.web.RequestCacheFilterImpl.HEADER_ENCODING;
import static org.cacheonix.impl.cache.web.RequestCacheFilterImpl.HEADER_IF_MODIFIED_SINCE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A tester for {@link RequestCacheFilterImpl}.
 */
public final class RequestCacheFilterImplTest extends TestCase {


   private static final String REQUEST_CACHE_FILTER_IMPL_XML = "cacheonix-config-RequestCacheFilterImpl.xml";

   private static final String TEST_REQUEST_CACHE = "TestRequestCache";

   private static final String TEST_REQUEST_URI = "test/request/uri";

   private RequestCacheFilterImpl requestCacheFilter;


   public void testInit() {

      // Prepare FilterConfig
      final FilterConfig filterConfig = mockFilterConfig();

      // Call method under test
      requestCacheFilter.init(filterConfig);

      verify(filterConfig, times(1)).getInitParameter(CONFIGURATION_PATH);
      verify(filterConfig, times(1)).getFilterName();
   }


   public void testDoFilterCachesRequest() throws Exception {

      //
      // Init
      //
      final FilterConfig filterConfig = mockFilterConfig();
      requestCacheFilter.init(filterConfig);


      //
      // Prepare
      //
      final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
      final HttpServletRequest httpServletRequest = mockRequest();
      final FilterChain filterChain = mock(FilterChain.class);

      //
      // Do filter pass
      //
      requestCacheFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

      // Verify that chain was called.
      verify(filterChain).doFilter(eq(httpServletRequest), any(CachingHttpServletResponseWrapper.class));
      assertEquals("Cache must contain a cached response", 1, requestCacheFilter.getCache().size());
   }


   public void testDoFilterReturnsDataFromCache() throws Exception {

      //
      // Init
      //
      final FilterConfig filterConfig = mockFilterConfig();
      requestCacheFilter.init(filterConfig);


      //
      // Prepare
      //
      final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
      final HttpServletRequest httpServletRequest = mockRequest();
      final FilterChain filterChain = mock(FilterChain.class);

      //
      // Do filter pass # 1
      //
      requestCacheFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

      //
      // Do filter pass # 2
      //
      requestCacheFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
   }


   public void testDoFilterCompressesOutput() throws Exception {

      // Init
      final FilterConfig filterConfig = mockFilterConfig();
      requestCacheFilter.init(filterConfig);


      // Prepare
      final ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
      final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
      when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

      //
      final HttpServletRequest httpServletRequest = mockRequest();
      when(httpServletRequest.getHeader(HEADER_ACCEPT_ENCODING)).thenReturn("gzip");

      final FilterChain filterChain = new FilterChain() {

         public void doFilter(final ServletRequest servletRequest,
                 final ServletResponse servletResponse) throws IOException {

            // Set content type
            servletResponse.setContentType("text/html");

            // Output
            final ServletOutputStream outputStream = servletResponse.getOutputStream();
            outputStream.write(new byte[]{0,1,2,3,4,5,6,7,8,9});
         }
      };

      // Do filter pass # 1
      requestCacheFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

      // Do filter pass # 2
      requestCacheFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

      //
      verify(httpServletResponse).setHeader(HEADER_ENCODING, GZIP);
   }


   public void testDestroy() {


      // Init
      final FilterConfig filterConfig = mockFilterConfig();
      requestCacheFilter.init(filterConfig);

      // Destroy
      requestCacheFilter.destroy();

      // Assert
      assertEquals(0, requestCacheFilter.getCache().size());
   }


   private static HttpServletRequest mockRequest() {

      final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
      when(httpServletRequest.getDateHeader(HEADER_IF_MODIFIED_SINCE)).thenReturn(System.currentTimeMillis() - 10000L);
      when(httpServletRequest.getParameterMap()).thenReturn(createRequestParameterMap(11));
      when(httpServletRequest.getRequestURI()).thenReturn(TEST_REQUEST_URI);
      when(httpServletRequest.getCookies()).thenReturn(createCookies(10));


      return httpServletRequest;
   }


   private static FilterConfig mockFilterConfig() {

      final FilterConfig filterConfig = mock(FilterConfig.class);
      when(filterConfig.getInitParameter(CONFIGURATION_PATH)).thenReturn(REQUEST_CACHE_FILTER_IMPL_XML);
      when(filterConfig.getFilterName()).thenReturn(TEST_REQUEST_CACHE);
      return filterConfig;
   }


   private static Cookie[] createCookies(final int cookieCount) {

      final Cookie[] cookies = new Cookie[cookieCount];
      for (int i = 0; i < cookieCount; i++) {
         cookies[i] = new Cookie("test_cookie_name" + i, "test_cookie_value" + i);
      }
      return cookies;
   }


   private static Map createRequestParameterMap(final int entryCount) {

      final Map<String, String[]> parameterMap = new HashMap<String, String[]>(entryCount);
      for (int i = 0; i < entryCount; i++) {

         final String paramName = "test_name_" + i;
         final String[] paramValues = new String[entryCount];
         for (int j = 0; j < entryCount; j++) {
            paramValues[j] = "test_value_" + j;
         }
         parameterMap.put(paramName, paramValues);

      }

      return parameterMap;
   }


   public void testToString() {

      assertNotNull(requestCacheFilter.toString());

   }


   public void setUp() throws Exception {

      super.setUp();

      requestCacheFilter = new RequestCacheFilterImpl();
   }


   public void tearDown() throws Exception {

      requestCacheFilter = null;

      super.tearDown();
   }


   public String toString() {

      return "RequestCacheFilterImplTest{" +
              "requestCacheFilter=" + requestCacheFilter +
              "} " + super.toString();
   }
}
