/*
 * @(#)file      DynamicObservedObject.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.13
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
import java.lang.reflect.*;

// RI imports
//
import javax.management.*;

/**
 * Definition of a simple dynamic MBean.
 *
 * @version     1.13     07/15/03
 * @author      Sun Microsystems, Inc
 */

public class DynamicObservedObject implements DynamicMBean, MBeanRegistration {

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */
    
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws java.lang.Exception {
        
        if (name == null)
            name = new ObjectName(server.getDefaultDomain() + ":name=" + this.getClass().getName());
        
        this.server = server;
        return name;
    } 

    public void postRegister(Boolean registrationDone) {
    } 

    public void preDeregister() throws java.lang.Exception {
    }

    public void postDeregister() {
    }
    
    // DYNAMIC MBEAN INTERFACE IMPLEMENTATION
    //---------------------------------------
    
    public AttributeList getAttributes(String[] attributes) {
        return  null;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    public Object getAttribute(String attribute) 
        throws AttributeNotFoundException, MBeanException, ReflectionException {
        
        if (attribute.equals("NbObjects")) {
            try {
		return new Integer((server.queryMBeans(new ObjectName("*:*"),  null)).size());
            } 
            catch (Exception e) {
                return new Integer(-1);
            }
        }
        return null;
    }
    
    public void setAttribute(Attribute attribute) 
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException  {
    }

    public Object invoke(String actionName, Object params[], String signature[])
        throws MBeanException, ReflectionException {
        return null;
    }

    public MBeanInfo getMBeanInfo(){
        
        MBeanAttributeInfo attributes[] = new MBeanAttributeInfo[1];
        try {
            attributes[0] = new MBeanAttributeInfo("NbObjects","java.lang.Integer", "Returns the number of MBeans registered", 
						      true, false,false);
        } 
        catch (Exception e) {
        }
    
        return new MBeanInfo(getClass().getName(), "The object to be observed by the CounterMonitor", 
			     attributes, null, null, null);	
    }

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    private MBeanServer server = null;
}
