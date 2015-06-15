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
package org.springmodules.cache.config.oscache;

import org.w3c.dom.Element;

import org.springframework.util.StringUtils;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.config.AbstractCacheModelParser;
import org.springmodules.cache.config.CacheModelParser;
import org.springmodules.cache.provider.oscache.OsCacheCachingModel;
import org.springmodules.cache.provider.oscache.OsCacheFlushingModel;

/**
 * <p>
 * Creates instances of <code>{@link OsCacheCachingModel}</code> and
 * <code>{@link OsCacheFlushingModel}</code> from a given XML element.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class OsCacheModelParser extends AbstractCacheModelParser {

  /**
   * Creates a <code>{@link OsCacheCachingModel}</code> from the given XML
   * element.
   * 
   * @param element
   *          the XML element to parse
   * @return the created caching model
   * 
   * @see CacheModelParser#parseCachingModel(Element)
   */
  public CachingModel parseCachingModel(Element element) {
    String cronExpression = element.getAttribute("cronExpression");
    String groups = element.getAttribute("groups");

    OsCacheCachingModel model = new OsCacheCachingModel(groups, cronExpression);

    String refreshPeriodAttr = element.getAttribute("refreshPeriod");
    if (StringUtils.hasText(refreshPeriodAttr)) {
      try {
        int refreshPeriod = Integer.parseInt(refreshPeriodAttr);
        model.setRefreshPeriod(refreshPeriod);

      } catch (NumberFormatException exception) {
        // ignore exception;
      }
    }

    return model;
  }

  /**
   * Creates a <code>{@link OsCacheFlushingModel}</code> from the given XML
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
    String csvGroups = element.getAttribute("groups");
    OsCacheFlushingModel model = new OsCacheFlushingModel(csvGroups);
    model.setFlushBeforeMethodExecution(flushBeforeMethodExecution);
    return model;
  }
}
