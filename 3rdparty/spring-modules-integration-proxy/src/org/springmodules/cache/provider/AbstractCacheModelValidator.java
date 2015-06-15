/* 
 * Created on Oct 13, 2005
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
 * Template for validators of cache models.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheModelValidator implements
    CacheModelValidator {

  /**
   * @see CacheModelValidator#validateCachingModel(Object)
   */
  public final void validateCachingModel(Object cachingModel)
      throws InvalidCacheModelException {
    Class targetClass = getCachingModelTargetClass();
    if (!isInstanceOf(cachingModel, targetClass)) {
      throw new InvalidCacheModelException(
          "The caching model should be an instance of <"
              + targetClass.getName() + ">");
    }
    validateCachingModelProperties(cachingModel);
  }

  /**
   * @see CacheModelValidator#validateFlushingModel(Object)
   */
  public final void validateFlushingModel(Object flushingModel)
      throws InvalidCacheModelException {
    Class targetClass = getFlushingModelTargetClass();
    if (!isInstanceOf(flushingModel, targetClass)) {
      throw new InvalidCacheModelException(
          "The flushing model should be an instance of <"
              + targetClass.getName() + ">");
    }
    validateFlushingModelProperties(flushingModel);
  }

  /**
   * @return the type of caching model that this validator accepts
   */
  protected abstract Class getCachingModelTargetClass();

  /**
   * @return the type of flushing model that this validator accepts
   */
  protected abstract Class getFlushingModelTargetClass();

  /**
   * Returns <code>true</code> if the given object is an instance of the given
   * class.
   * 
   * @param obj
   *          the object to check
   * @param targetClass
   *          the given class
   * @return <code>true</code> if the given object is an instance of the given
   *         class.
   */
  protected final boolean isInstanceOf(Object obj, Class targetClass) {
    return (obj != null && targetClass.isAssignableFrom(obj.getClass()));
  }

  /**
   * Validates the properties of the given model.
   * 
   * @param cachingModel
   *          the model to validate.
   * @throws InvalidCacheModelException
   *           if one or more properties have invalid values.
   */
  protected void validateCachingModelProperties(Object cachingModel)
      throws InvalidCacheModelException {
    // no implementation.
  }

  /**
   * Validates the properties of the given model.
   * 
   * @param flushingModel
   *          the model to validate.
   * @throws InvalidCacheModelException
   *           if one or more properties have invalid values.
   */
  protected void validateFlushingModelProperties(Object flushingModel)
      throws InvalidCacheModelException {
    // no implementation.
  }

}
