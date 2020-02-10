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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import de.nmichael.pda.*;
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.*;
import gov.noaa.pmel.sgt.SGLabel;

public class MainFrame extends JFrame {

    // Data
    private Project project = null;
    private ProjectItem projectItem = null;

    // Base Layout
    private JPanel contentPane;
    private BorderLayout contentPaneBorderLayout = new BorderLayout();

    // Right Panel
    private JPanel rightPanel = new JPanel();
    private BorderLayout rightPanelBorderLayout = new BorderLayout();
    private JLabel freeMem = new JLabel();

    // Control Panel
    private JPanel controlPanel = new JPanel();
    private GridBagLayout controlPanelGridBagLayout = new GridBagLayout();
    private JButton pdaButton = new JButton();
    private JButton configureProjectButton = new JButton();
    private JButton selectSeriesButton = new JButton();
    private JButton statisticsButton = new JButton();
    private JButton printButton = new JButton();
    private JButton saveImageButton = new JButton();
    private JButton addLabelButton = new JButton();

    // Series Panel
    private JPanel seriesPanel = null;
    private JComboBox seriesActionCombo = null;

    // Graph Panel
    private JPanel graphRootPanel = new JPanel();
    private GraphPanel graphPanel = null;
    private JPanel graphControlPanel;
    private JLabel graphZoomLabel;

    // Menu Bar
    private JMenuBar menuBar = new JMenuBar();
    JMenu menuProject = new JMenu();
    JMenuItem menuProjectOpen = new JMenuItem();
    JMenuItem menuProjectSave = new JMenuItem();
    JMenuItem menuProjectSaveAs = new JMenuItem();
    JMenuItem menuProjectChangeDir = new JMenuItem();
    JMenuItem menuProjectClose = new JMenuItem();
    JMenuItem menuProjectConfigure = new JMenuItem();
    JMenuItem menuProjectSelect = new JMenuItem();
    JMenuItem menuProjectStatistics = new JMenuItem();
    JMenuItem menuProjectPrint = new JMenuItem();
    JMenuItem menuProjectSaveAsImage = new JMenuItem();
    JMenuItem menuProjectExit = new JMenuItem();
    JMenu menuConfiguration = new JMenu();
    JMenuItem menuConfigurationSettings = new JMenuItem();
    JMenu menuHelp = new JMenu();
    JMenuItem menuHelpAbout = new JMenuItem();

