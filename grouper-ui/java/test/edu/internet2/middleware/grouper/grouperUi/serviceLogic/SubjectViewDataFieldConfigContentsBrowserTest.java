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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Security advisory H5 (entityDataStoredXss), browser check: the subject data field assignments
 * screen (subjectViewDataFieldConfigContents.jsp) renders entity data values that come from a data
 * provider, and must escape them.  SubjectViewDataFieldConfigContentsJspTest checks the JSP source;
 * this test checks what the JUnit test cannot: that the patched JSP compiles in the running UI and
 * shows a scalar (attribute) string value and a data row string value as literal text, with no
 * script run and no injected element.
 *
 * <p>Runs against the running local UI (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running UI uses the same database.  Setup and cleanup are this test's own:
 * subject secPatch27 in the example JDBC subject table, entity data config secPatch27* (public
 * privacy realm, a scalar field, a row column field, a row, a provider with two SQL queries against
 * the grouper database) and its full sync job otherJob.secPatch27Sync, run once in this JVM.  At the
 * end the config, the synced data (assignments, aliases, field/row/provider rows), the member and
 * the subject are deleted.</p>
 *
 * <p>Login is the UI's form login as GrouperSystem/pass (local dev).  Uses the raw Playwright API
 * (not GrouperPage) so the same file works on releases without the app.browser helpers.</p>
 */
public class SubjectViewDataFieldConfigContentsBrowserTest extends TestCase {

  /** subject the provider assigns data to */
  private static final String SUBJECT_ID = "secPatch27";

  /** privacy realm (public, so GrouperSystem can read values) */
  private static final String REALM = "secPatch27realm";

  /** scalar string field */
  private static final String ATTR_FIELD = "secPatch27attrField";

  /** row column string field */
  private static final String ROW_FIELD = "secPatch27rowField";

  /** data row */
  private static final String ROW = "secPatch27row";

  /** data provider */
  private static final String PROVIDER = "secPatch27provider";

  /** full sync job (otherJob config id) */
  private static final String JOB = "secPatch27Sync";

  /** scalar value; adds an img if it is not escaped */
  private static final String ATTR_PAYLOAD = "<img id=secPatch27xssAttr src=x onerror=alert(271)>";

  /** row value; adds an img if it is not escaped */
  private static final String ROW_PAYLOAD = "<img id=secPatch27xssRow src=x onerror=alert(272)>";

  /** grouper.properties keys and values this test stores */
  private static final String[][] GROUPER_KEYS = {
      {"grouperPrivacyRealm." + REALM + ".privacyRealmName", REALM},
      {"grouperPrivacyRealm." + REALM + ".privacyRealmPublic", "true"},

      {"grouperDataField." + ATTR_FIELD + ".fieldAliases", ATTR_FIELD},
      {"grouperDataField." + ATTR_FIELD + ".fieldPrivacyRealm", REALM},
      {"grouperDataField." + ATTR_FIELD + ".descriptionHtml", ATTR_FIELD},
      {"grouperDataField." + ATTR_FIELD + ".fieldDataType", "string"},
      {"grouperDataField." + ATTR_FIELD + ".fieldDataStructure", "attribute"},
      {"grouperDataField." + ATTR_FIELD + ".fieldDataAssignableTo", "individuals"},

      {"grouperDataField." + ROW_FIELD + ".fieldAliases", ROW_FIELD},
      {"grouperDataField." + ROW_FIELD + ".fieldPrivacyRealm", REALM},
      {"grouperDataField." + ROW_FIELD + ".descriptionHtml", ROW_FIELD},
      {"grouperDataField." + ROW_FIELD + ".fieldDataType", "string"},
      {"grouperDataField." + ROW_FIELD + ".fieldDataStructure", "rowColumn"},
      {"grouperDataField." + ROW_FIELD + ".fieldDataAssignableTo", "individuals"},

      {"grouperDataRow." + ROW + ".rowAliases", ROW},
      {"grouperDataRow." + ROW + ".rowPrivacyRealm", REALM},
      {"grouperDataRow." + ROW + ".descriptionHtml", ROW},
      {"grouperDataRow." + ROW + ".oneRowPerSubject", "true"},
      {"grouperDataRow." + ROW + ".rowNumberOfDataFields", "1"},
      {"grouperDataRow." + ROW + ".rowDataField.0.colDataFieldConfigId", ROW_FIELD},

      {"grouperDataProvider." + PROVIDER + ".name", PROVIDER},

      {"grouperDataProviderQuery.secPatch27attrQuery.providerConfigId", PROVIDER},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryType", "sql"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQuerySqlConfigId", "grouper"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQuerySqlQuery",
        "select '" + SUBJECT_ID + "' as subject_id, '" + ATTR_PAYLOAD + "' as attr_val"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryDataStructure", "attribute"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQuerySubjectIdAttribute", "subject_id"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQuerySubjectIdType", "subjectId"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQuerySubjectSourceId", "jdbc"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryNumberOfDataFields", "1"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryDataField.0.providerDataFieldConfigId", ATTR_FIELD},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryDataField.0.providerDataFieldMappingType", "attribute"},
      {"grouperDataProviderQuery.secPatch27attrQuery.providerQueryDataField.0.providerDataFieldAttribute", "attr_val"},

      {"grouperDataProviderQuery.secPatch27rowQuery.providerConfigId", PROVIDER},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryType", "sql"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQuerySqlConfigId", "grouper"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQuerySqlQuery",
        "select '" + SUBJECT_ID + "' as subject_id, '" + ROW_PAYLOAD + "' as row_val"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryDataStructure", "row"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryRowConfigId", ROW},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQuerySubjectIdAttribute", "subject_id"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQuerySubjectIdType", "subjectId"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQuerySubjectSourceId", "jdbc"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryNumberOfDataFields", "1"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryDataField.0.providerDataFieldConfigId", ROW_FIELD},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryDataField.0.providerDataFieldMappingType", "attribute"},
      {"grouperDataProviderQuery.secPatch27rowQuery.providerQueryDataField.0.providerDataFieldAttribute", "row_val"},
  };

  /** grouper-loader.properties keys and values this test stores */
  private static final String[][] LOADER_KEYS = {
      {"otherJob." + JOB + ".class", GrouperDataProviderFullSyncJob.class.getName()},
      // never fires while the test runs (3am); the test runs the job itself
      {"otherJob." + JOB + ".quartzCron", "0 0 3 * * ?"},
      {"otherJob." + JOB + ".dataProviderConfigId", PROVIDER},
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(SubjectViewDataFieldConfigContentsBrowserTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public SubjectViewDataFieldConfigContentsBrowserTest() {
    super();
  }

  /**
   * @param name
   */
  public SubjectViewDataFieldConfigContentsBrowserTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup helpers

  /**
   * subject, config, and one run of the full sync job
   */
  private static void createData() {
    HibernateSession.bySqlStatic().executeSql(
        "insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        GrouperUtil.toListObject(SUBJECT_ID, "secPatch27 entity data xss proof"));
    // GrouperDbConfig checks the caller may edit config, so it needs a (root) session
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : GROUPER_KEYS) {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName(key[0]).value(key[1]).store();
      }
      for (String[] key : LOADER_KEYS) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key[0]).value(key[1]).store();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_" + JOB);
  }

  /**
   * run one cleanup statement, keep going on error
   * @param sql
   * @param param
   */
  private static void sql(String sql, String param) {
    try {
      HibernateSession.bySqlStatic().executeSql(sql, GrouperUtil.toListObject(param));
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  /**
   * delete config, synced data, member and subject
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] key : GROUPER_KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper.properties").propertyName(key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
      for (String[] key : LOADER_KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // synced data, children first (see the foreign keys on the grouper_data_* tables)
    String fields = "(select internal_id from grouper_data_field where config_id like 'secPatch27%')";
    String rows = "(select internal_id from grouper_data_row where config_id like 'secPatch27%')";
    String providers = "(select internal_id from grouper_data_provider where config_id like 'secPatch27%')";
    String like = "secPatch27%";
    sql("delete from grouper_data_row_field_asn_hst where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_field_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_assign_hst where data_row_internal_id in " + rows + " and 'x' <> ?", like);
    sql("delete from grouper_data_row_assign where data_row_internal_id in " + rows + " and 'x' <> ?", like);
    sql("delete from grouper_data_field_assign_hst where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_field_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_global_assign where data_field_internal_id in " + fields + " and 'x' <> ?", like);
    sql("delete from grouper_data_alias where (data_field_internal_id in " + fields
        + " or data_row_internal_id in " + rows + ") and 'x' <> ?", like);
    sql("delete from grouper_data_field where config_id like ?", like);
    sql("delete from grouper_data_row where config_id like ?", like);
    sql("delete from grouper_data_provider where config_id like ?", like);
    // the sync job's bookkeeping rows (grouper_sync named dataProvider_secPatch27provider)
    for (String table : new String[] {"grouper_sync_log", "grouper_sync_member", "grouper_sync_job"}) {
      sql("delete from " + table + " where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?)",
          "dataProvider_" + PROVIDER);
    }
    sql("delete from grouper_sync where provisioner_name = ?", "dataProvider_" + PROVIDER);
    sql("delete from grouper_loader_log where job_name = ?", "OTHER_JOB_" + JOB);
    sql("delete from grouper_members where subject_id = ? and subject_source = 'jdbc'", SUBJECT_ID);
    sql("delete from subject where subjectid = ?", SUBJECT_ID);
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

  // ---------------------------------------------------------------- browser helpers

  /**
   * log in through the UI's form login
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

  // ---------------------------------------------------------------- test

  /**
   * the scalar value and the row value show as literal text; no dialog opens and no element from
   * a payload lands in the page
   * @throws Exception
   */
  public void testEntityDataValuesRenderedAsText() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    // a previous aborted run may have left data behind
    deleteData();
    createData();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newContext().newPage();
      // fail in seconds, not the 30 second Playwright default, when a screen does not render
      page.setDefaultTimeout(15000);

      // any alert/confirm/prompt means a payload ran
      final List<String> dialogs = new ArrayList<String>();
      page.onDialog(dialog -> {
        dialogs.add(dialog.message());
        dialog.dismiss();
      });

      String baseUrl = uiBaseUrl();
      login(page, baseUrl);

      // the running UI reads the entity data config through a cache; retry until it shows the
      // row (an unescaped payload is an element, not text, so without the fix this never matches)
      String text = null;
      for (int i = 0; i < 10; i++) {
        page.navigate(baseUrl + "/grouperUi/app/UiV2Main.index?operation=UiV2Subject.viewDataFieldAssignments&subjectId="
            + SUBJECT_ID + "&sourceId=jdbc");
        page.locator("#grouperMainContentDivId").waitFor();
        Thread.sleep(1000);
        text = page.locator("#grouperMainContentDivId").textContent();
        if (text.contains(ROW_PAYLOAD) && text.contains(ATTR_PAYLOAD)) {
          break;
        }
        if (page.locator("[id^=secPatch27xss]").count() > 0) {
          break;
        }
        Thread.sleep(4000);
      }

      assertEquals("payload must not add an element", 0, page.locator("[id^=secPatch27xss]").count());
      assertTrue("row value as literal text: " + text, text.contains(ROW_PAYLOAD));
      assertTrue("scalar value as literal text: " + text, text.contains(ATTR_PAYLOAD));
      assertTrue("row config id shown", text.contains(ROW));
      assertTrue("no dialog may open: " + dialogs, dialogs.isEmpty());

      browser.close();
    } finally {
      deleteData();
    }
  }

}
