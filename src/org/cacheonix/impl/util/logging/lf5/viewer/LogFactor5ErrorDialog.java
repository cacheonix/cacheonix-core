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
package org.cacheonix.impl.util.logging.lf5.viewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * LogFactor5ErrorDialog
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

@SuppressWarnings("deprecation")
public final class LogFactor5ErrorDialog extends LogFactor5Dialog {

   private static final long serialVersionUID = 3287071157067565556L;

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   public LogFactor5ErrorDialog(final JFrame jframe, final String message) {
      super(jframe, "Error", true);

      final JButton ok = new JButton("Ok");
      ok.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            hide();
         }
      });

      final JPanel bottom = new JPanel();
      bottom.setLayout(new FlowLayout());
      bottom.add(ok);

      final JPanel main = new JPanel();
      main.setLayout(new GridBagLayout());
      wrapStringOnPanel(message, main);

      getContentPane().add(main, BorderLayout.CENTER);
      getContentPane().add(bottom, BorderLayout.SOUTH);
      show();

   }
   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces
   //--------------------------------------------------------------------------
}