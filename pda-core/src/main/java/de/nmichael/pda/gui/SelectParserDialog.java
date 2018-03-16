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

public class SelectParserDialog extends BaseDialog {

    // Data
    String filename;
    Vector<Parser> allParsers;
    int preferredParser;
    Parser selectedParser = null;
    
    // Main Panel
    private JComboBox parsers = new JComboBox();
    private JEditorPane fileFormatInfo = new JEditorPane();
    
    // Ok Button
    private JButton okButton = new JButton();
    
    /** Creates a new instance of SelectParserFrame */
    public SelectParserDialog(JDialog parent, String filename, Vector<Parser> allParsers, int preferredParser) {
        super(parent, "Select Parser");
        this.filename = filename;
        this.allParsers = allParsers;
        this.preferredParser = preferredParser;
    }
    
    @Override
    protected void initialize() {
        // Top Panel
        mainPanel.setLayout(new GridBagLayout());
        JLabel infoLabel = new JLabel();
        JLabel filenameLabel = new JLabel();
        JLabel preferredParserLabel = new JLabel();
        JLabel parserLabel = new JLabel();
        infoLabel.setText("Please select a parser to handle the file:");
        filenameLabel.setText(filename);
        filenameLabel.setForeground(Color.blue);
        if (preferredParser < 0) {
            preferredParserLabel.setText("No parsers found that think they can handle this file!");
            parserLabel.setText("");
            preferredParserLabel.setForeground(Color.red);
        } else {
            String p = allParsers.get(preferredParser).getClass().getName();
            preferredParserLabel.setText("The preferred parser is:");
            parserLabel.setText(p);
            parserLabel.setForeground(new Color(0, 0, 128));
        }
        mainPanel.add(infoLabel,new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(filenameLabel,new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(preferredParserLabel,new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        mainPanel.add(parserLabel,new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        // File Format Info
        JScrollPane fileFormatInfoScrollPane = new JScrollPane();
        fileFormatInfoScrollPane.setMinimumSize(new Dimension(900,350));
        fileFormatInfoScrollPane.setPreferredSize(new Dimension(900,350));
        fileFormatInfoScrollPane.getViewport().add(fileFormatInfo, null);
        fileFormatInfo.setEditable(false);
        fileFormatInfo.setContentType("text/html");
        
        
        // Select Parser ComboBox
        for (int i=0; i<allParsers.size(); i++) {
            Parser parser = allParsers.get(i);
            String p = parser.getName()+" ("+parser.getClass().getName()+")";
            parsers.addItem(p);
        }
        parsers.setMaximumRowCount(20);
        parsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parserChanged(e);
            }
        });
        if (preferredParser >= 0) {
            parsers.setSelectedIndex(preferredParser);
        }
        mainPanel.add(parsers,new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));
        mainPanel.add(fileFormatInfoScrollPane,new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 0, 0), 0, 0));
    }
    
    protected void closeWindow(boolean okButton) {
        if (okButton && parsers.getSelectedIndex() >= 0) {
            selectedParser = allParsers.get(parsers.getSelectedIndex());
        }
        super.closeWindow(okButton);
    }
    
    void parserChanged(ActionEvent e) {
        if (parsers.getSelectedIndex() >= 0) {
            selectedParser = allParsers.get(parsers.getSelectedIndex());
            FileFormatDescription ffd = selectedParser.getSupportedFileFormat();
            TimeStamp ts = selectedParser.getCurrentTimeStamp();
                StringBuilder argTable = new StringBuilder();
                String[][] args = (ffd != null ? ffd.getArgumentOption() : null);
                if (args != null && args.length > 0) {
                    argTable.append("<table width=\"100%\"><tr>");
                    for (int i = 0; i < args.length; i++) {
                        argTable.append("<td>");
                        for (int j=0; args[i] != null && j<args[i].length; j++) {
                            argTable.append(args[i][j] + "<br>");
                        }
                        argTable.append("</td>");
                    }
                    argTable.append("</tr></table>");
                }
                
                StringBuilder s = new StringBuilder();
                s.append("<html><body>");
                s.append("<table border=\"1\" width=\"100%\">");
                s.append("<tr><td>Parser:</td><td>" + selectedParser.getName() + "</td></tr>");
                if (ffd != null && ffd.getDescription() != null && ffd.getDescription().length() > 0) {
                    s.append("<tr><td>Description:</td><td>" + ffd.getDescription() + "</td></tr>");
                }
                if (ffd != null && ffd.getProductName() != null && ffd.getProductName().length() > 0) {
                    s.append("<tr><td>Supported Product(s):</td><td>" + ffd.getProductName() + "</td></tr>");
                }
                if (ffd != null && ffd.getProductVersion() != null && ffd.getProductVersion().length() > 0) {
                    s.append("<tr><td>Product Version(s):</td><td>" + ffd.getProductVersion() + "</td></tr>");
                }
                if (ffd != null && ffd.getComponentName() != null && ffd.getComponentName().length() > 0) {
                    s.append("<tr><td>Supported Component(s):</td><td>" + ffd.getComponentName() + "</td></tr>");
                }
                if (argTable.length() > 0) {
                    s.append("<tr><td>Supported Arguments:</td><td>" + argTable.toString() + "</td></tr>");
                }
                if (ffd != null && ffd.getExampleFormat() != null && ffd.getExampleFormat().length() > 0) {
                    s.append("<tr><td>Example Format:</td><td>" + ffd.getExampleFormat() + "</td></tr>");
                }
                if (ts != null && ts.size() > 0) {
                    StringBuilder tsTable = new StringBuilder();
                    tsTable.append("<table width=\"100%\">");
                    for (int i=0; i<ts.size(); i++) {
                        tsTable.append("<tr><td>" + ts.getTimeStampPattern(i).toString() + "</td><td>" + ts.getTimeStampDescription(i) + "</td></tr>");
                    }
                    s.append("<tr><td>Supported Timestamps:</td><td>" + tsTable.toString()  + "</td></tr>");
                }
                s.append("</table>");
                s.append("</body></html>");
                fileFormatInfo.setText(s.toString());
                fileFormatInfo.setCaretPosition(0);
                fileFormatInfo.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
                return;
            }
        fileFormatInfo.setText("");
    }
    
    public Parser getSelectedParser() {
        return selectedParser;
    }

    
}
