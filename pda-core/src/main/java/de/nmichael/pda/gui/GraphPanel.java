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

import de.nmichael.pda.Logger;
import de.nmichael.pda.Main;
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.*;
import gov.noaa.pmel.sgt.*;
import gov.noaa.pmel.util.*;
import gov.noaa.pmel.sgt.dm.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.beans.*;
import java.util.*;
import java.io.*;

public class GraphPanel extends gov.noaa.pmel.sgt.JPane {
    
    private static final int AXISLEFT = 0;
    private static final int AXISRIGHT = 1;
    private static final int MAX_YAXIS_CNT = 4;
    
    private static final float G_WIDTH = 12.0f;
    private static final float G_HEIGHT = 12.0f;
    private static final float G_MARGIN_LEFT = 0.5f;
    private static final float G_MARGIN_RIGHT = 0.5f;
    private static final float G_MARGIN_TOP = 0.5f;
    private static final float G_MARGIN_BOTTOM = 0.5f;
    private static final float G_LABEL_WIDTH = 1.5f;
    private static final int G_LABEL_FONTSIZE = 10;
    
    private MainFrame mainFrame;
    private ProjectItem projectItem;
    private float myWidth = 0;
    private float myHeight = 0;
    private float xyRatio = 0;
    private Vector<Layer> layers;
    private Hashtable<String,Layer> series2layer;
    private Hashtable<String,CartesianGraph> series2highlightedGraph = new Hashtable<String,CartesianGraph>();;
    private Vector labels;
    private double nextLabelPosition;
    private Hashtable graph2yUserRange;
    private Hashtable graph2xAxis;
    private Hashtable graph2yAxis;
    private Hashtable graph2yAxisOrientation;
    private int graphcnt;
    private int yaxiscnt;
    private SGLabel selectedLabel;
    private boolean hasSeriesLabels = false;
    private double maxMin2MaxRatio = 0;
    
    public enum GraphAction {
        begin,
        backward,
        backwardslow,
        forwardslow,
        forward,
        end,
        zoomreset,
        zoomin,
        zoomout
    }

