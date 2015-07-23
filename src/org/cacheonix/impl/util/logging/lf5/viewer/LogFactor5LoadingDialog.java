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
import javax.swing.*;

/**
 * LogFactor5LoadingDialog
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public final class LogFactor5LoadingDialog extends LogFactor5Dialog {

   private static final long serialVersionUID = -2219832715671146198L;

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


   public LogFactor5LoadingDialog(final JFrame jframe, final String message) {
      super(jframe, "LogFactor5", false);

      final JPanel bottom = new JPanel();
      bottom.setLayout(new FlowLayout());

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