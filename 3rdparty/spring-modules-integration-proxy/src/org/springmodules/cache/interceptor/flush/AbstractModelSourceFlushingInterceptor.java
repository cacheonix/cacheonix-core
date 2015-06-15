/* 
 * Created on Oct 28, 2005
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
package org.springmodules.cache.interceptor.flush;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Template for flushing interceptors that intercept the methods that have
 * flushing models associated to them.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractModelSourceFlushingInterceptor extends
    AbstractFlushingInterceptor {

  private FlushingModelSource flushingModelSource;

  /**
   * @return the source of flushing models for class methods
   */
  public FlushingModelSource getFlushingModelSource() {
    return flushingModelSource;
  }

  /**
   * Sets the source of flusshing models for class methods.
   * 
   * @param newFlushingModelSource
   *          the new source of flushing models
   */
  public void setFlushingModelSource(FlushingModelSource newFlushingModelSource) {
    flushingModelSource = newFlushingModelSource;
  }

  /**
   * @see AbstractFlushingInterceptor#getModel(MethodInvocation)
   */
  protected FlushingModel getModel(MethodInvocation methodInvocation) {
    Object thisObject = methodInvocation.getThis();
    Class targetClass = (thisObject != null) ? thisObject.getClass() : null;
    Method method = methodInvocation.getMethod();
    return flushingModelSource.getFlushingModel(method, targetClass);
  }

}