    /** Creates a new instance of GraphPanel */
    public GraphPanel(MainFrame mainFrame, ProjectItem projectItem, int width, int height) {
        super("Performance Data",  new Dimension(width,height));
        this.mainFrame = mainFrame;
        this.projectItem = projectItem;
        this.graphcnt = 0;
        this.yaxiscnt = 0;
        this.labels = new Vector();
        this.setLayout(new StackedLayout());
        this.setBackground(Color.white);
        this.setOpaque(true);
        this.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e)  {
                panePropertyChange(e);
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                panelMouseClicked();
            }            
        });
    }
    
    public static double[] smoothCurve(double[] v1, int smooth) {
        double[] v2 = new double[v1.length];
        for (int i=0; i<v1.length; i++) {
            double sum = 0.0;
            int cnt = 0;
            for (int j=i-smooth+1; j<=i; j++) {
                if (j>=0) {
                    sum += v1[j];
                    cnt++;
                }
            }
            v2[i] = sum/cnt;
        }
        return v2;
    }
    
    public static double getArrayAverage(double[] v, int maxFieldToConsider) {
        double sum = 0.0;
        int cnt = 0;
        for (int i=0; i<v.length && i<=maxFieldToConsider; i++) {
            sum += v[i];
            cnt++;
        }
        return sum / cnt;
    }
    
    public void updateGraphPanel() {
        xyRatio = 0; // recalculate
        layers = new Vector<Layer>();
        series2layer = new Hashtable<String,Layer>();
        graph2yUserRange = new Hashtable();
        graph2xAxis = new Hashtable();
        graph2yAxis = new Hashtable();
        graph2yAxisOrientation = new Hashtable();
        nextLabelPosition = G_MARGIN_LEFT;

        maxMin2MaxRatio = findAndAdjustMinToMaxRatio(projectItem.getSeriesProperties());        
        
        DataSeriesPropertySet prop = projectItem.getSeriesProperties();
        for (int i = 0; i < prop.size(); i++) {
            DataSeriesProperties dp = prop.getDataProperties(i);
            if (!dp.isVisible()) {
                continue;
            }
            Layer l = createLayer(dp);
            if (l != null) {
                this.add(l);
            }
        }
        
        if (maxMin2MaxRatio > 0 && graphcnt > 0) {
            Layer l = createNullLayer();
            if (l != null) {
                this.add(l);
            }
        }
        
        if (layers.size() == 0) {
            add(mainFrame.getPdaLogo());
        }
    }
    
    private double findAndAdjustMinToMaxRatio(DataSeriesPropertySet prop) {
        // find all series with negative values and determine the maximum min:max ratio
        double min2max = 0;
        for (int i = 0; i < prop.size(); i++) {
            DataSeriesProperties dp = prop.getDataProperties(i);
            double scaleMin = dp.getScaleMin();
            double scaleMax = dp.getScaleMax();
            if (scaleMin >= 0 || scaleMax <= 0) {
                continue;
            }
            min2max = Math.max(Math.abs(scaleMin) / scaleMax, min2max);
        }
        return min2max;
    }
    
    private Layer createLayer(DataSeriesProperties prop) {
        String name = prop.getDisplayName();
        
        if (xyRatio == 0) {
            xyRatio = ((float)getWidth())/((float)getHeight());
            myWidth = G_WIDTH * xyRatio;
            myHeight = G_HEIGHT;
        }
        final Layer layer = new Layer(name, new Dimension2D(myWidth, myHeight));
        double scaleMin = prop.getScaleMin();
        double scaleMax = prop.getScaleMax();
        if (scaleMax <= scaleMin) {
            scaleMax = scaleMin + 1;
        }
        if (scaleMin < 0 && scaleMax > 0) {
            double myMin2MaxRatio = Math.abs(scaleMin) / scaleMax;
            if (myMin2MaxRatio != maxMin2MaxRatio) {
                scaleMin = -1 * maxMin2MaxRatio * scaleMax;
            }
        }
        Logger.log(Logger.LogType.debug, "createLayer("+prop.getName()+"): xyRatio=" + xyRatio + ", myWidth=" + myWidth + ", myHeight=" + myHeight + ", scaleMin=" + scaleMin + ", scaleMax=" + scaleMax);
        CartesianGraph g = createGraph(layer, prop, name, scaleMin, scaleMax);
        if (g == null) {
            Logger.log(Logger.LogType.debug, "createLayer("+prop.getName()+") - skipped (no graph created)");
            return null;
        }
        layer.setGraph(g);
        
        SGLabel label_name = new SGLabel(name, name, 
                new Point2D.Double(nextLabelPosition, myHeight - Main.config.getLabelHeight()));
        label_name.setAlign(SGLabel.BOTTOM, SGLabel.LEFT);
        label_name.setColor(prop.getColor());
        label_name.setHeightP(Main.config.getLabelHeight());
        label_name.setFont(new Font("Dialog", Font.PLAIN, G_LABEL_FONTSIZE));
        label_name.setSelectable(false);
        layer.addChild(label_name);
        
        SGLabel label_range = new SGLabel(name, 
                "[" + Util.double2string(scaleMin, 0, true) +":"+ Util.double2string(scaleMax, 0, true) +"]", 
                new Point2D.Double(nextLabelPosition, myHeight - 2*Main.config.getLabelHeight()));
        label_range.setAlign(SGLabel.BOTTOM, SGLabel.LEFT);
        label_range.setColor(prop.getColor());
        label_range.setHeightP(Main.config.getLabelHeight());
        label_range.setFont(new Font("Dialog", Font.PLAIN, G_LABEL_FONTSIZE));
        label_range.setSelectable(false);
        layer.addChild(label_range);
        
        double scaleWidth = (name.length() < 10 ? 0.6 : (name.length() > 20 ? 0.45 : 0.5));
        double addWidth = Math.max(((double)name.length()) * scaleWidth * Main.config.getLabelHeight(), 
                4.5 * Main.config.getLabelHeight());
        nextLabelPosition += addWidth; // rect.width; // G_MARGIN_LEFT + (graphcnt * G_LABEL_WIDTH)

        if (graphcnt == 0) {
            SGLabel label = new SGLabel(name, projectItem.getName(), new Point2D.Double(myWidth / 2, myHeight - 4*Main.config.getLabelHeight()));
            label.setAlign(SGLabel.BOTTOM, SGLabel.CENTER);
            label.setColor(Color.black);
            label.setHeightP(2*Main.config.getLabelHeight());
            label.setFont(new Font("Dialog", Font.BOLD, 2*G_LABEL_FONTSIZE));
            label.setSelectable(false);
            layer.addChild(label);
        }
        
        graphcnt++;
        
        layers.add(layer);
        series2layer.put(prop.getName(), layer);
        
        return layer;
    }
    
    public synchronized void updateLayerHighlight(DataSeriesProperties prop, boolean highlight) {
		try {
			Layer l = (series2layer != null ? series2layer.get(prop.getName()) : null);
			if (!prop.isVisible()) {
				if (highlight) {
					prop = prop.clone();
					prop.setLineStyle(DataSeriesProperties.LINESTYLE_STRONG);
					prop.setLineWidth(5);
				}
				if (l == null) {
				    l = createLayer(prop);
                    if (l == null) {
                        return;
                    }
				}
				// add and re-hide layer that's been marked invisible
				if (highlight) {
					this.add(l);
				} else {
					this.remove(l);
				}
			} else {
				if (l == null) {
					return;
				}
				double scaleMin = prop.getScaleMin();
				double scaleMax = prop.getScaleMax();
				if (scaleMax <= scaleMin) {
					scaleMax = scaleMin + 1;
				}
				CartesianGraph g;
				if (highlight) {
					// save original graph (including axis, if appplicable)
					series2highlightedGraph.put(prop.getName(), (CartesianGraph)l.getGraph());
					prop = prop.clone();
					prop.setLineStyle(DataSeriesProperties.LINESTYLE_STRONG);
					prop.setLineWidth(5);
					g = createGraph(l, prop, prop.getDisplayName(), scaleMin, scaleMax);
				} else {
					// restore original graph (with axis, if applicable)
					g = series2highlightedGraph.get(prop.getName());
				}
				if (g == null) {
					return;
				}
				l.setGraph(g);
			}
			if (highlight) {
				l.draw(this.getGraphics());
			} else {
				// if not highlighted, we have to redraw everything, otherwise the strong line
				// will
				// not be over-drawn
				this.draw();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private CartesianGraph createGraph(Layer l, DataSeriesProperties prop, String name,
            double scaleMin, double scaleMax) {
        DataSeries series = prop.getSeries();
        if (series == null) {
            Logger.log(Logger.LogType.debug, "createGraph("+prop.getName()+") - skipped (series not found)");
            return null;
        }
        if (series.getNumberOfSamples() == 0) {
            Logger.log(Logger.LogType.debug, "createGraph("+prop.getName()+") - skipped (series has 0 samples)");
            return null;
        }
        if (prop.getStyle() == DataSeriesProperties.STYLE_LINE &&
            series.getNumberOfSamples() < 2) {
            Logger.log(Logger.LogType.debug, "createGraph("+prop.getName()+") - skipped (line series has less than 2)");
            return null;
        }

        CartesianGraph graph = new CartesianGraph();
        
        long minTs = projectItem.getScaleMinX();
        long maxTs = projectItem.getScaleMaxX();
        if (minTs <= 0 || maxTs == Long.MAX_VALUE || minTs >= maxTs) {
            Logger.log(Logger.LogType.debug, "createGraph("+prop.getName()+") - skipped (invalid time range min=" + minTs + ", max=" + maxTs + ")");
            return null;
        }
        
        GeoDate startDate = new GeoDate(minTs);
        GeoDate endDate = new GeoDate(maxTs);
        
        Range2D xPhysRange = new Range2D(G_MARGIN_LEFT, myWidth - G_MARGIN_RIGHT);
        Range2D yPhysRange = new Range2D(G_MARGIN_BOTTOM, myHeight - G_MARGIN_TOP);
        
        TimeRange xUserRange = new TimeRange(startDate,endDate);
        Range2D yUserRange = new Range2D(scaleMin, scaleMax);
        graph2yUserRange.put(graph,yUserRange);
        
        Logger.log(Logger.LogType.debug, "createGraph("+prop.getName()+", " + name + "): scaleMin=" + scaleMin + ", scaleMax=" + scaleMax +
                ", minTs=" + minTs + ", maxTs=" + maxTs + ", myWidth=" + myWidth + ", myHeight=" + myHeight);

        gov.noaa.pmel.sgt.LinearTransform xt = new gov.noaa.pmel.sgt.LinearTransform(xPhysRange, xUserRange);
        gov.noaa.pmel.sgt.AxisTransform yt = null;
        switch (prop.getValueAxis()) {
            case DataSeriesProperties.VALUEAXIS_LINEAR:
                yt = new gov.noaa.pmel.sgt.LinearTransform(yPhysRange, yUserRange);
                break;
            case DataSeriesProperties.VALUEAXIS_LOGARITHMIC:
                try {
                    if (yUserRange.start<=0) yUserRange.start = 1.0;
                    if (yUserRange.end<=0) yUserRange.end = 1.1;
                    yt = new gov.noaa.pmel.sgt.LogTransform(yPhysRange, yUserRange);
                } catch(Exception ee) {
                    yt = new gov.noaa.pmel.sgt.LinearTransform(yPhysRange, yUserRange);
                }
                break;
            default:
                yt = new gov.noaa.pmel.sgt.LinearTransform(yPhysRange, yUserRange);
        }
        
        graph.setXTransform(xt);
        graph.setYTransform(yt);
        SoTPoint origin = new SoTPoint(xUserRange.start, yUserRange.start);
        
        if (graphcnt == 0) {
            TimeAxis xaxis = new TimeAxis(TimeAxis.AUTO);
            xaxis.setRangeU(xUserRange);
            xaxis.setLocationU(origin);
            graph.addXAxis(xaxis);
            graph2xAxis.put(graph,xaxis);
        }
        
        PlainAxis yaxis;
        boolean newaxis = true;
        Object[] keys = graph2yAxis.keySet().toArray();
        for (int yy=0; yy<keys.length; yy++) {
            PlainAxis _yaxis = (PlainAxis)graph2yAxis.get(keys[yy]);
            if (_yaxis.getRangeU().end == prop.getScaleMax()) {
                _yaxis.setLabelColor(Color.black);
                newaxis = false;
            }
        }
        if (newaxis && yaxiscnt < MAX_YAXIS_CNT) {
            yaxis = new PlainAxis(name);
            yaxis.setRangeU(yUserRange);
            yaxis.setRangeP(yPhysRange);
            yaxis.setLabelColor(prop.getColor());
            SoTPoint axisorigin = null;
            if (yaxiscnt == 0) {
                axisorigin = new SoTPoint(xUserRange.start, yUserRange.start);
                yaxis.setLabelPosition(Axis.NEGATIVE_SIDE);
                graph2yAxisOrientation.put(graph,new Integer(AXISLEFT));
            } else if (yaxiscnt == 1) {
                axisorigin = new SoTPoint(xUserRange.end, yUserRange.start);
                yaxis.setLabelPosition(Axis.POSITIVE_SIDE);
                graph2yAxisOrientation.put(graph,new Integer(AXISRIGHT));
            } else if (yaxiscnt == 2) {
                axisorigin = new SoTPoint(xUserRange.start, yUserRange.start);
                yaxis.setLabelPosition(Axis.POSITIVE_SIDE);
                graph2yAxisOrientation.put(graph,new Integer(AXISLEFT));
            } else if (yaxiscnt == 3) {
                axisorigin = new SoTPoint(xUserRange.end, yUserRange.start);
                yaxis.setLabelPosition(Axis.NEGATIVE_SIDE);
                graph2yAxisOrientation.put(graph,new Integer(AXISRIGHT));
            }
            yaxis.setLocationU(axisorigin);
            graph.addYAxis(yaxis);
            graph2yAxis.put(graph,yaxis);
            yaxiscnt++;
        }
        
        int cnt = series.getNumberOfSamples();
        if (cnt <= 0) {
            return graph;
        }
        Sample sample = null;
        Attribute attr = null;
        SGTData sgtdata = null;
        gov.noaa.pmel.sgt.dm.Collection coll;
        series.gotoFirstSample();
        switch(prop.getStyle()) {
            case DataSeriesProperties.STYLE_LINE:
                GeoDate[] date = new GeoDate[cnt];
                double[] values = new double[cnt];
                int i=0;
                long lastTs = 0;
                while ( (sample = series.getNextSample()) != null && i<cnt) {
                    long ts = Math.max(sample.getTimeStamp(), lastTs);
                    lastTs = ts;
                    date[i] = new GeoDate(ts);
                    values[i] = sample.getValue();
                    if (sample.getLabel() != null) {
                        addSampleLabel(l, prop, sample,
                                xt.getTransP(date[i]), yt.getTransP(values[i]));
                    }
                    i++;
                }
                if (prop.getSmooth() != 1) {
                    values = smoothCurve(values,prop.getSmooth());
                }
                attr = new LineAttribute(prop.getLineStyle(),prop.getColor());
                ((LineAttribute)attr).setWidth(prop.getLineWidth());
                sgtdata = new SimpleLine(date,values,name);
                break;
            case DataSeriesProperties.STYLE_DOTS:
            case DataSeriesProperties.STYLE_POINTS:
                attr = new PointAttribute((prop.getStyle() == DataSeriesProperties.STYLE_DOTS ? 51 : 1),prop.getColor());
                coll = new gov.noaa.pmel.sgt.dm.Collection();
                double[] smooth_values = null;
                int smooth_idx = 0;
                if (prop.getSmooth() != 1) {
                    smooth_values = new double[prop.getSmooth()];
                }
                while ( (sample = series.getNextSample()) != null) {
                    SoTPoint pt;
                    if (smooth_values == null) {
                        pt = new SoTPoint(new GeoDate(sample.getTimeStamp()),sample.getValue());
                    } else {
                        smooth_values[smooth_idx++ % smooth_values.length] = sample.getValue();
                        pt = new SoTPoint(new GeoDate(sample.getTimeStamp()),getArrayAverage(smooth_values,smooth_idx-1));
                    }
                    coll.add(new SimplePoint(pt, name));
                    if (sample.getLabel() != null) {
                        addSampleLabel(l, prop, sample,
                                xt.getTransP(pt.getX()), yt.getTransP(pt.getY()));
                    }
                }
                sgtdata = coll;
                break;
            case DataSeriesProperties.STYLE_IMPULSES:
                attr = new LineAttribute(prop.getLineStyle(),prop.getColor());
                ((LineAttribute)attr).setWidth(prop.getLineWidth());
                coll = new gov.noaa.pmel.sgt.dm.Collection();
                while ( (sample = series.getNextSample()) != null) {
                    double[] yval = new double[2];
                    yval[0] = scaleMin;
                    yval[1] = sample.getValue();
                    GeoDate[] xval = new GeoDate[2];
                    xval[0] = xval[1] = new GeoDate(sample.getTimeStamp());
                    coll.add(new SimpleLine(xval,yval,name));
                    if (sample.getLabel() != null) {
                        addSampleLabel(l, prop, sample,
                                xt.getTransP(xval[0]), yt.getTransP(yval[1]));
                    }
                }
                sgtdata = coll;
                break;
        }
        graph.setData(sgtdata,attr);
        return graph;
    }
    
    private Layer createNullLayer() {
        float xyRatio = ((float)getWidth())/((float)getHeight());
        myWidth = G_WIDTH * xyRatio;
        myHeight = G_HEIGHT;
        final Layer layer = new Layer("NULLLAYER", new Dimension2D(myWidth, myHeight));
        double scaleMin = -1 * maxMin2MaxRatio;
        double scaleMax = 1;
        CartesianGraph graph = new CartesianGraph();
        long minTs = projectItem.getScaleMinX();
        long maxTs = projectItem.getScaleMaxX();
        if (minTs <= 0 || maxTs == Long.MAX_VALUE || minTs >= maxTs || maxMin2MaxRatio <= 0) {
            return null;
        }
        
        GeoDate startDate = new GeoDate(minTs);
        GeoDate endDate = new GeoDate(maxTs);
        
        Range2D xPhysRange = new Range2D(G_MARGIN_LEFT, myWidth - G_MARGIN_RIGHT);
        Range2D yPhysRange = new Range2D(G_MARGIN_BOTTOM, myHeight - G_MARGIN_TOP);
        
        TimeRange xUserRange = new TimeRange(startDate,endDate);
        Range2D yUserRange = new Range2D(scaleMin, scaleMax);
        graph2yUserRange.put(graph,yUserRange);
        
        gov.noaa.pmel.sgt.LinearTransform xt = new gov.noaa.pmel.sgt.LinearTransform(xPhysRange, xUserRange);
        gov.noaa.pmel.sgt.AxisTransform yt = new gov.noaa.pmel.sgt.LinearTransform(yPhysRange, yUserRange);
        graph.setXTransform(xt);
        graph.setYTransform(yt);
        
        GeoDate[] date = new GeoDate[2];
        double[] values = new double[2];
        date[0] = new GeoDate(minTs);
        values[0] = 0;
        date[1] = new GeoDate(maxTs);
        values[1] = 0;
        Attribute attr = new LineAttribute(LineAttribute.DASHED, Color.gray);
        ((LineAttribute) attr).setWidth(1);
        SGTData sgtdata = new SimpleLine(date, values, "NULLLINE");
        graph.setData(sgtdata, attr);
        layer.setGraph(graph);
        layers.add(layer);
        return layer;
    }
    
    private void addSampleLabel(Layer l, DataSeriesProperties prop, Sample sample,
            double px, double py) {
        String text = sample.getLabel();
        if (text == null || text.length() == 0) {
            return;
        }
        hasSeriesLabels = true;
        SGLabel label = new SGLabel(text, text, 
                new Point2D.Double(px, py + 0.1));
        label.setAlign(SGLabel.BOTTOM, SGLabel.CENTER);
        label.setColor(prop.getColor());
        label.setHeightP(Main.config.getLabelHeight());
        label.setFont(new Font("Dialog", Font.BOLD, G_LABEL_FONTSIZE));
        l.addChild(label);
    }
    
    public void zoomGraph(CartesianGraph g, TimeRange xr) {
        if(xr.start.compareTo(xr.end) > 0) {
            GeoDate temp = xr.start;
            xr.start = xr.end;
            xr.end = temp;
        }
        this.setBatch(true);
        
        gov.noaa.pmel.sgt.LinearTransform xt = (gov.noaa.pmel.sgt.LinearTransform)g.getXTransform();
        xt.setRangeU(xr);
        
        Range2D yr = (Range2D)graph2yUserRange.get(g);
        TimeAxis xaxis = (TimeAxis)graph2xAxis.get(g);
        PlainAxis yaxis = (PlainAxis)graph2yAxis.get(g);
        Integer axisorientation = (Integer)graph2yAxisOrientation.get(g);
        
        SoTPoint orig = new SoTPoint( (axisorientation == null || axisorientation.intValue() == AXISLEFT ? xr.start : xr.end), yr.start);
        if (xaxis != null) {
            xaxis.setRangeU(xr);
            xaxis.setLocationU(orig);
        }
        if (yaxis != null) {
            yaxis.setRangeU(yr);
            yaxis.setLocationU(orig);
        }
        
        g.setClip(xr.start, xr.end, yr.start, yr.end);
        this.setBatch(false);
        
    }
    
    public void setZoomRange(long zoomStart, long zoomEnd) {
        zoomStart = projectItem.getMinX(zoomStart);
        zoomEnd = projectItem.getMaxX(zoomEnd);
        zoomAllGraphs(zoomStart, zoomEnd);
    }

    public void setZoomStart(long zoomStart) {
        zoomStart = projectItem.getMinX(zoomStart);
        long zoomEnd = projectItem.getMaxX(zoomStart + getZoomWidth());
        zoomAllGraphs(zoomStart, zoomEnd);
    }

    public void setZoomEnd(long zoomEnd) {
        zoomEnd = projectItem.getMaxX(zoomEnd);
        long zoomStart = projectItem.getMinX(zoomEnd - getZoomWidth());
        zoomAllGraphs(zoomStart, zoomEnd);
    }

    public void setZoomWidth(long zoomWidth) {
        long diff = (zoomWidth - (projectItem.getScaleMaxX() - projectItem.getScaleMinX())) / 2;
        long zoomStart = projectItem.getMinX(projectItem.getScaleMinX() - diff);
        long zoomEnd = projectItem.getMaxX(projectItem.getScaleMaxX() + diff);
        zoomAllGraphs(zoomStart, zoomEnd);
    }
    
    public void scrollZoom(long direction) {
        long width = getZoomWidth();
        if (direction < 0) {
            long zoomStart = projectItem.getMinX(projectItem.getScaleMinX() + direction);
            long zoomEnd = projectItem.getMaxX(zoomStart + width);
            zoomAllGraphs(zoomStart, zoomEnd);
        } else {
            long zoomEnd = projectItem.getMaxX(projectItem.getScaleMaxX() + direction);
            long zoomStart = projectItem.getMinX(zoomEnd - width);
            zoomAllGraphs(zoomStart, zoomEnd);
        }
    }

    public long getZoomWidth() {
        return projectItem.getScaleMaxX() - projectItem.getScaleMinX();
    }

    public void panePropertyChange(PropertyChangeEvent event) {
        String name = event.getPropertyName();
        
        if (layers.size() == 0) {
            return;
        }
        
        if(name.equals("zoomRectangle")) {
            TimeRange xr = new TimeRange();
            Rectangle zm = (Rectangle)event.getNewValue();
            if(zm.width <= 1 || zm.height <= 1) return;
            
            if (hasSeriesLabels) {
                // if we have series with labels, those labels are attached
                // to physical locations on the layer. in this case, we need
                // to recalculate their physical locations, so we need to go
                // the slow path through updateDisplay() where we rebuild the
                // entire graph instead of just rescaling it
                if (layers == null || layers.size() == 0) {
                    return;
                }
                Layer l = layers.get(0);
                CartesianGraph g = (CartesianGraph) l.getGraph();
                xr.start = g.getXPtoTime(l.getXDtoP(zm.x));
                xr.end = g.getXPtoTime(l.getXDtoP(zm.x + zm.width));
                setZoomRange(Sample.createTimeStamp(xr.start), Sample.createTimeStamp(xr.end));
                mainFrame.updateDisplay();

            } else {
                // fast path: just rescale all graphs

                for (int i = 0; i < layers.size(); i++) {
                    CartesianGraph g = (CartesianGraph) layers.get(i).getGraph();
                    Layer l = layers.get(i);
                    xr.start = g.getXPtoTime(l.getXDtoP(zm.x));
                    xr.end = g.getXPtoTime(l.getXDtoP(zm.x + zm.width));
                    zoomGraph(g, xr);
                }

                projectItem.setScaleMinX(Sample.createTimeStamp(xr.start));
                projectItem.setScaleMaxX(Sample.createTimeStamp(xr.end));
                mainFrame.updateZoomLabel();
            }
        }

        if(name.equals("objectSelected")) {
            Object o = getSelectedObject();
            if (o != null && o instanceof SGLabel) {
                selectedLabel = (SGLabel)o;
            }
        }
    }
    
    public void panelMouseClicked() {
        SGLabel label = selectedLabel;
        if (label != null) {
            try {
                label = LabelDialog.editLabel(mainFrame, label);
                if (label != null) {
                    String s = label.getText();
                    if (s.length() == 0) {
                        Layer layer = layers.get(0);
                        if (layer != null) {
                            synchronized (this) {
                                layer.removeChild(label);
                            }
                        }
                        labels.remove(label);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void addLabel(DataLabel d) {
        addLabel(d.getText(), d.getPX(), d.getPY(), d.getColor(), d.getSize());
    }
    
    public void addLabel(String text) {
        addLabel(text, myWidth / 2, myHeight / 2);
    }
    
    public void addLabel(String text, double x, double y) {
        addLabel(text, x, y, Color.black, Main.config.getLabelHeight());
    }
    
    public void addLabel(String text, Color c, double h) {
        addLabel(text, myWidth / 2, myHeight / 2, c, h);
    }
    
    public void addLabel(String text, double x, double y, Color c, double h) {
        if (layers == null || layers.size() == 0) {
            return;
        }
        Layer layer = layers.get(layers.size()-1);
        if (layer == null) {
            return;
        }
        SGLabel label = new SGLabel(text, text, new Point2D.Double(x, y));
        label.setAlign(SGLabel.BOTTOM, SGLabel.CENTER);
        label.setColor(c);
        label.setHeightP(h);
        label.setFont(new Font("Dialog", Font.BOLD, G_LABEL_FONTSIZE));
        layer.addChild(label);
        labels.add(label);
    }

    public DataLabel[] getAllLabels() {
        if (labels == null || labels.size() == 0) {
            return null;
        }
        DataLabel[] dl = new DataLabel[labels.size()];
        for (int i=0; i<labels.size(); i++) {
            SGLabel label = (SGLabel)labels.get(i);
            dl[i] = new DataLabel(label.getText(), 
                    label.getLocationP().x, 
                    label.getLocationP().y,
                    label.getColor(),
                    label.getHeightP());
        }
        return dl;
    }
    
    public void zoomAllGraphs(long startX, long endX) {
        if (endX <= startX) {
            return;
        }

        TimeRange tr = new TimeRange(new GeoDate(startX),new GeoDate(endX));
        for (int i=0; i<layers.size(); i++) {
            CartesianGraph g = (CartesianGraph) layers.get(i).getGraph();
            zoomGraph(g, tr);
        }
        projectItem.setScaleMinX(startX);
        projectItem.setScaleMaxX(endX);
    }
    
    public String saveImageToFile(String fname) {
        int width = this.getWidth();
        int height = this.getHeight();
        BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)img.getGraphics();
        String result = null;
        try {
            g.setBackground(Color.WHITE);
            if (this.isShowing()) { // GUI Mode
                this.paintComponent(g);
            } else { // Batch Mode
                this.draw((java.awt.Graphics)g);
            }
            File file = new File(fname);
            javax.imageio.ImageIO.write(img, "PNG", file);
        } catch (Exception ee) {
            ee.printStackTrace();
            result = "Saving failed: "+ee.getMessage();
        } finally {
            g.dispose();
        }
        return result;
    }    
    
}