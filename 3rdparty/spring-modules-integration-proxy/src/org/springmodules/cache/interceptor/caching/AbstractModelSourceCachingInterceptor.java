/* 
 * Created on Oct 3, 2005
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
package org.springmodules.cache.interceptor.caching;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

import org.springmodules.cache.CachingModel;

/**
 * <p>
 * Template for caching interceptors that intercept the methods that have
 * caching models bound to them.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractModelSourceCachingInterceptor extends
    AbstractCachingInterceptor {

  private CachingModelSource cachingModelSource;

  /**
   * @return the source of caching models for class methods
   */
  public final CachingModelSource getCachingModelSource() {
    return cachingModelSource;
  }

  /**
   * Sets the source of caching models for class methods.
   * 
   * @param newCachingModelSource
   *          the new source of caching models
   */
  public final void setCachingModelSource(
      CachingModelSource newCachingModelSource) {
    cachingModelSource = newCachingModelSource;
  }

  /**
   * @see AbstractCachingInterceptor#model(MethodInvocation)
   */
  protected final CachingModel model(MethodInvocation methodInvocation) {
    Object thisObject = methodInvocation.getThis();
    Class targetClass = (thisObject != null) ? thisObject.getClass() : null;
    Method method = methodInvocation.getMethod();
    return cachingModelSource.model(method, targetClass);
  }

}
