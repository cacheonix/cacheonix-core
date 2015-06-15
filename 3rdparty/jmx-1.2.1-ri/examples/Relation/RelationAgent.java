/*
 * @(#)file      RelationAgent.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
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
import javax.management.MBeanException;

import javax.management.relation.*;


public class RelationAgent {

    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    
    public RelationAgent() {
    
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
	echo("\n>>> START of Relation Service example");
	echo("\n>>> CREATE the agent...");
	RelationAgent agent = new RelationAgent();
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();
        
	// DO THE DEMO
	//
	agent.doSimpleDemo();

	// END
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	echo("\n>>> END of the Relation Service example:\n");
	String localHost = null;
	try {
	    localHost = java.net.InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	    localHost = "localhost" ;
	}
	echo("\n\tpress <Enter> to stop the agent...\n");
	waitForEnterPressed();
	System.exit(0);
    }


    /*
     * ------------------------------------------
     *  PRIVATE METHODS
     * ------------------------------------------
     */

    private void doSimpleDemo() {

	// build ObjectName of RelationService
	String relServClassName = "javax.management.relation.RelationService";
	ObjectName relServObjName = createMBeanName(relServClassName, 1);

	// create and register the Relation Service
	// Flag set for immediate purge
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	createRelationService(relServObjName, relServClassName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// get and display the management information exposed by the relation
	// service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printMBeanInfo(relServObjName, relServClassName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create two role infos
	// - container:
	//   - element class: SimpleStandard
	//   - read: y, write: y
	//   - multiplicity: 1,1
	// - contained:
	//   - element class: SimpleStandard
	//   - read: y, write: y
	//   - multiplicity: 0,n
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	RoleInfo[] roleInfoArray = new RoleInfo[2];
	String role1Name = "container";
	roleInfoArray[0] =
	    createRoleInfo(role1Name,
			   "SimpleStandard",
			   true,
			   true,
			   1,
			   1,
			   null);
	String role2Name = "contained";
	roleInfoArray[1] =
	    createRoleInfo(role2Name,
			   "SimpleStandard",
			   true,
			   true,
			   0,
			   -1,
			   null);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create invalid relation type: no name
	// echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	// createRelationType(null, roleInfoArray, relServObjName);
	// echo("\npress <Enter> to continue...\n");
	// waitForEnterPressed();

	// create a relation type with those role infos
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	String relTypeName = "myRelationType";
	createRelationType(relTypeName, roleInfoArray, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about the known relation types
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printRelationTypeInfo(relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// build six simple MBean ObjectNames
	//
	String mbeanClassName = "SimpleStandard";
	ObjectName mbeanObjectName1 = createMBeanName(mbeanClassName, 1);
	ObjectName mbeanObjectName2 = createMBeanName(mbeanClassName, 2);
	ObjectName mbeanObjectName3 = createMBeanName(mbeanClassName, 3);
	ObjectName mbeanObjectName4 = createMBeanName(mbeanClassName, 4);
	ObjectName mbeanObjectName5 = createMBeanName(mbeanClassName, 5);
	ObjectName mbeanObjectName6 = createMBeanName(mbeanClassName, 6);

	// create and register six simple MBeans with given ObjectNames
	//
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	createSimpleBean(mbeanObjectName1, mbeanClassName);
	createSimpleBean(mbeanObjectName2, mbeanClassName);
	createSimpleBean(mbeanObjectName3, mbeanClassName);
	createSimpleBean(mbeanObjectName4, mbeanClassName);
	createSimpleBean(mbeanObjectName5, mbeanClassName);
	createSimpleBean(mbeanObjectName6, mbeanClassName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();


	// create roles
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	ArrayList role1Value = new ArrayList();
	role1Value.add(mbeanObjectName1);
	//
	// Invalid case: empty role value, where role info expects at least one
	// reference.
	// Will throw a MBeanException->InvalidRoleValueException
	// role1Value = new ArrayList();
	//
	Role role1 = createRole(role1Name, role1Value);
	ArrayList role2Value = new ArrayList();
	role2Value.add(mbeanObjectName2);
	//
	// Another invalid case: reference a MBean not registered
	// Will throw a MBeanException->InvalidRoleValueException
	// ObjectName mbeanObjectName7 = createMBeanName(mbeanClassName, 7);
	// role2Value.add(mbeanObjectName7);
	//
	Role role2 = createRole(role2Name, role2Value);
	RoleList roleList1 = new RoleList();
	roleList1.add(role1);
	roleList1.add(role2);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create an internal relation
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	String relId1 = relTypeName + "_internal_1";
	//
	// Invalid relation, as container role is expected to have at least one
	// reference
	// Will throw a MBeanException wrapping an InvalidRoleValueException
	// roleList1 = null;
	//
	createRelation(relId1, relTypeName, roleList1, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);

	// do queries
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	//
	// Invalid case: query for a MBean not referenced anywhere (here it
	// does not even exist)
	// will return an empty HashMap
	// ObjectName mbeanObjectName7 = createMBeanName(mbeanClassName, 7);
	// doQueries(mbeanObjectName7,
	//	  relTypeName,
	//	  "container",
	//	  relServObjName);
	//
	doQueries(mbeanObjectName2,
		  relTypeName,
		  "contained",
		  relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// add a reference in a role
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	//
	// Invalid case: add a reference to exceed maximum degree
	// Will throw a MBeanException wrapping an InvalidRoleValueException
	// singleRoleIndirectAdd(relId1,
	//			 "container",
	//			 mbeanObjectName3,
	//			 relServObjName);
	//
	// Invalid case: try to update a role whch does not exist
	// Will throw a RoleNotFoundException
	// singleRoleIndirectAdd(relId1,
	//		      "foo",
	//		      mbeanObjectName3,
	//		      relServObjName);
	//
	singleRoleIndirectAdd(relId1,
			      "contained",
			      mbeanObjectName3,
			      relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// update several roles
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	ArrayList role1List = new ArrayList();
	role1List.add(mbeanObjectName4);
	//
	// Invalid case: role exceeds maximum cardinality
	// A RoleUnresolved element is returned in the RoleResult
	// role1List.add(mbeanObjectName5);
	//
	Role role1_1 = new Role(role1Name, role1List);
	ArrayList role2List = new ArrayList();
	role2List.add(mbeanObjectName5);
	role2List.add(mbeanObjectName6);
	Role role2_1 = new Role(role2Name, role2List);
	RoleList roleList2 = new RoleList();
	roleList2.add(role1_1);
	roleList2.add(role2_1);
	//
	// Invalid case: try to update a role which does not exist
	// the returned RoleResult will include a RoleUnresolved part
	// Role dummyRole = new Role("dummy", role2List);
	// roleList2.add(dummyRole);
	//
	multiRoleIndirectUpdate(relId1, roleList2, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);

	// create a relation MBean and add it in the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	String relMBeanClassName = "SimpleRelation";
	String relId2 = relTypeName + "_relMBean_2";
	//
	// Invalid case: add a relation with an id already used
	// relId2 = relId1;
	//
	// Another invalid case: null roleList
	// roleList1 = null;
	//
	// Another invalid case: add a role not described in relation type
	// ArrayList role3Value = new ArrayList();
	// Role role3 = createRole("dummy", role3Value);
	// roleList1.add(role3);
	//
	ObjectName relMBeanObjName1 = createMBeanName(relMBeanClassName, 2);
	createRelationMBean(relMBeanObjName1,
			    relMBeanClassName,
			    relId2,
			    relTypeName,
			    roleList1,
			    relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// Invalid case: add as a reference a MBean not of the correct class.
	// Will throw a MBeanException->InvalidRoleValueException
	// singleRoleIndirectAdd(relId1,
	//			 "contained",
	//			 relMBeanObjName1,
	//			 relServObjName);
	//

	// use relation operations on relation MBean, and specific accessors
	// as isRelationMBean
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	testRelationMBeanOperations(relMBeanObjName1);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();
	testRelationMBeanSpecificOperations(relId2,
					    relServObjName,
					    relMBeanObjName1);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// add a reference in a role via the relation service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	//
	// Invalid case: add a reference to exceed maximum degree
	// Will throw a MBeanException wrapping an InvalidRoleValueException
	// singleRoleIndirectUpdate(relId1,
	//			 "container",
	//			 mbeanObjectName3,
	//			 relServObjName);
	//
	singleRoleIndirectAdd(relId2,
			      "contained",
			      mbeanObjectName3,
			      relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// update several roles via the relation service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	multiRoleIndirectUpdate(relId2, roleList2, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);

	// add a reference in a role of relation MBean using direct call
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	singleRoleDirectAdd(relMBeanObjName1,
			    "contained",
			    mbeanObjectName3);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// update several roles in relation MBean via direct call
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	multiRoleDirectUpdate(relMBeanObjName1, roleList1);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);


	// do queries
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	doQueries(mbeanObjectName2,
		  relTypeName,
		  "contained",
		  relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// unregistration of a referenced MBean in "contained"
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	unregisterMBean(mbeanObjectName2);
	echo("The unregistration of this MBean which was referenced in a relation does not invalidate the relation.");
	echo("This because the minimum cardinality of the role is 0.");
	echo("So the role value has just to be updated");
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// do queries
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	doQueries(mbeanObjectName1,
		  relTypeName,
		  "container",
		  relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();	

	// remove the internal relation
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	removeRelation(relId1, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);

	// get all relations of given type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationIdsOfType(relTypeName, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// remove the relation MBean from the relation service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	removeRelation(relId2, relServObjName);
	echo("This relation is a relation MBean. Removing it as a relation just removes it from the Relation Service.");
	echo("The MBean is not unregistered.");
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// get all relations of given type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationIdsOfType(relTypeName, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// add again the relation MBean to the relation service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	addRelation(relMBeanObjName1, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// get all relations of given type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationIdsOfType(relTypeName, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// unregister relation MBean
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	unregisterMBean(relMBeanObjName1);
	echo("Unregistering a MBean being a relation will also remove it from the Relation Service, as it cannot be accessed any longer.");
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// get all relations of given type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationIdsOfType(relTypeName, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create a new internal relation
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	role1Value = new ArrayList();
	role1Value.add(mbeanObjectName1);
	role1 = createRole(role1Name, role1Value);
	role2Value = new ArrayList();
	// do not use mbeanObjectName2, as it has been unregistered
	role2Value.add(mbeanObjectName3);
	role2 = createRole(role2Name, role2Value);
	roleList1 = new RoleList();
	roleList1.add(role1);
	roleList1.add(role2);
	String relId3 = relTypeName + "_internal_3";
	createRelation(relId3, relTypeName, roleList1, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// unregistration of a referenced MBean in "container"
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	unregisterMBean(mbeanObjectName1);
	echo("Unregistering this MBean will invalidate the relation as it was referenced in a role with a cardinality of 1,1.");
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// re-create internal relation
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	role1Value = new ArrayList();
	// do not use mbeanObjectName1, unregistered
	role1Value.add(mbeanObjectName3);
	role1 = createRole(role1Name, role1Value);
	roleList1 = new RoleList();
	roleList1.add(role1);
	// role "contained" to be defaulted
	String relId4 = relTypeName + "_internal_4";
	createRelation(relId4, relTypeName, roleList1, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);

	// remove a relation type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	removeRelationType(relTypeName, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about the known relation types
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printRelationTypeInfo(relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create a user relation type
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	String usrRelTypeName = "SimpleRelationType";
	SimpleRelationType usrRelType =
	    new SimpleRelationType("SimpleRelationType");
	addRelationType(usrRelType, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about the known relation types
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printRelationTypeInfo(relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create roles
	// mbeanObjectName1 and mbeanObjectName2 unregistered
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	ArrayList primaryValue = new ArrayList();
        primaryValue.add(mbeanObjectName3);
	primaryValue.add(mbeanObjectName4);
	Role primaryRole = createRole("primary", primaryValue);
	ArrayList secondaryValue = new ArrayList();
	secondaryValue.add(mbeanObjectName5);
	secondaryValue.add(mbeanObjectName6);
	Role secondaryRole = createRole("secondary", secondaryValue);
	RoleList otherRoleList = new RoleList();
	otherRoleList.add(primaryRole);
	otherRoleList.add(secondaryRole);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// create an internal relation
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	String relId5 =  usrRelTypeName + "_internal_1";
	createRelation(relId5, usrRelTypeName, otherRoleList, relServObjName);
	echo("\npress <Enter> to continue...\n");
	waitForEnterPressed();

	// print info about all relations known by the Relation Service
	echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	printAllRelationInfo(relServObjName);
    }

    private void createSimpleBean(ObjectName mbeanObjectName, String mbeanName) {

	echo("\n>>> CREATE THE MBEAN " + mbeanName + " within the MBeanServer:");
	String mbeanClassName = mbeanName;
	echo("\tOBJECT NAME           = " + mbeanObjectName);
	try {
	    server.createMBean(mbeanClassName,mbeanObjectName);
	}
	catch(Exception e) {
	    echo("\t!!! Could not create the " + mbeanName + " MBean !!!");
	    printException(e);
	}
    }

    private void printMBeanInfo(ObjectName mbeanObjectName, String mbeanName) {

	echo("\n>>> GETTING THE MANAGEMENT INFORMATION for the "+ mbeanName +" MBean");
	echo("\tusing the getMBeanInfo method of the MBeanServer");
	sleep(1000);
	MBeanInfo info = null;
	try {
	    info = server.getMBeanInfo(mbeanObjectName);
	} catch (Exception e) {
	    echo("\t!!! Could not get MBeanInfo object for "+ mbeanName +" !!!");
	    printException(e);
	}
	echo("\nCLASSNAME: \t"+ info.getClassName());
	echo("\nDESCRIPTION: \t"+ info.getDescription());
	echo("\nATTRIBUTES");
	MBeanAttributeInfo[] attrInfo = info.getAttributes();
	if (attrInfo.length>0) {
	    for(int i=0; i< attrInfo.length; i++) {
		echo(" ** NAME: \t"+ attrInfo[i].getName());
		echo("    DESCR: \t"+ attrInfo[i].getDescription());
		echo("    TYPE: \t"+ attrInfo[i].getType() +
		     "\tREAD: "+ attrInfo[i].isReadable() +
		     "\tWRITE: "+ attrInfo[i].isWritable());
	    }
	} else echo(" ** No attributes **");
	echo("\nCONSTRUCTORS");
	MBeanConstructorInfo[] constrInfo = info.getConstructors();
	for(int i=0; i< constrInfo.length; i++) {
	    echo(" ** NAME: \t"+ constrInfo[i].getName());
	    echo("    DESCR: \t"+ constrInfo[i].getDescription());
	    echo("    PARAM: \t"+ constrInfo[i].getSignature().length +" parameter(s)");
	}
	echo("\nOPERATIONS");
	MBeanOperationInfo[] opInfo = info.getOperations();
	if (opInfo.length>0) {
	    for(int i=0; i< opInfo.length; i++) {
		echo(" ** NAME: \t"+ opInfo[i].getName());
		echo("    DESCR: \t"+ opInfo[i].getDescription());
		echo("    PARAM: \t"+ opInfo[i].getSignature().length +" parameter(s)");
	    }
	} else echo(" ** No operations ** ");
	echo("\nNOTIFICATIONS");
	MBeanNotificationInfo[] notifInfo = info.getNotifications();
	if (notifInfo.length>0) {
	    for(int i=0; i< notifInfo.length; i++) {
		echo(" ** NAME: \t"+ notifInfo[i].getName());
		echo("    DESCR: \t"+ notifInfo[i].getDescription());
	    }
	} else echo(" ** No notifications **");
    }

    private static void echo(String msg) {
	System.out.println(msg);
    }

    private static void sleep(int millis) {
	try {
	    Thread.sleep(millis);
	} catch (InterruptedException e) {
	    return;
	}
    }

    private static void waitForEnterPressed() {
	try {
	    System.in.read();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	return;
    }

    // create ObjectName
    private ObjectName createMBeanName(String theMBeanClassName,
				       int theMBeanNbr) {
	ObjectName mbeanObjectName = null;
	try {
	    String domain = server.getDefaultDomain();
	    StringBuffer mbeanName = new StringBuffer(theMBeanClassName);
	    mbeanName.append(theMBeanNbr);
	    mbeanObjectName = new ObjectName(domain + ":type=" + mbeanName);
	} catch(Exception e) {
	    echo("\t!!! Could not create the MBean ObjectName !!!");
	    printException(e);
	}
	return mbeanObjectName;
    }

    // create and register the relation service
    private void createRelationService(ObjectName theRelServObjName,
				       String theRelServClassName) {

	echo("\n>>> CREATE THE RELATION SERVICE " + theRelServClassName + " within the MBeanServer:");
	echo("\tOBJECT NAME           = " + theRelServObjName);
	try {
	    Object[] params = new Object[1];
	    params[0] = new Boolean(true);
	    String[] signature = new String[1];
	    signature[0] = "boolean";
	    server.createMBean(theRelServClassName,
			       theRelServObjName,
			       params,
			       signature);
	} catch(Exception e) {
	    echo("\t!!! Could not create the " + theRelServClassName + " MBean !!!");
	    printException(e);
	}
    }

    // create a role info
    private RoleInfo createRoleInfo(String theRoleName,
				    String theRefMBeanClassName,
				    boolean theIsReadable,
				    boolean theIsWritable,
				    int theMinDegree,
				    int theMaxDegree,
				    String theDesc) {
	echo("\n>>> CREATE THE ROLE INFO " + theRoleName);
	RoleInfo roleInfo = null;
	try {
	    roleInfo =  new RoleInfo(theRoleName,
				     theRefMBeanClassName,
				     theIsReadable,
				     theIsWritable,
				     theMinDegree,
				     theMaxDegree,
				     theDesc);
	    echo("\n\t" + roleInfo.toString());

	} catch (Exception e) {
	    echo("\t!!! Could not create the role info " + theRoleName+ " !!!");
	    printException(e);
	}
	return roleInfo;
    }

    // create a relation type
    private void createRelationType(String theRelTypeName,
				    RoleInfo[] theRoleInfoArray,
				    ObjectName theRelServObjName) {
	echo("\n>>> CREATE THE RELATION TYPE " + theRelTypeName);
	try {
	    Object[] params = new Object[2];
	    params[0] = theRelTypeName;
	    params[1] = theRoleInfoArray;
	    String[] signature = new String[2];
	    signature[0] = "java.lang.String";
	    try {
		signature[1] =
		    (theRoleInfoArray.getClass()).getName();
	    } catch (Exception exc) {
		throw exc;
	    }
	    server.invoke(theRelServObjName,
			  "createRelationType",
			  params,
			  signature);
	} catch (Exception e) {
	    echo("\t!!! Could not create the relation type " + theRelTypeName + " !!!");
	    printException(e);
	}
    }

    // print info about the relation types
    private void printRelationTypeInfo(ObjectName relServObjName) {
	echo("\n>>> PRINT RELATION TYPE INFO about the relation types in Relation Service " + relServObjName);
	try {
	    echo("\n-> Retrieve all relation types");
	    ArrayList relTypeNameList = (ArrayList)
		(server.getAttribute(relServObjName,
                                     "AllRelationTypeNames"));

	    for (Iterator relTypeNameIter = relTypeNameList.iterator();
		 relTypeNameIter.hasNext();) {
		String currRelTypeName = (String)(relTypeNameIter.next());
		echo("\n-> Print role info for relation type " + currRelTypeName);
		Object[] params2 = new Object[1];
		params2[0] = currRelTypeName;
		String[] signature2 = new String[1];
		signature2[0] = "java.lang.String";
		ArrayList roleInfoList = (ArrayList)
		    (server.invoke(relServObjName,
				   "getRoleInfos",
				   params2,
				   signature2));
		printList(roleInfoList);
	    }
	} catch (Exception e) {
	    echo("\t!!! Could not browse the relation types!!!");
	    printException(e);
	}
    }

    // create a role
    private Role createRole(String theRoleName, ArrayList theRoleValue) {
	echo("\n>>> CREATING ROLE " + theRoleName);
	Role role = null;
	try {
	    role = new Role(theRoleName, theRoleValue);
	    echo("\t" + role.toString());
	} catch (Exception e) {
	    echo("\t!!! Could create role " + theRoleName + "!!!");
	    printException(e);
	}
	return role;
    }

    // create an internal relation
    private void createRelation(String theRelId,
				String theRelTypeName,
				RoleList theRoleList,
				ObjectName theRelServObjName) {	
	echo("\n>>> CREATE INTERNAL RELATION " + theRelId + " of type " + theRelTypeName);
	try {
	    Object[] params = new Object[3];
	    params[0] = theRelId;
	    params[1] = theRelTypeName;
	    params[2] = theRoleList;
	    String[] signature = new String[3];
	    signature[0] = "java.lang.String";
	    signature[1] = "java.lang.String";
	    signature[2] = "javax.management.relation.RoleList";
	    server.invoke(theRelServObjName,
			  "createRelation",
			  params,
			  signature);
	} catch(Exception e) {
	    echo("\t!!! Could not create the relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // print info about all relations handled by given Relation Service
    private void printAllRelationInfo(ObjectName theRelServObjName) {
	echo("\n>>> PRINT ALL RELATIONS in relation service " + theRelServObjName);
	try {
	    ArrayList relIdList = (ArrayList)
              (server.getAttribute(theRelServObjName,
                                   "AllRelationIds"));

	    for (Iterator relIdIter = relIdList.iterator();
		 relIdIter.hasNext();) {
		String currRelId = (String)(relIdIter.next());
		printRelationInfo(currRelId, theRelServObjName);

		echo("\npress <Enter> to continue...\n");
		waitForEnterPressed();
	    }
	} catch(Exception e) {
	    echo("\t!!! Could not browse relations of relation service " + theRelServObjName + "!!!");
	    printException(e);
	}
    }

    // print info about given relation
    private void printRelationInfo(String theRelId,
				   ObjectName theRelServObjName) {
	echo("\n>>> PRINT RELATION " + theRelId + " accessed via relation service " + theRelServObjName);

	testRelationIndirectAccess(false, theRelId, theRelServObjName, null);
    }

    private void testRelationIndirectAccess(boolean theRelMBeanFlg,
					    String theRelId,
					    ObjectName theRelServObjName,
					    ObjectName theRelMBeanName) {
	try {
	    echo("\n-> Retrieve all roles");
	    // retrieves all roles
	    ObjectName theName1 = null;
	    Object[] params1 = new Object[] { theRelId };
            String[] signature1 = new String[] { "java.lang.String" };
            RoleResult roleResult = null;
            if (!theRelMBeanFlg) {
		theName1 = theRelServObjName;
                roleResult = (RoleResult)
		  (server.invoke(theName1,
                                 "getAllRoles",
                                 params1,
                                 signature1));
	    } else {
		theName1 = theRelMBeanName;
                roleResult = (RoleResult)
		  (server.getAttribute(theName1,
                                       "AllRoles"));
	    }	
	    printRoleResult(roleResult);

	    RoleList roleList1 = roleResult.getRoles();
	    String[] roleNameArray = new String[(roleList1.size())];
	    int i = 0;
	    for (Iterator roleIter = roleList1.iterator();
		 roleIter.hasNext();) {
		Role currRole = (Role)(roleIter.next());
		String currRoleName = currRole.getRoleName();

		roleNameArray[i] = currRoleName;
		i++;

		// Check that the role can be accessed directly
		echo("\n-> Checking access via getRole() on role " + currRoleName);
		ObjectName theName2 = null;
		Object[] params2 = null;
		String[] signature2 = null;
		if (!theRelMBeanFlg) {
		    theName2 = theRelServObjName;
		    params2 = new Object[2];
		    params2[0] = theRelId;
		    params2[1] = currRoleName;
		    signature2 = new String[2];
		    signature2[0] = "java.lang.String";
		    signature2[1] = "java.lang.String";
		} else {
		    theName2 = theRelMBeanName;
		    params2 = new Object[1];
		    params2[0] = currRoleName;
		    signature2 = new String[1];
		    signature2[0] = "java.lang.String";
		}
		ArrayList currRoleValue = (ArrayList)
		    (server.invoke(theName2,
				   "getRole",
				   params2,
				   signature2));
		echo("\n\tvalue retrieved via getRole on role " + currRoleName);
		printList(currRoleValue);

		// Print number of MBeans referenced in role
		echo("\n-> Print number of MBeans referenced in " + currRoleName);
		Integer currRoleRefNbr = (Integer)
		    (server.invoke(theName2,
				   "getRoleCardinality",
				   params2,
				   signature2));
		echo("\n\t" + currRoleRefNbr.intValue() + " referenced MBeans");
	    }

	    // Check getRoles()
	    echo("\n-> Access via getRoles()");
	    ObjectName theName3 = null;
	    Object[] params3 = null;
	    String[] signature3 = null;
	    if (!theRelMBeanFlg) {
		theName3 = theRelServObjName;
		params3 = new Object[2];
		params3[0] = theRelId;
		params3[1] = roleNameArray;
		signature3 = new String[2];
		signature3[0] = "java.lang.String";
		signature3[1] = (roleNameArray.getClass()).getName();
	    } else {
		theName3 = theRelMBeanName;
		params3 = new Object[1];
		params3[0] = roleNameArray;
		signature3 = new String[1];
		signature3[0] = (roleNameArray.getClass()).getName();
	    }
	    RoleResult roleRes = (RoleResult)
		(server.invoke(theName3,
			       "getRoles",
			       params3,
			       signature3));
	    printRoleResult(roleRes);

	    // referenced MBeans
	    echo("\n-> Referenced MBeans");
            HashMap refMBeanMap = null;
            if (!theRelMBeanFlg) {
              refMBeanMap = (HashMap)
		(server.invoke(theName1,
			       "getReferencedMBeans",
			       params1,
			       signature1));
	    } else {
              refMBeanMap = (HashMap)
		(server.getAttribute(theName1,
			       "ReferencedMBeans"));
	    }	
	    printMap(refMBeanMap);

	    // relation type name
	    echo("\n-> Relation type");
	    String relTypeName = null;
            if (!theRelMBeanFlg) {
              relTypeName = (String)
		(server.invoke(theName1,
			       "getRelationTypeName",
			       params1,
			       signature1));
	    } else {
              relTypeName = (String)
		(server.getAttribute(theName1,
                                     "RelationTypeName"));
	    }
	    echo("\n\tRELATION TYPE is " + relTypeName);
	} catch(Exception e) {
	    echo("\t!!! Could not print relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // print info on a role
    private void printRole(Role theRole) {
	try {
	    echo("\n\t" + theRole.toString());
	} catch(Exception e) {
	    echo("\t!!! Could not print role " + theRole + "!!!");
	    printException(e);
	}
    }

    // print a RoleResult
    private void printRoleResult(RoleResult theRoleResult) {
	echo("\tPrinting RoleResult");
	try {
	    RoleList roleList = theRoleResult.getRoles();
	    echo("\t-> Printing the RoleList");
	    printList(roleList);
	    RoleUnresolvedList roleUnresList =
		theRoleResult.getRolesUnresolved();
	    echo("\t-> Printing the RoleUnresolvedList");
	    printList(roleUnresList);
	} catch(Exception e) {
	    echo("\t!!! Could not print RoleResult!!!");
	    printException(e);
	}
    }

    // execute queries
    private void doQueries(ObjectName theMBeanObjName,
			   String theRelTypeName,
			   String theRoleName,
			   ObjectName theRelServObjName) {
	echo("\n>>> PERFORMING QUERIES to retrieve where the MBean " + theMBeanObjName + " is referenced and to which MBeans it is associated.");
	try {
	    echo("\n-> Referencing relations");
	    echo("\n\tFIRST, only looking with the MBean");
	    Object[] params1 = new Object[3];
	    params1[0] = theMBeanObjName;
	    params1[1] = null;
	    params1[2] = null;
	    String[] signature1 = new String[3];
	    signature1[0] = "javax.management.ObjectName";
	    signature1[1] = "java.lang.String";
	    signature1[2] = "java.lang.String";
	    HashMap refRelMap1 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findReferencingRelations",
			       params1,
			       signature1));
	    printMap(refRelMap1);

	    echo("\n\tSECOND, MBean + relation type");
	    Object[] params2 = new Object[3];
	    params2[0] = theMBeanObjName;
	    params2[1] = theRelTypeName;
	    params2[2] = null;
	    HashMap refRelMap2 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findReferencingRelations",
			       params2,
			       signature1));
	    printMap(refRelMap2);

	    echo("\n\tTHIRD, MBean + role name");
	    Object[] params3 = new Object[3];
	    params3[0] = theMBeanObjName;
	    params3[1] = null;
	    params3[2] = theRoleName;
	    HashMap refRelMap3 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findReferencingRelations",
			       params3,
			       signature1));
	    printMap(refRelMap3);

	    echo("\n\tTHEN, MBean + relation type + role name");
	    Object[] params4 = new Object[3];
	    params4[0] = theMBeanObjName;
	    params4[1] = theRelTypeName;
	    params4[2] = theRoleName;
	    HashMap refRelMap4 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findReferencingRelations",
			       params4,
			       signature1));
	    printMap(refRelMap4);

	    echo("\n");

	    echo("\n-> Associated MBeans");
	    echo("\n\tFIRST, only looking with the MBean");
	    Object[] params5 = new Object[3];
	    params5[0] = theMBeanObjName;
	    params5[1] = null;
	    params5[2] = null;
	    HashMap refRelMap5 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findAssociatedMBeans",
			       params5,
			       signature1));
	    printMap(refRelMap5);

	    echo("\n\tSECOND, MBean + relation type");
	    Object[] params6 = new Object[3];
	    params6[0] = theMBeanObjName;
	    params6[1] = theRelTypeName;
	    params6[2] = null;
	    HashMap refRelMap6 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findAssociatedMBeans",
			       params2,
			       signature1));
	    printMap(refRelMap6);

	    echo("\n\tTHIRD, MBean + role name");
	    Object[] params7 = new Object[3];
	    params7[0] = theMBeanObjName;
	    params7[1] = null;
	    params7[2] = theRoleName;
	    HashMap refRelMap7 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findAssociatedMBeans",
			       params3,
			       signature1));
	    printMap(refRelMap7);

	    echo("\n\tTHEN, MBean + relation type + role name");
	    Object[] params8 = new Object[3];
	    params8[0] = theMBeanObjName;
	    params8[1] = theRelTypeName;
	    params8[2] = theRoleName;
	    HashMap refRelMap8 = (HashMap)
		(server.invoke(theRelServObjName,
			       "findAssociatedMBeans",
			       params8,
			       signature1));
	    printMap(refRelMap8);
	} catch(Exception e) {
	    echo("\t!!! Could not do queries!!!");
	    printException(e);
	}
    }

    // print a list
    private void printList(ArrayList theList) {
	if (theList == null) {
	    return;
	}
	StringBuffer listDesc = new StringBuffer();
	for (Iterator iter = theList.iterator();
	     iter.hasNext();) {
	    listDesc.append("\t");
	    Object currObj = iter.next();
	    listDesc.append(currObj.toString());
	    if (iter.hasNext()) {
		listDesc.append(",\n");
	    }
	}
	echo(listDesc.toString());
	return;
    }

    // print a map with keys, and values being ArrayList
    private void printMap(HashMap theMap) {
	if (theMap == null) {
	    return;
	}
	for (Iterator keyIter = (theMap.keySet()).iterator();
	     keyIter.hasNext();) {
	    Object currObj = keyIter.next();
	    echo("\t" + currObj.toString());
	    echo("\tvalue:");
	    ArrayList currVal = (ArrayList)(theMap.get(currObj));
	    printList(currVal);
	}
	return;
    }

    // Add a reference in a role in a relation via the relation service
    private void singleRoleIndirectAdd(String theRelId,
				       String theRoleName,
				       ObjectName theRefToAdd,
					  ObjectName theRelServObjName) {
	echo("\n>>> UPDATE ROLE VIA RELATION SERVICE: add reference to " + theRefToAdd + " in role " + theRoleName + " in relation " + theRelId);
	try {
	    // First get the role value
	    echo("\n-> FIRST, get role value");
	    Object[] params1 = new Object[2];
	    params1[0] = theRelId;
	    params1[1] = theRoleName;
	    String[] signature1 = new String[2];
	    signature1[0] = "java.lang.String";
	    signature1[1] = "java.lang.String";
	    ArrayList oldRoleValue = (ArrayList)
		(server.invoke(theRelServObjName,
			       "getRole",
			       params1,
			       signature1));

	    ArrayList newValue = (ArrayList)(oldRoleValue.clone());
	    newValue.add(theRefToAdd);
	    echo("\n-> THEN update the value");
	    Role newRole = new Role(theRoleName, newValue);
	    Object[] params2 = new Object[2];
	    params2[0] = theRelId;
	    params2[1] = newRole;
	    String[] signature2 = new String[2];
	    signature2[0] = "java.lang.String";
	    signature2[1] = "javax.management.relation.Role";
	    server.invoke(theRelServObjName,
			  "setRole",
			  params2,
			  signature2);

	    echo("\n-> THEN check the update");
	    ArrayList newRoleValue = (ArrayList)
		(server.invoke(theRelServObjName,
			       "getRole",
			       params1,
			       signature1));
	    echo("\tnew value:");
	    printList(newRoleValue);
	} catch(Exception e) {
	    echo("\t!!! Could not do update a role in relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // update several roles in a relation via the relation service
    private void multiRoleIndirectUpdate(String theRelId,
					 RoleList theRoleList,
					 ObjectName theRelServObjName) {
	echo("\n>>> UPDATE ROLES VIA RELATION SERVICE: in relation " + theRelId);
	try {
	    Object[] params = new Object[2];
	    params[0] = theRelId;
	    params[1] = theRoleList;
	    String[] signature = new String[2];
	    signature[0] = "java.lang.String";
	    signature[1] = "javax.management.relation.RoleList";
	    RoleResult result = (RoleResult)
		(server.invoke(theRelServObjName,
			       "setRoles",
			       params,
			       signature));
	    printRoleResult(result);
	} catch(Exception e) {
	    echo("\t!!! Could not update several roles in relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // Add a reference in a role in a relation MBean via direct calls
    private void singleRoleDirectAdd(ObjectName theRelMBeanName,
				     String theRoleName,
				     ObjectName theRefToAdd) {
	echo("\n>>> UPDATE ROLE IN RELATION MBEAN: add reference to " + theRefToAdd + " in role " + theRoleName);
	try {
	    // First get the role value
	    echo("\n-> FIRST, get role value");
	    Object[] params1 = new Object[1];
	    params1[0] = theRoleName;
	    String[] signature1 = new String[1];
	    signature1[0] = "java.lang.String";
	    ArrayList oldRoleValue = (ArrayList)
		(server.invoke(theRelMBeanName,
			       "getRole",
			       params1,
			       signature1));

	    ArrayList newValue = (ArrayList)(oldRoleValue.clone());
	    newValue.add(theRefToAdd);
	    echo("\n-> THEN update the value");
	    Role newRole = new Role(theRoleName, newValue);
	    server.setAttribute(theRelMBeanName,
                                new Attribute("Role", newRole));

	    echo("\n-> THEN check the update");
	    ArrayList newRoleValue = (ArrayList)
		(server.invoke(theRelMBeanName,
			       "getRole",
			       params1,
			       signature1));
	    echo("\tnew value:");
	    printList(newRoleValue);
	} catch(Exception e) {
	    echo("\t!!! Could not update a role in relation " + theRelMBeanName + "!!!");
	    printException(e);
	}
    }

    // update several roles in a relation MBean via direct call
    private void multiRoleDirectUpdate(ObjectName theRelMBeanObjName,
				       RoleList theRoleList) {
	echo("\n>>> UPDATE ROLES IN RELATION MBEAN");
	try {
	    Object[] params = new Object[1];
	    params[0] = theRoleList;
	    String[] signature = new String[1];
	    signature[0] = "javax.management.relation.RoleList";
	    RoleResult result = (RoleResult)
		(server.invoke(theRelMBeanObjName,
			       "setRoles",
			       params,
			       signature));
	    printRoleResult(result);
	} catch(Exception e) {
	    echo("\t!!! Could not update several roles in relation MBean " + theRelMBeanObjName + "!!!");
	    printException(e);
	}
    }

    // create a relation MBean and add it in relation service
    private void createRelationMBean(ObjectName theRelMBeanObjName,
				     String theRelMBeanClassName,
				     String theRelId,
				     String theRelTypeName,
				     RoleList theRoleList,
				     ObjectName theRelServObjName) {
	echo("\n>>> CREATE RELATION MBEAN objectName: " + theRelMBeanObjName + ", relation id: " + theRelId + ", relation type name: " + theRelTypeName);
	try {
	    Object[] params1 = new Object[4];
	    params1[0] = theRelId;
	    params1[1] = theRelServObjName;
	    params1[2] = theRelTypeName;
	    params1[3] = theRoleList;
	    String[] signature1 = new String[4];
	    signature1[0] = "java.lang.String";
	    signature1[1] = "javax.management.ObjectName";
	    signature1[2] = "java.lang.String";
	    signature1[3] = "javax.management.relation.RoleList";
	    server.createMBean(theRelMBeanClassName,
			       theRelMBeanObjName,
			       params1,
			       signature1);

	    addRelation(theRelMBeanObjName, theRelServObjName);

	} catch(Exception e) {
	    echo("\t!!! Could not create relation MBean for relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // Adds an existing MBean as a relation
    private void addRelation(ObjectName theRelMBeanObjName,
			     ObjectName theRelServObjName) {
	echo("\n>>> ADDING MBEAN AS RELATION, ObjectName: " + theRelMBeanObjName);
	try {
	    Object[] params2 = new Object[1];
	    params2[0] = theRelMBeanObjName;
	    String[] signature2 = new String[1];
	    signature2[0] = "javax.management.ObjectName";
	    server.invoke(theRelServObjName,
			  "addRelation",
			  params2,
			  signature2);
	} catch(Exception e) {
	    echo("\t!!! Could not add relation MBean " + theRelMBeanObjName + "!!!");
	    printException(e);
	}
    }

    // use relation operations on relation MBean, and specific accessors
    // as isRelationMBean
    private void testRelationMBeanOperations(ObjectName theRelMBeanObjName) {
	echo("\n>>> TEST RELATION MBEAN RELATION OPERATIONS objectName: " + theRelMBeanObjName);
	try {
	    // Will test getRole(), getRoles(), getAllRoles(),
	    // getReferencedMBeans() and getRelationTypeName()
	    testRelationIndirectAccess(true, null, null, theRelMBeanObjName);

	    // test other methods

	    echo("\n-> Retrieve Relation Service Name");
	    ObjectName relServName = (ObjectName)
		(server.getAttribute(theRelMBeanObjName,
                                     "RelationServiceName"));
	    echo("\n Relation Service name: " + relServName);

	    echo("\n-> Retrieve relation id");
	    String relId = (String)
		(server.getAttribute(theRelMBeanObjName,
                                     "RelationId"));
	    echo("\n Relation id: " + relId);

	} catch(Exception e) {
	    echo("\t!!! Could not exercise relation management operations on relation " + theRelMBeanObjName + "!!!");
	    printException(e);
	}
    }

    // Test Relation Service specific accessors for relation MBean
    private void
	testRelationMBeanSpecificOperations(String theRelId,
					    ObjectName theRelServObjName,
					    ObjectName theRelMBeanObjName) {
	echo("\n>>> TEST RELATION SERVICE ACCESSORS FOR RELATION MBEAN " + theRelId + " with name " + theRelMBeanObjName);
	try {
	    // Test accessor to determine if relation id is associated to a
	    // relation MBean
	    echo("\n-> test that relation id is associated to right ObjectName");
	    Object[] params1 = new Object[1];
	    params1[0] = theRelId;
	    String[] signature1 = new String[1];
	    signature1[0] = "java.lang.String";
	    ObjectName relMBeanName = (ObjectName)
		(server.invoke(theRelServObjName,
			       "isRelationMBean",
			       params1,
			       signature1));
	    echo("\n\t Retrieved ObjectName: " + relMBeanName);

	    // Test accessor to retrieve relation id associated to an
	    // ObjectName if associated MBean has been added as a relation
	    echo("\n -> test that ObjectName is associated to relation id");
	    Object[] params2 = new Object[1];
	    params2[0] = theRelMBeanObjName;
	    String[] signature2 = new String[1];
	    signature2[0] = "javax.management.ObjectName";
	    String relId = (String)
		(server.invoke(theRelServObjName,
			       "isRelation",
			       params2,
			       signature2));
	    echo("\n\t Retrieved relation id: " + relId);
	    
	} catch(Exception e) {
	    echo("\t!!! Could not exercise relation service specific operations for relation MBean " + theRelId + "!!!");
	    printException(e);
	}
    }

    // Unregister a referenced MBean
    private void unregisterMBean(ObjectName theMBeanObjName) {
	echo("\n>>> UNREGISTER " + theMBeanObjName);
	try {
	    server.unregisterMBean(theMBeanObjName);
	} catch(Exception e) {
	    echo("\t!!! Cannot unregister MBean " + theMBeanObjName + "!!!");
	    printException(e);
	}
    }

    // Remove a relation
    private void removeRelation(String theRelId,
				ObjectName theRelServObjName) {
	echo("\n>>> REMOVE RELATION " + theRelId);
	try {
	    Object[] params = new Object[1];
	    params[0] = theRelId;
	    String[] signature = new String[1];
	    signature[0] = "java.lang.String";
	    server.invoke(theRelServObjName,
			  "removeRelation",
			  params,
			  signature);
	} catch(Exception e) {
	    echo("\t!!! Cannot remove relation " + theRelId + "!!!");
	    printException(e);
	}
    }

    // Print the relation ids for all relations of given type
    private void printAllRelationIdsOfType(String theRelTypeName,
					   ObjectName theRelServObjName) {
	echo("\n>>> PRINTING ALL RELATION IDS OF TYPE " + theRelTypeName);
	try {
	    Object[] params = new Object[1];
	    params[0] = theRelTypeName;
	    String[] signature = new String[1];
	    signature[0] = "java.lang.String";
	    ArrayList relIdList = (ArrayList)
		(server.invoke(theRelServObjName,
			       "findRelationsOfType",
			       params,
			       signature));
	    printList(relIdList);
	} catch(Exception e) {
	    echo("\t!!! Cannot retrieve relations of type " + theRelTypeName + "!!!");
	    printException(e);
	}
    }

    // Removes relation type
    private void removeRelationType(String theRelTypeName,
				    ObjectName theRelServObjName) {
	echo("\n>>> REMOVING RELATION TYPE " + theRelTypeName);
	echo("This will remove all the relations of that type.");
	try {
	    Object[] params = new Object[1];
	    params[0] = theRelTypeName;
	    String[] signature = new String[1];
	    signature[0] = "java.lang.String";
	    ArrayList relIdList = (ArrayList)
		(server.invoke(theRelServObjName,
			       "removeRelationType",
			       params,
			       signature));
	    printList(relIdList);
	} catch(Exception e) {
	    echo("\t!!! Cannot remove relation type " + theRelTypeName + "!!!");
	    printException(e);
	}
    }

    private void printException(Exception theExc) {
	theExc.printStackTrace();
	StringBuffer excName = new StringBuffer();
	Exception exc = null;
	if (theExc instanceof MBeanException) {
	    boolean isMBeanExcFlg = true;
	    excName.append("MBeanException->");
	    Exception exc1 = theExc;
	    while (isMBeanExcFlg) {
		exc = ((MBeanException)exc1).getTargetException();
		if (!(exc instanceof MBeanException)) {
		    isMBeanExcFlg = false;
		} else {
		    exc1 = exc;
		    excName.append("MBeanException->");
		}
	    }
	} else {
	    exc = theExc;
	}
	excName.append((exc.getClass()).getName());
	echo("Exception: " + excName.toString());
	echo(exc.getMessage());
	echo("\nEXITING...\n");
	System.exit(1);
    }

    // Adds user relation type to the Relation Service
    private void addRelationType(RelationType theRelTypeObj,
				 ObjectName theRelServObjName) {
	echo("\n>>> ADDING USER RELATION TYPE");
	try {
	    Object[] params = new Object[1];
	    params[0] = theRelTypeObj;
	    String[] signature = new String[1];
	    signature[0] = "javax.management.relation.RelationType";
	    server.invoke(theRelServObjName,
			  "addRelationType",
			  params,
			  signature);
	} catch(Exception e) {
	    echo("\t!!! Cannot add user relation type!!!");
	    printException(e);
	}
    }

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    private MBeanServer server = null;
    
}
