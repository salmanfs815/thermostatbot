package com.redhat.thermostat.bot;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BugInfo extends ListenerAdapter {

    private static Logger logger = Logger.getLogger(BugInfo.class);

    private static final Pattern openjdkBugPattern = Pattern.compile("\\b(?<prefix>(JDK|JMC))([- ])?(?<bugId>\\d{1,10})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern redhatBugPattern = Pattern.compile("\\b(RH)(BZ)?([- ])?(?<bugId>\\d{1,10})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern icedteaBugPattern = Pattern.compile("\\b[Pp][Rr] *(?<bugId>\\d{1,6})\\b", Pattern.CASE_INSENSITIVE);

    private static final String OPENJDK_BUG_URL = "https://bugs.openjdk.java.net/browse/%s-%s";

    private static final String REDHAT_BUGZILLA_URL = "https://bugzilla.redhat.com/";
    private static final String REDHAT_XMLRPC_SERVER = REDHAT_BUGZILLA_URL + "xmlrpc.cgi";
    private static final String REDHAT_BUG_URL = REDHAT_BUGZILLA_URL + "show_bug.cgi?id=%s";

    private static final String ICEDTEA_BUGZILLA_URL = "https://icedtea.classpath.org/bugzilla/";
    private static final String ICEDTEA_XMLRPC_SERVER = ICEDTEA_BUGZILLA_URL + "xmlrpc.cgi";
    private static final String ICEDTEA_BUG_URL = ICEDTEA_BUGZILLA_URL + "show_bug.cgi?id=%s";

    private static final String JIRA_URL = "https://bugs.openjdk.java.net/";
    private static URI jiraUri;
    private static JiraRestClient jiraClient;
    private static final XmlRpcClient redhatBugzillaClient = new XmlRpcClient();
    private static final XmlRpcClient icedteaBugzillaClient = new XmlRpcClient();

    static class Bug {
        final String prefix;
        final String id;
        final String description;
        final String url;
        final String product;
        final String component;

        Bug(String prefix, String id, String description, String url, String product, String component) {
            this.prefix = prefix;
            this.id = id;
            this.description = description;
            this.url = url;
            this.product = product;
            this.component = component;
        }

        @Override
        public String toString() {
            if (url.equals("")) {
                return String.format("%s-%s: %s", prefix, id, description);
            } else {
                return String.format("[%s][%s] %s-%s: %s (%s)", product, component, prefix, id, description, url);
            }
        }
    }

    BugInfo() {
        try {
            jiraUri = new URI(JIRA_URL);
            initializeJiraClient();

            XmlRpcClientConfigImpl redhatBugzillaConfig = new XmlRpcClientConfigImpl();
            redhatBugzillaConfig.setServerURL(new URL(REDHAT_XMLRPC_SERVER));
            redhatBugzillaClient.setConfig(redhatBugzillaConfig);

            XmlRpcClientConfigImpl icedteaBugzillaConfig = new XmlRpcClientConfigImpl();
            icedteaBugzillaConfig.setServerURL(new URL(ICEDTEA_XMLRPC_SERVER));
            icedteaBugzillaClient.setConfig(icedteaBugzillaConfig);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void initializeJiraClient() {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        AnonymousAuthenticationHandler authHandler = new AnonymousAuthenticationHandler();
        jiraClient = factory.create(jiraUri, authHandler);
    }

    @Override
    public void onGenericMessage(final GenericMessageEvent event) {
        // only speak when spoken to
        if (event instanceof PrivateMessageEvent || event.getMessage().toLowerCase().contains(event.getBot().getNick().toLowerCase())) {
            Matcher openjdkMatcher = openjdkBugPattern.matcher(event.getMessage());
            Matcher redhatMatcher = redhatBugPattern.matcher(event.getMessage());
            Matcher icedteaMatcher = icedteaBugPattern.matcher(event.getMessage());

            if (openjdkMatcher.find()) {
                handleOpenjdkBug(event, openjdkMatcher);
            } else if (redhatMatcher.find()) {
                handleRedhatBug(event, redhatMatcher);
            } else if (icedteaMatcher.find()) {
                handleIcedteaBug(event, icedteaMatcher);
            }
        }
    }

    private void handleOpenjdkBug(GenericMessageEvent event, Matcher matcher) {
        String prefix = matcher.group("prefix").toUpperCase();
        String bugId = matcher.group("bugId");
        String summary = "";
        String url = "";
        String product = "";
        String component = "";
        try {
            Promise<Issue> promise = jiraClient.getIssueClient().getIssue(String.format("%s-%s", prefix, bugId));
            Issue issue = promise.claim();

            summary = issue.getSummary();
            url = String.format(OPENJDK_BUG_URL, prefix, bugId);
            product = issue.getProject().getName();
            StringBuilder componentBuilder = new StringBuilder();
            for (BasicComponent comp: issue.getComponents()) {
                componentBuilder.append(comp.getName()).append(" ");
            }
            component = componentBuilder.toString().trim();
        } catch (RestClientException e) {
            if (e.getMessage().contains("org.codehaus.jettison.json.JSONException")) { // JIRA Client timed out
                System.out.println("Re-initializing JIRA Client...");
                initializeJiraClient();
                handleOpenjdkBug(event, matcher);
            } else {
                summary = e.getMessage().replaceAll("\n", " ");
            }
        } catch (Exception err) {
            summary = err.getMessage().replaceAll("\n", " ");
        }
        String reply = new Bug(prefix, bugId, summary, url, product, component).toString();
        logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
        event.respondWith(reply);
    }

    private void handleRedhatBug(GenericMessageEvent event, Matcher matcher) {
        String bugId = matcher.group("bugId");
        try {
            Map<String, List<String>> bug = new HashMap<>();
            List<String> ids = new ArrayList<>();
            ids.add(bugId);
            bug.put("id", ids);
            Object[] params = new Object[] { bug };

            Map<?,?> response = (Map<?,?>) redhatBugzillaClient.execute("Bug.search", params);
            Object[] responseBugs = (Object[]) (response.get("bugs"));
            String summary = (String) ((Map<?,?>)responseBugs[0]).get("summary");
            String url = String.format(REDHAT_BUG_URL, bugId);
            String component = (String) ((Object[])((Map<?,?>)responseBugs[0]).get("component"))[0];
            String product = (String) ((Map<?,?>)responseBugs[0]).get("product");
            String reply = new Bug("RH", bugId, summary, url, product, component).toString();
            logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
            event.respondWith(reply);
        } catch (XmlRpcException e) {
            String errMsg = e.getMessage();
            if (errMsg.contains("does not exist")) {
                String reply = String.format("Red Hat bug #%s does not exist", bugId);
                logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
                event.respondWith(reply);
            } else {
                logger.error(errMsg);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            String reply = String.format("Red Hat bug #%s not found", bugId);
            logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
            event.respondWith(reply);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            logger.error(errMsg);
        }
    }

    private void handleIcedteaBug(GenericMessageEvent event, Matcher matcher) {
        String bugId = matcher.group("bugId");
        try {
            Map<String, List<String>> bugs = new HashMap<>();
            List<String> ids = new ArrayList<>();
            ids.add(bugId);
            bugs.put("ids", ids);
            Object[] params = new Object[] {bugs};

            Map<?,?> response = (Map<?,?>) icedteaBugzillaClient.execute("Bug.get", params);

            Object[] responseBugs = (Object[]) (response.get("bugs"));
            String summary = (String) ((Map<?,?>)responseBugs[0]).get("summary");
            String url = String.format(ICEDTEA_BUG_URL, bugId);
            String component = (String) ((Map<?,?>)responseBugs[0]).get("component");
            String product = (String) ((Map<?,?>)responseBugs[0]).get("product");
            String reply = new Bug("PR", bugId, summary, url, product, component).toString();
            logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
            event.respondWith(reply);
        } catch (XmlRpcException e) {
            String errMsg = e.getMessage();
            if (errMsg.contains("does not exist")) {
                String reply = String.format("Iced Tea bug #%s does not exist", bugId);
                logger.info(String.format("[%s] %s", event.getBot().getNick(), reply));
                event.respondWith(reply);
            } else {
                logger.error(errMsg);
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            System.err.println(errMsg);
        }
    }
}
