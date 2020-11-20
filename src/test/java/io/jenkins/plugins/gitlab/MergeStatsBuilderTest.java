package io.jenkins.plugins.gitlab;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MergeStatsBuilderTest {

    final String projectURL = "https://api.github.com/";
    final String iProject = "angular";
    final String days = "5";
    final String token = "02be899c7bde22be535214d94881f6545e0adf1b";
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        MergeStatsBuilder builder = new MergeStatsBuilder(projectURL, "baby-yodas", "", token, days);
        builder.setTimescale(days);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Project: " + iProject, build);
    }

    @Test
    public void testBuildTimescale() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject();
        MergeStatsBuilder builder = new MergeStatsBuilder(projectURL, "baby-yodas", "", token, days);
        builder.setTimescale(days);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("No of days searched: " + days, build);
    }
}
