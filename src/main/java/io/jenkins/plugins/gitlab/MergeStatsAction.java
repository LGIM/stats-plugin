package io.jenkins.plugins.gitlab;

import hudson.model.Run;
import jenkins.model.RunAction2;
import org.json.JSONObject;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.HashMap;

public class MergeStatsAction implements RunAction2 {

    private String project;
    private int daysSearched;
    private HashMap<String, ArrayList<JSONObject>> statsBreakdown;
    private ArrayList<JSONObject> merged;
    private ArrayList<JSONObject> closed;
    private ArrayList<JSONObject> open;
    private int noOfMerged;
    private int noOfClosed;
    private int noOfOpen;
    private int total;
    private transient Run run;

    public MergeStatsAction(String project, int daysSearched, HashMap<String, ArrayList<JSONObject>> statsBreakdown) {
        this.project = project;
        this.daysSearched = daysSearched;
        this.statsBreakdown = statsBreakdown;
        if (statsBreakdown != null) {
            merged = statsBreakdown.get("merged");
            closed = statsBreakdown.get("closed");
            open = statsBreakdown.get("opened");
        }
        if (merged != null) {
            noOfMerged = merged.size();
        }
        if (closed != null) {
            noOfClosed = closed.size();
        }
        if (open != null) {
            noOfOpen = open.size();
        }
        total = noOfClosed + noOfOpen + noOfMerged;
    }


    public String getProject() {
        return project;
    }

    public int getDaysSearched() {
        return daysSearched;
    }

    public HashMap<String, ArrayList<JSONObject>> getStatsBreakdown() {
        return statsBreakdown;
    }

    public ArrayList<JSONObject> getMerged() {
        return merged;
    }

    public ArrayList<JSONObject> getClosed() {
        return closed;
    }

    public ArrayList<JSONObject> getOpen() {
        return open;
    }

    public int getNoOfMerged() {
        return noOfMerged;
    }

    public int getNoOfClosed() {
        return noOfClosed;
    }

    public int getNoOfOpen() {
        return noOfOpen;
    }

    public int getTotal() {
        return total;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Show merge Stats";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "mergeStats";
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run getRun() {
        return run;
    }
}
