/*
 * Cacheonix systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.com/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.cache.loader;

import java.io.Serializable;

/**
 * A loader of data into a cache. Use <code>CacheLoader</code> to populate Cacheonix at startup.
 * <p/>
 * Distributed Cacheonix calls CacheLoader when the first cache member joins the cluster.
 * <p/>
 * The name of the class implementing <code>CacheLoader</code> is configured using <code>loader</code> attribute of the
 * <code>cache</code> element in <a href="http://wiki.cacheonix.com/display/CCHNX20/Configuring+Cacheonix">cacheonix-config.xml</a>.
 * <p/>
 * <b>Example:</b>
 * <pre>
 *   &lt;cache name="my.cache" maxSize="1000"
 *          <b>loader=</b>"my.project.CacheLoaderImpl"
 *          <b>loaderProperties</b>="connectionURL=my/database/url;user=my_user;password=my_password"/&gt;
 * </pre>
 * <p/>
 * <b>Important:</b>
 * <p/>
 * Classes implementing <code>CacheLoader</code> must provide a public no-argument constructor.
 *
 * @author <a href="mailto:simeshev@cacheonix.org">Slava Imeshev</a>
 */
public interface CacheLoader {

   /**
    * Sets cache loader context. Cacheonix will call this method immediately after creating an instance of the class
    * that implements <code>CacheLoader</code>.
    *
    * @param context an instance of {@link CacheLoaderContext}
    */
   void setContext(final CacheLoaderContext context);


   /**
    * Cacheonix will call this method shortly after calling <code>setContext()</code>.  Classes implementing
    * <code>load()</code> must call {@link Loadable#load(Serializable, Serializable)} to load data into Cacheonix.
    *
    * @param loadable a key/value representation of the cache.
    */
   void load(Loadable loadable);
}
