/*
 * Created on July 12, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2008 the original author or authors.
 */
package org.springmodules.cache.provider.cacheonix;

import org.springframework.util.ObjectUtils;
import org.springmodules.cache.CachingModel;
import org.springframework.util.StringUtils;
import org.springmodules.util.Objects;

/**
 * 
 */
public class CacheonixCachingModel implements CachingModel {

   private static final long serialVersionUID = 3762529035888112945L;
   private String cacheName = null;


   /**
    * 
    */
   public CacheonixCachingModel() {
   }


   /**
    * @param cacheName
    */
   public CacheonixCachingModel(final String cacheName) {
      this.cacheName = cacheName;
   }


   /**
    * @return the name of the Cacheonix cache.
    */
   public final String getCacheName() {
      return cacheName;
   }


   /**
    * Sets the name of the Cacheonix cache.
    *
    * @param newName the new name of the Cacheonix cache
    */
   public final void setCacheName(final String newName) {
      cacheName = newName;
   }


   /**
    * @see Object#equals(Object)
    */
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof CacheonixCachingModel)) {
         return false;
      }

      final CacheonixCachingModel cachingModel = (CacheonixCachingModel)obj;

      return ObjectUtils.nullSafeEquals(cacheName, cachingModel.cacheName);

   }


   /**
    * @see Object#hashCode()
    */
   public int hashCode() {
      final int multiplier = 31;
      int hash = 7;
      hash = multiplier * hash + Objects.nullSafeHashCode(cacheName);
      return hash;
   }


   /**
    * @see Object#toString()
    */
   public String toString() {
      return Objects.identityToString(this)
              .append("[cacheName=")
              .append(StringUtils.quote(cacheName))
              .append("]")
              .toString();
   }
}