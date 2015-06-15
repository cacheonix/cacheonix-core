/* 
 * Created on Mar 28, 2006
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
package org.springmodules.cache.config.jcs;

import java.util.List;

import org.w3c.dom.Element;

import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.config.AbstractCacheModelParser;
import org.springmodules.cache.config.CacheModelParser;
import org.springmodules.cache.provider.jcs.JcsCachingModel;
import org.springmodules.cache.provider.jcs.JcsFlushingModel;
import org.springmodules.cache.provider.jcs.JcsFlushingModel.CacheStruct;

/**
 * <p>
 * Creates instances of <code>{@link JcsCachingModel}</code> and
 * <code>{@link JcsFlushingModel}</code> from a given XML element.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class JcsModelParser extends AbstractCacheModelParser {

  /**
   * Creates a <code>{@link JcsCachingModel}</code> from the given XML
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
    String group = element.getAttribute("group");
    JcsCachingModel model = new JcsCachingModel(cacheName, group);
    return model;
  }

  /**
   * Creates a <code>{@link JcsFlushingModel}</code> from the given XML
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

    JcsFlushingModel model = new JcsFlushingModel();

    List cacheElements = DomUtils.getChildElementsByTagName(element, "cache");
    if (!CollectionUtils.isEmpty(cacheElements)) {
      int count = cacheElements.size();
      CacheStruct[] cacheStructs = new CacheStruct[count];

      for (int i = 0; i < count; i++) {
        Element cacheElement = (Element) cacheElements.get(i);
        String cacheName = cacheElement.getAttribute("name");
        String groups = cacheElement.getAttribute("groups");

        CacheStruct cache = new CacheStruct(cacheName, groups);
        cacheStructs[i] = cache;
      }

      model.setCacheStructs(cacheStructs);
    }

    model.setFlushBeforeMethodExecution(flushBeforeMethodExecution);
    return model;
  }
}
