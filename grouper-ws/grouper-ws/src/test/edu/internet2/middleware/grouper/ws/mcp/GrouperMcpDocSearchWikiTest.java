/*******************************************************************************
 * Copyright 2024 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex.DocSearchResult;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex.MarkdownDocument;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for doc_search sources which read markdown files from the filesystem,
 * which is how the Grouper wiki documentation shipped in the container is indexed
 *
 * @author mchyzer
 */
public class GrouperMcpDocSearchWikiTest extends GrouperTest {

  public GrouperMcpDocSearchWikiTest() {
  }

  public GrouperMcpDocSearchWikiTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpDocSearchWikiTest("testWikiSearch"));
    TestRunner.run(GrouperMcpDocSearchWikiTest.class);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** temp directories created by a test, deleted on teardown */
  private List<File> tempDirectories = new ArrayList<File>();

  /** config keys overridden by a test, removed on teardown */
  private List<String> configKeysOverridden = new ArrayList<String>();

  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting = null;
  }

  @Override
  protected void tearDown() {

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting = null;

    for (String configKey : this.configKeysOverridden) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(configKey);
    }
    this.configKeysOverridden.clear();

    for (File tempDirectory : this.tempDirectories) {
      try {
        FileUtils.deleteDirectory(tempDirectory);
      } catch (Exception e) {
        // ignore, this is a temp directory
      }
    }
    this.tempDirectories.clear();

    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * override a config property, and remember to remove it on teardown
   * @param key the config key
   * @param value the config value
   */
  private void overrideConfig(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
    this.configKeysOverridden.add(key);
  }

  /**
   * remove a config override which was set by overrideConfig
   * @param key the config key
   */
  private void removeConfig(String key) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    this.configKeysOverridden.remove(key);
  }

  /**
   * make a temp directory which is deleted on teardown
   * @return the directory
   */
  private File makeTempDirectory() {
    File tempDirectory = new File(System.getProperty("java.io.tmpdir"),
        "grouperMcpDocSearchTest_" + GrouperUtil.uniqueId());
    GrouperUtil.mkdirs(tempDirectory);
    this.tempDirectories.add(tempDirectory);
    return tempDirectory;
  }

  /**
   * write a markdown page with wiki style frontmatter into a directory
   * @param directory the source directory
   * @param relativePath the path of the file relative to the source directory
   * @param title the page title
   * @param body the page body
   */
  private void writeWikiPage(File directory, String relativePath, String title, String body) {
    File file = new File(directory, relativePath);
    GrouperUtil.mkdirs(file.getParentFile());
    String contents = "---\n"
        + "title: \"" + title + "\"\n"
        + "space: Grouper\n"
        + "pageId: 12345\n"
        + "version: 3\n"
        + "lastUpdated: 2026-07-12T06:23:52.199Z\n"
        + "url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/12345/"
        + title.replace(' ', '+') + "\n"
        + "---\n"
        + "\n"
        + body + "\n";
    GrouperUtil.saveStringIntoFile(file, contents);
  }

  /**
   * build a small wiki style directory tree and point the built-in grouperWiki source at it
   * @return the directory
   */
  private File configureWikiSource() {

    File directory = makeTempDirectory();

    writeWikiPage(directory, "Grouper_Wiki_Home/Grouper_Administration_Guides/"
        + "Provisioning_and_Integration.md", "Provisioning and Integration",
        "Integrating Grouper with an application may involve web services, the Grouper client, "
        + "LDAP, or SAML. You design the integration to suit the requirements of your site.");

    writeWikiPage(directory, "Grouper_Wiki_Home/Grouper_Administration_Guides/"
        + "Tools_Topics_for_Ongoing_Administration/GrouperShell_gsh.md", "GrouperShell gsh",
        "The Grouper shell is a command line interface for administering Grouper. "
        + "It can run scripts and templates against the Grouper API.");

    writeWikiPage(directory, "Grouper_Wiki_Home/Community_Contributions.md",
        "Community Contributions",
        "Institutions share their Grouper deployment notes and tooling here.");

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting =
        directory.getAbsolutePath();
    overrideConfig("grouper.mcp.docSearch.grouperWiki.reindexIntervalSeconds", "0");

    return directory;
  }

  /**
   * test parsing frontmatter out of a markdown file
   */
  public void testParseMarkdown() {

    MarkdownDocument markdownDocument = GrouperMcpDocSearchIndex.parseMarkdown(
        "---\n"
        + "title: \"Provisioning and Integration\"\n"
        + "space: Grouper\n"
        + "version: 6\n"
        + "lastUpdated: 2026-07-12T15:26:07.658Z\n"
        + "url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543540/Provisioning\n"
        + "---\n"
        + "\n"
        + "Integrating Grouper with an application.\n");

    assertEquals("Provisioning and Integration", markdownDocument.getTitle());
    assertEquals("https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543540/Provisioning",
        markdownDocument.getUrl());
    assertEquals("2026-07-12T15:26:07.658Z", markdownDocument.getLastUpdated());
    assertEquals("Integrating Grouper with an application.", markdownDocument.getBody().trim());
    assertFalse("Frontmatter should not be in the body",
        markdownDocument.getBody().contains("pageId"));
  }

  /**
   * test parsing a markdown file which has no frontmatter
   */
  public void testParseMarkdownNoFrontmatter() {

    MarkdownDocument markdownDocument = GrouperMcpDocSearchIndex.parseMarkdown(
        "# Some heading\n\nSome content about grouper.\n");

    assertNull(markdownDocument.getTitle());
    assertNull(markdownDocument.getUrl());
    assertEquals("# Some heading\n\nSome content about grouper.\n", markdownDocument.getBody());
  }

  /**
   * test that a leading dashes line which is never closed is treated as body, not frontmatter
   */
  public void testParseMarkdownUnterminatedFrontmatter() {

    String contents = "---\ntitle: \"Not really frontmatter\"\n\nsome content\n";

    MarkdownDocument markdownDocument = GrouperMcpDocSearchIndex.parseMarkdown(contents);

    assertNull(markdownDocument.getTitle());
    assertEquals(contents, markdownDocument.getBody());
  }

  /**
   * test that null and empty contents do not blow up
   */
  public void testParseMarkdownEmpty() {

    assertEquals("", GrouperMcpDocSearchIndex.parseMarkdown(null).getBody());
    assertEquals("", GrouperMcpDocSearchIndex.parseMarkdown("").getBody());
  }

  /**
   * test that the wiki source is not enabled when its directory does not exist
   */
  public void testWikiDisabledWhenDirectoryMissing() {

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting =
        new File(System.getProperty("java.io.tmpdir"), "grouperWikiDoesNotExist_"
            + GrouperUtil.uniqueId()).getAbsolutePath();

    assertFalse("Wiki source should be disabled when the directory is missing",
        GrouperMcpDocSearchIndex.isGrouperWikiEnabled());
    assertFalse(GrouperMcpDocSearchIndex.getConfigIds().contains(
        GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID));
  }

  /**
   * test that the wiki source can be turned off by config even when the directory is present
   */
  public void testWikiDisabledByConfig() {

    configureWikiSource();
    overrideConfig("grouper.mcp.docSearch.grouperWiki.enable", "false");

    assertFalse("Wiki source should be disabled by config",
        GrouperMcpDocSearchIndex.isGrouperWikiEnabled());
    assertFalse(GrouperMcpDocSearchIndex.getConfigIds().contains(
        GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID));
  }

  /**
   * test that the wiki source is enabled by default when the directory is present, and shows up
   * with documentation for the AI client
   */
  public void testWikiEnabledByDefault() {

    configureWikiSource();

    assertTrue("Wiki source should be enabled by default",
        GrouperMcpDocSearchIndex.isGrouperWikiEnabled());
    assertTrue(GrouperMcpDocSearchIndex.getConfigIds().contains(
        GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID));
    assertTrue("Expected default documentation for the AI client",
        GrouperMcpDocSearchIndex.getDocumentationForAiClient(
            GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID).contains("Grouper"));
    assertTrue("doc_search should be offered when the wiki is indexed",
        GrouperMcpDocSearchIndex.hasAnySourcesForSubject(SubjectTestHelper.SUBJ0));
  }

  /**
   * test searching the wiki source, and that results carry the url and breadcrumb
   */
  public void testWikiSearch() {

    configureWikiSource();

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search(
          "command line interface for administering", 10,
          GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID, SubjectTestHelper.SUBJ0);

      assertTrue("Expected results", results.size() > 0);

      DocSearchResult result = results.get(0);
      assertEquals("GrouperShell gsh", result.getName());
      assertEquals(GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID,
          result.getSourceConfigId());
      assertTrue("Expected the wiki url, got: " + result.getUrl(),
          result.getUrl().startsWith("https://grouper.atlassian.net/wiki/"));
      assertEquals("2026-07-12T06:23:52.199Z", result.getLastUpdated());

      // the breadcrumb from the directory structure should be on the chunk so the AI knows
      // what page a chunk came from
      assertTrue("Expected breadcrumb in content, got: " + result.getContent(),
          result.getContent().contains(
              "Grouper Wiki Home > Grouper Administration Guides > "
              + "Tools Topics for Ongoing Administration > GrouperShell gsh"));

      // the frontmatter itself should not be indexed
      assertFalse("Frontmatter should not be in the content",
          result.getContent().contains("pageId"));

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a match on the title of a page outranks a match only in the body of another page
   */
  public void testTitleBoost() {

    File directory = makeTempDirectory();

    // the page about kerberos, which mentions the word once, in its title
    writeWikiPage(directory, "Kerberos_Authentication.md", "Kerberos Authentication",
        "This page explains how to configure the authentication mechanism for your deployment "
        + "so that users can sign on to the user interface and the web services.");

    // a page which is not about kerberos, but mentions it once in the body
    writeWikiPage(directory, "Database_Setup.md", "Database Setup",
        "This page explains how to configure the database for your deployment. "
        + "Note that kerberos is not required for the database connection to work.");

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting =
        directory.getAbsolutePath();
    overrideConfig("grouper.mcp.docSearch.grouperWiki.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search("kerberos", 10,
          GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID, SubjectTestHelper.SUBJ0);

      assertEquals("Expected both pages to match", 2, results.size());
      assertEquals("The page titled about kerberos should rank first",
          "Kerberos Authentication", results.get(0).getName());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a boost on a source makes its results outrank an equally good match from
   * another source
   */
  public void testSourceBoost() {

    File institutionalDirectory = makeTempDirectory();
    writeWikiPage(institutionalDirectory, "Onboarding.md", "Onboarding",
        "Provisioning at our institution is handled by the identity management team.");

    File wikiDirectory = makeTempDirectory();
    writeWikiPage(wikiDirectory, "Provisioning.md", "Provisioning",
        "Provisioning at our institution is handled by the identity management team.");

    overrideConfig("grouper.mcp.docSearch.institutionalDocs.directory",
        institutionalDirectory.getAbsolutePath());
    overrideConfig("grouper.mcp.docSearch.institutionalDocs.reindexIntervalSeconds", "0");
    overrideConfig("grouper.mcp.docSearch.institutionalDocs.boost", "10.0");

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting =
        wikiDirectory.getAbsolutePath();
    overrideConfig("grouper.mcp.docSearch.grouperWiki.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search(
          "identity management team", 10, null, SubjectTestHelper.SUBJ0);

      assertTrue("Expected results from both sources", results.size() >= 2);
      assertEquals("The boosted source should rank first", "institutionalDocs",
          results.get(0).getSourceConfigId());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test the listNames and retrieveChunk actions against the wiki source
   */
  public void testWikiListNamesAndRetrieveChunk() {

    configureWikiSource();

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");
      arguments.put("sourceConfigId", GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID);

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success, got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      JsonNode responseNode = objectMapper.readTree(
          result.get("content").get(0).get("text").asText());
      assertEquals(3, responseNode.get("totalNames").asInt());

      boolean foundGsh = false;
      for (JsonNode nameNode : responseNode.get("names")) {
        if ("GrouperShell gsh".equals(nameNode.asText())) {
          foundGsh = true;
        }
      }
      assertTrue("Expected the gsh page in listNames: " + responseNode.toString(), foundGsh);

      // now retrieve chunk 0 of that page by name
      arguments = objectMapper.createObjectNode();
      arguments.put("action", "retrieveChunk");
      arguments.put("sourceConfigId", GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID);
      arguments.put("name", "GrouperShell gsh");
      arguments.set("chunkIndexes", objectMapper.createArrayNode().add(0));

      result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success, got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      responseNode = objectMapper.readTree(result.get("content").get(0).get("text").asText());
      String text = responseNode.toString();
      assertTrue("Expected the gsh content, got: " + text,
          text.contains("command line interface"));

    } catch (Exception e) {
      throw new RuntimeException("Unexpected exception", e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that filtering a search by sourceConfigId still finds documents in a small source when
   * a much larger source, e.g. the wiki, matches the same query many more times.  the source
   * filter has to be part of the lucene query, because filtering after the search lets the big
   * source fill the result window and starve the source which was asked for
   */
  public void testSourceFilterNotStarvedByLargeSource() {

    // a small institutional source whose one matching page is a weak match: the phrase is
    // buried in a long body, so it scores well below the pages in the other source
    StringBuilder longBody = new StringBuilder();
    longBody.append("This page covers local deployment notes for our campus. ");
    for (int i = 0; i < 40; i++) {
      longBody.append("Assorted unrelated background about folders, loaders and reports. ");
    }
    longBody.append("Escalations go to the identity management team. ");
    for (int i = 0; i < 40; i++) {
      longBody.append("More unrelated background about subjects, sources and daemons. ");
    }

    File institutionalDirectory = makeTempDirectory();
    writeWikiPage(institutionalDirectory, "Local_Notes.md", "Local Notes",
        longBody.toString());

    // a much larger source whose pages are all tighter matches for the same query
    File wikiDirectory = makeTempDirectory();
    for (int i = 0; i < 60; i++) {
      writeWikiPage(wikiDirectory, "Escalation_Page_" + i + ".md", "Escalation Page " + i,
          "Contact the identity management team.");
    }

    overrideConfig("grouper.mcp.docSearch.institutionalDocs.directory",
        institutionalDirectory.getAbsolutePath());
    overrideConfig("grouper.mcp.docSearch.institutionalDocs.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting =
        wikiDirectory.getAbsolutePath();
    overrideConfig("grouper.mcp.docSearch.grouperWiki.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search(
          "identity management team", 3, "institutionalDocs", SubjectTestHelper.SUBJ0);

      assertTrue("Expected the institutional page even though the other source matches "
          + "more often and scores higher", results.size() > 0);
      for (DocSearchResult result : results) {
        assertEquals("Local Notes", result.getName());
        assertEquals("institutionalDocs", result.getSourceConfigId());
      }

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that each source has its own index, so several sources are searchable together, and
   * that a source which is removed from configuration is dropped from the index
   */
  public void testSourceRemovedFromConfigIsDropped() {

    File institutionalDirectory = makeTempDirectory();
    writeWikiPage(institutionalDirectory, "Local_Notes.md", "Local Notes",
        "Our campus uses the xyzzyplugh convention for naming folders.");

    configureWikiSource();

    overrideConfig("grouper.mcp.docSearch.institutionalDocs.directory",
        institutionalDirectory.getAbsolutePath());
    overrideConfig("grouper.mcp.docSearch.institutionalDocs.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // both sources are searchable through the one composite reader
      assertTrue("Expected the institutional source to be searchable",
          GrouperMcpDocSearchIndex.search("xyzzyplugh", 10, null,
              SubjectTestHelper.SUBJ0).size() > 0);
      assertTrue("Expected the wiki source to be searchable",
          GrouperMcpDocSearchIndex.search("command line interface", 10, null,
              SubjectTestHelper.SUBJ0).size() > 0);

      // drop the institutional source from configuration
      removeConfig("grouper.mcp.docSearch.institutionalDocs.directory");
      removeConfig("grouper.mcp.docSearch.institutionalDocs.reindexIntervalSeconds");

      GrouperMcpDocSearchIndex.forceRebuild();

      assertEquals("Removed source should be dropped from the index", 0,
          GrouperMcpDocSearchIndex.search("xyzzyplugh", 10, null,
              SubjectTestHelper.SUBJ0).size());
      assertTrue("The remaining source should still be searchable",
          GrouperMcpDocSearchIndex.search("command line interface", 10, null,
              SubjectTestHelper.SUBJ0).size() > 0);

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a source whose content cannot be loaded, here a SQL source whose query does not
   * run, does not take doc search down for the sources which are fine.  the broken source must
   * not leave the index reporting "still building", which would make every doc_search call tell
   * the client to retry forever.
   *
   * <p>Note this exercises the graceful path, where loading the source's content fails and the
   * source ends up with an empty index. The safety net in handleFailedBuild, for an unexpected
   * error in the build itself, is not reachable from configuration and is not covered here.</p>
   */
  public void testBrokenSourceDoesNotBlockOtherSources() {

    configureWikiSource();

    // a SQL source whose query cannot run
    overrideConfig("grouper.mcp.docSearch.brokenDocs.externalSystemId", "grouper");
    overrideConfig("grouper.mcp.docSearch.brokenDocs.query",
        "select this_is_not_a_column from this_table_does_not_exist_xyzzy");
    overrideConfig("grouper.mcp.docSearch.brokenDocs.reindexIntervalSeconds", "0");

    GrouperMcpDocSearchIndex.forceRebuild();

    assertTrue("A source which fails to build must still count as built, otherwise doc search "
        + "reports 'still building' forever", GrouperMcpDocSearchIndex.isIndexReady());

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertTrue("The healthy source should still be searchable",
          GrouperMcpDocSearchIndex.search("command line interface", 10, null,
              SubjectTestHelper.SUBJ0).size() > 0);

      assertEquals("The broken source should contribute no results", 0,
          GrouperMcpDocSearchIndex.search("command line interface", 10, "brokenDocs",
              SubjectTestHelper.SUBJ0).size());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that the index reports ready even when nothing is configured to index.  callers use
   * isIndexReady to tell "still building, retry" from "searched and found nothing", so an
   * install with no sources must not look like it is permanently building
   */
  public void testIndexReadyWithNoSourcesConfigured() {

    overrideConfig("grouper.mcp.docSearch.grouperWiki.enable", "false");

    GrouperMcpDocSearchIndex.forceRebuild();

    assertTrue("Index should be ready even with no sources configured",
        GrouperMcpDocSearchIndex.isIndexReady());
  }

  /**
   * search while the index is being rebuilt underneath.
   *
   * <p>Each source builds into a new index which is swapped in, and readers are reference
   * counted so that a search already running against an older composite keeps working. That
   * only matters when a swap happens during a search, which no single threaded test can
   * produce. This does not prove the reference counting correct, but a reader being closed
   * while still in use shows up here as an exception or as an empty result, and would
   * otherwise only show up in production under load.</p>
   */
  public void testSearchDuringConcurrentRebuild() {

    configureWikiSource();

    // configureWikiSource reindexes on every call, which would have the searcher threads
    // spawning background builds too.  the swaps under test come from the rebuild thread, so
    // keep the searchers from starting builds of their own
    overrideConfig("grouper.mcp.docSearch.grouperWiki.reindexIntervalSeconds", "3600");

    GrouperMcpDocSearchIndex.forceRebuild();

    final List<Throwable> failures = Collections.synchronizedList(new ArrayList<Throwable>());
    final AtomicInteger emptyResultCount = new AtomicInteger(0);
    final AtomicInteger searchCount = new AtomicInteger(0);
    final AtomicBoolean stop = new AtomicBoolean(false);

    List<Thread> threads = new ArrayList<Thread>();

    // searchers.  a null subject skips privacy realm checks, which keeps this test on the
    // reader lifecycle rather than on authorization
    for (int i = 0; i < 4; i++) {
      Thread searchThread = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            while (!stop.get()) {
              List<DocSearchResult> results = GrouperMcpDocSearchIndex.search(
                  "command line interface", 10, null, null);
              searchCount.incrementAndGet();
              if (results.isEmpty()) {
                emptyResultCount.incrementAndGet();
              }
            }
          } catch (Throwable t) {
            failures.add(t);
          }
        }

      }, "docSearchTestSearcher-" + i);
      threads.add(searchThread);
    }

    // rebuilder, swapping indexes out from under the searchers
    Thread rebuildThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          while (!stop.get()) {
            GrouperMcpDocSearchIndex.forceRebuild();
          }
        } catch (Throwable t) {
          failures.add(t);
        }
      }

    }, "docSearchTestRebuilder");
    threads.add(rebuildThread);

    for (Thread thread : threads) {
      thread.start();
    }

    GrouperUtil.sleep(3000);
    stop.set(true);

    for (Thread thread : threads) {
      try {
        thread.join(30000);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }

    if (!failures.isEmpty()) {
      throw new RuntimeException("Searching during rebuild threw "
          + failures.size() + " failure(s), first: " + failures.get(0), failures.get(0));
    }

    assertTrue("Expected searches to have run", searchCount.get() > 0);

    // the index always has content, so an empty result means a search ran against a reader
    // which had been swapped out or closed underneath it
    assertEquals("Searches returned no results while the index was being rebuilt, after "
        + searchCount.get() + " searches", 0, emptyResultCount.get());
  }

  /**
   * test that files which are not markdown are not indexed
   */
  public void testNonMarkdownFilesSkipped() {

    File directory = configureWikiSource();

    GrouperUtil.saveStringIntoFile(new File(directory, "notes.txt"),
        "This text file mentions xyzzyplugh and should not be indexed.");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search("xyzzyplugh", 10,
          GrouperMcpDocSearchIndex.GROUPER_WIKI_SOURCE_CONFIG_ID, SubjectTestHelper.SUBJ0);

      assertEquals("Text files should not be indexed", 0, results.size());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

}
