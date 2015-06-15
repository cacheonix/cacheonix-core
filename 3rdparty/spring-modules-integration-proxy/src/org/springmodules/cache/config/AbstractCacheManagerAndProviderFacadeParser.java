/* 
 * Created on Feb 3, 2006
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

import org.w3c.dom.Element;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Template that handles the parsing of the XML tag "config". Creates and
 * registers and implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * and a cache manager in the provided registry of bean definitions.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheManagerAndProviderFacadeParser extends
    AbstractCacheProviderFacadeParser {

  /**
   * Parses the given XML element containing the properties of the cache manager
   * to register in the given registry of bean definitions.
   * 
   * @param element
   *          the XML element to parse
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractCacheProviderFacadeParser#doParse(String, Element,
   *      BeanDefinitionRegistry)
   */
  protected final void doParse(String cacheProviderFacadeId, Element element,
      BeanDefinitionRegistry registry) {
    String id = "cacheManager";
    Class clazz = getCacheManagerClass();
    RootBeanDefinition cacheManager = new RootBeanDefinition(clazz);
    MutablePropertyValues cacheManagerProperties = new MutablePropertyValues();
    cacheManager.setPropertyValues(cacheManagerProperties);

    PropertyValue configLocation = parseConfigLocationProperty(element);
    cacheManagerProperties.addPropertyValue(configLocation);
    registry.registerBeanDefinition(id, cacheManager);

    BeanDefinition cacheProviderFacade = registry
        .getBeanDefinition(cacheProviderFacadeId);
    cacheProviderFacade.getPropertyValues().addPropertyValue("cacheManager",
        new RuntimeBeanReference(id));
  }

  /**
   * @return the class of the cache manager to create
   */
  protected abstract Class getCacheManagerClass();

  /**
   * Parses the given XML element to obtain the value of the property
   * <code>configLocation</code>. This property specifies the location of the
   * configuration file to use to configure the cache manager.
   * 
   * @param element
   *          the XML element to parse
   * @return the value of the property <code>configLocation</code>
   */
  private PropertyValue parseConfigLocationProperty(Element element) {
    Resource resource = null;

    String configLocation = element.getAttribute("configLocation");
    if (StringUtils.hasText(configLocation)) {
      ResourceEditor resourceEditor = new ResourceEditor();
      resourceEditor.setAsText(configLocation);
      resource = (Resource) resourceEditor.getValue();
    }

    return new PropertyValue("configLocation", resource);
  }

}
