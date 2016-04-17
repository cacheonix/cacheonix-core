package org.cacheonix.impl.cache.web;

import java.util.Date;
import java.util.Map;

import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.util.array.HashMap;

/**
 * A generator of HTTP client headers.
 */
final class CacheHeadersGenerator {


   static final String LAST_MODIFIED = "Last-Modified";

   static final String CACHE_CONTROL = "Cache-Control";

   static final String EXPIRES = "Expires";

   static final String DATE = "Date";


   /**
    * Creates cache headers.
    *
    * @param createdTime    the time the cached request was created.
    * @param expirationTime the time the cached request expires.
    * @return a map with Cached headers.
    * @see #CACHE_CONTROL
    * @see #EXPIRES
    * @see #DATE
    */
   public Map<String, Header> createHeaders(final Time createdTime, final Time expirationTime) {

      final Map<String, Header> headers = new HashMap<String, Header>(5);

      // Add "Cache-Control" header
      addCacheControlHeader(headers, expirationTime);

      // Create "Expires" header. Example: Mon, 29 Apr 2013 21:44:55 GMT
      addExpiresHeader(headers, expirationTime);

      // Add "Last-Modified"
      addLastModifiedHeader(headers, createdTime);

      // Add "Date" header
      addDateHeader(headers);

      return headers;
   }


   /**
    * Adds a "Cache-Control" header.
    *
    * @param headers        the map to add the header to.
    * @param expirationTime the expiration time.
    */
   private static void addCacheControlHeader(final Map<String, Header> headers, final Time expirationTime) {

      final long maxAgeSeconds = expirationTime == null ? 31557600L : expirationTime.getMillis() / 1000L;
      final String cacheControl = "public, max-age=" + maxAgeSeconds;
      headers.put(CACHE_CONTROL, new StringHeader(CACHE_CONTROL, cacheControl));
   }


   /**
    * Adds a "Expires" header.
    *
    * @param headers        the map to add the header to.
    * @param expirationTime the expiration time.
    */
   private static void addExpiresHeader(final Map<String, Header> headers, final Time expirationTime) {

      final long expiresDate = createExpiresDate(expirationTime);
      headers.put(EXPIRES, new DateHeader(EXPIRES, expiresDate));
   }


   /**
    * Adds a "Last-Modified" header.
    *
    * @param headers     the list of headers to add to.
    * @param createdTime the time the cached request was created.
    */
   private static void addLastModifiedHeader(final Map<String, Header> headers, final Time createdTime) {

      if (createdTime == null) {
         return;
      }

      headers.put(LAST_MODIFIED, new DateHeader(LAST_MODIFIED, createdTime.getMillis()));
   }


   /**
    * Adds a "Date" header.
    *
    * @param headers the map to add the header to.
    */
   private static void addDateHeader(final Map<String, Header> headers) {

      final Date date = new Date();
      headers.put(DATE, new DateHeader(DATE, date.getTime()));
   }


   private static long createExpiresDate(final Time expirationTime) {

      if (expirationTime == null) {

         return System.currentTimeMillis() + 31556952000L;
      }

      return expirationTime.getMillis();
   }
}
