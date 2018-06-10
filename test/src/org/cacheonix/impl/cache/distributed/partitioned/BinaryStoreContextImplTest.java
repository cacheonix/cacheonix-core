package org.cacheonix.impl.cache.distributed.partitioned;

import junit.framework.TestCase;
import org.cacheonix.cache.datastore.DataStore;
import org.cacheonix.cache.invalidator.CacheInvalidator;
import org.cacheonix.impl.cache.datasource.BinaryStoreDataSource;
import org.cacheonix.impl.cache.storage.disk.DiskStorage;
import org.cacheonix.impl.cache.util.ObjectSizeCalculator;

import static org.mockito.Mockito.mock;

/**
 * A tester for {@link BinaryStoreContextImpl}.
 */
public final class BinaryStoreContextImplTest extends TestCase {


   private BinaryStoreContextImpl binaryStoreContext;


   public void testGetObjectSizeCalculator() {

      final ObjectSizeCalculator objectSizeCalculator = mock(ObjectSizeCalculator.class);
      binaryStoreContext.setObjectSizeCalculator(objectSizeCalculator);

      assertEquals(objectSizeCalculator, binaryStoreContext.getObjectSizeCalculator());
   }


   public void testGetInvalidator() {

      final CacheInvalidator invalidator = mock(CacheInvalidator.class);
      binaryStoreContext.setInvalidator(invalidator);

      assertEquals(invalidator, binaryStoreContext.getInvalidator());
   }


   public void testGetDiskStorage() {

      final DiskStorage diskStorage = mock(DiskStorage.class);
      binaryStoreContext.setDiskStorage(diskStorage);

      assertEquals(diskStorage, binaryStoreContext.getDiskStorage());
   }


   public void testGetDataSource() {

      final BinaryStoreDataSource dataSource = mock(BinaryStoreDataSource.class);
      binaryStoreContext.setDataSource(dataSource);

      assertEquals(dataSource, binaryStoreContext.getDataSource());
   }


   public void testGetDataStore() {

      final DataStore dataStore = mock(DataStore.class);
      binaryStoreContext.setDataStore(dataStore);

      assertEquals(dataStore, binaryStoreContext.getDataStore());
   }


   public void setUp() throws Exception {

      super.setUp();

      binaryStoreContext = new BinaryStoreContextImpl();
   }


   public void tearDown() throws Exception {

      binaryStoreContext = null;

      super.tearDown();
   }


   public String toString() {

      return "BinaryStoreContextImplTest{" +
              "binaryStoreContext=" + binaryStoreContext +
              '}';
   }
}