/**
 * Title:        Performance Data Analyzer (PDA)
 * Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
 * Website:      http://pda.nmichael.de/
 * License:      GNU General Public License v2
 *
 * @author Nicolas Michael
 * @version 2
 */

package de.nmichael.pda.data;

import de.nmichael.pda.gui.BaseDialog;
import de.nmichael.pda.util.ColorSelector;
import de.nmichael.pda.util.Util;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

public class ProjectFile extends DefaultHandler {

    public static final String ENCODING               = "UTF-8";
    public static final String XML_PROJECT            = "PDAProject";
    public static final String XML_HTML_OUTPUT        = "HtmlOutput";
    public static final String XML_IMAGE_SIZE         = "ImageSize";
    public static final String XML_IMAGE_WIDTH        = "Width";
    public static final String XML_IMAGE_HEIGHT       = "Height";
    public static final String XML_LABELS             = "Labels";

    private Project project;
    private ProjectItem projectItem;
    private DataLabel dataLabel;
    private String paramName;
    private DataSeriesProperties dataSeries;
    private DataSeriesGroup dataGroup;
    private DataTable dataTable;
    private Locator locator;
    private Stack<String> parentElements = new Stack<String>();
    private String fieldValue;
    private boolean documentComplete = false;
    private ColorSelector colorSelector = new ColorSelector();

    public ProjectFile(Project project) {
        this.project = project;
    }
    
    public boolean saveToFile() {
        if (project.getFileName() == null) {
            return false;
        }
        try {
            BufferedWriter f = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(project.getFileName()), ENCODING));
            writeln(f, "<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>\n");
            XMLHelper.reset();
            writeln(f, XMLHelper.xmlTagStart(XML_PROJECT));
            
            // HTML Output
            if (project.getHtmlOutputFile() != null && project.getHtmlOutputFile().length() > 0) {
                writeln(f, XMLHelper.xmlTag(XML_HTML_OUTPUT, project.getHtmlOutputFile()));
            }
            
            // Image Size
            writeln(f, XMLHelper.xmlTagStart(XML_IMAGE_SIZE));
            writeln(f, XMLHelper.xmlTag(XML_IMAGE_WIDTH, Integer.toString(project.getPngWidth())));
            writeln(f, XMLHelper.xmlTag(XML_IMAGE_HEIGHT, Integer.toString(project.getPngHeight())));
            writeln(f, XMLHelper.xmlTagEnd(XML_IMAGE_SIZE));
            
            // Labels
            if (project.getLabelCount() > 0) {
                writeln(f, XMLHelper.xmlTagStart(XML_LABELS));
                for (int j = 0; j < project.getLabelCount(); j++) {
                    DataLabel l = project.getLabel(j);
                    writeln(f, l.toString());
                }
                writeln(f, XMLHelper.xmlTagEnd(XML_LABELS));
            }

            for (int i=0; i<project.size(); i++) {
                ProjectItem item = project.getProjectItem(i);
                item.setBaseFileName(project.getFileName());
                writeln(f, item.toString());
            }
            
            writeln(f, XMLHelper.xmlTagEnd(XML_PROJECT));
            f.close();
        } catch(Exception e) {
            e.printStackTrace();
            BaseDialog.errorDlg(null, e.getMessage());
            return false;
        }
        return true;
    }
    
    public boolean loadFromFile() {
        if (project.getFileName() == null) {
            return false;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(project.getFileName());
            Element e = doc.getDocumentElement();
            if (!XML_PROJECT.equals(e.getNodeName())) {
                throw new Exception("This does not look like a PDA Project File.");
            }
            NodeList nl = e.getChildNodes();
            for (int i=0; nl != null && i<nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    e = (Element)n;
                    if (XML_HTML_OUTPUT.equals(e.getNodeName())) {
                        project.setHtmlOutputFile(e.getTextContent().trim());
                        continue;
                    }
                    
                    if (XML_IMAGE_SIZE.equals(e.getNodeName())) {
                        Element ie = getChildNode(e, XML_IMAGE_WIDTH);
                        if (ie != null) {
                            project.setPngWidth(Util.string2int(ie.getTextContent().trim(), Project.PNG_WIDTH));
                        }
                        ie = getChildNode(e, XML_IMAGE_HEIGHT);
                        if (ie != null) {
                            project.setPngHeight(Util.string2int(ie.getTextContent().trim(), Project.PNG_HEIGHT));
                        }
                        continue;
                    }
                    
                    if (XML_LABELS.equals(e.getNodeName())) {
                        project.setLabels(DataLabel.restoreLabels(e).toArray(new DataLabel[0]));
                    }
                    
                    if (ProjectItem.XML_PROJECT_ITEM.equals(e.getNodeName())) {
                        ProjectItem pi = ProjectItem.restoreProjectItem(e, project.getFileName());
                        if (pi != null) {
                            project.addProjectItem(pi);
                        }
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            BaseDialog.errorDlg(null, "Error reading file '" + project.getFileName() + "': " + e.toString());
            return false;
        }
        return true;
    }
    
    private void writeln(BufferedWriter f, String s) throws IOException {
        f.write(s);
    }
    
    public static Element getChildNode(Element e, String name) {
        NodeList nl = e.getChildNodes();
        for (int i=0; nl != null && i<nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE &&
                name.equals(n.getNodeName())) {
                return (Element)n;
            }
        }
        return null;
    }
    
}
