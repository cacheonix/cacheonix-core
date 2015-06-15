/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.remote.interception;

import java.io.ObjectInputStream;
import java.security.AccessController;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.remote.MBeanServerForwarder;
import javax.security.auth.Subject;

/**
 * This class tracks the Subject of the current invocation, and prints it to System.out.
 * It should be better implemented as JDK 1.3 dynamic proxy, but this is left as a simple
 * exercise to the reader ;)
 *
 * @version $Revision: 1.3 $
 */
public class SubjectTrackingMBeanServer implements MBeanServerForwarder
{
   private MBeanServer server;

   public synchronized MBeanServer getMBeanServer()
   {
      return server;
   }

   public synchronized void setMBeanServer(MBeanServer server) throws IllegalArgumentException
   {
      if (server == null) throw new IllegalArgumentException("Cannot forward to a null MBeanServer");
      this.server = server;
   }

   private void trackSubject()
   {
      Subject subject = Subject.getSubject(AccessController.getContext());
      System.out.println("Subject = " + subject);
   }

   public void addNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException
   {
      trackSubject();
      getMBeanServer().addNotificationListener(observed, listener, filter, handback);
   }

   public void addNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException
   {
      trackSubject();
      getMBeanServer().addNotificationListener(observed, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName observed, ObjectName listener)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      trackSubject();
      getMBeanServer().removeNotificationListener(observed, listener);
   }

   public void removeNotificationListener(ObjectName observed, NotificationListener listener)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      trackSubject();
      getMBeanServer().removeNotificationListener(observed, listener);
   }

   public void removeNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      trackSubject();
      getMBeanServer().removeNotificationListener(observed, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      trackSubject();
      getMBeanServer().removeNotificationListener(observed, listener, filter, handback);
   }

   public MBeanInfo getMBeanInfo(ObjectName objectName)
           throws InstanceNotFoundException, IntrospectionException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().getMBeanInfo(objectName);
   }

   public boolean isInstanceOf(ObjectName objectName, String className)
           throws InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().isInstanceOf(objectName, className);
   }

   public String[] getDomains()
   {
      trackSubject();
      return getMBeanServer().getDomains();
   }

   public String getDefaultDomain()
   {
      trackSubject();
      return getMBeanServer().getDefaultDomain();
   }

   public ObjectInstance createMBean(String className, ObjectName objectName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      trackSubject();
      return getMBeanServer().createMBean(className, objectName);
   }

   public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().createMBean(className, objectName, loaderName);
   }

   public ObjectInstance createMBean(String className, ObjectName objectName, Object[] args, String[] parameters)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      trackSubject();
      return getMBeanServer().createMBean(className, objectName, args, parameters);
   }

   public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName, Object[] args, String[] parameters)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().createMBean(className, objectName, loaderName, args, parameters);
   }

   public void unregisterMBean(ObjectName objectName)
           throws InstanceNotFoundException, MBeanRegistrationException
   {
      trackSubject();
      getMBeanServer().unregisterMBean(objectName);
   }

   public Object getAttribute(ObjectName objectName, String attribute)
           throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().getAttribute(objectName, attribute);
   }

   public void setAttribute(ObjectName objectName, Attribute attribute)
           throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      trackSubject();
      getMBeanServer().setAttribute(objectName, attribute);
   }

   public AttributeList getAttributes(ObjectName objectName, String[] attributes)
           throws InstanceNotFoundException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().getAttributes(objectName, attributes);
   }

   public AttributeList setAttributes(ObjectName objectName, AttributeList attributes)
           throws InstanceNotFoundException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().setAttributes(objectName, attributes);
   }

   public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] parameters)
           throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().invoke(objectName, methodName, args, parameters);
   }

   public Integer getMBeanCount()
   {
      trackSubject();
      return getMBeanServer().getMBeanCount();
   }

   public boolean isRegistered(ObjectName objectname)
   {
      trackSubject();
      return getMBeanServer().isRegistered(objectname);
   }

   public ObjectInstance getObjectInstance(ObjectName objectName)
           throws InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().getObjectInstance(objectName);
   }

   public Set queryMBeans(ObjectName patternName, QueryExp filter)
   {
      trackSubject();
      return getMBeanServer().queryMBeans(patternName, filter);
   }

   public Set queryNames(ObjectName patternName, QueryExp filter)
   {
      trackSubject();
      return getMBeanServer().queryNames(patternName, filter);
   }

   public Object instantiate(String className)
           throws ReflectionException, MBeanException
   {
      trackSubject();
      return getMBeanServer().instantiate(className);
   }

   public Object instantiate(String className, ObjectName loaderName)
           throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().instantiate(className, loaderName);
   }

   public Object instantiate(String className, Object[] args, String[] parameters)
           throws ReflectionException, MBeanException
   {
      trackSubject();
      return getMBeanServer().instantiate(className, args, parameters);
   }

   public Object instantiate(String className, ObjectName loaderName, Object[] args, String[] parameters)
           throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().instantiate(className, loaderName, args, parameters);
   }

   public ObjectInstance registerMBean(Object mbean, ObjectName objectName)
           throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
   {
      trackSubject();
      return registerMBean(mbean, objectName);
   }

   public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] bytes)
           throws InstanceNotFoundException, OperationsException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().deserialize(className, loaderName, bytes);
   }

   public ObjectInputStream deserialize(String className, byte[] bytes)
           throws OperationsException, ReflectionException
   {
      trackSubject();
      return getMBeanServer().deserialize(className, bytes);
   }

   public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes)
           throws InstanceNotFoundException, OperationsException
   {
      trackSubject();
      return getMBeanServer().deserialize(objectName, bytes);
   }

   public ClassLoader getClassLoaderFor(ObjectName mbeanName)
           throws InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().getClassLoaderFor(mbeanName);
   }

   public ClassLoader getClassLoader(ObjectName loaderName)
           throws InstanceNotFoundException
   {
      trackSubject();
      return getMBeanServer().getClassLoader(loaderName);
   }

   public ClassLoaderRepository getClassLoaderRepository()
   {
      trackSubject();
      return getMBeanServer().getClassLoaderRepository();
   }
}
