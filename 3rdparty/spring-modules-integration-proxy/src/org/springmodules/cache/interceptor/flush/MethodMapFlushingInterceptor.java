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

import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;

/**
 * <p>
 * Caching interceptor that internally uses a
 * <code>{@link MethodMapFlushingModelSource}</code> to retrieve flushing
 * models bound to intercepted methods.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class MethodMapFlushingInterceptor extends
    AbstractModelSourceFlushingInterceptor {

  /**
   * @see AbstractFlushingInterceptor#onAfterPropertiesSet()
   */
  protected void onAfterPropertiesSet() throws FatalCacheException {
    Map flushingModels = getFlushingModels();

    if (flushingModels != null && !flushingModels.isEmpty()
        && getFlushingModelSource() == null) {
      MethodMapFlushingModelSource newSource = new MethodMapFlushingModelSource();

      String key = null;
      try {
        for (Iterator i = flushingModels.keySet().iterator(); i.hasNext();) {
          key = (String) i.next();
          FlushingModel model = (FlushingModel) flushingModels.get(key);
          newSource.addModel(model, key);
        }
      } catch (Exception exception) {
        throw new FatalCacheException(
            "Unable to add model stored under the key "
                + StringUtils.quote(key), exception);
      }
      setFlushingModelSource(newSource);
    }
  }

}
