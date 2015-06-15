/*
 * @(#)file      AgentListener.java
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

// java import
//
import java.util.Vector;

// RI imports
//
import javax.management.*;
import javax.management.monitor.*;

/**
 * @version     4.9     07/15/03
 * @author      Sun Microsystems, Inc
 */

public class AgentListener implements NotificationListener {

    /*
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    
    public AgentListener() {
        super();
    }

    /*
     * ------------------------------------------
     *  PUBLIC METHODS
     * ------------------------------------------
     */
    
    public void handleNotification(Notification notification, Object handback) {
        
        MonitorNotification notif = (MonitorNotification)notification;
        
        // Get a handle on the CounterMonitor responsible for the notification emmited.
        //
        Monitor monitor = (Monitor)notif.getSource();
    
        // Process the different types of notifications fired by the CounterMonitor.
        //
        String type = notif.getType();
        
        try {
            if (type.equals(MonitorNotification.OBSERVED_OBJECT_ERROR)) {
                System.out.println("\n\t>> " + notif.getObservedObject().getClass().getName() + " is not registered in the server");
                System.out.println("\t>> Stopping the CounterMonitor...\n");
                monitor.stop();
            }                
            else if (type.equals(MonitorNotification.OBSERVED_ATTRIBUTE_ERROR)) {
                System.out.println("\n\t>> " + notif.getObservedAttribute() + " attribute is not contained in " + 
                                   notif.getObservedObject().getClass().getName());
                System.out.println("\t>> Stopping the CounterMonitor...\n");
                monitor.stop();
            }                
            else if (type.equals(MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR)) {
                System.out.println("\n\t>> The type of " + notif.getObservedAttribute() + " attribute is not correct");
                System.out.println("\t>> Stopping the CounterMonitor...\n");
                monitor.stop();
                
            }
            else if (type.equals(MonitorNotification.THRESHOLD_ERROR)) {
                System.out.println("\n\t>> Threshold type is not <Integer>");     
                System.out.println("\t>> Stopping the CounterMonitor...\n");
                monitor.stop();
            }                
            else if (type.equals(MonitorNotification.RUNTIME_ERROR)) {
                System.out.println("\n\t>> Runtime error (?)"); 
                System.out.println("\t>> Stopping the CounterMonitor...\n");
                monitor.stop();
            }                
            else if (type.equals(MonitorNotification.THRESHOLD_VALUE_EXCEEDED)) {
                System.out.println("\n\t>> " + notif.getObservedAttribute() + " has reached the threshold\n");
            }                
            else {
                System.out.println("\n\t>> Unknown event type (?)\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
