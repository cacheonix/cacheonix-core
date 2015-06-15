/* 
 * Created on Oct 4, 2005
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

import java.util.Map;

import org.springmodules.cache.FatalCacheException;

/**
 * <p>
 * Flushing interceptor that internally uses a
 * <code>{@link NameMatchFlushingModelSource}</code> to retrieve flushing
 * models bound to intercepted methods.
 * </p>
 * 
 * @author Alex Ruiz
 * 
 * @see org.springmodules.cache.interceptor.flush.AbstractFlushingInterceptor
 */
public final class NameMatchFlushingInterceptor extends
    AbstractModelSourceFlushingInterceptor {

  /**
   * @see AbstractFlushingInterceptor#onAfterPropertiesSet()
   */
  protected void onAfterPropertiesSet() throws FatalCacheException {
    Map flushingModels = getFlushingModels();

    if (flushingModels != null && !flushingModels.isEmpty()) {
      if (getFlushingModelSource() == null) {
        NameMatchFlushingModelSource newSource = new NameMatchFlushingModelSource();
        newSource.setFlushingModels(getFlushingModels());
        setFlushingModelSource(newSource);
      }
    }
  }

}
