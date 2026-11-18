/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (loaderJobHistoryTooltipStoredXss), browser check: the daemon job history
 * chart puts each loader log row's job name into a tooltip that gantt-chart-d3.js inserts with
 * d3 .html() on mouseover.  UiV2AdminJobHistoryTooltipTest checks the tooltip string the helper
 * builds; this test checks what that test cannot: that the running UI uses the patched
 * UiV2Admin (and the new helper class is deployed), and that hovering the bar of a job whose
 * name holds markup shows the markup as text, with no script run and no injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  It stores one grouper_loader_log row
 * (job name contains secPatch143) and deletes it by id in finally.</p>
 *
 * <p>Login is the UI's form login (or HTTP basic) as GrouperSystem/pass (local dev).  Raw
 * Playwright API so the same file works on every release that has Playwright.</p>
 */
public class UiV2AdminJobHistoryTooltipBrowserTest extends TestCase {

  /** stored job name: a group loader job name holds the group name, which can hold markup */
  private static final String JOB_NAME =
      "SQL_SIMPLE__test:secPatch143<img id=secPatch143xss src=x onerror=alert(143)>__secPatch143";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2AdminJobHistoryTooltipBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2AdminJobHistoryTooltipBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AdminJobHistoryTooltipBrowserTest(String name) {
    super(name);
  }

  /**
   * base url of the running UI: grouper.ui.url (what GrouperPage uses) if set, else the same
   * junit.test.tomcat.* keys the mock tests use
   * @return e.g. http://localhost:8080/grouper
   */
  private static String uiBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    String uiUrl = config.propertyValueString("grouper.ui.url");
    if (uiUrl != null && uiUrl.trim().length() > 0) {
      return uiUrl.trim().replaceAll("/+$", "");
    }
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String host = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    String context = config.propertyValueString("junit.test.tomcat.uiContext", "grouper");
    return (ssl ? "https" : "http") + "://" + host + ":" + port + "/" + context;
  }

  /**
   * log in through the UI's form login, or HTTP basic if the UI answers 401
   * @param page
   * @param baseUrl
   */
  private static void login(Page page, String baseUrl) {
    com.microsoft.playwright.Response loginResponse = page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    if (loginResponse != null && loginResponse.status() == 401) {
      // this UI uses HTTP basic authentication (grouper.is.ui.basicAuthn), not the form login
      page.setExtraHTTPHeaders(java.util.Collections.singletonMap("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString(("GrouperSystem" + ":pass").getBytes(java.nio.charset.StandardCharsets.UTF_8))));
      page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain");
    }
    if (page.locator("input[type=password]").count() > 0) {
      page.locator("input[type=text]").first().fill("GrouperSystem");
      page.locator("input[type=password]").first().fill("pass");
      page.locator("input[type=password]").first().press("Enter");
      // logged in when the login form is gone (waitForURL races a fast login on Playwright 1.46, whose
      // waitForURL always waits for a new navigation); 60 s for the first login after a Tomcat start
      page.locator("input[type=password]").first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
          .setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED).setTimeout(60000));
    }
  }

  /**
   * open the chart, filter to this test's job, and hover its bar
   * @param page
   * @param baseUrl
   * @return the tooltip div that shows this test's job
   */
  private static Locator hoverJobBar(Page page, String baseUrl) {
    page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Admin.jobHistoryChart");
    page.locator("#names-like-filter").waitFor();
    page.locator("#names-like-filter").fill("%secPatch143%");
    page.locator("#min-elapsed-seconds").fill("0");
    page.evaluate("id => document.getElementById(id).setAttribute('data-secpatch-old', 'true')", "names-like-filter");
    page.locator("#jobHistoryChartSubmitButtonId").click();
    page.locator("#names-like-filter:not([data-secpatch-old])").waitFor();
    // the bars are svg rects in the chart pane; the filter leaves only this test's job
    // fire the hover events on the bar itself: a 1px bar is covered by the chart svg (and briefly
    // the blockUI overlay), so a real mouse hover lands on something else
    Locator bar = page.locator("#jobHistoryChartPaneDiv rect").first();
    bar.waitFor();
    bar.dispatchEvent("mouseover");
    bar.dispatchEvent("mousemove");
    // every chart draw appends a tooltip div to body; take the one showing this job
    Locator tooltip = page.locator("div.d3tooltip", new Page.LocatorOptions().setHasText("secPatch143")).last();
    tooltip.waitFor();
    return tooltip;
  }

  /**
   * hovering the bar of a job named with markup shows the markup as text
   * @throws Exception
   */
  public void testTooltipShowsJobNameAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // one finished job a minute ago, long enough for any minimum elapsed filter
    long now = System.currentTimeMillis();
    Hib3GrouperLoaderLog log = new Hib3GrouperLoaderLog();
    log.setJobName(JOB_NAME);
    log.setJobType("SQL_SIMPLE");
    log.setStatus("SUCCESS");
    log.setHost("secPatch143");
    log.setStartedTime(new Timestamp(now - 120000L));
    log.setEndedTime(new Timestamp(now - 60000L));
    log.setMillis(60000);
    log.setInsertCount(0);
    log.setUpdateCount(0);
    log.setDeleteCount(0);
    log.setTotalCount(0);
    log.store();
    String logId = log.getId();
    assertNotNull("loader log id", logId);

    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      // any alert/confirm/prompt means the payload ran
      final List<String> dialogs = new ArrayList<String>();
      page.onDialog(dialog -> {
        dialogs.add(dialog.message());
        dialog.dismiss();
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      Locator tooltip;
      try {
        tooltip = hoverJobBar(page, baseUrl);
      } catch (com.microsoft.playwright.TimeoutError te) {
        // one retry for a busy shared Tomcat
        tooltip = hoverJobBar(page, baseUrl);
      }

      String text = tooltip.textContent();
      assertTrue("tooltip must show the job name markup as text: " + text,
          text.contains("<img id=secPatch143xss src=x onerror=alert(143)>"));
      assertEquals("payload must not add an element", 0, page.locator("#secPatch143xss").count());
      // the line breaks are still real br elements (layout unchanged)
      assertTrue("tooltip keeps its line breaks", tooltip.locator("br").count() >= 3);

      // give a late onerror a moment to fire before checking
      Thread.sleep(1000);
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      // id is a generated uuid hex, safe to inline
      HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where id = '" + logId + "'");
    }
    System.out.println("^secPatch143 browser test ran");
  }

}
