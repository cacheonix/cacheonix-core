/*
 * @(#)file      TestBeanFriend.java
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
 * Simple definition of a ModelMBean managed application Bean, named "TestBeanFriend".
 *
 * The "TestBeanFriend" implements the same methods as "TestBean".  It is used by 
 * the ModelAgent test program to illustrate and demonstrate how to have attributes
 * in the same ModelMBean supported by methods on different objects.
 *
 * This Bean exposes for management two attributes and one operation:
 *      - the read/write "State" attribute,
 *      - the read only "NbChanges" attribute,
 *      - the "reset()" operation.
 * 
 * Note that as "TestBeanFriend" explicitly defines one constructor.
 */

public class TestBeanFriend 
   implements java.io.Serializable
{

    /*
     * -----------------------------------------------------
     * CONSTRUCTORS
     * -----------------------------------------------------
     */

    public TestBeanFriend() {
        echo("\n\tTestBeanFriend Constructor invoked: State " + state + " nbChanges: " + nbChanges + " nbResets: " + nbResets);
    }

    /*
     * -----------------------------------------------------
     * OTHER PUBLIC METHODS
     * -----------------------------------------------------
     */

    /**
     * Getter: get the "State" attribute of the "TestBeanFriend" managed resource.
     */
    public String getState() {
        echo("\n\tTestBeanFriend: getState invoked:" + state);
        return state;
    }

    /** 
     * Setter: set the "State" attribute of the "TestBeanFriend" managed resourc.
     */
    public void setState(String s) {
        state = s;
        nbChanges++;
        echo("\n\tTestBeanFriend: setState invoked: to " + state + " nbChanges: " + nbChanges);
    }

    /**
     * Getter: get the "NbChanges" attribute of the "TestBeanFriend" managed resource.
     */
    public Integer getNbChanges() {
        echo("\n\tTestBeanFriend:getNbChanges invoked: " + nbChanges);
        return new Integer(nbChanges);
    }

    /**
     * Operation: reset to their initial values the "State" and "NbChanges" 
     * attributes of the "TestBeanFriend" managed resource. 
     */
    public void reset() {
        echo("\n\tTestBeanFriend: reset invoked ");
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
        echo("\n\tTestBeanFriend: getNbResets invoked " + nbResets);
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
