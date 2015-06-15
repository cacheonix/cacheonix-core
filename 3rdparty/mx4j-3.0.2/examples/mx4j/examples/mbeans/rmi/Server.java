/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.rmi;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

/**
 * This example starts a RMI over IIOP server that listens for RMI clients to connect
 * and exposes its functionalities via JMX.
 * To be run, be sure to have started the rmiregistry utility on the port
 * specified by {@link MyRemoteService#JNDI_NAME}, with the following command:
 * <pre>
 * $JAVA_HOME/bin/rmiregistry 1099
 * </pre>
 *
 * @version $Revision: 1.4 $
 */
public class Server
{
   public static void main(String[] args) throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      ObjectName name = new ObjectName("examples:type=remote");
      MyRemoteServiceObject remote = new MyRemoteServiceObject();
      server.registerMBean(remote, name);

      MyRemoteServiceObjectMBean managed = (MyRemoteServiceObjectMBean)MBeanServerInvocationHandler.newProxyInstance(server, name, MyRemoteServiceObjectMBean.class, false);
      managed.start();
   }
}
