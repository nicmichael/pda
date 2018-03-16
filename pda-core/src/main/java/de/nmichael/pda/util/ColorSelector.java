/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.util;

import java.awt.*;

public class ColorSelector {
    
    private static final int MAX_COLORS = 12;
    private Color[] colors = new Color[MAX_COLORS];
    private int cnt;
    
    /** Creates a new instance of ColorSelector */
    public ColorSelector() {
            for (int i=0; i<MAX_COLORS; i++) {
                switch(i) {
                    case  0: colors[i] = new Color(255,0,0); break;   // red
                    case  1: colors[i] = new Color(0,0,255); break;   // blue
                    case  2: colors[i] = new Color(0,255,0); break;   // green
                    case  3: colors[i] = new Color(255,0,255); break; // magenta
                    case  4: colors[i] = new Color(0,255,255); break; // cyan
                    case  5: colors[i] = new Color(128,128,0); break; // brown
                    case  6: colors[i] = new Color(0,0,128); break;   // dark blue
                    case  7: colors[i] = new Color(128,0,0); break;   // dark red
                    case  8: colors[i] = new Color(0,128,0); break;   // dark green
                    case  9: colors[i] = new Color(128,0,128); break; // dark magenta
                    case 10: colors[i] = new Color(0,128,128); break; // dark cyan
                    case 11: colors[i] = new Color(192,0,0); break;   // red 3
                    case 12: colors[i] = new Color(0,0,192); break;   // blue 4
                    case 13: colors[i] = new Color(0,192,0); break;   // green 3
                    case 14: colors[i] = new Color(192,0,192); break; // magenta 3
                    case 15: colors[i] = new Color(0,192,192); break; // cyan 3
                    case 16: colors[i] = new Color(64,64,0); break;   // brown 3
                    case 17: colors[i] = new Color(0,0,64); break;    // dark blue 4
                    case 18: colors[i] = new Color(64,0,0); break;    // dark red 4
                    case 19: colors[i] = new Color(0,64,0); break;    // dark green 4
                    case 20: colors[i] = new Color(64,0,64); break;   // dark magenta 4
                    case 21: colors[i] = new Color(0,64,64); break;   // dark cyan 4
                    default: colors[i] = Color.black;
                }
            }
            reset();
    }
    
    public Color getColor(int idx) {
        return colors[idx % colors.length];
    }
    
    public void gotoColor(int idx) {
        cnt = idx;
    }
    
    public void reset() {
        cnt = 0;
    }
    
    public Color getNextColor() {
        return getColor(cnt++);
    }
    
}
