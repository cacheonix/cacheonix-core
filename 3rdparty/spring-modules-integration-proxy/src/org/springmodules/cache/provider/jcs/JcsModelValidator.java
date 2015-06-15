/* 
 * Created on Jan 13, 2005
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
 * Copyright @2005 the original author or authors.
 */

package org.springmodules.cache.provider.jcs;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;
import org.springmodules.cache.provider.jcs.JcsFlushingModel.CacheStruct;

/**
 * <p>
 * Validates the properties of <code>{@link JcsCachingModel}</code>s and
 * <code>{@link JcsFlushingModel}</code>s.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class JcsModelValidator extends AbstractCacheModelValidator {

  /**
   * @see AbstractCacheModelValidator#getCachingModelTargetClass()
   */
  protected Class getCachingModelTargetClass() {
    return JcsCachingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#getFlushingModelTargetClass()
   */
  protected Class getFlushingModelTargetClass() {
    return JcsFlushingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#validateCachingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if the model does not have a cache name.
   */
  protected void validateCachingModelProperties(Object cachingModel)
      throws InvalidCacheModelException {
    JcsCachingModel model = (JcsCachingModel) cachingModel;
    if (!StringUtils.hasText(model.getCacheName())) {
      throw new InvalidCacheModelException("Cache name should not be empty");
    }
  }

  /**
   * @see AbstractCacheModelValidator#validateFlushingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if any of the cache structs does not have a cache name.
   */
  protected void validateFlushingModelProperties(Object flushingModel)
      throws InvalidCacheModelException {
    JcsFlushingModel model = (JcsFlushingModel) flushingModel;
    CacheStruct[] structs = model.getCacheStructs();

    if (ObjectUtils.isEmpty(structs)) {
      throw new InvalidCacheModelException(
          "There should be at least one cache to flush");
    }

    int structCount = structs.length;
    for (int i = 0; i < structCount; i++) {
      CacheStruct struct = structs[i];
      if (!StringUtils.hasText(struct.getCacheName())) {
        throw new InvalidCacheModelException(
            "Cache name should not be empty in the struct with index <" + i
                + ">");
      }
    }
  }
}