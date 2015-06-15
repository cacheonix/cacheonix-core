=========================================
== Spring JPetStore sample application ==
=========================================

@author Juergen Hoeller
Based on Clinton Begin's JPetStore (http://www.ibatis.com).


1. MOTIVATION

Features a Spring-managed middle tier with iBATIS Database Layer as data access
strategy, in combination with Spring's transaction and DAO abstractions.
Can work with local JDBC transactions or JTA, with the latter on two databases.
Uses the same data model and demo contents as the original JPetStore.
See the context definitions "WEB-INF/dataAccessContext-local.xml" respectively
"WEB-INF/dataAccessContext-jta.xml" for details.

Offers two alternative web tier implementations with the same user interface:
one based on Spring's web MVC, and one based on Struts 1.1. The latter is close
to the original JPetStore but reworked for JSTL, to make the JSP implementations
as comparable as possible. See "WEB-INF/web.xml", "WEB-INF/petstore-servlet.xml",
and "WEB-INF/struts-config.xml" for details.

Compared to the original JPetStore, this implementation is significantly
improved in terms of internal structure and loose coupling: Leveraging Spring's
application context concept, there's a central place for wiring application
objects now. The most notable improvement is the former PetStoreLogic, now
called PetStoreFacade: It is no longer concerned with configuration, resource,
or transaction details.

Note that the Spring-based web tier implementation is deliberately similar to
the Struts-based one and does not aim to improve in terms of in-place error
messages or the like. The inclusion of two web tier alternatives outlines the
differences as well as the similarities in the respective programming model,
and also illustrates the different configuration styles.

This version of JPetStore also demonstrates various remoting options with Spring:
Hessian, Burlap, RMI, and Web Services via Apache Axis. They are all provided
out-of-the-box by the default web application (note that RMI is commented out
to avoid conflicts with EJB containers). The "client" directory contains a simple
command-line client that invokes the exported OrderService via all protocols.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.

To execute the web application with its default settings, simply start the
HSQLDB instance in the "db/hsqldb" directory, for example using "server.bat".
For other databases, you'll need to use the corresponding schema and load scripts
in the "db" subdirectories (same as with the original JPetStore). In the local
case, the JDBC settings can be adapted in "WEB-INF/jdbc.properties". With JTA,
you need to set up corresponding DataSources in your J2EE container.

Note that the "WEB-INF/dataAccessContext-*.xml" files might have to be adapted
for certain databases like MS-SQL and Oracle, to use appropriate generation
strategies for order IDs. See the corresponding commented-out DAO definitions
in the context XML files. WARNING: As of Spring 1.0 M4, only HSQLDB will be
properly tested. We are happy to accept any feedback on other databases.

A guide to step-by-step deployment, assuming JDK 1.4.x and Tomcat 4.x:
1. if not already set, set the JAVA_HOME environment variable
2. run "ant warfile" respectively "warfile.bat" to generate the WAR file
3. copy the generated "dist/jpetstore.war" to Tomcat's "webapps" directory
4. start HSQLDB via "db/hsqldb/server.bat" respectively "server.sh"
5. start Tomcat (default port will be 8080)
6. open "http://localhost:8080/jpetstore" in an Internet browser

If you want to test remote service access:
1. create an order with the JPetStore web UI (first order number will be 1000)
2. adapt server URL in "client/client.properties" (if not using the default)
3. switch to the "client" directory as execution directory
4. run "client.bat 1000" to fetch and show the order with number 1000
5. run "client.bat 1000 10" to fetch the order 10 times per protocol


3. VERSIONS WITH SOURCE-LEVEL METADATA

See the "attributes" and "annotation" directories for examples of declarative
transaction management driven by source-level metadata. These versions leverage
Spring's simple metadata model, similar to that of .NET Enterprise Services, but
extensible to arbitrary (possibly application-specific) declarative services.

The JPetStore version in the "attributes" directory uses Jakarta Commons
Attributes, which works on JDK >= 1.3. The version in the "annotation" directory
uses JDK 1.5+ annotations in an analogous fashion (available since Spring 1.2).

Each directory has its own build file, which invokes the attribute compilation
process and builds the WAR, using its own application context XML files from its
"WEB-INF" subdirectory. The WAR file will be created in the "dist" directory.

