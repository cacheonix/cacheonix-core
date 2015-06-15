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
package org.springmodules.cache.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.serializable.SerializableFactory;

import java.io.Serializable;

/**
 * Template for implementations of <code>{@link CacheProviderFacade}</code>.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public abstract class AbstractCacheProviderFacade implements
		CacheProviderFacade, InitializingBean  {

	/**
	 * Logger available to subclasses
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean failQuietlyEnabled;

	private SerializableFactory serializableFactory;

	/**
	 * Validates the properties of this class after being set by the
	 * <code>BeanFactory</code>.
	 *
	 * @throws FatalCacheException if one or more properties of this facade are in an illegal state
	 * @see #validateCacheManager()
	 */
	public final void afterPropertiesSet() throws FatalCacheException {
		validateCacheManager();
		onAfterPropertiesSet();
	}

	/**
	 * @see CacheProviderFacade#cancelCacheUpdate(Serializable)
	 */
	public final void cancelCacheUpdate(Serializable key) throws CacheException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempt to cancel a cache update using the key <"
					+ StringUtils.quoteIfString(key) + ">");
		}

		try {
			onCancelCacheUpdate(key);

		} catch (CacheException exception) {
			handleCatchedException(exception);
		}
	}

	/**
	 * @see CacheProviderFacade#flushCache(FlushingModel)
	 */
	public final void flushCache(FlushingModel model) throws CacheException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempt to flush the cache using model <" + model + ">");
		}

		if (model != null) {
			try {
				onFlushCache(model);
				logger.debug("Cache has been flushed.");

			} catch (CacheException exception) {
				handleCatchedException(exception);
			}
		}
	}

	/**
	 * @see CacheProviderFacade#getFromCache(Serializable,CachingModel)
	 */
	public final Object getFromCache(Serializable key, CachingModel model)
			throws CacheException {

		if (logger.isDebugEnabled()) {
			logger.debug("Attempt to retrieve a cache entry using key <"
					+ StringUtils.quoteIfString(key) + "> and cache model <" + model
					+ ">");
		}

		Object cachedObject = null;

		try {
			if (model != null) {
				cachedObject = onGetFromCache(key, model);

				// deserialize the value if required
				if (cachedObject != null) {
					cachedObject = deserializeValueIfNecessary(cachedObject);
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Retrieved cache element <"
						+ StringUtils.quoteIfString(cachedObject) + ">");
			}

		} catch (CacheException exception) {
			handleCatchedException(exception);
		}
		return cachedObject;
	}

	/**
	 * @see CacheProviderFacade#isFailQuietlyEnabled()
	 */
	public final boolean isFailQuietlyEnabled() {
		return failQuietlyEnabled;
	}

	/**
	 * @see CacheProviderFacade#putInCache(Serializable,CachingModel,Object)
	 * @see #makeSerializableIfNecessary(Object)
	 */
	public final void putInCache(Serializable key, CachingModel model, Object obj)
			throws CacheException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempt to store the object <" + obj
					+ "> in the cache using key <" + StringUtils.quoteIfString(key)
					+ "> and model <" + model + ">");
		}

		try {
			Object newCacheElement = makeSerializableIfNecessary(obj);

			if (model != null) {
				onPutInCache(key, model, newCacheElement);
				logger.debug("Object was successfully stored in the cache");
			}
		} catch (CacheException exception) {
			handleCatchedException(exception);
		}
	}

	/**
	 * @see CacheProviderFacade#removeFromCache(Serializable,CachingModel)
	 */
	public final void removeFromCache(Serializable key, CachingModel model)
			throws CacheException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempt to remove an entry from the cache using key <"
					+ StringUtils.quoteIfString(key) + "> and model <" + model + ">");
		}

		if (model != null) {
			try {
				onRemoveFromCache(key, model);
				logger.debug("Object removed from the cache");

			} catch (CacheException exception) {
				handleCatchedException(exception);
			}
		}
	}

	/**
	 * Sets the flag that indicates if any exception thrown at run-time by the
	 * cache manager should be propagated (<code>false</code>) or not (<code>true</code>.)
	 *
	 * @param newFailQuietlyEnabled the new value for the flag
	 */
	public final void setFailQuietlyEnabled(boolean newFailQuietlyEnabled) {
		failQuietlyEnabled = newFailQuietlyEnabled;
	}

	/**
	 * Sets the factory that makes serializable the objects to be stored in the
	 * cache (if the cache requires serializable elements).
	 *
	 * @param newSerializableFactory the new factory of serializable objects
	 */
	public final void setSerializableFactory(
			SerializableFactory newSerializableFactory) {
		serializableFactory = newSerializableFactory;
	}

	/**
	 * Asserts that the given cache manager is not <code>null</code>.
	 *
	 * @param cacheManager the cache manager to check
	 * @throws FatalCacheException if the cache manager is <code>null</code>
	 */
	protected final void assertCacheManagerIsNotNull(Object cacheManager)
			throws FatalCacheException {
		if (cacheManager == null) {
			throw new FatalCacheException("The cache manager should not be null");
		}
	}

	/**
	 * Rethrows the given exception if "fail quietly" is enabled.
	 *
	 * @param exception the catched exception to be potentially rethrown.
	 * @throws CacheException if this cache provider has not been configured to "fail quietly."
	 */
	protected final void handleCatchedException(CacheException exception)
			throws CacheException {
		logger.error(exception.getMessage(), exception);
		if (!isFailQuietlyEnabled()) {
			throw exception;
		}
	}

	/**
	 * @return <code>true</code> if the cache used by this facade can only store
	 *         serializable objects.
	 */
	protected abstract boolean isSerializableCacheElementRequired();

	/**
	 * Makes the given object serializable if:
	 * <ul>
	 * <li>The cache can only store serializable objects</li>
	 * <li>The given object does not implement <code>java.io.Serializable</code>
	 * </li>
	 * </ul>
	 * Otherwise, will return the same object passed as argument.
	 *
	 * @param obj the object to check.
	 * @return the given object as a serializable object if necessary.
	 * @throws ObjectCannotBeCachedException if the cache requires serializable elements, the given object
	 *                                       does not implement <code>java.io.Serializable</code> and the
	 *                                       factory of serializable objects is <code>null</code>.
	 * @see #setSerializableFactory(SerializableFactory)
	 * @see org.springmodules.cache.serializable.SerializableFactory#makeSerializableIfNecessary(Object)
	 */
	protected final Object makeSerializableIfNecessary(Object obj) {
		if (!isSerializableCacheElementRequired()) {
			return obj;
		}
		if (obj instanceof Serializable) {
			return obj;
		}
		if (serializableFactory != null) {
			return serializableFactory.makeSerializableIfNecessary(obj);
		}
		throw new ObjectCannotBeCachedException(
				"The cache can only store implementations of java.io.Serializable");
	}

	/**
	 * Deserialize the value from the given object if:
	 * <ul>
	 * <li>The cache can only store serializable objects</li>
	 * <li>The given object does not implement <code>java.io.Serializable</code>
	 * </li>
	 * </ul>
	 * Otherwise, will throw an {@link InvalidObjectInCacheException}.
	 *
	 * @param obj the object to check.
	 * @return the given object as a serializable object if necessary.
	 * @throws InvalidObjectInCacheException if the cache requires serializable elements, the given object
	 *                                       does not implement <code>java.io.Serializable</code> and the
	 *                                       factory of serializable objects is <code>null</code>.
	 * @see #setSerializableFactory(SerializableFactory)
	 * @see org.springmodules.cache.serializable.SerializableFactory#getOriginalValue(Object)
	 */
	protected final Object deserializeValueIfNecessary(Object obj) {
		if (!isSerializableCacheElementRequired()) {
			return obj;
		}
		if (obj instanceof Serializable) {
			if (serializableFactory != null) {
				return serializableFactory.getOriginalValue(obj);
			}
			return obj;
		}
		throw new InvalidObjectInCacheException(
				"The object retrieved from the cache is not of the required " +
						"type: java.io.Serializable.");
	}

	/**
	 * Gives subclasses the opportunity to initialize their own properties. Called
	 * after <code>{@link #afterPropertiesSet()}</code> has finished setting up
	 * the properties of an instance of this class.
	 *
	 * @throws FatalCacheException if one or more properties of the facade or its dependencies have
	 *                             incorrect values.
	 */
	protected void onAfterPropertiesSet() throws FatalCacheException {
		// no implementation.
	}

	/**
	 * Cancels the update being made to the cache.
	 *
	 * @param key the key being used in the cache update.
	 * @throws CacheException if an unexpected error takes place when attempting to cancel the
	 *                        update.
	 */
	protected void onCancelCacheUpdate(Serializable key) throws CacheException {
		logger.info("Cache provider does not support cancelation of updates");
	}

	/**
	 * Flushes the caches specified in the given model.
	 *
	 * @param model the model that specifies what and how to flush.
	 * @throws CacheException if an unexpected error takes place when flushing the cache.
	 */
	protected abstract void onFlushCache(FlushingModel model)
			throws CacheException;

	/**
	 * Retrieves an entry from the cache.
	 *
	 * @param key   the key under which the entry is stored.
	 * @param model the model that specifies how to retrieve an entry.
	 * @return the cached entry.
	 * @throws CacheException if an unexpected error takes place when retrieving the entry from
	 *                        the cache.
	 */
	protected abstract Object onGetFromCache(Serializable key, CachingModel model)
			throws CacheException;

	/**
	 * Stores an object in the cache.
	 *
	 * @param key   the key used to store the object.
	 * @param model the model that specifies how to store an object in the cache.
	 * @param obj   the object to store in the cache.
	 * @throws CacheException if an unexpected error takes place when storing an object in the
	 *                        cache.
	 */
	protected abstract void onPutInCache(Serializable key, CachingModel model,
										 Object obj) throws CacheException;

	/**
	 * Removes an entry from the cache.
	 *
	 * @param key   the key the entry to remove is stored under.
	 * @param model the model that specifies how to remove the entry from the cache.
	 * @throws CacheException if an unexpected error takes place when removing an entry from
	 *                        the cache.
	 */
	protected abstract void onRemoveFromCache(Serializable key, CachingModel model)
			throws CacheException;

	/**
	 * Validates the cache manager used by this facade.
	 *
	 * @throws FatalCacheException if the cache manager is in an invalid state.
	 */
	protected abstract void validateCacheManager() throws FatalCacheException;
}