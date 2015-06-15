/*
* Copyright 2006 GigaSpaces, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.springmodules.cache.provider.gigaspaces;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;

/**
 * <p>
 * Validates the property values of <code>{@link GigaSpacesCachingModel}</code>s
 * and <code>{@link GigaSpacesFlushingModel}</code>s.
 * </p>
 *
 * @author Lior Ben Yizhak
 */
public class GigaSpacesModelValidator extends AbstractCacheModelValidator {

  /**
   * Constructor.
   */
  public GigaSpacesModelValidator() {
    super();
  }

  /**
   * @see AbstractCacheModelValidator#getCachingModelTargetClass()
   */
  protected Class getCachingModelTargetClass() {
    return GigaSpacesCachingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#getFlushingModelTargetClass()
   */
  protected Class getFlushingModelTargetClass() {
    return GigaSpacesFlushingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#validateCachingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if the given model does not specify a cache.
   */
  protected void validateCachingModelProperties(Object cachingModel)
      throws InvalidCacheModelException {
    GigaSpacesCachingModel model = (GigaSpacesCachingModel) cachingModel;
    if (!StringUtils.hasText(model.getCacheName())) {
      throw new InvalidCacheModelException("Cache name should not be empty");
    }
  }

  /**
   * @see AbstractCacheModelValidator#validateFlushingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if the given model does not specify at least one cache.
   */
  protected void validateFlushingModelProperties(Object flushingModel)
      throws InvalidCacheModelException {
    GigaSpacesFlushingModel model = (GigaSpacesFlushingModel) flushingModel;
    String[] cacheNames = model.getCacheNames();

    if (ObjectUtils.isEmpty(cacheNames)) {
      throw new InvalidCacheModelException(
          "There should be at least one cache name");
    }
  }
}