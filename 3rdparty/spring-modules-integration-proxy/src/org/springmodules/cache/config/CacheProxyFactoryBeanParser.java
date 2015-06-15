/* 
 * Created on Feb 19, 2006
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

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;

import org.springmodules.cache.interceptor.proxy.CacheProxyFactoryBean;

/**
 * <p>
 * Template that handles the parsing of the XML tag "proxy". Creates, configures
 * and registers and implementation of
 * <code>{@link CacheProxyFactoryBean}</code> in the provided registry of bean
 * definitions.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class CacheProxyFactoryBeanParser extends
    AbstractCacheSetupStrategyParser {

  private static final String CACHE_MODEL_KEY = "methodName";
  
  /**
   * @see AbstractCacheSetupStrategyParser#getCacheModelKey()
   */
  protected String getCacheModelKey() {
    return CACHE_MODEL_KEY;
  }

  /**
   * Creates and registers a <code>{@link CacheProxyFactoryBean}</code> by
   * parsing the given XML element.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the registry of bean definitions
   * @param propertySource
   *          contains common properties for the different cache setup
   *          strategies
   * @throws IllegalStateException
   *           if the "proxy" tag does not contain any reference to an existing
   *           bean or if it does not contain a bean definition
   * 
   * @see AbstractCacheSetupStrategyParser#parseCacheSetupStrategy(Element,
   *      ParserContext, CacheSetupStrategyPropertySource)
   */
  protected void parseCacheSetupStrategy(Element element,
      ParserContext parserContext,
      CacheSetupStrategyPropertySource propertySource) {

    Object target = getBeanReferenceParser().parse(element, parserContext);

    RootBeanDefinition cacheProxyFactoryBean = new RootBeanDefinition(
        CacheProxyFactoryBean.class, propertySource.getAllProperties());

    cacheProxyFactoryBean.getPropertyValues()
        .addPropertyValue("target", target);

    String id = element.getAttribute("id");
    BeanDefinitionRegistry registry = parserContext.getRegistry();
    registry.registerBeanDefinition(id, cacheProxyFactoryBean);
  }
}
