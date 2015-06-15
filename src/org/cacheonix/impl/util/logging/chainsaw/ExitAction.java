/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cacheonix.impl.util.logging.chainsaw;

import java.awt.event.ActionEvent;
import javax.swing.*;

import org.cacheonix.impl.util.logging.Logger;

/**
 * Encapsulates the action to exit.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 * @version 1.0
 */
final class ExitAction extends AbstractAction {

   /**
    * use to log messages *
    */
   private static final Logger LOG = Logger.getLogger(ExitAction.class);
   private static final long serialVersionUID = 0L;

   /**
    * The instance to share *
    */
   public static final ExitAction INSTANCE = new ExitAction();


   /**
    * Stop people creating instances *
    */
   private ExitAction() {
   }


   /**
    * Will shutdown the application.
    *
    * @param aIgnore ignored
    */
   public final void actionPerformed(final ActionEvent aIgnore) {
      LOG.info("shutting down");
      System.exit(0);
   }
}
