/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cacheonix.impl.util.logging.jmx;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Vector;
import javax.management.*;

import org.cacheonix.impl.util.logging.Appender;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.Logger;
import org.cacheonix.impl.util.logging.helpers.OptionConverter;

public class LoggerDynamicMBean extends AbstractDynamicMBean
        implements NotificationListener {

   private final MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
   private final MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];

   private final Vector dAttributes = new Vector();
   private final String dClassName = this.getClass().getName();

   private final String dDescription =
           "This MBean acts as a management facade for a org.cacheonix.impl.util.logging.Logger instance.";

   // This Logger instance is for logging.
   private static final Logger cat = Logger.getLogger(LoggerDynamicMBean.class);

   // We wrap this Logger instance.
   private final Logger logger;

   public LoggerDynamicMBean(final Logger logger) {
      this.logger = logger;
      buildDynamicMBeanInfo();
   }

   public void handleNotification(final Notification notification, final Object handback) {
      cat.debug("Received notification: " + notification.getType());
      registerAppenderMBean((Appender)notification.getUserData());


   }

   private void buildDynamicMBeanInfo() {
      final Constructor[] constructors = this.getClass().getConstructors();
      dConstructors[0] = new MBeanConstructorInfo(
              "HierarchyDynamicMBean(): Constructs a HierarchyDynamicMBean instance",
              constructors[0]);

      dAttributes.add(new MBeanAttributeInfo("name",
              "java.lang.String",
              "The name of this Logger.",
              true,
              false,
              false));

      dAttributes.add(new MBeanAttributeInfo("priority",
              "java.lang.String",
              "The priority of this logger.",
              true,
              true,
              false));


      final MBeanParameterInfo[] params = new MBeanParameterInfo[2];
      params[0] = new MBeanParameterInfo("class name", "java.lang.String",
              "add an appender to this logger");
      params[1] = new MBeanParameterInfo("appender name", "java.lang.String",
              "name of the appender");

      dOperations[0] = new MBeanOperationInfo("addAppender",
              "addAppender(): add an appender",
              params,
              "void",
              MBeanOperationInfo.ACTION);
   }

   protected Logger getLogger() {
      return logger;
   }


   /**
    * @noinspection UnnecessaryLocalVariable
    */
   public MBeanInfo getMBeanInfo() {
      //cat.debug("getMBeanInfo called.");

      final MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[dAttributes.size()];
      dAttributes.toArray(attribs);

      final MBeanInfo mb = new MBeanInfo(dClassName,
              dDescription,
              attribs,
              dConstructors,
              dOperations,
              new MBeanNotificationInfo[0]);
      //cat.debug("getMBeanInfo exit.");
      return mb;
   }

   public Object invoke(final String operationName, final Object[] params, final String[] signature)
           throws MBeanException,
           ReflectionException {

      if (operationName.equals("addAppender")) {
         addAppender((String)params[0], (String)params[1]);
         return "Hello world.";
      }

      return null;
   }


   public Object getAttribute(final String attributeName) throws AttributeNotFoundException,
           MBeanException,
           ReflectionException {

      // Check attributeName is not null to avoid NullPointerException later on
      if (attributeName == null) {
         throw new RuntimeOperationsException(new IllegalArgumentException(
                 "Attribute name cannot be null"),
                 "Cannot invoke a getter of " + dClassName + " with null attribute name");
      }

      // Check for a recognized attributeName and call the corresponding getter
      if (attributeName.equals("name")) {
         return logger.getName();
      } else if (attributeName.equals("priority")) {
         final Level l = logger.getLevel();
         if (l == null) {
            return null;
         } else {
            return l.toString();
         }
      } else if (attributeName.startsWith("appender=")) {
         try {
            return new ObjectName("log4j:" + attributeName);
         } catch (Exception e) {
            cat.error("Could not create ObjectName" + attributeName);
         }
      }


      // If attributeName has not been recognized throw an AttributeNotFoundException
      throw new AttributeNotFoundException("Cannot find " + attributeName + " attribute in " + dClassName);

   }


   void addAppender(final String appenderClass, final String appenderName) {
      cat.debug("addAppender called with " + appenderClass + ", " + appenderName);
      final Appender appender = (Appender)
              OptionConverter.instantiateByClassName(appenderClass,
                      Appender.class,
                      null);
      appender.setName(appenderName);
      logger.addAppender(appender);

      //appenderMBeanRegistration();

   }


   public void setAttribute(final Attribute attribute) throws AttributeNotFoundException,
           InvalidAttributeValueException,
           MBeanException,
           ReflectionException {

      // Check attribute is not null to avoid NullPointerException later on
      if (attribute == null) {
         throw new RuntimeOperationsException(
                 new IllegalArgumentException("Attribute cannot be null"),
                 "Cannot invoke a setter of " + dClassName +
                         " with null attribute");
      }
      final String name = attribute.getName();
      final Object value = attribute.getValue();

      if (name == null) {
         throw new RuntimeOperationsException(
                 new IllegalArgumentException("Attribute name cannot be null"),
                 "Cannot invoke the setter of " + dClassName +
                         " with null attribute name");
      }


      if (name.equals("priority")) {
         if (value instanceof String) {
            final String s = (String)value;
            Level p = logger.getLevel();
            if (s.equalsIgnoreCase("NULL")) {
               p = null;
            } else {
               p = OptionConverter.toLevel(s, p);
            }
            logger.setLevel(p);
         }
      } else {
         throw new AttributeNotFoundException("Attribute " + name + " not found in " + this.getClass().getName());
      }
   }

   void appenderMBeanRegistration() {
      final Enumeration enumeration = logger.getAllAppenders();
      while (enumeration.hasMoreElements()) {
         final Appender appender = (Appender)enumeration.nextElement();
         registerAppenderMBean(appender);
      }
   }

   void registerAppenderMBean(final Appender appender) {
      final String name = appender.getName();
      cat.debug("Adding AppenderMBean for appender named " + name);
      ObjectName objectName = null;
      try {
         final AppenderDynamicMBean appenderMBean = new AppenderDynamicMBean(appender);
         objectName = new ObjectName("log4j", "appender", name);
         if (!server.isRegistered(objectName)) {
            server.registerMBean(appenderMBean, objectName);
            dAttributes.add(new MBeanAttributeInfo("appender=" + name, "javax.management.ObjectName",
                    "The " + name + " appender.", true, true, false));
         }

      } catch (Exception e) {
         cat.error("Could not add appenderMBean for [" + name + "].", e);
      }
   }

   public void postRegister(final Boolean registrationDone) {
      appenderMBeanRegistration();
   }
}
