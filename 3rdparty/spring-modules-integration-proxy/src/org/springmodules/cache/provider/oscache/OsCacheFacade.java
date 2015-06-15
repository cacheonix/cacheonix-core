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
package org.springmodules.cache.provider.oscache;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.ObjectUtils;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of
 * <code>{@link org.springmodules.cache.provider.CacheProviderFacade}</code>
 * that uses OSCache as the underlying cache implementation
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class OsCacheFacade extends AbstractCacheProviderFacade {

	/**
	 * OSCache cache manager.
	 */
	private GeneralCacheAdministrator cacheManager;

	private CacheModelValidator cacheModelValidator;

	/**
	 * Constructor.
	 */
	public OsCacheFacade() {
		super();
		cacheModelValidator = new OsCacheModelValidator();
	}

	/**
	 * Returns the validator of cache models. It is always an instance of
	 * <code>{@link OsCacheModelValidator}</code>.
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
		Map propertyEditors = new HashMap();
		propertyEditors.put("refreshPeriod", new RefreshPeriodEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(OsCacheCachingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
	}

	/**
	 * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
	 */
	public PropertyEditor getFlushingModelEditor() {
		Map propertyEditors = new HashMap();
		propertyEditors.put("cacheNames", new StringArrayPropertyEditor());

		ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
		editor.setCacheModelClass(OsCacheFlushingModel.class);
		editor.setCacheModelPropertyEditors(propertyEditors);
		return editor;
	}

	/**
	 * Sets the OSCache cache manager to use.
	 *
	 * @param newCacheManager the new cache manager
	 */
	public void setCacheManager(GeneralCacheAdministrator newCacheManager) {
		cacheManager = newCacheManager;
	}

	/**
	 * Returns the <code>String</code> representation of the given key.
	 *
	 * @param key the cache key.
	 * @return the <code>String</code> representation of <code>cacheKey</code>.
	 */
	protected String getEntryKey(Serializable key) {
		return key.toString();
	}

	/**
	 * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
	 */
	protected boolean isSerializableCacheElementRequired() {
		return false;
	}

	/**
	 * @see AbstractCacheProviderFacade#onCancelCacheUpdate(Serializable)
	 */
	protected void onCancelCacheUpdate(Serializable key) {
		String newKey = getEntryKey(key);
		cacheManager.cancelUpdate(newKey);
	}

	/**
	 * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
	 */
	protected void onFlushCache(FlushingModel model) {
		OsCacheFlushingModel cachingModel = (OsCacheFlushingModel) model;
		String[] groups = cachingModel.getGroups();

		if (!ObjectUtils.isEmpty(groups)) {
			int groupCount = groups.length;

			for (int i = 0; i < groupCount; i++) {
				String group = groups[i];
				cacheManager.flushGroup(group);
			}

		} else {
			cacheManager.flushAll();
		}
	}

	/**
	 * @see AbstractCacheProviderFacade#onGetFromCache(Serializable,CachingModel)
	 */
	protected Object onGetFromCache(Serializable key, CachingModel model) {
		OsCacheCachingModel cachingModel = (OsCacheCachingModel) model;

		Integer refreshPeriod = cachingModel.getRefreshPeriod();
		String cronExpression = cachingModel.getCronExpression();

		String newKey = getEntryKey(key);
		Object cachedObject = null;

		try {
			if (null == refreshPeriod) {
				cachedObject = cacheManager.getFromCache(newKey);

			} else if (null == cronExpression) {
				cachedObject = cacheManager.getFromCache(newKey, refreshPeriod
						.intValue());

			} else {
				cachedObject = cacheManager.getFromCache(newKey, refreshPeriod
						.intValue(), cronExpression);
			}
		} catch (NeedsRefreshException needsRefreshException) {
			// prevent the cache entry from being locked
			// see: http://www.opensymphony.com/oscache/api/com/opensymphony/oscache/base/Cache.html#getFromCache(java.lang.String, int)
			cacheManager.cancelUpdate(newKey);
		}

		return cachedObject;
	}

	/**
	 * @see AbstractCacheProviderFacade#onPutInCache(Serializable,CachingModel,
	 *Object)
	 */
	protected void onPutInCache(Serializable key, CachingModel model, Object obj) {
		OsCacheCachingModel cachingModel = (OsCacheCachingModel) model;

		String newKey = getEntryKey(key);
		String[] groups = cachingModel.getGroups();

		if (groups == null || groups.length == 0) {
			cacheManager.putInCache(newKey, obj);

		} else {
			cacheManager.putInCache(newKey, obj, groups);
		}
	}

	/**
	 * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable,
	 *CachingModel)
	 */
	protected void onRemoveFromCache(Serializable key, CachingModel model) {
		String newKey = getEntryKey(key);
		cacheManager.flushEntry(newKey);
	}

	/**
	 * @throws FatalCacheException if the cache manager is <code>null</code>.
	 * @see AbstractCacheProviderFacade#validateCacheManager()
	 */
	protected void validateCacheManager() throws FatalCacheException {
		assertCacheManagerIsNotNull(cacheManager);
	}

}