/* 
 * Created on Apr 29, 2005
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
package org.springmodules.cache.annotations;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springmodules.cache.CacheAttribute;
import org.springmodules.cache.interceptor.MetadataCacheAttributeSource;
import org.springmodules.cache.interceptor.MetadataCacheAttributeSource.MetadataFinder;
import org.springmodules.cache.interceptor.flush.FlushCache;
import org.springmodules.cache.interceptor.flush.FlushingAttributeSource;

/**
 * Binds cache-flushing metadata annotations to methods.
 * 
 * @author Alex Ruiz
 */
public class AnnotationFlushingAttributeSource implements
    FlushingAttributeSource {

  private final MetadataFinder finder = new MetadataFinder() {
    public CacheAttribute find(Method m) {
      return find(m.getAnnotations());
    }

    private CacheAttribute find(Annotation[] annotations) {
      if (isEmpty(annotations)) return null;
      for (Annotation a : annotations)
        if (a instanceof CacheFlush) return attribute((CacheFlush)a);
      return null;
    }

    private CacheAttribute attribute(CacheFlush a) {
      return new FlushCache(a.modelId());
    }
  };

  private final MetadataCacheAttributeSource source;

  public AnnotationFlushingAttributeSource() {
    source = new MetadataCacheAttributeSource(finder);
  }

  public FlushCache attribute(Method m, Class t) {
    return (FlushCache)source.attribute(m, t);
  }
}
