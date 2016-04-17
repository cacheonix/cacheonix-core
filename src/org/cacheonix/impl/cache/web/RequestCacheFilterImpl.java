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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;
import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.array.HashMap;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static org.cacheonix.impl.util.StringUtils.isBlank;

/**
 * This filter provides an ability to cache web app requests.
 */
public final class RequestCacheFilterImpl implements Filter {

   static final String IF_MODIFIED_SINCE = "If-Modified-Since";

   static final String CONFIGURATION_PATH = "configurationPath";

   /**
    * A generator of HTTP cache headers.
    */
   private final CacheHeadersGenerator cacheHeadersGenerator = new CacheHeadersGenerator();

   /**
    * A request cache.
    */
   private Cache<CachedResponseKey, CachedResponseValue> cache;


   public void init(final FilterConfig filterConfig) {

      final String filterName = filterConfig.getFilterName();

      // Get cache name
      final String configurationPath = filterConfig.getInitParameter(CONFIGURATION_PATH);
      final String cacheName = filterConfig.getInitParameter("cacheName");

      // Get Cacheonix manager
      final Cacheonix cacheonix;
      if (isBlank(configurationPath)) {

         cacheonix = Cacheonix.getInstance();
      } else {
         cacheonix = Cacheonix.getInstance(configurationPath);
      }

      // Get cache
      if (isBlank(cacheName)) {

         cache = cacheonix.getCache(filterName);
      } else {

         cache = cacheonix.getCache(cacheName);
      }
   }


