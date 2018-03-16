/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.gui;

import java.util.*;
import java.awt.*;
import java.beans.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import de.nmichael.pda.data.*;

public class ConfigureParserDialog extends BaseDialog {
    
    // Data
    private Parser parser;
    private String[] parameters;
    private Hashtable parameterFields = new Hashtable();;
    
    /** Creates a new instance of ConfigureParserFrame */
    public ConfigureParserDialog(JDialog parent, Parser parser) {
        super(parent, "Configure Parser");
        this.parent = parent;
        this.parser = parser;
        this.parameters = parser.getParameterNames();
    }
    
   @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        iniData();
    }
    
    void iniData() {
        for (int i=0; i<parameters.length; i++) {
            JLabel label = new JLabel();
            label.setText(parameters[i]+": ");
            JTextField field = new JTextField();
            field.setText(parser.getParameter(parameters[i]));
            field.setPreferredSize(new Dimension(200,19));
            mainPanel.add(label,new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
            mainPanel.add(field,new GridBagConstraints(1, i, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
            parameterFields.put(parameters[i],field);
        }
    }
        
      
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            for (int i = 0; i < parameters.length; i++) {
                JTextField field = (JTextField) this.parameterFields.get(parameters[i]);
                parser.setParameter(parameters[i], field.getText().trim());
            }
            parser.parse(true, true);
        }
        super.closeWindow(okButton);
    }
    
}