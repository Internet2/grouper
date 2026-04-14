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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex.DocSearchResult;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex.ListNamesResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for searching institutional documentation.
 * Uses a Lucene in-memory index built from configured database content sources
 * to provide full-text search with relevance ranking.
 *
 * <p>Supports four actions:
 * <ul>
 *   <li><b>query</b> (default): full-text search returning ranked chunks</li>
 *   <li><b>retrieveChunk</b>: retrieve specific chunks by sourceConfigId, name, and chunkIndexes</li>
 *   <li><b>listSourceConfigIds</b>: list available source config ids with descriptions</li>
 *   <li><b>listNames</b>: list document names for a given sourceConfigId</li>
 * </ul>
 *
 * @author mchyzer
 */
public class GrouperMcpDocSearch {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpDocSearch.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** default max results */
  static final int DEFAULT_MAX_RESULTS = 10;

  /** maximum characters in the response */
  static final int MAX_RESPONSE_CHARS = 500000;

  /**
   * Return the MCP tool definition for doc_search.
   * The description dynamically includes documentation about configured sources
   * so the AI client knows what content is searchable.
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", "doc_search");

    // build description with configured source documentation
    StringBuilder description = new StringBuilder();
    description.append("Search institutional documentation or retrieve specific document chunks. "
        + "Use action 'query' (default) to search by keyword. "
        + "Use action 'retrieveChunk' to retrieve specific chunks by sourceConfigId, name, and chunkIndexes "
        + "(useful for getting additional context around a search result). "
        + "Use action 'listSourceConfigIds' to list available documentation sources with descriptions. "
        + "Use action 'listNames' to list document names for a given sourceConfigId. "
        + "If results contain 'url' attributes, display them to the user as links so they can navigate to the source document. ");

    Set<String> configIds = GrouperMcpDocSearchIndex.getConfigIds();
    if (!configIds.isEmpty()) {
      description.append("Available documentation sources: ");
      boolean first = true;
      for (String configId : configIds) {
        String docForAi = GrouperMcpDocSearchIndex.getDocumentationForAiClient(configId);
        if (StringUtils.isNotBlank(docForAi)) {
          if (!first) {
            description.append("; ");
          }
          description.append(configId).append(" - ").append(docForAi);
          first = false;
        }
      }
      description.append(". ");
    }

    description.append("Use sourceConfigId to limit search to a specific source.");

    tool.put("description", description.toString());

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("query");
    actionEnum.add("retrieveChunk");
    actionEnum.add("listSourceConfigIds");
    actionEnum.add("listNames");
    actionProp.set("enum", actionEnum);
    actionProp.put("description",
        "The action to perform. 'query' (default) searches documents by keyword. "
        + "'retrieveChunk' retrieves specific chunks by sourceConfigId, name, and chunkIndexes. "
        + "'listSourceConfigIds' lists available documentation sources with descriptions. "
        + "'listNames' lists document names for a given sourceConfigId.");
    properties.set("action", actionProp);

    ObjectNode queryProp = objectMapper.createObjectNode();
    queryProp.put("type", "string");
    queryProp.put("description",
        "The search query. Required for 'query' action. "
        + "Use natural language terms related to the topic you want to find. "
        + "The search uses full-text matching with stemming.");
    properties.set("query", queryProp);

    ObjectNode maxResultsProp = objectMapper.createObjectNode();
    maxResultsProp.put("type", "integer");
    maxResultsProp.put("description",
        "Maximum number of document chunks to return (default "
        + DEFAULT_MAX_RESULTS + "). Only for 'query' action.");
    properties.set("maxResults", maxResultsProp);

    ObjectNode sourceConfigIdProp = objectMapper.createObjectNode();
    sourceConfigIdProp.put("type", "string");
    sourceConfigIdProp.put("description",
        "Limit to a specific documentation source by config ID. "
        + "Optional for 'query' action. Required for 'retrieveChunk' and 'listNames' actions.");
    properties.set("sourceConfigId", sourceConfigIdProp);

    ObjectNode nameProp = objectMapper.createObjectNode();
    nameProp.put("type", "string");
    nameProp.put("description",
        "Document name for 'retrieveChunk' action. "
        + "At least one of 'name' or 'url' is required. "
        + "Use the exact 'name' value from a previous search result.");
    properties.set("name", nameProp);

    ObjectNode urlProp = objectMapper.createObjectNode();
    urlProp.put("type", "string");
    urlProp.put("description",
        "Document URL for 'retrieveChunk' action. "
        + "At least one of 'name' or 'url' is required. "
        + "Use the exact 'url' value from a previous search result.");
    properties.set("url", urlProp);

    ObjectNode chunkIndexesProp = objectMapper.createObjectNode();
    chunkIndexesProp.put("type", "array");
    ObjectNode chunkIndexItems = objectMapper.createObjectNode();
    chunkIndexItems.put("type", "integer");
    chunkIndexesProp.set("items", chunkIndexItems);
    chunkIndexesProp.put("description",
        "Array of chunk indexes to retrieve. Required for 'retrieveChunk' action. "
        + "Use chunkIndex and totalChunksForDocument from search results to determine which chunks to request.");
    properties.set("chunkIndexes", chunkIndexesProp);

    ObjectNode searchTypeProp = objectMapper.createObjectNode();
    searchTypeProp.put("type", "string");
    ArrayNode searchTypeEnum = objectMapper.createArrayNode();
    searchTypeEnum.add("keyword");
    searchTypeEnum.add("lucene");
    searchTypeProp.set("enum", searchTypeEnum);
    searchTypeProp.put("description",
        "Search mode for 'query' action. 'keyword' (default) performs simple keyword matching. "
        + "'lucene' allows full Lucene query syntax (AND, OR, wildcards, phrases, etc.).");
    properties.set("searchType", searchTypeProp);

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the doc_search tool, dispatching by action
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : "query";

    if ("query".equals(action)) {
      return executeQuery(arguments, authUser);
    } else if ("retrieveChunk".equals(action)) {
      return executeRetrieveChunk(arguments, authUser);
    } else if ("listSourceConfigIds".equals(action)) {
      return executeListSourceConfigIds(authUser);
    } else if ("listNames".equals(action)) {
      return executeListNames(arguments, authUser);
    } else {
      return buildErrorResult("Unknown action '" + action
          + "'. Use 'query', 'retrieveChunk', 'listSourceConfigIds', or 'listNames'.");
    }
  }

  /**
   * execute the query action -- full-text search
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private static ObjectNode executeQuery(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String query = arguments != null && arguments.has("query")
        ? arguments.get("query").asText() : null;
    int maxResults = arguments != null && arguments.has("maxResults")
        ? arguments.get("maxResults").asInt(DEFAULT_MAX_RESULTS) : DEFAULT_MAX_RESULTS;
    String sourceConfigId = arguments != null && arguments.has("sourceConfigId")
        ? arguments.get("sourceConfigId").asText() : null;
    String searchType = arguments != null && arguments.has("searchType")
        ? arguments.get("searchType").asText() : "keyword";

    if (StringUtils.isBlank(query)) {
      return buildErrorResult("query is required for 'query' action.");
    }

    if (maxResults < 1) {
      maxResults = 1;
    }
    if (maxResults > 50) {
      maxResults = 50;
    }

    Set<String> configIds = GrouperMcpDocSearchIndex.getConfigIds();
    if (configIds.isEmpty()) {
      return buildErrorResult("No document search sources are configured. "
          + "Configure grouper.mcp.docSearch.<configId>.query in grouper.properties.");
    }

    if (StringUtils.isNotBlank(sourceConfigId) && !configIds.contains(sourceConfigId)) {
      return buildErrorResult("Unknown sourceConfigId: " + sourceConfigId
          + ". Available sources: " + StringUtils.join(configIds, ", "));
    }

    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.search(
          query, maxResults, sourceConfigId, authUser.getSubject(), searchType);

      ObjectNode resultNode = objectMapper.createObjectNode();
      ArrayNode resultsArray = objectMapper.createArrayNode();

      int totalChars = 0;
      for (DocSearchResult result : results) {
        ObjectNode resultObj = objectMapper.createObjectNode();
        resultObj.put("content", result.getContent());
        if (StringUtils.isNotBlank(result.getUrl())) {
          resultObj.put("url", result.getUrl());
        }
        resultObj.put("name", result.getName());
        resultObj.put("sourceConfigId", result.getSourceConfigId());
        resultObj.put("score", result.getScore());
        resultObj.put("chunkIndex", result.getChunkIndex());
        resultObj.put("totalChunksForDocument", result.getTotalChunksForDocument());

        String resultText = resultObj.toString();
        totalChars += resultText.length();
        if (totalChars > MAX_RESPONSE_CHARS) {
          ObjectNode truncObj = objectMapper.createObjectNode();
          truncObj.put("notice", "Results truncated due to response size limit. "
              + "Try a more specific query or reduce maxResults.");
          resultsArray.add(truncObj);
          break;
        }

        resultsArray.add(resultObj);
      }

      resultNode.set("results", resultsArray);
      resultNode.put("totalResults", results.size());

      ObjectNode response = objectMapper.createObjectNode();
      ArrayNode contentArray = objectMapper.createArrayNode();
      ObjectNode textContent = objectMapper.createObjectNode();
      textContent.put("type", "text");
      textContent.put("text", resultNode.toString());
      contentArray.add(textContent);
      response.set("content", contentArray);

      return response;

    } catch (Exception e) {
      LOG.error("Error executing doc_search query", e);
      return buildErrorResult("Error searching documents: " + e.getMessage());
    }
  }

  /**
   * execute the retrieveChunk action -- retrieve specific chunks by exact match
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private static ObjectNode executeRetrieveChunk(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String sourceConfigId = arguments != null && arguments.has("sourceConfigId")
        ? arguments.get("sourceConfigId").asText() : null;
    String name = arguments != null && arguments.has("name")
        ? arguments.get("name").asText() : null;
    String url = arguments != null && arguments.has("url")
        ? arguments.get("url").asText() : null;
    JsonNode chunkIndexesNode = arguments != null ? arguments.get("chunkIndexes") : null;

    if (StringUtils.isBlank(sourceConfigId)) {
      return buildErrorResult("sourceConfigId is required for 'retrieveChunk' action.");
    }
    if (StringUtils.isBlank(name) && StringUtils.isBlank(url)) {
      return buildErrorResult("At least one of 'name' or 'url' is required for 'retrieveChunk' action.");
    }
    if (chunkIndexesNode == null || !chunkIndexesNode.isArray() || chunkIndexesNode.size() == 0) {
      return buildErrorResult(
          "chunkIndexes is required and must be a non-empty array of integers for 'retrieveChunk' action.");
    }

    Set<String> configIds = GrouperMcpDocSearchIndex.getConfigIds();
    if (!configIds.contains(sourceConfigId)) {
      return buildErrorResult("Unknown sourceConfigId: " + sourceConfigId
          + ". Available sources: " + StringUtils.join(configIds, ", "));
    }

    List<Integer> chunkIndexes = new ArrayList<>();
    for (int i = 0; i < chunkIndexesNode.size(); i++) {
      chunkIndexes.add(chunkIndexesNode.get(i).asInt());
    }

    if (chunkIndexes.size() > 50) {
      return buildErrorResult("chunkIndexes must not contain more than 50 entries.");
    }

    try {
      List<DocSearchResult> results = GrouperMcpDocSearchIndex.retrieveChunks(
          sourceConfigId, name, url, chunkIndexes, authUser.getSubject());

      ObjectNode resultNode = objectMapper.createObjectNode();
      ArrayNode resultsArray = objectMapper.createArrayNode();

      int totalChars = 0;
      for (DocSearchResult result : results) {
        ObjectNode resultObj = objectMapper.createObjectNode();
        resultObj.put("content", result.getContent());
        if (StringUtils.isNotBlank(result.getUrl())) {
          resultObj.put("url", result.getUrl());
        }
        resultObj.put("name", result.getName());
        resultObj.put("sourceConfigId", result.getSourceConfigId());
        resultObj.put("chunkIndex", result.getChunkIndex());
        resultObj.put("totalChunksForDocument", result.getTotalChunksForDocument());

        String resultText = resultObj.toString();
        totalChars += resultText.length();
        if (totalChars > MAX_RESPONSE_CHARS) {
          ObjectNode truncObj = objectMapper.createObjectNode();
          truncObj.put("notice", "Results truncated due to response size limit. Request fewer chunks.");
          resultsArray.add(truncObj);
          break;
        }

        resultsArray.add(resultObj);
      }

      resultNode.set("results", resultsArray);
      resultNode.put("totalResults", results.size());

      ObjectNode response = objectMapper.createObjectNode();
      ArrayNode contentArray = objectMapper.createArrayNode();
      ObjectNode textContent = objectMapper.createObjectNode();
      textContent.put("type", "text");
      textContent.put("text", resultNode.toString());
      contentArray.add(textContent);
      response.set("content", contentArray);

      return response;

    } catch (Exception e) {
      LOG.error("Error executing doc_search retrieveChunk", e);
      return buildErrorResult("Error retrieving chunks: " + e.getMessage());
    }
  }

  /**
   * execute the listSourceConfigIds action -- list available sources with descriptions
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private static ObjectNode executeListSourceConfigIds(GrouperMcpAuthUser authUser) {

    Set<String> configIds = GrouperMcpDocSearchIndex.getConfigIds();

    ObjectNode resultNode = objectMapper.createObjectNode();
    ArrayNode sourcesArray = objectMapper.createArrayNode();

    for (String configId : configIds) {
      ObjectNode sourceObj = objectMapper.createObjectNode();
      sourceObj.put("sourceConfigId", configId);
      String description = GrouperMcpDocSearchIndex.getDocumentationForAiClient(configId);
      if (StringUtils.isNotBlank(description)) {
        sourceObj.put("description", description);
      }
      sourcesArray.add(sourceObj);
    }

    resultNode.set("sources", sourcesArray);
    resultNode.put("totalSources", sourcesArray.size());

    ObjectNode response = objectMapper.createObjectNode();
    ArrayNode contentArray = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", resultNode.toString());
    contentArray.add(textContent);
    response.set("content", contentArray);

    return response;
  }

  /**
   * execute the listNames action -- list document names for a sourceConfigId
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private static ObjectNode executeListNames(JsonNode arguments, GrouperMcpAuthUser authUser) {

    String sourceConfigId = arguments != null && arguments.has("sourceConfigId")
        ? arguments.get("sourceConfigId").asText() : null;

    if (StringUtils.isBlank(sourceConfigId)) {
      return buildErrorResult("sourceConfigId is required for 'listNames' action.");
    }

    Set<String> configIds = GrouperMcpDocSearchIndex.getConfigIds();
    if (!configIds.contains(sourceConfigId)) {
      return buildErrorResult("Unknown sourceConfigId: " + sourceConfigId
          + ". Available sources: " + StringUtils.join(configIds, ", "));
    }

    ListNamesResult listNamesResult = GrouperMcpDocSearchIndex.listNames(
        sourceConfigId, authUser.getSubject(), 1000);

    ObjectNode resultNode = objectMapper.createObjectNode();
    ArrayNode namesArray = objectMapper.createArrayNode();
    for (String name : listNamesResult.getNames()) {
      namesArray.add(name);
    }
    resultNode.set("names", namesArray);
    resultNode.put("totalNames", listNamesResult.getNames().size());
    if (listNamesResult.isTruncated()) {
      resultNode.put("truncated", true);
      resultNode.put("notice", "List truncated to 1000 names. Use 'query' action to search for specific documents.");
    }

    ObjectNode response = objectMapper.createObjectNode();
    ArrayNode contentArray = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", resultNode.toString());
    contentArray.add(textContent);
    response.set("content", contentArray);

    return response;
  }

  /**
   * build an MCP error result
   * @param message the error message
   * @return the error result
   */
  private static ObjectNode buildErrorResult(String message) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode contentArray = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", message);
    contentArray.add(textContent);
    result.set("content", contentArray);
    result.put("isError", true);
    return result;
  }

}
