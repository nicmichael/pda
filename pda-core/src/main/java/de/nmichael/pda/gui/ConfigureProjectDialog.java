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
import java.io.*;
import java.net.URI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import de.nmichael.pda.*;
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.*;

public class ConfigureProjectDialog extends BaseDialog {
    
    // Data
    private ProjectItem projectItem;
    private ColorSelector colorSelector;
    
    // Project Panel
    private JTextField projectName = null;

    // Data Panel
    private JTextField filename = new JTextField();
    private JScrollPane parserScrollPane = new JScrollPane();
    private JPanel parserPanel = null;
    private JScrollPane seriesScrollPane = new JScrollPane();
    private JPanel seriesPanel = null;
    
    // dimensions
    int maxWidth;
    int maxHeight;
    
    
    public ConfigureProjectDialog(JFrame parent, ProjectItem projectItem) {
        super(parent, "Configure Project");
        this.projectItem = projectItem;
        this.colorSelector = new ColorSelector();
        this.maxWidth = parent.getWidth();
        this.maxHeight = parent.getHeight();
    }
    
    private void createProjectPanel() {
        // Project Panel
        JPanel projectPanel = new JPanel();
        projectPanel.setLayout(new BorderLayout());
        projectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "Project"));
        JPanel projectInnerPanel = new JPanel();        
        projectInnerPanel.setLayout(new GridBagLayout());
        projectInnerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        projectPanel.add(projectInnerPanel, BorderLayout.CENTER);
        JLabel projectNameLabel = new JLabel();
        projectName = new JTextField();
        super.setLabel(projectNameLabel, "Title:", 't', projectName);
        projectName.setPreferredSize(new Dimension(700,19));
        projectName.setMinimumSize(new Dimension(600,19));
        projectName.setText(projectItem.getName());
        projectName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(FocusEvent e) {
                projectName_focusLost(e);
            }
        });
        projectInnerPanel.add(projectNameLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        projectInnerPanel.add(projectName,new GridBagConstraints(1, 0, 5, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(projectPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
    }
    
    private void createFilePanel() {
        // File Panel
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "Files"));
        JPanel fileInnerPanel = new JPanel();        
        fileInnerPanel.setLayout(new BorderLayout());
        fileInnerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filePanel.add(fileInnerPanel, BorderLayout.CENTER);
        
        // File Select Panel
        JPanel fileSelectPanel = new JPanel();
        fileSelectPanel.setLayout(new GridBagLayout());
        fileSelectPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel filenameLabel = new JLabel();
        setLabel(filenameLabel, "File: ", 'f', filename);
        fileSelectPanel.add(filenameLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        filename.setPreferredSize(new Dimension(700,19));
        filename.setMinimumSize(new Dimension(600,19));
        fileSelectPanel.add(filename,new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        JButton selectFilenameButton = new JButton();        
        setIcon(selectFilenameButton, "fileopen.png");
        selectFilenameButton.setPreferredSize(new Dimension(40, 20));
        selectFilenameButton.setMargin(new Insets(0, 5, 0, 5));
        selectFilenameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectFilename();
            }
        });
        fileSelectPanel.add(selectFilenameButton,new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        JButton addFilenameButton = new JButton();
        setIcon(addFilenameButton, "apply.png");
        addFilenameButton.setPreferredSize(new Dimension(40, 20));
        addFilenameButton.setMargin(new Insets(0, 5, 0, 5));
        addFilenameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFilename();
            }
        });
        fileSelectPanel.add(addFilenameButton,new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        
        // Parser Panel
        parserScrollPane.setMinimumSize(new Dimension(1000,100));
        parserScrollPane.setPreferredSize(new Dimension(maxWidth-200,100));
        
        
        fileInnerPanel.add(fileSelectPanel, BorderLayout.NORTH);
        fileInnerPanel.add(parserScrollPane, BorderLayout.CENTER);
        mainPanel.add(filePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
    }
    
    private void createSeriesPanel() {
        // File Panel
        JPanel seriesPanel = new JPanel();
        seriesPanel.setLayout(new BorderLayout());
        seriesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.gray), "Series"));
        JPanel seriesInnerPanel = new JPanel();        
        seriesInnerPanel.setLayout(new BorderLayout());
        seriesInnerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        seriesPanel.add(seriesInnerPanel, BorderLayout.CENTER);
        
        // Series Add Panel
        JPanel seriesAddPanel = new JPanel();
        seriesAddPanel.setLayout(new GridBagLayout());
        JButton addSeriesButton = new JButton();
        addSeriesButton.setText("Select Series");
        addSeriesButton.setMnemonic('s');
        setIcon(addSeriesButton, "addseries.png");
        addSeriesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSeries();
            }
        });
        seriesAddPanel.add(addSeriesButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
        seriesInnerPanel.add(seriesAddPanel, BorderLayout.NORTH);
        JButton configureGroupsButton = new JButton();
        configureGroupsButton.setText("Configure Groups");
        configureGroupsButton.setMnemonic('g');
        setIcon(configureGroupsButton, "groupseries.png");
        configureGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureGroups();
            }
        });
        seriesAddPanel.add(configureGroupsButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 0, 0));
        
        // Series Panel
        seriesScrollPane.setMinimumSize(new Dimension(1000,250));
        seriesScrollPane.setPreferredSize(new Dimension(maxWidth-200,maxHeight/2));
        seriesInnerPanel.add(seriesScrollPane, BorderLayout.CENTER);
        mainPanel.add(seriesPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
    }
    
    @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        createProjectPanel();
        createFilePanel();
        createSeriesPanel();
        updateDataPanel();
    }
    
    void updateParserPanel() {
        if (parserPanel != null) {
            synchronized (parserScrollPane) {
                parserScrollPane.remove(parserPanel);
            }
        }
        parserPanel = new JPanel();
        parserPanel.setLayout(new GridBagLayout());
        for (int i = 0; i < projectItem.getParsers().size(); i++) {
            final Parser data = projectItem.getParsers().getParser(i);
            JLabel name = new JLabel();
            name.setText(data.getName());
            name.setForeground(new Color(0, 0, 128));
            parserPanel.add(name, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
            JLabel file = new JLabel();
            String fn = data.getFilename();
            if (fn.length() > 80) {
                fn = fn.substring(0, 20) + "..." + fn.substring(fn.length() - 57);
            }
            file.setText("(" + fn + ")");
            parserPanel.add(file, new GridBagConstraints(1, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            JButton configureParserButton = new JButton();
            configureParserButton.setText("Settings");
            configureParserButton.setPreferredSize(new Dimension(120, 20));
            configureParserButton.setMargin(new Insets(0, 5, 0, 5));
            setIcon(configureParserButton, "configure.png");
            configureParserButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    configureParser(data);
                }
            });
            parserPanel.add(configureParserButton, new GridBagConstraints(2, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));

            JButton deleteParserButton = new JButton();
            deleteParserButton.setPreferredSize(new Dimension(40, 20));
            setIcon(deleteParserButton, "delete.png");
            deleteParserButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteParser(data);
                }
            });
            parserPanel.add(deleteParserButton, new GridBagConstraints(3, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
        }
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        innerPanel.add(parserPanel, BorderLayout.NORTH);
        parserScrollPane.getViewport().setView(innerPanel);
    }
    
    void updateSeriesPanel() {
        if (seriesPanel != null) {
            synchronized (seriesScrollPane) {
                seriesScrollPane.remove(seriesPanel);
            }
        }
        seriesPanel = new JPanel();
        seriesPanel.setLayout(new GridBagLayout());
        DataSeriesPropertySet prop = projectItem.getSeriesProperties();
        for (int i = 0; i < prop.size(); i++) {
            final DataSeriesProperties p = prop.getDataProperties(i);
            DataSeries series = p.getSeries();
            if (series == null || !series.isSelected()) {
                continue;
            }

            JLabel nameLabel = new JLabel();
            nameLabel.setText(p.getName());
            nameLabel.setForeground(p.getColor());
            seriesPanel.add(nameLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            JLabel countLabel = new JLabel();
            countLabel.setText("(" + (p.getSeries() == null ? 
                    "null" : 
                    Integer.toString(p.getSeries().getNumberOfSamples()))
                    + ")");
            countLabel.setForeground(p.getColor());
            seriesPanel.add(countLabel, new GridBagConstraints(1, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

            JLabel minmaxLabel = new JLabel();
            minmaxLabel.setText("[" + (p.getSeries() == null ? 
                    "n/a" : 
                    Util.double2string(p.getScaleMin(), 0, true) + ";" + 
                    Util.double2string(p.getScaleMax(), 0, true))
                    + "]");
            minmaxLabel.setForeground(p.getColor());
            seriesPanel.add(minmaxLabel, new GridBagConstraints(2, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

            JButton confButton = new JButton();
            confButton.setText("Settings");
            confButton.setPreferredSize(new Dimension(120, 20));
            confButton.setMargin(new Insets(0, 5, 0, 5));
            setIcon(confButton, "configure.png");
            confButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    configureSeries(p);
                }
            });
            seriesPanel.add(confButton, new GridBagConstraints(3, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

            JButton removeButton = new JButton();
            removeButton.setPreferredSize(new Dimension(40, 20));
            setIcon(removeButton, "delete.png");
            removeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    removeSeries(p);
                }
            });
            seriesPanel.add(removeButton, new GridBagConstraints(4, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

            JButton moveUpButton = new JButton();
            moveUpButton.setPreferredSize(new Dimension(40, 20));
            moveUpButton.setMargin(new Insets(0, 0, 0, 0));
            setIcon(moveUpButton, "raise.png");
            moveUpButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveUp(p);
                }
            });
            seriesPanel.add(moveUpButton, new GridBagConstraints(5, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

            JButton moveDownButton = new JButton();
            moveDownButton.setPreferredSize(new Dimension(40, 20));
            moveDownButton.setMargin(new Insets(0, 0, 0, 0));
            setIcon(moveDownButton, "lower.png");
            moveDownButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveDown(p);
                }
            });
            seriesPanel.add(moveDownButton, new GridBagConstraints(6, i, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        }
        seriesScrollPane.getViewport().setView(seriesPanel);
    }
    
    void updateDataPanel() {
        try {
            updateParserPanel();
            updateSeriesPanel();
        } catch(Exception e) {
            errorDlg(this, e.getMessage());
            e.printStackTrace();
        }
    }
    
    void selectFilename() {
        String fname = BaseDialog.openFileDlg(this, "Select Data File", null, null, Main.config.getWorkdirData());
        if (fname != null) {
            Main.config.setWorkdirData(Util.getPathOfFile(fname));
            filename.setText(fname);
            addFilename();
        }
    }
    
    void addFilename() {
        String f = filename.getText().trim();
        if (f.length() == 0) return;
        String filesep = System.getProperty("file.separator");
        
        Vector<Parser> allParsers = new Vector<Parser>();
        int preferredParser = -1;
        for (int i=0; i<Parsers.getNumberOfParsers(); i++) {
            try {
                Parser dataSet = (Parser)Class.forName(Parsers.getParserName(i)).newInstance();
                allParsers.add(dataSet);
                if (dataSet.canHandle(f) && preferredParser == -1) {
                    preferredParser = allParsers.size() - 1;
                }
            } catch(Exception ee) {
                errorDlg(this, ee.getMessage());
                ee.printStackTrace();
            }
        }
        if (allParsers.size() == 0) {
            BaseDialog.errorDlg(this, "No Parsers found");
            return;
        }
        
        SelectParserDialog dlg = new SelectParserDialog(this, f, allParsers, preferredParser);
        dlg.showDialog();
        Parser dataSet = dlg.getSelectedParser();
        if (dataSet != null) {
            dataSet.setFilename(f);
            dataSet.getAllSeriesNames(true);
            projectItem.getParsers().addParser(dataSet);
            updateDataPanel();
        }
    }
    
    void configureParser(Parser parser) {
        ConfigureParserDialog dlg = new ConfigureParserDialog(this,parser);
        dlg.showDialog();
        updateDataPanel();
    }
    
    void deleteParser(Parser p) {
        if (BaseDialog.yesNoDialog(this, 
                "Are you sure", 
                "Do you really want to remove the parser and all its series?") != BaseDialog.YES) {
            return;
        }
        projectItem.removeParser(p);
        updateDataPanel();
    }
    
    void addSeries() {
        if (projectItem.getParsers().size() == 0) {
            selectFilename();
            if (projectItem.getParsers().size() == 0) {
                return;
            }
        }
        SelectSeriesDialog dlg = new SelectSeriesDialog(this, projectItem);
        dlg.showDialog();
        updateDataPanel();
    }
        
    void configureSeries(DataSeriesProperties p) {
        SeriesSettingsDialog dlg = new SeriesSettingsDialog(this, p);
        dlg.showDialog();
        updateDataPanel();
    }
    
    void removeSeries(DataSeriesProperties p) {
        if (BaseDialog.yesNoDialog(this, "Delete Series", 
                "Do you really want to delete the series\n" +
                p.getName() + "?") == BaseDialog.YES) {
            projectItem.getSeriesProperties().removeDataProperties(p);
            updateDataPanel();
        }
    }
    
    void configureGroups() {
        ConfigureGroupsDialog dlg = new ConfigureGroupsDialog(this, projectItem);
        dlg.showDialog();
        updateDataPanel();
    }
    
    void projectName_focusLost(FocusEvent e) {
        projectItem.setName(projectName.getText().trim());
    }
    
    void moveUp(DataSeriesProperties p) {
        DataSeriesPropertySet prop = projectItem.getSeriesProperties();
        prop.moveUp(p);
        updateDataPanel();
    }

    void moveDown(DataSeriesProperties p) {
        DataSeriesPropertySet prop = projectItem.getSeriesProperties();
        prop.moveDown(p);
        updateDataPanel();        
    }

    
}
