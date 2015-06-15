/* 
 * Created on Nov 10, 2004
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

package org.springmodules.cache.interceptor.caching;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

import org.springmodules.cache.CachingModel;

import org.springframework.metadata.Attributes;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Caching interceptor that internally uses a
 * <code>{@link CachingAttributeSource}</code> to retrieve caching metadata
 * attributes bound to intercepted methods.
 * </p>
 * 
 * @author Alex Ruiz
 * 
 * @see org.springmodules.cache.interceptor.caching.AbstractCachingInterceptor
 */
public class MetadataCachingInterceptor extends AbstractCachingInterceptor {

  /**
   * Retrieves metadata attributes from class methods.
   */
  private CachingAttributeSource cachingAttributeSource;

  /**
   * @return the source of caching metadata attributes for class methods
   */
  public final CachingAttributeSource getCachingAttributeSource() {
    return cachingAttributeSource;
  }

  /**
   * Sets the underlying implementation of attributes to use.
   * 
   * @param attributes
   *          the new implementation of attributes to use.
   */
  public final void setAttributes(Attributes attributes) {
    MetadataCachingAttributeSource source = new MetadataCachingAttributeSource();
    source.setAttributes(attributes);
    setCachingAttributeSource(source);
  }

  public final void setCachingAttributeSource(CachingAttributeSource s) {
    cachingAttributeSource = s;
  }

  /**
   * Returns the metadata attribute of the intercepted method.
   * 
   * @param methodInvocation
   *          the description of an invocation to the method.
   * @return the metadata attribute of the intercepted method.
   */
  protected Cached getCachingAttribute(MethodInvocation methodInvocation) {
    Object thisObject = methodInvocation.getThis();
    Class targetClass = (thisObject != null) ? thisObject.getClass() : null;
    Method method = methodInvocation.getMethod();
    Cached attribute = cachingAttributeSource.attribute(method, targetClass);
    return attribute;
  }

  /**
   * @see AbstractCachingInterceptor#model(MethodInvocation)
   */
  protected final CachingModel model(MethodInvocation methodInvocation) {
    Cached attribute = getCachingAttribute(methodInvocation);
    if (attribute == null) return null;
    String modelId = attribute.getModelId();
    if (!StringUtils.hasText(modelId)) return null;
    return (CachingModel) models().get(modelId);
  }
}