# Cacheonix Distributed Java Cache

Cacheonix is a distributed cache for Java capable of running in clusters small and large.

## cacheonix-core

Cacheonix's Core project contains distributed caching functionality.

## Cacheonix and Maven
```xml
<dependency>
   <groupId>org.cacheonix</groupId>
   <artifactId>cacheonix-core</artifactId>
   <version>2.1.1</version>
<dependency>
```

## Strict Data Consistency in A Cluster

The most important feature of Cacheonix is strict data consistency in a cluster. Cacheonix guarantees that once an update to a key happend, it's impossible to get an old value for that key. This makese Cacheonix suitable for mission critical applications such as e-commerce and banking. Also, Cacheonix allows developing a highly-performant applications using Hibernate that have to run in a cluster.   

## Documentation 

Chechk [Cacheonix Wiki](http://wiki.cacheonix.org/display/CCHNX/Cacheonix+Knowledge+Base) for detailed documentation.
