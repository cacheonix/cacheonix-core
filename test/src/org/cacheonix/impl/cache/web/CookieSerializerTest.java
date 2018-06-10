package org.cacheonix.impl.cache.web;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

/**
 * A tester for {@link CookieSerializer}.
 */
public final class CookieSerializerTest extends TestCase {


   private static final String NAME = "name";

   private static final String VALUE = "value";


   public void testEquals() {

      final Cookie thisCookie = new Cookie(NAME, VALUE);
      final Cookie thatCookie = new Cookie(NAME, VALUE);

      assertTrue(CookieSerializer.equals(thisCookie, thatCookie));
   }


   public void testNoEquals() {

      final Cookie thisCookie = new Cookie(NAME, VALUE);
      final Cookie thatCookie = new Cookie("other_name", "other_value");

      assertFalse(CookieSerializer.equals(thisCookie, thatCookie));
   }


   public void testNotEqualsWithPathSet() {

      final Cookie thisCookie = new Cookie(NAME, VALUE);
      thisCookie.setPath("test/path");

      final Cookie thatCookie = new Cookie(NAME, VALUE);
      thatCookie.setPath("other/path");

      assertFalse(CookieSerializer.equals(thisCookie, thatCookie));
   }
}