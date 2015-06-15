/* 
 * Created on Aug 2, 2005
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

import org.springmodules.cache.CacheException;

/**
 * <p>
 * Exception thrown when the cache to access cannot be found.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class CacheNotFoundException extends CacheException {

  private static final long serialVersionUID = 6601590278654078802L;

  /**
   * Creates a <code>CacheNotFoundException</code>.
   * 
   * @param cacheName
   *          the name of the cache that could be found
   */
  public CacheNotFoundException(String cacheName) {
    super("Unable to find cache '" + cacheName + "'");
  }

}
