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

import org.springmodules.cache.config.AbstractCacheProviderFacadeParser;
import org.springmodules.cache.provider.cacheonix.CacheonixFacade;

/**
 * Parses everything under "config" when using the XML "cacheonix" namespace. Registers {@link
 * CacheProviderFacade} and the cache manager in the provided registry.
 */
public final class CacheonixFacadeParser extends AbstractCacheProviderFacadeParser {

   /**
    * @see AbstractCacheProviderFacadeParser#getCacheProviderFacadeClass()
    */
   protected Class getCacheProviderFacadeClass() {
      return CacheonixFacade.class;
   }

}
