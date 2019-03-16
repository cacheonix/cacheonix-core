package org.cacheonix.impl.net;

import java.util.Arrays;

import junit.framework.TestCase;


/**
 * A tester for {@link Protocol}.
 */
public class ProtocolTest extends TestCase {

   public void testGetProtocolSignature() {

      assertTrue(Arrays.equals("cchnx".getBytes(), Protocol.getProtocolSignature()));
   }


   public void testGetProtocolSignatureLength() {

      assertEquals(5, Protocol.getProtocolSignatureLength());
   }


   public void testGetProtocolMagicNumber() {

      assertEquals(65973751, Protocol.getProtocolMagicNumber());
   }


   public void testGetProtocolVersion() {

      assertEquals(11, Protocol.getProtocolVersion());
   }
}