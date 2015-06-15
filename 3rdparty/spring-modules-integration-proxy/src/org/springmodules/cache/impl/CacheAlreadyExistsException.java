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

import org.springframework.util.StringUtils;

/**
 * Exception thrown when trying to add a cache to the cache manager using an
 * already existing name.
 * 
 * @author Alex Ruiz
 * 
 */
public class CacheAlreadyExistsException extends CachingException {

  private static final long serialVersionUID = -6479571399378243183L;

  /**
   * Constructor.
   * 
   * @param cacheName
   *          the name of the cache that already exists in the cache manager
   */
  public CacheAlreadyExistsException(String cacheName) {
    super("The cache " + StringUtils.quote(cacheName) + " already exists");
  }
}
