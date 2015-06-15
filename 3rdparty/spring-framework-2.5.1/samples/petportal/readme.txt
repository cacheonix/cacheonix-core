=========================================
== Spring PetPortal sample application ==
=========================================

@author John A. Lewis
@author Mark Fisher
@author Juergen Hoeller


1. MOTIVATION

This sample application serves as a showcase for Spring's Portlet MVC framework.
It consists of 5 portlets which demonstrate the various Controller and
HandlerMapping options available:

  - the Welcome portlet: demonstrates Spring's PortletWrappingController which
         enables the integration of pre-existing portlets within Spring in order
         to take advantage of Spring Portlet MVC's capabilities for handler 
         mapping, interceptors, and exception handling.

  - the Portlet Modes portlet: a portlet that simply renders JSP views 
         corresponding to the Portlet Mode. No custom Java code is used in this
         portlet at all -- just JSP and some simple Spring configuration.
         
  - the Pets portlet: the central portlet of the sample application. It 
         demonstrates usage of multiple custom controllers within a single 
         portlet mode including a wizard-based form. There are multiple handler
         mappings and a validator. It also demonstrates delegation to a common 
         service from the various controllers. The shared service is made 
         available to the controllers via dependency-injection. Finally, this
         portlet shows how PortletPreferences can be modified within a 
         Controller's action phase.
         
  - the Description Upload portlet: shows how Spring Portlet MVC enables 
         multipart file uploads. In this case, an uploaded text file will be 
         used as the description for a Pet. This portlet also demonstrates
         custom exception handler mappings as well as localization of
         error messages.
         
  - the Pet Sites portlet: demonstrates redirection out of the portal to 
         display external web pages. The available sites can be added and
         removed while in "edit" mode for this portlet. The initial sites
         are read from a properties file.


2. BUILD AND DEPLOYMENT

This directory contains the web app source.
For deployment, it needs to be built with Apache Ant.
The only requirements are JDK >=1.5 and Ant >=1.6.

Run "build.bat" in this directory for available targets (e.g. "build.bat build",
"build.bat warfile"). You can use "warfile.bat" as a shortcut for WAR file
creation. The WAR file will be created in the "dist" directory.

Unlike a Servlet-based application, in a Portlet-based application, the WAR file
cannot simply be dropped into a directory and used as-is. There will be 
additional deployment requirements, and these will be specific to the targeted 
portlet-container. The deployment process usually consists of the following two 
changes to the webapp:

1. The 'web.xml' file needs to be modified. This is necessary so that your 
   portlet-container webapp can issue cross-context requests into the portlet
   webapp.  This generally involves some kind of well-defined servlet from the
   portlet-container itself.  This may be a wrapper servlet for each portlet
   or a single servlet for access to the entire webapp.

2. A 'portlet.tld' file needs to be provided either via a jar file in the
   classloader (for Servlet 2.4 containers) or an actual file in the WEB-INF
   directory. The contents of this file define the standard JSR-168 JSP tags
   and the classes that implement them. The implementation of the tags is a 
   part of the portlet-container's responsibilities and your webapp needs direct
   access to them, so this definition is specific to the given portlet-
   container.

This is the basic set of changes required to deploy a portlet webapp to work 
with any portlet-container webapp. However, a given portlet-container may 
require additional changes, such as entire jar files that must be placed in 
WEB-INF/lib or additional xml files that must be placed into WEB-INF. They also 
usually require some kind of configuration on the side of the portlet-container 
webapp to "register" your portlets -- these will be entirely unique to the 
portlet-container.

Many portal platforms have an "automatic" deployment process that will
make the necessary modifications to the webapp for you. Make sure you
carefully review the resulting web.xml file after it is deployed. Some
of these deployment tools will mangle listeners or the ViewRendererServlet 
and break the example. It is generally safer to learn how to deploy the webapp 
yourself and do it by hand or with a custom ant script.

This application has been tested in jetspeed-2 where the underlying portlet-
container is Apache Pluto -- the JSR-168 reference implementation. To deploy
the WAR file in jetspeed-2, simply copy it to this directory:
<jetspeed2-install-dir>/webapps/jetspeed/WEB-INF/deploy
The 'petportal.psml' file can then be added to this directory:
<jetspeed2-install-dir>/webapps/jetspeed/WEB-INF/pages

