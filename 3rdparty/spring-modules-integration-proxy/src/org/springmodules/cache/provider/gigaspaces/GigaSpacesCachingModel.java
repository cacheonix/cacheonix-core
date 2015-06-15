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

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springmodules.cache.CachingModel;
import org.springmodules.util.Objects;

/**
 * <p/>
 * Configuration options needed to store, retrieve and remove objects from
 * GigaSpaces.
 *
 * @author Omar Irbouh
 * @author Lior Ben Yizhak
 */
public class GigaSpacesCachingModel implements CachingModel {

	private static final long serialVersionUID = 3762529035888112945L;

	private String cacheName;
	private Long timeToLive;
	private Long waitForResponse;


	/**
	 * Constructor.
	 */
	public GigaSpacesCachingModel() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param cacheName the name of the GigaSpaces cache to use
	 */
	public GigaSpacesCachingModel(String cacheName) {
		this();
		setCacheName(cacheName);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GigaSpacesCachingModel)) {
			return false;
		}

		GigaSpacesCachingModel cachingModel = (GigaSpacesCachingModel) obj;

		if (!ObjectUtils.nullSafeEquals(cacheName, cachingModel.cacheName)) {
			return false;
		}
		if (!ObjectUtils.nullSafeEquals(timeToLive, cachingModel.timeToLive)) {
			return false;
		}
		if (!ObjectUtils.nullSafeEquals(waitForResponse, cachingModel.waitForResponse)) {
			return false;
		}

		return true;
	}

	/**
	 * @return how long (in milliseconds) a given entry should stay in the cache
	 */
	public final Long getTimeToLive() {
		return timeToLive;
	}

	/**
	 * @return how long (in milliseconds) a take entry should be.
	 */
	public final Long getWaitForResponse() {
		return waitForResponse;
	}

	/**
	 * @return the name of the GigaSpaces cache to use
	 */
	public final String getCacheName() {
		return cacheName;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		int multiplier = 31;
		int hash = 7;
		hash = multiplier * hash + (Objects.nullSafeHashCode(cacheName));
		hash = multiplier * hash + (Objects.nullSafeHashCode(timeToLive));
		hash = multiplier * hash + (Objects.nullSafeHashCode(waitForResponse));
		return hash;
	}


	/**
	 * Sets the name of the GigaSpaces cache to use.
	 *
	 * @param newCacheName the new name of the GigaSpaces cache
	 */
	public final void setCacheName(String newCacheName) {
		cacheName = newCacheName;
	}

	/**
	 * Sets the time in milliseconds an entry should stay in the cache
	 *
	 * @param newTimeToLive the new time to set
	 */
	public void setTimeToLive(long newTimeToLive) {
		setTimeToLive(new Long(newTimeToLive));
	}

	/**
	 * Sets the time in milliseconds an entry should stay in the cache
	 *
	 * @param newTimeToLive the new time to set
	 */
	public final void setTimeToLive(Long newTimeToLive) {
		timeToLive = newTimeToLive;
	}

	/**
	 * Sets the time in milliseconds an entry should be taken
	 *
	 * @param newWaitForResponse the new time to set
	 */
	public void setWaitForResponse(long newWaitForResponse) {
		setWaitForResponse(new Long(newWaitForResponse));
	}

	/**
	 * Sets the time in milliseconds an entry should stay in the cache
	 *
	 * @param newWaitForResponse the new time to set
	 */
	public final void setWaitForResponse(Long newWaitForResponse) {
		waitForResponse = newWaitForResponse;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return Objects.identityToString(this)
				.append("[cacheName=")
				.append(StringUtils.quote(cacheName))
				.append(", waitForResponse=")
				.append(waitForResponse)
				.append(", timeToLive=")
				.append(timeToLive)
				.append("]")
				.toString();
	}

}