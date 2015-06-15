/*
 * Created on Sep 1, 2005
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
package org.springmodules.cache.provider.jboss;

import org.jboss.cache.TreeCache;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheAccessException;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.ObjectCannotBeCachedException;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * that uses JBoss Cache as the underlying cache implementation
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class JbossCacheFacade extends AbstractCacheProviderFacade {

	private TreeCache cacheManager;

	private CacheModelValidator cacheModelValidator;

	/**
	 * Constructor.
	 */
	public JbossCacheFacade() {
		super();
		cacheModelValidator = new JbossCacheModelValidator();
	}

	/**
	 * Returns the validator of cache models. It is always an instance of
	 * <code>{@link JbossCacheModelValidator}</code>.
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
		editor.setCacheModelClass(JbossCacheCachingModel.class);
		return editor;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
	 */
	public PropertyEditor getFlushingModelEditor() {
		Map propertyEditors = new HashMap();
		propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(JbossCacheFlushingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
	}

	/**
	 * Sets the JBossCache cache manager to use.
	 *
	 * @param newCacheManager the new cache manager
	 */
	public void setCacheManager(TreeCache newCacheManager) {
		cacheManager = newCacheManager;
	}

	/**
	 * @return <code>true</code>. Serializable entries are not necessary if the
	 *         cache is local (not replicated). It is recommended to use
	 *         serializable objects to enable users to change the cache mode at
	 *         any time.
	 * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
	 */
	protected boolean isSerializableCacheElementRequired() {
		return true;
	}

	/**
	 * Removes all the nodes which FQNs are specified in the given flushing model.
	 * The flushing model should be an instance of
	 * <code>{@link JbossCacheFlushingModel}</code>.
	 *
	 * @param model the flushing model
	 * @throws CacheAccessException wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
	 */
	protected void onFlushCache(FlushingModel model) {
		JbossCacheFlushingModel flushingModel = (JbossCacheFlushingModel) model;

		String[] nodeFqns = flushingModel.getNodes();

		if (nodeFqns != null) {
			int fqnCount = nodeFqns.length;

			try {
				for (int i = 0; i < fqnCount; i++) {
					cacheManager.remove(nodeFqns[i]);
				}
			} catch (Exception exception) {
				throw new CacheAccessException(exception);
			}
		}
	}

	/**
	 * Retrieves an object stored under the given key from the node specified in
	 * the given caching model. The caching model should be an instance of
	 * <code>{@link JbossCacheCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @return the object retrieved from the cache. Can be <code>null</code>.
	 * @throws CacheAccessException wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onGetFromCache(Serializable,CachingModel)
	 */
	protected Object onGetFromCache(Serializable key, CachingModel model) {
		JbossCacheCachingModel cachingModel = (JbossCacheCachingModel) model;

		Object cachedObject = null;

		try {
			cachedObject = cacheManager.get(cachingModel.getNode(), key);
		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
		return cachedObject;
	}

	/**
	 * Stores the given object under the given key in the node specified in the
	 * given caching model. The caching model should be an instance of
	 * <code>{@link JbossCacheCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @param obj   the object to store in the cache
	 * @throws ObjectCannotBeCachedException if the object to store is not an implementation of
	 *                                       <code>java.io.Serializable</code>.
	 * @throws CacheAccessException		  wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onPutInCache(Serializable,CachingModel,
	 *Object)
	 */
	protected void onPutInCache(Serializable key, CachingModel model, Object obj) {
		JbossCacheCachingModel cachingModel = (JbossCacheCachingModel) model;

		try {
			cacheManager.put(cachingModel.getNode(), key, obj);
		} catch (Exception exception) {
			throw new CacheAccessException(exception);
		}
	}

	/**
	 * Removes the object stored under the given key from the node specified in
	 * the given caching model. The caching model should be an instance of
	 * <code>{@link JbossCacheCachingModel}</code>.
	 *
	 * @param key   the key of the cache entry
	 * @param model the caching model
	 * @throws CacheAccessException wrapping any unexpected exception thrown by the cache.
	 * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable,
	 *CachingModel)
	 */
	protected void onRemoveFromCache(Serializable key, CachingModel model) {
		JbossCacheCachingModel cachingModel = (JbossCacheCachingModel) model;

		try {
			cacheManager.remove(cachingModel.getNode(), key);
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
