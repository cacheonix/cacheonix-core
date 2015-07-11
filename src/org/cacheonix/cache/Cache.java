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
package org.cacheonix.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.cacheonix.Cacheonix;
import org.cacheonix.cache.entry.EntryFilter;
import org.cacheonix.cache.executor.Aggregator;
import org.cacheonix.cache.executor.Executable;
import org.cacheonix.cache.subscriber.EntryModifiedEvent;
import org.cacheonix.cache.subscriber.EntryModifiedSubscriber;
import org.cacheonix.cluster.CacheMember;
import org.cacheonix.exceptions.NotSubscribedException;
import org.cacheonix.locks.ReadWriteLock;

/**
 * A Cacheonix cache. <p/> Use <code>Cacheonix.getInstance().getCache()</code> to obtain an instance of the cache.
 * Example:
 * <pre>
 * // Get cache
 * final Cache<String, String> cache = Cacheonix.getInstance().getCache("my.cache");
 * &nbsp;
 * // Get value associated with a key
 * final String key = "key";
 * final String value = cache.get(key);
 * &nbsp;
 * // Put cache value
 * fina String previousValue = cache.put(key, "new.value");
 * </pre>
 * <p>Visit <a href="http://wiki.cacheonix.com/display/CCHNX20/Programming+With+Cacheonix">online code examples</a> for
 * more examples on working with the cache API.</p>
 * <p/>
 * <b>Configuring a cache</b> <p/> A typical distributed cache configuration defined in the cacheonix-config.xml looks
 * as the following:
 * <pre>
 * &lt;partitionedCache name="my.cache"&gt;
 *    &lt;store&gt;
 *       &lt;size&gt;
 *          &lt;lru maxBytes="1mb" maxElements="1000"/&gt;
 *       &lt;/size&gt;
 *    &lt;/store&gt;
 * &lt;/partitionedCache&gt;
 * </pre>
 * Please visit <a href="http://wiki.cacheonix.com/display/CCHNX20/Configuring+Cacheonix"> Cacheonix knowledge base</a>
 * for detailed information on configuring Cacheonix.
 *
 * @see Cacheonix#getCache(String)
 */
public interface Cache<K extends Serializable, V extends Serializable> extends ConcurrentMap<K, V> {

   /**
    * Returns the number of key-value mappings in this map.  If the map contains more than <tt>Integer.MAX_VALUE</tt>
    * elements, returns <tt>Integer.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this map.
    */
   int size();


   /**
    * Removes all mappings from this map.
    *
    * @throws UnsupportedOperationException clear is not supported by this map.
    */
   void clear();


   /**
    * Returns <tt>true</tt> if this map contains no key-value mappings.
    *
    * @return <tt>true</tt> if this map contains no key-value mappings.
    */
   boolean isEmpty();


   /**
    * Returns <tt>true</tt> if this map contains a mapping for the specified key.  More formally, returns <tt>true</tt>
    * if and only if this map contains at a mapping for a key <tt>k</tt> such that <tt>(key==null ? k==null :
    * key.equals(k))</tt>.  (There can be at most one such mapping.)
    *
    * @param key key whose presence in this map is to be tested. The key must implement
    *            <code>java.io.Serializable</code>.
    * @return <tt>true</tt> if this map contains a mapping for the specified key.
    * @throws ClassCastException   if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException if the key is <tt>null</tt> and this map does not permit <tt>null</tt> keys
    *                              (optional).
    */
   boolean containsKey(Object key);


   /**
    * Returns <tt>true</tt> if this map maps one or more keys to the specified value.  More formally, returns
    * <tt>true</tt> if and only if this map contains at least one mapping to a value <tt>v</tt> such that
    * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation will probably require time linear in the map
    * size for most implementations of the <tt>Map</tt> interface.
    *
    * @param value value whose presence in this map is to be tested.  The value must implement
    *              <code>java.io.Serializable</code>.
    * @return <tt>true</tt> if this map maps one or more keys to the specified value.
    * @throws ClassCastException   if the value is of an inappropriate type for this map (optional).
    * @throws NullPointerException if the value is <tt>null</tt> and this map does not permit <tt>null</tt> values
    *                              (optional).
    */
   boolean containsValue(Object value);


   /**
    * Returns a collection view of the values contained in this map.  The collection is detached from the map, so
    * changes to the map are not reflected in the collection, and vice-versa.
    *
    * @return a collection of the values contained in this map.
    */
   Collection<V> values();


