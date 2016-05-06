# Cacheonix Distributed Java Cache

Cacheonix is a distributed cache for Java capable of running in clusters small and large.

## cacheonix-core

Cacheonix's Core project contains the distributed caching functionality.

## Cacheonix and Maven
```xml
<dependency>
   <groupId>org.cacheonix</groupId>
   <artifactId>cacheonix-core</artifactId>
   <version>2.2.2</version>
<dependency>
```

## Java Web Cache

Cacheonix 2.2.2 adds support for caching requests using a servlet filter. Key features include:

* Caching web requests
* Client-side cache control headers
* Automatic gzip compression of responses

Please visit Cacheonix wiki section [Cacheonix Java web cache](http://wiki.cacheonix.org/display/CCHNX20/Cacheonix+Web+Cache) for detailed information or concepts, configuration and use.  

## Strict Data Consistency in A Cluster

The most important feature of Cacheonix is strict data consistency in a cluster. Cacheonix guarantees that once an update to a key happend, it's impossible to get an old value for that key. This makese Cacheonix suitable for mission critical applications such as e-commerce and banking. Also, Cacheonix allows developing a highly-performant applications using Hibernate that have to run in a cluster.   

## Documentation 

Chechk [Cacheonix Wiki](http://wiki.cacheonix.org/display/CCHNX/Cacheonix+Knowledge+Base) for detailed documentation.
