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
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.SeriesStatistics;
import de.nmichael.pda.util.Util;

public class SeriesStatisticsDialog extends BaseDialog {
    
    // Data
    ProjectItem projectItem;
    Vector<DataSeriesProperties> vprop; // Vector of DataProperties
    
    // Base Layout
    private JFrame parent;
    private JPanel contentPane;
    private BorderLayout contentPaneBorderLayout = new BorderLayout();
    
    // Main Panel
    private JScrollPane mainScrollPane = new JScrollPane();
    private JTextArea mainTextArea = new JTextArea();
    
    // Ok Button
    private JButton okButton = new JButton();

    public SeriesStatisticsDialog(JFrame parent, ProjectItem projectItem, Vector<DataSeriesProperties> vprop) {
        super(parent, "Series Statistics");
        this.parent = parent;
        this.projectItem = projectItem;
        if (vprop == null) {
            vprop = new Vector<DataSeriesProperties>();
            for (int i=0; i<projectItem.getSeriesProperties().size(); i++) {
                vprop.add(projectItem.getSeriesProperties().getDataProperties(i));
            }
        }
        this.vprop = vprop;
    }

    public SeriesStatisticsDialog(JFrame parent, ProjectItem projectItem, DataSeriesProperties prop) {
        super(parent, "Series Statistics");
        this.parent = parent;
        this.projectItem = projectItem;
        vprop = new Vector<DataSeriesProperties>();
        if (prop != null) {
            vprop.add(prop);
        } else {
            for (int i=0; i<projectItem.getSeriesProperties().size(); i++) {
                vprop.add(projectItem.getSeriesProperties().getDataProperties(i));
            }
        }
    }

    @Override
    protected void initialize() {
        mainPanel.setLayout(new BorderLayout());
        mainScrollPane.setMinimumSize(new Dimension(800,500));
        mainScrollPane.setPreferredSize(new Dimension(800,500));
        mainPanel.add(mainScrollPane,BorderLayout.CENTER);
        mainTextArea.setFont(new java.awt.Font("Courier", 1, 14));
        mainScrollPane.getViewport().add(mainTextArea,null);
        
        SeriesStatistics stats = new SeriesStatistics(projectItem, vprop);
        mainTextArea.setText(stats.getStats());
        mainTextArea.setCaretPosition(0);
        mainTextArea.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
    }

}
