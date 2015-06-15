/* 
 * Created on Oct 21, 2004
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
 * Copyright @2004 the original author or authors.
 */

package org.springmodules.cache.interceptor.flush;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.CacheProviderFacade;

/**
 * <p>
 * Template for advices that flush a cache when the intercepted method is
 * executed.
 * </p>
 * 
 * @author Alex Ruiz
 */
public abstract class AbstractFlushingInterceptor implements MethodInterceptor,
    InitializingBean {

  /** Logger available to subclasses */
  protected final Log logger = LogFactory.getLog(getClass());

  private CacheProviderFacade cacheProviderFacade;

  /**
   * Map of <code>{@link FlushingModel}</code>s that specify how to flush the
   * cache. Each cache model is stored under a unique id (a String).
   */
  private Map flushingModels;

  /**
   * @throws FatalCacheException
   *           if the cache provider facade is <code>null</code>.
   * 
   * @see InitializingBean#afterPropertiesSet()
   * @see #onAfterPropertiesSet()
   */
  public final void afterPropertiesSet() throws FatalCacheException {
    if (cacheProviderFacade == null) {
      throw new FatalCacheException(
          "The cache provider facade should not be null");
    }

    if (flushingModels == null || flushingModels.isEmpty()) {
      return;
    }

    CacheModelValidator validator = cacheProviderFacade
        .modelValidator();

    if (flushingModels instanceof Properties) {
      PropertyEditor editor = cacheProviderFacade.getFlushingModelEditor();
      Properties properties = (Properties) flushingModels;
      Map newFlushingModels = new HashMap();

      String id = null;

      try {
        for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
          id = (String) i.next();

          String property = properties.getProperty(id);
          editor.setAsText(property);
          Object flushingModel = editor.getValue();
          validator.validateFlushingModel(flushingModel);

          newFlushingModels.put(id, flushingModel);
        }
      } catch (Exception exception) {
        throw new FatalCacheException(
            "Unable to create the flushing model with id "
                + StringUtils.quote(id), exception);
      }

      setFlushingModels(newFlushingModels);

    } else {
      String id = null;

      try {
        for (Iterator i = flushingModels.keySet().iterator(); i.hasNext();) {
          id = (String) i.next();
          Object flushingModel = flushingModels.get(id);
          validator.validateFlushingModel(flushingModel);
        }
      } catch (Exception exception) {
        throw new FatalCacheException(
            "Unable to validate flushing model with id "
                + StringUtils.quote(id), exception);
      }
    }

    onAfterPropertiesSet();
  }

  /**
   * Flushes the cache.
   * 
   * @param methodInvocation
   *          the description of the intercepted method.
   * @return the return value of the intercepted method.
   * @throws Throwable
   *           any exception thrown when executing the intercepted method
   */
  public final Object invoke(MethodInvocation methodInvocation)
      throws Throwable {
    FlushingModel model = getModel(methodInvocation);

    if (null == model) {
      logger.info("Unable to flush cache. "
          + "No model is associated to the intercepted method");
      return methodInvocation.proceed();
    }

    Object proceedReturnValue = null;

    if (model.flushBeforeMethodExecution()) {
      cacheProviderFacade.flushCache(model);
      proceedReturnValue = methodInvocation.proceed();

    } else {
      proceedReturnValue = methodInvocation.proceed();
      cacheProviderFacade.flushCache(model);
    }

    return proceedReturnValue;
  }

  /**
   * Sets the facade for the cache provider to use.
   * 
   * @param newCacheProviderFacade
   *          the new cache provider facade
   */
  public final void setCacheProviderFacade(
      CacheProviderFacade newCacheProviderFacade) {
    cacheProviderFacade = newCacheProviderFacade;
  }

  /**
   * Sets the flushing models to use.
   * 
   * @param newFlushingModels
   *          the new flushing models
   */
  public final void setFlushingModels(Map newFlushingModels) {
    flushingModels = newFlushingModels;
  }

  /**
   * @return the map that specifies how caching models should be bound to class
   *         methods
   */
  protected final Map getFlushingModels() {
    return flushingModels;
  }

  /**
   * Returns the flushing model bound to an intercepted method.
   * 
   * @param methodInvocation
   *          the description of the invocation to the intercepted method
   * @return the flushing model boudn to the given intercepted method
   */
  protected abstract FlushingModel getModel(MethodInvocation methodInvocation);

  /**
   * Gives subclasses the opportunity to set up their own properties.
   * 
   * @throws FatalCacheException
   *           if one or more properties of this interceptor contain invalid
   *           values or have an illegal state.
   */
  protected void onAfterPropertiesSet() throws FatalCacheException {
    // no implementation.
  }

}