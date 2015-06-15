/* 
 * Created on Feb 26, 2006
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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import org.springmodules.cache.annotations.AnnotationCachingAttributeSource;
import org.springmodules.cache.annotations.AnnotationFlushingAttributeSource;

/**
 * <p>
 * Template that handles the parsing of the XML tag "annotations". Creates and
 * registers the necessary bean definitions to configure caching services using
 * J2SE 5.0 Annotations.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class AnnotationsParser extends AbstractMetadataAttributesParser {

  /**
   * Registers a <code>{@link AnnotationCachingAttributeSource}</code> and
   * adds it as a property of the caching interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the caching interceptor
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractMetadataAttributesParser#configureCachingInterceptor(MutablePropertyValues,
   *      BeanDefinitionRegistry)
   */
  @Override
  protected void configureCachingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry) {

    String beanName = AnnotationCachingAttributeSource.class.getName();

    registry.registerBeanDefinition(beanName, new RootBeanDefinition(
        AnnotationCachingAttributeSource.class));

    propertyValues.addPropertyValue("cachingAttributeSource",
        new RuntimeBeanReference(beanName));
  }

  /**
   * Registers a <code>{@link AnnotationFlushingAttributeSource}</code> and
   * adds it as a property of the flushing interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the caching interceptor
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractMetadataAttributesParser#configureFlushingInterceptor(MutablePropertyValues,
   *      BeanDefinitionRegistry)
   */
  @Override
  protected void configureFlushingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry) {

    String beanName = AnnotationFlushingAttributeSource.class.getName();

    registry.registerBeanDefinition(beanName, new RootBeanDefinition(
        AnnotationFlushingAttributeSource.class));

    propertyValues.addPropertyValue("flushingAttributeSource",
        new RuntimeBeanReference(beanName));
  }
}
