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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import javax.swing.*;

public class ChangeDirDialog extends BaseDialog {
    
    // Data
    private String oldDirName;
    private String newDirName;
    
    // Base Layout
    private JTextField oldDir;
    private JTextField newDir;
    
    // Ok Button
    private JButton okButton = new JButton();

    public ChangeDirDialog(JFrame parent, String oldDir) {
        super(parent, "Change Data Directory");
        this.oldDirName = oldDir;
    }

    @Override
    protected void initialize() {
        mainPanel.setLayout(new GridBagLayout());
        
        JLabel oldDirLabel = new JLabel();
        oldDir = new JTextField();
        oldDir.setText(oldDirName);
        setLabel(oldDirLabel, "Old Directory:", 'o', oldDir);
        mainPanel.add(oldDirLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        oldDir.setPreferredSize(new Dimension(800, 19));
        mainPanel.add(oldDir, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        JLabel newDirLabel = new JLabel();
        newDir = new JTextField();
        newDir.setText(oldDirName);
        setLabel(newDirLabel, "New Directory:", 'n', newDir);
        mainPanel.add(newDirLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        newDir.setPreferredSize(new Dimension(800, 19));
        mainPanel.add(newDir, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        JButton selectDirButton = new JButton();
        selectDirButton.setPreferredSize(new Dimension(30, 22));
        setIcon(selectDirButton, "fileopen.png");
        selectDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = BaseDialog.openDirDlg(parent, "Select New Directory", null, null, newDir.getText().trim());
                if (s != null) {
                    newDir.setText(s);
                }
            }
        });
        mainPanel.add(selectDirButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton) {
            oldDirName = oldDir.getText().trim();
            newDirName = newDir.getText().trim();
        }
        super.closeWindow(okButton);
    }
    
    private String getDir(String dir) {
        if (dir != null) {
            dir = dir.trim();
            if (!dir.endsWith(Main.FILESEP)) {
                dir = dir + Main.FILESEP;
            }
        }
        return dir;
    }
    
    public String getOldDir() {
        return getDir(oldDirName);
    }
    
    public String getNewDir() {
        return getDir(newDirName);
    }
    
}
