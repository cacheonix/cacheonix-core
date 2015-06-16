# Cacheonix Distributed Java Cache

Cacheonix is a distributed cache for Java capable of running is clusters as small as a few machines to thousands of machines.

## Strict Data Consistency

The most important feature of Cacheonix is strict data consistency. Cacheonix guarantees that once an update to a key happend, it's impossible to get an old value for that key. This makese Cacheonix suitable for mission critical applications such as e-commerce and banking. Also, Cacheonix allows developing a highly-performant applications using Hibernate that have to run in a cluster.   

## cacheonix-core

Cacheonix's Core project contains distributed caching functionality.
