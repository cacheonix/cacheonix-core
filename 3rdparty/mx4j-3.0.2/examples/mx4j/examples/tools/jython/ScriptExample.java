/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.jython;

import java.net.MalformedURLException;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Example as how to use the Jython MBean. An MBean will be created and some scripts executed
 *
 * @version $Revision: 1.3 $
 */
public class ScriptExample
{
   public ScriptExample()
   {
   }

   /**
    * Executes the script
    */
   public void start() throws JMException, MalformedURLException
   {
      // creates new server
      MBeanServer server = MBeanServerFactory.createMBeanServer("Script");
      ObjectName scriptingName = new ObjectName("Test:name=script");
      server.createMBean("mx4j.tools.jython.JythonRunner", scriptingName, null);

      // Sample. Starts all monitors
      server.setAttribute(scriptingName, new Attribute("Script", "[proxy(name).start() for name in server.queryNames(None, None) if server.isInstanceOf(name, 'javax.management.monitor.Monitor')]"));
      server.invoke(scriptingName, "runScript", null, null);

      // Sample. Stops all timers
      server.setAttribute(scriptingName, new Attribute("Script", "[proxy(name).start() for name in server.queryNames(None, None) if server.isInstanceOf(name, 'javax.management.timer.Timer')]"));
      server.invoke(scriptingName, "runScript", null, null);

      // Sample. prints all MBeans which description is not null
      server.setAttribute(scriptingName, new Attribute("Script", "desc = [server.getMBeanInfo(name).description for name in server.queryNames(None, None)]\nprint filter(lambda x:x, desc)"));
      server.invoke(scriptingName, "runScript", null, null);
   }

   public static void main(String[] str) throws JMException, MalformedURLException
   {
      ScriptExample example = new ScriptExample();
      example.start();
   }
}

