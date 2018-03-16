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
import java.awt.print.*;
import java.io.*;
import javax.swing.JLayeredPane;

public class SimpleFilePrinter implements Printable {

  private static final int    DPI         = 72;         // Referenzaufl�sung des Drucksystems
  private static final double CM          = DPI / 2.54; // Dots pro Zentimeter
  private static final double MM          = DPI / 25.4; // Dots pro Millimeter
  private static final double MARGIN_TOP  = 0.0 * CM;   // zus�tzlicher Seitenrand oben

  // folgende Werte werden im Konstruktor gesetzt (hier nur Beispielwerte)
  private static       double OVERLAP     = 5 * MM;     // �berlappung bei mehrseitigen Dokumenten
  private static       double PAGE_WIDTH  = 595.0;      // Seitenbreite (total)
  private static       double PAGE_HEIGHT = 842.0;      // Seitenh�he (total)
  private static       double PAGE_X      =  17.0;      // linker Rand
  private static       double PAGE_Y      =  22.0;      // oberer Rand
  private static       double PAGE_W      = 560.0;      // Seitenbreite (nutzbar)
  private static       double PAGE_H      = 797.0;      // Seitenh�he (nutzbar)

  private PrinterJob        pjob;           // Printjob
  private PageFormat        pageformat;     // Seitenformat
  protected  static PageFormat pageSetup=null; // Seitenformat-Setup
  private JLayeredPane       out;            // HTML-Ausgabe

  public SimpleFilePrinter(JLayeredPane out) {
    this.pjob  = PrinterJob.getPrinterJob();
    this.out   = out;

    // Seitenlayout
    /*
    if (Daten.efaConfig != null) {
      this.PAGE_WIDTH  = ((double)Daten.efaConfig.printPageWidth) * MM;
      this.PAGE_HEIGHT = ((double)Daten.efaConfig.printPageHeight) * MM;
      this.PAGE_X      = ((double)Daten.efaConfig.printLeftMargin) * MM;
      this.PAGE_Y      = ((double)Daten.efaConfig.printTopMargin) * MM;
      this.PAGE_W      = PAGE_WIDTH  - 2 * PAGE_X;
      this.PAGE_H      = PAGE_HEIGHT - 2 * PAGE_Y;
      this.OVERLAP     = ((double)Daten.efaConfig.printPageOverlap) * MM;
    }
    */
  }

  public boolean setupPageFormat() {
    PageFormat defaultPF = pjob.defaultPage();
    if (pageSetup == null) {
      this.pageformat = (PageFormat)defaultPF.clone();
      Paper p = new Paper();
      p.setSize(PAGE_WIDTH,PAGE_HEIGHT);
      p.setImageableArea(PAGE_X, PAGE_Y, PAGE_W, PAGE_H);
      this.pageformat.setPaper(p);
    } else this.pageformat = pageSetup;

    pjob.setPrintable(this, this.pageformat);

    return true;
  }

  public boolean setupJobOptions() {
    return pjob.printDialog();
  }

  public void printFile() throws PrinterException {
    pjob.print();
  }



  //---Implementierung von Printable-------------------
  public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
    if (page > 0) return NO_SUCH_PAGE;

    double scaleX = (double)out.getWidth() / pf.getImageableWidth();
    double scaleY = (double)out.getHeight() / pf.getImageableHeight();
    double scale = Math.max(scaleX,scaleY);

    int offset = (int)((pf.getImageableHeight()-MARGIN_TOP-OVERLAP)*scale*page);

    System.out.println("Printing page "+(page+1)+"...");
    System.out.println("PageMargin: "+pf.getImageableX()+" ; "+pf.getImageableY());
    System.out.println("PageUsable: "+pf.getImageableWidth()+" x "+pf.getImageableHeight());
    System.out.println("PageSize  : "+pf.getWidth()+" x "+pf.getHeight());
    System.out.println("Document  : "+out.getWidth()+" x "+out.getHeight());

    Graphics2D g2 = (Graphics2D)g;
    g2.scale(1.0/scale, 1.0/scale);
    g.translate((int)(pf.getImageableX()*scale),(int)((pf.getImageableY()+MARGIN_TOP)*scale)-offset);
    out.paint(g);

    return PAGE_EXISTS;
  }

}