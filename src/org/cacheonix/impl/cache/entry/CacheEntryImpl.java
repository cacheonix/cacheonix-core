package org.cacheonix.impl.cache.entry;

import org.cacheonix.cache.entry.CacheEntry;
import org.cacheonix.impl.clock.Time;

/**
 * Created by vimeshev on 4/13/16.
 */
public class CacheEntryImpl implements CacheEntry {

   /**
    * The key.
    */
   private Object key;

   /**
    * The value.
    */
   private Object value;

   /**
    * Time the element was created.
    */
   private Time createdTime;

   /**
    * Time to expire.
    */
   private Time expirationTime;


   /**
    * Creates a new instance of <tt>CacheEntryImpl</tt>
    *
    * @param key            the key.
    * @param value          the value.
    * @param createdTime    the time this entry was created.
    * @param expirationTime the time this entry expires.
    */
   public CacheEntryImpl(final Object key, final Object value, final Time createdTime, final Time expirationTime) {

      this.expirationTime = expirationTime;
      this.createdTime = createdTime;
      this.value = value;
      this.key = key;
   }


   /**
    * Returns a cache entry key.
    *
    * @return the cache entry key.
    */
   public Object getKey() {

      return key;
   }


   /**
    * Returns a cache entry value.
    *
    * @return the cache entry value.
    */
   public Object getValue() {


      return value;
   }


   /**
    * Returns time this element expires.
    *
    * @return time this element expires.
    */
   public Time getExpirationTime() {

      return expirationTime;
   }


   /**
    * Returns the time this element was created.
    *
    * @return the time this element was created.
    */
   public Time getCreatedTime() {

      return createdTime;
   }


   public String toString() {

      return "CacheEntryImpl{" +
              "key=" + key +
              ", value=" + value +
              ", createdTime=" + createdTime +
              ", expirationTime=" + expirationTime +
              '}';
   }
}
