package io.jenkins.plugins.gitlab;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class MergeStatsBuilderTest {

    final String projectURL = "https://github.com/angular/angular.git";
    final String iProject = "angular";
    final String days = "5";
    final String token = "yyeyjeVZUjpP6X3rzfxM";
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        MergeStatsBuilder builder = new MergeStatsBuilder(projectURL, token, days);
        builder.setTimescale(days);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Project: " + iProject, build);
    }

    @Test
    public void testBuildTimescale() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject();
        MergeStatsBuilder builder = new MergeStatsBuilder(projectURL, token, days);
        builder.setTimescale(days);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("No of days searched: " + days, build);
    }
}
