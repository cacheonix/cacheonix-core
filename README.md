# Cacheonix Distributed Strictly-Consistent Key-Value Store

Cacheonix is an open source project that provides a strictly-consistent distributed key-value store written in Java. The main use cases include a distributed Java cache, a cloud-ready distributed Hibernate cache, a fast local JVM cache, a servlet HTTP request-response cache, and a large-scale, distributed, strictly-consistent key-value store.

Cacheonix supports clusters up to 1024 nodes.

## cacheonix-core

Cacheonix's Core project contains the caching functionality.

## Java Web cache

Cacheonix 2.2.2 adds support for caching requests using a servlet filter. Key features include:

* Caching web requests
* Client-side cache control headers
* Automatic gzip compression of responses

Please visit Cacheonix wiki section [Cacheonix Java web cache](http://wiki.cacheonix.org/display/CCHNX20/Cacheonix+Web+Cache) for detailed information or concepts, configuration and use.  

## Local Cache

Cacheonix local cache improves application performance and vertical scalability by servicing a high-demand data from memory and by avoiding running into bottlenecks in the database and business tiers.

## Strictly-consistent Distributed Cache

One of the most important Cacheonix features is strict data consistency in a cluster. Cacheonix guarantees that once an update to a key happened, it's impossible to get an old value for that key. This makes Cacheonix suitable for mission critical applications such as e-commerce and banking. Also, Cacheonix allows developing a highly-performant applications using Hibernate that have to run in a cluster.   

## Cacheonix and Maven

Adding Cacheonix to your project is easy. Just add the following to the dependencies section of your pom.xml:

```xml
<dependency>
   <groupId>org.cacheonix</groupId>
   <artifactId>cacheonix-core</artifactId>
   <version>2.2.2</version>
</dependency>
```

## Cacheonix Downloads

You can also add Cacheonix to your project directly by downloading Cacheonix jar, sources and the complete distribution from http://downloads.cacheonix.org.

## Documentation 

Check [Cacheonix Wiki](http://wiki.cacheonix.org/display/CCHNX/Cacheonix+Knowledge+Base) for detailed documentation.

## Contact Us

Shoot us an email at simeshev@cacheonix.org
