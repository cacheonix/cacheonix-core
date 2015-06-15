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
package org.springmodules.cache.interceptor.caching;

import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;

/**
 * <p>
 * Caching interceptor that internally uses a
 * <code>{@link MethodMapCachingModelSource}</code> to retrieve caching models
 * bound to intercepted methods.
 * </p>
 * 
 * @author Alex Ruiz
 * 
 * @see org.springmodules.cache.interceptor.caching.AbstractCachingInterceptor
 */
public final class MethodMapCachingInterceptor extends
    AbstractModelSourceCachingInterceptor {

  /**
   * @see AbstractCachingInterceptor#onAfterPropertiesSet()
   */
  protected void onAfterPropertiesSet() throws FatalCacheException {
    CachingModelSource cachingModelSource = getCachingModelSource();

    if (cachingModelSource == null) {
      MethodMapCachingModelSource newSource = new MethodMapCachingModelSource();

      Map models = models();
      String key = null;
      try {
        for (Iterator i = models.entrySet().iterator(); i.hasNext();) {
          Map.Entry entry = (Map.Entry) i.next();
          key = (String) entry.getKey();
          newSource.addModel((CachingModel) entry.getValue(), key);
        }

      } catch (Exception exception) {
        throw new FatalCacheException(
            "Unable to add model stored under the key "
                + StringUtils.quote(key), exception);
      }

      setCachingModelSource(newSource);
    }
  }
}
