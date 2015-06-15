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
import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;

public class CacheonixModelValidator extends AbstractCacheModelValidator {

   protected Class getCachingModelTargetClass() {
      return CacheonixCachingModel.class;
   }


   protected Class getFlushingModelTargetClass() {
      return CacheonixFlushingModel.class;
   }


   protected void validateCachingModelProperties(
     final Object cachingModel) throws InvalidCacheModelException {
      final CacheonixCachingModel model = (CacheonixCachingModel)cachingModel;
      if (!StringUtils.hasText(model.getCacheName())) {
         throw new InvalidCacheModelException("Cache name should not be empty");
      }
   }


   protected void validateFlushingModelProperties(
     final Object flushingModel) throws InvalidCacheModelException {
      final CacheonixFlushingModel model = (CacheonixFlushingModel)flushingModel;
      final String[] cacheNames = model.getCacheNames();

      if (ObjectUtils.isEmpty(cacheNames)) {
         throw new InvalidCacheModelException("There should be at least one cache name");
      }
   }


   public String toString() {
      return "CacheonixModelValidator{}";
   }
}