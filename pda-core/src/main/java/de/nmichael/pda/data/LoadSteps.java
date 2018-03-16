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

import java.util.*;

public abstract class LoadSteps extends Parser implements ILoadSteps {
    
    private class LoadStep {
        public long start = 0;
        public long end = 0;
        public long load = 0;
        public long actload = 0;
        
        public Hashtable<String,Long> totalRequests = new Hashtable<String,Long>();
        public Hashtable<String,Long> successfulRequests = new Hashtable<String,Long>();
        public Hashtable<String,Long> failedRequests = new Hashtable<String,Long>();
        public Hashtable<String,Float> failureRate = new Hashtable<String,Float>();
        public Hashtable<String,Float> respMin = new Hashtable<String,Float>();
        public Hashtable<String,Float> respMax = new Hashtable<String,Float>();
        public Hashtable<String,Float> respAvg = new Hashtable<String,Float>();
        public Hashtable<String,Quantiles> respQuant = new Hashtable<String,Quantiles>();
    }
    
    private Vector<LoadStep> steps = new Vector<LoadStep>();
    private LoadStep step = null;
    private Hashtable<String,String> allRequestNames = new Hashtable<String,String>();
    private Hashtable<String,String> allQuantileNames = new Hashtable<String,String>();
    
    public LoadSteps(String filename) {
        super(filename);
    }
    
    public int getNumberOfLoadSteps() {
        try {
            return steps.size();
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getStart(int step) {
        try {
            return (steps.get(step)).start;
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getEnd(int step) {
        try {
            return (steps.get(step)).end;
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getLoad(int step) {
        try {
            return (steps.get(step)).load;
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getActLoad(int step) {
        try {
            return (steps.get(step)).actload;
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getTotalRequests(int step, String req) {
        try {
            return (steps.get(step)).totalRequests.get(req);
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getSuccessfulRequests(int step, String req) {
        try {
            return (steps.get(step)).successfulRequests.get(req);
        } catch(Exception e) {
            return 0;
        }
    }
    
    public long getFailedRequests(int step, String req) {
        try {
            return (steps.get(step)).failedRequests.get(req);
        } catch(Exception e) {
            return 0;
        }
    }
    
    public float getRequestFailureRate(int step, String req) {
        try {
            return (steps.get(step)).failureRate.get(req);
        } catch(Exception e) {
            return 0.0f;
        }
    }
    
    public String[] getAllRequestNames() {
        try {
            String[] names = allRequestNames.keySet().toArray(new String[0]);
            Arrays.sort(names);
            return names;
        } catch(Exception e) {
            return null;
        }
    }
    
    public String[] getAllQuantileNames() {
        try {
            String[] names = allQuantileNames.keySet().toArray(new String[0]);
            Arrays.sort(names);
            return names;
        } catch(Exception e) {
            return null;
        }
    }    
    
    public float getRespMin(int step, String req) {
        try {
            return (steps.get(step)).respMin.get(req);
        } catch(Exception e) {
            return 0.0f;
        }
    }
    
    public float getRespMax(int step, String req) {
        try {
            return (steps.get(step)).respMax.get(req);
        } catch(Exception e) {
            return 0.0f;
        }
    }
    
    public float getRespAvg(int step, String req) {
        try {
            return (steps.get(step)).respAvg.get(req);
        } catch(Exception e) {
            return 0.0f;
        }
    }
    
    public float getRespQuant(int step, String req, String quant) {
        try {
            return (steps.get(step)).respQuant.get(req).get(quant);
        } catch(Exception e) {
            return 0.0f;
        }
    }
    
    public void stepNew(long start) {
        if (step != null) {
            stepEnd(start);
        }
        step = new LoadStep();
        step.start = start;
    }
    
    public void stepEnd(long end) {
        if (step == null) return;
        step.end = end;
        steps.add(step);
        step = null;
    }
    
    public void stepLoad(long load) {
        if (step == null) return;
        step.load = load;
    }
    
    public void stepActLoad(long load) {
        if (step == null) return;
        step.actload = load;
    }
    
    public void stepTotalRequests(String req, long cnt) {
        if (step == null) return;
        step.totalRequests.put(req, cnt);
        allRequestNames.put(req, "foo");
    }

    public void stepSuccessfullRequests(String req, long cnt) {
        if (step == null) return;
        step.successfulRequests.put(req, cnt);
        allRequestNames.put(req, "foo");
    }

    public void stepFailedRequests(String req, long cnt) {
        if (step == null) return;
        step.failedRequests.put(req, cnt);
        allRequestNames.put(req, "foo");
    }
    
    public void stepRequestFailureRate(String req, float failureRate) {
        if (step == null) return;
        step.failureRate.put(req, failureRate);
        allRequestNames.put(req, "foo");
    }
    
    public void stepRespMin(String req, float resp) {
        if (step == null) return;
        step.respMin.put(req, resp);
        allRequestNames.put(req, "foo");
    }
    
    public void stepRespMax(String req, float resp) {
        if (step == null) return;
        step.respMax.put(req, resp);
        allRequestNames.put(req, "foo");
    }
    
    public void stepRespAvg(String req, float resp) {
        if (step == null) return;
        step.respAvg.put(req, resp);
        allRequestNames.put(req, "foo");
    }
    
    public void stepRespQ95(String req, String quant, float resp) {
        if (step == null) return;
        Quantiles q = step.respQuant.get(req);
        if (q == null) {
            q = new Quantiles();
        }
        step.respQuant.put(req, q);
        allRequestNames.put(req, "foo");
        allQuantileNames.put(quant, "foo");
    }
    
}
