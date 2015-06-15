/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.mbeans.dynamic;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;

/**
 * Purpose of this example is to show how to use DynamicMBean in general, with the help
 * of the {@link mx4j.AbstractDynamicMBean AbstractDynamicMBean} class, see
 * {@link DynamicService}.
 * It also shows usage of the Monitor classes.
 *
 * @version $Revision: 1.4 $
 */
public class DynamicMBeanExample
{
   public static void main(String[] args) throws Exception
   {
      // Let's create the MBeanServer
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      // Let's create a dynamic MBean and register it
      DynamicService serviceMBean = new DynamicService();
      ObjectName serviceName = new ObjectName("examples", "mbean", "dynamic");
      server.registerMBean(serviceMBean, serviceName);

      // Now let's register a Monitor
      // We would like to know if we have peaks in activity, so we can use JMX's
      // GaugeMonitor
      GaugeMonitor monitorMBean = new GaugeMonitor();
      ObjectName monitorName = new ObjectName("examples", "monitor", "gauge");
      server.registerMBean(monitorMBean, monitorName);

      // Setup the monitor: we want to be notified if we have too many clients or too less
      monitorMBean.setThresholds(new Integer(8), new Integer(4));
      // Setup the monitor: we want to know if a threshold is exceeded
      monitorMBean.setNotifyHigh(true);
      monitorMBean.setNotifyLow(true);
      // Setup the monitor: we're interested in absolute values of the number of clients
      monitorMBean.setDifferenceMode(false);
      // Setup the monitor: link to the service MBean
      monitorMBean.addObservedObject(serviceName);
      monitorMBean.setObservedAttribute("ConcurrentClients");
      // Setup the monitor: a short granularity period
      monitorMBean.setGranularityPeriod(50L);
      // Setup the monitor: register a listener
      monitorMBean.addNotificationListener(new NotificationListener()
      {
         public void handleNotification(Notification notification, Object handback)
         {
            System.out.println(notification);
         }
      }, null, null);
      // Setup the monitor: start it
      monitorMBean.start();

      // Now start also the service
      serviceMBean.start();
   }
}
