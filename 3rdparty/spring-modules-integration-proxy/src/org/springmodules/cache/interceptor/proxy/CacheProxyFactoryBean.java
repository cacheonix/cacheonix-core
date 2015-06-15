/* 
 * Created on Jan 17, 2005
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

package org.springmodules.cache.interceptor.proxy;

import java.util.Map;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

import org.springmodules.cache.interceptor.caching.CachingListener;
import org.springmodules.cache.interceptor.caching.CachingModelSourceAdvisor;
import org.springmodules.cache.interceptor.caching.NameMatchCachingInterceptor;
import org.springmodules.cache.interceptor.flush.FlushingModelSourceAdvisor;
import org.springmodules.cache.interceptor.flush.NameMatchFlushingInterceptor;
import org.springmodules.cache.key.CacheKeyGenerator;
import org.springmodules.cache.provider.CacheProviderFacade;

/**
 * <p>
 * Proxy factory bean for simplified declarative caching services.
 * </p>
 * 
 * @author Alex Ruiz
 */
public final class CacheProxyFactoryBean extends ProxyConfig implements
    FactoryBean, InitializingBean {

  private static final long serialVersionUID = 3688501099833603120L;

  private NameMatchCachingInterceptor cachingInterceptor;

  private NameMatchFlushingInterceptor flushingInterceptor;

  private boolean hasFlushingModels;

  /**
   * The proxy to create.
   */
  private Object proxy;

  /**
   * Set of interfaces to be implemented by the proxy.
   */
  private Class[] proxyInterfaces;

  private Object target;

  /**
   * Construct a <code>CacheProxyFactoryBean</code>.
   */
  public CacheProxyFactoryBean() {
    super();
    flushingInterceptor = new NameMatchFlushingInterceptor();
    cachingInterceptor = new NameMatchCachingInterceptor();
  }

  /**
   * Creates the proxy for target object. This method is invoked by a
   * BeanFactory after it has set all bean properties supplied.
   * 
   * @throws IllegalStateException
   *           if target is <code>null</code>.
   * @throws AopConfigException
   *           if the proxy interfaces or proxyTargetClass are not set and the
   *           target type is <code>org.springframework.aop.TargetSource</code>.
   */
  public void afterPropertiesSet() throws IllegalStateException,
      AopConfigException {
    cachingInterceptor.afterPropertiesSet();
    flushingInterceptor.afterPropertiesSet();

    if (target == null) {
      throw new IllegalStateException("Property 'target' is required");
    }

    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.addAdvisor(new CachingModelSourceAdvisor(cachingInterceptor));

    if (hasFlushingModels) {
      proxyFactory.addAdvisor(new FlushingModelSourceAdvisor(
          flushingInterceptor));
    }

    proxyFactory.copyFrom(this);

    TargetSource targetSource = createTargetSource(target);
    proxyFactory.setTargetSource(targetSource);

    if (proxyInterfaces != null) {
      proxyFactory.setInterfaces(proxyInterfaces);
    } else if (!isProxyTargetClass()) {
      if (target instanceof TargetSource) {
        throw new AopConfigException(
            "Either 'proxyInterfaces' or 'proxyTargetClass' is required "
                + "when using a TargetSource as 'target'");
      }

      // rely on AOP infrastructure to tell us what interfaces to proxy
      proxyFactory.setInterfaces(ClassUtils.getAllInterfaces(target));
    }

    proxy = proxyFactory.getProxy();
  }

  /**
   * @return the created AOP proxy.
   */
  public Object getObject() {
    return proxy;
  }

  /**
   * @return the class of the created AOP proxy (if created), the target object
   *         (if set) or <code>null</code>.
   * 
   * @see FactoryBean#getObjectType()
   */
  public Class getObjectType() {
    Class objectType = null;

    if (proxy != null) {
      objectType = proxy.getClass();
    } else if (target != null && target instanceof TargetSource) {
      objectType = target.getClass();
    }

    return objectType;
  }

  /**
   * @see FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * Sets the generator of cache entry keys.
   * 
   * @param cacheKeyGenerator
   *          the new generator of cache entry keys
   */
  public void setCacheKeyGenerator(CacheKeyGenerator cacheKeyGenerator) {
    cachingInterceptor.setCacheKeyGenerator(cacheKeyGenerator);
  }

  /**
   * Sets the cache provider facade for the interceptors
   * <code>{@link #flushingInterceptor}</code> and
   * <code>{@link #cachingInterceptor}</code>.
   * 
   * @param cacheProviderFacade
   *          the cache provider facade
   */
  public void setCacheProviderFacade(CacheProviderFacade cacheProviderFacade) {
    flushingInterceptor.setCacheProviderFacade(cacheProviderFacade);
    cachingInterceptor.setCacheProviderFacade(cacheProviderFacade);
  }

  /**
   * Sets the listener to be notified each time an entry is stored in the cache.
   * 
   * @param cachingListeners
   *          the new caching listeners
   */
  public void setCachingListeners(CachingListener[] cachingListeners) {
    cachingInterceptor.setCachingListeners(cachingListeners);
  }

  /**
   * Sets the caching models with method names as keys.
   * <p>
   * Note: Method names are always applied to the target class, no matter if
   * defined in an interface or the class itself.
   * <p>
   * Internally, a
   * <code>{@link org.springmodules.cache.interceptor.caching.NameMatchCachingModelSource}</code>
   * will be created from the given properties.
   * 
   * @param cachingModels
   *          the new caching models
   * 
   * @see org.springmodules.cache.interceptor.caching.NameMatchCachingModelSource
   */
  public void setCachingModels(Map cachingModels) {
    cachingInterceptor.setCachingModels(cachingModels);
  }

  /**
   * Sets the cache-flushing models with method names as keys.
   * <p>
   * Note: Method names are always applied to the target class, no matter if
   * defined in an interface or the class itself.
   * <p>
   * Internally, a
   * <code>{@link org.springmodules.cache.interceptor.flush.NameMatchFlushingModelSource}</code>
   * will be created from the given properties.
   * 
   * @param flushingModels
   *          the new flushing models
   * 
   * @see org.springmodules.cache.interceptor.flush.NameMatchFlushingModelSource
   */
  public void setFlushingModels(Map flushingModels) {
    hasFlushingModels = flushingModels != null && !flushingModels.isEmpty();

    if (hasFlushingModels) {
      flushingInterceptor.setFlushingModels(flushingModels);
    } else {
      flushingInterceptor.setFlushingModelSource(null);
    }
  }

  /**
   * <p>
   * Specify the set of interfaces being proxied.
   * </p>
   * <p>
   * If left null (the default), the AOP infrastructure works out which
   * interfaces need proxying by analyzing the target, proxying all the
   * interfaces that the target object implements.
   * </p>
   * 
   * @param interfaceNames
   *          the interfaces
   * @throws ClassNotFoundException
   *           if any of the classes can't be loaded
   */
  public void setProxyInterfaces(String[] interfaceNames)
      throws ClassNotFoundException {
    proxyInterfaces = toInterfaceArray(interfaceNames);
  }
  
  private Class[] toInterfaceArray(String[] interfaceNames) throws ClassNotFoundException
  {
	  Class interfaces[] = new Class[interfaceNames.length];
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaces[i] = ClassUtils.forName(interfaceNames[i].trim());
			// Check whether it is an interface.
			if (!interfaces[i].isInterface()) {
				throw new IllegalArgumentException(
						"Can proxy only interfaces: [" + interfaces[i].getName() + "] is a class");
			}
		}
		return interfaces;
  }

  /**
   * Sets the target object to be proxied.
   * 
   * @param newTarget
   *          the new target object
   */
  public void setTarget(Object newTarget) {
    target = newTarget;
  }

  /**
   * Set the target or <code>TargetSource</code>.
   * 
   * @param targetObject
   *          target. If this is an implementation of <code>TargetSource</code>
   *          it is used as our <code>TargetSource</code>; otherwise it is
   *          wrapped in a <code>SingletonTargetSource</code>.
   * @return a <code>TargetSource</code> for this object.
   */
  protected TargetSource createTargetSource(Object targetObject) {
    TargetSource targetSource = null;

    if (targetObject instanceof TargetSource) {
      targetSource = (TargetSource) targetObject;
    } else {
      targetSource = new SingletonTargetSource(targetObject);
    }

    return targetSource;
  }

  /**
   * @return the internal caching interceptor
   */
  protected NameMatchCachingInterceptor getCachingInterceptor() {
    return cachingInterceptor;
  }

  /**
   * @return the internal flushing interceptor
   */
  protected NameMatchFlushingInterceptor getFlushingInterceptor() {
    return flushingInterceptor;
  }

  /**
   * @return the created AOP proxy
   */
  protected Object getProxy() {
    return proxy;
  }

  /**
   * @return the interfaces to be implemented by the AOP proxy
   */
  protected Class[] getProxyInterfaces() {
    return proxyInterfaces;
  }

  /**
   * @return <code>true</code> if this proxy factory contains flushing models
   */
  protected boolean isHasFlushingModels() {
    return hasFlushingModels;
  }

}