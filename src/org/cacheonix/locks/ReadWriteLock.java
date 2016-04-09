/*
 * Cacheonix Systems licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.cacheonix.org/products/cacheonix/license-lgpl-2.1.htm
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.locks;

import java.io.Serializable;

/**
 * A <tt>ReadWriteLock</tt> maintains a pair of associated {@link Lock locks}, one for read-only operations and one for
 * writing. The {@link #readLock read lock} may be held simultaneously by multiple reader threads, so long as there are
 * no writers.  The {@link #writeLock write lock} is exclusive. <p>Cacheonix implementation of <tt>ReadWriteLock</tt>
 * guarantees that the memory synchronization effects of <tt>writeLock</tt> operations (as specified in the {@link Lock}
 * interface) also hold with respect to the associated <tt>readLock</tt>. That is, a thread successfully acquiring the
 * read lock will see all updates made upon previous release of the write lock. <p>A read-write lock allows for a
 * greater level of concurrency in accessing shared data than that permitted by a mutual exclusion lock. It exploits the
 * fact that while only a single thread at a time (a <em>writer</em> thread) can modify the shared data, in many cases
 * any number of threads can concurrently read the data (hence <em>reader</em> threads). In theory, the increase in
 * concurrency permitted by the use of a read-write lock will lead to performance improvements over the use of a mutual
 * exclusion lock. In practice this increase in concurrency will only be fully realized on a multi-processor, and then
 * only if the access patterns for the shared data are suitable.</p> <p>Whether or not a read-write lock will improve
 * performance over the use of a mutual exclusion lock depends on the frequency that the data is read compared to being
 * modified, the duration of the read and write operations, and the contention for the data - that is, the number of
 * threads that will try to read or write the data at the same time. For example, a collection that is initially
 * populated with data and thereafter infrequently modified, while being frequently searched (such as a directory of
 * some kind) is an ideal candidate for the use of a read-write lock. However, if updates become frequent then the data
 * spends most of its time being exclusively locked and there is little, if any increase in concurrency. Further, if the
 * read operations are too short the overhead of the read-write lock implementation (which is inherently more complex
 * than a mutual exclusion lock) can dominate the execution cost, particularly as many read-write lock implementations
 * still serialize all threads through a small section of code. Ultimately, only profiling and measurement will
 * establish whether the use of a read-write lock is suitable for your application.</p> <p>Although the basic operation
 * of a read-write lock is straight-forward, there are many policy decisions that an implementation must make, which may
 * affect the effectiveness of the read-write lock in a given application. Examples of these policies include: </p> <ul>
 * <li>Determining whether to grant the read lock or the write lock, when both readers and writers are waiting, at the
 * time that a writer releases the write lock. Writer preference is common, as writes are expected to be short and
 * infrequent. Reader preference is less common as it can lead to lengthy delays for a write if the readers are frequent
 * and long-lived as expected. Fair, or &quot;in-order&quot; implementations are also possible.</li> <li>Determining
 * whether readers that request the read lock while a reader is active and a writer is waiting, are granted the read
 * lock. Preference to the reader can delay the writer indefinitely, while preference to the writer can reduce the
 * potential for concurrency. </li> <li>Determining whether the locks are reentrant: can a thread with the write lock
 * reacquire it? Can it acquire a read lock while holding the write lock? Is the read lock itself reentrant?</li>
 * <li>Can the write lock be downgraded to a read lock without allowing an intervening writer? Can a read lock be
 * upgraded to a write lock, in preference to other waiting readers or writers? </li></ul>
 * <p/>
 * You should consider all of these things when evaluating the suitability of a given implementation for your
 * application.
 * <p/>
 * Acquiring Distributed Read-write Lock
 * <p/>
 * Cacheonix enables concurrent object-oriented programing in a cluster by providing distributed shared locks. Cacheonix
 * ensures application liveness by building distributed locks on top of its reliable symmetric clustering protocol that
 * automatically releases locks when servers fail while holding a lock.
 * <p/>
 * Distributed Cacheonix supports nested locks and automatically upgrades distributed read locks to write locks when
 * necessary. Cacheonix makes acquiring and releasing distributed locks as simple as in a single Java VM:
 * <p/>
 * Acquiring a distributed read-write lock in Cacheonix is very easy. Below is an example that shows a normal use
 * pattern for distributed locks:
 * <p/>
 * <pre>
 *    Cacheonix cacheonix = Cacheonix.getInstance();
 *    ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
 *    Lock readLock = readWriteLock.readLock();
 *    readLock.lock();
 *    try {
 *       // Critical section protected by the lock
 *       ...
 *    } finally {
 *       lock.unlock();
 *    }
 * </pre>
 *
 * @see Lock
 */
public interface ReadWriteLock extends java.util.concurrent.locks.ReadWriteLock {

   /**
    * Returns the lock used for reading.
    *
    * @return the lock used for reading.
    */
   Lock readLock();

   /**
    * Returns the lock used for writing.
    *
    * @return the lock used for writing.
    */
   Lock writeLock();


   /**
    * Returns a lock key.
    *
    * @return the lock key.
    */
   Serializable getLockKey();
}
