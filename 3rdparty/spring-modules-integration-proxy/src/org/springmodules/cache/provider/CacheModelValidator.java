/* 
 * Created on Jan 21, 2005
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

package org.springmodules.cache.provider;

/**
 * <p>
 * Validates the properties of cache models.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface CacheModelValidator {

  /**
   * Validates the properties of the specified caching model.
   * 
   * @param cachingModel
   *          the model to validate
   * @throws InvalidCacheModelException
   *           if one or more properties of the given model are not valid
   */
  void validateCachingModel(Object cachingModel)
      throws InvalidCacheModelException;

  /**
   * Validates the properties of the specified cache-flushing model.
   * 
   * @param flushingModel
   *          the model to validate
   * @throws InvalidCacheModelException
   *           if one or more properties of the given model are not valid
   */
  void validateFlushingModel(Object flushingModel)
      throws InvalidCacheModelException;
}