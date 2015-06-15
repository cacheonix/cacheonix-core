/* 
 * Created on Mar 28, 2006
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
package org.springmodules.cache.config.oscache;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

import org.springmodules.cache.config.AbstractCacheNamespaceHandler;
import org.springmodules.cache.config.CacheModelParser;

/**
 * <p>
 * Registers the parsers of the XML elements in the namespace "oscache".
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class OsCacheNamespaceHandler extends
    AbstractCacheNamespaceHandler {

  private CacheModelParser cacheModelParser;

  private BeanDefinitionParser cacheProviderFacadeParser;

  /**
   * Constructor.
   */
  public OsCacheNamespaceHandler() {
    super();
    cacheProviderFacadeParser = new OsCacheFacadeParser();
    cacheModelParser = new OsCacheModelParser();
  }

  protected CacheModelParser getCacheModelParser() {
    return cacheModelParser;
  }

  protected BeanDefinitionParser getCacheProviderFacadeParser() {
    return cacheProviderFacadeParser;
  }
}
