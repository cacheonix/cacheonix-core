/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.helloworld;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * JMX HelloWorld example. <p>
 * Instead of saying "Hello", this simple class shows how is possible to create services,
 * register them in the JMX Agent, and invoke methods on them <strong>without</strong>
 * having a reference to them. <br>
 * One can create a service that can reload its configuration at runtime, and be queried
 * on how many times the configuration is reloaded, and expose it as standard MBean. <br>
 * This class shows in code what is possible to do via a management interface: once the
 * service is registered, one can connect via (for example) the HTTP adaptor and
 * invoke methods on the MBean. <br>
 * The service (the MBean) can be registered in one host, while the system administrator
 * can connect to the HTTP adaptor from another host using a browser and ask the service
 * to reload its configuration, without stopping it nor being forced to login to the
 * remote host.
 *
 * @version $Revision: 1.3 $
 */
public class HelloWorldExample
{
   public static void main(String[] args) throws Exception
   {
      // Create an instance of MBeanServer
      MBeanServer server = MBeanServerFactory.createMBeanServer();

      // Create an ObjectName for the MBean
      ObjectName name = new ObjectName(":mbean=helloworld");

      // Create and register the MBean in the MBeanServer
      server.createMBean("mx4j.examples.mbeans.helloworld.HelloWorld", name, null);

      // Invoke a method on it
      server.invoke(name, "reloadConfiguration", new Object[0], new String[0]);

      // Invoke an attribute on it
      Integer times = (Integer)server.getAttribute(name, "HowManyTimes");

      System.out.println("The configuration was reloaded " + times + " times.");
   }
}
