/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.adaptor.http;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import mx4j.tools.adaptor.ssl.SSLAdaptorServerSocketFactoryMBean;

/**
 * Example as how to use the HttpAdaptor and the XSLTProcessor with
 * SSL support. This example assumes that you have created a keystore
 * as described in the documentation.
 *
 * @version $Revision: 1.4 $
 */
public class SSLHttpAdaptor
{
   private int port = 8080;

   private String host = "localhost";

   private String path = null, pathInJar = null;

   private static interface TestClassMBean
   {
      public String getStr();

      public Double getDouble();

      public boolean isTrue();

      public void setStr(String str);

      public Boolean aMethod(String string);

      public void anotherMethod(String string, int test);
   }

   public static class TestClass extends NotificationBroadcasterSupport implements TestClassMBean
   {
      private String str;

      public TestClass(String str)
      {
         this.str = str;
      }

      public String getStr()
      {
         return str;
      }

      public void setStr(String str)
      {
         this.str = str;
      }

      public Double getDouble()
      {
         return new Double(0);
      }

      public boolean isTrue()
      {
         return true;
      }

      public Boolean aMethod(String string)
      {
         return new Boolean(string.equals("true"));
      }

      public void anotherMethod(String string, int test)
      {
         this.str = string;
      }

      public MBeanNotificationInfo[] getNotificationInfo()
      {
         MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[1];
         notifications[0] = new MBeanNotificationInfo(new String[]{"test1"
                                                                   , "test2"}, "name", "test");
         return notifications;
      }

   }

   /**
    * Creates a new SSLHttpAdaptor example. You can optionally pass the host/port as
    * java -cp CLASSPATH adaptor.http.HttpAdaptor localhost 8080 path
    */
   public SSLHttpAdaptor(String args[])
   {
      if (args.length > 0)
      {
         host = args[0];
      }
      if (args.length > 1)
      {
         port = Integer.parseInt(args[1]);
      }
      if (args.length > 2)
      {
         path = args[2];
      }
      if (args.length > 3)
      {
         pathInJar = args[3];
      }
   }

   /**
    * Starts the http server
    */
   public void start() throws JMException
   {
      // creates new server
      MBeanServer server = MBeanServerFactory.createMBeanServer("test");
      ObjectName serverName = new ObjectName("Http:name=HttpAdaptor");
      server.createMBean("mx4j.tools.adaptor.http.HttpAdaptor", serverName, null);
      // set attributes
      if (port > 0)
      {
         server.setAttribute(serverName, new Attribute("Port", new Integer(port)));
      }
      else
      {
         System.out.println("Incorrect port value " + port);
      }
      if (host != null)
      {
         server.setAttribute(serverName, new Attribute("Host", host));
      }
      else
      {
         System.out.println("Incorrect null hostname");
      }
      // set the XSLTProcessor. If you want to use pure XML comment this out
      ObjectName processorName = new ObjectName("Http:name=XSLTProcessor");
      server.createMBean("mx4j.tools.adaptor.http.XSLTProcessor", processorName, null);
      if (path != null)
      {
         server.setAttribute(processorName, new Attribute("File", path));
      }
      server.setAttribute(processorName, new Attribute("UseCache", new Boolean(false)));
      if (pathInJar != null)
      {
         server.setAttribute(processorName, new Attribute("PathInJar", pathInJar));
      }
      server.setAttribute(serverName, new Attribute("ProcessorName", processorName));

      // add a couple of MBeans
      TestClass test1 = new TestClass("t1");
      TestClass test2 = new TestClass("t2");
      server.registerMBean(test1, new ObjectName("Test:name=test1"));
      server.registerMBean(test2, new ObjectName("Test:name=test2"));

      // add user names
      //server.invoke(serverName, "addAuthorization", new Object[] {"mx4j", "mx4j"}, new String[] {"java.lang.String", "java.lang.String"});

      // use basic authentication
      //server.setAttribute(serverName, new Attribute("AuthenticationMethod", "basic"));

      // SSL support
      ObjectName sslFactory = new ObjectName("Adaptor:service=SSLServerSocketFactory");
      server.createMBean("mx4j.tools.adaptor.ssl.SSLAdaptorServerSocketFactory", sslFactory, null);

      SSLAdaptorServerSocketFactoryMBean factory = (SSLAdaptorServerSocketFactoryMBean)MBeanServerInvocationHandler.newProxyInstance(server, sslFactory, SSLAdaptorServerSocketFactoryMBean.class, false);
      // Customize the values below
      factory.setKeyStoreName("certs");
      factory.setKeyStorePassword("mx4j");

      server.setAttribute(serverName, new Attribute("SocketFactoryName", sslFactory));

      // starts the server
      server.invoke(serverName, "start", null, null);
   }

   public static void main(String[] str) throws JMException
   {
      SSLHttpAdaptor adaptor = new SSLHttpAdaptor(str);
      adaptor.start();
   }
}

