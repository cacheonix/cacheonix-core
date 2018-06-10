package org.cacheonix.impl.cache.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashMap;

import static org.cacheonix.impl.cache.web.CachedResponseValue.CONTENT_TYPE;
import static org.cacheonix.impl.cache.web.CachedResponseValue.ENCODING;
import static org.cacheonix.impl.net.serializer.Serializer.TYPE_JAVA;

/**
 * A tester for {@link CachedResponseValue}.
 */
public final class CachedResponseValueTest extends TestCase {


   private static final String TEST_REDIRECT_URL = "Test redirect URL";

   private static final String TEST_CONTENT_TYPE = "text/html";

   private static final int TEST_CONTENT_LENGTH = 111;

   private static final int TEST_STATUS_CODE = 222;

   private static final Locale TEST_LOCALE = Locale.US;

   private static final String TEST_STATUS_MESSAGE = "Test status message";


   private static final byte[] TEST_CHARACTER_RESPONSE = new byte[]{0, 1, 2, 3, 4, 5};


   private static final byte[] TEST_BYTE_RESPONSE = new byte[]{6, 7, 8, 9, 10};

   private static final Map<String, Collection<Header>> TEST_HEADERS = createHeaders();

   private static final Map<String, Cookie> TEEST_COOKIES = createCookies();


   private CachedResponseValue cachedResponseValue;


   public void testGetContentType() {

      assertEquals(TEST_CONTENT_TYPE, cachedResponseValue.getContentType());
   }


   public void testGetContentLength() {

      assertEquals(TEST_CONTENT_LENGTH, cachedResponseValue.getContentLength());
   }


   public void testGetLocale() {

      assertEquals(TEST_LOCALE, cachedResponseValue.getLocale());
   }


   public void testGetStatusMessage() {

      assertEquals(TEST_STATUS_MESSAGE, cachedResponseValue.getStatusMessage());
   }


   public void testGetRedirectUrl() {

      assertEquals(TEST_REDIRECT_URL, cachedResponseValue.getRedirectUrl());
   }


   public void testGetStatusCode() {

      assertEquals(TEST_STATUS_CODE, cachedResponseValue.getStatusCode());
   }


   public void testGetByteResponse() {

      assertEquals(true, Arrays.equals(TEST_BYTE_RESPONSE, cachedResponseValue.getByteResponse()));
   }


   public void testGetCookies() {

      assertEquals(TEEST_COOKIES, cachedResponseValue.getCookies());
   }


   public void testGetHeaders() {

      assertEquals(TEST_HEADERS, cachedResponseValue.getHeaders());
   }


   public void testIsTextContentType() {

      assertTrue(cachedResponseValue.isTextContentType());
   }


   public void testIsCompressed() {

      assertTrue(cachedResponseValue.isCompressed());
   }


   public void testWriteReadExternal() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(cachedResponseValue, ser.deserialize(ser.serialize(cachedResponseValue)));
   }


   public void testEquals() {

      assertEquals(cachedResponseValue, createCachedResponseValue());
   }


   public void testHashCode() {

      assertEquals(-760112032, cachedResponseValue.hashCode());
   }


   public void setUp() throws Exception {

      super.setUp();

      cachedResponseValue = createCachedResponseValue();
   }


   private static CachedResponseValue createCachedResponseValue() {

      return new CachedResponseValue(TEST_STATUS_MESSAGE, TEST_BYTE_RESPONSE, TEST_REDIRECT_URL,
              TEST_CONTENT_TYPE, TEST_CONTENT_LENGTH, TEST_STATUS_CODE, TEST_LOCALE, TEEST_COOKIES, TEST_HEADERS);
   }


   private static Map<String, Cookie> createCookies() {

      final Map<String, Cookie> result = new HashMap<String, Cookie>();
      final int cookieCount = 10;
      for (int i = 0; i < cookieCount; i++) {

         final String cookieName = "test_cookie_" + i;
         final String cookieValue = "test_cookie_value_" + i;
         result.put(cookieName, new Cookie(cookieName, cookieValue));
      }

      return result;
   }


   private static Map<String, Collection<Header>> createHeaders() {

      final Map<String, Collection<Header>> result = new HashMap<String, Collection<Header>>();
      final int headerValueCount = 10;
      final int headerCount = 10;

      for (int i = 0; i < headerCount; i++) {

         final String headerName = "test_header_" + i;
         final ArrayList<Header> values = new ArrayList<Header>(headerValueCount);

         for (int j = 0; j < headerValueCount; j++) {

            final String headerValue = "test_header_value " + j;
            final Header header = new StringHeader(headerName, headerValue);
            values.add(header);
         }
         result.put(headerName, values);
      }

      // Add text/html content type
      final List<Header> contentTypeHeaders = new ArrayList<Header>(1);
      contentTypeHeaders.add(new StringHeader(CONTENT_TYPE, "text/html"));
      result.put(CONTENT_TYPE, contentTypeHeaders);

      // Add encoding
      final List<Header> encodingHeaders = new ArrayList<Header>(1);
      encodingHeaders.add(new StringHeader(ENCODING, "gzip"));
      result.put(ENCODING, encodingHeaders);

      return result;
   }


   public void tearDown() throws Exception {

      cachedResponseValue = null;

      super.tearDown();
   }


   public String toString() {

      return "CachedResponseValueTest{" +
              "cachedResponseValue=" + cachedResponseValue +
              "} " + super.toString();
   }
}