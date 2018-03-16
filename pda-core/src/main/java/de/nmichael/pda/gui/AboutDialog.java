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
import java.awt.event.*;
import javax.swing.*;
import de.nmichael.pda.*;

public class AboutDialog extends BaseDialog {
    
    /** Creates a new instance of AboutFrame */
    public AboutDialog(JFrame parent) {
        super(parent, Main.PROGRAM + " - " + Main.VERSION, BaseDialog.CloseButton.close);
    }

    @Override
    protected void initialize() {
        mainPanel.setLayout(new BorderLayout());
        
        JLabel iconLabel = new JLabel();
        setIcon(iconLabel, "pda128.png");
                
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        JLabel programLabel = new JLabel();
        JLabel versionLabel = new JLabel();
        JLabel copyrightLabel = new JLabel();
        JLabel licenseLabel = new JLabel();
        JLabel contactLabel = new JLabel();
        JLabel homepageLabel = new JLabel();
        JLabel emailLabel = new JLabel();
        JLabel sgtLabel = new JLabel();
        JLabel sgtHomepageLabel = new JLabel();
        programLabel.setText(Main.PROGRAM + " (" + Main.PROGRAMSHORT + ")");
        versionLabel.setText(Main.VERSION);
        copyrightLabel.setText(Main.COPYRIGHT);
        licenseLabel.setText("Licensed under the " + Main.LICENSE);
        contactLabel.setText("Homepage:");
        setLink(homepageLabel, Main.HOMEPAGE);
        emailLabel.setText(Main.EMAIL);
        sgtLabel.setText("based on the Scientific Graphics Toolkit (SGT)");
        setLink(sgtHomepageLabel, "http://www.epic.noaa.gov/java/sgt");
        infoPanel.add(programLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 0, 20), 0, 0));
        infoPanel.add(versionLabel,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 20), 0, 0));
        infoPanel.add(copyrightLabel,new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 20, 0, 20), 0, 0));
        infoPanel.add(licenseLabel,new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 20), 0, 0));
        infoPanel.add(contactLabel,new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 0, 20), 0, 0));
        infoPanel.add(homepageLabel,new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 20), 0, 0));
        infoPanel.add(emailLabel,new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 0, 20), 0, 0));
        infoPanel.add(sgtLabel,new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 0, 20), 0, 0));
        infoPanel.add(sgtHomepageLabel,new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 20, 20, 20), 0, 0));
        
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
    }
    
}
