/* 
 * Created on Oct 18, 2004
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

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;

/**
 * <p>
 * Template for factories of cache managers.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheManagerFactoryBean implements
    CacheManagerFactoryBean {

  /** Logger available to subclasses */
  protected final Log logger = LogFactory.getLog(getClass());

  private String cacheProviderName;

  /**
   * Location of the cache manager configuration file.
   */
  private Resource configLocation;

  /**
   * Constructor.
   */
  public AbstractCacheManagerFactoryBean() {
    super();
  }

  /**
   * Creates the cache manager after all the properties of this factory has been
   * set by the Spring container.
   * 
   * @throws Exception
   *           any exception thrown when creating the cache manager
   * @see #createCacheManager()
   */
  public final void afterPropertiesSet() throws Exception {
    cacheProviderName = getCacheProviderName();
    logger.info("Creating the " + cacheProviderName + " cache manager.");
    createCacheManager();
  }

  /**
   * Shuts down the cache manager before this factory is destroyed by the Spring
   * container.
   * 
   * @throws Exception
   *           any exception thrown when shutting down the cache manager
   */
  public final void destroy() throws Exception {
    if (getObject() != null) {
      logger.info("Shutting down the " + cacheProviderName + " cache manager.");
      destroyCacheManager();

    } else {
      logger.info("The " + cacheProviderName
          + " cache manager was not built. No need to shut it down.");
    }
  }

  /**
   * Notifies the Spring container that this factory is a singleton bean.
   * 
   * @return <code>true</code>.
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * Sets the location of the cache manager configuration file.
   * 
   * @param newConfigLocation
   *          the new location of the cache manager configuration file
   */
  public final void setConfigLocation(Resource newConfigLocation) {
    configLocation = newConfigLocation;
  }

  /**
   * Creates the cache manager.
   * 
   * @throws Exception
   *           any exception thrown when creating the cache manager
   */
  protected abstract void createCacheManager() throws Exception;

  /**
   * Shuts down the cache manager (if it was previously created.)
   * 
   * @throws Exception
   *           any exception thrown when shutting down the cache manager
   */
  protected abstract void destroyCacheManager() throws Exception;

  /**
   * @return the name of the cache provider whose cache manager is created by
   *         this factory
   */
  protected abstract String getCacheProviderName();

  /**
   * @return the location of the cache manager configuration file
   */
  protected final Resource getConfigLocation() {
    return configLocation;
  }

  /**
   * @return the configuration resource as a <code>java.util.Properties</code>.
   * @throws IOException
   *           thrown if there is any I/O error when reading the configuration
   *           resource.
   */
  protected final Properties getConfigProperties() throws IOException {
    Properties properties = null;

    if (configLocation != null) {
      properties = new Properties();
      properties.load(configLocation.getInputStream());
    }

    return properties;
  }
}