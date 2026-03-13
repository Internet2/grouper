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
package edu.internet2.middleware.grouper.mcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * Manages an in-memory Lucene index for MCP document search (RAG).
 * Reads document content from institution-managed database tables via
 * configurable SQL queries, chunks the content, and indexes it for
 * full-text search.
 *
 * <p>Also indexes the Grouper data field/row dictionary as a built-in source
 * ({@code grouperDataDictionary}). Search results from data dictionary entries
 * are filtered by privacy realm access at query time.</p>
 *
 * <p>The index is built lazily on first search and rebuilt periodically
 * based on the configured reindex interval.</p>
 *
 * <p>Thread-safe: builds a new index then swaps the reference atomically.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpDocSearchIndex {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpDocSearchIndex.class);

  /** pattern to find all doc search config ids */
  private static final Pattern CONFIG_ID_PATTERN =
      Pattern.compile("^grouper\\.mcp\\.docSearch\\.([^.]+)\\.query$");

  /** source config id for the built-in data dictionary source */
  public static final String DATA_DICTIONARY_SOURCE_CONFIG_ID = "grouperDataDictionary";

  /** default target chunk size in characters (~800 tokens) */
  static final int CHUNK_SIZE_CHARS = 3200;

  /** default overlap between chunks in characters (~100 tokens) */
  static final int CHUNK_OVERLAP_CHARS = 400;

  /** override chunk size for testing, or -1 for default */
  public static int chunkSizeCharsOverrideForTesting = -1;

  /** override chunk overlap for testing, or -1 for default */
  public static int chunkOverlapCharsOverrideForTesting = -1;

  /** default reindex interval in seconds (1 hour) */
  static final int DEFAULT_REINDEX_INTERVAL_SECONDS = 3600;

  /** the current in-memory directory holding the index */
  private static volatile Directory currentDirectory;

  /** the current index reader */
  private static volatile DirectoryReader currentReader;

  /** the analyzer used for indexing and searching */
  private static final StandardAnalyzer analyzer = new StandardAnalyzer();

  /** when the index was last built (millis since epoch) */
  private static volatile long lastIndexBuildMillis = 0;

  /** whether a background build is currently in progress */
  private static volatile boolean buildInProgress = false;

  /** lock for index building */
  private static final Object INDEX_LOCK = new Object();

  /**
   * result object for a search hit
   */
  public static class DocSearchResult {
    private String content;
    private String url;
    private String name;
    private String sourceConfigId;
    private float score;
    private int chunkIndex;
    private int totalChunksForDocument;
    private String privacyRealmConfigId;

    public String getContent() {
      return this.content;
    }

    public String getUrl() {
      return this.url;
    }

    public String getName() {
      return this.name;
    }

    public String getSourceConfigId() {
      return this.sourceConfigId;
    }

    public float getScore() {
      return this.score;
    }

    public int getChunkIndex() {
      return this.chunkIndex;
    }

    public int getTotalChunksForDocument() {
      return this.totalChunksForDocument;
    }

    public String getPrivacyRealmConfigId() {
      return this.privacyRealmConfigId;
    }
  }

  /**
   * search the document index, filtering by privacy realm access for the subject.
   * uses keyword search (escaped query) for backward compatibility.
   * @param queryString the search query
   * @param maxResults max results to return
   * @param filterSourceConfigId optional source config id to filter by, or null for all
   * @param subject the authenticated subject for privacy realm filtering (may be null for no filtering)
   * @return list of search results ordered by relevance
   */
  public static List<DocSearchResult> search(String queryString, int maxResults,
      String filterSourceConfigId, Subject subject) {
    return search(queryString, maxResults, filterSourceConfigId, subject, "keyword");
  }

  /**
   * search the document index, filtering by privacy realm access for the subject
   * @param queryString the search query
   * @param maxResults max results to return
   * @param filterSourceConfigId optional source config id to filter by, or null for all
   * @param subject the authenticated subject for privacy realm filtering (may be null for no filtering)
   * @param searchType "keyword" (default, escaped query) or "lucene" (raw Lucene query syntax)
   * @return list of search results ordered by relevance
   */
  public static List<DocSearchResult> search(String queryString, int maxResults,
      String filterSourceConfigId, Subject subject, String searchType) {

    List<DocSearchResult> results = new ArrayList<>();

    rebuildIfNeeded();

    DirectoryReader reader = currentReader;
    if (reader == null) {
      return results;
    }

    // set up privacy realm access checking
    GrouperDataEngine dataEngine = null;
    if (subject != null) {
      dataEngine = new GrouperDataEngine();
      dataEngine.loadConfigPrivacyRealms(null);
    }

    try {
      IndexSearcher searcher = new IndexSearcher(reader);
      QueryParser parser = new QueryParser("content", analyzer);

      Query query;
      if ("lucene".equals(searchType)) {
        parser.setAllowLeadingWildcard(true);
        query = parser.parse(queryString);
      } else {
        // keyword mode: escape special chars for simple keyword matching
        parser.setAllowLeadingWildcard(false);
        query = parser.parse(QueryParser.escape(queryString));
      }

      // fetch more than maxResults since some may be filtered out by source or privacy
      int fetchCount = maxResults * 3;
      if (fetchCount < 10) {
        fetchCount = 10;
      }

      TopDocs topDocs = searcher.search(query, fetchCount);

      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        if (results.size() >= maxResults) {
          break;
        }

        Document doc = searcher.storedFields().document(scoreDoc.doc);

        String sourceConfigId = doc.get("sourceConfigId");
        if (filterSourceConfigId != null && !filterSourceConfigId.equals(sourceConfigId)) {
          continue;
        }

        // check privacy realm access
        String privacyRealmConfigId = doc.get("privacyRealmConfigId");
        if (!checkPrivacyRealmAccess(privacyRealmConfigId, subject, dataEngine)) {
          continue;
        }

        DocSearchResult result = populateResult(doc, scoreDoc.score);
        results.add(result);
      }

    } catch (Exception e) {
      LOG.error("Error searching document index", e);
    }

    return results;
  }

  /**
   * check if a subject has access to a privacy realm
   * @param privacyRealmConfigId the privacy realm config id (null/blank means no filtering)
   * @param subject the subject to check
   * @param dataEngine the data engine with privacy realm configs loaded
   * @return true if accessible
   */
  private static boolean checkPrivacyRealmAccess(String privacyRealmConfigId,
      Subject subject, GrouperDataEngine dataEngine) {

    if (StringUtils.isBlank(privacyRealmConfigId)) {
      return true;
    }
    if (subject == null || dataEngine == null) {
      return true;
    }

    GrouperPrivacyRealmConfig realmConfig =
        dataEngine.getPrivacyRealmConfigByConfigId().get(privacyRealmConfigId);
    if (realmConfig == null) {
      return false;
    }

    String access = dataEngine.calculateHighestLevelAccess(realmConfig, subject);
    return StringUtils.isNotBlank(access);
  }

  /**
   * populate a DocSearchResult from a Lucene document
   * @param doc the Lucene document
   * @param score the relevance score
   * @return the populated result
   */
  private static DocSearchResult populateResult(Document doc, float score) {
    DocSearchResult result = new DocSearchResult();
    result.content = doc.get("content");
    result.url = doc.get("url");
    result.name = doc.get("name");
    result.sourceConfigId = doc.get("sourceConfigId");
    result.score = score;
    String chunkIndexStr = doc.get("chunkIndex");
    result.chunkIndex = chunkIndexStr != null ? Integer.parseInt(chunkIndexStr) : 0;
    String totalChunksStr = doc.get("totalChunksForDocument");
    result.totalChunksForDocument = totalChunksStr != null ? Integer.parseInt(totalChunksStr) : 0;
    result.privacyRealmConfigId = doc.get("privacyRealmConfigId");
    return result;
  }

  /**
   * rebuild the index if it is stale or not yet built.
   * runs the build in a background thread so the calling thread is not blocked.
   * if a build is already in progress, this is a no-op.
   */
  public static void rebuildIfNeeded() {
    if (currentReader != null) {
      // check if any source needs reindexing
      long now = System.currentTimeMillis();
      long elapsed = now - lastIndexBuildMillis;

      int minReindexSeconds = getMinReindexIntervalSeconds();
      if (elapsed < minReindexSeconds * 1000L) {
        return;
      }
    }

    // if a build is already in progress, don't start another one
    if (buildInProgress) {
      return;
    }

    synchronized (INDEX_LOCK) {
      // double-check after acquiring lock
      if (buildInProgress) {
        return;
      }
      if (currentReader != null) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastIndexBuildMillis;
        int minReindexSeconds = getMinReindexIntervalSeconds();
        if (elapsed < minReindexSeconds * 1000L) {
          return;
        }
      }

      buildInProgress = true;
    }

    // run the build in a background thread
    Thread buildThread = new Thread(() -> {
      try {
        synchronized (INDEX_LOCK) {
          buildIndex();
        }
      } finally {
        buildInProgress = false;
      }
    }, "GrouperMcpDocSearchIndexBuilder");
    buildThread.setDaemon(true);
    buildThread.start();
  }

  /**
   * force rebuild the index now (synchronously, blocks the calling thread)
   */
  public static void forceRebuild() {
    synchronized (INDEX_LOCK) {
      buildInProgress = true;
      try {
        buildIndex();
      } finally {
        buildInProgress = false;
      }
    }
  }

  /**
   * get the minimum reindex interval across all configured sources
   * @return seconds
   */
  private static int getMinReindexIntervalSeconds() {
    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(CONFIG_ID_PATTERN);
    int minSeconds = DEFAULT_REINDEX_INTERVAL_SECONDS;
    for (String configId : configIds) {
      int seconds = GrouperConfig.retrieveConfig().propertyValueInt(
          "grouper.mcp.docSearch." + configId + ".reindexIntervalSeconds",
          DEFAULT_REINDEX_INTERVAL_SECONDS);
      if (seconds < minSeconds) {
        minSeconds = seconds;
      }
    }
    return minSeconds;
  }

  /**
   * build the index from all configured doc search sources and the data dictionary.
   * creates a new in-memory directory, indexes all content, then swaps
   * the reference so searches use the new index.
   */
  private static void buildIndex() {

    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(CONFIG_ID_PATTERN);
    boolean dataDictionaryEnabled = isDataDictionaryEnabled();

    if (configIds.isEmpty() && !dataDictionaryEnabled) {
      LOG.debug("No doc search sources configured and data dictionary disabled");
      lastIndexBuildMillis = System.currentTimeMillis();
      return;
    }

    ByteBuffersDirectory newDirectory = new ByteBuffersDirectory();

    try {
      IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
      IndexWriter writer = new IndexWriter(newDirectory, writerConfig);

      int totalDocs = 0;
      int totalChunks = 0;

      for (String configId : configIds) {
        String externalSystemId = GrouperConfig.retrieveConfig().propertyValueString(
            "grouper.mcp.docSearch." + configId + ".externalSystemId", "grouper");
        String query = GrouperConfig.retrieveConfig().propertyValueStringRequired(
            "grouper.mcp.docSearch." + configId + ".query");

        try {
          List<? extends Map<String, Object>> rows = new GcDbAccess()
              .connectionName(externalSystemId)
              .sql(query)
              .selectListMap();

          for (Map<String, Object> row : GrouperUtil.nonNull(rows)) {
            totalDocs++;

            String content = objectToString(row.get("grouper_content"));
            if (content == null) {
              content = objectToString(row.get("GROUPER_CONTENT"));
            }
            String url = objectToString(row.get("grouper_url"));
            if (url == null) {
              url = objectToString(row.get("GROUPER_URL"));
            }
            String name = objectToString(row.get("grouper_name"));
            if (name == null) {
              name = objectToString(row.get("GROUPER_NAME"));
            }

            if (StringUtils.isBlank(content)) {
              continue;
            }

            // name is required -- skip documents without a name
            if (StringUtils.isBlank(name)) {
              continue;
            }

            List<String> chunks = chunkContent(content);
            int totalChunksForDoc = chunks.size();
            for (int i = 0; i < chunks.size(); i++) {
              Document doc = new Document();
              doc.add(new TextField("content", chunks.get(i), Field.Store.YES));
              if (StringUtils.isNotBlank(url)) {
                doc.add(new StringField("url", url, Field.Store.YES));
              }
              doc.add(new StringField("name", name, Field.Store.YES));
              doc.add(new StringField("sourceConfigId", configId, Field.Store.YES));
              doc.add(new StringField("chunkIndex", String.valueOf(i), Field.Store.YES));
              doc.add(new StoredField("totalChunksForDocument", String.valueOf(totalChunksForDoc)));
              writer.addDocument(doc);
              totalChunks++;
            }
          }

        } catch (Exception e) {
          LOG.error("Error loading doc search source: " + configId, e);
        }
      }

      // index the data dictionary (data fields and data rows)
      if (dataDictionaryEnabled) {
        int[] dataDictCounts = indexDataDictionary(writer);
        totalDocs += dataDictCounts[0];
        totalChunks += dataDictCounts[1];
      }

      writer.close();

      // swap the index reference
      DirectoryReader newReader = DirectoryReader.open(newDirectory);
      DirectoryReader oldReader = currentReader;
      Directory oldDirectory = currentDirectory;

      currentReader = newReader;
      currentDirectory = newDirectory;
      lastIndexBuildMillis = System.currentTimeMillis();

      // close old resources
      if (oldReader != null) {
        try {
          oldReader.close();
        } catch (IOException e) {
          LOG.debug("Error closing old reader", e);
        }
      }
      if (oldDirectory != null) {
        try {
          oldDirectory.close();
        } catch (IOException e) {
          LOG.debug("Error closing old directory", e);
        }
      }

      int sourceCount = configIds.size() + (dataDictionaryEnabled ? 1 : 0);
      LOG.info("Doc search index built: " + totalDocs + " documents, "
          + totalChunks + " chunks from " + sourceCount + " sources");

    } catch (Exception e) {
      LOG.error("Error building doc search index", e);
      try {
        newDirectory.close();
      } catch (IOException e2) {
        // ignore
      }
    }
  }

  /**
   * index the data field and data row dictionary entries into the Lucene index
   * @param writer the index writer
   * @return int array of [totalDocs, totalChunks]
   */
  private static int[] indexDataDictionary(IndexWriter writer) {

    int totalDocs = 0;
    int totalChunks = 0;

    try {
      GrouperDataEngine dataEngine = new GrouperDataEngine();
      dataEngine.loadConfigFields(null);
      dataEngine.loadConfigRows(null);
      dataEngine.loadConfigPrivacyRealms(null);

      // index data fields
      for (Map.Entry<String, GrouperDataFieldConfig> entry :
          dataEngine.getFieldConfigByConfigId().entrySet()) {

        String configId = entry.getKey();
        GrouperDataFieldConfig fieldConfig = entry.getValue();

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Data Field: ").append(configId).append("\n");

        Set<String> aliases = fieldConfig.getFieldAliases();
        if (aliases != null && !aliases.isEmpty()) {
          contentBuilder.append("Aliases: ").append(StringUtils.join(aliases, ", ")).append("\n");
        }

        if (fieldConfig.getFieldDataType() != null) {
          contentBuilder.append("Type: ").append(fieldConfig.getFieldDataType()).append("\n");
        }

        if (fieldConfig.getFieldDataStructure() != null) {
          contentBuilder.append("Structure: ").append(fieldConfig.getFieldDataStructure()).append("\n");
        }

        contentBuilder.append("\n");

        String descriptionMd = convertHtmlToMarkdown(fieldConfig.getDescriptionHtml());
        if (StringUtils.isNotBlank(descriptionMd)) {
          contentBuilder.append(descriptionMd).append("\n");
        }

        String examplesMd = convertHtmlToMarkdown(fieldConfig.getZeroToManyExamplesHtml());
        if (StringUtils.isNotBlank(examplesMd)) {
          contentBuilder.append("\nExamples:\n").append(examplesMd).append("\n");
        }

        String content = contentBuilder.toString().trim();
        if (StringUtils.isBlank(content)) {
          continue;
        }

        String privacyRealmConfigId = fieldConfig.getGrouperPrivacyRealmConfigId();

        List<String> chunks = chunkContent(content);
        int totalChunksForDoc = chunks.size();
        totalDocs++;

        for (int i = 0; i < chunks.size(); i++) {
          Document doc = new Document();
          doc.add(new TextField("content", chunks.get(i), Field.Store.YES));
          doc.add(new StringField("name", configId, Field.Store.YES));
          doc.add(new StringField("sourceConfigId", DATA_DICTIONARY_SOURCE_CONFIG_ID, Field.Store.YES));
          doc.add(new StringField("chunkIndex", String.valueOf(i), Field.Store.YES));
          doc.add(new StoredField("totalChunksForDocument", String.valueOf(totalChunksForDoc)));
          if (StringUtils.isNotBlank(privacyRealmConfigId)) {
            doc.add(new StringField("privacyRealmConfigId", privacyRealmConfigId, Field.Store.YES));
          }
          writer.addDocument(doc);
          totalChunks++;
        }
      }

      // index data rows
      for (Map.Entry<String, GrouperDataRowConfig> entry :
          dataEngine.getRowConfigByConfigId().entrySet()) {

        String configId = entry.getKey();
        GrouperDataRowConfig rowConfig = entry.getValue();

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Data Row: ").append(configId).append("\n");

        Set<String> aliases = rowConfig.getRowAliases();
        if (aliases != null && !aliases.isEmpty()) {
          contentBuilder.append("Aliases: ").append(StringUtils.join(aliases, ", ")).append("\n");
        }

        Set<String> fieldConfigIds = rowConfig.getDataFieldConfigIds();
        if (fieldConfigIds != null && !fieldConfigIds.isEmpty()) {
          contentBuilder.append("Fields: ").append(StringUtils.join(fieldConfigIds, ", ")).append("\n");
        }

        contentBuilder.append("\n");

        String descriptionMd = convertHtmlToMarkdown(rowConfig.getDescriptionHtml());
        if (StringUtils.isNotBlank(descriptionMd)) {
          contentBuilder.append(descriptionMd).append("\n");
        }

        String examplesMd = convertHtmlToMarkdown(rowConfig.getZeroToManyExamplesHtml());
        if (StringUtils.isNotBlank(examplesMd)) {
          contentBuilder.append("\nExamples:\n").append(examplesMd).append("\n");
        }

        String content = contentBuilder.toString().trim();
        if (StringUtils.isBlank(content)) {
          continue;
        }

        String privacyRealmConfigId = rowConfig.getPrivacyRealmName();

        List<String> chunks = chunkContent(content);
        int totalChunksForDoc = chunks.size();
        totalDocs++;

        for (int i = 0; i < chunks.size(); i++) {
          Document doc = new Document();
          doc.add(new TextField("content", chunks.get(i), Field.Store.YES));
          doc.add(new StringField("name", configId, Field.Store.YES));
          doc.add(new StringField("sourceConfigId", DATA_DICTIONARY_SOURCE_CONFIG_ID, Field.Store.YES));
          doc.add(new StringField("chunkIndex", String.valueOf(i), Field.Store.YES));
          doc.add(new StoredField("totalChunksForDocument", String.valueOf(totalChunksForDoc)));
          if (StringUtils.isNotBlank(privacyRealmConfigId)) {
            doc.add(new StringField("privacyRealmConfigId", privacyRealmConfigId, Field.Store.YES));
          }
          writer.addDocument(doc);
          totalChunks++;
        }
      }

    } catch (Exception e) {
      LOG.error("Error indexing data dictionary", e);
    }

    return new int[] {totalDocs, totalChunks};
  }

  /**
   * convert HTML to markdown using flexmark
   * @param html the HTML string
   * @return the markdown string, or empty string if input is blank
   */
  public static String convertHtmlToMarkdown(String html) {
    if (StringUtils.isBlank(html)) {
      return "";
    }
    try {
      return FlexmarkHtmlConverter.builder().build().convert(html).trim();
    } catch (Exception e) {
      LOG.debug("Error converting HTML to markdown, using raw text", e);
      return html;
    }
  }

  /**
   * convert an object from a database result to a string
   * @param obj the object
   * @return the string value, or null
   */
  private static String objectToString(Object obj) {
    if (obj == null) {
      return null;
    }
    if (obj instanceof String) {
      return (String) obj;
    }
    if (obj instanceof java.sql.Clob) {
      try {
        java.sql.Clob clob = (java.sql.Clob) obj;
        return clob.getSubString(1, (int) clob.length());
      } catch (Exception e) {
        throw new RuntimeException("Error reading clob", e);
      }
    }
    return obj.toString();
  }

  /**
   * chunk content into pieces of approximately CHUNK_SIZE_CHARS characters
   * with CHUNK_OVERLAP_CHARS overlap, splitting on paragraph boundaries.
   * @param content the full content text
   * @return list of chunks
   */
  public static List<String> chunkContent(String content) {
    int chunkSize = chunkSizeCharsOverrideForTesting > 0 ? chunkSizeCharsOverrideForTesting : CHUNK_SIZE_CHARS;
    int overlapSize = chunkOverlapCharsOverrideForTesting > 0 ? chunkOverlapCharsOverrideForTesting : CHUNK_OVERLAP_CHARS;
    return chunkContent(content, chunkSize, overlapSize);
  }

  /**
   * chunk content into pieces of approximately chunkSizeChars characters
   * with overlapChars overlap, splitting on paragraph boundaries.
   * @param content the full content text
   * @param chunkSizeChars target chunk size in characters
   * @param overlapChars overlap between chunks in characters
   * @return list of chunks
   */
  public static List<String> chunkContent(String content, int chunkSizeChars, int overlapChars) {
    List<String> chunks = new ArrayList<>();

    if (StringUtils.isBlank(content)) {
      return chunks;
    }

    // if content is small enough, return as a single chunk
    if (content.length() <= chunkSizeChars) {
      chunks.add(content);
      return chunks;
    }

    int start = 0;
    while (start < content.length()) {
      int end = Math.min(start + chunkSizeChars, content.length());

      // if not at the end, try to break on a paragraph boundary
      if (end < content.length()) {
        int paragraphBreak = content.lastIndexOf("\n\n", end);
        if (paragraphBreak > start + chunkSizeChars / 2) {
          // found a paragraph break in the second half of the chunk
          end = paragraphBreak + 2; // include the newlines
        } else {
          // try a single newline
          int lineBreak = content.lastIndexOf("\n", end);
          if (lineBreak > start + chunkSizeChars / 2) {
            end = lineBreak + 1;
          }
          // otherwise just cut at chunkSizeChars
        }
      }

      chunks.add(content.substring(start, end));

      // if we reached the end, stop
      if (end >= content.length()) {
        break;
      }

      // advance with overlap, but always make forward progress
      int nextStart = end - overlapChars;
      if (nextStart <= start) {
        nextStart = end;
      }
      start = nextStart;
    }

    return chunks;
  }

  /**
   * retrieve specific chunks by exact match on sourceConfigId, name or url, and chunkIndexes.
   * filters by privacy realm access for the subject.
   * @param sourceConfigId the source config id (required)
   * @param name the document name (optional, at least one of name or url required)
   * @param url the document url (optional, at least one of name or url required)
   * @param chunkIndexes the chunk indexes to retrieve (required, non-empty)
   * @param subject the authenticated subject for privacy realm filtering (may be null)
   * @return list of results ordered by chunkIndex request order
   */
  public static List<DocSearchResult> retrieveChunks(String sourceConfigId, String name,
      String url, List<Integer> chunkIndexes, Subject subject) {

    List<DocSearchResult> results = new ArrayList<>();

    rebuildIfNeeded();

    DirectoryReader reader = currentReader;
    if (reader == null) {
      return results;
    }

    // set up privacy realm access checking
    GrouperDataEngine dataEngine = null;
    if (subject != null) {
      dataEngine = new GrouperDataEngine();
      dataEngine.loadConfigPrivacyRealms(null);
    }

    try {
      IndexSearcher searcher = new IndexSearcher(reader);

      for (int chunkIdx : chunkIndexes) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new TermQuery(new Term("sourceConfigId", sourceConfigId)),
            BooleanClause.Occur.MUST);
        if (StringUtils.isNotBlank(name)) {
          builder.add(new TermQuery(new Term("name", name)),
              BooleanClause.Occur.MUST);
        }
        if (StringUtils.isNotBlank(url)) {
          builder.add(new TermQuery(new Term("url", url)),
              BooleanClause.Occur.MUST);
        }
        builder.add(new TermQuery(new Term("chunkIndex", String.valueOf(chunkIdx))),
            BooleanClause.Occur.MUST);

        TopDocs topDocs = searcher.search(builder.build(), 1);

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
          Document doc = searcher.storedFields().document(scoreDoc.doc);

          // check privacy realm access
          String privacyRealmConfigId = doc.get("privacyRealmConfigId");
          if (!checkPrivacyRealmAccess(privacyRealmConfigId, subject, dataEngine)) {
            continue;
          }

          DocSearchResult result = populateResult(doc, scoreDoc.score);
          results.add(result);
        }
      }

    } catch (Exception e) {
      LOG.error("Error retrieving chunks from document index", e);
    }

    return results;
  }

  /**
   * check if the data dictionary indexing is enabled and has any content
   * @return true if enabled and there are data field or row configs
   */
  public static boolean isDataDictionaryEnabled() {
    boolean enabled = GrouperConfig.retrieveConfig().propertyValueBoolean(
        "grouper.mcp.docSearch.dataDictionary.enable", true);
    if (!enabled) {
      return false;
    }

    GrouperDataEngine dataEngine = new GrouperDataEngine();
    dataEngine.loadConfigFields(null);
    dataEngine.loadConfigRows(null);

    return !dataEngine.getFieldConfigByConfigId().isEmpty()
        || !dataEngine.getRowConfigByConfigId().isEmpty();
  }

  /**
   * check if there are any doc search sources available for the given subject.
   * considers SQL-based sources and data dictionary access.
   * @param subject the subject to check, or null to skip privacy checks
   * @return true if any sources are available
   */
  public static boolean hasAnySourcesForSubject(Subject subject) {
    // check SQL-based sources (not including data dictionary)
    Set<String> sqlConfigIds = GrouperConfig.retrieveConfig().propertyConfigIds(CONFIG_ID_PATTERN);
    if (!sqlConfigIds.isEmpty()) {
      return true;
    }

    // check data dictionary
    if (!isDataDictionaryEnabled()) {
      return false;
    }

    // check if subject has access to at least one privacy realm
    GrouperDataEngine dataEngine = new GrouperDataEngine();
    dataEngine.loadConfigFields(null);
    dataEngine.loadConfigRows(null);
    dataEngine.loadConfigPrivacyRealms(null);

    // collect all privacy realm config ids from fields and rows
    Set<String> realmConfigIds = new HashSet<>();
    for (GrouperDataFieldConfig fieldConfig : dataEngine.getFieldConfigByConfigId().values()) {
      if (StringUtils.isNotBlank(fieldConfig.getGrouperPrivacyRealmConfigId())) {
        realmConfigIds.add(fieldConfig.getGrouperPrivacyRealmConfigId());
      }
    }
    for (GrouperDataRowConfig rowConfig : dataEngine.getRowConfigByConfigId().values()) {
      if (StringUtils.isNotBlank(rowConfig.getPrivacyRealmName())) {
        realmConfigIds.add(rowConfig.getPrivacyRealmName());
      }
    }

    if (subject == null) {
      return !realmConfigIds.isEmpty();
    }

    for (String realmConfigId : realmConfigIds) {
      GrouperPrivacyRealmConfig realmConfig =
          dataEngine.getPrivacyRealmConfigByConfigId().get(realmConfigId);
      if (realmConfig == null) {
        continue;
      }
      // check if public
      if (realmConfig.isPrivacyRealmPublic()) {
        return true;
      }
      // check user access
      String access = dataEngine.calculateHighestLevelAccess(realmConfig, subject);
      if (StringUtils.isNotBlank(access)) {
        return true;
      }
    }

    return false;
  }

  /**
   * list distinct document names for a given sourceConfigId, filtered by privacy realm access.
   * @param sourceConfigId the source config id
   * @param subject the subject for privacy filtering
   * @param maxNames maximum names to return
   * @return list of distinct names, and whether the list was truncated
   */
  public static ListNamesResult listNames(String sourceConfigId, Subject subject, int maxNames) {

    ListNamesResult listNamesResult = new ListNamesResult();

    rebuildIfNeeded();

    Directory dir = currentDirectory;
    if (dir == null) {
      return listNamesResult;
    }

    GrouperDataEngine dataEngine = null;
    if (subject != null) {
      dataEngine = new GrouperDataEngine();
      dataEngine.loadConfigPrivacyRealms(null);
    }

    try {
      DirectoryReader reader = DirectoryReader.open(dir);
      IndexSearcher searcher = new IndexSearcher(reader);

      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      builder.add(new TermQuery(new Term("sourceConfigId", sourceConfigId)),
          BooleanClause.Occur.MUST);
      // only need chunk 0 to get unique names
      builder.add(new TermQuery(new Term("chunkIndex", "0")),
          BooleanClause.Occur.MUST);

      TopDocs topDocs = searcher.search(builder.build(), Integer.MAX_VALUE);

      Set<String> names = new LinkedHashSet<>();
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        Document doc = searcher.storedFields().document(scoreDoc.doc);

        // check privacy realm access
        String privacyRealmConfigId = doc.get("privacyRealmConfigId");
        if (!checkPrivacyRealmAccess(privacyRealmConfigId, subject, dataEngine)) {
          continue;
        }

        String name = doc.get("name");
        if (StringUtils.isNotBlank(name)) {
          names.add(name);
        }

        if (names.size() > maxNames) {
          listNamesResult.truncated = true;
          break;
        }
      }

      // if we collected maxNames+1, trim to maxNames
      if (names.size() > maxNames) {
        List<String> namesList = new ArrayList<>(names);
        listNamesResult.names = namesList.subList(0, maxNames);
      } else {
        listNamesResult.names = new ArrayList<>(names);
      }

    } catch (Exception e) {
      LOG.error("Error listing names for sourceConfigId: " + sourceConfigId, e);
    }

    return listNamesResult;
  }

  /**
   * result of listNames
   */
  public static class ListNamesResult {

    /** the names */
    List<String> names = new ArrayList<>();

    /** whether the list was truncated */
    boolean truncated = false;

    /**
     * get the names
     * @return names
     */
    public List<String> getNames() {
      return names;
    }

    /**
     * whether the list was truncated
     * @return true if truncated
     */
    public boolean isTruncated() {
      return truncated;
    }
  }

  /**
   * get the configured SQL-based doc search source config ids
   * @return set of config ids
   */
  public static Set<String> getConfigIds() {
    Set<String> configIds = new HashSet<>(
        GrouperConfig.retrieveConfig().propertyConfigIds(CONFIG_ID_PATTERN));
    if (isDataDictionaryEnabled()) {
      configIds.add(DATA_DICTIONARY_SOURCE_CONFIG_ID);
    }
    return configIds;
  }

  /**
   * get the documentation for AI client for a given config id
   * @param configId the config id
   * @return the documentation string
   */
  public static String getDocumentationForAiClient(String configId) {
    if (DATA_DICTIONARY_SOURCE_CONFIG_ID.equals(configId)) {
      return "Grouper data field and data row dictionary - descriptions and examples "
          + "for configured data fields and data rows, filtered by privacy realm access";
    }
    return GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.docSearch." + configId + ".documentationForAiClient");
  }

}
