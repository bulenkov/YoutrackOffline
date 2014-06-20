package com.intellij.youtrack.util;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.youtrack.model.MyyIssue;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.xerces.util.XMLChar;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class YoutrackSession {
  private final String myHost;
  private final String myUser;
  private final String myPassword;
  private String myDefaultSearch = "for: me sort by: updated";
  private static final String YOUTRACK_OFFLINE_USER = "YOUTRACK_OFFLINE_USER";
  private static final String YOUTRACK_OFFLINE_PASS = "YOUTRACK_OFFLINE_PASS";
  private static final String YOUTRACK_OFFLINE_HOST = "YOUTRACK_OFFLINE_HOST";


  public YoutrackSession(Project project) {
    PropertiesComponent storage = PropertiesComponent.getInstance(project);
    myHost = storage.getOrInit(YOUTRACK_OFFLINE_HOST, "");
    myUser = storage.getOrInit(YOUTRACK_OFFLINE_USER, "");
    myPassword = storage.getOrInit(YOUTRACK_OFFLINE_PASS, "");
  }

  public String getUrl() {
    return myHost;
  }

  public void refresh() {

  }


  public List<MyyIssue> getIssues(ProgressIndicator indicator, long since) throws Exception {
    int after = 0;
    int step = 1000;
    List<String> issues = new ArrayList<String>();
    while (true) {
      indicator.setText("Getting issues from " + after + " to " + (after + step));
      indicator.checkCanceled();
      String query = getDefaultSearch();
      String requestUrl = "/rest/project/issues/?filter=" + encodeUrl(query) + "&max=" + step + "&updatedAfter=" + since + "&after=" + after;
      try {
        Element element = doREST(requestUrl, false);

        List children = element.getChildren();
        if (children.isEmpty()) {
          break;
        } else {
          for (Object child : children) {
            if (child instanceof Element) {
              issues.add(((Element) child).getAttributeValue("id"));
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      after += step;
    }
    ArrayList<MyyIssue> result = new ArrayList<MyyIssue>();
    for (int i = 0; i < issues.size(); i++) {
      indicator.checkCanceled();
      indicator.setText("Updating issue " + (i+1) + " from " + issues.size());
      try {
        Element element = doREST("/rest/issue/" + issues.get(i), false);
        element.setAttribute("read", "false");
        result.add(new MyyIssue(element));
      } catch (Exception e) {
        Thread.sleep(500);
      }
    }
    return result;
  }

  @Nullable
  public CancellableConnection createCancellableConnection() {
    PostMethod method = new PostMethod(getUrl() + "/rest/user/login");
    return new HttpTestConnection<PostMethod>(method) {
      @Override
      protected void doTest(PostMethod method) throws Exception {
        login(method);
      }
    };
  }

  private HttpClient login(PostMethod method) throws Exception {
    if (method.getHostConfiguration().getProtocol() == null) {
      throw new Exception("Protocol not specified");
    }
    HttpClient client = getHttpClient();
    client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()));
    configureHttpMethod(method);
    method.addParameter("login", getUsername());
    method.addParameter("password", getPassword());
    client.getParams().setContentCharset("UTF-8");
    client.executeMethod(method);
    if (method.getStatusCode() != 200) {
      throw new Exception("Cannot login: HTTP status code " + method.getStatusCode());
    }
    String response = method.getResponseBodyAsString(1000);
    if (response == null) {
      throw new NullPointerException();
    }
    if (!response.contains("<login>ok</login>")) {
      int pos = response.indexOf("</error>");
      int length = "<error>".length();
      if (pos > length) {
        response = response.substring(length, pos);
      }
      throw new Exception("Cannot login: " + response);
    }
    return client;
  }

  private String getPassword() {
    return myPassword;
  }

  private String getUsername() {
    return myUser;
  }

//  @Nullable
//  public Task findTask(String id) throws Exception {
//    HttpMethod method = doREST("/rest/issue/byid/" + id, false);
//    InputStream stream = method.getResponseBodyAsStream();
//    Element element = new SAXBuilder(false).build(stream).getRootElement();
//    return element.getName().equals("issue") ? createIssue(element) : null;
//  }


  private Element doREST(String request, boolean post) throws Exception {
    HttpClient client = login(new PostMethod(getUrl() + "/rest/user/login"));
    String uri = getUrl() + request;
    HttpMethod method = post ? new PostMethod(uri) : new GetMethod(uri);
    configureHttpMethod(method);
    client.executeMethod(method);
    InputStream stream = method.getResponseBodyAsStream();

    // todo workaround for http://youtrack.jetbrains.net/issue/JT-7984
    String s = StreamUtil.readText(stream, "UTF-8");
    for (int i = 0; i < s.length(); i++) {
      if (!XMLChar.isValid(s.charAt(i))) {
        s = s.replace(s.charAt(i), ' ');
      }
    }

    Element element;
    try {
      element = new SAXBuilder(false).build(new StringReader(s)).getRootElement();
    } catch (JDOMException e) {
      LOG.error("Can't parse YouTrack response for " + request, e);
      throw e;
    }
    if ("error".equals(element.getName())) {
      throw new Exception("Error from YouTrack for " + request + ": '" + element.getText() + "'");
    }
    return element;
  }

  private void configureHttpMethod(HttpMethod method) {
    method.addRequestHeader("accept", "application/xml");
  }


  public String getDefaultSearch() {
    return myDefaultSearch;
  }

  public void setDefaultSearch(String defaultSearch) {
    if (defaultSearch != null) {
      myDefaultSearch = defaultSearch;
    }
  }


  private static final Logger LOG = Logger.getInstance("#com.intellij.tasks.youtrack.YouTrackRepository");


//  private void checkVersion() throws Exception {
//    HttpMethod method = doREST("/rest/workflow/version", false);
//    InputStream stream = method.getResponseBodyAsStream();
//    Element element = new SAXBuilder(false).build(stream).getRootElement();
//    final boolean timeTrackingAvailable = element.getName().equals("version") && VersionComparatorUtil.compare(element.getChildText("version"), "4.1") >= 0;
//    if (!timeTrackingAvailable) {
//      throw new Exception("This version of Youtrack the time tracking is not supported");
//    }
//  }

  protected static String encodeUrl(@NotNull String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  protected HttpClient getHttpClient() {
    HttpClient client = new HttpClient();
    configureHttpClient(client);
    return client;
  }

  protected void configureHttpClient(HttpClient client) {
    client.getParams().setConnectionManagerTimeout(30000);
    client.getParams().setSoTimeout(30000);
//    if (isUseProxy()) {
//      HttpConfigurable proxy = HttpConfigurable.getInstance();
//      client.getHostConfiguration().setProxy(proxy.PROXY_HOST, proxy.PROXY_PORT);
//      if (proxy.PROXY_AUTHENTICATION) {
//        AuthScope authScope = new AuthScope(proxy.PROXY_HOST, proxy.PROXY_PORT);
//        Credentials credentials = getCredentials(proxy.PROXY_LOGIN, proxy.getPlainProxyPassword(), proxy.PROXY_HOST);
//        client.getState().setProxyCredentials(authScope, credentials);
//      }
//    }
//    if (isUseHttpAuthentication()) {
//      client.getParams().setCredentialCharset("UTF-8");
//      client.getParams().setAuthenticationPreemptive(true);
//      client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()));
//    }
  }

  @Nullable
  private static Credentials getCredentials(String login, String password, String host) {
    int domainIndex = login.indexOf("\\");
    if (domainIndex > 0) {
      // if the username is in the form "user\domain"
      // then use NTCredentials instead of UsernamePasswordCredentials
      String domain = login.substring(0, domainIndex);
      if (login.length() > domainIndex + 1) {
        String user = login.substring(domainIndex + 1);
        return new NTCredentials(user, password, host, domain);
      } else {
        return null;
      }
    } else {
      return new UsernamePasswordCredentials(login, password);
    }
  }

  public static void saveCredentials(Project project, String host, String user, String password) {
    PropertiesComponent storage = PropertiesComponent.getInstance(project);
    storage.setValue(YOUTRACK_OFFLINE_HOST, host);
    storage.setValue(YOUTRACK_OFFLINE_USER, user);
    storage.setValue(YOUTRACK_OFFLINE_PASS, password);
  }
}
