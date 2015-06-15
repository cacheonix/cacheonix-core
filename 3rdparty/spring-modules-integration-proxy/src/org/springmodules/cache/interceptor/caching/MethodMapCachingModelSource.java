/* 
 * Created on March 2, 2005
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

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.interceptor.MethodMapCacheModelSource;

/**
 * <p>
 * Binds a <code>{@link CachingModel}</code> to a method.
 * </p>
 * 
 * @author Xavier Dury
 * @author Alex Ruiz
 */
public final class MethodMapCachingModelSource implements CachingModelSource {

  private final MethodMapCacheModelSource source;

  public MethodMapCachingModelSource() {
    source = new MethodMapCacheModelSource();
  }

  public void addModel(CachingModel m, String fullyQualifiedMethodName) {
    source.addModel(m, fullyQualifiedMethodName);
  }

  public CachingModel model(Method m, Class c) {
    return (CachingModel) source.model(m);
  }
}