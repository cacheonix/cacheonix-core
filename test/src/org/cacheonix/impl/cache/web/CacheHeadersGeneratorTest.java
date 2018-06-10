package org.cacheonix.impl.cache.web;

import java.util.Map;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.TimeImpl;

import static org.cacheonix.impl.cache.web.CacheHeadersGenerator.CACHE_CONTROL;
import static org.cacheonix.impl.cache.web.CacheHeadersGenerator.DATE;
import static org.cacheonix.impl.cache.web.CacheHeadersGenerator.EXPIRES;
import static org.cacheonix.impl.cache.web.CacheHeadersGenerator.LAST_MODIFIED;

/**
 * A tester for {@link CacheHeadersGenerator}.
 */
public final class CacheHeadersGeneratorTest extends TestCase {


   private static final int CREATE_TIME_MILLIS = 1334567890;

   private static final int EXPIRATION_TIME_MILLIS = 1334567890;

   private CacheHeadersGenerator cacheHeadersGenerator;


   public void testCreateHeaders() {

      // "Expires" header
      final TimeImpl createdTime = new TimeImpl(CREATE_TIME_MILLIS, 0);
      final TimeImpl expirationTime = new TimeImpl(EXPIRATION_TIME_MILLIS, 0);
      final Map<String, Header> headers = cacheHeadersGenerator.createHeaders(createdTime, expirationTime);
      final DateHeader expiresHeader = (DateHeader) headers.get(EXPIRES);
      assertEquals(EXPIRATION_TIME_MILLIS, expiresHeader.getValue());

      // "Cache-Control" header
      final StringHeader cacheControlHeader = (StringHeader) headers.get(CACHE_CONTROL);
      assertEquals("public, max-age=1334567", cacheControlHeader.getValue());

      // "Date" header
      final DateHeader dateHeader = (DateHeader) headers.get(DATE);
      assertNotNull(dateHeader);

      // "Last-Modified" header
      final DateHeader lastModified = (DateHeader) headers.get(LAST_MODIFIED);
      assertEquals(EXPIRATION_TIME_MILLIS, lastModified.getValue());
   }


   public void setUp() throws Exception {

      super.setUp();

      cacheHeadersGenerator = new CacheHeadersGenerator();
   }


   public void tearDown() throws Exception {

      cacheHeadersGenerator = null;
      super.tearDown();
   }
}