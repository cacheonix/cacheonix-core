package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.impl.cache.item.Binary;
import org.cacheonix.impl.cache.item.InvalidObjectException;
import org.cacheonix.impl.cache.store.ReadableElement;
import org.cacheonix.impl.clock.Time;
import org.cacheonix.impl.config.FrontCacheConfiguration;

/**
 * A front cache is a cache used by the partitioned cache to keep frequently used remote keys locally.
 */
public interface FrontCache {

   void put(Binary key, Binary value, Time expirationTime);

   /**
    * Clears front cache.
    */
   void clear();

   ReadableElement get(Binary key) throws InvalidObjectException;

   /**
    * Returns the configuration of this front cache.
    *
    * @return the configuration of this front cache.
    */
   FrontCacheConfiguration getFrontCacheConfiguration();

   /**
    * Clears bucket cache.
    *
    * @param bucketNumber the bucket number to invalidate.
    */
   void clearBucket(int bucketNumber);
}
