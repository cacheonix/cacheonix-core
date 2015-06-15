/* 
 * Created on Nov 4, 2004
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

package org.springmodules.cache.interceptor.caching;

import java.io.Serializable;

import org.springmodules.cache.CachingModel;

/**
 * <p>
 * Receives a notification that a new entry was stored in the cache.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface CachingListener {

  /**
   * Notification that a new entry was stored in the cache.
   * 
   * @param key
   *          the key used to store the entry.
   * @param obj
   *          the object stored in the cache.
   * @param model
   *          the caching model that specified how to store the object.
   */
  void onCaching(Serializable key, Object obj, CachingModel model);
}