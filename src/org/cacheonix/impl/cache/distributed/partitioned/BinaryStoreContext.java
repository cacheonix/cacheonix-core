package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.util.cache.ObjectSizeCalculator;

/**
 * Created by vimeshev on 1/30/16.
 */
public interface BinaryStoreContext {

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

   /**
    * Sets an auxiliary, user-provided data source. This method must be called immediately after de-serialization is
    * complete.
    *
    * @param dataSource the data source to set.
    */
   void setDataSource(BinaryStoreDataSource dataSource);

   /**
    * Sets an auxiliary, user-provided data store. This method must be called immediately after de-serialization is
    * complete.
    *
    * @param dataStore the data store to set.
    */
   void setDataStore(DataStore dataStore);


   /**
    * Returns a supplier of data to the cache for the case when a key is not in the cache (a cache miss).
    *
    * @return a supplier of data to the cache for the case when a key is not in the cache (a cache miss).
    */
   BinaryStoreDataSource getDataSource();

   /**
    * Returns a write-through data store that isolates the application from the details of how the data is stored.
    *
    * @return a write-through data store that isolates the application from the details of how the data is stored.
    */
   DataStore getDataStore();
}
