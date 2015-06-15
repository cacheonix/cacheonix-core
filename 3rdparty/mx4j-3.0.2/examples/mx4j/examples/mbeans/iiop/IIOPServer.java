/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.mbeans.iiop;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

/**
 * This example starts a RMI over IIOP server that listens for RMI clients to connect
 * and exposes its functionalities via JMX.
 * To be run, be sure to have started the tnameserv or the orbd utility on the port
 * specified by {@link Hello#IIOP_JNDI_NAME}, with the following command:
 * <pre>
 * $JAVA_HOME/bin/orbd -ORBInitialPort 1900
 * </pre>
 *
 * @version $Revision: 1.5 $
 */
public class IIOPServer
{
   public static void main(String[] args) throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      ObjectName name = new ObjectName("examples:type=iiop-remote");
      HelloImpl remote = new HelloImpl();
      server.registerMBean(remote, name);

      HelloImplMBean managed = (HelloImplMBean)MBeanServerInvocationHandler.newProxyInstance(server, name, HelloImplMBean.class, false);
      managed.start();
   }
}
