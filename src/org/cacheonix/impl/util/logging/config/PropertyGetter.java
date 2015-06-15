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

package org.cacheonix.impl.util.logging.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.cacheonix.impl.util.logging.Priority;
import org.cacheonix.impl.util.logging.helpers.LogLog;


/**
 * Used for inferring configuration information for a log4j's component.
 *
 * @author Anders Kristensen
 */
public final class PropertyGetter {

   protected static final Object[] NULL_ARG = {};

   protected Object obj;

   protected PropertyDescriptor[] props;

   public interface PropertyCallback {

      void foundProperty(Object obj, String prefix, String name, Object value);
   }


   /**
    * Create a new PropertyGetter for the specified Object. This is done in prepartion for invoking {@link
    * #getProperties(PropertyCallback, String)} one or more times.
    *
    * @param obj the object for which to set properties
    */
   public PropertyGetter(final Object obj) throws IntrospectionException {

      final BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
      props = bi.getPropertyDescriptors();
      this.obj = obj;
   }


   public
   static void getProperties(final Object obj, final PropertyCallback callback,
                             final String prefix) {

      try {
         new PropertyGetter(obj).getProperties(callback, prefix);
      } catch (final IntrospectionException ex) {
         LogLog.error("Failed to introspect object " + obj, ex);
      }
   }


   public final void getProperties(final PropertyCallback callback, final String prefix) {

      for (int i = 0; i < props.length; i++) {
         final Method getter = props[i].getReadMethod();
         if (getter == null) {
            continue;
         }
         if (!isHandledType(getter.getReturnType())) {
            //System.err.println("Ignoring " + props[i].getName() +" " + getter.getReturnType());
            continue;
         }
         final String name = props[i].getName();
         try {
            final Object result = getter.invoke(obj, NULL_ARG);
            //System.err.println("PROP " + name +": " + result);
            if (result != null) {
               callback.foundProperty(obj, prefix, name, result);
            }
         } catch (final Exception ex) {
            LogLog.warn("Failed to get value of property " + name);
         }
      }
   }


   protected final boolean isHandledType(final Class type) {

      return String.class.isAssignableFrom(type) ||
              Integer.TYPE.isAssignableFrom(type) ||
              Long.TYPE.isAssignableFrom(type) ||
              Boolean.TYPE.isAssignableFrom(type) ||
              Priority.class.isAssignableFrom(type);
   }
}
