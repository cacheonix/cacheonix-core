/* 
 * Created on Sep 21, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.interceptor.caching;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import org.springmodules.cache.CacheAttribute;
import org.springmodules.cache.interceptor.MetadataCacheAttributeSource;
import org.springmodules.cache.interceptor.MetadataCacheAttributeSource.MetadataFinder;

import org.springframework.metadata.Attributes;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * Binds caching metadata attributes to methods.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class MetadataCachingAttributeSource implements
    CachingAttributeSource {

  Attributes attributes;

  private final MetadataFinder finder = new MetadataFinder() {
    public CacheAttribute find(Method m) {
      return find(attributes.getAttributes(m));
    }

    private CacheAttribute find(Collection methodAttributes) {
      if (CollectionUtils.isEmpty(methodAttributes)) return null;
      for (Iterator i = methodAttributes.iterator(); i.hasNext();) {
        Object attribute = i.next();
        if (attribute instanceof Cached) return (Cached)attribute;
      }
      return null;
    }
  };

  private final MetadataCacheAttributeSource source;

  public MetadataCachingAttributeSource() {
    source = new MetadataCacheAttributeSource(finder);
  }

  public Cached attribute(Method m, Class t) {
    if (!CachingUtils.isCacheable(m)) return null;
    return (Cached)source.attribute(m, t);
  }

  public void setAttributes(Attributes a) {
    attributes = a;
  }
}