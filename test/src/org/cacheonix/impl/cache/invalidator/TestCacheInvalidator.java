package org.cacheonix.impl.cache.invalidator;

import java.util.Properties;

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.cache.invalidator.CacheInvalidatorContext;
import org.cacheonix.cache.invalidator.Invalidateable;

/**
 * A test cache invalidator that allos to verify that ptoper methods are called when {@link
 * CacheInvalidatorFactory#createInvalidator(String, String, Properties)} is called.
 */
public final class TestCacheInvalidator implements CacheInvalidator {

   private CacheInvalidatorContext context = null;


   public void setContext(final CacheInvalidatorContext context) {

      this.context = context;
   }


   public void process(final Invalidateable cacheElement) {
   }


   CacheInvalidatorContext getContext() {

      return context;
   }

   public String toString() {

      return "TestCacheInvalidator{" +
              "context=" + context +
              '}';
   }
}
