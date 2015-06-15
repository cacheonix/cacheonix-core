/* 
 * Created on Jan 19, 2005
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

package org.springmodules.cache.interceptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springmodules.cache.CacheModel;
import org.springmodules.cache.util.TextMatcher;

/**
 * <p>
 * Template that allows attributes to be matched by registered name.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractNameMatchCacheModelSource {

  /** Logger available to subclasses */
  protected final Log logger = LogFactory.getLog(getClass());

  /**
   * Stores instances of <code>{@link CacheModel}</code> implementations under
   * the name of the method associated to it.
   */
  private Map cacheModels;

  /**
   * Returns the cache model bound to the intercepted method.
   * 
   * @param method
   *          the definition of the intercepted method
   * @return the model bound to the intercepted method
   */
  protected final CacheModel getCacheModel(Method method) {
    String methodName = method.getName();
    CacheModel model = (CacheModel) cacheModels.get(methodName);

    if (model == null) {
      // look up most specific name match
      String bestNameMatch = null;

      for (Iterator i = cacheModels.keySet().iterator(); i.hasNext();) {
        String mappedMethodName = (String) i.next();

        if (isMatch(methodName, mappedMethodName)
            && (bestNameMatch == null || bestNameMatch.length() <= mappedMethodName
                .length())) {
          model = (CacheModel) cacheModels.get(mappedMethodName);
          bestNameMatch = mappedMethodName;
        }
      }
    }

    return model;
  }

  /**
   * <p>
   * Returns <code>true</code> if the given method name matches the mapped
   * name. The default implementation checks for "xxx*" and "*xxx" matches.
   * </p>
   * <p>
   * For example, this method will return <code>true</code> if the given
   * method name is &quot;getUser&quot; and the mapped name is &quot;get*&quot;
   * </p>
   * 
   * @param methodName
   *          the method name
   * @param mappedName
   *          the name in the descriptor
   * @return <code>true</code> if the names match
   */
  protected boolean isMatch(String methodName, String mappedName) {
    return TextMatcher.isMatch(methodName, mappedName);
  }

  /**
   * Sets the map of cache models to use. Each map entry uses the name of the
   * method to advise as key (a String) and the cache model to bind as value.
   * 
   * @param newCacheModels
   *          the new map of cache models
   */
  protected final void setCacheModels(Map newCacheModels) {
    cacheModels = newCacheModels;
  }
}