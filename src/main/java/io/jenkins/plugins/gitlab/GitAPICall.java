package io.jenkins.plugins.gitlab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class GitAPICall {
    private static final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.com", 8080));

    private static final String PROJECTS_URI = "api/v4/projects";
    int numberOfPages;
    ArrayList<JSONObject> projects;
    ArrayList<JSONObject> mergeStats;
    private String token;
    private String projectsBaseURL;

    public GitAPICall(String token) {
        ignoreCerts();
        this.token = token;
        projects = new ArrayList<>();
        mergeStats = new ArrayList<>();
        numberOfPages = 0;
        projectsBaseURL = Constants.GITHUB_HOST + PROJECTS_URI;
    }

    //to run locally if you do not have the correct certificates
    public static void ignoreCerts() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        try {
            SSLContext sc;
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(URL url) throws IOException {
        //HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(proxy);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        String headerField = connection.getHeaderField("X-Total-Pages");
        if (headerField != null) {
            numberOfPages = Integer.parseInt(headerField);
        }
    }

    public ArrayList<JSONObject> getMergeStats(JSONObject project, String time) throws JSONException, IOException {
        if (project != null) {
            setNumberOfPages(new URL(projectsBaseURL + "/" + project.getString("id") + "/merge_requests?per_page=100&private_token=" + token + "&updated_after=" + time));
            for (int i = 0; i < numberOfPages; i++) {
                URL url = new URL(projectsBaseURL + "/" + project.getString("id") + "/merge_requests?per_page=100&page=" + i + "&private_token=" + token + "&updated_after=" + time);
                JSONArray jsonArray = openConnection(url);

                extractJSONObjects(jsonArray, mergeStats);
            }
        }
        return mergeStats;
    }

    public ArrayList<JSONObject> getProjects() throws IOException, JSONException {
        setNumberOfPages(new URL(projectsBaseURL + "?per_page=100&private_token=" + token));

        for (int i = 0; i < numberOfPages; i++) {
            int page = i + 1;
            URL url = new URL(projectsBaseURL + "?per_page=100&page=" + page + "&private_token=" + token);

            JSONArray jsonArray = openConnection(url);

            extractJSONObjects(jsonArray, projects);
        }
        return projects;
    }

    public JSONArray openConnection(URL url) throws IOException, JSONException {
        //HttpsURLConnection con = (HttpsURLConnection) url.openConnection(proxy);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestProperty("Accept-Encoding", "gzip");

        BufferedReader reader = null;
        try {
            if ("gzip".equals(con.getContentEncoding())) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream()), "UTF-8"));
            } else {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            }

            String result = reader.lines().collect(Collectors.joining());
            return new JSONArray(result);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void extractJSONObjects(JSONArray jsonArray, ArrayList<JSONObject> list) throws JSONException {
        for (int j = 0; j < jsonArray.length(); j++) {
            JSONObject jsonObject = jsonArray.getJSONObject(j);
            list.add(jsonObject);
        }
    }
}
