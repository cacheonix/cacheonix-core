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
package org.springmodules.cache.interceptor.flush;

import java.lang.reflect.Method;

import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.interceptor.MethodMapCacheModelSource;

/**
 * <p>
 * Binds a <code>{@link FlushingModel}</code> to a method.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class MethodMapFlushingModelSource implements FlushingModelSource {

  private MethodMapCacheModelSource source;

  public MethodMapFlushingModelSource() {
    source = new MethodMapCacheModelSource();
  }

  public void addModel(FlushingModel m, String fullyQualifiedMethodName)
      throws IllegalArgumentException {
    source.addModel(m, fullyQualifiedMethodName);
  }

  public FlushingModel getFlushingModel(Method m, Class c) {
    return (FlushingModel) source.model(m);
  }
}