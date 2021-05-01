# Cacheonix Distributed Java Cache

Cacheonix is an open source project that provides a stricly-consistent distributed Java cache, a fast local cache and a servlet request cache.

## cacheonix-core

Cacheonix's Core project contains the caching functionality.

## Java Web cache

Cacheonix 2.2.2 adds support for caching requests using a servlet filter. Key features include:

* Caching web requests
* Client-side cache control headers
* Automatic gzip compression of responses

Please visit Cacheonix wiki section [Cacheonix Java web cache](http://wiki.cacheonix.org/display/CCHNX20/Cacheonix+Web+Cache) for detailed information or concepts, configuration and use.  

## Local Cache

Cacheonix local cache improves application performance and verifical scalability by servicing a high-demand data from memory and by avoiding running into bottlenecks in the dababase and business tiers.

## Strictly-consistent Distributed Cache

One of the most important Cacheonix features is strict data consistency in a cluster. Cacheonix guarantees that once an update to a key happend, it's impossible to get an old value for that key. This makese Cacheonix suitable for mission critical applications such as e-commerce and banking. Also, Cacheonix allows developing a highly-performant applications using Hibernate that have to run in a cluster.   

## Cacheonix and Maven

Adding Cachenix to your project is easy. Just add the following to the dependencies section of your pom.xml:

```xml
<dependency>
   <groupId>org.cacheonix</groupId>
   <artifactId>cacheonix-core</artifactId>
   <version>2.2.2</version>
<dependency>
```

## Cacheonix Downloads

You can aslo add Cacheonix to your project directly by downloading Cacheonix jar, sources and the complete distribution from htttp://downloads.cacheonix.org.

## Documentation 

Chechk [Cacheonix Wiki](http://wiki.cacheonix.org/display/CCHNX/Cacheonix+Knowledge+Base) for detailed documentation.

## Contact Us

Shoot us an email at simeshev@cacheonix.org
