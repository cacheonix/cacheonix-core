/* 
 * Created on Oct 27, 2005
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

/**
 * <p>
 * Utility methods related to caching.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class CachingUtils {

  /**
   * Returns <code>true</code> if the return type of a method can be
   * cacheable. In order to be cacheable, the method should have a return type
   * (not <code>void</code>).
   * 
   * @param method
   *          the method definition to verify.
   * @return <code>true</code> if the return type of a method can be
   *         cacheable.
   */
  public static boolean isCacheable(Method method) {
    Class returnType = method.getReturnType();
    return !void.class.equals(returnType);
  }

}
