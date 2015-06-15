/* 
 * Created on Sep 24, 2004
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

package org.springmodules.cache.key;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInvocation;

/**
 * <p>
 * Generates a unique key based on the description of an invocation to an
 * intercepted method.
 * </p>
 * 
 * @author Alex Ruiz
 */
public interface CacheKeyGenerator {

  /**
   * Generates the key for a cache entry.
   * 
   * @param methodInvocation
   *          the description of an invocation to the intercepted method.
   * @return the created key.
   */
  Serializable generateKey(MethodInvocation methodInvocation);
}