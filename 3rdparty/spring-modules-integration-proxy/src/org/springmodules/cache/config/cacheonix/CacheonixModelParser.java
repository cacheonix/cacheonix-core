/*
 * Created on July 12, 2008
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
 * Copyright @2008 the original author or authors.
 */
package org.springmodules.cache.config.cacheonix;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.config.AbstractCacheModelParser;
import org.w3c.dom.Element;

import org.springmodules.cache.provider.cacheonix.CacheonixCachingModel;
import org.springmodules.cache.provider.cacheonix.CacheonixFlushingModel;

public final class CacheonixModelParser extends AbstractCacheModelParser {

   public CachingModel parseCachingModel(final Element element) {
      final String cacheName = element.getAttribute("cacheName");
      return new CacheonixCachingModel(cacheName);
   }


   protected FlushingModel doParseFlushingModel(final Element element,
                                                final boolean flushBeforeMethodExecution) {
      final String csvCacheNames = element.getAttribute("cacheNames");
      final CacheonixFlushingModel model = new CacheonixFlushingModel(csvCacheNames);
      model.setFlushBeforeMethodExecution(flushBeforeMethodExecution);
      return model;
   }
}
