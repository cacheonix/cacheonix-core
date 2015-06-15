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
package org.springmodules.cache.provider.cacheonix;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.ObjectUtils;
import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheAccessException;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.CacheNotFoundException;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;
import org.springmodules.util.Objects;

public final class CacheonixFacade extends AbstractCacheProviderFacade {

   private CacheModelValidator cacheModelValidator;
   private Map caches = new HashMap(11);

   private Object cacheManager;

   public CacheonixFacade() {
      cacheModelValidator = new CacheonixModelValidator();
   }


   public CacheModelValidator modelValidator() {
      return cacheModelValidator;
   }


   /**
    * @see org.springmodules.cache.provider.CacheProviderFacade#getCachingModelEditor()
    */
   public PropertyEditor getCachingModelEditor() {
      final ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
      editor.setCacheModelClass(CacheonixCachingModel.class);
      return editor;
   }


   /**
    * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
    */
   public PropertyEditor getFlushingModelEditor() {
      final Map propertyEditors = new HashMap(11);
      propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

      final ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
      editor.setCacheModelClass(CacheonixFlushingModel.class);
      editor.setCacheModelPropertyEditors(propertyEditors);
      return editor;
   }

   public synchronized void setCacheManager(Object newCacheManager) {
       cacheManager = newCacheManager;
   }

   protected CacheProxy getCache(final CachingModel model) throws CacheNotFoundException, CacheAccessException {
      final CacheonixCachingModel cacheonixCachingModel = (CacheonixCachingModel)model;
      final String cacheName = cacheonixCachingModel.getCacheName();
      return getCache(cacheName);
   }


   protected CacheProxy getCache(final String name) throws CacheNotFoundException, CacheAccessException {
      CacheProxy cache = (CacheProxy)caches.get(name);
      try {
         if (cache == null) {
            if (cacheManager == null)
            {
                setCacheManager(CacheManagerProxy.getInstance());
            }
            Object realCache = CacheManagerProxy.getCache(cacheManager, name);
            if (realCache != null)
            {
                cache = new CacheProxy(realCache);
                caches.put(name, cache);
            }
         }
      } catch (Exception exception) {
         throw new CacheAccessException(exception);
      }

      if (cache == null) {
         throw new CacheNotFoundException(name);
      }
      
      return cache;
   }


   /**
    * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
    */
   protected boolean isSerializableCacheElementRequired() {
      return false;
   }


   /**
    * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
    */
   protected void onFlushCache(final FlushingModel model) throws CacheException {
      final CacheonixFlushingModel flushingModel = (CacheonixFlushingModel)model;
      final String[] cacheNames = flushingModel.getCacheNames();

      if (!ObjectUtils.isEmpty(cacheNames)) {
         CacheException cacheException = null;
         final int nameCount = cacheNames.length;

         try {
            for (int i = 0; i < nameCount; i++) {
               final CacheProxy cache = getCache(cacheNames[i]);
               cache.clear();
            }
         } catch (CacheException exception) {
            cacheException = exception;
         } catch (Exception exception) {
            cacheException = new CacheAccessException(exception);
         }

         if (cacheException != null) {
            throw cacheException;
         }
      }
   }


   /**
    * @see AbstractCacheProviderFacade#onGetFromCache(Serializable,CachingModel)
    */
   protected Object onGetFromCache(final Serializable key,
                                   final CachingModel model) throws CacheException {
      final CacheProxy cache = getCache(model);
      final Object cachedObject;

      try {
         cachedObject = cache.get(key);
      } catch (Exception exception) {
         throw new CacheAccessException(exception);
      }
      return cachedObject;
   }


   /**
    * @see AbstractCacheProviderFacade#onPutInCache(Serializable,CachingModel, Object)
    */
   protected void onPutInCache(final Serializable key, final CachingModel model,
                               final Object obj) throws CacheException {
      try {
         final CacheonixCachingModel cacheonixCachingModel = (CacheonixCachingModel)model;
         final CacheProxy cache = getCache(cacheonixCachingModel);
         cache.put(key, obj);
      } catch (Exception exception) {
         throw new CacheAccessException(exception);
      }
   }


   /**
    * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable, CachingModel)
    */
   protected void onRemoveFromCache(final Serializable key,
                                    final CachingModel model) throws CacheException {
      final CacheProxy cache = getCache(model);
      try {
         cache.remove(key);
      } catch (Exception exception) {
         throw new CacheAccessException(exception);
      }
   }


   /**
    * @see AbstractCacheProviderFacade#validateCacheManager()
    */
   protected void validateCacheManager() throws FatalCacheException {
   }


   public String toString() {
      return Objects.identityToString(this)
      .append("[caches=")
      .append(caches)
      .append("]")
      .toString();
   }
}