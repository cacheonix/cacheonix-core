/* 
 * Created on Apr 21, 2006
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
 * Copyright @2006 the original author or authors.
 */
package org.springmodules.cache.interceptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.springmodules.cache.CacheModel;
import org.springmodules.cache.util.TextMatcher;

/**
 * TODO Describe this class
 * 
 * @author Alex Ruiz
 */
public final class NameMatchCacheModelSource {

  private Map models;

  public void setModels(Map m) {
    models = m;
  }

  public CacheModel model(Method m) {
    String key = m.getName();
    CacheModel model = model(key);
    if (model != null) return model;
    return mostSpecificModel(key);
  }

  private CacheModel mostSpecificModel(String method) {
    CacheModel model = null;
    String bestMatch = null;
    for (Iterator i = models.keySet().iterator(); i.hasNext();) {
      String mapped = (String)i.next();
      if (!mostSpecificMethodFound(method, bestMatch, mapped)) continue;
      model = model(mapped);
      bestMatch = mapped;
    }
    return model;
  }

  private boolean mostSpecificMethodFound(String method, String bestMatch,
      String mapped) {
    return TextMatcher.isMatch(method, mapped)
        && (bestMatch == null || bestMatch.length() <= mapped.length());
  }

  private CacheModel model(String key) {
    return (CacheModel)models.get(key);
  }
}
