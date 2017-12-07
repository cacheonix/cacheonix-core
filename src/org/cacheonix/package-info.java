/**
 * <p>A root package for Cacheonix package hierarchy.</p>
 *
 * <h1>Cacheonix Overview</h1>
 *
 * <p>Cacheonix is a coherent distributed Java cache and in-memory data grid for mission-critical applications that
 * require reliable operation in presence of server failures. Cacheonix provides a clean, extensive API for backend
 * reads and writes and notifications about updates to cache keys. Cacheonix offers an ability to process cache data
 * with data affinity in parallel using executor API. Cacheonix fully supports clustered distributed mutual exclusions
 * and distributed nested read-write locks. Cacheonix also provides plugins for Hibernate, MyBatis and iBatis.</p>
 *
 * <h1>Cacheonix in Three Lines</h1>
 *
 * <p>Cacheonix is very easy to use. Here is a 3-line example that shows how to get a cache, to put a key in a cache and how to get a key:</p>
 *
 * <pre>
 *
 * Cache&lt;Integer, String&gt; cache = Cacheonix.getInstance().getCache("MyCache");
 * cache.put(1, "MyValue);
 * String value = cache.get(1);
 *
 * </pre>
 *
 * <h1>Documentation</h1>
 *
 * <ul>
 *   <li><a href="http://wiki.cacheonix.org/display/CCHNX20/Configuring+Cacheonix">Configuring Cacheonix</a></li>
 *   <li><a href="http://wiki.cacheonix.org/display/CCHNX/Cacheonix+Knowledge+Base">Online Documentation</a></li>
 *   <li><a href="http://wiki.cacheonix.org/display/CCHNX20/Programming+With+Cacheonix">Online Code Examples</a></li>
 * </ul>
 *
 * <h1>High-level Cacheonix Packages</h1>
 *
 * <table cellpadding="5" border="1">
 *    <tr><td> {@link org.cacheonix.cache} </td><td> Provides interfaces and classes for the cache framework </td></tr>
 *    <tr><td> {@link org.cacheonix.cluster} </td><td> Provides interfaces and classes for accessing cluster framework </td></tr>
 *    <tr><td> {@link org.cacheonix.locks} </td><td> Provides interfaces and classes for the distributed locks framework </td></tr>
 *    <tr><td> {@link org.cacheonix.plugin} </td><td> Contains plug-ins for third-party products such as Spring, Hibernate, MyBatis and iBatis </td></tr>
 *    <tr><td> {@link org.cacheonix.util} </td><td> Provides useful utility classes for testing network environment </td></tr>
 * </table>
 *
 * <p>Package <code>cacheonix</code> itself contains class {@link org.cacheonix.Version} the provides information about Cacheonix version.</p>
 *
 * <p><strong>See Also:</strong>
 *
 * <ul>
 *    <li><a href="http://www.cacheonix.org/products/cacheonix/index.htm">Cacheonix Home</a></li>
 *    <li><a href="http://www.cacheonix.org/support.htm">Cacheonix Support</a></li>
 * </ul>
 * </p>
 * @since Cacheonix 1.0
 */
package org.cacheonix;