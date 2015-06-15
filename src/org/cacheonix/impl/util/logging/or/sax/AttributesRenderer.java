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

package org.cacheonix.impl.util.logging.or.sax;

import org.cacheonix.impl.util.logging.or.ObjectRenderer;
import org.xml.sax.Attributes;

/**
 * Render <code>org.xml.sax.Attributes</code> objects.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2
 */
public final class AttributesRenderer implements ObjectRenderer {


   /**
    * Render a {@link Attributes}.
    */
   public String doRender(final Object o) {

      if (o instanceof Attributes) {
         final StringBuilder sbuf = new StringBuilder(100);
         final Attributes a = (Attributes) o;
         final int len = a.getLength();
         boolean first = true;
         for (int i = 0; i < len; i++) {
            if (first) {
               first = false;
            } else {
               sbuf.append(", ");
            }
            sbuf.append(a.getQName(i));
            sbuf.append('=');
            sbuf.append(a.getValue(i));
         }
         return sbuf.toString();
      } else {
         return o.toString();
      }
   }
}