    /** Creates a new instance of MainFrame */
    public MainFrame() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
            createNewProject();
            updateDisplay();
            this.pack();
            configureProjectButton.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        BaseDialog.setIcon(this, "pda64.png");
        // Base Properties
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });

        // Title
        updateTitle();

        // Base Layout
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(contentPaneBorderLayout);

        // Right Panel
        contentPane.add(rightPanel, BorderLayout.EAST);
        rightPanel.setLayout(rightPanelBorderLayout);
        rightPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        // Control Panel
        rightPanel.add(controlPanel, BorderLayout.NORTH);
        controlPanel.setLayout(controlPanelGridBagLayout);
        rightPanel.add(freeMem, BorderLayout.SOUTH);

        BaseDialog.setIcon(pdaButton, "pdav2_64.png", false);
        pdaButton.setHorizontalAlignment(SwingConstants.CENTER);
        pdaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuHelpAbout_actionPerformed(e);
            }
        });
        controlPanel.add(pdaButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
        pdaButton.setVisible(false);

        // Control Panel: pda Info Button
        configureProjectButton.setText("Configure Project");
        configureProjectButton.setMnemonic('c');
        BaseDialog.setIcon(configureProjectButton, "curves.png", false);
        configureProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureProject(e);
            }
        });
        controlPanel.add(configureProjectButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

        selectSeriesButton.setText("Select Series");
        selectSeriesButton.setMnemonic('s');
        BaseDialog.setIcon(selectSeriesButton, "addseries.png", false);
        selectSeriesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectSeries(e);
            }
        });
        controlPanel.add(selectSeriesButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 0));

        // Control Panel: Statistics Button
        statisticsButton.setText("Statistics");
        statisticsButton.setMnemonic('t');
        BaseDialog.setIcon(statisticsButton, "statistics.png", false);
        statisticsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seriesStatsButtonPressed(null);
            }
        });
        controlPanel.add(statisticsButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 0));

        // Control Panel: Print Button
        printButton.setText("Print Image");
        printButton.setMnemonic('r');
        BaseDialog.setIcon(printButton, "print.png", false);
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printGraphs(e);
            }
        });
        controlPanel.add(printButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

        // Control Panel: Save Image Button
        saveImageButton.setText("Save Image");
        saveImageButton.setMnemonic('i');
        BaseDialog.setIcon(saveImageButton, "image.png", false);
        saveImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage(e);
            }
        });
        controlPanel.add(saveImageButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 0));

        addLabelButton.setText("Add Label");
        addLabelButton.setMnemonic('l');
        BaseDialog.setIcon(addLabelButton, "label.png", false);
        addLabelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addLabel(e);
            }
        });
        controlPanel.add(addLabelButton, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

        // Graph Panel
        graphRootPanel.setLayout(new BorderLayout());
        contentPane.add(graphRootPanel, BorderLayout.CENTER);

        // Graph Control Panel
        graphControlPanel = new JPanel();
        graphControlPanel.setLayout(new GridBagLayout());
        graphRootPanel.add(graphControlPanel, BorderLayout.SOUTH);

        JButton graphBegin = new JButton();
        graphBegin.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphBegin, "graphbegin.png");
        graphBegin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.begin);
            }
        });
        graphControlPanel.add(graphBegin, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphBackward = new JButton();
        graphBackward.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphBackward, "graphbackward.png");
        graphBackward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.backward);
            }
        });
        graphControlPanel.add(graphBackward, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphBackwardSlow = new JButton();
        graphBackwardSlow.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphBackwardSlow, "graphbackwards.png");
        graphBackwardSlow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.backwardslow);
            }
        });
        graphControlPanel.add(graphBackwardSlow, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphForwardSlow = new JButton();
        graphForwardSlow.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphForwardSlow, "graphforwards.png");
        graphForwardSlow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.forwardslow);
            }
        });
        graphControlPanel.add(graphForwardSlow, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphForward = new JButton();
        graphForward.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphForward, "graphforward.png");
        graphForward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.forward);
            }
        });
        graphControlPanel.add(graphForward, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphEnd = new JButton();
        graphEnd.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphEnd, "graphend.png");
        graphEnd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.end);
            }
        });
        graphControlPanel.add(graphEnd, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphZoomIn = new JButton();
        graphZoomIn.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphZoomIn, "graphzoomin.png");
        graphZoomIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.zoomin);
            }
        });
        graphControlPanel.add(graphZoomIn, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 00), 0, 0));
        JButton graphZoomOut = new JButton();
        graphZoomOut.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphZoomOut, "graphzoomout.png");
        graphZoomOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.zoomout);
            }
        });
        graphControlPanel.add(graphZoomOut, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));
        JButton graphZoomReset = new JButton();
        graphZoomReset.setPreferredSize(new Dimension(26, 26));
        BaseDialog.setIcon(graphZoomReset, "graphreset.png");
        graphZoomReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphAction(GraphPanel.GraphAction.zoomreset);
            }
        });
        graphControlPanel.add(graphZoomReset, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 00), 0, 0));

        graphZoomLabel = new JLabel();
        graphControlPanel.add(graphZoomLabel, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 00), 0, 0));

        // Menu Bar
        menuProject.setText("Project");
        menuProject.setMnemonic('p');
        menuProjectOpen.setText("Open ...");
        menuProjectOpen.setMnemonic('o');
        BaseDialog.setIcon(menuProjectOpen, "fileopen.png");
        menuProjectOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectOpen_actionPerformed(e);
            }
        });
        menuProjectSave.setText("Save ...");
        menuProjectSave.setMnemonic('s');
        BaseDialog.setIcon(menuProjectSave, "filesave.png");
        menuProjectSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectSave_actionPerformed(e);
            }
        });
        menuProjectSaveAs.setText("Save As ...");
        menuProjectSaveAs.setMnemonic('a');
        BaseDialog.setIcon(menuProjectSaveAs, "filesaveas.png");
        menuProjectSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectSaveAs_actionPerformed(e);
            }
        });
        menuProjectChangeDir.setText("Change Data Directory ...");
        menuProjectChangeDir.setMnemonic('c');
        BaseDialog.setIcon(menuProjectChangeDir, "filedirreplace.png");
        menuProjectChangeDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectChangeDir_actionPerformed(e);
            }
        });
        menuProjectClose.setText("Close");
        menuProjectClose.setMnemonic('c');
        BaseDialog.setIcon(menuProjectClose, "fileclose.png");
        menuProjectClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectClose_actionPerformed(e);
            }
        });
        menuProjectConfigure.setText("Configure Project ...");
        menuProjectConfigure.setMnemonic('f');
        BaseDialog.setIcon(menuProjectConfigure, "curves.png");
        menuProjectConfigure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureProject(e);
            }
        });
        menuProjectSelect.setText("Select Series ...");
        menuProjectSelect.setMnemonic('s');
        BaseDialog.setIcon(menuProjectSelect, "addseries.png");
        menuProjectSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectSeries(e);
            }
        });
        menuProjectStatistics.setText("Statistics ...");
        menuProjectStatistics.setMnemonic('t');
        BaseDialog.setIcon(menuProjectStatistics, "statistics.png");
        menuProjectStatistics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seriesStatsButtonPressed(null);
            }
        });
        menuProjectPrint.setText("Print Image ...");
        menuProjectPrint.setMnemonic('p');
        BaseDialog.setIcon(menuProjectPrint, "print.png");
        menuProjectPrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printGraphs(e);
            }
        });
        menuProjectSaveAsImage.setText("Save Image ...");
        menuProjectSaveAsImage.setMnemonic('i');
        BaseDialog.setIcon(menuProjectSaveAsImage, "image.png");
        menuProjectSaveAsImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage(e);
            }
        });
        menuProjectExit.setText("Exit");
        menuProjectExit.setMnemonic('x');
        BaseDialog.setIcon(menuProjectExit, "exit.png");
        menuProjectExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuProjectExit_actionPerformed(e);
            }
        });
        menuConfiguration.setText("Configuration");
        menuConfiguration.setMnemonic('c');
        menuConfigurationSettings.setText("Settings ...");
        menuConfigurationSettings.setMnemonic('s');
        BaseDialog.setIcon(menuConfigurationSettings, "configure.png");
        menuConfigurationSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuConfigurationSettings_actionPerformed(e);
            }
        });
        menuHelp.setText("Help");
        menuHelp.setMnemonic('h');
        menuHelpAbout.setText("About ...");
        menuHelpAbout.setMnemonic('a');
        BaseDialog.setIcon(menuHelpAbout, "about.png");
        menuHelpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuHelpAbout_actionPerformed(e);
            }
        });

        menuProject.add(menuProjectOpen);
        menuProject.add(menuProjectSave);
        menuProject.add(menuProjectSaveAs);
        menuProject.add(menuProjectChangeDir);
        menuProject.add(menuProjectClose);
        menuProject.addSeparator();
        menuProject.add(menuProjectConfigure);
        menuProject.add(menuProjectSelect);
        menuProject.add(menuProjectStatistics);
        menuProject.addSeparator();
        menuProject.add(menuProjectPrint);
        menuProject.add(menuProjectSaveAsImage);
        menuProject.addSeparator();
        menuProject.add(menuProjectExit);

        menuConfiguration.add(menuConfigurationSettings);

        menuHelp.add(menuHelpAbout);

        menuBar.add(menuProject);
        menuBar.add(menuConfiguration);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    void updateTitle() {
        String title = Main.PROGRAM;
        if (project != null && project.getFileName() != null)
            title = title + " - " + project.getFileName();
        this.setTitle(title);
    }

    void this_windowClosing(WindowEvent e) {
        Main.exit();
    }

    void configureProject(ActionEvent e) {
        ConfigureProjectDialog dlg = new ConfigureProjectDialog(this, projectItem);
        dlg.showDialog();
        updateDisplay();
    }

    void selectSeries(ActionEvent e) {
        if (projectItem == null || projectItem.getParsers().size() == 0) {
            configureProject(e);
        } else {
            SelectSeriesDialog dlg = new SelectSeriesDialog(this, projectItem);
            dlg.showDialog();
            updateDisplay();
        }
    }

    void printGraphs(ActionEvent e) {
        SimpleFilePrinter printer = new SimpleFilePrinter(this.graphPanel);
        if (printer.setupPageFormat()) {
            if (printer.setupJobOptions()) {
                try {
                    printer.printFile();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    void saveImage(ActionEvent e) {
        String fname = BaseDialog.saveFileDlg(this, "Save Image", "PNG Image", "png", Main.config.getWorkdirProject());
        if (fname == null)
            return;
        if (fname.indexOf(".") < 0)
            fname += ".png";
        Main.config.setWorkdirProject(Util.getPathOfFile(fname));
        String result = graphPanel.saveImageToFile(fname);
        if (result != null) {
            BaseDialog.errorDlg(this, result);
        }
    }

    void resetZoom(ActionEvent e) {
    }

    void addLabel(ActionEvent e) {
        SGLabel label = LabelDialog.editLabel(this, null);
        if (label != null && graphPanel != null) {
            graphPanel.addLabel(label.getText(), label.getColor(), label.getHeightP());
        }
    }

    void updateDisplay() {
        DataLabel[] dl = null;
        if (graphPanel != null) {
            dl = graphPanel.getAllLabels();
            synchronized (graphRootPanel) {
                graphRootPanel.remove(graphPanel);
            }
        }
        graphPanel = new GraphPanel(this, projectItem, graphRootPanel.getWidth(), graphRootPanel.getHeight());
        graphPanel.updateGraphPanel();
        graphRootPanel.add(graphPanel, BorderLayout.CENTER);
        graphPanel.validate();

        if (projectItem.isScaleXSet()) {
            graphPanel.zoomAllGraphs(projectItem.getScaleMinX(), projectItem.getScaleMaxX());
        }

        updateSeriesPanel();

        for (int i = 0; dl != null && i < dl.length; i++) {
            graphPanel.addLabel(dl[i]);
        }

        updateZoomLabel();

        freeMem.setText("Free Mem: " + Runtime.getRuntime().freeMemory());

        validateFrame();
    }

    void updateZoomLabel() {
        if (projectItem.getScaleMinX() > 0 && projectItem.getScaleMaxX() > 0
                && projectItem.getScaleMaxX() < Long.MAX_VALUE) {
            graphZoomLabel.setText(Util.getTimeString(projectItem.getScaleMinX()) + " - "
                    + Util.getTimeString(projectItem.getScaleMaxX()) + " ("
                    + ((projectItem.getScaleMaxX() - projectItem.getScaleMinX()) / 1000) + " sec)");
        } else {
            graphZoomLabel.setText("");
        }
    }

    private void setLabels() {
        if (project != null && graphPanel != null) {
            for (int i = 0; i < project.getLabelCount(); i++) {
                DataLabel label = project.getLabel(i);
                graphPanel.addLabel(label);
            }
        }
    }

    private void validateFrame() {
        ValidateThread vt = new ValidateThread(this);
        vt.start();
    }

    private void seriesStatsButtonPressed(DataSeriesProperties p) {
        SeriesStatisticsDialog dlg = new SeriesStatisticsDialog(this, projectItem, p);
        dlg.showDialog();
    }

    private void seriesConfigButtonPressed(DataSeriesProperties p) {
        SeriesSettingsDialog dlg = new SeriesSettingsDialog(this, graphPanel, p);
        dlg.showDialog();
        updateDisplay();
    }

    private void seriesEnableButtonPressed(DataSeriesProperties p, JCheckBox checkBox) {
        p.setVisible(checkBox.isSelected());
        updateDisplay();
    }

    private void seriesEnableButtonPressed(boolean enableAll) {
        DataSeriesPropertySet props = projectItem.getSeriesProperties();
        for (int i = 0; i < props.size(); i++) {
            props.getDataProperties(i).setVisible(enableAll);
        }
        updateDisplay();
    }

    private void seriesHighlight(DataSeriesProperties p, boolean highlight) {
        graphPanel.updateLayerHighlight(p, highlight);
        graphPanel.validate();
        if (projectItem.isScaleXSet()) {
            graphPanel.zoomAllGraphs(projectItem.getScaleMinX(), projectItem.getScaleMaxX());
        }
        validateFrame();
    }


    private void addSeriesButton(JPanel panel, final DataSeriesProperties dp, int i, int width) {
        JButton but = new JButton();
        but.setText(dp.getDisplayName());
        but.setToolTipText(dp.getDisplayName());
        but.setPreferredSize(new Dimension(width - 60, 19));
        but.setMargin(new Insets(0, 0, 0, 0));
        but.setFont(but.getFont().deriveFont(10.0f));
        but.setForeground(dp.getColor());
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seriesStatsButtonPressed(dp);
            }
        });
        but.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                seriesHighlight(dp, true);
            }

            public void mouseExited(MouseEvent e) {
                seriesHighlight(dp, false);
            }
        });

        final JCheckBox butEnable = new JCheckBox();
        butEnable.setPreferredSize(new Dimension(20, 19));
        butEnable.setText("");
        butEnable.setSelected(dp.isVisible());
        butEnable.setMargin(new Insets(0, 0, 0, 0));
        butEnable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seriesEnableButtonPressed(dp, butEnable);
            }
        });
        panel.add(butEnable, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(but, new GridBagConstraints(1, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        JButton butCfg = new JButton();
        butCfg.setPreferredSize(new Dimension(20, 19));
        BaseDialog.setIcon(butCfg, "configure.png");
        butCfg.setMargin(new Insets(0, 0, 0, 0));
        butCfg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                seriesConfigButtonPressed(dp);
            }
        });
        panel.add(butCfg, new GridBagConstraints(2, i, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void updateSeriesPanel() {
        if (seriesPanel != null) {
            synchronized (rightPanel) {
                rightPanel.remove(seriesPanel);
            }
        }
        seriesPanel = new JPanel();
        seriesPanel.setLayout(new GridBagLayout());

        DataSeriesPropertySet prop = projectItem.getSeriesProperties();

        if (prop.size() > 0) {
            int width = 300; // controlPanel.getWidth() - 22;
            JLabel label = new JLabel();
            label.setText("Series");
            seriesPanel.add(label, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                    GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(width, getHeight() - 300));
            JPanel seriesNamesPanel = new JPanel();
            seriesNamesPanel.setLayout(new GridBagLayout());
            seriesPanel.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                    GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
            JPanel innerPanel = new JPanel(new BorderLayout());
            innerPanel.add(seriesNamesPanel, BorderLayout.NORTH);
            scrollPane.getViewport().setView(innerPanel);

            for (int i = 0; i < prop.size(); i++) {
                addSeriesButton(seriesNamesPanel, prop.getDataProperties(i), i, width);
            }

            final JButton butAllEnable = new JButton();
            butAllEnable.setText("show all");
            butAllEnable.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    seriesEnableButtonPressed(true);
                }
            });
            seriesNamesPanel.add(butAllEnable, new GridBagConstraints(0, prop.size() + 10, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            final JButton butAllDisable = new JButton();
            butAllDisable.setText("hide all");
            butAllDisable.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    seriesEnableButtonPressed(false);
                }
            });
            seriesNamesPanel.add(butAllDisable, new GridBagConstraints(0, prop.size() + 11, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }

        rightPanel.add(seriesPanel, BorderLayout.CENTER);

    }

    protected void graphAction(GraphPanel.GraphAction action) {
        synchronized (graphRootPanel) {
            if (graphPanel == null) {
                return;
            }
            switch (action) {
            case begin:
                graphPanel.setZoomStart(0);
                break;
            case backward:
                graphPanel.scrollZoom(-1 * graphPanel.getZoomWidth());
                break;
            case backwardslow:
                graphPanel.scrollZoom(-1 * (graphPanel.getZoomWidth() / 10));
                break;
            case forwardslow:
                graphPanel.scrollZoom(graphPanel.getZoomWidth() / 10);
                break;
            case forward:
                graphPanel.scrollZoom(graphPanel.getZoomWidth());
                break;
            case end:
                graphPanel.setZoomEnd(Long.MAX_VALUE);
                break;
            case zoomin:
                graphPanel.setZoomWidth(graphPanel.getZoomWidth() / 2);
                break;
            case zoomout:
                graphPanel.setZoomWidth(graphPanel.getZoomWidth() * 2);
                break;
            case zoomreset:
                projectItem.unsetScaleMaxX();
                projectItem.unsetScaleMinX();
                break;
            }
        }
        updateDisplay();
    }

    protected JPanel getPdaLogo() {
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setHorizontalTextPosition(SwingConstants.CENTER);
        final MainFrame _this = this;
        logo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                AboutDialog dlg = new AboutDialog(_this);
                dlg.showDialog();
            }
        });
        BaseDialog.setIcon(logo, "pdav2_400copy.png");

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(logo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        JLabel version = new JLabel(Main.VERSION);
        panel.add(version, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        version.setHorizontalAlignment(SwingConstants.CENTER);
        version.setHorizontalTextPosition(SwingConstants.CENTER);

        return panel;
    }

    private void menuProjectOpen_actionPerformed(ActionEvent e) {
        String fname = BaseDialog.openFileDlg(this, "Open Project", "PDA Project", "pda",
                Main.config.getWorkdirProject());
        if (fname == null)
            return;
        openProject(fname);
    }

    public Project createNewProject() {
        project = new Project();
        project.addProjectItem(new ProjectItem(ProjectItem.Type.graph));
        projectItem = project.getProjectItem(0);
        return project;
    }

    public void openProject(String fname) {
        project = new Project();
        Main.config.setWorkdirProject(Util.getPathOfFile(fname));

        project.setFileName(fname);
        if (project.loadFromFile()) {
            if (project.size() == 0) {
                project.addProjectItem(new ProjectItem(ProjectItem.Type.graph));
            }
            projectItem = project.getProjectItem(0);
            updateTitle();
            updateDisplay();
            setLabels();
        } else {
            BaseDialog.errorDlg(this, "Loading failed.");
        }

    }

    public void openProject(Project p) {
        project = p;
        if (p.getFileName() != null) {
            Main.config.setWorkdirProject(Util.getPathOfFile(p.getFileName()));
        }

        if (project.loadFromFile()) {
            if (project.size() == 0) {
                project.addProjectItem(new ProjectItem(ProjectItem.Type.graph));
            }
            projectItem = project.getProjectItem(0);
            updateTitle();
            updateDisplay();
            setLabels();
        } else {
            BaseDialog.errorDlg(this, "Loading failed.");
        }

    }

    private void menuProjectSave_actionPerformed(ActionEvent e) {
        if (project != null) {
            String fname = project.getFileName();
            if (fname == null) {
                menuProjectSaveAs_actionPerformed(e);
            } else {
                project.setLabels((graphPanel != null ? graphPanel.getAllLabels() : null));
                if (!project.saveToFile()) {
                    BaseDialog.errorDlg(this, "Saving failed.");
                }
            }
        }
    }

    private void menuProjectSaveAs_actionPerformed(ActionEvent e) {
        if (project != null) {
            String fname = BaseDialog.saveFileDlg(this, "Save Project", "PDA Project", "pda",
                    Main.config.getWorkdirProject());
            if (fname == null) {
                return;
            }
            if (fname.indexOf(".") < 0) {
                fname += ".pda";
            }
            Main.config.setWorkdirProject(Util.getPathOfFile(fname));

            project.setFileName(fname);
            project.setLabels((graphPanel != null ? graphPanel.getAllLabels() : null));
            if (!project.saveToFile()) {
                BaseDialog.errorDlg(this, "Saving failed.");
            }
            updateTitle();
        }
    }

    private void menuProjectChangeDir_actionPerformed(ActionEvent e) {
        if (project != null) {
            String oldDir = Util.getPathOfFile(project.getRandomParserFileName());
            ChangeDirDialog dlg = new ChangeDirDialog(this, oldDir);
            if (dlg.showDialog()) {
                oldDir = dlg.getOldDir();
                String newDir = dlg.getNewDir();
                if (newDir != null) {
                    int count = project.replaceDirectory(oldDir, newDir);
                    BaseDialog.infoDlg(this, count + " filenames changed.");
                    updateDisplay();
                }
            }
        }
    }

    private void menuProjectClose_actionPerformed(ActionEvent e) {
        createNewProject();
        updateTitle();
        updateDisplay();
    }

    private void menuProjectExit_actionPerformed(ActionEvent e) {
        Main.exit();
    }

    private void menuConfigurationSettings_actionPerformed(ActionEvent e) {
        SettingsDialog dlg = new SettingsDialog(this);
        dlg.showDialog();
        updateDisplay();
    }

    private void menuHelpAbout_actionPerformed(ActionEvent e) {
        AboutDialog dlg = new AboutDialog(this);
        dlg.showDialog();
    }

}

class ValidateThread extends Thread {
    JFrame frame;

    public ValidateThread(JFrame frame) {
        this.frame = frame;
    }

    public void run() {
        try {
            Thread.sleep(200);
            frame.validate();
        } catch (Exception e) {
        }
    }
}