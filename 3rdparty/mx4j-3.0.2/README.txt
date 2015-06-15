
Welcome to MX4J, an open source implementation of the
Java(TM) Management Extensions (JMX) (JSR 3) and of the
Java(TM) Management Extensions (JMX) remote API (JSR 160).

For further information about JMX and JSR 3, see
http://java.sun.com/jmx and
http://jcp.org/en/jsr/detail?id=3

For further information about JMX remote and JSR 160, see
http://java.sun.com/jmx and
http://jcp.org/en/jsr/detail?id=160


Refer to the MX4J home page (http://mx4j.sourceforge.net) for
new releases and further information about the MX4J project.

For up-to-date documentation see http://mx4j.sourceforge.net/docs.



The MX4J distribution
~~~~~~~~~~~~~~~~~~~~~

MX4J's binary distribution is bundled in a compressed file, that
contains documentation, examples, libraries.

The structure of binary distribution is as follows, where ${release}
indicates the MX4J release of the distribution:

/mx4j-${release}/
                 docs/      contains the documentation
                 examples/  contains examples on how to use JMX
                 lib/       contains the MX4J implementation,
                            tools and examples jars

MX4J's source distribution is bundled in a compressed file, that
contains the source code and an Ant build file, but no libraries.
Refer to the BUILD-HOWTO file for further information on how to
build MX4J from sources.

The structure of source distribution is as follows, where ${release}
indicates the MX4J release of the distribution:

/mx4j-${release}/
                 build/  contains the Ant build file
                 lib/    put here the libraries needed to build MX4J
                 src/    contains the source files



Installing MX4J
~~~~~~~~~~~~~~~

MX4J does not need to be installed.
It simply provides libraries in form of jars that can be used
to develop JMX applications.



Running MX4J examples
~~~~~~~~~~~~~~~~~~~~~

MX4J examples source files are present in the /examples/ directory, while compiled
example classes are packaged in the /lib/mx4j-examples.jar file of the binary
distribution (see below).
MX4J examples include JSR 160 examples, under the mx4j.examples.remote package.
To run JSR 160 examples with J2SE 1.3, you need to download JAAS from
http://java.sun.com/products/jaas and put jaas.jar in the classpath.
MX4J examples run out of the box when using J2SE 1.4, since JAAS classes are already
included in standard Java Runtime.
Since J2SE 5.0 (JDK 1.5), JMX and JSR 160 classes are included in the standard Java
Runtime, so running the examples will pick up JDK classes, not MX4J's. To pick up
MX4J classes, you should put relevant MX4J jars in the boot classpath with the /p
option (normally this is done using: java -Xbootclasspath/p:<MX4J jars>).



MX4J jars
~~~~~~~~~

Under the /lib/ directory of the binary distribution there are the following jars:
+ mx4j-jmx.jar       -->  contains the JSR 3 javax.management.* classes
+ mx4j-impl.jar      -->  contains the mx4j.* classes that implements
                          JSR 3 functionalities
+ mx4j.jar           -->  The mx4j-jmx.jar and mx4j-impl.jar packed together

+ mx4j-rjmx.jar      -->  contains the JSR 160 javax.management.remote.* classes
+ mx4j-rimpl.jar     -->  contains the mx4j.* classes that implements
                          JSR 160 functionalities
+ mx4j-remote.jar    -->  The mx4j-rjmx.jar and mx4j-rimpl.jar packed together

+ mx4j-tools.jar     -->  contains the MX4J tools
+ mx4j-soap.war      -->  contains a sample web application that deploys a JSR 160
                          connector server over the soap protocol
+ mx4j-examples.jar  -->  contains the MX4J examples



MX4J's Tools
~~~~~~~~~~~~

+ HTTP adaptor, shows the MBeanServer's status in XML.
  By transforming with XSLT the XML, can be used to see the HTML view
  of an MBeanServer in a browser
+ Configuration loader, an XML file format and loader to startup and shutdown
  a JMX application
+ Naming MBeans, wrapping the rmiregistry and tnameserv
+ Statistics MBeans, to gather and show statistics about MBean's attributes
+ Mail MBean, to send email via the JavaMail API
+ Jython MBean, to run Python scripts
+ I18N StandardMBean, to show MBean metadata description in several languages
+ JSR 160 utilities such as:
  * PasswordAuthenticator, to authenticate users over JSR 160 connectors
  * A 'local' JSR 160 connector and connector server
  * A 'soap' JSR 160 connector and connector server
  * A 'hessian' JSR 160 connector and connector server
  * A 'burlap' JSR 160 connector and connector server
  * RemoteMBeanProxy, to proxy MBeans that reside on a remote MBeanServer
  * RMI [Client|Server] SocketFactories over SSL
  * Base classes to develop custom JSR 160 protocol connector and connector servers



MX4J's Deprecated Tools
~~~~~~~~~~~~~~~~~~~~~~~
+ The JRMPAdaptor and the IIOPAdaptor were tools that allowed connection with a
  remote MBeanServer via plain RMI and via CORBA's IIOP protocol.
  Since MX4J 2.x now implements the standard JMX Remote API (JSR 160), these
  tools are not mantained anymore and they're not shipped with the binary
  distribution, to protote use of the standard JMX Remote API.
  They're still present in the source distribution and can be compiled using the
  provided build file if needed.
  Their usage is strongly discouraged, though, and they will not be supported
  any longer.
+ The HeartBeat tool, which was dependent on the above JRMPAdaptor, is also
  deprecated and not mantained nor supported anymore.



MX4J's Documentation
~~~~~~~~~~~~~~~~~~~~

Refer to the included documentation under the /docs/ directory of the binary
distribution for further details on how to use MX4J, and to the online,
nightly updated, documentation for the latest changes.



MX4J Development
~~~~~~~~~~~~~~~~

MX4J includes software that has been developed using
libraries from the following projects:
+ The Apache Software Foundation (http://www.apache.org)
  * The Commons Logging project (http://jakarta.apache.org/commons)
  * The Log4J project (http://jakarta.apache.org/log4j)
  * The Byte Code Engineering Library project (http://jakarta.apache.org/bcel)
  * The Axis project (http://ws.apache.org/axis)
+ The Jetty project (http://jetty.mortbay.org)
+ The Jython project (http://www.jython.org)
+ The Caucho project (http://www.caucho.com)
