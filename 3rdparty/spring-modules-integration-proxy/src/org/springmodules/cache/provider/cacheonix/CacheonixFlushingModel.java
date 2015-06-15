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

import java.util.Arrays;

import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractFlushingModel;
import org.springmodules.util.Objects;

public final class CacheonixFlushingModel extends AbstractFlushingModel {

   private static final long serialVersionUID = 7299844898815952890L;

   private String[] cacheNames = null;


   public CacheonixFlushingModel() {
   }


   public CacheonixFlushingModel(final String csvCacheNames) {
      setCacheNames(csvCacheNames);
   }


   public CacheonixFlushingModel(final String[] newCacheNames) {
      cacheNames = newCacheNames;
   }


   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof CacheonixFlushingModel)) {
         return false;
      }
      final CacheonixFlushingModel flushingModel = (CacheonixFlushingModel)obj;
      return Arrays.equals(cacheNames, flushingModel.cacheNames);
   }


   public String[] getCacheNames() {
       return cacheNames;
   }


   public int hashCode() {
      final int multiplier = 31;
      int hash = 7;
      hash = multiplier * hash + Objects.nullSafeHashCode(cacheNames);
      return hash;
   }


   public void setCacheNames(final String csvCacheNames) {
      String[] newCacheNames = null;
      if (csvCacheNames != null) {
         newCacheNames = StringUtils.commaDelimitedListToStringArray(csvCacheNames);
      }
      setCacheNames(newCacheNames);
   }


   public void setCacheNames(final String[] newCacheNames) {
      cacheNames = newCacheNames;
   }


   public String toString() {
       return Objects.identityToString(this)
       .append("[cacheNames=")
       .append(Objects.nullSafeToString(cacheNames))
       .append(", flushBeforeMethodExecution=")
       .append(flushBeforeMethodExecution())
       .append("]")
       .toString();
   }
}
