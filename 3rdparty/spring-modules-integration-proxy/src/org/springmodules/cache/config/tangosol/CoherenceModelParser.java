/* 
 * Created on Jan 21, 2006
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
package org.springmodules.cache.config.tangosol;

import org.w3c.dom.Element;

import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.config.AbstractCacheModelParser;
import org.springmodules.cache.config.CacheModelParser;
import org.springmodules.cache.provider.tangosol.CoherenceCachingModel;
import org.springmodules.cache.provider.tangosol.CoherenceFlushingModel;

/**
 * <p>
 * Creates instances of <code>{@link CoherenceCachingModel}</code> and
 * <code>{@link CoherenceFlushingModel}</code> from a given XML element.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class CoherenceModelParser extends AbstractCacheModelParser {

  /**
   * Creates a <code>{@link CoherenceCachingModel}</code> from the given XML
   * element.
   * 
   * @param element
   *          the XML element to parse
   * @return the created caching model
   * 
   * @see CacheModelParser#parseCachingModel(Element)
   */
  public CachingModel parseCachingModel(Element element) {
    String cacheName = element.getAttribute("cacheName");
    
    CoherenceCachingModel model = new CoherenceCachingModel(cacheName);
    
    String timeToLiveAttr = element.getAttribute("timeToLive");
    if (StringUtils.hasText(timeToLiveAttr)) {
      try {
        long timeToLive = Long.parseLong(timeToLiveAttr);
        model.setTimeToLive(timeToLive);
      }
      catch (NumberFormatException exception) {
        // ignore exception
      }
    }
    
    return model;
  }

  /**
   * Creates a <code>{@link CoherenceFlushingModel}</code> from the given XML
   * element.
   * 
   * @param element
   *          the XML element to parse
   * @param flushBeforeMethodExecution
   *          indicates if the cache should be flushed before or after the
   *          execution of the intercepted method
   * @return the created flushing model
   * 
   * @see AbstractCacheModelParser#doParseFlushingModel(Element, boolean)
   */
  protected FlushingModel doParseFlushingModel(Element element,
      boolean flushBeforeMethodExecution) {
    String csvCacheNames = element.getAttribute("cacheNames");
    CoherenceFlushingModel model = new CoherenceFlushingModel(csvCacheNames);
    model.setFlushBeforeMethodExecution(flushBeforeMethodExecution);
    return model;
  }
}
