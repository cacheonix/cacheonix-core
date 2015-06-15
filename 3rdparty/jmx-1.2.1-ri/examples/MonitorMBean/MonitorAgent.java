/*
 * @(#)file      MonitorAgent.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.23
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
import java.util.Vector;
import java.util.Set;
import java.util.Enumeration;

// RI imports
//
import javax.management.*;
import javax.management.monitor.*;

/**
 * @version     1.23     07/15/03
 * @author      Sun Microsystems, Inc
 */

public class MonitorAgent {

    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    
    public MonitorAgent() {
        
        server = MBeanServerFactory.createMBeanServer();
        listener = new AgentListener();
        
        standardObsObj = new StandardObservedObject();
        dynamicObsObj = new DynamicObservedObject();
        
        counterMonitor = new CounterMonitor();
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */
    
    public static void main(String[] args) {
        
        trace("\n>>> CREATE and START the Agent...");
        MonitorAgent agent = new MonitorAgent();
        
        // Initialize the CounterMonitor MBean.
        //
        if (agent.initializeCounter() != 0) {
            trace("\n>>> An error occurred when initializing the CounterMonitor MBean...");
            System.exit(1);
        }        
        
        // MANAGEMENT OF A STANDARD MBEAN.
        //
        
        // Initialize the Standard Observed MBean.
        // Update and start the CounterMonitor MBean.
        //
        if (agent.initializeStandardMBean() != 0) {
            trace("\n>>> An error occurred when initializing the Standard Observed MBean...");
            System.exit(1);
        }
        
        // Create 6 instances of Simple MBeans.
        //            
        agent.populateTheAgent();
        
        // Delete the 6 instances of Simple MBeans.
        //            
        agent.depopulateTheAgent();
                
        // Finalize the Standard Observed MBean.
        // Stop the CounterMonitor MBean.
        //
        if (agent.finalizeStandardMBean() != 0) {
            trace("\n>>> An error occurred when finalizing the Standard Observed MBean...");
            System.exit(1);
        }
                
        // MANAGEMENT OF A DYNAMIC MBEAN.
        //
        
        // Initialize the Dynamic Observed MBean.
        // Update and start the CounterMonitor MBean.
        //
        if (agent.initializeDynamicMBean() != 0) {
            trace("\n>>> An error occurred when initializing the Dynamic Observed MBean...");
            System.exit(1);
        }
        
        // Create 6 instances of Simple MBeans.
        //            
        agent.populateTheAgent();
        
        // Delete the 6 instances of Simple MBeans.
        //            
        agent.depopulateTheAgent();
                
        // Finalize the Dynamic Observed MBean.
        // Stop the CounterMonitor MBean.
        //
        if (agent.finalizeDynamicMBean() != 0) {
            trace("\n>>> An error occurred when finalizing the Dynamic Observed MBean...");
            System.exit(1);
        }
        
        // Finalize the CounterMonitor MBean.
        //
        if (agent.finalizeCounter() != 0) {
            trace("\n>>> An error occurred when finalizing the CounterMonitor MBean...");
            System.exit(1);
        }
        
        trace("\n>>> STOP the Agent...\n");
        System.exit(0);
    }

    /*
     * ------------------------------------------
     *  PRIVATE METHODS
     * ------------------------------------------
     */
    
    private int initializeCounter() {
    
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Get the domain name from the MBeanServer.
        //
        String domain = server.getDefaultDomain();
                
        // Create a new CounterMonitor MBean and add it to the MBeanServer.
        // 
        trace("\n>>> CREATE a new CounterMonitor MBean:");
        try {
            counterMonitorName = new ObjectName(domain + ":name=" + counterMonitorClass);
        } catch(MalformedObjectNameException e) {
            e.printStackTrace();
            return 1;
        }
        trace("\tOBJECT NAME = " + counterMonitorName);
        try {
            server.registerMBean(counterMonitor, counterMonitorName);
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
        
        // Register a CounterMonitor notification listener with the CounterMonitor MBean,
        // enabling the Agent to receive CounterMonitor notification emitted by the CounterMonitor. 
        //
        trace("\n>>> ADD a listener to the CounterMonitor...");
        try {
            counterMonitor.addNotificationListener(listener, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        
        return 0;
    }
    
    private int initializeStandardMBean() {
        	
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Get the domain name from the MBeanServer.
        //
        String domain = server.getDefaultDomain();
        
        // Create a new StandardObservedObject MBean and add it to the MBeanServer.
        //
        trace("\n>>> CREATE a new StandardObservedObject MBean:");
        try {
            standardObsObjName = new ObjectName(domain + ":name=" + standardObsObjClass);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            return 1;
        }
        trace("\tOBJECT NAME           = " + standardObsObjName);
        try {
            server.registerMBean(standardObsObj, standardObsObjName) ;
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
      
        // Access the attributes of the StandardObservedObject MBean and get the initial attribute values.
        //
        trace("\tATTRIBUTE \"NbObjects\" = " + standardObsObj.getNbObjects());
        
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Initialize the attributes of the CounterMonitor MBean.
        //
        Integer threshold = new Integer(6);
        Integer offset  = new Integer(2);

        trace("\n>>> SET the attributes of the CounterMonitor:");
        
        try {
            counterMonitor.addObservedObject(standardObsObjName);
            trace("\tATTRIBUTE \"ObservedObject\"    = " + standardObsObjName);
            
            counterMonitor.setObservedAttribute("NbObjects");
            trace("\tATTRIBUTE \"ObservedAttribute\" = NbObjects");
            
            counterMonitor.setNotify(true);
            trace("\tATTRIBUTE \"Notify\"            = true");
            
            counterMonitor.setInitThreshold(threshold);
            trace("\tATTRIBUTE \"Threshold\"         = " + threshold);
            
            counterMonitor.setOffset(offset);
            trace("\tATTRIBUTE \"Offset\"            = " + offset);
            
            counterMonitor.setGranularityPeriod(1000);
            trace("\tATTRIBUTE \"GranularityPeriod\" = 1 second");
        } 
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
            
        trace("\n>>> START the CounterMonitor...");
        counterMonitor.start();
        
        return 0;
    }
  
    private int initializeDynamicMBean() {
        	
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Get the domain name from the MBeanServer.
        //
        String domain = server.getDefaultDomain();
        
        // Create a new DynamicObservedObject MBean and add it to the MBeanServer.
        //
        trace("\n>>> CREATE a new DynamicObservedObject MBean:");
        try {
            dynamicObsObjName = new ObjectName(domain + ":name=" + dynamicObsObjClass);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            return 1;
        }
        trace("\tOBJECT NAME           = " + dynamicObsObjName);
        try {
            server.registerMBean(dynamicObsObj, dynamicObsObjName) ;
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
      
        // Access the attributes of the DynamicObservedObject MBean and get the initial attribute values.
        //
        MBeanAttributeInfo[] attributes = dynamicObsObj.getMBeanInfo().getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            try {
                trace("\tATTRIBUTE \"" + attributes[i].getName() + "\" = " + dynamicObsObj.getAttribute(attributes[i].getName()));
            } 
            catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
        }
        
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Initialize the attributes of the CounterMonitor MBean.
        //
        Integer threshold = new Integer(6);

        trace("\n>>> SET the attributes of the CounterMonitor:");
        
        try {
            
            counterMonitor.addObservedObject(dynamicObsObjName);
            trace("\tATTRIBUTE \"ObservedObject\"    = " + dynamicObsObjName);
            
            counterMonitor.setObservedAttribute(attributes[0].getName());
            trace("\tATTRIBUTE \"ObservedAttribute\" = " + counterMonitor.getObservedAttribute());
            
            trace("\tATTRIBUTE \"Notify\"            = " + counterMonitor.getNotify());
            
            counterMonitor.setInitThreshold(threshold);
            trace("\tATTRIBUTE \"Threshold\"         = " + threshold);
            
            trace("\tATTRIBUTE \"Offset\"            = " + counterMonitor.getOffset());
            
            trace("\tATTRIBUTE \"GranularityPeriod\" = 1 second");
        } 
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
            
        trace("\n>>> START the CounterMonitor...");
        counterMonitor.start();
        
        return 0;
    }
    
    private int finalizeCounter() {
	
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Remove the CounterMonitor notification listener. 
        //
        trace("\n>>> REMOVE the CounterMonitor listener");
        try {
            counterMonitor.removeNotificationListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        
        // Delete the CounterMonitor MBean.
        //
        trace("\n>>> DELETE the CounterMonitor MBean:");
        trace("\tOBJECT NAME = " + counterMonitorName);
        try {
            server.unregisterMBean(counterMonitorName);
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }    
        return 0;
    }
    
    private int finalizeStandardMBean() {
	
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        trace("\n>>> STOP the CounterMonitor...");
        counterMonitor.stop();

        // Removes the StandardObservedObject MBean from the counter monitor's
        // observed objects
        //
        trace("\n>>> REMOVE the the StandardObservedObject MBean from the observed objects in the CounterMonitor...");
        counterMonitor.removeObservedObject(standardObsObjName);
            
        // Deletes the StandardObservedObject MBean.
        //
        trace("\n>>> DELETE the StandardObservedObject MBean:");
        trace("\tOBJECT NAME = " + standardObsObjName);
        try {
            server.unregisterMBean(standardObsObjName);
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
    
    private int finalizeDynamicMBean() {
	
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        trace("\n>>> STOP the CounterMonitor...");
        counterMonitor.stop();

        // Removes the StandardObservedObject MBean from the counter monitor's
        // observed objects
        //
        trace("\n>>> REMOVE the the DynamicObservedObject MBean from the observed objects in the CounterMonitor...");
        counterMonitor.removeObservedObject(dynamicObsObjName);

        // Deletes the DynamicObservedObject MBean.
        //
        trace("\n>>> DELETE the DynamicObservedObject MBean:");
        trace("\tOBJECT NAME = " + dynamicObsObjName);
        try {
            server.unregisterMBean(dynamicObsObjName);
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
    
    private void populateTheAgent() {
        
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        // Get the domain name from the MBeanServer.
        //
        String domain = server.getDefaultDomain();
      
        try {
            trace("\n>>> PRESS <Enter> TO START THE CREATION OF 6 SIMPLE BEANS...");
            System.in.read();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        // Create 6 instances of Simple MBeans and add them to the MBeanServer.
        // 
        simpleNameVector = new Vector();
        for (int count = 0; count < 6; count++) {
            Simple simpleBean = new Simple();
            ObjectName simpleName = null;
            try {
                simpleName = new ObjectName(domain + ":name=Simple,number=" + count);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
                System.exit(1);
            }
            try {
                server.registerMBean(simpleBean, simpleName);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            trace(">>> CREATE a new Simple MBean => " + getNbObjects() + " NbObjects in the MBeanServer");
        
            simpleNameVector.addElement(simpleName);
                        
            // Wait a few seconds for the monitor to update.
            //
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }  
    }
    
    private void depopulateTheAgent() {
        
        trace("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                
        try {
            trace("\n>>> PRESS <Enter> TO START THE DELETION OF 6 SIMPLE BEANS...");
            System.in.read();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        // Delete 6 instances of Simple MBeans.
        //
        int count = 0;
        for (Enumeration simpleNames = simpleNameVector.elements(); 
	     simpleNames.hasMoreElements(); ) {
            ObjectName simpleName = (ObjectName)simpleNames.nextElement();
            try {
                server.unregisterMBean(simpleName);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            trace(">>> DELETE a Simple MBean => " + getNbObjects() + " NbObjects in the MBeanServer");
        }
    }
    
    private int getNbObjects() {
        
        try {
	    return server.queryMBeans(new ObjectName("*:*"),  null).size();
        } 
        catch (Exception e) {
            return (-1);
        }
    }
    
    private static void trace(String msg) {
        System.out.println(msg);
    }
    
    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    private MBeanServer server = null;
    private AgentListener listener = null;
    
    private StandardObservedObject standardObsObj = null;
    private DynamicObservedObject dynamicObsObj = null;
    
    private CounterMonitor counterMonitor = null;
    
    // Object names.
    //
    ObjectName standardObsObjName = null;
    ObjectName dynamicObsObjName = null;
    ObjectName counterMonitorName = null;
    
    // Class names.
    //
    static String standardObsObjClass = "StandardObservedObject";
    static String dynamicObsObjClass = "DynamicObservedObject";
    static String counterMonitorClass = "javax.management.monitor.CounterMonitor";
    
    // Vector containing all the Simple MBeans names.
    //
    Vector simpleNameVector = null;
}
