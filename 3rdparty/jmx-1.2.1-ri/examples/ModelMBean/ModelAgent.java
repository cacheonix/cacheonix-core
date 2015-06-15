/*
 * @(#)file      ModelAgent.java
 * @(#)author    IBM Corp.
 * @(#)version   1.4
 * @(#)date      00/09/01
 */
/*
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 * 
 * The program is provided "as is" without any warranty express or implied,
 * including the warranty of non-infringement and the implied warranties of
 * merchantibility and fitness for a particular purpose. IBM will not be
 * liable for any damages suffered by you or any third party claim against 
 * you regarding the Program.
 *
 * Copyright 2000-2003 Sun Microsystems, Inc.  All rights reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * 
 * Copyright 2000-2003 Sun Microsystems, Inc.  Tous droits réservés.
 * Ce logiciel est proprieté de Sun Microsystems, Inc.
 * Distribué par des licences qui en restreignent l'utilisation. 
 *
 */


// java imports//
import java.io.*;
import java.net.*;
import java.lang.reflect.Constructor;


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
import javax.management.Descriptor;

import javax.management.NotificationListener;

import javax.management.MalformedObjectNameException;
import javax.management.modelmbean.*;



public class ModelAgent
{

	/*
	 * ------------------------------------------
	 *  CONSTRUCTORS
	 * ------------------------------------------
	 */

