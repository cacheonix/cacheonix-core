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
package org.springmodules.cache.config.gigaspaces;


import org.springmodules.cache.config.AbstractCacheProviderFacadeParser;
import org.springmodules.cache.provider.gigaspaces.GigaSpacesFacade;

/**
 * <p>
 * Parses the XML tag "config" when using the XML namespace "gigaspaces". Creates
 * and registers and implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * and a cache manager in the provided registry of bean definitions.
 * </p>
 *
 * @author Lior Ben Yizhak
 */
public final class GigaSpacesFacadeParser extends AbstractCacheProviderFacadeParser {


  /**
   * @see org.springmodules.cache.config.AbstractCacheProviderFacadeParser#getCacheProviderFacadeClass()
   */
  protected Class getCacheProviderFacadeClass() {
    return GigaSpacesFacade.class;
  }

}
