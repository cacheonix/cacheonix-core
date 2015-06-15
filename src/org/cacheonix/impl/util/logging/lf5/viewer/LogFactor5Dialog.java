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
 * LogFactor5Dialog
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public abstract class LogFactor5Dialog extends JDialog {

   //--------------------------------------------------------------------------
   //   Constants:
   //--------------------------------------------------------------------------
   protected static final Font DISPLAY_FONT = new Font("Arial", Font.BOLD, 12);
   //--------------------------------------------------------------------------
   //   Protected Variables:
   //--------------------------------------------------------------------------

   //--------------------------------------------------------------------------
   //   Private Variables:
   //--------------------------------------------------------------------------


   //--------------------------------------------------------------------------
   //   Constructors:
   //--------------------------------------------------------------------------


   protected LogFactor5Dialog(final JFrame jframe, final String message, final boolean modal) {
      super(jframe, message, modal);
   }


   //--------------------------------------------------------------------------
   //   Public Methods:
   //--------------------------------------------------------------------------
   public final void show() {
      pack();
      minimumSizeDialog(this, 200, 100);
      centerWindow(this);
      super.show();
   }

   //--------------------------------------------------------------------------
   //   Protected Methods:
   //--------------------------------------------------------------------------


   //--------------------------------------------------------------------------
   //   Private Methods:
   //--------------------------------------------------------------------------


   protected final void centerWindow(final Window win) {
      final Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

      // If larger than screen, reduce window width or height
      if (screenDim.width < win.getSize().width) {
         win.setSize(screenDim.width, win.getSize().height);
      }

      if (screenDim.height < win.getSize().height) {
         win.setSize(win.getSize().width, screenDim.height);
      }

      // Center Frame, Dialogue or Window on screen
      final int x = screenDim.width - win.getSize().width >> 1;
      final int y = screenDim.height - win.getSize().height >> 1;
      win.setLocation(x, y);
   }


   protected final void wrapStringOnPanel(String message,
                                          final Container container) {
      final GridBagConstraints c = getDefaultConstraints();
      c.gridwidth = GridBagConstraints.REMAINDER;
      // Insets() args are top, left, bottom, right
      c.insets = new Insets(0, 0, 0, 0);
      final GridBagLayout gbLayout = (GridBagLayout) container.getLayout();


      while (message.length() > 0) {
         final int newLineIndex = message.indexOf((int) '\n');
         final String line;
         if (newLineIndex >= 0) {
            line = message.substring(0, newLineIndex);
            message = message.substring(newLineIndex + 1);
         } else {
            line = message;
            message = "";
         }
         final Label label = new Label(line);
         label.setFont(DISPLAY_FONT);
         gbLayout.setConstraints(label, c);
         container.add(label);
      }
   }


   protected final GridBagConstraints getDefaultConstraints() {
      final GridBagConstraints constraints = new GridBagConstraints();
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.gridheight = 1; // One row high
      // Insets() args are top, left, bottom, right
      constraints.insets = new Insets(4, 4, 4, 4);
      // fill of NONE means do not change size
      constraints.fill = GridBagConstraints.NONE;
      // WEST means align left
      constraints.anchor = GridBagConstraints.WEST;

      return constraints;
   }


   protected final void minimumSizeDialog(final Component component,
                                          final int minWidth,
                                          final int minHeight) {
      // set the min width
      if (component.getSize().width < minWidth) {
         component.setSize(minWidth, component.getSize().height);
      }
      // set the min height
      if (component.getSize().height < minHeight) {
         component.setSize(component.getSize().width, minHeight);
      }
   }
   //--------------------------------------------------------------------------
   //   Nested Top-Level Classes or Interfaces
   //--------------------------------------------------------------------------
}