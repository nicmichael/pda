/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.out;

import java.util.*;
import java.io.*;

public class Table {
    
    public static final int MAX_ROWS = 1000;
    
    private String title = null;
    private String[] header;
    private String[][] data;
    private int lastRow, currentRow, currentColumn;
    private int repeatHeaderEachNRows = 0;
    
    public Table(String[] header) {
        this.header = header;
        data = new String[MAX_ROWS][header.length];
        lastRow = -1;
        currentRow = -1;
        currentColumn = -1;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean addRow() {
        if (lastRow+1 >= MAX_ROWS) return false;
        lastRow++;
        currentRow++;
        for (int i=0; i<data[currentRow].length; i++) {
            data[currentRow][i] = "";
        }
        currentColumn = 0;
        return true;
    }
    
    public boolean addField(String s) {
        if (currentRow < 0) return false;
        if (currentColumn+1 > data[currentRow].length) return false;
        data[currentRow][currentColumn++] = s;
        return true;
    }
    
    public void setRepeatHeaderEachNRows(int n) {
        repeatHeaderEachNRows = n;
    }
    
    private void writeHeader(BufferedWriter f) throws Exception{
        f.write("  <tr>\n");
        for (int i=0; i<header.length; i++) f.write("    <th>"+header[i]+"</th>\n");
        f.write("  </tr>\n");
    }
    
    public boolean printToFile(BufferedWriter f) {
        try {
            f.write("<br>\n");
            f.write("<table border align=\"center\">\n");
            
            if (title != null) {
                f.write("<tr><th colspan=\""+header.length+"\" bgcolor=\"#00ffff\">"+title+"</th></tr>\n");
            }
            
            if (repeatHeaderEachNRows == 0) {
                writeHeader(f);
            }
            
            for (int x=0; x<data.length && x<=lastRow; x++) {
                if (repeatHeaderEachNRows > 0 && x % repeatHeaderEachNRows == 0) {
                    writeHeader(f);
                }

                f.write("  <tr>\n");
                for (int y=0; y<data[x].length; y++) {
                    if (data[x][y].length() > 0) {
                        f.write("    <td>"+data[x][y]+"</td>\n");
                    } else {
                        f.write("    <td>&nbsp;</td>\n");
                    }
                }
                f.write("  </tr>\n");
            }

            f.write("</table>\n");
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    
}
