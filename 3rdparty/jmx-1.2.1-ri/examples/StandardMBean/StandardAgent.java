/*
 * @(#)file      StandardAgent.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.20
 * @(#)lastedit      03/07/15
 *
 * Copyright 2000-2003 Sun Microsystems, Inc.  All rights reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * 
 * Copyright 2000-2003 Sun Microsystems, Inc.  Tous droits réservés.
 * Ce logiciel est proprieté de Sun Microsystems, Inc.
 * Distribué par des licences qui en restreignent l'utilisation. 
 */


// java imports
//
import java.util.*;
import java.io.*;
import java.net.*;

// RI imports
//
import com.sun.jmx.trace.Trace;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;

import javax.management.MalformedObjectNameException;


public class StandardAgent {

    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    
    public StandardAgent() {
    
	// CREATE the MBeanServer
	//
	echo("\n\tCREATE the MBeanServer.");
	server = MBeanServerFactory.createMBeanServer();
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */
  
    public static void main(String[] args) {
        
	// START
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	echo("\n>>> CREATE the agent...");
	StandardAgent agent = new StandardAgent();
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();
        
	// DO THE DEMO
	//
	agent.doSimpleDemo();

	// END
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	echo("\n>>> END of the SimpleStandard example:\n");
	echo("\n\tpress <Enter> to exit...\n");
	waitForEnterPressed();
	System.exit(0);
    }


    /*
     * ------------------------------------------
     *  PRIVATE METHODS
     * ------------------------------------------
     */

    private void doSimpleDemo() {

	// build the simple MBean ObjectName
	//
	ObjectName mbeanObjectName = null;
	String domain = server.getDefaultDomain();
	String mbeanName = "SimpleStandard";
	try {
	    mbeanObjectName = new ObjectName(domain + ":type=" + mbeanName);
	} catch(MalformedObjectNameException e) {
	    echo("\t!!! Could not create the MBean ObjectName !!!");
	    e.printStackTrace();
	    echo("\nEXITING...\n");
	    System.exit(1);
	}
	// create and register the MBean
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	createSimpleBean(mbeanObjectName, mbeanName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// get and display the management information exposed by the MBean
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printMBeanInfo(mbeanObjectName, mbeanName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// manage the MBean
	// 
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	manageSimpleBean(mbeanObjectName ,mbeanName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// trying to do illegal management actions...
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	goTooFar(mbeanObjectName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();
    }

    private void createSimpleBean(ObjectName mbeanObjectName, String mbeanName) {

	echo("\n>>> CREATE the " + mbeanName + " MBean within the MBeanServer:");
	String mbeanClassName = mbeanName;
	echo("\tOBJECT NAME           = " + mbeanObjectName);
	try {
	    server.createMBean(mbeanClassName,mbeanObjectName);
	} catch(Exception e) {
	    echo("\t!!! Could not create the " + mbeanName + " MBean !!!");
	    e.printStackTrace();
	    echo("\nEXITING...\n");
	    System.exit(1);
	}
    }

    private void manageSimpleBean(ObjectName mbeanObjectName, String mbeanName) {

	echo("\n>>> MANAGING the " + mbeanName + " MBean");
	echo("    using its attributes and operations exposed for management");

	try {
	    // Get attribute values
	    sleep(1000);
	    printSimpleAttributes(mbeanObjectName);

	    // Change State attribute
	    sleep(1000);
	    echo("\n    Setting State attribute to value \"new state\"...");
	    Attribute stateAttribute = new Attribute("State","new state");
	    server.setAttribute(mbeanObjectName, stateAttribute);

	    // Get attribute values
	    sleep(1000);
	    printSimpleAttributes(mbeanObjectName);

	    // Invoking reset operation
	    sleep(1000);
	    echo("\n    Invoking reset operation...");
	    server.invoke(mbeanObjectName,"reset",null,null);

	    // Get attribute values
	    sleep(1000);
	    printSimpleAttributes(mbeanObjectName);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void goTooFar(ObjectName mbeanObjectName) {

	echo("\n>>> Trying to set the NbChanges attribute (read-only)!");
	echo("\n... We should get an AttributeNotFoundException:\n");
	sleep(1000);
	// Try to set the NbChanges attribute
	Attribute nbChangesAttribute = new Attribute("NbChanges", new Integer(1));
	try {
	    server.setAttribute(mbeanObjectName, nbChangesAttribute);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	echo("\n\n>>> Trying to access the NbResets property (not exposed for management)!");
	echo("\n... We should get an AttributeNotFoundException:\n");
	sleep(1000);
	// Try to access the NbResets property
	try {
	    Integer NbResets = (Integer) server.getAttribute(mbeanObjectName,"NbResets");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void printMBeanInfo(ObjectName mbeanObjectName, String mbeanName) {

	echo("\n>>> Getting the management information for the "+ mbeanName +" MBean");
	echo("    using the getMBeanInfo method of the MBeanServer");
	sleep(1000);
	MBeanInfo info = null;
	try {
	    info = server.getMBeanInfo(mbeanObjectName);
	} catch (Exception e) {
	    echo("\t!!! Could not get MBeanInfo object for "+ mbeanName +" !!!");
	    e.printStackTrace();
	    return;
	}
	echo("\nCLASSNAME: \t"+ info.getClassName());
	echo("\nDESCRIPTION: \t"+ info.getDescription());
	echo("\nATTRIBUTES");
	MBeanAttributeInfo[] attrInfo = info.getAttributes();
	if (attrInfo.length>0) {
	    for(int i=0; i<attrInfo.length; i++) {
		echo(" ** NAME: \t"+ attrInfo[i].getName());
		echo("    DESCR: \t"+ attrInfo[i].getDescription());
		echo("    TYPE: \t"+ attrInfo[i].getType() +
		     "\tREAD: "+ attrInfo[i].isReadable() +
		     "\tWRITE: "+ attrInfo[i].isWritable());
	    }
	} else echo(" ** No attributes **");
	echo("\nCONSTRUCTORS");
	MBeanConstructorInfo[] constrInfo = info.getConstructors();
	for(int i=0; i<constrInfo.length; i++) {
	    echo(" ** NAME: \t"+ constrInfo[i].getName());
	    echo("    DESCR: \t"+ constrInfo[i].getDescription());
	    echo("    PARAM: \t"+ constrInfo[i].getSignature().length +" parameter(s)");
	}
	echo("\nOPERATIONS");
	MBeanOperationInfo[] opInfo = info.getOperations();
	if (opInfo.length>0) {
	    for(int i=0; i<opInfo.length; i++) {
		echo(" ** NAME: \t"+ opInfo[i].getName());
		echo("    DESCR: \t"+ opInfo[i].getDescription());
		echo("    PARAM: \t"+ opInfo[i].getSignature().length +" parameter(s)");
	    }
	} else echo(" ** No operations ** ");
	echo("\nNOTIFICATIONS");
	MBeanNotificationInfo[] notifInfo = info.getNotifications();
	if (notifInfo.length>0) {
	    for(int i=0; i<notifInfo.length; i++) {
		echo(" ** NAME: \t"+ notifInfo[i].getName());
		echo("    DESCR: \t"+ notifInfo[i].getDescription());
	    }
	} else echo(" ** No notifications **");
    }

    private void printSimpleAttributes(ObjectName mbeanObjectName) {

	try {
	    echo("\n    Getting attribute values:");
	    String State = (String) server.getAttribute(mbeanObjectName,"State");
	    Integer NbChanges = (Integer) server.getAttribute(mbeanObjectName,"NbChanges");
	    echo("\tState     = \"" + State + "\"");
	    echo("\tNbChanges = " + NbChanges);
	} catch (Exception e) {
	    echo("\t!!! Could not read attributes !!!");
	    e.printStackTrace();
	}
    }
    
    private static void echo(String msg) {
	System.out.println(msg);
    }

    private static void sleep(int millis) {
	try {
	    Thread.sleep(millis);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private static void waitForEnterPressed() {
	try {
	    System.in.read();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    private MBeanServer server = null;
    
}
