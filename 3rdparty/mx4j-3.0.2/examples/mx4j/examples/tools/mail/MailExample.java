/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.mail;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Example as how to use the SMTP MBean. There is a monitor which looks for the Str property
 * on the TestMBean, when the value changes a notification is sent by the Monitor and a mail
 * is produced.
 * <p/>
 * Modify the values of the SMTP server for your needs
 *
 * @version $Revision: 1.3 $
 */
public class MailExample
{

   private static interface TestClassMBean
   {
      public String getStr();

      public void setStr(String str);
   }

   public static class TestClass implements TestClassMBean
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
   }

   public MailExample()
   {
   }

   /**
    * Starts the http server
    */
   public void start() throws JMException
   {
      // creates new server
      MBeanServer server = MBeanServerFactory.createMBeanServer("Mail");
      ObjectName beanName = new ObjectName("Test:name=test");
      server.registerMBean(new TestClass("original"), beanName);

      ObjectName monitorName = new ObjectName("Test:name=monitor");
      server.createMBean("javax.management.monitor.StringMonitor", monitorName, null);

      server.setAttribute(monitorName, new Attribute("ObservedObject", beanName));
      server.setAttribute(monitorName, new Attribute("ObservedAttribute", "Str"));
      server.setAttribute(monitorName, new Attribute("StringToCompare", "original"));
      server.setAttribute(monitorName, new Attribute("GranularityPeriod", new Integer(100)));
      server.setAttribute(monitorName, new Attribute("NotifyDiffer", Boolean.TRUE));

      server.invoke(monitorName, "start", null, null);

      ObjectName mailerName = new ObjectName("Test:name=mailer");
      server.createMBean("mx4j.tools.mail.SMTP", mailerName, null);

      // Sets attributes
      server.setAttribute(mailerName, new Attribute("ObservedObject", monitorName));
      server.setAttribute(mailerName, new Attribute("NotificationName", "jmx.monitor.string.differs"));
      server.setAttribute(mailerName, new Attribute("FromAddress", "monitor@someserver"));
      server.setAttribute(mailerName, new Attribute("FromName", "MX4J"));
      server.setAttribute(mailerName, new Attribute("ServerHost", "smpt-server"));
      server.setAttribute(mailerName, new Attribute("To", "nobody@nobody"));
      server.setAttribute(mailerName, new Attribute("Subject", "Notification on $date$ at $time$"));
      server.setAttribute(mailerName, new Attribute("Content", "Notification on $datetime$ sent by $objectname$ on $observed$ monitor and a notification $notification$\nNotice how $$$$ gets expanded to $$"));

      // this will trigger the monitor and the mailer (Wait for 10 secs app)
      server.setAttribute(beanName, new Attribute("Str", "something-else"));

   }

   public static void main(String[] str) throws JMException
   {
      MailExample example = new MailExample();
      example.start();
   }
}

