package org.cacheonix.cache.web;

import junit.framework.TestCase;

/**
 * A tester for {@link RequestCacheFilter}.
 */
public final class RequestCacheFilterTest extends TestCase {


   private RequestCacheFilter requestCacheFilter;


   public void tearDown() throws Exception {

      requestCacheFilter = null;

      super.tearDown();
   }


   public void setUp() throws Exception {

      super.setUp();

      requestCacheFilter = new RequestCacheFilter();
   }


   public void testoString() {

      assertNotNull(requestCacheFilter.toString());
   }


   public String toString() {

      return "RequestCacheFilterTest{" +
              "requestCacheFilter=" + requestCacheFilter +
              "} " + super.toString();
   }
}