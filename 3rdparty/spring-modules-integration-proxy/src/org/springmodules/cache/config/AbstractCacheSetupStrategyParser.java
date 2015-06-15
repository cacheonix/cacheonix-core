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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Template that handles the parsing of setup strategy for declarative caching
 * services.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractCacheSetupStrategyParser implements
    BeanDefinitionParser {

  private BeanReferenceParser beanReferenceParser;

  private CacheModelParser cacheModelParser;

  private CachingListenerValidator cachingListenerValidator;

  /**
   * Constructor.
   */
  public AbstractCacheSetupStrategyParser() {
    super();
    beanReferenceParser = new BeanReferenceParserImpl();
    cachingListenerValidator = new CachingListenerValidatorImpl();
  }

  /**
   * Parses the given XML element containing the properties and/or sub-elements
   * necessary to configure a strategy for setting up declarative caching
   * services.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parser context
   * @throws IllegalStateException
   *           if the bean definition registry does not have a definition for
   *           the <code>CacheProviderFacade</code> registered under the name
   *           specified in the XML attribute "providerId"
   * @throws IllegalStateException
   *           if the cache provider facade is in invalid state
   * @throws IllegalStateException
   *           if any of the caching listeners is not an instance of
   *           <code>CachingListener</code>
   * 
   * @see BeanDefinitionParser#parse(Element, ParserContext)
   */
  public final BeanDefinition parse(Element element, ParserContext parserContext)
      throws NoSuchBeanDefinitionException, IllegalStateException {
    String cacheProviderFacadeId = element.getAttribute("providerId");

    BeanDefinitionRegistry registry = parserContext.getRegistry();
    if (!registry.containsBeanDefinition(cacheProviderFacadeId)) {
      throw new IllegalStateException(
          "An implementation of CacheProviderFacade should be registered under the name "
              + StringUtils.quote(cacheProviderFacadeId));
    }

    RuntimeBeanReference cacheProviderFacadeReference = new RuntimeBeanReference(
        cacheProviderFacadeId);

    Object cacheKeyGenerator = parseCacheKeyGenerator(element, parserContext);
    List cachingListeners = parseCachingListeners(element, parserContext);
    Map cachingModels = parseCachingModels(element);
    Map flushingModels = parseFlushingModels(element);

    CacheSetupStrategyPropertySource ps = new CacheSetupStrategyPropertySource(
        cacheKeyGenerator, cacheProviderFacadeReference, cachingListeners,
        cachingModels, flushingModels);

    parseCacheSetupStrategy(element, parserContext, ps);
    return null;
  }

  public final void setCacheModelParser(CacheModelParser newCacheModelParser) {
    cacheModelParser = newCacheModelParser;
  }

  protected final BeanReferenceParser getBeanReferenceParser() {
    return beanReferenceParser;
  }

  /**
   * Returns the key to be used to store a
   * <code>{@link org.springmodules.cache.CacheModel}</code> in a map. Each
   * implementation of this class has two maps, one for caching models and one
   * for flushing models. The key of each model is specified by each
   * implementation of this template.
   * 
   * @return the key to be used to store a <code>CacheModel</code> in a map
   */
  protected abstract String getCacheModelKey();

  protected final CacheModelParser getCacheModelParser() {
    return cacheModelParser;
  }

  /**
   * Parses the given XML element to create the strategy for setting up
   * declarative caching services.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parser context
   * @param propertySource
   *          contains common properties for the different cache setup
   *          strategies
   */
  protected abstract void parseCacheSetupStrategy(Element element,
      ParserContext parserContext,
      CacheSetupStrategyPropertySource propertySource);

  protected final void setBeanReferenceParser(
      BeanReferenceParser newBeanReferenceParser) {
    beanReferenceParser = newBeanReferenceParser;
  }

  protected final void setCachingListenerValidator(
      CachingListenerValidator newCachingListenerValidator) {
    cachingListenerValidator = newCachingListenerValidator;
  }

  private Object parseCacheKeyGenerator(Element element,
      ParserContext parserContext) {
    Object keyGenerator = null;

    List cacheKeyGeneratorElements = DomUtils.getChildElementsByTagName(
        element, "cacheKeyGenerator");
    if (!CollectionUtils.isEmpty(cacheKeyGeneratorElements)) {
      Element cacheKeyGeneratorElement = (Element) cacheKeyGeneratorElements
          .get(0);
      keyGenerator = beanReferenceParser.parse(cacheKeyGeneratorElement,
          parserContext);
    }

    return keyGenerator;
  }

  /**
   * Parses the given XML element containing references to the caching listeners
   * to be added to the caching setup strategy.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parser context
   * @return a list containing references to caching listeners already
   *         registered in the given register
   * @throws IllegalStateException
   *           if any of the given ids reference a caching listener that does
   *           not exist in the registry
   * @throws IllegalStateException
   *           if the given id references a caching listener that is not an
   *           instance of <code>CachingListener</code>
   * @throws IllegalStateException
   *           if the caching listener elements does not contain a reference to
   *           an existing caching listener and does not contain an inner
   *           definition of a caching listener
   */
  private List parseCachingListeners(Element element,
      ParserContext parserContext) throws IllegalStateException {

    List listenersElements = DomUtils.getChildElementsByTagName(element,
        "cachingListeners");

    if (CollectionUtils.isEmpty(listenersElements)) {
      return null;
    }

    Element listenersElement = (Element) listenersElements.get(0);
    List listenerElements = DomUtils.getChildElementsByTagName(
        listenersElement, "cachingListener");

    ManagedList listeners = new ManagedList();
    boolean registerCachingListener = true;
    int listenerCount = listenerElements.size();

    for (int i = 0; i < listenerCount; i++) {
      Element listenerElement = (Element) listenerElements.get(i);

      Object cachingListener = beanReferenceParser.parse(listenerElement,
          parserContext, registerCachingListener);
      cachingListenerValidator.validate(cachingListener, i, parserContext);
      listeners.add(cachingListener);
    }

    return listeners;
  }

  /**
   * Parses the given XML element which sub-elements containing the properties
   * of the caching models to create.
   * 
   * @param element
   *          the XML element to parse
   * @return a map containing the parsed caching models.The key of each element
   *         is the value of the XML attribute <code>target</code> (a String)
   *         and the value is the caching model (an instance of
   *         <code>CachingModel</code>)
   */
  private Map parseCachingModels(Element element) {
    List modelElements = DomUtils.getChildElementsByTagName(element, "caching");
    if (CollectionUtils.isEmpty(modelElements)) {
      return null;
    }

    String cacheModelKey = getCacheModelKey();
    Map models = new HashMap();
    int modelElementCount = modelElements.size();

    for (int i = 0; i < modelElementCount; i++) {
      Element modelElement = (Element) modelElements.get(i);
      String key = modelElement.getAttribute(cacheModelKey);

      CachingModel model = cacheModelParser.parseCachingModel(modelElement);
      models.put(key, model);
    }

    return models;
  }

  /**
   * Parses the given XML element which sub-elements containing the properties
   * of the flushing models to create.
   * 
   * @param element
   *          the XML element to parse
   * @return a map containing the parsed flushing models.The key of each element
   *         is the value of the XML attribute <code>target</code> (a String) *
   *         and the value is the flushing model (an instance of
   *         <code>FlushingModel</code>)
   */
  private Map parseFlushingModels(Element element) {
    List modelElements = DomUtils.getChildElementsByTagName(element,
        "flushing");
    if (CollectionUtils.isEmpty(modelElements)) {
      return null;
    }

    String cacheModelKey = getCacheModelKey();
    Map models = new HashMap();
    int modelElementCount = modelElements.size();

    for (int i = 0; i < modelElementCount; i++) {
      Element modelElement = (Element) modelElements.get(i);
      String key = modelElement.getAttribute(cacheModelKey);

      FlushingModel model = cacheModelParser.parseFlushingModel(modelElement);
      models.put(key, model);
    }

    return models;
  }
}