   /**
    * Copies all of the mappings from the specified map to this map.  The effect of this call is equivalent to that of
    * calling {@link #put(Object, Object) put(k, v)} on this map once for each mapping from key <tt>k</tt> to value
    * <tt>v</tt> in the specified map. The behavior of this operation is unspecified if the specified map is modified
    * while the operation is in progress.
    *
    * @param t Mappings to be stored in this map.
    * @throws UnsupportedOperationException if the <tt>putAll</tt> method is not supported by this map.
    * @throws ClassCastException            if the class of a key or value in the specified map prevents it from being
    *                                       stored in this map.
    * @throws IllegalArgumentException      some aspect of a key or value in the specified map prevents it from being
    *                                       stored in this map.
    * @throws NullPointerException          the specified map is <tt>null</tt>, or if this map does not permit
    *                                       <tt>null</tt> keys or values, and the specified map contains <tt>null</tt>
    *                                       keys or values.
    */
   void putAll(Map<? extends K, ? extends V> t);

   /**
    * Returns a set view of the mappings contained in this map.  Each element in the returned set is a Map.Entry.  The
    * set is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified
    * while an iteration over the set is in progress, the results of the iteration are undefined.  The set supports
    * element removal, which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
    * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
    * the <tt>add</tt> or <tt>addAll</tt> operations.
    *
    * @return a set view of the mappings contained in this map.
    */
   Set<Entry<K, V>> entrySet();


   /**
    * Returns a set view of the keys contained in this map.  The set is backed by the map, so changes to the map are
    * reflected in the set, and vice-versa.  If the map is modified while an iteration over the set is in progress, the
    * results of the iteration are undefined.  The set supports element removal, which removes the corresponding mapping
    * from the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt> <tt>retainAll</tt>, and
    * <tt>clear</tt> operations. It does not support add or <tt>addAll</tt> operations.
    *
    * @return a set view of the keys contained in this map.
    */
   Set<K> keySet();


   /**
    * Returns the value to which this map maps the specified key.  Returns <tt>null</tt> if the map contains no mapping
    * for this key.  A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the map contains no
    * mapping for the key; it's also possible that the map explicitly maps the key to <tt>null</tt>.  The
    * <tt>containsKey</tt> operation may be used to distinguish these two cases.
    * <p/>
    * <p>More formally, if this map contains a mapping from a key <tt>k</tt> to a value <tt>v</tt> such that
    * <tt>(key==null ? k==null : key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise it returns
    * <tt>null</tt>.  (There can be at most one such mapping.)
    *
    * @param key key whose associated value is to be returned. The key must implement <code>java.io.Serializable</code>.
    * @return the value to which this map maps the specified key, or <tt>null</tt> if the map contains no mapping for
    *         this key.
    * @throws ClassCastException   if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException key is <tt>null</tt> and this map does not permit <tt>null</tt> keys (optional).
    * @see #containsKey(Object)
    */
   V get(Object key);


   /**
    * Removes the mapping for this key from this map if it is present.   More formally, if this map contains a mapping
    * from key <tt>k</tt> to value <tt>v</tt> such that <code>(key==null ? k==null : key.equals(k))</code>, that mapping
    * is removed.  (The map can contain at most one such mapping.)
    * <p/>
    * <p>Returns the value to which the map previously associated the key, or <tt>null</tt> if the map contained no
    * mapping for this key.  (A <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt>
    * with the specified key if the implementation supports <tt>null</tt> values.)  The map will not contain a mapping
    * for the specified  key once the call returns.
    *
    * @param key key whose mapping is to be removed from the map. The key must implement
    *            <code>java.io.Serializable</code>.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.
    * @throws ClassCastException            if the key is of an inappropriate type for this map (optional).
    * @throws NullPointerException          if the key is <tt>null</tt> and this map does not permit <tt>null</tt> keys
    *                                       (optional).
    * @throws UnsupportedOperationException if the <tt>remove</tt> method is not supported by this map.
    */
   V remove(Object key);


