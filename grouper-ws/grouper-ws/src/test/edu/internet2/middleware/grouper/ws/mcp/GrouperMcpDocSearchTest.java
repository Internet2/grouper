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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex.DocSearchResult;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpDocSearch (doc_search MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpDocSearchTest extends GrouperTest {

  public GrouperMcpDocSearchTest() {
  }

  public GrouperMcpDocSearchTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpDocSearchTest("testDocSearchBasic"));
    TestRunner.run(GrouperMcpDocSearchTest.class);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    // these tests assert on exactly which sources are configured, and the shipped Grouper wiki
    // is a source which is on by default when its directory is present, e.g. in the container.
    // it is covered by GrouperMcpDocSearchWikiTest
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.grouperWiki.enable", "false");
    GrouperMcpDocSearchIndex.grouperWikiDirectoryOverrideForTesting = null;

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    // create the test content table
    try {
      new GcDbAccess().connectionName("grouper")
          .sql("create table test_doc_content (url varchar(2000), name varchar(500), content varchar(4000))")
          .executeSql();
    } catch (Exception e) {
      // table might already exist
    }

    // clear any existing data
    new GcDbAccess().connectionName("grouper")
        .sql("delete from test_doc_content").executeSql();
  }

  @Override
  protected void tearDown() {
    // clean up config overrides
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs.externalSystemId");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs.query");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs.documentationForAiClient");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs.reindexIntervalSeconds");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.grouperWiki.enable");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs2.externalSystemId");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs2.query");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs2.documentationForAiClient");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.mcp.docSearch.testDocs2.reindexIntervalSeconds");

    // drop test table
    try {
      new GcDbAccess().connectionName("grouper")
          .sql("drop table test_doc_content").executeSql();
    } catch (Exception e) {
      // ignore
    }

    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * configure the doc search source for testing
   */
  private void configureDocSearchSource() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs.externalSystemId", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs.query",
        "select content as grouper_content, url as grouper_url, name as grouper_name from test_doc_content");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs.documentationForAiClient",
        "Test documentation for unit testing");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs.reindexIntervalSeconds", "0");
  }

  /**
   * configure a second doc search source, which indexes only one of the test rows, so that
   * filtering by sourceConfigId has something to exclude
   */
  private void configureSecondDocSearchSource() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs2.externalSystemId", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs2.query",
        "select content as grouper_content, url as grouper_url, name as grouper_name "
        + "from test_doc_content where name = 'Second Source Page'");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs2.documentationForAiClient",
        "Second test documentation source for unit testing");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.mcp.docSearch.testDocs2.reindexIntervalSeconds", "0");
  }

  /**
   * insert test content into the database
   */
  private void insertTestContent(String url, String name, String content) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into test_doc_content (url, name, content) values (?, ?, ?)")
        .addBindVar(url).addBindVar(name).addBindVar(content)
        .executeSql();
  }

  /**
   * test chunking of content
   */
  public void testChunkContent() {

    // small content should be a single chunk
    List<String> chunks = GrouperMcpDocSearchIndex.chunkContent("Hello world");
    assertEquals(1, chunks.size());
    assertEquals("Hello world", chunks.get(0));

    // empty content should return empty list
    chunks = GrouperMcpDocSearchIndex.chunkContent("");
    assertEquals(0, chunks.size());

    // null content should return empty list
    chunks = GrouperMcpDocSearchIndex.chunkContent(null);
    assertEquals(0, chunks.size());

    // large content should be chunked
    StringBuilder largeContent = new StringBuilder();
    for (int i = 0; i < 200; i++) {
      largeContent.append("This is paragraph ").append(i)
          .append(" with some content about grouper access management.\n\n");
    }
    chunks = GrouperMcpDocSearchIndex.chunkContent(largeContent.toString());
    assertTrue("Expected multiple chunks, got: " + chunks.size(), chunks.size() > 1);

    // verify chunks have overlap: a paragraph line from the end of chunk N-1
    // should appear somewhere in the beginning of chunk N
    for (int i = 1; i < chunks.size(); i++) {
      String prevChunk = chunks.get(i - 1);
      String thisChunk = chunks.get(i);
      // grab the last full paragraph line from the previous chunk
      String[] prevLines = prevChunk.split("\n");
      String lastLine = null;
      for (int j = prevLines.length - 1; j >= 0; j--) {
        if (prevLines[j].trim().length() > 0) {
          lastLine = prevLines[j].trim();
          break;
        }
      }
      if (lastLine != null) {
        // the beginning of the next chunk should contain some overlapping content
        String thisStart = thisChunk.substring(0, Math.min(thisChunk.length(),
            600));
        assertTrue("Expected overlap between chunks " + (i - 1) + " and " + i,
            thisStart.contains(lastLine));
      }
    }
  }

  /**
   * test basic doc search with configured source
   */
  public void testDocSearchBasic() {

    configureDocSearchSource();

    insertTestContent("https://example.com/groups", "Managing Groups",
        "Grouper is an enterprise access management system. "
        + "It allows administrators to create and manage groups for access control. "
        + "Groups can be organized into folders called stems.");

    insertTestContent("https://example.com/provisioning", "Provisioning Guide",
        "Provisioning in Grouper pushes group membership data to external systems. "
        + "Supported targets include LDAP, Active Directory, and cloud services like Azure and Google.");

    // force rebuild so our test data is indexed
    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "access management groups");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected results", responseNode.get("totalResults").asInt() > 0);

      JsonNode results = responseNode.get("results");
      assertNotNull(results);
      assertTrue(results.isArray());
      assertTrue(results.size() > 0);

      // first result should have content, name, sourceConfigId, totalChunksForDocument
      JsonNode firstResult = results.get(0);
      assertNotNull(firstResult.get("content"));
      assertTrue(firstResult.get("content").asText().length() > 0);
      assertEquals("testDocs", firstResult.get("sourceConfigId").asText());
      assertNotNull("Expected name", firstResult.get("name"));
      assertTrue("Expected totalChunksForDocument",
          firstResult.has("totalChunksForDocument"));
      assertEquals(1, firstResult.get("totalChunksForDocument").asInt());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test doc search with no results
   */
  public void testDocSearchNoResults() {

    configureDocSearchSource();

    insertTestContent("https://example.com/page1", "Page One",
        "This page is about cats and dogs and pets.");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "quantum physics thermodynamics");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(0, responseNode.get("totalResults").asInt());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test doc search with no sources configured returns error
   */
  public void testDocSearchNoSourcesConfigured() {

    // don't call configureDocSearchSource()

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "test query");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test doc search with missing query returns error
   */
  public void testDocSearchMissingQuery() {

    configureDocSearchSource();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      // no query parameter

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertTrue("Expected error", result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test doc search with sourceConfigId filter.
   *
   * <p>Two sources index overlapping content, so that filtering has something to exclude.
   * Asserting only that the call succeeds would pass even if the filter did nothing at all.</p>
   */
  public void testDocSearchSourceFilter() {

    configureDocSearchSource();
    configureSecondDocSearchSource();

    insertTestContent("https://example.com/page1", "Test Page",
        "This is test content about grouper access management for filtering test.");
    insertTestContent("https://example.com/page2", "Second Source Page",
        "This is other content about grouper access management for filtering test.");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // unfiltered, both sources should be represented
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "grouper access management");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      Set<String> unfilteredSources = sourceConfigIdsOfResults(result);
      assertTrue("Expected results from testDocs, got: " + unfilteredSources,
          unfilteredSources.contains("testDocs"));
      assertTrue("Expected results from testDocs2, got: " + unfilteredSources,
          unfilteredSources.contains("testDocs2"));

      // filtered to the second source, only that source may come back
      arguments = objectMapper.createObjectNode();
      arguments.put("query", "grouper access management");
      arguments.put("sourceConfigId", "testDocs2");

      result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      Set<String> filteredSources = sourceConfigIdsOfResults(result);
      assertFalse("Expected results when filtering to testDocs2", filteredSources.isEmpty());
      assertEquals("Filtered search must only return the requested source, got: "
          + filteredSources, 1, filteredSources.size());
      assertTrue("Expected only testDocs2, got: " + filteredSources,
          filteredSources.contains("testDocs2"));

      // and the same the other way around
      arguments = objectMapper.createObjectNode();
      arguments.put("query", "grouper access management");
      arguments.put("sourceConfigId", "testDocs");

      result = GrouperMcpDocSearch.execute(arguments, authUser);
      filteredSources = sourceConfigIdsOfResults(result);
      assertFalse("Expected results when filtering to testDocs", filteredSources.isEmpty());
      assertEquals("Filtered search must only return the requested source, got: "
          + filteredSources, 1, filteredSources.size());
      assertTrue("Expected only testDocs, got: " + filteredSources,
          filteredSources.contains("testDocs"));

      // search with invalid sourceConfigId
      arguments = objectMapper.createObjectNode();
      arguments.put("query", "grouper");
      arguments.put("sourceConfigId", "nonExistentSource");

      result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertTrue("Expected error for bad sourceConfigId", result.get("isError").asBoolean());

    } catch (Exception e) {
      throw new RuntimeException("Unexpected exception", e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * collect the distinct sourceConfigId values from a doc_search query result
   * @param result the MCP tool result
   * @return set of sourceConfigId values
   * @throws Exception if the response cannot be parsed
   */
  private Set<String> sourceConfigIdsOfResults(ObjectNode result) throws Exception {

    Set<String> sourceConfigIds = new LinkedHashSet<String>();

    String text = result.get("content").get(0).get("text").asText();
    JsonNode responseNode = objectMapper.readTree(text);
    JsonNode results = responseNode.get("results");

    if (results != null) {
      for (JsonNode resultNode : results) {
        if (resultNode.has("sourceConfigId")) {
          sourceConfigIds.add(resultNode.get("sourceConfigId").asText());
        }
      }
    }

    return sourceConfigIds;
  }

  /**
   * test tool definition includes configured source documentation and action parameter
   */
  public void testToolDefinition() {

    configureDocSearchSource();

    ObjectNode toolDef = GrouperMcpDocSearch.toolDefinition();

    assertEquals("doc_search", toolDef.get("name").asText());
    String description = toolDef.get("description").asText();
    assertTrue("Expected description to include source doc",
        description.contains("Test documentation for unit testing"));
    assertTrue("Expected description to include configId",
        description.contains("testDocs"));

    // verify input schema
    JsonNode inputSchema = toolDef.get("inputSchema");
    assertNotNull(inputSchema);
    JsonNode properties = inputSchema.get("properties");
    assertNotNull(properties.get("query"));
    assertNotNull(properties.get("maxResults"));
    assertNotNull(properties.get("sourceConfigId"));

    // verify action property
    assertNotNull("Expected action property", properties.get("action"));
    JsonNode actionEnum = properties.get("action").get("enum");
    assertNotNull("Expected action enum", actionEnum);
    assertEquals(4, actionEnum.size());
    assertEquals("query", actionEnum.get(0).asText());
    assertEquals("retrieveChunk", actionEnum.get(1).asText());
    assertEquals("listSourceConfigIds", actionEnum.get(2).asText());
    assertEquals("listNames", actionEnum.get(3).asText());

    // verify retrieveChunk properties
    assertNotNull("Expected name property", properties.get("name"));
    assertNotNull("Expected url property", properties.get("url"));
    assertNotNull("Expected chunkIndexes property", properties.get("chunkIndexes"));
    assertEquals("array", properties.get("chunkIndexes").get("type").asText());
  }

  /**
   * test that getConfigIds returns empty when nothing configured
   */
  public void testGetConfigIdsEmpty() {
    assertTrue("Expected no config ids",
        GrouperMcpDocSearchIndex.getConfigIds().isEmpty());
  }

  /**
   * test that getConfigIds returns configured ids
   */
  public void testGetConfigIdsConfigured() {
    configureDocSearchSource();
    assertTrue("Expected testDocs config id",
        GrouperMcpDocSearchIndex.getConfigIds().contains("testDocs"));
  }

  /**
   * test retrieveChunk action retrieves specific chunks by exact match
   */
  public void testRetrieveChunk() {

    configureDocSearchSource();

    // use smaller chunk size so content fits in varchar(4000) but produces multiple chunks
    GrouperMcpDocSearchIndex.chunkSizeCharsOverrideForTesting = 500;
    GrouperMcpDocSearchIndex.chunkOverlapCharsOverrideForTesting = 50;

    // insert content that will produce multiple chunks with the smaller chunk size
    StringBuilder content = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      content.append("Paragraph ").append(i)
          .append(" describes grouper access management and group provisioning.\n");
    }
    insertTestContent("https://example.com/large", "Large Document", content.toString());
    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // first search to get chunk info
      ObjectNode searchArgs = objectMapper.createObjectNode();
      searchArgs.put("query", "grouper access management");
      ObjectNode searchResult = GrouperMcpDocSearch.execute(searchArgs, authUser);
      String searchText = searchResult.get("content").get(0).get("text").asText();
      JsonNode searchResponse = objectMapper.readTree(searchText);
      JsonNode firstHit = searchResponse.get("results").get(0);
      int totalChunks = firstHit.get("totalChunksForDocument").asInt();
      assertTrue("Expected multiple chunks, got: " + totalChunks, totalChunks > 1);

      // now retrieve specific chunks
      ObjectNode retrieveArgs = objectMapper.createObjectNode();
      retrieveArgs.put("action", "retrieveChunk");
      retrieveArgs.put("sourceConfigId", "testDocs");
      retrieveArgs.put("name", "Large Document");
      ArrayNode chunkIndexes = objectMapper.createArrayNode();
      chunkIndexes.add(0);
      chunkIndexes.add(1);
      retrieveArgs.set("chunkIndexes", chunkIndexes);

      ObjectNode result = GrouperMcpDocSearch.execute(retrieveArgs, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(2, responseNode.get("totalResults").asInt());

      JsonNode results = responseNode.get("results");
      assertEquals(0, results.get(0).get("chunkIndex").asInt());
      assertEquals(1, results.get(1).get("chunkIndex").asInt());
      assertEquals(totalChunks, results.get(0).get("totalChunksForDocument").asInt());
      assertEquals(totalChunks, results.get(1).get("totalChunksForDocument").asInt());
      assertEquals("Large Document", results.get(0).get("name").asText());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperMcpDocSearchIndex.chunkSizeCharsOverrideForTesting = -1;
      GrouperMcpDocSearchIndex.chunkOverlapCharsOverrideForTesting = -1;
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test retrieveChunk action validates required parameters
   */
  public void testRetrieveChunkMissingParams() {

    configureDocSearchSource();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // missing sourceConfigId
      ObjectNode args = objectMapper.createObjectNode();
      args.put("action", "retrieveChunk");
      args.put("name", "someName");
      ArrayNode indexes = objectMapper.createArrayNode();
      indexes.add(0);
      args.set("chunkIndexes", indexes);
      ObjectNode result = GrouperMcpDocSearch.execute(args, authUser);
      assertTrue("Expected error for missing sourceConfigId", result.get("isError").asBoolean());

      // missing both name and url
      args = objectMapper.createObjectNode();
      args.put("action", "retrieveChunk");
      args.put("sourceConfigId", "testDocs");
      indexes = objectMapper.createArrayNode();
      indexes.add(0);
      args.set("chunkIndexes", indexes);
      result = GrouperMcpDocSearch.execute(args, authUser);
      assertTrue("Expected error for missing name and url", result.get("isError").asBoolean());

      // missing chunkIndexes
      args = objectMapper.createObjectNode();
      args.put("action", "retrieveChunk");
      args.put("sourceConfigId", "testDocs");
      args.put("name", "someName");
      result = GrouperMcpDocSearch.execute(args, authUser);
      assertTrue("Expected error for missing chunkIndexes", result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that documents with blank name are skipped during indexing
   */
  public void testDocSearchBlankNameSkipped() {

    configureDocSearchSource();

    // insert content with blank name
    insertTestContent("https://example.com/noname", null,
        "Content about grouper access management without a name.");
    insertTestContent("https://example.com/withname", "Named Document",
        "Content about grouper access management with a name.");

    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "grouper access management");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);

      // only the named document should appear
      JsonNode results = responseNode.get("results");
      assertTrue("Expected at least one result", results.size() > 0);
      for (int i = 0; i < results.size(); i++) {
        assertTrue("All results should have a name",
            results.get(i).has("name") && results.get(i).get("name").asText().length() > 0);
        assertEquals("Named Document", results.get(i).get("name").asText());
      }

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test query action with explicit action parameter
   */
  public void testDocSearchWithExplicitAction() {

    configureDocSearchSource();

    insertTestContent("https://example.com/page1", "Test Page",
        "Content about grouper access management and groups.");
    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "query");
      arguments.put("query", "grouper access");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue(responseNode.get("totalResults").asInt() > 0);

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * helper to configure a privacy realm, data field, and viewer group for data dictionary tests
   */
  private void configureDataDictionaryTestData() {

    // create viewer group
    GrouperSession session = GrouperSession.startRootSession();
    try {
      new GroupSave(session)
          .assignName("test:dataDictViewers")
          .assignCreateParentStemsIfNotExist(true)
          .save();
    } finally {
      GrouperSession.stopQuietly(session);
    }

    // configure privacy realm
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmName", "Test Realm");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmPublic", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmAuthenticated", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmSysadminsCanView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmViewersGroupName", "test:dataDictViewers");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmReadersGroupName", "test:dataDictViewers");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmUpdatersGroupName", "test:dataDictViewers");

    // configure a data field
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.fieldAliases", "pennId, pennkey");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.fieldPrivacyRealm", "testRealm");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.descriptionHtml",
        "<p>The <b>Penn ID</b> is a unique identifier assigned to each person at the university.</p>");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.zeroToManyExamplesHtml",
        "<ul><li>12345678</li><li>87654321</li></ul>");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.fieldDataType", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperDataField.testField1.fieldDataStructure", "attribute");
  }

  /**
   * helper to clean up data dictionary test config
   */
  private void cleanupDataDictionaryTestData() {
    String[] keys = {
        "grouperPrivacyRealm.testRealm.privacyRealmName",
        "grouperPrivacyRealm.testRealm.privacyRealmPublic",
        "grouperPrivacyRealm.testRealm.privacyRealmAuthenticated",
        "grouperPrivacyRealm.testRealm.privacyRealmSysadminsCanView",
        "grouperPrivacyRealm.testRealm.privacyRealmViewersGroupName",
        "grouperPrivacyRealm.testRealm.privacyRealmReadersGroupName",
        "grouperPrivacyRealm.testRealm.privacyRealmUpdatersGroupName",
        "grouperDataField.testField1.fieldAliases",
        "grouperDataField.testField1.fieldPrivacyRealm",
        "grouperDataField.testField1.descriptionHtml",
        "grouperDataField.testField1.zeroToManyExamplesHtml",
        "grouperDataField.testField1.fieldDataType",
        "grouperDataField.testField1.fieldDataStructure"
    };
    for (String key : keys) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
  }

  /**
   * test that data dictionary entries are indexed and searchable by authorized users
   */
  public void testDataDictionarySearchAuthorized() {

    configureDataDictionaryTestData();

    // add SUBJ0 to the viewer group so they have access
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Group viewersGroup = Group.saveGroup(session, null, null, "test:dataDictViewers", null, null, null, false);
      viewersGroup.addMember(SubjectTestHelper.SUBJ0);
      // populate sql cache so privacy realm membership check works
      GrouperLoader.runOnceByJobName(session, "CHANGE_LOG_changeLogTempToChangeLog");
    } finally {
      GrouperSession.stopQuietly(session);
    }

    GrouperDataEngine.clearHighestLevelCache();
    GrouperMcpDocSearchIndex.forceRebuild();

    session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "Penn ID unique identifier");
      arguments.put("sourceConfigId", "grouperDataDictionary");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected results for authorized user",
          responseNode.get("totalResults").asInt() > 0);

      // verify the result has the data field configId as name
      JsonNode firstResult = responseNode.get("results").get(0);
      assertEquals("testField1", firstResult.get("name").asText());
      assertEquals("grouperDataDictionary", firstResult.get("sourceConfigId").asText());

      // verify content contains markdown-converted text (not HTML tags)
      String content = firstResult.get("content").asText();
      assertTrue("Expected description content", content.contains("Penn ID"));
      assertFalse("Expected HTML to be converted to markdown", content.contains("<p>"));

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
      cleanupDataDictionaryTestData();
    }
  }

  /**
   * test that data dictionary entries are NOT returned for unauthorized users
   */
  public void testDataDictionarySearchUnauthorized() {

    configureDataDictionaryTestData();

    // do NOT add SUBJ1 to the viewer group
    GrouperDataEngine.clearHighestLevelCache();
    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ1);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "Penn ID unique identifier");
      arguments.put("sourceConfigId", "grouperDataDictionary");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success (not error), got: " + result.toString(),
          result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("Expected no results for unauthorized user",
          0, responseNode.get("totalResults").asInt());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
      cleanupDataDictionaryTestData();
    }
  }

  /**
   * test that data dictionary with public privacy realm is accessible to all
   */
  public void testDataDictionarySearchPublicRealm() {

    configureDataDictionaryTestData();

    // make the realm public
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperPrivacyRealm.testRealm.privacyRealmPublic", "true");

    GrouperDataEngine.clearHighestLevelCache();
    GrouperMcpDocSearchIndex.forceRebuild();

    // SUBJ1 is NOT in any viewer group but realm is public
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ1);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("query", "Penn ID unique identifier");
      arguments.put("sourceConfigId", "grouperDataDictionary");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);

      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected results for public realm",
          responseNode.get("totalResults").asInt() > 0);

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
      cleanupDataDictionaryTestData();
    }
  }

  /**
   * test hasAnySourcesForSubject with data dictionary
   */
  public void testHasAnySourcesForSubjectWithDataDictionary() {

    // no sources configured, no data dictionary
    assertFalse("Expected no sources with nothing configured",
        GrouperMcpDocSearchIndex.hasAnySourcesForSubject(SubjectTestHelper.SUBJ0));

    configureDataDictionaryTestData();

    // data dictionary exists but user has no access
    assertFalse("Expected no sources for unauthorized user",
        GrouperMcpDocSearchIndex.hasAnySourcesForSubject(SubjectTestHelper.SUBJ1));

    // add user to viewer group
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Group viewersGroup = Group.saveGroup(session, null, null, "test:dataDictViewers", null, null, null, false);
      viewersGroup.addMember(SubjectTestHelper.SUBJ0);
      GrouperLoader.runOnceByJobName(session, "CHANGE_LOG_changeLogTempToChangeLog");
    } finally {
      GrouperSession.stopQuietly(session);
    }
    GrouperDataEngine.clearHighestLevelCache();

    assertTrue("Expected sources for authorized user",
        GrouperMcpDocSearchIndex.hasAnySourcesForSubject(SubjectTestHelper.SUBJ0));

    cleanupDataDictionaryTestData();
  }

  /**
   * test listSourceConfigIds action returns configured sources with descriptions
   */
  public void testListSourceConfigIds() {

    configureDocSearchSource();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listSourceConfigIds");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);

      assertTrue("Expected at least one source",
          responseNode.get("totalSources").asInt() > 0);

      JsonNode sources = responseNode.get("sources");
      boolean foundTestDocs = false;
      for (int i = 0; i < sources.size(); i++) {
        if ("testDocs".equals(sources.get(i).get("sourceConfigId").asText())) {
          foundTestDocs = true;
          assertEquals("Test documentation for unit testing",
              sources.get(i).get("description").asText());
        }
      }
      assertTrue("Expected testDocs source", foundTestDocs);

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listNames action returns document names for a sourceConfigId
   */
  public void testListNames() {

    configureDocSearchSource();

    insertTestContent("https://example.com/page1", "Document Alpha",
        "Content about grouper access management alpha.");
    insertTestContent("https://example.com/page2", "Document Beta",
        "Content about grouper provisioning beta.");
    insertTestContent("https://example.com/page3", "Document Gamma",
        "Content about grouper security gamma.");
    GrouperMcpDocSearchIndex.forceRebuild();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");
      arguments.put("sourceConfigId", "testDocs");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);

      assertEquals(3, responseNode.get("totalNames").asInt());
      assertFalse("Expected not truncated", responseNode.has("truncated"));

      JsonNode names = responseNode.get("names");
      assertEquals(3, names.size());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listNames action requires sourceConfigId
   */
  public void testListNamesMissingSourceConfigId() {

    configureDocSearchSource();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertTrue("Expected error for missing sourceConfigId",
          result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listNames action with invalid sourceConfigId
   */
  public void testListNamesInvalidSourceConfigId() {

    configureDocSearchSource();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");
      arguments.put("sourceConfigId", "nonexistent");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertTrue("Expected error for invalid sourceConfigId",
          result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test listNames with data dictionary filters by privacy realm
   */
  public void testListNamesDataDictionaryFiltered() {

    configureDataDictionaryTestData();

    // add SUBJ0 to the viewer group
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Group viewersGroup = Group.saveGroup(session, null, null, "test:dataDictViewers", null, null, null, false);
      viewersGroup.addMember(SubjectTestHelper.SUBJ0);
      GrouperLoader.runOnceByJobName(session, "CHANGE_LOG_changeLogTempToChangeLog");
    } finally {
      GrouperSession.stopQuietly(session);
    }

    GrouperDataEngine.clearHighestLevelCache();
    GrouperMcpDocSearchIndex.forceRebuild();

    // authorized user should see names
    session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");
      arguments.put("sourceConfigId", "grouperDataDictionary");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected names for authorized user",
          responseNode.get("totalNames").asInt() > 0);

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
    }

    // unauthorized user should see no names
    session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ1);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "listNames");
      arguments.put("sourceConfigId", "grouperDataDictionary");

      ObjectNode result = GrouperMcpDocSearch.execute(arguments, authUser);
      assertFalse("Expected success", result.has("isError") && result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("Expected no names for unauthorized user",
          0, responseNode.get("totalNames").asInt());

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    } finally {
      GrouperSession.stopQuietly(session);
      cleanupDataDictionaryTestData();
    }
  }

}
