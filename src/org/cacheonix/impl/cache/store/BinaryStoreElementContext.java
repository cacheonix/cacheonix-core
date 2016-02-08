package org.cacheonix.impl.cache.store;

import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;

/**
 *
 */
public interface BinaryStoreElementContext {

   /**
    * Returns object size calculator.
    */
   ObjectSizeCalculator getObjectSizeCalculator();

   /**
    * Sets object size calculator.
    */
   void setObjectSizeCalculator(ObjectSizeCalculator objectSizeCalculator);

   /**
    * Returns cache element invalidator.
    */
   CacheInvalidator getInvalidator();

   /**
    * Sets cache element invalidator.
    */
   void setInvalidator(CacheInvalidator invalidator);

   /**
    * Returns storage used to store this element.
    */
   DiskStorage getDiskStorage();

   /**
    * Sets disk storage to use to store this element.
    */
   void setDiskStorage(DiskStorage diskStorage);
}