   /**
    * Removes an entry for the key only if it is currently mapped to the given value. Acts as
    * <pre>
    * if ((map.containsKey(key) && map.get(key).equals(value)) {
    *    map.remove(key);
    *    return true;
    * } else {
    *    return false;
    * }
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key   a key with which the specified value is associated. The key must implement
    *              <code>java.io.Serializable</code>.
    * @param value a value associated with the specified key. The key must implement <code>java.io.Serializable</code>.
    * @return true if the value was removed, false otherwise.
    */
   boolean remove(Object key, Object value);

   /**
    * Replaces an entry for the key only if it is currently mapped to the given value. Acts as
    * <pre>
    *  if ((map.containsKey(key) && map.get(key).equals(oldValue)) {
    *     map.put(key, newValue);
    *     return true;
    * } else {
    *    return false;
    * }
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key      a key with which the specified value is associated with.
    * @param oldValue a value expected to be associated with the specified key.
    * @param newValue a value to be associated with the specified key.
    * @return true if the value was replaced
    */
   boolean replace(K key, V oldValue, V newValue);

   /**
    * Replaces an entry for the key only if it is currently mapped to some value. Acts as
    * <pre>
    *    if ((map.containsKey(key)) {
    *       return map.put(key, value);
    *    } else {
    *       return null;
    *    }
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key   a key with which the specified value is associated with.
    * @param value a value to be associated with the specified key.
    * @return a previous value associated with the specified key, or null if there was no mapping for the key. A null
    *         can also indicate that the map previously associated null with the specified key, if the implementation
    *         supports null values.
    */
   V replace(K key, V value);


   /**
    * Associates the specified value with the specified key in this map.  If the map previously contained a mapping for
    * this key, the old value is replaced by the specified value.  (A map <tt>m</tt> is said to contain a mapping for a
    * key <tt>k</tt> if and only if {@link #containsKey(Object) m.containsKey(k)} would return <tt>true</tt>.))
    *
    * @param key   key with which the specified value is to be associated.
    * @param value value to be associated with the specified key.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  A
    *         <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with the specified
    *         key, if the implementation supports <tt>null</tt> values.
    * @throws UnsupportedOperationException if the <tt>put</tt> operation is not supported by this map.
    * @throws ClassCastException            if the class of the specified key or value prevents it from being stored in
    *                                       this map.
    * @throws IllegalArgumentException      if some aspect of this key or value prevents it from being stored in this
    *                                       map.
    * @throws NullPointerException          this map does not permit <tt>null</tt> keys or values, and the specified key
    *                                       or value is <tt>null</tt>.
    */
   V put(K key, V value);

   /**
    * Associates the specified value with the specified key in this map.  If the map previously contained a mapping for
    * this key, the old value is replaced by the specified value.  (A map <tt>m</tt> is said to contain a mapping for a
    * key <tt>k</tt> if and only if {@link #containsKey(Object) m.containsKey(k)} would return <tt>true</tt>.))
    *
    * @param key                  key with which the specified value is to be associated.
    * @param value                value to be associated with the specified key.
    * @param expirationTimeMillis time after the cache element expires. This time overrides the expiration time set in
    *                             the cache configuration. Setting <code>expirationTimeMillis</code> to zero disables
    *                             expiration for the cache element.
    * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  A
    *         <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with the specified
    *         key, if the implementation supports <tt>null</tt> values.
    * @throws UnsupportedOperationException if the <tt>put</tt> operation is not supported by this map.
    * @throws ClassCastException            if the class of the specified key or value prevents it from being stored in
    *                                       this map.
    * @throws IllegalArgumentException      if some aspect of this key or value prevents it from being stored in this
    *                                       map.
    * @throws NullPointerException          this map does not permit <tt>null</tt> keys or values, and the specified key
    *                                       or value is <tt>null</tt>.
    */
   V put(K key, V value, long expirationTimeMillis);


   /**
    * If the specified key is not already associated with a value, associate it with the given value. This is equivalent
    * to
    * <pre>
    *    if (!map.containsKey(key))
    *       return map.put(key, value);
    *    else
    *       return map.get(key);
    * </pre>
    * except that the action is performed atomically.
    *
    * @param key   a key with which the specified value is to be associated.
    * @param value a value to be associated with the specified key.
    * @return a previous value associated with the specified key, or null if there was no mapping for the key.
    * @throws NullPointerException if the specified key is null.
    */
   V putIfAbsent(K key, V value);

   /**
    * Returns current cache statistics. The statistics is returned in a {@link CacheStatistics} object.
    *
    * @return current cache statistics.
    */
   CacheStatistics getStatistics();


