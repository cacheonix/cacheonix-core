/* 
 * Created on Feb 20, 2006
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

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;

import org.springmodules.cache.interceptor.caching.CachingAttributeSourceAdvisor;
import org.springmodules.cache.interceptor.caching.MetadataCachingInterceptor;
import org.springmodules.cache.interceptor.flush.FlushingAttributeSourceAdvisor;
import org.springmodules.cache.interceptor.flush.MetadataFlushingInterceptor;

/**
 * <p>
 * Template that handles the parsing of the XML tags related to source-level
 * metadata attributes.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractMetadataAttributesParser extends
    AbstractCacheSetupStrategyParser {

  /**
   * Contains the names of beans to register.
   */
  private static class BeanName {

    static final String CACHING_INTERCEPTOR = MetadataCachingInterceptor.class
        .getName();

    static final String FLUSHING_INTERCEPTOR = MetadataFlushingInterceptor.class
        .getName();
  }
  
  private static final String CACHE_MODEL_KEY = "id";

  /**
   * Adds extra properties to the caching interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the caching interceptor
   * @param registry
   *          the registry of bean definitions
   */
  protected abstract void configureCachingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry);

  /**
   * Adds extra properties to the flushing interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the flushing interceptor
   * @param registry
   *          the registry of bean definitions
   */
  protected abstract void configureFlushingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry);

  /**
   * @see AbstractCacheSetupStrategyParser#getCacheModelKey()
   */
  protected String getCacheModelKey() {
    return CACHE_MODEL_KEY;
  }

  /**
   * Creates and registers the necessary bean definitions to set up
   * configuration of caching services using source-level metadata attributes.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the registry of bean definitions
   * @param propertySource
   *          contains common properties for the different cache setup
   *          strategies
   * 
   * @see AbstractCacheSetupStrategyParser#parseCacheSetupStrategy(Element,
   *      ParserContext, CacheSetupStrategyPropertySource)
   */
  protected final void parseCacheSetupStrategy(Element element,
      ParserContext parserContext,
      CacheSetupStrategyPropertySource propertySource) {

    BeanDefinitionRegistry registry = parserContext.getRegistry();

    registerAutoproxy(registry);
    registerCustomBeans(registry);
    registerCachingInterceptor(registry, propertySource);
    registerFlushingInterceptor(registry, propertySource);
    registerCachingAdvisor(registry);
    registerFlushingAdvisor(registry);
  }

  /**
   * Gives subclasses the opportunity to register custom bean definitions in the
   * given registry.
   * 
   * @param registry
   *          the registry of bean definitions
   */
  protected void registerCustomBeans(BeanDefinitionRegistry registry) {
    // no implementation
  }

  private void registerAutoproxy(BeanDefinitionRegistry registry) {
    RootBeanDefinition autoproxy = new RootBeanDefinition(
        DefaultAdvisorAutoProxyCreator.class);
    registry.registerBeanDefinition("autoproxy", autoproxy);
  }

  private void registerCachingAdvisor(BeanDefinitionRegistry registry) {
    Class cachingAdvisorClass = CachingAttributeSourceAdvisor.class;
    RootBeanDefinition cachingAdvisor = new RootBeanDefinition(
        cachingAdvisorClass);
    cachingAdvisor.getConstructorArgumentValues().addGenericArgumentValue(
        new RuntimeBeanReference(BeanName.CACHING_INTERCEPTOR));
    registry.registerBeanDefinition(cachingAdvisorClass.getName(),
        cachingAdvisor);
  }

  private void registerCachingInterceptor(BeanDefinitionRegistry registry,
      CacheSetupStrategyPropertySource propertySource) {

    MutablePropertyValues propertyValues = new MutablePropertyValues();
    propertyValues.addPropertyValue(propertySource
        .getCacheKeyGeneratorProperty());
    propertyValues.addPropertyValue(propertySource
        .getCacheProviderFacadeProperty());
    propertyValues.addPropertyValue(propertySource
        .getCachingListenersProperty());
    propertyValues.addPropertyValue(propertySource.getCachingModelsProperty());

    RootBeanDefinition cachingInterceptor = new RootBeanDefinition(
        MetadataCachingInterceptor.class, propertyValues);

    configureCachingInterceptor(propertyValues, registry);

    String beanName = BeanName.CACHING_INTERCEPTOR;
    registry.registerBeanDefinition(beanName, cachingInterceptor);
  }

  private void registerFlushingAdvisor(BeanDefinitionRegistry registry) {
    Class flushingAdvisorClass = FlushingAttributeSourceAdvisor.class;
    RootBeanDefinition flushingAdvisor = new RootBeanDefinition(
        flushingAdvisorClass);
    flushingAdvisor.getConstructorArgumentValues().addGenericArgumentValue(
        new RuntimeBeanReference(BeanName.FLUSHING_INTERCEPTOR));
    registry.registerBeanDefinition(flushingAdvisorClass.getName(),
        flushingAdvisor);
  }

  private void registerFlushingInterceptor(BeanDefinitionRegistry registry,
      CacheSetupStrategyPropertySource propertySource) {

    MutablePropertyValues propertyValues = new MutablePropertyValues();
    propertyValues.addPropertyValue(propertySource
        .getCacheProviderFacadeProperty());
    propertyValues.addPropertyValue(propertySource.getFlushingModelsProperty());

    RootBeanDefinition flushingInterceptor = new RootBeanDefinition(
        MetadataFlushingInterceptor.class, propertyValues);

    configureFlushingInterceptor(propertyValues, registry);

    String beanName = BeanName.FLUSHING_INTERCEPTOR;
    registry.registerBeanDefinition(beanName, flushingInterceptor);
  }
}
