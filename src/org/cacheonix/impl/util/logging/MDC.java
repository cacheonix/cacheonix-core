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

package org.cacheonix.impl.util.logging;

import java.util.Hashtable;

import org.cacheonix.impl.util.logging.helpers.Loader;
import org.cacheonix.impl.util.logging.helpers.ThreadLocalMap;

/**
 * The MDC class is similar to the {@link NDC} class except that it is based on a map instead of a stack. It provides
 * <em>mapped diagnostic contexts</em>. A <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is typically interleaved when a server
 * handles multiple clients near-simultaneously.
 * <p/>
 * <p><b><em>The MDC is managed on a per thread basis</em></b>. A child thread automatically inherits a <em>copy</em> of
 * the mapped diagnostic context of its parent.
 * <p/>
 * <p>The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC will always return empty values but otherwise will
 * not affect or harm your application.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2
 */
public final class MDC {

   static final MDC mdc = new MDC();

   static final int HT_SIZE = 7;

   final boolean java1;

   Object tlm = null;


   private MDC() {

      java1 = Loader.isJava1();
      if (!java1) {
         tlm = new ThreadLocalMap();
      }
   }


   /**
    * Put a context value (the <code>o</code> parameter) as identified with the <code>key</code> parameter into the
    * current thread's context map.
    * <p/>
    * <p>If the current thread does not have a context map it is created as a side effect.
    */
   public static void put(final String key, final Object o) {

      if (mdc != null) {
         mdc.put0(key, o);
      }
   }


   /**
    * Get the context identified by the <code>key</code> parameter.
    * <p/>
    * <p>This method has no side effects.
    */
   public static Object get(final String key) {

      if (mdc != null) {
         return mdc.get0(key);
      }
      return null;
   }


   /**
    * Remove the the context identified by the <code>key</code> parameter.
    */
   public static void remove(final String key) {

      if (mdc != null) {
         mdc.remove0(key);
      }
   }


   /**
    * Get the current thread's MDC as a hashtable. This method is intended to be used internally.
    */
   public static Hashtable getContext() {

      if (mdc != null) {
         return mdc.getContext0();
      } else {
         return null;
      }
   }


   /**
    * @noinspection UnnecessaryReturnStatement, UnnecessaryReturnStatement
    */
   private void put0(final String key, final Object o) {

      if (java1 || tlm == null) {
         return;
      } else {
         Hashtable ht = (Hashtable) ((ThreadLocal) tlm).get();
         if (ht == null) {
            ht = new Hashtable(HT_SIZE);
            ((ThreadLocal) tlm).set(ht);
         }
         ht.put(key, o);
      }
   }


   private Object get0(final String key) {

      if (java1 || tlm == null) {
         return null;
      } else {
         final Hashtable ht = (Hashtable) ((ThreadLocal) tlm).get();
         if (ht != null && key != null) {
            return ht.get(key);
         } else {
            return null;
         }
      }
   }


   private void remove0(final String key) {

      if (!java1 && tlm != null) {
         final Hashtable ht = (Hashtable) ((ThreadLocal) tlm).get();
         if (ht != null) {
            ht.remove(key);
         }
      }
   }


   private Hashtable getContext0() {

      if (java1 || tlm == null) {
         return null;
      } else {
         return (Hashtable) ((ThreadLocal) tlm).get();
      }
   }
}
