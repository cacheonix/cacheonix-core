/*
 * Created on Mar 31, 2006
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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;

import org.springmodules.cache.interceptor.caching.CachingListener;

/**
 * <p>
 * Default implementation of <code>{@link CachingListenerValidator}</code>.
 * </p>
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class CachingListenerValidatorImpl implements CachingListenerValidator {

  /**
   * @see CachingListenerValidator#validate(Object, int, ParserContext)
   */
  public void validate(Object cachingListener, int index,
      ParserContext parserContext) throws IllegalStateException {
    BeanDefinitionRegistry registry = parserContext.getRegistry();
    BeanDefinition beanDefinition = null;

    if (cachingListener instanceof RuntimeBeanReference) {
      String beanName = ((RuntimeBeanReference) cachingListener).getBeanName();
      beanDefinition = registry.getBeanDefinition(beanName);

    } else if (cachingListener instanceof BeanDefinitionHolder) {
      beanDefinition = ((BeanDefinitionHolder) cachingListener)
          .getBeanDefinition();
    } else {
      throw new IllegalStateException("The caching listener reference/holder ["
          + index + "] should be an instance of <"
          + RuntimeBeanReference.class.getName() + "> or <"
          + BeanDefinitionHolder.class.getName() + ">");
    }

    Class expectedClass = CachingListener.class;
    Class actualClass = resolveBeanClass(beanDefinition);

    if (!expectedClass.isAssignableFrom(actualClass)) {
      throw new IllegalStateException("The caching listener [" + index
          + "] should be an instance of <" + expectedClass.getName() + ">");
    }
  }

	/**
	 * Resolves class of a beanDefinition
	 * @param beanDefinition the bean definition
	 * @return class of the bean definition
	 * @throws IllegalStateException if the given bean definition class can not
	 * be resolved
	 * @see org.springframework.beans.factory.config.BeanDefinition#setBeanClassName(String)
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#resolveBeanClass(ClassLoader)
	 */
	protected Class resolveBeanClass(BeanDefinition beanDefinition)
			throws IllegalStateException {
		try {
			return ((AbstractBeanDefinition) beanDefinition)
					.resolveBeanClass(ClassUtils.getDefaultClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Could not resolve class ["
					+ beanDefinition.getBeanClassName() + "] of caching listener");
		}
	}

}
