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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Default implementation of <code>{@link BeanReferenceParser}</code>.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class BeanReferenceParserImpl implements BeanReferenceParser {

	/**
	 * @see BeanReferenceParser#parse(Element,ParserContext)
	 */
	public Object parse(Element element, ParserContext parserContext) {
		return parse(element, parserContext, false);
	}

	/**
	 * @see BeanReferenceParser#parse(Element,ParserContext,boolean)
	 */
	public Object parse(Element element, ParserContext parserContext,
						boolean registerInnerBean) {

		String refId = element.getAttribute("refId");
		if (StringUtils.hasText(refId)) {
			return new RuntimeBeanReference(refId);
		}

		Element beanElement = null;
		List beanElements = DomUtils.getChildElementsByTagName(element, "bean");
		if (!CollectionUtils.isEmpty(beanElements)) {
			beanElement = (Element) beanElements.get(0);
		}
		if (beanElement == null) {
			throw new IllegalStateException("The XML element "
					+ StringUtils.quote(element.getNodeName()) + " should either have a "
					+ "reference to an already registered bean definition or contain a "
					+ "bean definition");
		}

		BeanDefinitionHolder holder = parserContext.getDelegate()
				.parseBeanDefinitionElement(beanElement);

		String beanName = holder.getBeanName();

		if (registerInnerBean && StringUtils.hasText(beanName)) {
			BeanDefinitionRegistry registry = parserContext.getRegistry();
			BeanDefinition beanDefinition = holder.getBeanDefinition();
			registry.registerBeanDefinition(beanName, beanDefinition);

			return new RuntimeBeanReference(beanName);
		}

		return holder;
	}
}
