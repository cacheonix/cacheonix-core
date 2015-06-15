/* 
 * Created on Feb 20, 2006
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
 * Copyright @2006 the original author or authors.
 */
package org.springmodules.cache.config;

import java.util.List;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * <p>
 * Stores properties that are common to all configuration strategies.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class CacheSetupStrategyPropertySource {

  public final Object cacheKeyGenerator;

  public final RuntimeBeanReference cacheProviderFacadeReference;

  public final List cachingListeners;

  public final Map cachingModelMap;

  public final Map flushingModelMap;

  /**
   * Constructor.
   * 
   * @param newCacheKeyGenerator
   *          a cache key generator or a reference to an already existing one
   * @param newCacheProviderFacade
   *          a reference to the cache provider facade
   * @param newCachingListeners
   *          a list of caching listeners
   * @param newCachingModelMap
   *          a list of caching models
   * @param newFlushingModelMap
   *          a list of flushing models
   */
  public CacheSetupStrategyPropertySource(Object newCacheKeyGenerator,
      RuntimeBeanReference newCacheProviderFacade, List newCachingListeners,
      Map newCachingModelMap, Map newFlushingModelMap) {
    super();
    cacheKeyGenerator = newCacheKeyGenerator;
    cacheProviderFacadeReference = newCacheProviderFacade;
    cachingListeners = newCachingListeners;
    cachingModelMap = newCachingModelMap;
    flushingModelMap = newFlushingModelMap;
  }

  /**
   * Returns the properties specified by:
   * <ul>
   * <li><code>{@link #getCacheProviderFacadeProperty()}</code></li>
   * <li><code>{@link #getCachingListenersProperty()}</code></li>
   * <li><code>{@link #getCachingModelsProperty()}</code></li>
   * <li><code>{@link #getFlushingModelsProperty()}</code></li>
   * </ul>
   * 
   * @return all the properties stored in this object.
   */
  public MutablePropertyValues getAllProperties() {
    MutablePropertyValues allPropertyValues = new MutablePropertyValues();
    allPropertyValues.addPropertyValue(getCacheKeyGeneratorProperty());
    allPropertyValues.addPropertyValue(getCacheProviderFacadeProperty());
    allPropertyValues.addPropertyValue(getCachingListenersProperty());
    allPropertyValues.addPropertyValue(getCachingModelsProperty());
    allPropertyValues.addPropertyValue(getFlushingModelsProperty());

    return allPropertyValues;
  }

  public PropertyValue getCacheKeyGeneratorProperty() {
    return new PropertyValue("cacheKeyGenerator", cacheKeyGenerator);
  }
  
  public PropertyValue getCacheProviderFacadeProperty() {
    return new PropertyValue("cacheProviderFacade",
        cacheProviderFacadeReference);
  }

  public PropertyValue getCachingListenersProperty() {
    return new PropertyValue("cachingListeners", cachingListeners);
  }

  public PropertyValue getCachingModelsProperty() {
    return new PropertyValue("cachingModels", cachingModelMap);
  }

  public PropertyValue getFlushingModelsProperty() {
    return new PropertyValue("flushingModels", flushingModelMap);
  }
}
