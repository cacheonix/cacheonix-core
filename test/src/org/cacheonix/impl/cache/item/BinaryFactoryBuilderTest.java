package org.cacheonix.impl.cache.item;

import java.io.Serializable;

import junit.framework.TestCase;

import static org.cacheonix.impl.cache.item.BinaryType.*;

/**
 * A tester for BinaryFactoryBuilder.
 */
public final class BinaryFactoryBuilderTest extends TestCase {


   private BinaryFactoryBuilder binaryFactoryBuilder;

   private static final Serializable[] SERIALIZABLE_ARRAY_0 = {0, 1, 2};

   private static final Serializable[] SERIALIZABLE_ARRAY_1 = {0, 1, 2};


   public final void testCreateCompressedCopyFactory() throws Exception {

      final BinaryFactory factory = binaryFactoryBuilder.createFactory(BY_COMPRESSED_COPY);
      assertEquals(new CompressedBinary(SERIALIZABLE_ARRAY_0), factory.createBinary(SERIALIZABLE_ARRAY_1));
   }


   public final void testCreateCopyFactory() throws Exception {

      final BinaryFactory factory = binaryFactoryBuilder.createFactory(BY_COPY);
      assertEquals(new PassByCopyBinary(SERIALIZABLE_ARRAY_0), factory.createBinary(SERIALIZABLE_ARRAY_1));
   }


   public final void testCreateCopyByReferenceFactory() throws Exception {

      final BinaryFactory factory = binaryFactoryBuilder.createFactory(BY_REFERERENCE);
      assertEquals(new PassByReferenceBinary(SERIALIZABLE_ARRAY_0), factory.createBinary(SERIALIZABLE_ARRAY_1));
   }


   public void setUp() throws Exception {

      super.setUp();

      binaryFactoryBuilder = new BinaryFactoryBuilder();
   }


   public void tearDown() throws Exception {

      binaryFactoryBuilder = null;

      super.tearDown();
   }
}