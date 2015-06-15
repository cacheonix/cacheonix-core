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
package org.cacheonix.impl.util.logging.spi;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * VectorWriter is an obsolete class provided only for binary compatibility with earlier versions of log4j and should
 * not be used.
 *
 * @deprecated
 */
final class VectorWriter extends PrintWriter {

   private final Vector v;


   /**
    * @deprecated
    */
   VectorWriter() {

      super(new NullWriter());
      v = new Vector(11);
   }


   public final void print(final Object o) {

      v.addElement(String.valueOf(o));
   }


   public final void print(final char[] chars) {

      v.addElement(new String(chars));
   }


   public final void print(final String s) {

      v.addElement(s);
   }


   public final void println(final Object o) {

      v.addElement(String.valueOf(o));
   }


   // JDK 1.1.x apprenly uses this form of println while in
   // printStackTrace()
   public final void println(final char[] chars) {

      v.addElement(new String(chars));
   }


   public final void println(final String s) {

      v.addElement(s);
   }


   public final void write(final char[] chars) {

      v.addElement(new String(chars));
   }


   public final void write(final char[] chars, final int off, final int len) {

      v.addElement(new String(chars, off, len));
   }


   public final void write(final String s, final int off, final int len) {

      v.addElement(s.substring(off, off + len));
   }


   public final void write(final String s) {

      v.addElement(s);
   }


   public String[] toStringArray() {

      final int len = v.size();
      final String[] sa = new String[len];
      for (int i = 0; i < len; i++) {
         sa[i] = (String) v.elementAt(i);
      }
      return sa;
   }

}

