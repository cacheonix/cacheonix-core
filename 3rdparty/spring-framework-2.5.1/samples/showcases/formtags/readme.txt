==============================================
== Spring MVC form tags showcase application ==
==============================================

@author Rob Harrop


1. OVERVIEW

The Spring MVC form tags application showcases the new form
tag library introduced in Spring 2.0.

The web application is *very* simplistic, because the intent is
to convey the essence of the new form tags themselves and nothing
else.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.3 and Ant >=1.5.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). Note that to start Ant this way, you'll need an XML parser
in your classpath (e.g. in "%JAVA_HOME%/jre/lib/ext"; included in JDK 1.4).
You can use "warfile.bat" as a shortcut for WAR file creation.
The WAR file will be created in the "dist" directory.
