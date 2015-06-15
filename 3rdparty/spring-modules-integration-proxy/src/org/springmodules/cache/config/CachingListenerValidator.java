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
import org.springframework.beans.factory.xml.ParserContext;

import org.springmodules.cache.interceptor.caching.CachingListener;

/**
 * <p>
 * Validates a <code>{@link BeanDefinition}</code> describing a
 * <code>{@link CachingListener}</code>.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface CachingListenerValidator {

  /**
   * Validates the given object that may be a:
   * <ul>
   * <li><code>{@link RuntimeBeanReference}</code></li>
   * <li><code>{@link BeanDefinitionHolder}</code></li>
   * </ul>
   * that references or defines a <code>{@link CachingListener}</code>
   * 
   * @param cachingListener
   *          the object to validate
   * @param index
   *          the index of the object to validate in the list of caching
   *          listeners
   * @param parserContext
   *          the parser context
   * @throws IllegalStateException
   *           if the given object does not describe a caching listener
   */
  void validate(Object cachingListener, int index, ParserContext parserContext)
      throws IllegalStateException;
}
