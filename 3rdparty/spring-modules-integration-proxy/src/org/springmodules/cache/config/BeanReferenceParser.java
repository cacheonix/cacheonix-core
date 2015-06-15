/* 
 * Created on Mar 13, 2006
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

import org.springframework.beans.factory.xml.ParserContext;

/**
 * <p>
 * Parses XML tags that have either a reference to an existing bean definition
 * or an inner bean definition.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface BeanReferenceParser {

  /**
   * Parses the given XML element which has either a reference to an existing
   * bean definition or an inner bean definition. The inner bean definition is
   * <strong>not</strong> registered in the bean definition registry.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parse context
   * @return the created object, which could be a
   *         <code>RuntimeBeanReference</code> (if the XML element contains a
   *         reference to an existing bean definition) or a
   *         <code>BeanDefinitionHolder</code> (if the XML element contains an
   *         inner bean definition)
   */
  Object parse(Element element, ParserContext parserContext);

  /**
   * Parses the given XML element which has either a reference to an existing
   * bean definition or an inner bean definition.
   * 
   * @param element
   *          the XML element to parse
   * @param parserContext
   *          the parse context
   * @param registerInnerBean
   *          indicates if the inner bean definition (if any) should be
   *          registered in the bean definition registry
   * @return the created object, which could be a
   *         <code>RuntimeBeanReference</code> (if the XML element contains a
   *         reference to an existing bean definition) or a
   *         <code>BeanDefinitionHolder</code> (if the XML element contains an
   *         inner bean definition)
   */
  Object parse(Element element, ParserContext parserContext,
      boolean registerInnerBean);

}