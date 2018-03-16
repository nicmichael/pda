/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
* 
*/

package de.nmichael.pda.data;

import de.nmichael.pda.Logger;
import de.nmichael.pda.util.ColorSelector;
import java.util.*;
import java.io.*;
import de.nmichael.pda.util.Util;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class Project {
    
    public static final int PNG_WIDTH = 1024;
    public static final int PNG_HEIGHT = 768;
    
    private String filename;
    private String htmlOutputFile;
    private int pngWidth = PNG_WIDTH;
    private int pngHeight = PNG_HEIGHT;
    private ArrayList<ProjectItem> items = new ArrayList<ProjectItem>();
    private ArrayList<DataLabel> labels = new ArrayList<DataLabel>();
    
    public Project() {
    }
    
    public void setFileName(String filename) {
        this.filename = filename;
    }
    
    public String getFileName() {
        return filename;
    }
    
    public void setHtmlOutputFile(String filename) {
        this.htmlOutputFile = filename;
    }
    
    public String getHtmlOutputFile() {
        return htmlOutputFile;
    }
    
    public void setPngWidth(int width) {
        pngWidth = width;
    }
    
    public int getPngWidth() {
        return pngWidth;
    }
    
    public void setPngHeight(int height) {
        pngHeight = height;
    }
    
    public int getPngHeight() {
        return pngHeight;
    }
    
    public int size() {
        return items.size();
    }
    
    public void addProjectItem(ProjectItem item) {
        item.setBaseFileName(this.filename);
        items.add(item);
    }
    
    public ProjectItem getProjectItem(int i) {
        return (ProjectItem)items.get(i);
    }

    public void setLabels(DataLabel[] l) {
        labels = new ArrayList<DataLabel>();
        for (int i=0; l != null && i<l.length; i++) {
            labels.add(l[i]);
        }
    }
    
    public void addLabel(DataLabel l) {
        if (labels == null) {
            labels = new ArrayList<DataLabel>();
        }
        labels.add(l);
    }

    public int getLabelCount() {
        return labels.size();
    }

    public DataLabel getLabel(int i) {
        if (i<0 || i>=labels.size()) {
            return null;
        }
        return (DataLabel)labels.get(i);
    }
    
    public int replaceDirectory(String oldDir, String newDir) {
        int count = 0;
        for (int i=0; i<items.size(); i++) {
            ProjectItem pi = items.get(i);
            ParserSet ps = pi.getParsers();
            for (int j=0; ps != null && j<ps.size(); j++) {
                Parser p = ps.getParser(j);
                boolean changed = false;
                String fname = p.getFilename();
                if (fname.startsWith(oldDir)) {
                    String newFname = Util.replace(fname, oldDir, newDir);
                    if (!fname.equals(newFname)) {
                        p.setFilename(newFname);
                        count++;
                        changed = true;
                    }
                }
                if (changed) {
                    p.parse(true, true);
                }
            }
        }
        return count++;
    }
    
    public String getRandomParserFileName() {
        for (int i=0; i<items.size(); i++) {
            ProjectItem pi = items.get(i);
            ParserSet ps = pi.getParsers();
            for (int j=0; ps != null && j<ps.size(); j++) {
                Parser p = ps.getParser(j);
                return p.getFilename();
            }
        }
        return null;
    }
    
    public boolean loadFromFile() {
        ProjectFile file = new ProjectFile(this);
        return file.loadFromFile();
    } 
    
    public boolean saveToFile() {
        ProjectFile file = new ProjectFile(this);
        return file.saveToFile();
    }
    
}
