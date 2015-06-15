/* 
 * Created on Sep 22, 2004
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

import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Advisor driven by a <code>{@link FlushingModelSource}</code> that tells
 * <code>{@link NameMatchFlushingInterceptor}</code> which methods should be
 * intercepted.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class FlushingModelSourceAdvisor extends
    StaticMethodMatcherPointcutAdvisor {

  private static final long serialVersionUID = -8490076545170797788L;

  /**
   * Retrieves instances of <code>{@link FlushingModel}</code> for intercepted
   * methods.
   */
  private FlushingModelSource flushingModelSource;

  /**
   * @param interceptor
   *          Advice that caches the returned value of intercepted methods.
   * @throws AopConfigException
   *           if the <code>FlushingAttributeSource</code> of
   *           <code>cacheInterceptor</code> is <code>null</code>.
   */
  public FlushingModelSourceAdvisor(NameMatchFlushingInterceptor interceptor) {
    super(interceptor);

    FlushingModelSource tempSource = interceptor.getFlushingModelSource();

    if (tempSource == null) {
      throw new AopConfigException("<" + interceptor.getClass().getName()
          + "> has no <" + FlushingModelSource.class.getName() + "> configured");
    }

    flushingModelSource = tempSource;
  }

  /**
   * @param method
   *          the intercepted method to verify.
   * @param targetClass
   *          the class declaring the method.
   * @return <code>true</code> if the return value of the intercepted method
   *         should be cached.
   */
  public final boolean matches(Method method, Class targetClass) {
    FlushingModel model = flushingModelSource.getFlushingModel(method,
        targetClass);

    boolean matches = (model != null);
    return matches;
  }
}