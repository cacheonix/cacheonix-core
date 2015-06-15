/*
 * @(#)file      StandardObservedObject.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.9
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
 * Definition of a simple standard MBean.
 *
 * @version     4.9     07/15/03
 * @author      Sun Microsystems, Inc
 */

public class StandardObservedObject implements StandardObservedObjectMBean, MBeanRegistration {

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
    
    
    // GETTERS AND SETTERS
    //--------------------
    
    public Integer getNbObjects() {
        
        try {
	      return new Integer((server.queryMBeans(new ObjectName("*:*"),  null)).size());
        } 
        catch (Exception e) {
            return new Integer(-1);
        }
    }

    /*
     * ------------------------------------------
     *  PRIVATE VARIABLES
     * ------------------------------------------
     */
    
    private MBeanServer server = null;
}
