package io.jenkins.plugins.gitlab;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MergeStatsBuilder extends Builder implements SimpleBuildStep {

    private static final int defaultTimescale = 1;
    JSONObject jProject;
    String timeStamp;
    HashMap<String, ArrayList<JSONObject>> statsBreakdown;
    private String gitURL;
    private String gitToken;
    private int timescale;
    private String project;
    private String namespace;
    private ArrayList<JSONObject> projects;
    private ArrayList<JSONObject> mergeStats;

    @DataBoundConstructor
    public MergeStatsBuilder(String gitURL, String gitToken, String timescale) {
        this.gitURL = gitURL;
        this.gitToken = gitToken;
        setTimescale(timescale);
    }

    private static int parseWithDefault(String s, int defaultTimescale) {
        return s.matches("-?\\d+") ? Integer.parseInt(s) : defaultTimescale;
    }

    public String getGitURL() {
        return gitURL;
    }

    @DataBoundSetter
    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }

    public int getTimescale() {
        return timescale;
    }

    @DataBoundSetter
    public void setTimescale(String timescale) {
        this.timescale = parseWithDefault(timescale, defaultTimescale);
    }

    public String getGitToken() {
        return gitToken;
    }

    @DataBoundSetter
    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        GitAPICall apiCall = new GitAPICall(gitToken);
        try {
            projects = apiCall.getProjects();
        } catch (JSONException e) {
            e.printStackTrace();
            logErrors(e, listener);
        }

        if (gitURL.length() > 5) {
            formatURL();
        }

        if (namespace != null && project != null) {
            try {
                for (JSONObject p : projects) {
                    if (gitURL.startsWith(p.get("web_url").toString())) {
                        jProject = p;
                        break;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                logErrors(e, listener);
            }
        }

        long millis = TimeUnit.DAYS.toMillis(timescale);
        timeStamp = new SimpleDateFormat("yyyy-MM-dd.HH:mm.ss").format(new Timestamp(System.currentTimeMillis() - millis));
        String timeStampFormatted = timeStamp.substring(0, 10) + 'T' + timeStamp.substring(11);

        try {
            mergeStats = apiCall.getMergeStats(jProject, timeStampFormatted);
        } catch (JSONException e) {
            e.printStackTrace();
            logErrors(e, listener);
        }

        CategoriseMergeStats categoriseMergeStats = new CategoriseMergeStats();
        if (mergeStats != null) {
            statsBreakdown = categoriseMergeStats.categorise(mergeStats);
        }
        listener.getLogger().println("End- Project: " + project);
        listener.getLogger().println("End- No of days searched: " + timescale);

        run.addAction(new MergeStatsAction(project, timescale, statsBreakdown));
    }

    public void formatURL() {
        String discard = gitURL.substring(gitURL.length() - 4);
        if (discard.equals(".git")) {
            gitURL = gitURL.substring(0, gitURL.length() - 4);
        }
        project = gitURL.substring(gitURL.lastIndexOf("/") + 1);

        if (gitURL.startsWith(Constants.GITHUB_HOST)) {
            namespace = gitURL.replace(Constants.GITHUB_HOST, "").replace("/" + project, "");
        } else {
            namespace = gitURL.replace(Constants.GITHUB_HOST, "").replace("/" + project, "");
        }
    }

    public void logErrors(JSONException e, TaskListener listener) {
        StringWriter outError = new StringWriter();
        e.printStackTrace(new PrintWriter(outError));
        String errorString = outError.toString();
        listener.getLogger().println(errorString);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.MergeStatsBuilder_DescriptorImpl_DisplayName();
        }

    }
}
