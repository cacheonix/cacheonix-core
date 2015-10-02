package org.cacheonix.impl.cache.datasource;

import org.cacheonix.CacheonixTestCase;
import org.cacheonix.impl.clock.TimeImpl;

import static org.mockito.Mockito.mock;

/**
 * A tester for PrefetchCommandImpl.
 */
public final class PrefetchCommandImplTest extends CacheonixTestCase {


   private PrefetchCommandImpl prefetchCommand;


   public void testToString() {

      assertNotNull(prefetchCommand.toString());
   }


   public void setUp() throws Exception {

      super.setUp();
      prefetchCommand = new PrefetchCommandImpl(mock(PrefetchElementUpdater.class),
              mock(BinaryStoreDataSource.class), toBinary("Test key"), new TimeImpl(), 1);
   }


   public void tearDown() throws Exception {

      prefetchCommand = null;
      super.tearDown();
   }
}
