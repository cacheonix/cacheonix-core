/*
* Copyright 2006 GigaSpaces, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springmodules.cache.provider.gigaspaces;

import com.j_spaces.map.CacheFinder;
import com.j_spaces.map.IMap;
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
import org.springmodules.cache.provider.ObjectCannotBeCachedException;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * that uses GigaSpaces as the underlying cache implementation.
 *
 * @author Omar Irbouh
 * @author Lior Ben Yizhak
 */
public final class GigaSpacesFacade extends AbstractCacheProviderFacade {


	private CacheModelValidator cacheModelValidator;
	private Map cachesHolder = new HashMap();

	/**
	 * Constructor.
	 */
	public GigaSpacesFacade() {
		super();
		cacheModelValidator = new GigaSpacesModelValidator();
	}

	/**
	 * Returns the validator of cache models. It is always an instance of
	 * <code>{@link GigaSpacesModelValidator}</code>.
	 *
	 * @return the validator of cache models
	 */
	public CacheModelValidator modelValidator() {
		return cacheModelValidator;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getCachingModelEditor()
	 */
	public PropertyEditor getCachingModelEditor() {
		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(GigaSpacesCachingModel.class);
		return editor;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
	 */
	public PropertyEditor getFlushingModelEditor() {
		Map propertyEditors = new HashMap();
		propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(GigaSpacesFlushingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
	}

	/**
	 * Returns a GigaSpaces cache from the cache manager.
	 *
	 * @param model the model containing the name of the cache to retrieve
	 * @return the cache retrieved from the cache manager
	 * @throws CacheNotFoundException if the cache does not exist
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache
	 */
	protected IMap getCache(CachingModel model) throws CacheNotFoundException,
			CacheAccessException {
		GigaSpacesCachingModel gigaSpacesCachingModel = (GigaSpacesCachingModel) model;
		String cacheName = gigaSpacesCachingModel.getCacheName();
		return getCache(cacheName);
	}

	/**
	 * Returns a GigaSpaces cache from the cache manager.
	 *
	 * @param name the name of the cache
	 * @return the cache retrieved from the cache manager
	 * @throws CacheNotFoundException if the cache does not exist
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache
	 */
	protected IMap getCache(String name) throws CacheNotFoundException,
			CacheAccessException {
		IMap cache = (IMap) cachesHolder.get(name);
		try {
			if (cache == null) {
				cache = (IMap) CacheFinder.find(name);
				cachesHolder.put(name, cache);
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
	 * @return <code>true</code>. GigaSpaces can only store Serializable objects
	 * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
	 */
	protected boolean isSerializableCacheElementRequired() {
		return false;
	}

	/**
	 * Removes all the entries in the caches specified in the given flushing
	 * model. The flushing model should be an instance of
	 * <code>{@link GigaSpacesFlushingModel}</code>.
	 *
	 * @param model the flushing model.
	 * @throws CacheNotFoundException if the cache specified in the given model cannot be found.
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
	 */
	protected void onFlushCache(FlushingModel model) throws CacheException {
		GigaSpacesFlushingModel flushingModel = (GigaSpacesFlushingModel) model;
		String[] cacheNames = flushingModel.getCacheNames();

		if (!ObjectUtils.isEmpty(cacheNames)) {
			CacheException cacheException = null;
			int nameCount = cacheNames.length;

			try {
				for (int i = 0; i < nameCount; i++) {
					IMap cache = getCache(cacheNames[i]);
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
	 * Retrieves an object stored under the given key from the cache specified in
	 * the given caching model. The caching model should be an instance of
	 * <code>{@link GigaSpacesCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @return the object retrieved from the cache. Can be <code>null</code>.
	 * @throws CacheNotFoundException if the cache specified in the given model cannot be found.
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onGetFromCache(Serializable,CachingModel)
	 */
	protected Object onGetFromCache(Serializable key, CachingModel model)
			throws CacheException {
		IMap cache = getCache(model);
		GigaSpacesCachingModel gigaSpacesCachingModel = (GigaSpacesCachingModel) model;
		Object cachedObject;

		try {
			Long waitForResponse = gigaSpacesCachingModel.getWaitForResponse();
			if (waitForResponse != null) {
				cachedObject = cache.get(key, waitForResponse.longValue());
			} else {
				cachedObject = cache.get(key);
			}
		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
		return cachedObject;
	}

	/**
	 * Stores the given object under the given key in the cache specified in the
	 * given caching model. The caching model should be an instance of
	 * <code>{@link GigaSpacesCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @param obj   the object to store in the cache
	 * @throws ObjectCannotBeCachedException if the object to store is not an implementation of
	 *                                       <code>java.io.Serializable</code>.
	 * @throws CacheNotFoundException		if the cache specified in the given model cannot be found.
	 * @throws CacheAccessException		  wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onPutInCache(Serializable,CachingModel,
	 *Object)
	 */
	protected void onPutInCache(Serializable key, CachingModel model, Object obj)
			throws CacheException {
		try {
			GigaSpacesCachingModel gigaSpacesCachingModel = (GigaSpacesCachingModel) model;
			IMap cache = getCache(gigaSpacesCachingModel);
			Long timeToLive = gigaSpacesCachingModel.getTimeToLive();
			if (timeToLive != null) {
				cache.put(key, obj, timeToLive.longValue());
			} else {
				cache.put(key, obj);
			}
		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * Removes the object stored under the given key from the cache specified in
	 * the given caching model. The caching model should be an instance of
	 * <code>{@link GigaSpacesCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @throws CacheNotFoundException if the cache specified in the given model cannot be found.
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable,
	 *CachingModel)
	 */
	protected void onRemoveFromCache(Serializable key, CachingModel model)
			throws CacheException {
		IMap cache = getCache(model);
		try {
			cache.remove(key);

		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * @throws FatalCacheException if the cache manager is <code>null</code>.
	 * @see AbstractCacheProviderFacade#validateCacheManager()
	 */
	protected void validateCacheManager() throws FatalCacheException {
		// No implementation.
	}

}