   @SuppressWarnings({"unchecked", "ReuseOfLocalVariable"})
   public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
           final FilterChain filterChain) throws IOException, ServletException {

      // Process HTTP request
      if (servletRequest instanceof HttpServletRequest) {

         // Create a key
         final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
         final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

         // Parameters
         final Map originalParameterMap = httpServletRequest.getParameterMap();
         final Map<String, List<String>> parameterMap = new HashMap<String, List<String>>(originalParameterMap.size());
         for (final Object o : originalParameterMap.entrySet()) {

            final Entry entry = (Entry) o;
            final String paramName = (String) entry.getKey();
            final List<String> paramValues = Arrays.asList((String[]) entry.getValue());
            parameterMap.put(paramName, paramValues);
         }

         // Cookies
         final List<Cookie> requestCookies = Arrays.asList(httpServletRequest.getCookies());


         // URI
         final String requestURI = httpServletRequest.getRequestURI();

         // Create the key
         final CachedResponseKey cachedResponseKey = new CachedResponseKey(requestURI, parameterMap, requestCookies);
         final CacheEntry entry = cache.entry(cachedResponseKey);
         if (entry == null) {

            // Not found in cache, process the filter chain and cache the response

            // Create a HttpServletResponse wrapper to collect output.
            final ServletOutputWrapperFactoryImpl outputWrapperFactory = new ServletOutputWrapperFactoryImpl();
            final CachingHttpServletResponseWrapper responseWrapper = new CachingHttpServletResponseWrapper(
                    httpServletResponse, outputWrapperFactory);

            // Execute the chain
            filterChain.doFilter(servletRequest, responseWrapper);

            // Cache only responses without a error sent
            final Integer error = responseWrapper.getError();
            if (error == null) {

               // Put the output into the cache
               final Map<String, Collection<Header>> headers = responseWrapper.getHeaders();
               final Map<String, Cookie> cookies = responseWrapper.getCookies();
               final String statusMessage = responseWrapper.getStatusMessage();
               final int contentLength = responseWrapper.getContentLength();
               final String contentType = responseWrapper.getContentType();
               final String redirectUrl = responseWrapper.getRedirectUrl();
               final byte[] byteOutput = responseWrapper.getByteOutput();
               final int statusCode = responseWrapper.getStatusCode();
               final Locale locale = responseWrapper.getLocale();
               final CachedResponseValue cachedResponseValue = new CachedResponseValue(statusMessage, byteOutput,
                       redirectUrl, contentType, contentLength, statusCode, locale, cookies, headers);
               cache.put(cachedResponseKey, cachedResponseValue);
            }
         } else {

            // Found in cache
            final CachedResponseValue cachedResponseValue = (CachedResponseValue) entry.getValue();

            // See if the value has been modified since the client cache received it.
            final Time createdTime = entry.getCreatedTime();
            if (createdTime != null) {

               // "The If-Modified-Since request-header field is used with a method to make it conditional:
               // if the requested variant has not been modified since the time specified in this field,
               // an entity will not be returned from the server; instead, a 304 (not modified) response
               // will be returned without any message-body"
               final long longIfModifiedSince = httpServletRequest.getDateHeader(IF_MODIFIED_SINCE);
               if (longIfModifiedSince > 0) {

                  final Date createdDate = new Date(createdTime.getMillis());
                  final Date ifModifiedSince = new Date(longIfModifiedSince);
                  if (ifModifiedSince.compareTo(createdDate) > 0) {

                     // Return 304 NOT MODIFIED
                     httpServletResponse.setStatus(SC_NOT_MODIFIED);

                     return;
                  }
               }
            }

            // Manage redirects
            final String redirectUrl = cachedResponseValue.getRedirectUrl();
            if (!isBlank(redirectUrl)) {

               // Cached response was a redirect, send redirect and exit
               httpServletResponse.sendRedirect(redirectUrl);

               return;
            }

            // Cached response was a normal response, not a redirect

            // Add status code and message
            final String statusMessage = cachedResponseValue.getStatusMessage();
            final int statusCode = cachedResponseValue.getStatusCode();
            if (statusCode != 0) {

               if (isBlank(statusMessage)) {

                  httpServletResponse.setStatus(statusCode);
               } else {

                  //noinspection deprecation
                  httpServletResponse.setStatus(statusCode, statusMessage);
               }
            }

            // Add content length
            final int contentLength = cachedResponseValue.getContentLength();
            if (contentLength > 0) {

               httpServletResponse.setContentLength(contentLength);
            }

            // Add content type
            final String contentType = cachedResponseValue.getContentType();
            if (!isBlank(contentType)) {

               httpServletResponse.setContentType(contentType);
            }

            // Add locale
            final Locale locale = cachedResponseValue.getLocale();
            if (locale != null) {

               httpServletResponse.setLocale(locale);
            }

            // Add cached cookies to the response
            for (final Cookie cookie : cachedResponseValue.getCookies().values()) {

               httpServletResponse.addCookie(cookie);
            }

            // Add cached headers to the response
            final Map<String, Collection<Header>> headers = cachedResponseValue.getHeaders();
            for (final Entry<String, Collection<Header>> headerEntry : headers.entrySet()) {

               final Collection<Header> value = headerEntry.getValue();
               for (final Header header : value) {

                  header.addToResponse(httpServletResponse);
               }
            }

            // Add client-level cache headers if they are not already in the cached response
            final Time expirationTime = entry.getExpirationTime();
            final Map<String, Header> clientCacheHeaders = cacheHeadersGenerator.createHeaders(createdTime,
                    expirationTime);
            for (final Entry<String, Header> clientCacheHeaderEntry : clientCacheHeaders.entrySet()) {

               // Don't add client cache header to response if it has been generated by the app
               final String clientCacheHeaderName = clientCacheHeaderEntry.getKey();
               if (headers.containsKey(clientCacheHeaderName)) {
                  continue;
               }

               // Add client cache header to response
               final Header clientCacheHeader = clientCacheHeaderEntry.getValue();
               clientCacheHeader.addToResponse(httpServletResponse);
            }

            // Output cached response
            final byte[] binaryResponse = cachedResponseValue.getByteResponse();
            if (binaryResponse != null) {

               final ServletOutputStream outputStream = servletResponse.getOutputStream();
               outputStream.write(binaryResponse);
            }
         }

      } else {

         // TODO: simeshev@cacheonix.org - 2016-03-24 - Caching of other types of requests is not
         // supported yet. Need to look at this after having feedback for HttpServletRequest handling.
         filterChain.doFilter(servletRequest, servletResponse);
      }
   }


   /**
    * This implementation of {@link Filter#destroy()} clears the request cache.
    */
   public void destroy() {

      if (cache == null) {
         return;
      }

      cache.clear();
   }


   /**
    * Returns a {@link Cache} instance used to store the response.
    *
    * @return a {@link Cache} instance used to store the response.
    */
   @SuppressWarnings("ReturnOfCollectionOrArrayField")
   Cache<CachedResponseKey, CachedResponseValue> getCache() {

      return cache;
   }


   public String toString() {

      return "RequestCacheFilterImpl{" +
              "cache=" + (cache == null ? "null" : Integer.toString(cache.size())) +
              '}';
   }
}
