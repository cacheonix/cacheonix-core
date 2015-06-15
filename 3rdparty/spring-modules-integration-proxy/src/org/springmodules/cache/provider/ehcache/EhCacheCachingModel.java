/*
 * Created on Oct 29, 2004
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
package org.springmodules.cache.provider.ehcache;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * Configuration options needed to store, retrieve and remove objects from
 * EHCache.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public class EhCacheCachingModel implements CachingModel {

	private static final long serialVersionUID = 3762529035888112945L;

	private String cacheName;

	private boolean blocking;

	private CacheEntryFactory cacheEntryFactory;

	/**
	 * Constructor.
	 */
	public EhCacheCachingModel() {
	}

	/**
	 * Constructor.
	 *
	 * @param cacheName the name of the EHCache cache to use
	 */
	public EhCacheCachingModel(String cacheName) {
		setCacheName(cacheName);
	}

	/**
	 * Sets the name of the EHCache cache to use.
	 *
	 * @param newCacheName the new name of the EHCache cache
	 */
	public final void setCacheName(String newCacheName) {
		cacheName = newCacheName;
	}

	/**
	 * @return the name of the EHCache cache to use
	 */
	public final String getCacheName() {
		return cacheName;
	}

	/**
	 * Set whether to use a blocking cache that lets read attempts block
	 * until the requested element is created.
	 * <p>If you intend to build a self-populating blocking cache,
	 * consider specifying a {@link #setCacheEntryFactory CacheEntryFactory}.
	 *
	 * @see net.sf.ehcache.constructs.blocking.BlockingCache
	 * @see #setCacheEntryFactory
	 * @see org.springframework.cache.ehcache.EhCacheFactoryBean#setBlocking(boolean)
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * Set an EHCache {@link net.sf.ehcache.constructs.blocking.CacheEntryFactory}
	 * to use for a self-populating cache. If such a factory is specified,
	 * the cache will be decorated with EHCache's
	 * {@link net.sf.ehcache.constructs.blocking.SelfPopulatingCache}.
	 * <p>The specified factory can be of type
	 * {@link net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory},
	 * which will lead to the use of an
	 * {@link net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache}.
	 * <p>Note: Any such self-populating cache is automatically a blocking cache.
	 *
	 * @see net.sf.ehcache.constructs.blocking.SelfPopulatingCache
	 * @see net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache
	 * @see net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory
	 * @see org.springframework.cache.ehcache.EhCacheFactoryBean#setCacheEntryFactory(net.sf.ehcache.constructs.blocking.CacheEntryFactory)
	 */
	public void setCacheEntryFactory(CacheEntryFactory cacheEntryFactory) {
		this.cacheEntryFactory = cacheEntryFactory;
	}

	public CacheEntryFactory getCacheEntryFactory() {
		return cacheEntryFactory;
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EhCacheCachingModel)) {
			return false;
		}

		EhCacheCachingModel cachingModel = (EhCacheCachingModel) obj;

		if (!ObjectUtils.nullSafeEquals(cacheName, cachingModel.cacheName)) {
			return false;
		}

		return true;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		int multiplier = 31;
		int hash = 7;
		hash = multiplier * hash + (Objects.nullSafeHashCode(cacheName));
		return hash;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheName=")
				.append(StringUtils.quote(cacheName))
				.append(", blocking=")
				.append(blocking)
				.append(", cacheEntryFactory=")
				.append((cacheEntryFactory != null)? cacheEntryFactory.getClass().getName() : null)
				.append("]")
				.toString();
	}
}