package org.cacheonix.impl.net;

import junit.framework.TestCase;

import static org.cacheonix.impl.net.NetUtils.getLocalInetAddresses;

/**
 * A tester for {@link NetUtils}.
 */
public final class NetUtilsTest extends TestCase {

   public void testGetLocalInetAddresses() {

      assertFalse(getLocalInetAddresses().isEmpty());
   }
}