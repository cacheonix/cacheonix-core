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

package org.cacheonix.impl.util.logging.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.cacheonix.impl.util.logging.helpers.LogLog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * An {@link EntityResolver} specifically designed to return <code>log4j.dtd</code> which is embedded within the log4j
 * jar file.
 *
 * @author Paul Austin
 */
public final class Log4jEntityResolver implements EntityResolver {

   private static final byte[] ZERO_LENGTH_BYTE_ARRAY = new byte[0];


   public final InputSource resolveEntity(final String publicId, final String systemId) {
      if (systemId.endsWith("log4j.dtd")) {
         final Class clazz = getClass();
         InputStream in = clazz.getResourceAsStream("/org/apache/log4j/xml/log4j.dtd");
         if (in == null) {
            //noinspection ObjectToString
            LogLog.warn("Could not find [log4j.dtd] using [" + clazz.getClassLoader()
                    + "] class loader, parsed without DTD.");
            in = new ByteArrayInputStream(ZERO_LENGTH_BYTE_ARRAY);
         }
         return new InputSource(in);
      } else {
         return null;
      }
   }
}
