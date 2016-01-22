package org.cacheonix.impl.cache.datasource;

import java.util.Properties;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.Clock;
import org.mockito.Mockito;

/**
 * A tester for {@link BinaryStoreDataSourceFactory}.
 */
public final class BinaryStoreDataSourceFactoryTest extends TestCase {

   private BinaryStoreDataSourceFactory binaryStoreDataSourceFactory;

   private Clock clock;


   /**
    * Confirms that supplying null DataSource class results in a dummy datasource.
    */
   public final void testCreateDummyDataSource() throws Exception {

      final BinaryStoreDataSource binaryStoreDataSource = binaryStoreDataSourceFactory.createDataSource(clock,
              "test-cache-name", null, new Properties(), false, null, null);

      assertNotNull(binaryStoreDataSource);
      assertTrue(binaryStoreDataSource instanceof DummyBinaryStoreDataSource);
   }


   public void setUp() throws Exception {

      super.setUp();

      binaryStoreDataSourceFactory = new BinaryStoreDataSourceFactory();

      clock = Mockito.mock(Clock.class);
   }


   public void tearDown() throws Exception {

      binaryStoreDataSourceFactory = null;
      clock = null;

      super.tearDown();
   }


   public String toString() {

      return "BinaryStoreDataSourceFactoryTest{" +
              "clock=" + clock +
              '}';
   }
}