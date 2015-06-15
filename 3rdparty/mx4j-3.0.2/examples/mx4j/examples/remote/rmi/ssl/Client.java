/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.rmi.ssl;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to connect to a JSR 160 RMIConnectorServer over SSL. <br />
 * An RMI client that has been setup to use SSL must know the X509 certificate
 * corrispondent to the private key used by the server to encrypt the communication.
 * This X509 certificate is usually distributed by the server to the clients, that should
 * import it into a 'trust store'.
 * This trust store can be the JDK's one ($JRE_HOME/lib/security/cacerts) or a custom one.
 * In both cases the import operation can be achieved by using JDK's keytool utility.
 * Here is the command to create a custom trust store containing the X509 certificate
 * from a certificate file 'myserver.cer' distributed by the server:
 * <pre>
 * keytool -import -v -file myserver.cer -storepass storepwd -keystore trust.store -noprompt
 * </pre>
 * When using a custom trust store, the system property <b><code>javax.net.ssl.trustStore<code></b>
 * must point to the file path of the trust store. <br />
 * If instead the X509 certificate has been imported into the JDK's default trust store,
 * then it is not necessary to specify the <b><code>javax.net.ssl.trustStore<code></b> system
 * property. <br /> <br />
 * This example is meant to show the usage of the JSR 160 API: it is not an example of how to
 * setup a secure environment. <br />
 * Please refer to the JDK documentation about usage of keytool, to the JCE and JSSE documentation
 * and to a good book on Java security before porting these examples to a real environment that
 * must be secured.
 * You know what I mean :-)
 *
 * @version $Revision: 1.4 $
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // The RMI server's host: this is actually ignored by JSR 160
      // since this information is stored in the RMI stub.
      String serverHost = "localhost";

      // The host where the rmiregistry runs.
      String namingHost = "localhost";

      String jndiPath = "/ssljmxconnector";
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + serverHost + "/jndi/rmi://" + namingHost + jndiPath);
      JMXConnector connector = JMXConnectorFactory.connect(url);
      MBeanServerConnection connection = connector.getMBeanServerConnection();

      // Call the server side
      ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
      Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName, MBeanServerDelegateMBean.class, true);
      MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean)proxy;

      System.out.println(delegate.getImplementationVendor() + " is cool !");
   }
}
