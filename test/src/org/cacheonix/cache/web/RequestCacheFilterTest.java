package org.cacheonix.cache.web;

import junit.framework.TestCase;

/**
 * A tester for {@link RequestCacheFilter}.
 */
public final class RequestCacheFilterTest extends TestCase {


   private RequestCacheFilter requestCacheFilter;


   public void testInit() throws Exception {

   }


   public void testDoFilter() throws Exception {

   }


   public void testDestroy() throws Exception {

   }


   public void tearDown() throws Exception {

      requestCacheFilter = null;

      super.tearDown();
   }


   public void setUp() throws Exception {

      super.setUp();

      requestCacheFilter = new RequestCacheFilter();
   }


   public String toString() {

      return "RequestCacheFilterTest{" +
              "requestCacheFilter=" + requestCacheFilter +
              "} " + super.toString();
   }
}