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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.*;
import gov.noaa.pmel.sgt.SGLabel;
import java.beans.PropertyChangeEvent;

public class SeriesSettingsDialog extends BaseDialog {
    
    // Data
    DataSeriesProperties prop;
    int category;
    int series;
    GraphPanel graphPanel;
    boolean addLabelButton = false;
    
    // Main Panel
    private JTextField name = new JTextField();
    private JTextField displayname = new JTextField();
    private JTextField rangeYMin = new JTextField();
    private JTextField rangeYMax = new JTextField();
    private JTextField color = new JTextField();
    private JButton colorChooser = new JButton();
    private JComboBox style = new JComboBox();
    private JTextField lineWidth = new JTextField();
    private JComboBox lineStyle = new JComboBox();
    private JComboBox valueAxis = new JComboBox();
    private JTextField smooth = new JTextField();
    
    // changed values
    private boolean changedMin = false;
    private boolean changedMax = false;
    private boolean changedColor = false;
    private boolean changedStyle = false;
    private boolean changedLineStyle = false;
    private boolean changedLineWidth = false;
    private boolean changedSmooth = false;
    private boolean changedValueAxis = false;
    
    public SeriesSettingsDialog(JDialog parent, DataSeriesProperties prop) {
        super(parent, "Series Settings");
        this.prop = prop;
    }
    
    public SeriesSettingsDialog(JFrame parent, GraphPanel graphPanel, DataSeriesProperties prop) {
        super(parent, "Series Settings");
        this.prop = prop;
        this.graphPanel = graphPanel;
        addLabelButton = true;
    }
    
