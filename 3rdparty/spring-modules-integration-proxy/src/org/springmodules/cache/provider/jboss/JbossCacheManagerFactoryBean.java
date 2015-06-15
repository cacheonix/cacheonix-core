/* 
 * Created on Aug 31, 2005
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
package org.springmodules.cache.provider.jboss;

import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;

import org.springframework.core.io.Resource;

import org.springmodules.cache.provider.AbstractCacheManagerFactoryBean;

/**
 * <p>
 * Singleton <code>FactoryBean</code> that constructs and exposes a JBossCache
 * <code>TreeCache</code>.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class JbossCacheManagerFactoryBean extends
    AbstractCacheManagerFactoryBean {

  private static final String CACHE_PROVIDER_NAME = "JBossCache";

  private TreeCache treeCache;

  /**
   * @return the cache manager (a JBossCache TreeCache) managed by this
   *         factory
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() {
    return treeCache;
  }

  /**
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class getObjectType() {
    return (treeCache != null) ? treeCache.getClass() : TreeCache.class;
  }

  /**
   * @see AbstractCacheManagerFactoryBean#createCacheManager()
   */
  protected void createCacheManager() throws Exception {
    treeCache = new TreeCache();

    Resource configLocation = getConfigLocation();

    if (configLocation != null) {
      PropertyConfigurator configurator = new PropertyConfigurator();
      configurator.configure(treeCache, configLocation.getInputStream());
    }

    treeCache.createService();
    treeCache.startService();
  }

  /**
   * @see AbstractCacheManagerFactoryBean#destroyCacheManager()
   */
  protected void destroyCacheManager() {
    treeCache.stopService();
    treeCache.destroyService();
  }

  /**
   * @see AbstractCacheManagerFactoryBean#getCacheProviderName()
   */
  protected String getCacheProviderName() {
    return CACHE_PROVIDER_NAME;
  }

}
