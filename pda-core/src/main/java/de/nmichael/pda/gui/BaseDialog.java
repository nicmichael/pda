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

import de.nmichael.pda.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JDialog;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public abstract class BaseDialog extends JDialog {

    public enum CloseButton {

        ok,
        close
    }
    public static final int YES = 1;
    public static final int NO = 2;
    public static final int CANCEL = 3;
    public static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    public static final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";

    protected Window parent;
    private JPanel contentPane;
    protected JPanel mainPanel;
    protected JButton okButton;
    private boolean result = false;
    private Component focusComponent;

    public BaseDialog(Window parent, String title) {
        super(parent);
        initializeBaseDialog(parent, title, CloseButton.ok);
    }

    public BaseDialog(Window parent, String title, CloseButton closeButton) {
        super(parent);
        initializeBaseDialog(parent, title, closeButton);
    }

    private void initializeBaseDialog(Window parent, String title, CloseButton closeButton) {
        this.parent = parent;
        setTitle(title);
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Main Panel
        mainPanel = new JPanel();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        
        // Key Action
        final BaseDialog _this = this;
        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                _this.dispatchEvent(new WindowEvent(
                        _this, WindowEvent.WINDOW_CLOSING));
            }
        };
        JRootPane root = _this.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                escapeStroke, dispatchWindowClosingActionMapKey);
        root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);
        
        // OK Button
        okButton = new JButton();
        if (closeButton == CloseButton.ok) {
            okButton.setText("OK");
            okButton.setMnemonic('o');
            setIcon(okButton, "apply.png");
        } else {
            okButton.setText("Close");
            okButton.setMnemonic('c');
            setIcon(okButton, "close.png");
        }
        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                closeWindow(true);
            }
        });
        JPanel okPanel = new JPanel();
        okPanel.setLayout(new BorderLayout());
        okPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        okPanel.add(okButton, BorderLayout.CENTER);

        contentPane.add(okPanel, BorderLayout.SOUTH);
    }

    protected abstract void initialize();

    protected void setLabel(JLabel label, String text, char mnemonic, JComponent labelFor) {
        label.setText(text);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(labelFor);
    }

    public boolean showDialog() {
        initialize();
        pack();
        setLocation(getLocation(getSize()));
        setModal(true);
        if (focusComponent != null) {
            focusComponent.requestFocus();
        }
        show();
        return result;
    }

    private static Point getLocation(Dimension dlgSize) {
        int x, y;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (dlgSize.height > screenSize.height) {
            dlgSize.height = screenSize.height;
        }
        if (dlgSize.width > screenSize.width) {
            dlgSize.width = screenSize.width;
        }
        x = (screenSize.width - dlgSize.width) / 2;
        y = (screenSize.height - dlgSize.height) / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return new Point(x, y);
    }

    protected void closeWindow(boolean okButton) {
        if (okButton) {
            result = true;
        }
        dispose();
    }

    public static void setIcon(Window window, String name) {
        try {
            window.setIconImage(Toolkit.getDefaultToolkit().createImage(BaseDialog.class.getResource("/de/nmichael/pda/gui/img/" + name)));
        } catch (Exception e) {
        }
    }

    public static void setIcon(AbstractButton button, String name) {
        setIcon(button, name, true);
    }

    public static void setIcon(AbstractButton button, String name, boolean centered) {
        try {
            button.setIcon(new ImageIcon(BaseDialog.class.getResource("/de/nmichael/pda/gui/img/" + name)));
            if (!centered) {
                button.setIconTextGap(10);
                button.setHorizontalAlignment(SwingConstants.LEFT);
            }
        } catch (Exception e) {
        }
    }

    public static void setIcon(JLabel label, String name) {
        try {
            label.setIcon(new ImageIcon(BaseDialog.class.getResource("/de/nmichael/pda/gui/img/" + name)));
        } catch (Exception e) {
        }
    }

    public static void setLink(final JLabel label, final String url) {
        String linktxt = url;
        if (linktxt.startsWith("http://")) {
            linktxt = linktxt.substring(7);
        }
        label.setText(linktxt);
        label.setForeground(Color.blue);
        label.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                openBrowser(url);
            }

            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.red);
            }

            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.blue);
            }
        });
    }
    
    protected void setRequestFocus(Component focusComponent) {
        this.focusComponent = focusComponent;
    }

    public static void openBrowser(String url) {
        try {
            String browser = null;
            String[] browsers = new String[]{
                "/usr/bin/firefox"
            };
            for (String b : browsers) {
                if ((new File(b)).exists()) {
                    browser = b;
                    break;
                }
            }
            if (browser != null) {
                ProcessBuilder pb = new ProcessBuilder(browser, url);
                pb.start();
            }
        } catch (Exception e) {
        }
    }

    public static void infoDlg(Window frame, String s) {
        JOptionPane.showConfirmDialog(frame, s, "Info", -1);
    }

    public static void errorDlg(Window frame, String s) {
        JOptionPane.showConfirmDialog(frame, s, "Error", -1);
    }

    public static int yesNoDialog(Window frame, String title, String s) {
        if (JOptionPane.showConfirmDialog(frame, s, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return YES;
        } else {
            return NO;
        }
    }

    public static int selectOptionDialog(Window frame, String title, String text, String opt1, String opt2) {
        String options[] = new String[]{opt1, opt2};
        return JOptionPane.showOptionDialog(frame, text, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    }

    public static String inputDialog(Window frame, String title, String initialValue) {
        return JOptionPane.showInputDialog(frame, title, initialValue);
    }

    private static String fileDlg(Window frame, String titel, String type, String ext, String startdir, boolean save, boolean dirs) {
        try {
            JFileChooser dlg;
            if (startdir != null) {
                dlg = new JFileChooser(startdir);
            } else {
                dlg = new JFileChooser();
            }
            if (type != null && ext != null) {
                dlg.setFileFilter((javax.swing.filechooser.FileFilter) new PDAFileFilter(type, ext));
            }
            if (titel != null) {
                dlg.setDialogTitle(titel);
            }
            if (dirs) {
               dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            int ret;
            dlg.setPreferredSize(new Dimension(950, 500));
            if (save) {
                ret = dlg.showSaveDialog(frame);
            } else {
                ret = dlg.showOpenDialog(frame);
            }
            if (ret == JFileChooser.APPROVE_OPTION) {
                return dlg.getSelectedFile().toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveFileDlg(Window frame, String titel, String type, String ext, String startdir) {
        return fileDlg(frame, titel, type, ext, startdir, true, false);
    }

    public static String openFileDlg(Window frame, String titel, String type, String ext, String startdir) {
        return fileDlg(frame, titel, type, ext, startdir, false, false);
    }
    
    public static String openDirDlg(Window frame, String titel, String type, String ext, String startdir) {
        return fileDlg(frame, titel, type, ext, startdir, false, true);
    }
    
    public static String choseColor(Window parent, String colorTxt) {
        Color color = Util.getColor(colorTxt);
        color = JColorChooser.showDialog(parent, "Chose Color", color);
        if (color != null) {
            return Util.getColor(color);
        } else {
            return colorTxt;
        }
    }

    static class PDAFileFilter extends javax.swing.filechooser.FileFilter {

        String description = "";
        String ext1 = "";

        public PDAFileFilter(String descr, String ext) {
            description = descr;
            ext1 = ext.toUpperCase();
        }

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toUpperCase().endsWith(ext1);
        }

        public String getDescription() {
            return description;
        }
    }
}