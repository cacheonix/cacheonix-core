/* 
 * Created on Nov 10, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.provider;

import java.beans.PropertyEditor;
import java.io.Serializable;

import org.springframework.beans.factory.InitializingBean;

import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Facade (unified interface) for different cache providers.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface CacheProviderFacade {

  /**
   * Cancels the update being made to the cache.
   * 
   * @param key
   *          the key being used in the cache update.
   * @throws CacheException
   *           if an unexpected error takes place when attempting to cancel the
   *           update.
   */
  void cancelCacheUpdate(Serializable key) throws CacheException;

  /**
   * Flushes the cache.
   * 
   * @param model
   *          the model that specifies what and how to flush.
   * @throws CacheException
   *           if an unexpected error takes place when flushing the cache.
   */
  void flushCache(FlushingModel model) throws CacheException;

  /**
   * @return the validator for both caching and flushing models
   */
  CacheModelValidator modelValidator();

  /**
   * @return the <code>PropertyEditor</code> for caching models
   */
  PropertyEditor getCachingModelEditor();

  /**
   * @return the <code>PropertyEditor</code> for flushing models
   */
  PropertyEditor getFlushingModelEditor();

  /**
   * Retrieves an entry from the cache.
   * 
   * @param key
   *          the key under which the entry is stored.
   * @param model
   *          the model that specifies how to retrieve an entry.
   * @return the cached entry.
   * @throws CacheException
   *           if an unexpected error takes place when retrieving the entry from
   *           the cache.
   */
  Object getFromCache(Serializable key, CachingModel model)
      throws CacheException;

  /**
   * @return <code>true</code> if no exception should be thrown if an error
   *         takes place when the cache provider is being configured or
   *         accessed.
   */
  boolean isFailQuietlyEnabled();

  /**
   * Stores an object in the cache.
   * 
   * @param key
   *          the key under which the object will be stored.
   * @param model
   *          the model that specifies how to store an object.
   * @param obj
   *          the object to store in the cache.
   * @throws CacheException
   *           if an unexpected error takes place when storing an object in the
   *           cache.
   */
  void putInCache(Serializable key, CachingModel model, Object obj)
      throws CacheException;

  /**
   * Removes an object from the cache.
   * 
   * @param key
   *          the key under which the object is stored.
   * @param model
   *          the model that specifies how to store an object.
   * @throws CacheException
   *           if an unexpected error takes place when removing an object from
   *           the cache.
   */
  void removeFromCache(Serializable key, CachingModel model)
      throws CacheException;
}