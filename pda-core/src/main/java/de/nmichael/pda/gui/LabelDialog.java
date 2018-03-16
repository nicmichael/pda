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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import javax.swing.*;

public class LabelDialog extends BaseDialog {
    
    // Data
    private SGLabel label;
    
    // Base Layout
    private JTextField text;
    private JTextField color;
    private JTextField size;
    private JButton colorChooser = new JButton();
    
    // Ok Button
    private JButton okButton = new JButton();

    public LabelDialog(JFrame parent, SGLabel label) {
        super(parent, "Configure Label");
        setLabel(label);
    }

    public LabelDialog(JDialog parent, SGLabel label) {
        super(parent, "Configure Label");
        setLabel(label);
    }
    
    private void setLabel(SGLabel label) {
        if (label == null) {
            label =  new SGLabel("", "", null);
            label.setColor(Color.black);
            label.setHeightP(Main.config.getLabelHeight());
        }
        this.label = label;
    }

    @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        
        JLabel textLabel = new JLabel();
        text = new JTextField();
        text.setText(label.getText());
        setLabel(textLabel, "Text:", 't', text);
        mainPanel.add(textLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        text.setPreferredSize(new Dimension(300, 19));
        mainPanel.add(text, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        JButton deleteLabel = new JButton();
        deleteLabel.setPreferredSize(new Dimension(40, 20));
        setIcon(deleteLabel, "delete.png");
        deleteLabel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (BaseDialog.yesNoDialog(parent, "Delete Label", 
                        "Do you really want to delete this label?") == BaseDialog.YES) {
                    text.setText("");
                    closeWindow(true);
                }
            }
        });
        mainPanel.add(deleteLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

        JLabel colorLabel = new JLabel();
        color = new JTextField();
        color.setText(Util.getColor(label.getColor()));
        updateColor(color);
        setLabel(colorLabel, "Color:", 'c', color);
        mainPanel.add(colorLabel,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        color.setPreferredSize(new Dimension(250,19));
        mainPanel.add(color,new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        color.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(FocusEvent e) {
                updateColor(color);
            }
        });
        setIcon(colorChooser, "colorchooser.png");
        colorChooser.setMargin(new Insets(0, 5, 0, 5));
        colorChooser.setPreferredSize(new Dimension(40, 20));
        colorChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choseColor();
            }
        });
        mainPanel.add(colorChooser,new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

        JLabel textSizeLabel = new JLabel();
        size = new JTextField();
        size.setText(Util.double2string(label.getHeightP(), 2, false).trim());
        setLabel(textSizeLabel, "Size:", 's', size);
        mainPanel.add(textSizeLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        size.setPreferredSize(new Dimension(100, 19));
        mainPanel.add(size, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        text.selectAll();
        text.requestFocus();
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
                field.setBackground(text.getBackground());
            }
        }
    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            label.setText(text.getText().trim());
            label.setColor(Util.getColor(color.getText().trim()));
            label.setHeightP(Util.string2double(size.getText().trim(), 
                    label.getHeightP()));
        }
        super.closeWindow(okButton);
    }
    
    public static SGLabel editLabel(JFrame parent, SGLabel label) {
        LabelDialog dlg = new LabelDialog(parent, label);
        if (dlg.showDialog()) {
            return dlg.label;
        } else {
            return null;
        }
    }
    
    public static SGLabel editLabel(JDialog parent, SGLabel label) {
        LabelDialog dlg = new LabelDialog(parent, label);
        if (dlg.showDialog()) {
            return dlg.label;
        } else {
            return null;
        }
    }

}
