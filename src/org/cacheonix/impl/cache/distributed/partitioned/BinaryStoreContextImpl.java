package org.cacheonix.impl.cache.distributed.partitioned;

import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;

/**
 * An implementation of the {@link BinaryStoreContext}.
 */
public class BinaryStoreContextImpl implements BinaryStoreContext {

   /**
    * Object size calculator.
    */
   private ObjectSizeCalculator objectSizeCalculator = null;

   /**
    * Cache element invalidator.
    */
   private CacheInvalidator invalidator = null;

   /**
    * Storage used to store this element.
    */
   private DiskStorage diskStorage = null;

   /**
    * A supplier of data to the cache for the case when a key is not in the cache (a cache miss)
    */
   private BinaryStoreDataSource dataSource = null;

   /**
    * A write-through data store that isolates the application from the details of how the data is stored.
    */
   private DataStore dataStore = null;


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


   public void setDataSource(final BinaryStoreDataSource dataSource) {

      this.dataSource = dataSource;
   }


   public void setDataStore(final DataStore dataStore) {

      this.dataStore = dataStore;
   }


   public BinaryStoreDataSource getDataSource() {

      return dataSource;
   }


   public DataStore getDataStore() {

      return dataStore;
   }


   public String toString() {

      return "BinaryStoreContextImpl{" +
              "objectSizeCalculator=" + objectSizeCalculator +
              ", invalidator=" + invalidator +
              ", diskStorage=" + diskStorage +
              ", dataSource=" + dataSource +
              ", dataStore=" + dataStore +
              '}';
   }
}
