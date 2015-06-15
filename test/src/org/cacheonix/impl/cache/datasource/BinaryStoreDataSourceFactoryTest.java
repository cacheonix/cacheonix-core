package org.cacheonix.impl.cache.datasource;

import java.util.Properties;
import java.util.Timer;

import junit.framework.TestCase;
import org.cacheonix.impl.clock.Clock;

/**
 * A tester for {@link BinaryStoreDataSourceFactory}.
 */
public final class BinaryStoreDataSourceFactoryTest extends TestCase {

   private BinaryStoreDataSourceFactory binaryStoreDataSourceFactory;

   private Timer timer;

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

      timer = new Timer("TestTimer", false);
      clock = new Clock(1L);
      clock.attachTo(timer);
   }


   public void tearDown() throws Exception {

      binaryStoreDataSourceFactory = null;
      timer.cancel();
      timer = null;
      clock = null;

      super.tearDown();
   }


   public String toString() {

      return "BinaryStoreDataSourceFactoryTest{" +
              "binaryStoreDataSourceFactory=" + binaryStoreDataSourceFactory +
              ", timer=" + timer +
              ", clock=" + clock +
              '}';
   }
}