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
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.Util;
import java.util.regex.Pattern;

public class ConfigureGroupsDialog extends BaseDialog {

    // Data
    ProjectItem projectItem;
    DataSeriesGroupSet groupSet;
    String[] seriesNames;
    private Pattern filterPattern;
    
    // Group Manage Panel
    private JPanel managePanel = new JPanel();
    private JLabel groupsListLabel = new JLabel();
    private JScrollPane groupsListScrollPane = new JScrollPane();
    private JList groupsList = new JList();
    private JPanel manageButtonsPanel = new JPanel();
    private JButton newGroupButton = new JButton();
    private JButton deleteGroupButton = new JButton();
    
    // Group Configure Panel
    private JPanel configurePanel = new JPanel();
    private JPanel functionPanel = new JPanel();
    private JLabel functionLabel = new JLabel();
    private JComboBox function = new JComboBox();
    private JPanel availableMembersPanel = new JPanel();
    private JLabel availableMembersLabel = new JLabel();
    private JScrollPane availableMembersScrollPane = new JScrollPane();
    private JList availableMembersList = new JList();
    private JPanel configuredMembersPanel = new JPanel();
    private JLabel configuredMembersLabel = new JLabel();
    private JScrollPane configuredMembersScrollPane = new JScrollPane();
    private JList configuredMembersList = new JList();
    private JPanel configureButtonsPanel = new JPanel();
    private JButton addMembersButton = new JButton();
    private JButton removeMembersButton = new JButton();
    
    // Filter Panel
    JTextField filter;
    
    public ConfigureGroupsDialog(JDialog parent, ProjectItem projectItem) {
        super(parent, "Series Groups");
        this.projectItem = projectItem;
        this.groupSet = projectItem.getGroups();
        this.seriesNames = projectItem.getAllSeriesNames(false);
    }
    
