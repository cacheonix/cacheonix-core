/*
 * @(#)file      SimpleRelation.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.9
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

import javax.management.ObjectName;

import javax.management.relation.*;

/**
 * Simple definition of a standard MBean, named "SimpleRelation".
 *
 * The "SimpleRelation" standard MBean shows how to define a relation MBean,
 * i.e. expected to represent a relation but also including attributes and  
 * operations for management by implementing its corresponding  
 * "SimpleRelationMBean" management interface.
 *
 * This MBean has two attributes and one operation exposed 
 * for management by a JMX agent:
 *      - the read/write "State" attribute,
 *      - the read only "NbChanges" attribute,
 *	- the "reset()" operation.
 *
 * This object also has one property and one method not exposed
 * for management by a JMX agent:
 *	- the "NbResets" property,
 *	- the "getNbResets()" method.
 *
 * It extends the RelationSupport class to retrieve the implementation of the
 * Relation interface.
 */

public class SimpleRelation extends RelationSupport
    implements SimpleRelationMBean {

    /*
     * -----------------------------------------------------
     * CONSTRUCTORS
     * -----------------------------------------------------
     */

    public SimpleRelation(String theRelId,
			  ObjectName theRelServiceName,
			  String theRelTypeName,
			  RoleList theRoleList)
	throws InvalidRoleValueException,
               IllegalArgumentException {

	super(theRelId, theRelServiceName, theRelTypeName, theRoleList);
    }


    /*
     * -----------------------------------------------------
     * IMPLEMENTATION OF THE SimpleRelationMBean INTERFACE
     * -----------------------------------------------------
     */

    /**
     * Getter: get the "State" attribute of the "SimpleRelation" standard MBean.
     *
     * @return the current value of the "State" attribute.
     */
    public String getState() {
        return state;
    }

    /** 
     * Setter: set the "State" attribute of the "SimpleRelation" standard MBean.
     *
     * @param <VAR>s</VAR> the new value of the "State" attribute.
     */
    public void setState(String s) {
        state = s;
        nbChanges++;
    }

    /**
     * Getter: get the "NbChanges" attribute of the "SimpleRelation" standard MBean.
     *
     * @return the current value of the "NbChanges" attribute.
     */
    public Integer getNbChanges() {
        return new Integer(nbChanges);
    }

    /**
     * Operation: reset to their initial values the "State" and "NbChanges" 
     * attributes of the "SimpleRelation" standard MBean. 
     */
    public void reset() {
	state = "initial state";
        nbChanges = 0;
	nbResets++;
    }


    /*
     * -----------------------------------------------------
     * METHOD NOT EXPOSED FOR MANAGEMENT BY A JMX AGENT
     * -----------------------------------------------------
     */

    /**
     * Return the "NbResets" property. 
     * This method is not a Getter in the JMX sense because 
     * it is not exposed in the "SimpleRelationMBean" interface.
     *
     * @return the current value of the "NbResets" property.
     */
    public Integer getNbResets() {
	return new Integer(nbResets);
    }


    /*
     * -----------------------------------------------------
     * ATTRIBUTES ACCESSIBLE FOR MANAGEMENT BY A JMX AGENT
     * -----------------------------------------------------
     */

    private String	state = "initial state";
    private int		nbChanges = 0;


    /*
     * -----------------------------------------------------
     * PROPERTY NOT ACCESSIBLE FOR MANAGEMENT BY A JMX AGENT
     * -----------------------------------------------------
     */

    private int		nbResets = 0;	
}
