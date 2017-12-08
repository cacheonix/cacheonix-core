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
package org.cacheonix.impl.util.logging.lf5.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Resource encapsulates access to Resources via the Classloader.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public final class Resource {
   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------
   private String _name = null;

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   /**
    * Default, no argument constructor.
    */
   public Resource() {
   }


   /**
    * Construct a Resource given a name.
    *
    * @see #setName(String)
    */
   public Resource(final String name) {
      _name = name;
   }

   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------


   /**
    * Set the name of the resource. <p/> A resource is some data (images, audio, text, etc) that can be accessed by
    * class code in a way that is independent of the location of the code. </p> <p/> The name of a resource is a
    * "/"-separated path name that identifies the resource. </p>
    *
    * @see #getName()
    */
   public void setName(final String name) {
      _name = name;
   }


   /**
    * Get the name of the resource.  Set setName() for a description of a resource.
    *
    * @see #setName
    */
   public final String getName() {
      return _name;
   }


   /**
    * Get the InputStream for this Resource.  Uses the classloader from this Resource.
    *
    * @see #getInputStreamReader
    * @see ResourceUtils
    */
   public InputStream getInputStream() {

      return ResourceUtils.getResourceAsStream(this, this);
   }


   /**
    * Get the InputStreamReader for this Resource. Uses the classloader from this Resource.
    *
    * @see #getInputStream
    * @see ResourceUtils
    */
   public InputStreamReader getInputStreamReader() {
      final InputStream in = ResourceUtils.getResourceAsStream(this, this);

      if (in == null) {
         return null;
      }

      return new InputStreamReader(in);
   }


   /**
    * Get the URL of the Resource.  Uses the classloader from this Resource.
    *
    * @see ResourceUtils
    */
   public URL getURL() {
      return ResourceUtils.getResourceAsURL(this, this);
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces:
   //--------------------------------------------------------------------------

}






