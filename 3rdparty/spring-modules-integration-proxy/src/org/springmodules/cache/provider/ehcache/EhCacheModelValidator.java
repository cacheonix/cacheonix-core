/*
 * Created on Jan 14, 2005
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

package org.springmodules.cache.provider.ehcache;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.provider.AbstractCacheModelValidator;
import org.springmodules.cache.provider.InvalidCacheModelException;

/**
 * <p/>
 * Validates the property values of <code>{@link EhCacheCachingModel}</code>s
 * and <code>{@link EhCacheFlushingModel}</code>s.
 * </p>
 *
 * @author Alex Ruiz
 */
public final class EhCacheModelValidator extends AbstractCacheModelValidator {

	/**
	 * @see AbstractCacheModelValidator#getCachingModelTargetClass()
	 */
	protected Class getCachingModelTargetClass() {
		return EhCacheCachingModel.class;
	}

	/**
	 * @see AbstractCacheModelValidator#getFlushingModelTargetClass()
	 */
	protected Class getFlushingModelTargetClass() {
		return EhCacheFlushingModel.class;
	}

	/**
	 * @throws InvalidCacheModelException if the given model does not specify a cache.
	 * @see AbstractCacheModelValidator#validateCachingModelProperties(Object)
	 */
	protected void validateCachingModelProperties(Object cachingModel)
			throws InvalidCacheModelException {
		EhCacheCachingModel model = (EhCacheCachingModel) cachingModel;
		if (!StringUtils.hasText(model.getCacheName())) {
			throw new InvalidCacheModelException("Cache name should not be empty");
		}
	}

	/**
	 * @throws InvalidCacheModelException if the given model does not specify at least one cache.
	 * @see AbstractCacheModelValidator#validateFlushingModelProperties(Object)
	 */
	protected void validateFlushingModelProperties(Object flushingModel)
			throws InvalidCacheModelException {
		EhCacheFlushingModel model = (EhCacheFlushingModel) flushingModel;
		String[] cacheNames = model.getCacheNames();

		if (ObjectUtils.isEmpty(cacheNames)) {
			throw new InvalidCacheModelException(
					"There should be at least one cache name");
		}
	}
}