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
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.*;
import java.util.regex.Pattern;

public class SelectSeriesDialog extends BaseDialog {
    
    // Data
    private ProjectItem projectItem;
    private ParserSet parserSet;
    private DataSeriesPropertySet propertySet;
    private ColorSelector colorSelector;
    private DataSeries[] series;
    private ArrayList<String> available;
    private ArrayList<String> selected;
    private Pattern filterPattern;
    private Hashtable<Parser,Boolean> scannedParsers = new Hashtable<Parser,Boolean>();
    
    // Main Panel
    private JList availableSeries;
    private JList selectedSeries;
    private JTextField filter;
    private JCheckBox onlySeriesWithData;
    private JButton selectButton;
    private JButton unselectButton;
    
    public SelectSeriesDialog(Window parent, ProjectItem projectItem) {
        super(parent, "Select Series");
        this.projectItem = projectItem;
        this.parserSet = projectItem.getParsers();
        this.propertySet = projectItem.getSeriesProperties();
        this.colorSelector = projectItem.getColorSelector();
        series = projectItem.getAllSeries(true);
    }
    
    @Override
    protected void initialize() {
        mainPanel.setLayout(new BorderLayout());

        JPanel scollAvailablePanel = new JPanel();
        scollAvailablePanel.setLayout(new BorderLayout());
        JLabel scollAvailableLabel = new JLabel("available Series:");
        scollAvailableLabel.setDisplayedMnemonic('a');
        scollAvailablePanel.add(scollAvailableLabel, BorderLayout.NORTH);
        JScrollPane scollAvailable = new JScrollPane();
        scollAvailable.setPreferredSize(new Dimension(450, 400));
        scollAvailablePanel.setMinimumSize(new Dimension(200, 200));
        availableSeries = new JList();
        availableSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectSeries();
                };
            }});
        scollAvailableLabel.setLabelFor(availableSeries);
        scollAvailable.getViewport().add(availableSeries, null);
        scollAvailablePanel.add(scollAvailable, BorderLayout.CENTER);
        mainPanel.add(scollAvailablePanel, BorderLayout.WEST);

        JPanel scollSelectedPanel = new JPanel();
        scollSelectedPanel.setLayout(new BorderLayout());
        JLabel scollSelectedLabel = new JLabel("selected Series:");
        scollAvailableLabel.setDisplayedMnemonic('s');
        scollSelectedPanel.add(scollSelectedLabel, BorderLayout.NORTH);
        JScrollPane scollSelected = new JScrollPane();
        scollSelected.setPreferredSize(new Dimension(450, 400));
        scollSelectedPanel.setMinimumSize(new Dimension(200, 200));
        selectedSeries = new JList();
        selectedSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    unselectSeries();
                };
            }});
        scollSelectedLabel.setLabelFor(selectedSeries);
        scollSelected.getViewport().add(selectedSeries, null);
        scollSelectedPanel.add(scollSelected, BorderLayout.CENTER);
        mainPanel.add(scollSelectedPanel, BorderLayout.EAST);

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new GridBagLayout());
        selectPanel.setMinimumSize(new Dimension(100, 200));
        selectButton = new JButton();
        setIcon(selectButton, "arrowright.png");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectSeries();
            }
        });
        selectPanel.add(selectButton,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        unselectButton = new JButton();
        setIcon(unselectButton, "arrowleft.png");
        unselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unselectSeries();
            }
        });
        selectPanel.add(unselectButton,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        mainPanel.add(selectPanel, BorderLayout.CENTER);

        // Filter Panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        JLabel filterLabel = new JLabel();
        filter = new JTextField();
        setLabel(filterLabel, "Filter:", 'f', filter);
        filter.setPreferredSize(new Dimension(400, 19));
        filter.setMinimumSize(new Dimension(200, 19));
        filter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filter();
            }
        });
        filterPanel.add(filterLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
        filterPanel.add(filter,new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
        
        onlySeriesWithData = new JCheckBox();
        onlySeriesWithData.setText("Only show series with non-zero samples");
        onlySeriesWithData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateLists();
            }
        });
        filterPanel.add(onlySeriesWithData,new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        JButton configureSettingsButton = new JButton();
        configureSettingsButton.setText("Configure Series Properties");
        configureSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureSeriesProperties();
            }
        });
        filterPanel.add(configureSettingsButton,new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
        
        mainPanel.add(filterPanel, BorderLayout.SOUTH);
        
        updateLists();
        setRequestFocus(filter);
    }

    private void addFilterField(JPanel panel, JTextField field, String text, int y) {
        JLabel label = new JLabel(text + ":");
        label.setDisplayedMnemonic(text.charAt(0));
        label.setLabelFor(field);
    }
    
    synchronized void updateLists() {
        available = new ArrayList<String>();
        selected = new ArrayList<String>();
        boolean onlyWithData = onlySeriesWithData.isSelected();
        boolean parsed = false;
    
        for (DataSeries s : series) {
            
            // if we only want to see series that have non-zero samples ...
            if (onlyWithData && !s.hasNonZeroSamples()) {
                if (s.hasBeenScanned()) {
                    // series has been scanned but doesn't have samples --> skip
                    continue;                    
                } else {
                    // series hasn't been scanned yet --> parse now
                    Parser p = s.getParser(); 
                    if (scannedParsers.get(p) == null) {
                        p.parse(true, true);
                        parsed = true;
                        scannedParsers.put(p, true);
                    }
                    if (!s.hasNonZeroSamples()) {
                        // series still has no samples --> skip
                        continue;
                    }
                }
            }
            
            if (filterPattern != null) {
                if (!filterPattern.matcher(s.getName()).matches()) {
                    continue;
                }
            }
            if (s.isSelected()) {
                selected.add(s.getName());
            } else {
                available.add(s.getName());
            }
        }

        availableSeries.setListData(available.toArray(new String[0]));
        selectedSeries.setListData(selected.toArray(new String[0]));
        
        if (parsed) {
            series = projectItem.getAllSeries(true);
            updateLists(); // do it again, this time with our newly parsed series
        }
    }

    private void filter() {
        String s = filter.getText().trim();
        Pattern newFilter = null;
        if (s.length() == 0) {
            newFilter = null;
        } else {
            if (!s.startsWith("^")) {
                s = ".*" + s;
            }
            if (!s.endsWith("$")) {
                s = s + ".*";
            }
            try {
                newFilter = Pattern.compile(s);
            } catch(Exception eingore) {
            }
        }
        if ( (newFilter != null && filterPattern == null) ||
             (newFilter == null && filterPattern != null) ||
             (newFilter != null && filterPattern != null && !newFilter.toString().equals(filterPattern.toString())) ) {
            filterPattern = newFilter;
            updateLists();
        }
    }

    synchronized void selectSeries() {
        int[] idx = availableSeries.getSelectedIndices();
        for (int i=0; idx != null && i<idx.length; i++) {
            try {
                String s = available.get(idx[i]);
                DataSeries ser = projectItem.getSeries(s);
                ser.setSelected(true);
            } catch(Exception eignore) {
            }
        }
        updateLists();
    }

    void unselectSeries() {
        int[] idx = selectedSeries.getSelectedIndices();
        for (int i=0; idx != null && i<idx.length; i++) {
            try {
                String s = selected.get(idx[i]);
                DataSeries ser = projectItem.getSeries(s);
                ser.setSelected(false);
            } catch(Exception eignore) {
            }
        }
        updateLists();
    }
    
    void configureSeriesProperties() {
        int[] idx = selectedSeries.getSelectedIndices();
        if (idx == null || idx.length == 0) {
            BaseDialog.errorDlg(this, "No series (on right-hand side) selected.");
            return;
        }
        DataSeriesProperties[] props = new DataSeriesProperties[idx.length]; 
        for (int i=0; idx != null && i<idx.length; i++) {
            try {
                String s = selected.get(idx[i]);
                DataSeries ser = projectItem.getSeries(s);
                if (ser.getDataProperties() == null) {
                    if (!ser.findDataProperties(propertySet)) {
                        ser.createDataProperties(propertySet, colorSelector);
                    }
                }
                props[i] = ser.getDataProperties();
            } catch(Exception eignore) {
            }
        }
        DataSeriesProperties newProps = new DataSeriesProperties(null); 
        SeriesSettingsDialog settingsDlg = new SeriesSettingsDialog(this, newProps);
        if (settingsDlg.showDialog()) {
            for (DataSeriesProperties prop : props) {
                if (prop == null) {
                    continue;
                }
                if (settingsDlg.isChangedMin()) {
                    prop.setScaleMin(newProps.getScaleMin());
                }
                if (settingsDlg.isChangedMax()) {
                    prop.setScaleMax(newProps.getScaleMax());
                }
                if (settingsDlg.isChangedColor()) {
                    prop.setColor(newProps.getColor());
                }
                if (settingsDlg.isChangedLineStyle() && newProps.getLineStyle() != -1) {
                    prop.setLineStyle(newProps.getLineStyle());
                }
                if (settingsDlg.isChangedLineWidth() && newProps.getLineWidth() != -1) {
                    prop.setLineWidth(newProps.getLineWidth());
                }
                if (settingsDlg.isChangedStyle() && newProps.getStyle() != -1) {
                    prop.setStyle(newProps.getStyle());
                }
                if (settingsDlg.isChangedSmooth()) {
                    prop.setSmooth(newProps.getSmooth());
                }
                if (settingsDlg.isChangedValueAxis()) {
                    prop.setValueAxis(newProps.getValueAxis());
                }
            } 
        }
        updateLists();
    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            Hashtable<Parser, String> needParsing = new Hashtable<Parser, String>();
            for (DataSeries s : series) {
                if (s.isUsed()) {
                    if (s.isSelected() && s.getDataProperties() == null) {
                        if (!s.findDataProperties(propertySet)) {
                            s.createDataProperties(propertySet, colorSelector);
                        }
                    }
                    if (!s.isParsed()) {
                        ArrayList<Parser> dirtyParsers = s.getDirtyParsers();
                        for (int i=0; dirtyParsers != null && i<dirtyParsers.size(); i++) {
                            needParsing.put(dirtyParsers.get(i), "foo");
                        }
                    }
                } else {
                    propertySet.removeDataProperties(s.getDataProperties());
                    s.setDataProperties(null);
                }
            }
            for (int i=0; i<propertySet.size(); i++) {
                DataSeriesProperties p = propertySet.getDataProperties(i);
                DataSeries series = p.getSeries();
                if (series == null || !series.isSelected()) {
                    propertySet.removeDataProperties(p);
                    i--;
                    if (series != null) {
                        series.setDataProperties(null);
                    }
                }
            }
            for (Parser p : needParsing.keySet()) {
                p.parse(false, true);
            }
            projectItem.getGroups().parseAllUpdate();
        }
        super.closeWindow(okButton);
    }

    
}
