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
package org.springmodules.cache.interceptor.caching;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.key.CacheKeyGenerator;
import org.springmodules.cache.key.HashCodeCacheKeyGenerator;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.CacheProviderFacade;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Template for advices that store in a cache the return value of intercepted
 * methods.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public abstract class AbstractCachingInterceptor implements MethodInterceptor,
		InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	public static final NullObject NULL_ENTRY = new NullObject();

	private CacheProviderFacade cache;

	private CacheKeyGenerator keyGenerator;

	private CachingListener[] listeners;

	private Map modelMap;

	public final void afterPropertiesSet() throws FatalCacheException {
		validateCache();
		if (modelMap instanceof Properties)
			setCachingModels(propertiesToModels());
		validateModels();
		if (keyGenerator == null) setCacheKeyGenerator(defaultKeyGenerator());
		onAfterPropertiesSet();
	}

	public final CacheKeyGenerator cacheKeyGenerator() {
		return keyGenerator;
	}

	public final Object invoke(MethodInvocation mi) throws Throwable {
		Method method = mi.getMethod();
		if (!CachingUtils.isCacheable(method))
			return methodNotCacheable(mi, method);

		CachingModel model = model(mi);
		if (model == null) return noModelFound(mi, method);

		Serializable key = keyGenerator.generateKey(mi);
		Object cached = cache.getFromCache(key, model);

		if (null == cached) return cachedValueFromSource(mi, key, model);
		return unmaskNull(cached);
	}

	public final void setCacheKeyGenerator(CacheKeyGenerator k) {
		keyGenerator = k;
	}

	public final void setCacheProviderFacade(CacheProviderFacade c) {
		cache = c;
	}

	public final void setCachingListeners(CachingListener[] l) {
		listeners = l;
	}

	public final void setCachingModels(Map m) {
		modelMap = m;
	}

	protected abstract CachingModel model(MethodInvocation mi);

	protected final Map models() {
		return modelMap;
	}

	protected void onAfterPropertiesSet() throws FatalCacheException {
		// no implementation.
	}

	private Object cachedValueFromSource(MethodInvocation mi, Serializable key,
										 CachingModel m) throws Throwable {
		boolean successful = true;
		try {
			Object value = mi.proceed();
			putInCache(key, m, value);
			return value;
		} catch (Throwable t) {
			successful = false;
			logger.debug("method " + mi.getMethod().getName() + " throwed a exception", t);
			throw t;
		} finally {
			if (!successful) cache.cancelCacheUpdate(key);
		}
	}

	private CacheKeyGenerator defaultKeyGenerator() {
		return new HashCodeCacheKeyGenerator(true);
	}

	private Object logAndProceed(String message, MethodInvocation mi)
			throws Throwable {
		logger.debug(message);
		return mi.proceed();
	}

	private Object maskNull(Object o) {
		return o != null ? o : NULL_ENTRY;
	}

	private Object methodNotCacheable(MethodInvocation mi, Method m)
			throws Throwable {
		return logAndProceed("Unable to perform caching. Intercepted method <"
				+ m + "> does not return a value", mi);
	}

	private Object noModelFound(MethodInvocation mi, Method m) throws Throwable {
		return logAndProceed("Unable to perform caching. "
				+ "No model is associated to the method <" + m + ">", mi);
	}

	private void notifyListeners(Serializable key, Object cachedObject,
								 CachingModel m) {
		if (ObjectUtils.isEmpty(listeners)) return;
		for (int i = 0; i < listeners.length; i++)
			listeners[i].onCaching(key, cachedObject, m);
	}

	private Map propertiesToModels() {
		PropertyEditor editor = cache.getCachingModelEditor();
		Properties properties = (Properties) modelMap;

		Map m = new HashMap();
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			editor.setAsText(properties.getProperty(id));
			m.put(id, editor.getValue());
		}
		return m;
	}

	private void putInCache(Serializable key, CachingModel m, Object o) {
		cache.putInCache(key, m, maskNull(o));
		notifyListeners(key, o, m);
	}

	private Object unmaskNull(Object obj) {
		return NULL_ENTRY.equals(obj) ? null : obj;
	}

	private void validateCache() throws FatalCacheException {
		if (cache == null)
			throw new FatalCacheException(
					"The cache provider facade should not be null");
	}

	private void validateModels() throws FatalCacheException {
		if (CollectionUtils.isEmpty(modelMap))
			throw new FatalCacheException(
					"The map of caching models should not be empty");

		CacheModelValidator validator = cache.modelValidator();
		String id = null;
		try {
			for (Iterator i = modelMap.keySet().iterator(); i.hasNext();) {
				id = (String) i.next();
				validator.validateCachingModel(modelMap.get(id));
			}
		} catch (Exception exception) {
			throw new FatalCacheException("Unable to validate caching model with id "
					+ StringUtils.quote(id), exception);
		}
	}

}