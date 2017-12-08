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

// Contributors:  Georg Lundesgaard

package org.cacheonix.impl.util.logging.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.cacheonix.impl.util.logging.Appender;
import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.Priority;
import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.cacheonix.impl.util.logging.helpers.OptionConverter;
import org.cacheonix.impl.util.logging.spi.OptionHandler;

/**
 * General purpose Object property setter. Clients repeatedly invokes {@link #setProperty setProperty(name,value)} in
 * order to invoke setters on the Object specified in the constructor. This class relies on the JavaBeans {@link
 * Introspector} to analyze the given Object Class using reflection.
 * <p/>
 * <p>Usage:
 * <pre>
 * PropertySetter ps = new PropertySetter(anObject);
 * ps.set("name", "Joe");
 * ps.set("age", "32");
 * ps.set("isMale", "true");
 * </pre>
 * will cause the invocations anObject.setName("Joe"), anObject.setAge(32), and setMale(true) if such methods exist with
 * those signatures. Otherwise an {@link IntrospectionException} are thrown.
 *
 * @author Anders Kristensen
 * @since 1.1
 */
public final class PropertySetter {

   private static final PropertyDescriptor[] ZERO_LENGTH_PROPERTY_DESCRIPTOR_ARRAY = new PropertyDescriptor[0];

   private final Object obj;
   private PropertyDescriptor[] props = null;


   /**
    * Create a new PropertySetter for the specified Object. This is done in prepartion for invoking {@link #setProperty}
    * one or more times.
    *
    * @param obj the object for which to set properties
    */
   public PropertySetter(final Object obj) {
      this.obj = obj;
   }


   /**
    * Uses JavaBeans {@link Introspector} to computer setters of object to be configured.
    */
   private final void introspect() {
      try {
         final BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
         props = bi.getPropertyDescriptors();
      } catch (final IntrospectionException ex) {
         LogLog.error("Failed to introspect " + obj + ": " + ex.getMessage());
         props = ZERO_LENGTH_PROPERTY_DESCRIPTOR_ARRAY;
      }
   }


   /**
    * Set the properties of an object passed as a parameter in one go. The <code>properties</code> are parsed relative
    * to a <code>prefix</code>.
    *
    * @param obj        The object to configure.
    * @param properties A java.util.Properties containing keys and values.
    * @param prefix     Only keys having the specified prefix will be set.
    */
   public
   static void setProperties(final Object obj, final Properties properties, final String prefix) {
      new PropertySetter(obj).setProperties(properties, prefix);
   }


   /**
    * Set the properites for the object that match the <code>prefix</code> passed as parameter.
    */
   public final void setProperties(final Properties properties, final String prefix) {
      final int len = prefix.length();

      for (final Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
         String key = (String) e.nextElement();

         // handle only properties that start with the desired frefix.
         if (key.startsWith(prefix)) {

            // ignore key if it contains dots after the prefix
            if (key.indexOf((int) '.', len + 1) > 0) {
               //System.err.println("----------Ignoring---["+key
               //	     +"], prefix=["+prefix+"].");
               continue;
            }

            final String value = OptionConverter.findAndSubst(key, properties);
            key = key.substring(len);
            if ("layout".equals(key) && obj instanceof Appender) {
               continue;
            }
            setProperty(key, value);
         }
      }
      activate();
   }


   /**
    * Set a property on this PropertySetter's Object. If successful, this method will invoke a setter method on the
    * underlying Object. The setter is the one for the specified property name and the value is determined partly from
    * the setter argument type and partly from the value specified in the call to this method.
    * <p/>
    * <p>If the setter expects a String no conversion is necessary. If it expects an int, then an attempt is made to
    * convert 'value' to an int using new Integer(value). If the setter expects a boolean, the conversion is by new
    * Boolean(value).
    *
    * @param name  name of the property
    * @param value String value of the property
    */
   public final void setProperty(String name, final String value) {
      if (value == null) {
         return;
      }

      name = Introspector.decapitalize(name);
      final PropertyDescriptor prop = getPropertyDescriptor(name);

      //LogLog.debug("---------Key: "+name+", type="+prop.getPropertyType());

      if (prop == null) {
         LogLog.warn("No such property [" + name + "] in " +
                 obj.getClass().getName() + '.');
      } else {
         try {
            setProperty(prop, name, value);
         } catch (final PropertySetterException ex) {
            LogLog.warn("Failed to set property [" + name +
                    "] to value \"" + value + "\". ", ex.rootCause);
         }
      }
   }


   /**
    * Set the named property given a {@link PropertyDescriptor}.
    *
    * @param prop  A PropertyDescriptor describing the characteristics of the property to set.
    * @param name  The named of the property to set.
    * @param value The value of the property.
    */
   public final void setProperty(final PropertyDescriptor prop, final String name, final String value)
           throws PropertySetterException {
      final Method setter = prop.getWriteMethod();
      if (setter == null) {
         throw new PropertySetterException("No setter for property [" + name + "].");
      }
      final Class[] paramTypes = setter.getParameterTypes();
      if (paramTypes.length != 1) {
         throw new PropertySetterException("#params for setter != 1");
      }

      final Object arg;
      try {
         arg = convertArg(value, paramTypes[0]);
      } catch (final Throwable t) {
         throw new PropertySetterException("Conversion to type [" + paramTypes[0] +
                 "] failed. Reason: " + t);
      }
      if (arg == null) {
         throw new PropertySetterException(
                 "Conversion to type [" + paramTypes[0] + "] failed.");
      }
      LogLog.debug("Setting property [" + name + "] to [" + arg + "].");
      try {
         setter.invoke(obj, arg);
      } catch (final Exception ex) {
         throw new PropertySetterException(ex);
      }
   }


   /**
    * Convert <code>val</code> a String parameter to an object of a given type.
    */
   private final Object convertArg(final String val, final Class type) {
      if (val == null) {
         return null;
      }

      final String v = val.trim();
      if (String.class.isAssignableFrom(type)) {
         return val;
      }

      if (Integer.TYPE.isAssignableFrom(type)) {
         return Integer.valueOf(v);
      }

      if (Long.TYPE.isAssignableFrom(type)) {
         return Long.valueOf(v);
      }

      if (Boolean.TYPE.isAssignableFrom(type)) {
         if ("true".equalsIgnoreCase(v)) {
            return Boolean.TRUE;
         } else if ("false".equalsIgnoreCase(v)) {
            return Boolean.FALSE;
         }
      } else if (Priority.class.isAssignableFrom(type)) {
         return OptionConverter.toLevel(v, Level.DEBUG);
      }
      return null;
   }


   private final PropertyDescriptor getPropertyDescriptor(final String name) {
      if (props == null) {
         introspect();
      }

      for (final PropertyDescriptor prop : props) {
         if (name.equals(prop.getName())) {
            return prop;
         }
      }
      return null;
   }


   public final void activate() {
      if (obj instanceof OptionHandler) {
         ((OptionHandler) obj).activateOptions();
      }
   }
}
