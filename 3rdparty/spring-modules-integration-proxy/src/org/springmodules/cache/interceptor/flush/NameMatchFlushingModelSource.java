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
package org.springmodules.cache.interceptor.flush;

import java.lang.reflect.Method;
import java.util.Map;

import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.interceptor.AbstractNameMatchCacheModelSource;

/**
 * <p>
 * Simple implementation of <code>{@link FlushingModelSource}</code> that
 * allows <code>{@link FlushingModel}</code> to be matched by registered name.
 * </p>
 * 
 * @author Alex Ruiz
 */
public class NameMatchFlushingModelSource extends
    AbstractNameMatchCacheModelSource implements FlushingModelSource {

  /**
   * @see FlushingModelSource#getFlushingModel(Method, Class)
   */
  public FlushingModel getFlushingModel(Method method, Class targetClass) {
    return (FlushingModel) getCacheModel(method);
  }

  /**
   * Sets the map of flushing models to use. Each map entry uses the name of the
   * method to advise as key (a String) and the flushing model to bind as value.
   * 
   * @param models
   *          the new map of flushing models
   */
  public void setFlushingModels(Map models) {
    setCacheModels(models);
  }
}
