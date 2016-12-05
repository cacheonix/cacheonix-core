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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cacheonix.impl.util.logging.Level;
import org.cacheonix.impl.util.logging.Priority;

/**
 * Represents the controls for filtering, pausing, exiting, etc.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
final class ControlPanel extends JPanel {

   private static final long serialVersionUID = -3604271086571353664L;

   /**
    * Creates a new <code>ControlPanel</code> instance.
    *
    * @param aModel the model to control
    */
   ControlPanel(final MyTableModel aModel) {

      setBorder(BorderFactory.createTitledBorder("Controls: "));
      final GridBagLayout gridbag = new GridBagLayout();
      final GridBagConstraints c = new GridBagConstraints();
      setLayout(gridbag);

      // Pad everything
      c.ipadx = 5;
      c.ipady = 5;

      // Add the 1st column of labels
      c.gridx = 0;
      c.anchor = GridBagConstraints.EAST;

      c.gridy = 0;
      JLabel label = new JLabel("Filter Level:");
      gridbag.setConstraints(label, c);
      add(label);

      c.gridy++;
      label = new JLabel("Filter Thread:");
      gridbag.setConstraints(label, c);
      add(label);

      c.gridy++;
      label = new JLabel("Filter Logger:");
      gridbag.setConstraints(label, c);
      add(label);

      c.gridy++;
      label = new JLabel("Filter NDC:");
      gridbag.setConstraints(label, c);
      add(label);

      c.gridy++;
      label = new JLabel("Filter Message:");
      gridbag.setConstraints(label, c);
      add(label);

      // Add the 2nd column of filters
      c.weightx = 1.0;
      //c.weighty = 1;
      c.gridx = 1;
      c.anchor = GridBagConstraints.WEST;

      c.gridy = 0;
      final Level[] allPriorities = {Level.FATAL,
              Level.ERROR,
              Level.WARN,
              Level.INFO,
              Level.DEBUG,
              Level.TRACE};

      final JComboBox priorities = new JComboBox(allPriorities);
      final Level lowest = allPriorities[allPriorities.length - 1];
      priorities.setSelectedItem(lowest);
      aModel.setPriorityFilter(lowest);
      gridbag.setConstraints(priorities, c);
      add(priorities);
      priorities.setEditable(false);
      priorities.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent aEvent) {

            aModel.setPriorityFilter(
                    (Priority) priorities.getSelectedItem());
         }
      });


      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridy++;
      final JTextField threadField = new JTextField("");
      threadField.getDocument().addDocumentListener(new DocumentListener() {

         public void insertUpdate(final DocumentEvent aEvent) {

            aModel.setThreadFilter(threadField.getText());
         }


         public void removeUpdate(final DocumentEvent aEvent) {

            aModel.setThreadFilter(threadField.getText());
         }


         public void changedUpdate(final DocumentEvent aEvent) {

            aModel.setThreadFilter(threadField.getText());
         }
      });
      gridbag.setConstraints(threadField, c);
      add(threadField);

      c.gridy++;
      final JTextField catField = new JTextField("");
      catField.getDocument().addDocumentListener(new DocumentListener() {

         public void insertUpdate(final DocumentEvent aEvent) {

            aModel.setCategoryFilter(catField.getText());
         }


         public void removeUpdate(final DocumentEvent aEvent) {

            aModel.setCategoryFilter(catField.getText());
         }


         public void changedUpdate(final DocumentEvent aEvent) {

            aModel.setCategoryFilter(catField.getText());
         }
      });
      gridbag.setConstraints(catField, c);
      add(catField);

      c.gridy++;
      final JTextField ndcField = new JTextField("");
      ndcField.getDocument().addDocumentListener(new DocumentListener() {

         public void insertUpdate(final DocumentEvent aEvent) {

            aModel.setNDCFilter(ndcField.getText());
         }


         public void removeUpdate(final DocumentEvent aEvent) {

            aModel.setNDCFilter(ndcField.getText());
         }


         public void changedUpdate(final DocumentEvent aEvent) {

            aModel.setNDCFilter(ndcField.getText());
         }
      });
      gridbag.setConstraints(ndcField, c);
      add(ndcField);

      c.gridy++;
      final JTextField msgField = new JTextField("");
      msgField.getDocument().addDocumentListener(new DocumentListener() {

         public void insertUpdate(final DocumentEvent aEvent) {

            aModel.setMessageFilter(msgField.getText());
         }


         public void removeUpdate(final DocumentEvent aEvent) {

            aModel.setMessageFilter(msgField.getText());
         }


         public void changedUpdate(final DocumentEvent aEvent) {

            aModel.setMessageFilter(msgField.getText());
         }
      });


      gridbag.setConstraints(msgField, c);
      add(msgField);

      // Add the 3rd column of buttons
      c.weightx = 0.0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.EAST;
      c.gridx = 2;

      c.gridy = 0;
      final JButton exitButton = new JButton("Exit");
      exitButton.setMnemonic('x');
      exitButton.addActionListener(ExitAction.INSTANCE);
      gridbag.setConstraints(exitButton, c);
      add(exitButton);

      c.gridy++;
      final JButton clearButton = new JButton("Clear");
      clearButton.setMnemonic('c');
      clearButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent aEvent) {

            aModel.clear();
         }
      });
      gridbag.setConstraints(clearButton, c);
      add(clearButton);

      c.gridy++;
      final JButton toggleButton = new JButton("Pause");
      toggleButton.setMnemonic('p');
      toggleButton.addActionListener(new ActionListener() {

         public void actionPerformed(final ActionEvent aEvent) {

            aModel.toggle();
            toggleButton.setText(
                    aModel.isPaused() ? "Resume" : "Pause");
         }
      });
      gridbag.setConstraints(toggleButton, c);
      add(toggleButton);
   }
}
