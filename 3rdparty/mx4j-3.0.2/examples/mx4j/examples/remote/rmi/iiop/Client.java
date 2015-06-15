/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.rmi.iiop;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example shows how to connect to a JSR 160 connector server over IIOP.
 * It is very similar to the simple example also present in these examples, except
 * that it uses the IIOP protocol instead of native RMI's one, called JRMP.
 *
 * @version $Revision: 1.4 $
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // The JMXConnectorServer protocol, in this case is IIOP
      String serverProtocol = "iiop";

      // The RMI server's host: this is actually ignored by JSR 160
      // since this information is stored in the RMI stub.
      String serverHost = "host";

      // The host and port where the COSNaming service runs and the path under which the stub is registered.
      String namingHost = "localhost";
      int namingPort = 1199;
      String jndiPath = "/jmxconnector";

      // The address of the connector server
      JMXServiceURL url = new JMXServiceURL("service:jmx:" + serverProtocol + "://" + serverHost + "/jndi/iiop://" + namingHost + ":" + namingPort + jndiPath);

      // Connect a JSR 160 JMXConnector to the server side
      JMXConnector connector = JMXConnectorFactory.connect(url);

      // Retrieve an MBeanServerConnection that represent the MBeanServer the remote
      // connector server is bound to
      MBeanServerConnection connection = connector.getMBeanServerConnection();

      // Call the server side as if it is a local MBeanServer
      ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
      Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName, MBeanServerDelegateMBean.class, true);
      MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean)proxy;

      // The magic of JDK 1.3 dynamic proxy and JSR 160:
      // delegate.getImplementationVendor() is actually a remote JMX call,
      // but it looks like a local, old-style, java call.
      System.out.println(delegate.getImplementationVendor() + " is cool !");
   }
}