    @Override
    protected void initialize() {
        mainPanel.setLayout(new BorderLayout());
        // Group Manage Panel
        mainPanel.add(managePanel,BorderLayout.NORTH);        
        managePanel.setLayout(new GridBagLayout());
        
        groupsListLabel.setText("Groups");
        groupsListLabel.setDisplayedMnemonic('g');
        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsListLabel.setLabelFor(groupsList);
        managePanel.add(groupsListLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
        
        groupsList.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                groupsListChanged();
            }
        });
        groupsList.addListSelectionListener(new ListSelectionListener() {
           public void valueChanged(ListSelectionEvent e)  {
               groupsListChanged();
           }
        });
        groupsListScrollPane.setPreferredSize(new Dimension(400,100));
        groupsListScrollPane.getViewport().add(groupsList);
        managePanel.add(groupsListScrollPane,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        
        manageButtonsPanel.setLayout(new GridBagLayout());
        managePanel.add(manageButtonsPanel,new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        
        newGroupButton.setText("New Group");
        newGroupButton.setMnemonic('n');
        newGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newGroup(e);
            }
        });
        manageButtonsPanel.add(newGroupButton,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        deleteGroupButton.setText("Delete Group");
        deleteGroupButton.setMnemonic('d');
        deleteGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteGroup(e);
            }
        });
        manageButtonsPanel.add(deleteGroupButton,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        
        // Group Configure Panel
        mainPanel.add(configurePanel,BorderLayout.CENTER);        
        configurePanel.setLayout(new BorderLayout());
        
        functionPanel.setLayout(new GridBagLayout());
        functionLabel.setText("Vallue Aggregate Function: ");
        functionLabel.setDisplayedMnemonic('f');
        functionLabel.setLabelFor(function);
        for (int i=0; i<DataSeriesGroup.FUNCTIONS.length; i++) {
            function.addItem(DataSeriesGroup.FUNCTIONS[i]);
        }
        function.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                function_itemStateChanged(e);
            }
        });
        functionPanel.add(functionLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 0), 0, 0));
        functionPanel.add(function,new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 10, 10), 0, 0));
        configurePanel.add(functionPanel,BorderLayout.NORTH);
        
        availableMembersPanel.setLayout(new GridBagLayout());
        availableMembersLabel.setText("Available Group Members");
        availableMembersLabel.setDisplayedMnemonic('v');
        availableMembersLabel.setLabelFor(availableMembersList);
        availableMembersPanel.add(availableMembersLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
        availableMembersScrollPane.setPreferredSize(new Dimension(450,300));
        availableMembersScrollPane.getViewport().add(availableMembersList);
        availableMembersPanel.add(availableMembersScrollPane,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        configurePanel.add(availableMembersPanel,BorderLayout.WEST);
        
        configuredMembersPanel.setLayout(new GridBagLayout());
        configuredMembersLabel.setText("Selected Group Members");
        configuredMembersLabel.setDisplayedMnemonic('s');
        configuredMembersLabel.setLabelFor(configuredMembersList);
        configuredMembersPanel.add(configuredMembersLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
        configuredMembersScrollPane.setPreferredSize(new Dimension(450,300));
        configuredMembersScrollPane.getViewport().add(configuredMembersList);
        configuredMembersPanel.add(configuredMembersScrollPane,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        configurePanel.add(configuredMembersPanel,BorderLayout.EAST);

        configureButtonsPanel.setLayout(new GridBagLayout());
        setIcon(addMembersButton, "arrowright.png");
        addMembersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addMembers(e);
            }
        });
        configureButtonsPanel.add(addMembersButton,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        setIcon(removeMembersButton, "arrowleft.png");
        removeMembersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeMembers(e);
            }
        });
        configureButtonsPanel.add(removeMembersButton,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        configurePanel.add(configureButtonsPanel,BorderLayout.CENTER);
        
        // Filter Panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        JLabel filterLabel = new JLabel();
        filter = new JTextField();
        setLabel(filterLabel, "Filter:", 'f', filter);
        filter.setPreferredSize(new Dimension(400, 19));
        filter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filter();
            }
        });
        filterPanel.add(filterLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
        filterPanel.add(filter,new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
        mainPanel.add(filterPanel, BorderLayout.SOUTH);

        updateData();
    }
    
    void updateData() {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) {
            return;
        }
        Vector<String> data = new Vector<String>();
        for (int i=0; i<groupSet.size(); i++) {
            data.add(groupSet.getSeriesGroup(i).getLocalName());
        }
        groupsList.setListData(data);
        groupsListChanged();
    }
    
    void newGroup(ActionEvent e) {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) {
            return;
        }
        String name = inputDialog(this, "Please enter a group name (category:subcategory:series):",
                "Name");
        if (name == null || name.trim().length() == 0) {
            return;
        }
        if (Util.countInString(name, DataSeries.SEPARATOR.charAt(0)) > 2) {
            errorDlg(this, "Malformed group name.");
            return;
        }
        // form name as parser:category:subcategory:series
        for (int i=Util.countInString(name, DataSeries.SEPARATOR.charAt(0)); i<3; i++) {
            name = DataSeries.SEPARATOR + name;
        }
        
        DataSeriesGroup group = new DataSeriesGroup(
                DataSeries.getCategoryName(name),
                DataSeries.getSubcategoryName(name),
                DataSeries.getSeriesName(name));
        name = group.getLocalName();
        
        if (groupSet.getSeriesGroup(name) != null) {
            errorDlg(this, "A group named '" + name + "' already exists.");
            return;
        }
        
        groupSet.addGroup(group);
        updateData();
        groupsListChanged();        
        groupsList.setSelectedValue(name, true);
    }
    
    void deleteGroup(ActionEvent e) {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) return;
        int i = groupsList.getSelectedIndex();
        if (i<0) {
            errorDlg(this, "Please first select a group to delete.");
            return;
        }
        DataSeriesGroup group = groupSet.getSeriesGroup(i);
        if (group == null) {
            errorDlg(this, "Please first select a group to delete.");
            return;
        }
        if (yesNoDialog(this, "Delete Group",
                "Do you really want to delete group "+group.getLocalName()+"?") != YES) {
            return;
        }
        groupSet.removeSeriesGroup(i);
        updateData();
        groupsListChanged();        
    }
    
    void groupsListChanged() {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) return;
        
        int idx = groupsList.getSelectedIndex();
        if (idx<0) {
            availableMembersList.setListData(new String[0]);
            configuredMembersList.setListData(new String[0]);
        } else {
            DataSeriesGroup group = groupSet.getSeriesGroup(idx);
            if (group == null) {
                return;
            }
            function.setSelectedIndex(group.getFunction());
            Vector<String> availableSeries = new Vector<String>();
            Vector<String> memberSeries = new Vector<String>();
            
            for (int i=0; i<seriesNames.length; i++) {
                if (filterPattern != null) {
                    if (!filterPattern.matcher(seriesNames[i]).matches()) {
                        continue;
                    }
                }
                if (group.isMember(seriesNames[i])) {
                    memberSeries.add(seriesNames[i]);
                } else {
                    availableSeries.add(seriesNames[i]);
                }
            }
            availableMembersList.setListData(availableSeries);
            configuredMembersList.setListData(memberSeries);
        }
    }
    
    void addMembers(ActionEvent e) {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) return;
        int idx = groupsList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        DataSeriesGroup group = groupSet.getSeriesGroup(idx);
        if (group == null) {
            return;
        }
        Object[] selected = availableMembersList.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        for (int i=0; i<selected.length; i++) {
            DataSeries series = projectItem.getSeries(selected[i].toString());
            if (series != null) {
                group.addMember(series);
                group.setParsed(false);
            }
        }
        groupsListChanged();
    }
    
    void removeMembers(ActionEvent e) {
        if (groupsList == null || availableMembersList == null || configuredMembersList == null) return;
        int idx = groupsList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        DataSeriesGroup group = groupSet.getSeriesGroup(idx);
        if (group == null) {
            return;
        }
        Object[] selected = configuredMembersList.getSelectedValues();
        if (selected.length == 0) {
            return;
        }
        for (int i=0; i<selected.length; i++) {
            DataSeries series = projectItem.getSeries(selected[i].toString());
            if (series != null) {
                group.removeMember(series);
                group.setParsed(false);
            }
        }
        groupsListChanged();
        
    }
    
    void function_itemStateChanged(ItemEvent e) {
        if (groupsList == null || function == null) return;
        int g = groupsList.getSelectedIndex();
        int f = function.getSelectedIndex();
        if (g<0 || f<0) return;
        DataSeriesGroup group = groupSet.getSeriesGroup(g);
        if (group == null) {
            return;
        }
        group.setFunction(f);
        group.setParsed(false);
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
            groupsListChanged();
        }
    }

   protected void closeWindow(boolean okButton) {
        if (okButton) {
            groupSet.parseAllUpdate();
        }
        super.closeWindow(okButton);
    }
    
}