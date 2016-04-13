package org.cacheonix.cache.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cacheonix.impl.cache.web.RequestCacheFilterImpl;

/**
 * This filter provides an ability to cache web app requests. To enable request caching, add the following lines to the
 * web app's <code>web.xml</code>
 * <p/>
 * <strong>Minimal configuration:</strong>
 * <p/>
 * <pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;RequestCacheFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.cacheonix.cache.web.RequestCacheFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * </pre>
 * <p/>
 * The above will create a cache using the default built-in configuration with the name the same as the filter name,
 * <code>RequestCacheFilter</code> in this case.
 * <p/>
 * Cacheonix also supports an advanced configuration that allows setting the cache name and using a custom cache
 * configuration.
 * <p/>
 * <strong>Advanced configuration:</strong>
 * <p/>
 * <pre>
 *
 * &lt;filter&gt;
 *    &lt;filter-name&gt;RequestCacheFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.cacheonix.cache.web.RequestCacheFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;cacheName&lt;/param-name&gt;
 *       &lt;param-value&gt;request-cache&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;configurationPath&lt;/param-name&gt;
 *       &lt;param-value&gt;custom-cacheonix-config.xml&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 */
public final class RequestCacheFilter implements Filter {

   private final Filter delegate = new RequestCacheFilterImpl();


   public void init(final FilterConfig filterConfig) throws ServletException {

      delegate.init(filterConfig);
   }


   public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
           final FilterChain filterChain) throws IOException, ServletException {

      delegate.doFilter(servletRequest, servletResponse, filterChain);
   }


   public void destroy() {

      delegate.destroy();
   }


   public String toString() {

      return "RequestCacheFilter{" +
              "delegate=" + delegate +
              '}';
   }
}
