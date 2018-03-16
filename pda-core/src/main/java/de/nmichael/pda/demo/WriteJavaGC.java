/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class WriteJavaGC {
    
    // -Xmx150M -XX:NewSize=20M -XX:MaxNewSize=20M -XX:SurvivorRatio=2 -XX:TargetSurvivorRatio=90 -XX:+UseParallelOldGC -XX:MaxTenuringThreshold=15 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:drawgc.log -verbose:gc
    public static final long EDEN =     10*1024*1024;
    public static final long SURVIVOR =  5*1024*1024;
    public static final long OLD      = 80*1024*1024;
    public static final long HEAP     = EDEN + 2*SURVIVOR + OLD;

    public static final long MAXX = 100;
    public static final long MAXY = 100;
    public static       long RUNTIME = 10 * 60 * 1000;

    public static final int  CHUNK    =  1*1024*1024;

    long tStart, tEnd;
    Hashtable<Long,byte[]> memory = new Hashtable<Long,byte[]>();
    long usedChunks = 0;
    long key = 0;
    BufferedWriter f;

    public WriteJavaGC(BufferedWriter f) {
        tStart = System.currentTimeMillis();
        tEnd = tStart + RUNTIME;
        this.f = f;
    }

    private long getX(long x) {
        return tStart + ((x * RUNTIME) / MAXX);
    }

    private long getY(long y) {
        return ((y * HEAP) / MAXY);
    }

    private long getM(long tx1, long my1, long tx2, long my2, long t) {
        return (((t - tx1) * (my2 - my1)) / (tx2 - tx1)) + my1;
    }

    private void ensureMemory(long mem) {
        long numberOfChunks = mem / CHUNK;
        Long[] keys = memory.keySet().toArray(new Long[0]);
        while (usedChunks < numberOfChunks) {
            byte[] b = new byte[CHUNK];
            memory.put(new Long(key++), b);
            usedChunks++;
        }
        int i=0;
        while (usedChunks > numberOfChunks) {
            memory.remove(new Long(keys[i++]));
            usedChunks--;
        }
    }

    private void triggerNewGC() {
        int edenChunks = ((int)EDEN/CHUNK);
        for(int i=0; i<=edenChunks; i++) {
            byte[] b = new byte[CHUNK];
        }
    }

    private void triggerOldGC() {
        int edenChunks = ((int)EDEN/CHUNK);
        int heapChunks = ((int)HEAP/CHUNK);
        byte[][]keep = new byte[edenChunks][];
        for(int i=0; i<=heapChunks; i++) {
            byte[] b = new byte[CHUNK];
            keep[i % edenChunks] = b;
        }
    }

    public void drawLine(long x1, long y1, long x2, long y2, boolean toggleBetweenY1Y2) {
        long tx1 = getX(x1);
        long tx2 = getX(x2);
        long my1 = getY(y1);
        long my2 = getY(y2);

        while(System.currentTimeMillis() < tx1) {
        }
        long tNow;
        int i=0;
        Calendar cal = new GregorianCalendar();
        do {
            tNow = System.currentTimeMillis();
            long mNow = getM(tx1,my1,tx2,my2,tNow);
            if (toggleBetweenY1Y2) {
                if (i % 2 == 0) {
                    mNow = my1;
                } else {
                    mNow = my2;
                }
            }
            if (f != null) {
                try {
                    cal.setTimeInMillis(tNow);
                    f.write(String.format("%04d-%02d-%02d-%02d:%02d:%02d.%03d;%d\n", 
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            cal.get(Calendar.SECOND),
                            cal.get(Calendar.MILLISECOND),
                            mNow));
                } catch(Exception e) {
                    e.printStackTrace();
                    // ignore
                }
            }
            //System.out.println("t="+tNow+"  m="+mNow);
            ensureMemory(mNow);
            triggerOldGC();
            i++;
        } while(tNow < tx2);
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                RUNTIME = Long.parseLong(args[0]) * 60 * 1000;
            } catch(Exception e) {
                System.err.println("usage: WriteJavaGC [runtime in minutes]");
                System.exit(1);
            }
        }
        System.out.println("Running for " + (RUNTIME / 60000) + " minutes ...");
        
        String logfile = (args.length > 1 ? args[1] : null);
        BufferedWriter f = null;
        try {
            f = new BufferedWriter(new FileWriter(logfile));
            f.write("Time;Samples\n");
        } catch(Exception e) {
            f = null;
        }

        WriteJavaGC demo = new WriteJavaGC(f);

        // J
        demo.drawLine(0,40,5,20,false);
        demo.drawLine(5,20,10,20,false);
        demo.drawLine(10,20,15,40,false);
        demo.drawLine(15,40,19,100,true);

        // A
        demo.drawLine(25,20,35,100,false);
        demo.drawLine(35,100,45,20,false);

        // V
        demo.drawLine(50,100,60,20,false);
        demo.drawLine(60,20,70,100,false);

        // A
        demo.drawLine(75,20,85,100,false);
        demo.drawLine(85,100,95,20,false);

        if (f != null) try {
            f.close();
        } catch(Exception e) {
            // ignore
        }
        System.out.println("Done.");
    }

}
