package io.jenkins.plugins.gitlab;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoriseMergeStats {

    private HashMap<String, ArrayList<JSONObject>> statsBreakdown;
    private ArrayList<JSONObject> merged;
    private ArrayList<JSONObject> opened;
    private ArrayList<JSONObject> closed;

    public CategoriseMergeStats() {
        statsBreakdown = new HashMap<>();
        merged = new ArrayList<>();
        opened = new ArrayList<>();
        closed = new ArrayList<>();
    }

    public HashMap<String, ArrayList<JSONObject>> categorise(ArrayList<JSONObject> mergeStats) {
        for (JSONObject jo : mergeStats) {
            try {
                if (jo.getString("state").equals("merged")) {
                    merged.add(jo);
                } else if (jo.getString("state").equals("opened")) {
                    opened.add(jo);
                } else if (jo.getString("state").equals("closed")) {
                    closed.add(jo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        statsBreakdown.put("merged", merged);
        statsBreakdown.put("opened", opened);
        statsBreakdown.put("closed", closed);
        return statsBreakdown;
    }
}
