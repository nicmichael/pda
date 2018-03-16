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

import de.nmichael.pda.Main;
import de.nmichael.pda.util.Util;
import gov.noaa.pmel.sgt.SGLabel;
import java.awt.*;
import javax.swing.*;

public class SettingsDialog extends BaseDialog {
    
    // Base Layout
    private JFrame parent;
    private JTextField labelSize;
    
    // Ok Button
    private JButton okButton = new JButton();

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings");
    }

    @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        
        JLabel textSizeLabel = new JLabel();
        labelSize = new JTextField();
        labelSize.setText(Util.double2string(Main.config.getLabelHeight(), 2, false).trim());
        setLabel(textSizeLabel, "Label Size:", 's', labelSize);
        mainPanel.add(textSizeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        labelSize.setPreferredSize(new Dimension(100, 19));
        mainPanel.add(labelSize, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            Main.config.setLabelHeight(Util.string2float(labelSize.getText().trim(), 0.2f));
        }
        super.closeWindow(okButton);
    }
    
}
