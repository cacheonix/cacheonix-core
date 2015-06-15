This directory tree shows how to use JDK 1.5+ annotations to achieve
auto-proxying. There's no need to use a TransactionFactoryProxyBean:
Methods of application classes that contain transaction annotations will be
automatically intercepted to perform declarative transaction management.

We need our own build file here to compile on JDK 1.5+.

The build script and the applicationContext.xml file are very similar to
those in the root directory. Only trivial modification is required.
However, we've put them in this separate directory to avoid complicating the
main build script and XML bean definition files by providing too many alternatives.

See the "attributes" directory for a similar style based on Commons Attributes.
