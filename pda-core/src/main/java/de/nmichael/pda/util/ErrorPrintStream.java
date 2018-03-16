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

import java.io.*;
import de.nmichael.pda.gui.*;
import de.nmichael.pda.Main;

public class ErrorPrintStream extends PrintStream {
    
    public ErrorPrintStream() {
        super(System.out);
    }
    
    public void print(Object o) {
        if (o.getClass().toString().indexOf("Exception")>0 ||
                o.getClass().toString().indexOf("java.lang.NoSuchMethodError")>0 ||
                o.getClass().toString().indexOf("java.lang.NoClassDefFoundError")>0 ||
                o.getClass().toString().indexOf("java.lang.OutOfMemoryError")>0) {
            String text = o.toString();
            new ErrorMessageThread(o.toString()).start();
        }
        super.print(o);
    }
    public void print(String s) {
        super.print(s);
    }
    
    class ErrorMessageThread extends Thread {
        String message;
        ErrorMessageThread(String message) {
            this.message = message;
        }
        public void run() {
            BaseDialog.errorDlg(null,message);
        }
    }
    
}