	public ModelAgent()
	{

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

	public static void main(String[] args)
	{

		// START
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		echo("\n>>> CREATE the agent...");
		ModelAgent agent = new ModelAgent();
	

		// DO THE DEMO
		//
		agent.doSimpleDemo();

		// END
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		echo("\n>>> END of the SimpleModel example:\n");
		String localHost = null;
		try
		{
			localHost = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e)
		{
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

	private void doSimpleDemo()
	{

		// build the simple MBean ObjectName
		//
		ObjectName mbeanObjectName = null;
		String domain = server.getDefaultDomain();
		String mbeanName = "ModelSample";

		attrListener = (NotificationListener) new TestBeanAttributeChangeListener();

		try
		{
			mbeanObjectName = new ObjectName(domain + ":type=" + mbeanName);
		} catch (MalformedObjectNameException e)
		{
			echo("\t!!! Could not create the MBean ObjectName !!!");
			e.printStackTrace();
			echo("\nEXITING...\n");
			System.exit(1);
		}
		// create and register the MBean
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		createMBean(mbeanObjectName, mbeanName);
		echo("\npress <Enter> to continue...\n");
		waitForEnterPressed();

		// get and display the management information exposed by the MBean
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		echo("\nPrinting Descriptors from MBeanInfo");
		printMBeanInfo(mbeanObjectName, mbeanName);
		echo("\npress <Enter> to continue...\n");
		waitForEnterPressed();

		// create an event listener
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		echo("\nCreate event listeners");
		createEventListeners(mbeanObjectName, attrListener);
		echo("\npress <Enter> to continue...\n");
		waitForEnterPressed();
		// manage the MBean
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		manageSimpleBean(mbeanObjectName ,mbeanName);
	
		// trying to do illegal management actions...
		//
		echo("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		goTooFar(mbeanObjectName);
		echo("\npress <Enter> to continue...\n");
		waitForEnterPressed();

		//issue a notification

	}

	private void createMBean(ObjectName mbeanObjectName, String mbeanName)
	{

		echo("\n>>> CREATE the " + mbeanName + " MBean within the MBeanServer:");
		String mbeanClassName = "javax.management.modelmbean.RequiredModelMBean";
		echo("\tOBJECT NAME = " + mbeanObjectName);
	
		// set management interface in ModelMBean - attributes, operations, notifications
		buildDynamicMBeanInfo(mbeanObjectName, mbeanName);
		try
		{
		    RequiredModelMBean modelmbean = new RequiredModelMBean(dMBeanInfo);
		    // Set the managed resource for ModelMBean instance
		    modelmbean.setManagedResource(new TestBean(), "objectReference");
		    // register the ModelMBean in the MBean Server
		    server.registerMBean(modelmbean,mbeanObjectName);
		} catch (Exception e)
		{
			echo("\t!!! ModelAgent: Could not create the " + mbeanName + " MBean !!!");
			e.printStackTrace();
			echo("\nEXITING...\n");
			System.exit(1);
		}      

		echo("\n\tModelMBean has been successfully created.\n");
	}

	private void createEventListeners(ObjectName mbeanObjectName, NotificationListener aListener)
	{
		try
		{

			server.invoke(mbeanObjectName, "addAttributeChangeNotificationListener",
						  (new Object[] {aListener, "State", null}),
						  (new String[] {"javax.management.NotificationListener",
									"java.lang.String",
									"java.lang.Object"}));
			echo("\n\tEvent listener created successfully\n");
		} catch (Exception e)
		{
			echo("Error! Creating Event listener with invoke failed with message:\n");
			echo(e.getMessage() + "\n");
			echo("\nEXITING...\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void manageSimpleBean(ObjectName mbeanObjectName, String mbeanName)
	{

		echo("\n>>> MANAGING the " + mbeanName + " MBean ");
		echo("using its attributes and operations exposed for management");

		try
		{
			// Get attribute values
			sleep(1000);
			echo("\n>> Printing attributes from ModelMBean \n");
			printSimpleAttributes(mbeanObjectName);

			sleep(1000);
			echo("\n>> Printing attributes from instance cache \n");
			printSimpleAttributes(mbeanObjectName);

			// Change State attribute
			sleep(1000);
			echo("\n>>  Setting State attribute to value \"new state\"...");
			Attribute stateAttribute = new Attribute("State","new state");
			server.setAttribute(mbeanObjectName, stateAttribute);
			
			// Get attribute values
			sleep(1000);
			printSimpleAttributes(mbeanObjectName);

			echo("\n>> The NbChanges attribute is still \"0\" as its cached value is valid for 5 seconds (currencyTimeLimit=5s)");

			echo("\n>> Wait for 5 seconds and print new attributes values ...");
			echo("\npress <Enter> to continue...\n");
			waitForEnterPressed();
			sleep(5000);
			printSimpleAttributes(mbeanObjectName);

			// Invoking reset operation
			sleep(1000);
			echo("\n>>  Invoking reset operation...");
			server.invoke(mbeanObjectName,"reset",null,null);

			// Get attribute values          1
			echo("\n>>  Printing reset attribute values");
			sleep(1000);
			printSimpleAttributes(mbeanObjectName);

			echo("\n>> The State and NbChanges attributes are still \"1\" and \"new state\" as their cached value is valid for 5 seconds (currencyTimeLimit=5s)");

			echo("\n>> Wait for 5 seconds and print new attributes values ...");
			echo("\npress <Enter> to continue...\n");

			waitForEnterPressed();
			sleep(5000);
			printSimpleAttributes(mbeanObjectName);

			// Getting Notifications list 
			echo("\n>> Printing Notifications Broadcasted");
			sleep(1000);
			MBeanNotificationInfo[] myNotifys = (MBeanNotificationInfo[]) server.invoke(mbeanObjectName,
																						"getNotificationInfo",
																						null, null);
			echo("\n\tSupported notifications are:");
			for (int i=0; i<myNotifys.length; i++)
			{
				echo("\n\t\t" + ((ModelMBeanNotificationInfo)myNotifys[i]).toString());
			}

			// Accesssing and printing Procol Map for NbChanges
			echo("\n>>  Exercising Protocol map for NbChanges");
			sleep(1000);
			ModelMBeanInfo myMMBI = (ModelMBeanInfo) server.invoke(mbeanObjectName,
																   "getMBeanInfo",
																   null, null);

			Descriptor myDesc = myMMBI.getDescriptor("NbChanges","attribute");

			echo("\n\tRetrieving specific protocols:");
			//          echo("Descriptor: " + myDesc.toString());
			Descriptor pm = (Descriptor) myDesc.getFieldValue("protocolMap");
			//          echo("ProtocolMap *"+pm.toString()+"*");
			echo("\tProtocolMap lookup SNMP is " + pm.getFieldValue("SNMP"));
			echo("\tProtocolMap lookup CIM is " + pm.getFieldValue("CIM"));

			echo("\n\tDynamically updating Protocol Map:");
			pm.setField("CIM","ManagedResource.LongVersion");
			pm.setField("CMIP","SwitchData");

			echo("\n\tPrinting Protocol Map");
			String[] pmKeys = pm.getFieldNames();
			Object[] pmEntries = pm.getFieldValues(null);
			for (int i=0; i < pmKeys.length; i++)
			{
				echo("\tProtocol Map Name " + i + ": Name: " + pmKeys[i] + ": Entry:" + ((String) pmEntries[i]).toString());
			}

			echo("\n>>  Testing operation caching");
			echo("\n>>  Invoking getNbResets");
			Integer numResets = (Integer) server.invoke(mbeanObjectName,"getNbResets",null,null);
			echo("\n\tReceived " + numResets + " from getNbResets first time");

			echo("\n>>  Invoking second reset operation...");
			server.invoke(mbeanObjectName,"reset",null,null);
			Integer numResets2 = (Integer) server.invoke(mbeanObjectName,"getNbResets",null,null);
			echo("\n\tReceived " + numResets2 + " from getNbResets second time (from operation cache)");

			echo("\n>>  Invoking get of attribute ONLY provided through ModelMBeanAttributeInfo (should be 99)...");
			Integer respHardValue = (Integer) server.getAttribute(mbeanObjectName,"HardValue");
			echo("\n\tReceived " + respHardValue + " from getAttribute of hardValue");


		} catch (Exception e)
		{
			echo("\nManageSimpleBean failed with " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void goTooFar(ObjectName mbeanObjectName)
	{

		echo("\n>>> Trying to set the NbChanges attribute (read-only)!");
		echo("\n... We should get an AttributeNotFoundException:\n");
		sleep(1000);
		// Try to set the NbChanges attribute
		Attribute nbChangesAttribute = new Attribute("NbChanges", new Integer(1));
		try
		{
			server.setAttribute(mbeanObjectName, nbChangesAttribute);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		echo("\n\n>>> Trying to access the NbResets property (not exposed for management)!");
		echo("\n... We should get an AttributeNotFoundException:\n");
		sleep(1000);
		// Try to access the NbResets property
		try
		{
			Integer NbResets = (Integer) server.getAttribute(mbeanObjectName,"NbResets");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}


	private void printMBeanInfo(ObjectName mbeanObjectName, String mbeanName)
	{

		echo("\n>>> Getting the management information for the "+ mbeanName + ":" + mbeanObjectName + " MBean");
		echo("    using the getMBeanInfo method of the MBeanServer");
		sleep(1000);
		ModelMBeanInfo info = null;
		try
		{
			info = (ModelMBeanInfo) (server.getMBeanInfo(mbeanObjectName));
			if (info == null)
			{
				echo("\nModelMBeanInfo from JMX Agent is null!");
			}
		} catch (Exception e)
		{
			echo("\t!!! ModelAgent:printMBeanInfo: Could not get MBeanInfo object for "+ mbeanName +" exception type " + e.getClass().toString() + ":" + e.getMessage() + "!!!");
			e.printStackTrace();
			return;
		}
		echo("\nCLASSNAME: \t"+ info.getClassName());
		echo("\nDESCRIPTION: \t"+ info.getDescription());
		try
		{
			echo("\nMBEANDESCRIPTOR: \t" + (info.getMBeanDescriptor()).toString());
		} catch (Exception e)
		{
			echo("\nMBEANDESCRIPTOR: \tNone");
		}

		echo("\nATTRIBUTES");
		MBeanAttributeInfo[] attrInfo = (info.getAttributes());
		if (attrInfo.length>0)
		{
			for (int i=0; i<attrInfo.length; i++)
			{
				echo("\n ** NAME: \t"+ attrInfo[i].getName());
				echo("    DESCR: \t"+ attrInfo[i].getDescription());
				echo("    TYPE: \t"+ attrInfo[i].getType() +
					 "\tREAD: "+ attrInfo[i].isReadable() +
					 "\tWRITE: "+ attrInfo[i].isWritable());
				echo("    DESCRIPTOR: \t" + (((ModelMBeanAttributeInfo)attrInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No attributes **");

		MBeanConstructorInfo[] constrInfo = info.getConstructors();
		echo("\nCONSTRUCTORS");
		if (constrInfo.length > 0)
		{
			for (int i=0; i<constrInfo.length; i++)
			{
				echo("\n ** NAME: \t"+ constrInfo[i].getName());
				echo("    DESCR: \t"+ constrInfo[i].getDescription());
				echo("    PARAM: \t"+ constrInfo[i].getSignature().length +" parameter(s)");
				echo("    DESCRIPTOR: \t" + (((ModelMBeanConstructorInfo)constrInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No Constructors **");

		echo("\nOPERATIONS");
		MBeanOperationInfo[] opInfo = info.getOperations();
		if (opInfo.length>0)
		{
			for (int i=0; i<opInfo.length; i++)
			{
				echo("\n ** NAME: \t"+ opInfo[i].getName());
				echo("    DESCR: \t"+ opInfo[i].getDescription());
				echo("    PARAM: \t"+ opInfo[i].getSignature().length +" parameter(s)");
				echo("    DESCRIPTOR: \t" + (((ModelMBeanOperationInfo)opInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No operations ** ");

		echo("\nNOTIFICATIONS");
		MBeanNotificationInfo[] notifInfo = info.getNotifications();
		if (notifInfo.length>0)
		{
			for (int i=0; i<notifInfo.length; i++)
			{
				echo("\n ** NAME: \t"+ notifInfo[i].getName());
				echo("    DESCR: \t"+ notifInfo[i].getDescription());
				echo("    DESCRIPTOR: \t" + (((ModelMBeanNotificationInfo)notifInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No notifications **");
		echo("\nEnd of MBeanInfo print");
	}

	private void printSimpleAttributes(ObjectName mbeanObjectName)
	{

		try
		{
			echo("\n\tGetting attribute values:");
			String State = (String) server.getAttribute(mbeanObjectName,"State");
			Integer NbChanges = (Integer) server.getAttribute(mbeanObjectName,"NbChanges");
			echo("\n\t\tState     = \"" + State + "\"");
			echo("\t\tNbChanges = \"" + NbChanges.toString() + "\"");
		} catch (Exception e)
		{
			echo("\tModelAgent:printSimpleAttributes: !!! Could not read attributes !!!");
			e.printStackTrace();
			return;
		}
	}

	private static void echo(String msg)
	{
		System.out.println(msg);
	}

	private static void sleep(int millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e)
		{
			return;
		}
	}

	private static void waitForEnterPressed()
	{
		try
		{
		    boolean done = false;
		    while (!done) {
			char ch = (char) System.in.read();
			if (ch<0||ch=='\n') {
			    done = true;
			}	
		    }	    
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * ------------------------------------------
	 *  PRIVATE VARIABLES
	 * ------------------------------------------
	 */

	private MBeanServer server = null;

	/*
	 * -----------------------------------------------------
	 * PRIVATE METHODS
	 * -----------------------------------------------------
	 */

	private void loadDynamicMBeanInfo(ObjectName inMbeanObjectName, String inMbeanName)
	{
		try
		{

			Class appBean = Class.forName(dClassName);


			//echo("\nSetting mbeanDescriptor " + mmbDesc);

			dMBeanInfo = new ModelMBeanInfoSupport(dClassName,
												   dDescription,
												   dAttributes,
												   dConstructors,
												   dOperations,
												   dNotifications,
												   mmbDesc);


		} catch (Exception e)
		{
			echo("\nException in loadDynamicMBeanInfo : " + e.getMessage());
			e.printStackTrace();
		}


	}
	/**
	 * Build the private dMBeanInfo field,
	 * which represents the management interface exposed by the MBean;
	 * that is, the set of attributes, constructors, operations and notifications
	 * which are available for management.
	 *
	 * A reference to the dMBeanInfo object is returned by the getMBeanInfo() method
	 * of the DynamicMBean interface. Note that, once constructed, an MBeanInfo object is i
	 */
	private void buildDynamicMBeanInfo(ObjectName inMbeanObjectName, String inMbeanName)
	{
		try
		{

			Class appBean = Class.forName(dClassName);

			mmbDesc = new DescriptorSupport(new String[] {("name="+inMbeanObjectName),
									  "descriptorType=mbean",
									  ("displayName="+inMbeanName),
									  "log=T",
									  "logfile=jmxmain.log",
									  "currencyTimeLimit=5"});
			
			
			Descriptor stateDesc = new DescriptorSupport();
			stateDesc.setField("name","State");
			stateDesc.setField("descriptorType","attribute");
			stateDesc.setField("displayName","MyState");
			stateDesc.setField("getMethod","getState");
			stateDesc.setField("setMethod","setState");
			stateDesc.setField("currencyTimeLimit","20");
			//          echo("\nbuildModelMBeanInfo: State descriptor is " + stateDesc.toString());
			dAttributes[0] = new ModelMBeanAttributeInfo("State",
								     "java.lang.String",
								     "State: state string.",
								     true,
								     true,
								     false,
								     stateDesc);
			
			Descriptor nbChangesDesc = new DescriptorSupport();
			nbChangesDesc.setField("name","NbChanges");
			nbChangesDesc.setField("descriptorType", "attribute");
			nbChangesDesc.setField("default", "0");
			nbChangesDesc.setField("displayName","MyChangesCount");
			nbChangesDesc.setField("getMethod","getNbChanges");
			nbChangesDesc.setField("setMethod","setNbChanges");
			Descriptor nbChangesMap = new DescriptorSupport(new String[] {
			    "SNMP=1.3.6.9.12.15.18.21.0",
				"CIM=ManagedResource.Version"});
	
			nbChangesDesc.setField("protocolMap",(nbChangesMap));
		
			
			dAttributes[1] = new ModelMBeanAttributeInfo("NbChanges",
								     "java.lang.Integer",
								     "NbChanges: number of times the State string",
								     true,
								     false,
								     false,
								     nbChangesDesc);
			
			Descriptor hardValueDesc = new DescriptorSupport();
			hardValueDesc.setField("name","HardValue");
			hardValueDesc.setField("descriptorType","attribute");
			hardValueDesc.setField("value", new Integer("99"));
			hardValueDesc.setField("displayName","HardCodedValue");
			hardValueDesc.setField("currencyTimeLimit","0");
			/* A currencyTimeLimit of 0 means that the value
			   cached in the Descriptor is always valid.  So
			   when we call getAttribute on this attribute we
			   will read this value of 99 out of the
			   Descriptor.  */
			
			dAttributes[2] = new ModelMBeanAttributeInfo("HardValue",
								     "java.lang.Integer",
								     "HardValue: static value in ModelMBeanInfo and not in TestBean",
								     true,
								     false,
								     false,
								     hardValueDesc);
			
			Constructor[] constructors = appBean.getConstructors();
			
			Descriptor testBeanDesc = new DescriptorSupport();
			testBeanDesc.setField("name","TestBean");
			testBeanDesc.setField("descriptorType", "operation");
			testBeanDesc.setField("role","constructor");
			
			dConstructors[0] = new ModelMBeanConstructorInfo("TestBean(): Constructs a TestBean App",
									 constructors[0],
									 testBeanDesc);
			
			MBeanParameterInfo[] params = null;
			
			Descriptor resetDesc = new DescriptorSupport();
			resetDesc.setField("name","reset");
			resetDesc.setField("descriptorType","operation");
			resetDesc.setField("class","TestBean");
			resetDesc.setField("role","operation");
			
			dOperations[0] = new ModelMBeanOperationInfo("reset",
								     "reset(): reset State and NbChanges",
								     params ,
								     "void",
								     MBeanOperationInfo.ACTION,
								     resetDesc);
			
			Descriptor getNbResetsDesc = new DescriptorSupport(new String[] {"name=getNbResets",
											     "class=TestBeanFriend",
											     "descriptorType=operation",
											     "role=operation"});	
			TestBeanFriend tbf = new TestBeanFriend();
			getNbResetsDesc.setField("targetObject",tbf);
			getNbResetsDesc.setField("targetType","objectReference");
			
			dOperations[1] = new ModelMBeanOperationInfo("getNbResets",
								     "getNbResets(): get number of resets done",
								     params ,
								     "java.lang.Integer",
								     MBeanOperationInfo.INFO,
								     getNbResetsDesc);
			
			Descriptor getStateDesc = new DescriptorSupport(new String[] {"name=getState",
											  "descriptorType=operation",
											  "class=TestBean",
											  "role=operation"} );
			
			dOperations[2] = new ModelMBeanOperationInfo("getState",
								     "get state attribute",
								     params ,
								     "java.lang.String",
								     MBeanOperationInfo.ACTION,
								     getStateDesc);
			
			Descriptor setStateDesc = new DescriptorSupport(new String[] {
			    "name=setState",
				"descriptorType=operation",
				"class=TestBean",
				"role=operation"});
			
			MBeanParameterInfo[] setStateParms = new MBeanParameterInfo[] { (new MBeanParameterInfo("newState",
														"java.lang.String",
														"new State value") )} ;
			
			dOperations[3] = new ModelMBeanOperationInfo("setState",
								     "set State attribute",
								     setStateParms,
								     "void",
								     MBeanOperationInfo.ACTION,
								     setStateDesc);
			
			Descriptor getNbChangesDesc = new DescriptorSupport( new String[] {
			    "name=getNbChanges",
				"descriptorType=operation",
				"class=TestBean",
				"role=operation"});
			
			dOperations[4] = new ModelMBeanOperationInfo("getNbChanges",
								     "get NbChanges attribute",
								     params,
								     "java.lang.Integer",
								     MBeanOperationInfo.INFO,
								     getNbChangesDesc);
			
			
			Descriptor setNbChangesDesc = new DescriptorSupport(new String[] {
			    "name=setNbChanges",
				"descriptorType=operation",
				"class=TestBean",
				"role=operation"});
			
			MBeanParameterInfo[] setNbChangesParms = new MBeanParameterInfo[] { (new MBeanParameterInfo("newNbChanges",
														    "java.lang.Integer",
														    "new value for Number of Changes") )} ;
			
			
			dOperations[5] = new ModelMBeanOperationInfo("setNbChanges",
								     "set NbChanges attribute",
								     setNbChangesParms,
								     "void",
								     MBeanOperationInfo.ACTION,
								     setNbChangesDesc);
			
			//dNotifications[0] = new MBeanNotificationInfo((new String[] {"jmx.attribute.change"}),
			//                        "AttributeChange","ModelMBean Attribute Change Event");
			
			Descriptor genEventDesc = new DescriptorSupport(new String[] {"descriptorType=notification", 
											  "name=jmx.ModelMBean.General", 
											  "severity=4", 
											  "MessageId=MA001", 
											  "log=T", 
											  "logfile=jmx.log"});
			String[] genTypes = new String[1];
			genTypes[0] = "jmx.ModelMBean.General";
			dNotifications[0] = new ModelMBeanNotificationInfo(genTypes,
									   "jmx.ModelMBean.General", // was Generic
									   "Generic Event",
									   genEventDesc); // test event
			
			dMBeanInfo = new ModelMBeanInfoSupport(dClassName,
							       dDescription,
							       dAttributes,
							       dConstructors,
							       dOperations,
							       dNotifications);
			
			dMBeanInfo.setMBeanDescriptor(mmbDesc);
			
		} catch (Exception e)
		    {
			echo("\nException in buildDynamicMBeanInfo : " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void printLocalMBeanInfo(MBeanInfo info)
	{
		echo("\nCLASSNAME: \t"+ info.getClassName());
		echo("\nDESCRIPTION: \t"+ info.getDescription());
		echo("\nATTRIBUTES");
		MBeanAttributeInfo[] attrInfo = info.getAttributes();
		if (attrInfo.length>0)
		{
			for (int i=0; i<attrInfo.length; i++)
			{
				echo(" ** NAME: \t"+ attrInfo[i].getName());
				echo("    DESCR: \t"+ attrInfo[i].getDescription());
				echo("    TYPE: \t"+ attrInfo[i].getType() +
					 "\tREAD: "+ attrInfo[i].isReadable() +
					 "\tWRITE: "+ attrInfo[i].isWritable());
				echo("    DESCRIPTOR: \t" + (((ModelMBeanAttributeInfo)attrInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No attributes **");
		echo("\nCONSTRUCTORS");
		MBeanConstructorInfo[] constrInfo = info.getConstructors();
		for (int i=0; i<constrInfo.length; i++)
		{
			echo(" ** NAME: \t"+ constrInfo[i].getName());
			echo("    DESCR: \t"+ constrInfo[i].getDescription());
			echo("    PARAM: \t"+ constrInfo[i].getSignature().length +" parameter(s)");
			echo("    DESCRIPTOR: \t" + (((ModelMBeanConstructorInfo)constrInfo[i]).getDescriptor()).toString());
		}
		echo("\nOPERATIONS");
		MBeanOperationInfo[] opInfo = info.getOperations();
		if (opInfo.length>0)
		{
			for (int i=0; i<opInfo.length; i++)
			{
				echo(" ** NAME: \t"+ opInfo[i].getName());
				echo("    DESCR: \t"+ opInfo[i].getDescription());
				echo("    PARAM: \t"+ opInfo[i].getSignature().length +" parameter(s)");
				echo("    DESCRIPTOR: \t" + (((ModelMBeanOperationInfo)opInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No operations ** ");
		echo("\nNOTIFICATIONS");
		MBeanNotificationInfo[] notifInfo = info.getNotifications();
		if (notifInfo.length>0)
		{
			for (int i=0; i<notifInfo.length; i++)
			{
				echo(" ** NAME: \t"+ notifInfo[i].getName());
				echo("    DESCR: \t"+ notifInfo[i].getDescription());
				echo("    DESCRIPTOR: \t" + (((ModelMBeanNotificationInfo) notifInfo[i]).getDescriptor()).toString());
			}
		} else echo(" ** No notifications **");

	}


	public void printLocalDescriptors(MBeanInfo mbi) throws javax.management.MBeanException
	{
		echo(mbi.getDescription() + "Descriptors:\n");
		echo("Attribute Descriptors:\n");
		Descriptor[] dArray;
		dArray = ((ModelMBeanInfo) mbi).getDescriptors("attribute");
		for (int i = 0; i < dArray.length; i++)
		{
			String[] afields = ((Descriptor) dArray[i]).getFields();
			for (int j=0; j < afields.length; j++)
			{
				echo(afields[j] + "\n");
			}
		}

		echo("Operation Descriptors:\n");
		dArray = ((ModelMBeanInfo) mbi).getDescriptors("operation");
		for (int i=0; i < dArray.length; i++)
		{
			echo("\n*Operation****************************");
			String[] ofields = ((Descriptor) dArray[i]).getFields();
			for (int j=0; j < ofields.length; j++)
			{
				echo(ofields[j] + "\n");
			}
		}

		echo("Notification Descriptors:\n");
		dArray = ((ModelMBeanInfo) mbi).getDescriptors("notification");
		for (int i=0; i < dArray.length; i++)
		{
			System.out.println("**Notification****************************");
			String[] nfields = ((Descriptor) dArray[i]).getFields();
			for (int j=0; j < nfields.length; j++)
			{
				System.out.println(nfields[j] + "\n");
			}
		}
	}

	public void printModelMBeanDescriptors(ObjectName mbeanObjectName)
	{
		sleep(1000);

		Descriptor[] dArray = new DescriptorSupport[0];
		try
		{
			dArray = (Descriptor[]) (server.invoke(mbeanObjectName, "getDescriptors",
												   new Object[] {},
												   new String[] {}));
			if (dArray == null)
			{
				echo("\nDescriptor list is null!");
			}
		} catch (Exception e)
		{
			echo("\t!!! Could not get descriptors for mbeanName ");
			e.printStackTrace();
			return;
		}

		echo("Descriptors: (");
		echo(dArray.length + ")\n");
		for (int i=0; i < dArray.length; i++)
		{
			echo("\n**Descriptor***********************");
			String[] dlfields =  ((Descriptor) dArray[i]).getFields();
			for (int j=0; j < dlfields.length; j++)
			{
				echo(dlfields[j] + "\n");
			}
		}
	}

	/*
	 * -----------------------------------------------------
	 * PRIVATE VARIABLES
	 * -----------------------------------------------------
	 */

	private String dClassName = "TestBean";
	private String dDescription = "Simple implementation of a test app Bean.";

	private ModelMBeanAttributeInfo[] dAttributes = new ModelMBeanAttributeInfo[3];
	private ModelMBeanConstructorInfo[] dConstructors = new ModelMBeanConstructorInfo[1];
	private ModelMBeanOperationInfo[] dOperations = new ModelMBeanOperationInfo[6];
	private ModelMBeanNotificationInfo[] dNotifications = new ModelMBeanNotificationInfo[1];
	private Descriptor mmbDesc = null;
	private ModelMBeanInfo dMBeanInfo = null;

	NotificationListener attrListener = null;
}
