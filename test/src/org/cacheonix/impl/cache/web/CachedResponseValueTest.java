package org.cacheonix.impl.cache.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.http.Cookie;

import junit.framework.TestCase;
import org.cacheonix.impl.net.serializer.Serializer;
import org.cacheonix.impl.net.serializer.SerializerFactory;
import org.cacheonix.impl.util.array.HashMap;

import static org.cacheonix.impl.net.serializer.Serializer.TYPE_JAVA;

/**
 * A tester for {@link CachedResponseValue}.
 */
public final class CachedResponseValueTest extends TestCase {


   private static final String REDIRECT_URL = "Test redirect URL";

   private static final String CONTENT_TYPE = "Test content type";

   private static final int CONTENT_LENGTH = 111;

   private static final int STATUS_CODE = 222;

   private static final Locale LOCALE = Locale.US;

   private static final String STATUS_MESSAGE = "Test status message";


   private static final byte[] CHARACTER_RESPONSE = new byte[]{0, 1, 2, 3, 4, 5};


   private static final byte[] BYTE_RESPONSE = new byte[]{6, 7, 8, 9, 10};

   private static final HashMap<String, Collection<Header>> HEADERS = createHeaders();

   private static final HashMap<String, Cookie> COOKIES = createCookies();

   private CachedResponseValue cachedResponseValue;


   public void testGetContentType() throws Exception {

      assertEquals(CONTENT_TYPE, cachedResponseValue.getContentType());
   }


   public void testGetContentLength() throws Exception {

      assertEquals(CONTENT_LENGTH, cachedResponseValue.getContentLength());
   }


   public void testGetLocale() throws Exception {

      assertEquals(LOCALE, cachedResponseValue.getLocale());
   }


   public void testGetStatusMessage() throws Exception {

      assertEquals(STATUS_MESSAGE, cachedResponseValue.getStatusMessage());
   }


   public void testGetRedirectUrl() throws Exception {

      assertEquals(REDIRECT_URL, cachedResponseValue.getRedirectUrl());
   }


   public void testGetStatusCode() throws Exception {

      assertEquals(STATUS_CODE, cachedResponseValue.getStatusCode());
   }


   public void testGetByteResponse() throws Exception {

      assertEquals(BYTE_RESPONSE, cachedResponseValue.getByteResponse());
   }


   public void testGetCookies() throws Exception {

      assertEquals(COOKIES, cachedResponseValue.getCookies());
   }


   public void testGetHeaders() throws Exception {

      assertEquals(HEADERS, cachedResponseValue.getHeaders());
   }


   public void testWriteReadExternal() throws Exception {

      final Serializer ser = SerializerFactory.getInstance().getSerializer(TYPE_JAVA);
      assertEquals(cachedResponseValue, ser.deserialize(ser.serialize(cachedResponseValue)));
   }


   public void testEquals() {

      assertEquals(cachedResponseValue, createCachedResponseValue());
   }


   public void testHashCode() {

      assertEquals(541918403, cachedResponseValue.hashCode());
   }


   public void setUp() throws Exception {

      super.setUp();

      cachedResponseValue = createCachedResponseValue();
   }


   private static CachedResponseValue createCachedResponseValue() {

      return new CachedResponseValue(STATUS_MESSAGE, BYTE_RESPONSE, REDIRECT_URL,
              CONTENT_TYPE, CONTENT_LENGTH, STATUS_CODE, LOCALE, COOKIES, HEADERS);
   }


   private static HashMap<String, Cookie> createCookies() {

      final HashMap<String, Cookie> result = new HashMap<String, Cookie>();
      final int cookieCount = 10;
      for (int i = 0; i < cookieCount; i++) {

         final String cookieName = "test_cookie_" + i;
         final String cookieValue = "test_cookie_value_" + i;
         result.put(cookieName, new Cookie(cookieName, cookieValue));
      }

      return result;
   }


   private static HashMap<String, Collection<Header>> createHeaders() {

      final HashMap<String, Collection<Header>> result = new HashMap<String, Collection<Header>>();
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