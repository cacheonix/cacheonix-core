/*
 * @(#)file      TestBeanAttributeChangeListener.java
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

import javax.management.NotificationListener;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

/**
* TestBeanAttributeChangeListener implements the NotificationListener interface.
* Listens for and recieves AttributeChangeNotifications from ModelMBean for TestBean managed resource.
* Notification information is echoed to the java console	
*
*/


public class TestBeanAttributeChangeListener implements NotificationListener {
	public void handleNotification(Notification acn, Object handback) {
		echo("\n\tTestBeanAttributeChangeListener received Attribute ChangeNotification ");
		AttributeChangeNotification myacn = (AttributeChangeNotification) acn;
		echo("\t\tEvent: " + acn.getType());
		echo("\t\tAttribute: " + myacn.getAttributeName());
		echo("\t\tAttribute type: " + myacn.getAttributeType());
		echo("\t\tOld value: " + myacn.getOldValue());
		echo("\t\tNew value: " + myacn.getNewValue());

	}

	private static void echo(String outstr) {
		System.out.println(outstr);
	}
}
