/* 
 * Created on Mar 16, 2006
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
package org.springmodules.cache.config.jboss;

import org.w3c.dom.Element;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.config.AbstractCacheModelParser;
import org.springmodules.cache.config.CacheModelParser;
import org.springmodules.cache.provider.jboss.JbossCacheCachingModel;
import org.springmodules.cache.provider.jboss.JbossCacheFlushingModel;

/**
 * <p>
 * Creates instances of <code>{@link JbossCacheCachingModel}</code> and
 * <code>{@link JbossCacheFlushingModel}</code> from a given XML element.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class JbossCacheModelParser extends AbstractCacheModelParser {

  /**
   * Creates a <code>{@link JbossCacheCachingModel}</code> from the given XML
   * element.
   * 
   * @param element
   *          the XML element to parse
   * @return the created caching model
   * 
   * @see CacheModelParser#parseCachingModel(Element)
   */
  public CachingModel parseCachingModel(Element element) {
    String cacheName = element.getAttribute("node");
    JbossCacheCachingModel model = new JbossCacheCachingModel(cacheName);
    return model;
  }

  /**
   * Creates a <code>{@link JbossCacheFlushingModel}</code> from the given XML
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
    String csvCacheNames = element.getAttribute("nodes");
    JbossCacheFlushingModel model = new JbossCacheFlushingModel(csvCacheNames);
    model.setFlushBeforeMethodExecution(flushBeforeMethodExecution);
    return model;
  }
}
