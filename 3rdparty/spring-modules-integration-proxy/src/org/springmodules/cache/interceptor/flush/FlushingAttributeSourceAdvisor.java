/* 
 * Created on Oct 21, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.interceptor.flush;

import java.lang.reflect.Method;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * <p>
 * Advisor driven by a <code>{@link FlushingAttributeSource}</code> that tells
 * <code>{@link MetadataFlushingInterceptor}</code> the methods that should be
 * intercepted.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class FlushingAttributeSourceAdvisor extends
    StaticMethodMatcherPointcutAdvisor {

  private static final long serialVersionUID = 3256442525337466931L;

  /**
   * Retrieves instances of <code>{@link FlushCache}</code> for intercepted
   * methods.
   */
  private FlushingAttributeSource cacheFlushAttributeSource;

  /**
   * @param cacheInterceptor
   *          Advice that caches the returned values of intercepted methods.
   * @throws AopConfigException
   *           if the <code>CacheFlushAttributeSource</code> of
   *           <code>cacheInterceptor</code> is <code>null</code>.
   */
  public FlushingAttributeSourceAdvisor(
      MetadataFlushingInterceptor cacheInterceptor) {

    super(cacheInterceptor);

    FlushingAttributeSource tempAttributeSource = cacheInterceptor
        .getFlushingAttributeSource();

    if (tempAttributeSource == null) {
      throw new AopConfigException(
          "Cannot construct a CacheFlushAttributeSourceAdvisor using a "
              + "CacheFlushInterceptor that has no CacheFlushAttributeSource configured");
    }

    cacheFlushAttributeSource = tempAttributeSource;
  }

  /**
   * Returns <code>true</code> if the intercepted method should flush the
   * cache.
   * 
   * @param method
   *          the intercepted method to verify.
   * @param targetClass
   *          the class declaring the method.
   * @return <code>true</code> if the specified method should flush the cache.
   */
  public final boolean matches(Method method, Class targetClass) {

    FlushCache attribute = cacheFlushAttributeSource.attribute(
        method, targetClass);

    boolean matches = (attribute != null);
    return matches;
  }

}