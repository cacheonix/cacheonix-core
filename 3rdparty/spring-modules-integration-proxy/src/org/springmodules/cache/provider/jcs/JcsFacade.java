/*
 * Created on Nov 10, 2004
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
package org.springmodules.cache.provider.jcs;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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
import org.springmodules.cache.provider.jcs.JcsFlushingModel.CacheStruct;

import java.beans.PropertyEditor;
import java.io.Serializable;

/**
 * Implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * that uses JCS as the underlying cache implementation.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class JcsFacade extends AbstractCacheProviderFacade {

	/**
	 * JCS cache manager.
	 */
	private CompositeCacheManager cacheManager;

	private CacheModelValidator cacheModelValidator;

	/**
	 * Constructor.
	 */
	public JcsFacade() {
		cacheModelValidator = new JcsModelValidator();
	}

	/**
	 * Returns the validator of cache models. It is always an instance of
	 * <code>{@link JcsModelValidator}</code>.
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
		editor.setCacheModelClass(JcsCachingModel.class);
		return editor;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
	 */
	public PropertyEditor getFlushingModelEditor() {
		return new JcsFlushingModelEditor();
	}

	/**
	 * Sets the JCS cache manager to use.
	 *
	 * @param newCacheManager the new cache manager
	 */
	public void setCacheManager(CompositeCacheManager newCacheManager) {
		cacheManager = newCacheManager;
	}

	/**
	 * Returns a JCS cache from the cache manager.
	 *
	 * @param name the name of the cache.
	 * @return the cache retrieved from the cache manager.
	 * @throws CacheNotFoundException if the cache does not exist.
	 */
	protected CompositeCache getCache(String name) {
		CompositeCache cache = cacheManager.getCache(name);
		if (cache == null) {
			throw new CacheNotFoundException(name);
		}

		return cache;
	}

	/**
	 * Returns the key of a cache entry. JCS needs to store group information (if
	 * any) with the entry key.
	 *
	 * @param key   the entry key.
	 * @param model the model that specifies how to retrieve or store an entry.
	 * @return the key of a cache entry containing group information.
	 */
	protected Serializable getKey(Serializable key, JcsCachingModel model) {
		Serializable newKey = key;

		String group = model.getGroup();
		if (StringUtils.hasText(group)) {
			GroupId groupId = new GroupId(model.getCacheName(), group);
			newKey = new GroupAttrName(groupId, key);
		}

		return newKey;
	}

	/**
	 * @return <code>true</code>. JCS can only store Serializable objects
	 * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
	 */
	protected boolean isSerializableCacheElementRequired() {
		return true;
	}

	/**
	 * Removes all the entries in the groups specified in the given flushing
	 * model. If a cache is specified without groups, the whole cache is flushed.
	 * The flushing model should be an instance of
	 * <code>{@link JcsFlushingModel}</code>.
	 *
	 * @param model the flushing model.
	 * @throws CacheNotFoundException if the cache specified in the given model cannot be found.
	 * @throws CacheAccessException   wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
	 */
	protected void onFlushCache(FlushingModel model) throws CacheException {
		JcsFlushingModel flushingModel = (JcsFlushingModel) model;

		CacheStruct[] cacheStructs = flushingModel.getCacheStructs();
		if (cacheStructs == null) {
			return;
		}

		try {
			int structCount = cacheStructs.length;

			for (int i = 0; i < structCount; i++) {
				CacheStruct cacheStruct = cacheStructs[i];
				if (cacheStruct == null) {
					continue;
				}

				String cacheName = cacheStruct.getCacheName();
				String[] groups = cacheStruct.getGroups();

				CompositeCache cache = getCache(cacheName);

				if (!ObjectUtils.isEmpty(groups)) {
					int groupCount = groups.length;
					for (int j = 0; j < groupCount; j++) {
						GroupId groupId = new GroupId(cacheName, groups[j]);
						cache.remove(groupId);
					}
				} else {
					cache.removeAll();
				}
			}
		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * Retrieves an object stored under the given key from the cache (and group,
	 * if any) specified in the given caching model. The caching model should be
	 * an instance of <code>{@link JcsCachingModel}</code>.
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
		JcsCachingModel cachingModel = (JcsCachingModel) model;
		String cacheName = cachingModel.getCacheName();

		CompositeCache cache = getCache(cacheName);

		Serializable newKey = getKey(key, cachingModel);
		Object cachedObject = null;

		try {
			ICacheElement cacheElement = cache.get(newKey);
			if (cacheElement != null) {
				cachedObject = cacheElement.getVal();
			}

		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}

		return cachedObject;
	}

	/**
	 * Stores the given object under the given key in the cache (and group, if
	 * any) specified in the given caching model. The caching model should be an
	 * instance of <code>{@link JcsCachingModel}</code>.
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

		JcsCachingModel cachingModel = (JcsCachingModel) model;
		String cacheName = cachingModel.getCacheName();

		CompositeCache cache = getCache(cacheName);

		Serializable newKey = getKey(key, cachingModel);
		ICacheElement newCacheElement = new CacheElement(cache.getCacheName(),
				newKey, obj);

		IElementAttributes elementAttributes = cache.getElementAttributes().copy();
		newCacheElement.setElementAttributes(elementAttributes);

		try {
			cache.update(newCacheElement);

		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * Removes the object stored under the given key from the cache (and group, if
	 * any) specified in the given caching model. The caching model should be an
	 * instance of <code>{@link JcsCachingModel}</code>.
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
		JcsCachingModel cachingModel = (JcsCachingModel) model;
		String cacheName = cachingModel.getCacheName();

		CompositeCache cache = getCache(cacheName);

		Serializable newKey = getKey(key, cachingModel);

		try {
			cache.remove(newKey);

		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * @throws FatalCacheException if the cache manager is <code>null</code>.
	 * @see AbstractCacheProviderFacade#validateCacheManager()
	 */
	protected void validateCacheManager() throws FatalCacheException {
		assertCacheManagerIsNotNull(cacheManager);
	}

}