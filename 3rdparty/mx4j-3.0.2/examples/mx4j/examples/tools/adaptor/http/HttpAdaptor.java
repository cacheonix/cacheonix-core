/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.adaptor.http;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import mx4j.tools.stats.TimedStatisticsRecorder;

/**
 * Example as how to use the HttpAdaptor and the XSLTProcessor
 *
 * @version $Revision: 1.4 $
 */
public class HttpAdaptor
{
   private int port = 8080;

   private String host = "localhost";

   private String path = null, pathInJar = null;

   public static interface TestClassMBean
   {
      public URL getURL();

      public void setURL(URL url);

      public String getStr();

      public String[] getStrArray();

      public Double getDouble();

      public boolean isTrue();

      public void setStr(String str);

      public void setStrArray(String[] str);

      public Boolean aMethod(String string);

      public void anotherMethod(String string, int test);

      public Map getaMap();

      public List getaList();

      public Date getDate();

      public void setDate(Date date);

      public BigInteger getBigInteger();

      public void setBigInteger(BigInteger integer);

      public BigDecimal getBigDecimal();

      public void setBigDecimal(BigDecimal decimal);

      public CompositeData getCompositeData();

      public void setCompositeData(CompositeData composite);
   }

   public static class TestClass extends NotificationBroadcasterSupport implements TestClassMBean
   {
      private String[] strArray = new String[]{"first", "second"};
      private String str;
      private URL url;
      private List list = new ArrayList();
      private Map map = new HashMap();
      private Date date = new Date();
      private BigInteger bigInteger = new BigInteger("123456789101112131415");
      private BigDecimal bigDecimal = new BigDecimal("123456789101112131415.987654321");
      private CompositeData compositeData = null;

      public TestClass(String str, URL url)
      {
         this.str = str;
         this.url = url;
         list.add("a");
         list.add("b");
         list.add("c");
         map.put("1", "a");
         map.put("2", "b");
         map.put("3", "c");
         try
         {
            CompositeType type = new CompositeType("My type",
                                                   "My type",
                                                   new String[]{"item1", "item2"},
                                                   new String[]{"item1", "item2"},
                                                   new OpenType[]{SimpleType.STRING, SimpleType.STRING});
            compositeData = new CompositeDataSupport(type, new String[]{"item1", "item2"}, new Object[]{"item value 1", "item value 2"});
         }
         catch (OpenDataException e)
         {
            e.printStackTrace();
         }

      }

      public void setCompositeData(CompositeData compositeData)
      {
         this.compositeData = compositeData;
      }

      public CompositeData getCompositeData()
      {
         return compositeData;
      }

      public void setBigInteger(BigInteger bigInteger)
      {
         this.bigInteger = bigInteger;
      }

      public BigInteger getBigInteger()
      {
         return bigInteger;
      }

      public void setBigDecimal(BigDecimal bigDecimal)
      {
         this.bigDecimal = bigDecimal;
      }

      public BigDecimal getBigDecimal()
      {
         return bigDecimal;
      }

      public void setDate(Date date)
      {
         this.date = date;
      }

      public Date getDate()
      {
         return date;
      }

      public void setURL(URL url)
      {
         this.url = url;
      }

      public URL getURL()
      {
         return url;
      }

      public String getStr()
      {
         return str;
      }

      public void setStr(String str)
      {
         this.str = str;
      }

      public String[] getStrArray()
      {
         return strArray;
      }

      public void setStrArray(String[] strArray)
      {
         this.strArray = strArray;
      }

      public Double getDouble()
      {
         return new Double(100 * Math.random());
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

      public Map getaMap()
      {
         return map;
      }

      public List getaList()
      {
         return list;
      }

   }

   /**
    * Creates a new HttpAdaptor example. You can optionally pass the host/port as
    * java -cp CLASSPATH adaptor.http.HttpAdaptor localhost 8080 path
    */
   public HttpAdaptor(String args[])
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
   public void start() throws JMException, MalformedURLException
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
      TestClass test1 = new TestClass("t1", new URL("http://mx4j.sourceforge.net"));
      TestClass test2 = new TestClass("t1", new URL("http://www.sourceforge.net/projects/mx4j"));
      server.registerMBean(test1, new ObjectName("Test:name=test1"));
      server.registerMBean(test2, new ObjectName("Test:name=test2"));

      // add a stats MBean
      TimedStatisticsRecorder recoder = new TimedStatisticsRecorder();
      recoder.setObservedObject(new ObjectName("Test:name=test1"));
      recoder.setObservedAttribute("Double");
      server.registerMBean(recoder, new ObjectName("Test:name=test1recorder"));
      server.invoke(new ObjectName("Test:name=test1recorder"), "start", null, null);

      // add a couple of MBeans

      // add user names
      server.invoke(serverName, "addAuthorization", new Object[]{"mx4j", "mx4j"}, new String[]{"java.lang.String", "java.lang.String"});

      // use basic authentication
      //server.setAttribute(serverName, new Attribute("AuthenticationMethod", "basic"));

      // starts the server
      server.invoke(serverName, "start", null, null);
   }

   public static void main(String[] str) throws Exception
   {
      HttpAdaptor adaptor = new HttpAdaptor(str);
      adaptor.start();
   }
}
