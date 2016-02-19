package org.cacheonix.impl.cache.store;

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;

/**
 *
 */
public final class BinaryStoreElementContextImpl implements BinaryStoreElementContext {

   /**
    * Object size calculator.
    */
   private ObjectSizeCalculator objectSizeCalculator;

   /**
    * Cache element invalidator.
    */
   private CacheInvalidator invalidator;

   /**
    * Storage used to store this element.
    */
   private DiskStorage diskStorage;


   public ObjectSizeCalculator getObjectSizeCalculator() {

      return objectSizeCalculator;
   }


   public void setObjectSizeCalculator(final ObjectSizeCalculator objectSizeCalculator) {

      this.objectSizeCalculator = objectSizeCalculator;
   }


   public CacheInvalidator getInvalidator() {

      return invalidator;
   }


   public void setInvalidator(final CacheInvalidator invalidator) {

      this.invalidator = invalidator;
   }


   public DiskStorage getDiskStorage() {

      return diskStorage;
   }


   public void setDiskStorage(final DiskStorage diskStorage) {

      this.diskStorage = diskStorage;
   }
}