   /**
    * Returns cache name.
    *
    * @return cache name.
    */
   String getName();


   /**
    * Returns maximum number of elements in memory.
    *
    * @return maximum number of elements in memory.
    */
   long getMaxSize();


   /**
    * Returns maximum size of the cache in bytes. Cacheonix does not limit the cache size if the maximum size is not set
    * or if it is set to zero.
    *
    * @return maximum size of the cache in bytes.
    */
   long getMaxSizeBytes();


   /**
    * Returns number of elements evicted to disk.
    *
    * @return number of elements evicted to disk.
    */
   long getSizeOnDisk();


   /**
    * Returns the number of key-value mappings in this map.  If the map contains more than <tt>Long.MAX_VALUE</tt>
    * elements, returns <tt>Long.MAX_VALUE</tt>.
    *
    * @return the number of key-value mappings in this map.
    */
   long longSize();


   /**
    * Returns <code>CacheMember</code> that is responsible for storing a given key at the moment of call.
    *
    * @param key a key for that to return the key owner.
    * @return <code>CacheMember</code> that is responsible for storing a given key.
    */
   CacheMember getKeyOwner(final K key);


   /**
    * Adds a subscriber to an event when a cache element is added, updated or removed.
    * <p/>
    * The <code>subscriber</code> may begin receiving <code>EntryModifiedEvent</code>s before this method returns
    * control to a calling thread.
    * <p/>
    * Use <code>removeEventSubscriber()</code> to un-subscribe.
    *
    * @param keys       a set of keys of interest.
    * @param subscriber the subscriber to an event when a cache element is added, updated or removed.
    * @see #removeEventSubscriber(Set, EntryModifiedSubscriber)
    * @see #addEventSubscriber(Serializable, EntryModifiedSubscriber)
    * @see EntryModifiedEvent
    */
   void addEventSubscriber(final Set<K> keys, final EntryModifiedSubscriber subscriber);


   /**
    * Adds a subscriber to an event when a cache element is added, updated or removed.
    * <p/>
    * The <code>subscriber</code> may begin receiving <code>EntryModifiedEvent</code>s before this method returns
    * control to a calling thread.
    * <p/>
    * Use <code>removeEventSubscriber()</code> to un-subscribe.
    * <p/>
    * Use <code>addEventSubscriber(Set, EntryModifiedSubscriber)</code> to subscribe to modifications to a set of keys.
    *
    * @param key        a key of interest.
    * @param subscriber the subscriber to an event when a cache element is added, updated or removed.
    * @see #removeEventSubscriber(Set, EntryModifiedSubscriber)
    * @see #addEventSubscriber(Set, EntryModifiedSubscriber)
    * @see EntryModifiedEvent
    */
   void addEventSubscriber(final K key, final EntryModifiedSubscriber subscriber);


   /**
    * Un-subscribes the subscriber previously added by <code>addEventSubscriber()</code>.
    *
    * @param keys       a set of keys of interest. The keys should be a subset of a set of keys supplied to
    *                   <code>addEventSubscriber()</code>
    * @param subscriber the subscriber to un-subscribe.
    * @throws NotSubscribedException if the subscriber is not subscribed to any of the keys.
    * @see #addEventSubscriber(Set, EntryModifiedSubscriber)
    */
   void removeEventSubscriber(final Set<K> keys,
                              final EntryModifiedSubscriber subscriber) throws NotSubscribedException;


   /**
    * Un-subscribes the subscriber previously added by <code>addEventSubscriber()</code>.
    *
    * @param key        a key of interest. The keys should be a part of a set of keys supplied to
    *                   <code>addEventSubscriber()</code>
    * @param subscriber the subscriber to un-subscribe.
    * @throws NotSubscribedException if the subscriber is not subscribed to any of the keys.
    * @see #addEventSubscriber(Serializable, EntryModifiedSubscriber)
    * @see #addEventSubscriber(Set, EntryModifiedSubscriber)
    */
   void removeEventSubscriber(final K key, final EntryModifiedSubscriber subscriber) throws NotSubscribedException;


   /**
    * Returns size of keys and values in bytes. Returns zero if eviction based on object size is not enabled for this
    * cache.
    *
    * @return size of keys and values in bytes or zero if eviction based on object size is not enabled for this cache.
    */
   long getSizeBytes();


