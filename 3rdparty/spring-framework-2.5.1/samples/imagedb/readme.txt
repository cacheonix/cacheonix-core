==============================================
== Spring Image Database sample application ==
==============================================

@author Juergen Hoeller


1. MOTIVATION

This simple sample application illustrates a couple of Spring features that
are not covered by the other samples, namely:

- BLOB/CLOB handling via a LobHandler (addressing Oracle's peculiar behavior)
- retrieving underlying native Connections from pools via NativeJdbcExtractor
- HTTP multipart file uploads via MultipartResolver
- integration of Velocity as view technology for Spring's web MVC


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.

To execute the web application with its default settings, start a MySQL instance on
your local machine. The JDBC settings can be adapted in "WEB-INF/jdbc.properties";
you'll find commented Oracle properties there too. You need to create the "imagedb"
table with the respective schema script in the "db" subdirectory. Note that you need
to make a corresponding JDBC driver available: You can put it in "WEB-INF/lib" in the
web app source before building the WAR, add it to the expanded WAR after deployment,
or drop it into the server's lib directory.

The "standalone" subdirectory includes a sample standalone program that lists all
image descriptors in the database. It accesses the very same "applicationContext.xml"
file in "war/WEB-INF" as the web application: Thus, it needs to be executed in the
web app root directory. The included "standalone.bat" can be used for out-of-the-box
execution in the distribution structure. Note that you need to build the web app
with the build script in this directory before running the standalone tool, and put
a corresponding JDBC driver in "WEB-INF/lib" or your JVM's "jre/lib/ext" directory.

