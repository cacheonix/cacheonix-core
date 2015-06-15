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
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.metadata.commons.CommonsAttributes;

/**
 * <p>
 * Template that handles the parsing of the XML tag "commons-attributes".
 * Creates and registers the necessary bean definitions to configure caching
 * services using Commons-Attributes.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class CommonsAttributesParser extends
    AbstractMetadataAttributesParser {

  /**
   * Contains the names of beans to register.
   */
  private static class BeanName {

    static final String ATTRIBUTES = CommonsAttributes.class.getName();
  }

  /**
   * Adds a reference to a <code>{@link CommonsAttributes}</code> as a
   * property of the caching interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the caching interceptor
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractMetadataAttributesParser#configureCachingInterceptor(MutablePropertyValues,
   *      BeanDefinitionRegistry)
   */
  protected void configureCachingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry) {
    propertyValues.addPropertyValue(getAttributesProperty());
  }

  /**
   * Adds a reference to a <code>{@link CommonsAttributes}</code> as a
   * property of the flushing interceptor.
   * 
   * @param propertyValues
   *          the set of properties of the flushing interceptor
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractMetadataAttributesParser#configureFlushingInterceptor(MutablePropertyValues,
   *      BeanDefinitionRegistry)
   */
  protected void configureFlushingInterceptor(
      MutablePropertyValues propertyValues, BeanDefinitionRegistry registry) {
    propertyValues.addPropertyValue(getAttributesProperty());
  }

  /**
   * Registers a <code>{@link CommonsAttributes}</code> in the given registry
   * of bean definitions.
   * 
   * @param registry
   *          the registry of bean definitions
   * 
   * @see AbstractMetadataAttributesParser#registerCustomBeans(BeanDefinitionRegistry)
   */
  protected void registerCustomBeans(BeanDefinitionRegistry registry) {
    RootBeanDefinition attributes = new RootBeanDefinition(
        CommonsAttributes.class);
    registry.registerBeanDefinition(BeanName.ATTRIBUTES, attributes);
  }

  private PropertyValue getAttributesProperty() {
    return new PropertyValue("attributes", new RuntimeBeanReference(
        BeanName.ATTRIBUTES));
  }
}
