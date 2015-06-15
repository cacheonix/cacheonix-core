/*
 * Creted on Jan 25, 2006
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
 * Copyright @2007 the original author or authors.
 */
package org.springmodules.cache.provider.tangosol;

import com.tangosol.net.NamedCache;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * that uses Tangosol Coherence as the underlying cache implementation.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class CoherenceFacade extends AbstractCacheProviderFacade {

	private static final String CACHE_FACTORY_CLASS_NAME = "com.tangosol.net.CacheFactory";

	private static final String GET_CACHE_METHOD_NAME = "getCache";

	private final Method getCacheMethod;

	private CacheModelValidator cacheModelValidator;

	/**
	 * Constructor.
	 */
	public CoherenceFacade() {
		try {
			Class clazz = ClassUtils.forName(CACHE_FACTORY_CLASS_NAME);
			this.getCacheMethod = ReflectionUtils.findMethod(clazz, GET_CACHE_METHOD_NAME, new Class[]{String.class});
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Could not initialize CoherenceFacade because Tangosol API classes are not available: " + ex);
		}

		this.cacheModelValidator = new CoherenceModelValidator();
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#modelValidator()
	 */
	public CacheModelValidator modelValidator() {
		return this.cacheModelValidator;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getCachingModelEditor()
	 */
	public PropertyEditor getCachingModelEditor() {
		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(CoherenceCachingModel.class);
		return editor;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
	 */
	public PropertyEditor getFlushingModelEditor() {
		Map propertyEditors = new HashMap();
		propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(CoherenceFlushingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
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
	protected void onFlushCache(FlushingModel model) throws CacheException {
		CoherenceFlushingModel coherenceFlushingModel = (CoherenceFlushingModel) model;
		String[] cacheNames = coherenceFlushingModel.getCacheNames();
		if (!ObjectUtils.isEmpty(cacheNames)) {
			int count = cacheNames.length;
			for (int i = 0; i < count; i++) {
				getCache(cacheNames[0]).clear();
			}
		}
	}

	/**
	 * @see AbstractCacheProviderFacade#onGetFromCache(Serializable,CachingModel)
	 */
	protected Object onGetFromCache(Serializable key, CachingModel model)
			throws CacheException {
		NamedCache cache = getCache(model);
		return cache.get(key);
	}

	/**
	 * @see AbstractCacheProviderFacade#onPutInCache(Serializable,CachingModel,
	 *Object)
	 */
	protected void onPutInCache(Serializable key, CachingModel model, Object obj)
			throws CacheException {
		CoherenceCachingModel coherenceCachingModel = (CoherenceCachingModel) model;
		String name = coherenceCachingModel.getCacheName();
		NamedCache cache = getCache(name);

		Long timeToLive = coherenceCachingModel.getTimeToLive();
		if (timeToLive != null) {
			cache.put(key, obj, timeToLive.longValue());
		} else {
			cache.put(key, obj);
		}
	}

	/**
	 * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable,
	 *CachingModel)
	 */
	protected void onRemoveFromCache(Serializable key, CachingModel model)
			throws CacheException {
		NamedCache cache = getCache(model);
		cache.remove(key);
	}

	/**
	 * @see AbstractCacheProviderFacade#validateCacheManager()
	 */
	protected void validateCacheManager() throws FatalCacheException {
		// No implementation.
	}

	/**
	 * Returns a Coherence cache from the cache manager.
	 *
	 * @param model the caching model containing the name of the cache to retrieve
	 * @return the cache retrieved from the cache manager
	 */
	private NamedCache getCache(CachingModel model) {
		CoherenceCachingModel coherenceCachingModel = (CoherenceCachingModel) model;
		String name = coherenceCachingModel.getCacheName();
		return getCache(name);
	}

	/**
	 * Returns a Coherence cache from the cache manager.
	 *
	 * @param name the name of the cache to retrieve
	 * @return the cache retrieved from the cache manager
	 */
	private NamedCache getCache(String name) {
		return (NamedCache) ReflectionUtils.invokeMethod(getCacheMethod, null, new String[]{name});
	}

}