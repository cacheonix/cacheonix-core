===================================================================
== Spring MVC Convention Over Configuration showcase application ==
===================================================================

@author Rick Evans


1. OVERVIEW

The Spring MVC Convention Over Configuration application showcases the
new Convention Over Configuration support introduced in Spring 2.0.

The web application is *very* simplistic, because the intent is
to convey the essence of the convention over configuration support
and nothing else.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.

Assuming you have deployed the war file to a default Tomcat installation on
your local machine, you can use the following URL to acess the aplication:

http://localhost:8080/coverc/
