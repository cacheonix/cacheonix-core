package org.cacheonix.impl.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import org.cacheonix.TestUtils;

/**
 * A tester for {@link BroadcastConfiguration}
 */
public final class BroadcastConfigurationTest extends TestCase {


   private static final String CONFIG_WITH_KNOWN_ADDRESSES = "cacheonix-config-cluster-member-w-multiple-known-addresses-1.xml";


   public final void testReadKnownAddresses() throws IOException {

      final ConfigurationReader configurationReader = new ConfigurationReader();
      final FileInputStream fileInputStream = TestUtils.getTestFileInputStream(CONFIG_WITH_KNOWN_ADDRESSES);
      final CacheonixConfiguration configuration = configurationReader.readConfiguration(fileInputStream);
      final ServerConfiguration serverConfiguration = configuration.getServer();
      final BroadcastConfiguration broadcastConfiguration = serverConfiguration.getBroadcastConfiguration();
      final List<KnownAddressBroadcastConfiguration> knownAddresses = broadcastConfiguration.getKnownAddresses();
      assertEquals(2, knownAddresses.size());
      assertEquals(8878, knownAddresses.get(0).getAddressConfiguration().getPort());
      assertEquals(8879, knownAddresses.get(1).getAddressConfiguration().getPort());
   }


   public void testToString() {

      assertNotNull(new BroadcastConfiguration().toString());
   }
}
