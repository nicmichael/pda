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

public interface ILoadSteps {
    
    public int getNumberOfLoadSteps();
    
    public long getStart(int step);
    public long getEnd(int step);
    public long getLoad(int step);
    public long getActLoad(int step);
    public long getTotalRequests(int step, String req);
    public long getSuccessfulRequests(int step, String req);
    public long getFailedRequests(int step, String req);
    public float getRequestFailureRate(int step, String req);
    public String[] getAllRequestNames();
    public String[] getAllQuantileNames();
    public float getRespMin(int step, String req);
    public float getRespMax(int step, String req);
    public float getRespAvg(int step, String req);
    public float getRespQuant(int step, String req, String quant);
    public void stepNew(long start);
    public void stepEnd(long end);
    public void stepLoad(long load);
    public void stepActLoad(long load);
    public void stepTotalRequests(String req, long cnt);
    public void stepSuccessfullRequests(String req, long cnt);
    public void stepFailedRequests(String req, long cnt);
    public void stepRequestFailureRate(String req, float failureRate);
    public void stepRespMin(String req, float resp);
    public void stepRespMax(String req, float resp);
    public void stepRespAvg(String req, float resp);
    public void stepRespQ95(String req, String quant, float resp);
}
