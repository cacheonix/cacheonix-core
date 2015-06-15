/*
 * @(#)file      TestBean.java
 * @(#)author    IBM Corp.
 * @(#)version   1.5
 * @(#)lastedit      03/07/15
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


/**
 * Simple definition of a Bean which is managed by a ModelMBean, named "TestBean"  .
 *
 * The "TestBean" Bean shows how to expose for management
 * attributes and operations, at runtime,  by instantiating and customizing
 * the ModelMBean implementation. (See AgentModel code for this)  
 *
 * This Bean exposes for management two attributes and one operation:
 *      - the read/write "State" attribute,
 *      - the read only "NbChanges" attribute,
 *  - the "reset()" operation.
 *
 * The program creating the ModelMBean puts this information in an ModelMBeanInfo object that
 * is returned by the getMBeanInfo() method of the DynamicMBean interface.
 *
 * The ModelMBean implementation implements the access to its attributes through the getAttribute(),
 * getAttributes(), setAttribute(), and setAttributes() methods of the
 * DynamicMBean interface.
 *
 * The ModelMBean implements the invocation of its reset() operation through the
 * invoke() method of the DynamicMBean interface.
 * 
 * Note that as "TestBean" explicitly defines one constructor,
 * this constructor must be public and exposed for management through
 * the ModelMBeanInfo object.
 */

public class TestBean 
   implements java.io.Serializable
{

    /*
     * -----------------------------------------------------
     * CONSTRUCTORS
     * -----------------------------------------------------
     */

    public TestBean() {
        echo("\n\tTestBean Constructor Invoked: State " + state + " nbChanges: " + nbChanges + " nbResets: " + nbResets);

    }

    /*
     * -----------------------------------------------------
     * OTHER PUBLIC METHODS
     * -----------------------------------------------------
     */

    /**
     * Getter: get the "State" attribute of the "TestBean" managed resource.
     */
    public String getState() {
        echo("\n\tTestBean: getState invoked: " + state);
        return state;
    }

    /** 
     * Setter: set the "State" attribute of the "TestBean" managed resource.
     */
    public void setState(String s) {
        state = s;
        nbChanges++;
        echo("\n\tTestBean: setState to " + state + " nbChanges: " + nbChanges);
    }

    /**
     * Getter: get the "NbChanges" attribute of the "TestBean" managed resource.
     */
    public Integer getNbChanges() {
        echo("\n\tTestBean: getNbChanges invoked: " + nbChanges);
        return new Integer(nbChanges);
    }

    /**
     * Operation: reset to their initial values the "State" and "NbChanges" 
     * attributes of the "SimpleDynamic" dynamic MBean. 
     */
    public void reset() {
        echo("\n\tTestBean: reset invoked ");
        state = "reset initial state";
        nbChanges = 0;
        nbResets++;
    }

    /**
     * Return the "NbResets" property. 
     * This method is not a Getter in the JMX sense because 
     * it is not returned by the getMBeanInfo() method.
     */
    public Integer getNbResets() {
        echo("\n\tTestBean: getNbResets invoked: " + nbResets);
        return new Integer(nbResets);
    }

    /*
     * -----------------------------------------------------
     * PRIVATE METHODS
     * -----------------------------------------------------
     */
    private void echo(String outstr) {
        System.out.println(outstr);
    }

    /**
    
    /*
     * -----------------------------------------------------
     * PRIVATE VARIABLES
     * -----------------------------------------------------
     */

    private String  state = "initial state";
    private int     nbChanges = 0;
    private int     nbResets = 0;



}