    @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        
        JLabel nameLabel = new JLabel();
        setLabel(nameLabel, "Series Name:", 'n', name);
        mainPanel.add(nameLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        name.setPreferredSize(new Dimension(500,19));
        name.setEditable(false);
        mainPanel.add(name,new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel displayNameLabel = new JLabel();
        setLabel(displayNameLabel, "Display Name:", 'd', displayname);
        mainPanel.add(displayNameLabel,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        displayname.setPreferredSize(new Dimension(500,19));
        mainPanel.add(displayname,new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel rangeYMinLabel = new JLabel();
        setLabel(rangeYMinLabel, "Range Y from:", 'f', rangeYMin);
        mainPanel.add(rangeYMinLabel,new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        rangeYMin.setPreferredSize(new Dimension(200,19));
        mainPanel.add(rangeYMin,new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel rangeYMaxLabel = new JLabel();
        setLabel(rangeYMaxLabel, "Range Y to:", 't', rangeYMax);
        mainPanel.add(rangeYMaxLabel,new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        rangeYMax.setPreferredSize(new Dimension(200,19));
        mainPanel.add(rangeYMax,new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel colorLabel = new JLabel();
        setLabel(colorLabel, "Color:", 'c', color);
        mainPanel.add(colorLabel,new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        color.setPreferredSize(new Dimension(200,19));
        mainPanel.add(color,new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        setIcon(colorChooser, "colorchooser.png");
        colorChooser.setMargin(new Insets(0, 5, 0, 5));
        colorChooser.setPreferredSize(new Dimension(40, 20));
        colorChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choseColor();
            }
        });
        mainPanel.add(colorChooser,new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        
        
        JLabel styleLabel = new JLabel();
        setLabel(styleLabel, "Style:", 'y', style);
        mainPanel.add(styleLabel,new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        style.setPreferredSize(new Dimension(200,19));
        mainPanel.add(style,new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel lineWidthLabel = new JLabel();
        setLabel(lineWidthLabel, "Line Width:", 'w', lineWidth);
        mainPanel.add(lineWidthLabel,new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        lineWidth.setPreferredSize(new Dimension(200,19));
        mainPanel.add(lineWidth,new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        lineWidth.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(FocusEvent e) {
                int width = Util.string2int(lineWidth.getText(), 2);
                int style = lineStyle.getSelectedIndex();
                if (width > 1 && (style == DataSeriesProperties.LINESTYLE_SOLID || style == DataSeriesProperties.LINESTYLE_DASHED)) {
                    lineStyle.setSelectedIndex(DataSeriesProperties.LINESTYLE_STRONG);
                }
                if (width < 2 && style == DataSeriesProperties.LINESTYLE_STRONG) {
                    lineStyle.setSelectedIndex(DataSeriesProperties.LINESTYLE_SOLID);
                }
            }
        });
        
        JLabel lineStyleLabel = new JLabel();
        setLabel(lineStyleLabel, "Line Style:", 's', lineStyle);
        mainPanel.add(lineStyleLabel,new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        lineStyle.setPreferredSize(new Dimension(200,19));
        mainPanel.add(lineStyle,new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        lineStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int width = Util.string2int(lineWidth.getText(), 2);
                switch (lineStyle.getSelectedIndex()) {
                    case DataSeriesProperties.LINESTYLE_SOLID:
                    case DataSeriesProperties.LINESTYLE_DASHED:
                        if (width > 1) {
                            lineWidth.setText("1");
                        }
                        break;
                    case DataSeriesProperties.LINESTYLE_STRONG:
                        if (width < 2) {
                            lineWidth.setText("2");
                        }
                        break;
                }
            }
        });
        
        JLabel valueAxisLabel = new JLabel();
        setLabel(valueAxisLabel, "Value Axis:", 'x', valueAxis);
        mainPanel.add(valueAxisLabel,new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        valueAxis.setPreferredSize(new Dimension(200,19));
        mainPanel.add(valueAxis,new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        JLabel smoothLabel = new JLabel();
        setLabel(smoothLabel, "Smooth:", 'm', smooth);
        mainPanel.add(smoothLabel,new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        smooth.setPreferredSize(new Dimension(200,19));
        mainPanel.add(smooth,new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        if (addLabelButton) {
            JButton alButton = new JButton("Add Label to Graph");
            BaseDialog.setIcon(alButton, "label.png", false);
            mainPanel.add(alButton, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            alButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addLabel(e);
            }
        });
        }

        iniData();
    }
    
    void iniData() {
        if (prop.getName() != null) {
            // normal editing of a single series properties
            name.setText(prop.getName());
            displayname.setText(prop.getDisplayName());
            rangeYMin.setText(Double.toString(prop.getScaleMin()));
            rangeYMax.setText(Double.toString(prop.getScaleMax()));
            color.setText(Util.getColor(prop.getColor()));
            updateColor(color);
            for (int i = 0; i < DataSeriesProperties.STYLES.length; i++) {
                style.addItem(DataSeriesProperties.STYLES[i]);
            }
            style.setSelectedIndex(prop.getStyle());
            for (int i = 0; i < DataSeriesProperties.LINESTYLES.length; i++) {
                lineStyle.addItem(DataSeriesProperties.LINESTYLES[i]);
            }
            lineStyle.setSelectedIndex(prop.getLineStyle());
            lineWidth.setText(Integer.toString(prop.getLineWidth()));
            smooth.setText(Integer.toString(prop.getSmooth()));
            for (int i = 0; i < DataSeriesProperties.VALUEAXIS.length; i++) {
                valueAxis.addItem(DataSeriesProperties.VALUEAXIS[i]);
            }
            valueAxis.setSelectedIndex(prop.getValueAxis());
        } else {
            // we're "batch-editing" a bunch of properties. leave things empty
            name.setVisible(false);
            displayname.setVisible(false);
            for (int i = 0; i < DataSeriesProperties.STYLES.length; i++) {
                style.addItem(DataSeriesProperties.STYLES[i]);
            }
            style.addItem("");
            style.setSelectedIndex(DataSeriesProperties.STYLES.length);
            for (int i = 0; i < DataSeriesProperties.LINESTYLES.length; i++) {
                lineStyle.addItem(DataSeriesProperties.LINESTYLES[i]);
            }
            lineStyle.addItem("");
            lineStyle.setSelectedIndex(DataSeriesProperties.LINESTYLES.length);
            for (int i = 0; i < DataSeriesProperties.VALUEAXIS.length; i++) {
                valueAxis.addItem(DataSeriesProperties.VALUEAXIS[i]);
            }
            valueAxis.addItem("");
            valueAxis.setSelectedIndex(DataSeriesProperties.VALUEAXIS.length);
        }
    }
    
    void choseColor() {
        String colorTxt = color.getText().trim();
        colorTxt = choseColor(this, colorTxt);
        if (colorTxt != null) {
            color.setText(colorTxt);
            updateColor(color);
        }
    }
    
    void updateColor(JTextField field) {
        String txt = field.getText().trim();
        Color color = Util.getColor(txt);
        if (color != null) {
            field.setForeground(color);
            if (color.equals(field.getBackground())) {
                field.setBackground(Color.blue);
            } else {
                field.setBackground(displayname.getBackground());
            }
        }
    }
    
    void addLabel(ActionEvent e) {
        SGLabel label = new SGLabel(displayname.getText().trim(),
                displayname.getText().trim(), null);
        label.setColor(Util.getColor(color.getText().trim()));
        label.setHeightP(Main.config.getLabelHeight());
        label = LabelDialog.editLabel(this, label);
        if (label != null && graphPanel != null) {
            graphPanel.addLabel(label.getText(), label.getColor(), label.getHeightP());
            closeWindow(true);
        }
    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            prop.setDisplayName(displayname.getText().trim());
            try {
                prop.setScaleMin(Double.parseDouble(rangeYMin.getText().trim()));
                changedMin = rangeYMin.getText().trim().length() > 0;
            } catch (Exception ee) {
            }
            try {
                prop.setScaleMax(Double.parseDouble(rangeYMax.getText().trim()));
                changedMax = rangeYMax.getText().trim().length() > 0;
            } catch (Exception ee) {
            }
            prop.setColor(Util.getColor(color.getText().trim()));
            changedColor = color.getText().trim().length() > 0;
            prop.setStyle(style.getSelectedIndex());
            changedStyle = style.getSelectedIndex() >= 0 && style.getSelectedItem().toString().length() > 0;
            try {
                prop.setLineWidth(Util.string2int(lineWidth.getText().trim(), 2));
                changedLineWidth = lineWidth.getText().trim().length() > 0;
            } catch (Exception ee) {
            }
            prop.setLineStyle(lineStyle.getSelectedIndex());
            changedLineStyle = lineStyle.getSelectedIndex() >= 0 && lineStyle.getSelectedItem().toString().length() > 0;
            try {
                prop.setSmooth(Util.string2int(smooth.getText().trim(), DataSeriesProperties.LINESTYLE_SOLID));
                changedSmooth = smooth.getText().trim().length() > 0;
            } catch (Exception ee) {
            }
            prop.setValueAxis(valueAxis.getSelectedIndex());
            changedValueAxis = valueAxis.getSelectedIndex() >= 0 && valueAxis.getSelectedItem().toString().length() > 0;
        }
        super.closeWindow(okButton);
    }

    public boolean isChangedMin() {
        return changedMin;
    }

    public boolean isChangedMax() {
        return changedMax;
    }
    
    public boolean isChangedColor() {
        return changedColor;
    }
    
    public boolean isChangedStyle() {
        return changedStyle;
    }
    
    public boolean isChangedLineStyle() {
        return changedLineStyle;
    }
    
    public boolean isChangedLineWidth() {
        return changedLineWidth;
    }
    
    public boolean isChangedSmooth() {
        return changedSmooth;
    }
    
    public boolean isChangedValueAxis() {
        return changedValueAxis;
    }
    
}
