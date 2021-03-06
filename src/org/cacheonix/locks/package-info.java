/**
 * <p>Provides interfaces and classes for the distributed lock framework.</p>
 * <p/>
 * Cacheonix provides a distributed mutual exclusions API that allows clustered applications to maintain consistency by
 * serializing access to shared data. Multiple servers that modify shared resources concurrently may cause interference
 * and data inconsistency. Distributed locks by Cacheonix provide safety of data access, application liveness and
 * simplicity of programming.
 * <p/>
 * Cacheonix write locks ensure safe access to the shared data. At most one thread on one server may enter the section
 * of code protected by a distributed write lock. Cacheonix increases concurrency by offering read locks. Applications
 * that are only reading the shared data may execute the section of the code protected by read locks simultaneously.
 * <p/>
 * Cacheonix ensures liveness of clustered applications by building on top of its reliable symmetric clustering
 * protocol. Cacheonix seamlessly releases locks held by servers that fail or leave the cluster. Cacheonix prevents
 * application stalling by detecting deadlocks and breaking the deadlocks automatically.
 * <p/>
 * Cacheonix makes programming with distributed locks as simple as in a single Java VM. Cacheonix supports nested locks.
 * Cacheonix upgrades read locks to write locks when necessary. The distributed locks API has the same interface as
 * java.concurrent.locks:
 * <p/>
 * <pre>
 *
 * Cacheonix cacheonix = Cacheonix.getInstance();
 * ReadWriteLock readWriteLock = cacheonix.getCluster().getReadWriteLock();
 * Lock writeLock = readWriteLock.writeLock();
 * writeLock.lock();
 * try {
 * // Critical section protected by the lock
 * ...
 * } finally {
 * writeLock.unlock();
 * }
 * </pre>
 */
package org.cacheonix.locks;