/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda;

import de.nmichael.pda.util.Util;
import java.io.*;
import java.util.*;

public class ConfigFile {
    
    private static final String WORKDIR_DATA    = "WORKDIR_DATA";
    private static final String WORKDIR_PROJECT = "WORKDIR_PROJECT";
    private static final String LABEL_HEIGHT    = "LABEL_HEIGHT";
    
    private String filename;
    private String workdirData;
    private String workdirProject;
    private float labelHeight;
    
    public ConfigFile(String filename) {
        this.filename = filename;
    }
    
    private void findConfigFile() {
        filename = Main.HOMEDIR + ".pda.cfg";
    }
    
    public void readConfig() throws Exception {
        workdirData = null;
        workdirProject = null;
        
        if (filename == null) {
            findConfigFile();
        }
        BufferedReader f = new BufferedReader(new FileReader(filename));
        String s;
        int section = 0;
        while ( (s = f.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0 || s.startsWith("#")) continue;
            
            if (s.startsWith(WORKDIR_DATA + "=")) {
                workdirData = s.substring(WORKDIR_DATA.length() + 1);
                continue;
            }
            if (s.startsWith(WORKDIR_PROJECT + "=")) {
                workdirProject = s.substring(WORKDIR_PROJECT.length() + 1);
                continue;
            }
            if (s.startsWith(LABEL_HEIGHT + "=")) {
                labelHeight = Util.string2float(s.substring(LABEL_HEIGHT.length() + 1), 0.2f);
                continue;
            }
           
        }
        f.close();
    }
    
    public void writeConfig() throws Exception {
        BufferedWriter f = new BufferedWriter(new FileWriter(filename));
        if (workdirData != null) {
            f.write(WORKDIR_DATA + "="+workdirData+"\n");
        }
        if (workdirProject != null) {
            f.write(WORKDIR_PROJECT + "="+workdirProject+"\n");
        }
        if (!Float.isNaN(labelHeight)) {
            f.write(LABEL_HEIGHT + "="+Float.toString(labelHeight) +"\n");
        }

        f.close();
    }
    
    public String getWorkdirData() {
        return workdirData;
    }
    
    public void setWorkdirData(String workdir) {
        this.workdirData = workdir;
    }
    
    public String getWorkdirProject() {
        return workdirProject;
    }
    
    public void setWorkdirProject(String workdir) {
        this.workdirProject = workdir;
    }
    
    public float getLabelHeight() {
        if (Double.isNaN(labelHeight) || labelHeight < 0.1 ||
                labelHeight > 1) {
            return 0.2f;
        }
        return labelHeight;
    }
    
    public void setLabelHeight(float labelHeight) {
        if (Double.isNaN(labelHeight) || labelHeight < 0.1 ||
                labelHeight > 1) {
            labelHeight = 0.2f;
        }
        this.labelHeight = labelHeight;
    }
    
    
}