   /**
    * Returns a cluster-wide lock. This method is equavalent of <code>getReadWriteLock("default")</code>. The created
    * lock is distributed and accessible by all members of the cluster.
    *
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock();

   /**
    * Returns a named, cluster-wide lock. The created lock is distributed and accessible by all members of the cluster.
    *
    * @param lockKey the case-sensitive name of the lock.
    * @return the created lock.
    */
   ReadWriteLock getReadWriteLock(final Serializable lockKey);

   /**
    * Returns a list of key owners.
    *
    * @return the list of key owners.
    */
   List getKeyOwners();

   /**
    * Removes all cache entries that match the <code>keySet</code>.
    *
    * @param keySet the set of keys to remove.
    * @return <code>true</code> if any of the keys was removed.n
    */
   boolean removeAll(Set<K> keySet);


   /**
    * Returns cache entries that match the <code>keySet</code>.
    *
    * @param keys the set of keys to return.
    * @return the cache entries that match the <code>keySet</code>.
    */
   Map<K, V> getAll(Set<K> keys);

   /**
    * Removes any entries in the cache which are not contained in the <code>keySet</code>.
    *
    * @param keySet the set of keys to retain.
    * @return <code>true</code> if the cache was modified by the <code>retainAll()</code> operation.
    */
   boolean retainAll(Set<K> keySet);

   /**
    * Invokes the <code>executable</code> for all entries in this cache. The <code>aggregator</code> performs conversion
    * of partial results provided by the <code>executable</code> to the final results.
    * <p/>
    * <b>Distributed cache</b>: Using this method significantly increases speed of operations that should be performed
    * on all cache entries. The <code>executable</code> runs in parallel on all nodes of the cluster that carry cached
    * data. As such, <code>executable</code> utilizes data affinity by processing a subset of the cached data that is
    * local to a cluster node it runs which completely eliminates latency caused by network data transfers.
    *
    * @param executable the executable to invoke.
    * @param aggregator the aggregator to call to convert partial results provided by executable to the final result.
    * @return the result of execution.
    * @see #executeAll(Set, Executable, Aggregator)
    * @see #execute(EntryFilter, Executable, Aggregator)
    */
   Serializable execute(Executable executable, Aggregator aggregator);

   /**
    * Invokes the <code>executable</code> for all entries in this cache that satisfy the filtering criteria provided by
    * the <code>entryFilter</code>. The <code>aggregator</code> performs conversion of partial results provided by the
    * <code>executable</code> to the final results.
    * <p/>
    * <b>Distributed cache</b>: Using this method significantly increases speed of operations that should be performed
    * on all cache entries. The <code>executable</code> runs in parallel on all nodes of the cluster that carry cached
    * data. As such, <code>executable</code> utilizes data affinity by processing a subset of the cached data that is
    * local to a cluster node it runs which completely eliminates latency caused by network data transfers.
    *
    * @param entryFilter the filter that is called to decide if a cache entry should be processed.
    * @param executable  the executable to invoke.
    * @param aggregator  the aggregator to call to convert partial results provided by executable to the final result.
    * @return the result of execution.
    * @see #executeAll(Set, Executable, Aggregator)
    * @see #execute(Executable, Aggregator)
    */
   Serializable execute(EntryFilter entryFilter, Executable executable, Aggregator aggregator);

   /**
    * Invokes the <code>executable</code> for a subset of entries in this cache that is defined by the
    * <code>keySet</code>. An <code>aggregator</code> performs conversion of partial results provided by the
    * <code>executable</code> to the final results.
    * <p/>
    * <b>Distributed cache</b>: Using this method significantly increases speed of operations that should be performed
    * on a set of cache entries. The <code>executable</code> runs in parallel on all nodes of the cluster that carry
    * cached data. As such, <code>executable</code> utilizes data affinity by processing a subset of the cached data
    * that is local to a cluster node it runs which completely eliminates latency caused by network data transfers.
    *
    * @param keySet     the set of keys used to limit the execution.
    * @param executable the executable to invoke.
    * @param aggregator the aggregator to call to convert partial results provided by executable to the final result.
    * @return the result of execution.
    * @see #execute(Executable, Aggregator)
    * @see #execute(EntryFilter, Executable, Aggregator)
    */
   Serializable executeAll(Set<K> keySet, Executable executable, Aggregator aggregator);
}
