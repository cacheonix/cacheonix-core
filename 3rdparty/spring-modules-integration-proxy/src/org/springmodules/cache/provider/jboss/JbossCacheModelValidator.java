/* 
 * Created on Sep 6, 2005
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
 * Copyright @2005 the original author or authors.
 */
package org.springmodules.cache.provider.jboss;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;

/**
 * <p>
 * Validates the properties of <code>{@link JbossCacheCachingModel}</code>s
 * and <code>{@link JbossCacheFlushingModel}</code>s.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class JbossCacheModelValidator extends AbstractCacheModelValidator {

  /**
   * @see AbstractCacheModelValidator#getCachingModelTargetClass()
   */
  protected Class getCachingModelTargetClass() {
    return JbossCacheCachingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#getFlushingModelTargetClass()
   */
  protected Class getFlushingModelTargetClass() {
    return JbossCacheFlushingModel.class;
  }

  /**
   * @see AbstractCacheModelValidator#validateCachingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if the model does not have a node FQN.
   */
  protected void validateCachingModelProperties(Object cachingModel)
      throws InvalidCacheModelException {
    JbossCacheCachingModel model = (JbossCacheCachingModel) cachingModel;
    if (!StringUtils.hasText(model.getNode())) {
      throw new InvalidCacheModelException(
          "The FQN of the cache node should not be empty");
    }
  }

  /**
   * @see AbstractCacheModelValidator#validateFlushingModelProperties(Object)
   * @throws InvalidCacheModelException
   *           if the model does not have at least one node FQN.
   */
  protected void validateFlushingModelProperties(Object flushingModel)
      throws InvalidCacheModelException {
    JbossCacheFlushingModel model = (JbossCacheFlushingModel) flushingModel;
    String[] nodes = model.getNodes();

    if (ObjectUtils.isEmpty(nodes)) {
      throw new InvalidCacheModelException(
          "There should be at least one node FQN");
    }
  }
}
