/* 
 * Created on Apr 7, 2006
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
package org.springmodules.cache.impl;

/**
 * Manages creation, access and destruction of caches.
 * 
 * @author Alex Ruiz
 * 
 */
public interface CacheManager {

  /**
   * Adds a new cache to this manager.
   * 
   * @param cacheName
   *          the name of the cache to add
   * @param cache
   *          the cache to add
   * @throws CacheAlreadyExistsException
   *           if there is already a cache stored under the given name
   */
  void addCache(String cacheName, Cache cache)
      throws CacheAlreadyExistsException;

  /**
   * Retrieves a reference to a cache.
   * 
   * @param cacheName
   *          the name of the cache to retrieve
   * @return the cache which name matches the specified String or
   *         <code>null</code> if there is none
   */
  Cache getCache(String cacheName);

  /**
   * Shuts down the cache manager and destroys its cache(s).
   */
  void shutDown();
}
