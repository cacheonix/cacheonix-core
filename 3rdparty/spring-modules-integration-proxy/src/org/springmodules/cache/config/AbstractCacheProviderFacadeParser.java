/* 
 * Created on Jan 19, 2006
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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;

import org.springmodules.cache.serializable.XStreamSerializableFactory;

/**
 * <p>
 * Template that handles the parsing of the XML tag "config". Creates and
 * registers and implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * in the provided registry of bean definitions.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheProviderFacadeParser implements
    BeanDefinitionParser {

  /**
   * Contains the names of the bean properties used in this parser.
   */
  private static abstract class PropertyName {

    static final String SERIALIZABLE_FACTORY = "serializableFactory";
  }

  /**
   * Contains the valid values for the XML element
   * <code>serializableFactory</code>.
   */
  private static abstract class SerializableFactory {

    static final String NONE = "NONE";

    static final String XSTREAM = "XSTREAM";
  }

  /**
   * Parses the specified XML element which contains the properties of the
   * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
   * to register in the given registry of bean definitions.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parser context
   * @throws IllegalStateException
   *           if the value of the property <code>serializableFactory</code>
   *           is not equal to "NONE" or "XSTREAM"
   * 
   * @see BeanDefinitionParser#parse(Element, ParserContext)
   */
  public final BeanDefinition parse(Element element, ParserContext parserContext)
      throws IllegalStateException {
    String id = element.getAttribute("id");

    // create the cache provider facade
    Class clazz = getCacheProviderFacadeClass();
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    RootBeanDefinition cacheProviderFacade = new RootBeanDefinition(clazz,
        propertyValues);
    propertyValues.addPropertyValue(parseFailQuietlyEnabledProperty(element));
    propertyValues.addPropertyValue(parseSerializableFactoryProperty(element));

    BeanDefinitionRegistry registry = parserContext.getRegistry();
    registry.registerBeanDefinition(id, cacheProviderFacade);

    doParse(id, element, registry);
    return null;
  }

  /**
   * Gives subclasses (of this class) the opportunity to parse their own bean
   * definitions.
   * 
   * @param cacheProviderFacadeId
   *          the id of the already registered <code>CacheProviderFacade</code>
   * @param element
   *          the XML element containing the values needed to parse bean
   *          definitions
   * @param registry
   *          the registry of bean definitions
   */
  protected void doParse(String cacheProviderFacadeId, Element element,
      BeanDefinitionRegistry registry) {
    // no implementation.
  }

  /**
   * @return the class of the <code>CacheProviderFacade</code> to register.
   */
  protected abstract Class getCacheProviderFacadeClass();

  /**
   * Parses the given XML element to obtain the value of the property
   * <code>failQuietly</code>. This property specifies if any exception
   * thrown at run-time by the cache should be propagated (<code>false</code>)
   * to the application or not (<code>true</code>.)
   * 
   * @param element
   *          the XML element to parse
   * @return the value of the property <code>failQuietly</code>
   */
  private PropertyValue parseFailQuietlyEnabledProperty(Element element) {
    String failQuietlyAttr = element.getAttribute("failQuietly");
    Boolean value = "true".equalsIgnoreCase(failQuietlyAttr) ? Boolean.TRUE
        : Boolean.FALSE;
    return new PropertyValue("failQuietlyEnabled", value);
  }

  /**
   * Parses the given XML element to obtain the value of the property
   * <code>serializableFactory</code>. This property specify the factory that
   * forces cache entries to implement the interface <code>Serializable</code>.
   * 
   * @param element
   *          the XML element to parse
   * @return the value of the property <code>serializableFactory</code>
   * @throws IllegalStateException
   *           if the value of the property <code>serializableFactory</code>
   *           is not equal to "NONE" or "XSTREAM"
   */
  private PropertyValue parseSerializableFactoryProperty(Element element)
      throws IllegalStateException {
    String serializableFactoryAttr = element
        .getAttribute("serializableFactory");

    if (!StringUtils.hasText(serializableFactoryAttr)
        || SerializableFactory.NONE.equalsIgnoreCase(serializableFactoryAttr)) {
      return new PropertyValue(PropertyName.SERIALIZABLE_FACTORY, null);
    }

    if (SerializableFactory.XSTREAM.equalsIgnoreCase(serializableFactoryAttr)) {
      return new PropertyValue(PropertyName.SERIALIZABLE_FACTORY,
          new XStreamSerializableFactory());
    }

    throw new IllegalStateException(StringUtils.quote(serializableFactoryAttr)
        + " is not a serializableFactory. Valid values include "
        + StringUtils.quote(SerializableFactory.NONE) + " and "
        + StringUtils.quote(SerializableFactory.XSTREAM));
  }
}
