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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * Manages in-memory Lucene indexes for MCP document search (RAG), chunking document content
 * and indexing it for full-text search.
 *
 * <p>There are three kinds of document source:</p>
 * <ul>
 *   <li>institution-managed database tables, read with a configurable SQL query</li>
 *   <li>directories of markdown files on the filesystem, which is how the Grouper wiki
 *   documentation shipped in the container ({@code grouperWiki}) is indexed</li>
 *   <li>the Grouper data field/row dictionary ({@code grouperDataDictionary}), whose results
 *   are filtered by privacy realm access at query time</li>
 * </ul>
 *
 * <p>Each source has its own index and its own refresh schedule, so an expensive source is not
 * rebuilt just because a cheap one is due, and sources which are already built stay searchable
 * while another one is being rebuilt. Searches run against a composite of every source's
 * index.</p>
 *
 * <p>Indexes are built lazily on first search rather than at startup, so nodes which never
 * search never pay to build them. The first search after a restart waits a short time for that
 * first build, see {@link #isIndexReady()}.</p>
 *
 * <p>Thread-safe: a source builds into a new index which is then swapped in, and readers are
 * reference counted so a search already running against an older composite keeps working until
 * it finishes.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpDocSearchIndex {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpDocSearchIndex.class);

  /** pattern to find doc search config ids which read documents from SQL */
  private static final Pattern SQL_CONFIG_ID_PATTERN =
      Pattern.compile("^grouper\\.mcp\\.docSearch\\.([^.]+)\\.query$");

  /** pattern to find doc search config ids which read documents from a directory on the filesystem */
  private static final Pattern FILESYSTEM_CONFIG_ID_PATTERN =
      Pattern.compile("^grouper\\.mcp\\.docSearch\\.([^.]+)\\.directory$");

  /** pattern to find doc search config ids which configure a score boost */
  private static final Pattern BOOST_CONFIG_ID_PATTERN =
      Pattern.compile("^grouper\\.mcp\\.docSearch\\.([^.]+)\\.boost$");

  /** source config id for the built-in data dictionary source */
  public static final String DATA_DICTIONARY_SOURCE_CONFIG_ID = "grouperDataDictionary";

  /** source config id for the built-in Grouper wiki documentation source */
  public static final String GROUPER_WIKI_SOURCE_CONFIG_ID = "grouperWiki";

  /** directory the Grouper wiki markdown documentation is shipped to in the container */
  public static final String DEFAULT_GROUPER_WIKI_DIRECTORY = "/opt/grouper/docs/wiki/Grouper";

  /** default file extensions indexed for filesystem sources */
  private static final String DEFAULT_FILE_EXTENSIONS = "md";

  /** how deep to recurse into a filesystem source directory */
  private static final int MAX_DIRECTORY_DEPTH = 20;

  /** skip files in a filesystem source larger than this many bytes */
  private static final int MAX_FILE_SIZE_BYTES = 5000000;

  /** stop after this many files in a single filesystem source */
  private static final int MAX_FILES_PER_SOURCE = 20000;

  /** how much more a match in the title/breadcrumb of a document counts than a match in the body */
  private static final float TITLE_BOOST = 3.0f;

  /** default target chunk size in characters (~800 tokens) */
  static final int CHUNK_SIZE_CHARS = 3200;

  /** default overlap between chunks in characters (~100 tokens) */
  static final int CHUNK_OVERLAP_CHARS = 400;

  /** override chunk size for testing, or -1 for default */
  public static int chunkSizeCharsOverrideForTesting = -1;

  /** override chunk overlap for testing, or -1 for default */
  public static int chunkOverlapCharsOverrideForTesting = -1;

  /**
   * override the directory of the built-in Grouper wiki source for testing, or null to use the
   * configured directory.  the wiki markdown is in the source tree under
   * grouper-misc/grouper-docs/wikiMirror/spaces/Grouper, and ships in the container at
   * {@link #DEFAULT_GROUPER_WIKI_DIRECTORY}
   */
  public static String grouperWikiDirectoryOverrideForTesting = null;

  /** default reindex interval in seconds (1 hour) */
  static final int DEFAULT_REINDEX_INTERVAL_SECONDS = 3600;

  /**
   * default reindex interval in seconds for filesystem sources (1 day).  documentation shipped
   * in the container does not change while the container is running, so there is no reason to
   * re-read it as often as a database source.
   */
  static final int DEFAULT_FILESYSTEM_REINDEX_INTERVAL_SECONDS = 86400;

  /**
   * how long the first query after a restart waits for the index to be built before giving up
   * and telling the client to retry
   */
  static final int DEFAULT_FIRST_QUERY_WAIT_SECONDS = 10;

  /**
   * how soon to retry a source whose build failed, rather than waiting out its normal reindex
   * interval, which for the shipped documentation is a day
   */
  static final int FAILED_BUILD_RETRY_SECONDS = 60;

  /** the analyzer used for indexing and searching */
  private static final StandardAnalyzer analyzer = new StandardAnalyzer();

  /**
   * one index per doc search source, keyed by config id.  each source is built and refreshed on
   * its own schedule, so a source which is cheap to build, e.g. a SQL query which reindexes
   * every few minutes, does not drag an expensive source, e.g. parsing a thousand wiki files,
   * along with it.  searches run against a composite of all of them.
   */
  private static final Map<String, SourceIndex> sourceIndexes =
      new ConcurrentHashMap<String, SourceIndex>();

  /**
   * composite of every source's reader, which is what searches run against.  it is a single
   * reader on purpose: Lucene computes collection statistics across the whole composite, so
   * scores from different sources are comparable.  searching each source separately and merging
   * the result lists would compute IDF per source and produce scores which cannot be ranked
   * against each other.
   */
  private static volatile MultiReader currentComposite;

  /** lock for index building and for swapping the composite reader */
  private static final Object INDEX_LOCK = new Object();

  /**
   * the index of one doc search source
   */
  private static class SourceIndex {

    /** reader over this source's index, null until the source is first built */
    private volatile DirectoryReader reader;

    /** when this source was last built (millis since epoch), 0 if never */
    private volatile long lastBuildMillis = 0;

    /** whether a build of this source is currently in progress */
    private volatile boolean buildInProgress = false;

    /** whether the last build of this source failed, so it is retried sooner */
    private volatile boolean lastBuildFailed = false;
  }

  /**
   * monitor threads wait on for the first index build to finish.  this is deliberately not
   * INDEX_LOCK, which is held for the duration of a build
   */
  private static final Object FIRST_BUILD_NOTIFIER = new Object();

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
    private String lastUpdated;

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

    /**
     * when the source document was last updated, if the source tracks that, e.g. the
     * lastUpdated of a wiki page.  null if unknown
     * @return the last updated string
     */
    public String getLastUpdated() {
      return this.lastUpdated;
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

    MultiReader reader = acquireComposite();
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

      // search the title/breadcrumb of the document as well as its body, and weight a title
      // match higher, so a document about a topic outranks one which merely mentions it
      Map<String, Float> fieldBoosts = new HashMap<String, Float>();
      fieldBoosts.put("content", 1.0f);
      fieldBoosts.put("title", TITLE_BOOST);
      MultiFieldQueryParser parser = new MultiFieldQueryParser(
          new String[] {"content", "title"}, analyzer, fieldBoosts);

      Query query;
      if ("lucene".equals(searchType)) {
        parser.setAllowLeadingWildcard(true);
        query = parser.parse(queryString);
      } else {
        // keyword mode: escape special chars for simple keyword matching
        parser.setAllowLeadingWildcard(false);
        query = parser.parse(QueryParser.escape(queryString));
      }

      query = applySourceBoosts(query);

      // filter by source in the query, not after the search.  one source can have many more
      // documents than another, e.g. the Grouper wiki, and post filtering would let that source
      // fill the results and starve the source which was asked for
      if (StringUtils.isNotBlank(filterSourceConfigId)) {
        BooleanQuery.Builder filteredBuilder = new BooleanQuery.Builder();
        filteredBuilder.add(query, BooleanClause.Occur.MUST);
        filteredBuilder.add(new TermQuery(new Term("sourceConfigId", filterSourceConfigId)),
            BooleanClause.Occur.FILTER);
        query = filteredBuilder.build();
      }

      // fetch more than maxResults since some may be filtered out by privacy realm
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
    } finally {
      releaseReader(reader);
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
    result.lastUpdated = doc.get("lastUpdated");
    return result;
  }

  /**
   * wrap a query so that hits from sources with a configured boost score higher than hits from
   * other sources.  this lets a site's own documentation outrank the shipped Grouper wiki.
   * if no source configures a boost, the query is returned unchanged.
   * @param query the parsed query
   * @return the query, possibly wrapped with per source boosts
   */
  private static Query applySourceBoosts(Query query) {

    // almost no one configures a boost, and enumerating the sources is not free, so check for
    // the properties before doing any work
    if (GrouperConfig.retrieveConfig().propertyConfigIds(BOOST_CONFIG_ID_PATTERN).isEmpty()) {
      return query;
    }

    Set<String> configIds = getConfigIds();

    boolean anyBoosts = false;
    for (String configId : configIds) {
      if (sourceBoost(configId) != 1.0f) {
        anyBoosts = true;
        break;
      }
    }

    if (!anyBoosts) {
      return query;
    }

    // every indexed document belongs to exactly one source, so a clause per source covers the
    // whole index.  boost each source's clause by its configured boost.
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String configId : configIds) {
      BooleanQuery.Builder sourceBuilder = new BooleanQuery.Builder();
      sourceBuilder.add(query, BooleanClause.Occur.MUST);
      sourceBuilder.add(new TermQuery(new Term("sourceConfigId", configId)),
          BooleanClause.Occur.FILTER);
      builder.add(new BoostQuery(sourceBuilder.build(), sourceBoost(configId)),
          BooleanClause.Occur.SHOULD);
    }

    return builder.build();
  }

  /**
   * get the configured score boost for a doc search source
   * @param configId the source config id
   * @return the boost, 1.0 if not configured
   */
  private static float sourceBoost(String configId) {
    String boostString = GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.docSearch." + configId + ".boost");
    if (StringUtils.isBlank(boostString)) {
      return 1.0f;
    }
    try {
      float boost = Float.parseFloat(boostString);
      if (boost <= 0.0f) {
        LOG.error("Ignoring non-positive boost for doc search source '" + configId
            + "': " + boostString);
        return 1.0f;
      }
      return boost;
    } catch (NumberFormatException nfe) {
      LOG.error("Ignoring invalid boost for doc search source '" + configId
          + "': " + boostString);
      return 1.0f;
    }
  }

  /**
   * rebuild the index if it is stale or not yet built.
   * runs the build in a background thread so the calling thread is not blocked.
   *
   * <p>On a cold start there is no index to search yet, and returning no results would look to
   * an AI client like the documentation has nothing on the topic, so it would answer from its
   * training data instead. So on a cold start only, wait a short time for the first build to
   * finish. If it does not finish in time the caller sees {@link #isIndexReady()} false and can
   * tell the client to retry, rather than silently searching nothing.</p>
   */
  public static void rebuildIfNeeded() {

    // work out the configured sources once and pass them down.  each of these is a scan of
    // every config property, and this runs on every query
    Set<String> filesystemConfigIds = getFilesystemConfigIds();
    Set<String> configIds = getConfigIds(filesystemConfigIds);

    boolean coldStart = !isIndexReady(configIds);

    startRebuildIfNeeded(configIds, filesystemConfigIds);

    if (coldStart) {
      waitForFirstBuild(configIds);
    }
  }

  /**
   * check if every configured doc search source has been built and can be searched.
   *
   * <p>This is all sources rather than any source on purpose. A search which silently left out
   * a source that had not finished building would look like a complete answer, and the caller
   * has no way to tell that something was missing. Refreshes of an already built source do not
   * affect this, so once warm, a slow source rebuilding never makes the others unavailable.</p>
   *
   * @return true if the index is ready to search
   */
  public static boolean isIndexReady() {
    return isIndexReady(getConfigIds());
  }

  /**
   * check if every configured doc search source has been built and can be searched
   * @param configIds the configured source config ids
   * @return true if the index is ready to search
   */
  private static boolean isIndexReady(Set<String> configIds) {

    for (String configId : configIds) {
      SourceIndex sourceIndex = sourceIndexes.get(configId);
      if (sourceIndex == null || sourceIndex.reader == null) {
        return false;
      }
    }

    // with no sources configured there is nothing to build, and an empty search result is a
    // real answer rather than a not ready yet answer
    return true;
  }

  /**
   * wait a short time for the first build of each source to finish, so the first search after a
   * restart does not silently return nothing while the background build runs
   */
  private static void waitForFirstBuild(Set<String> configIds) {

    if (isIndexReady(configIds)) {
      return;
    }

    int waitSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.mcp.docSearch.firstQueryWaitSeconds", DEFAULT_FIRST_QUERY_WAIT_SECONDS);

    if (waitSeconds <= 0) {
      return;
    }

    long deadline = System.currentTimeMillis() + (waitSeconds * 1000L);

    // note: this waits on its own monitor, not INDEX_LOCK.  a build thread holds INDEX_LOCK
    // while it swaps in its results, so waiting on that lock would tie the timeout to the
    // build rather than to the caller's patience
    synchronized (FIRST_BUILD_NOTIFIER) {
      while (!isIndexReady(configIds)) {
        long remainingMillis = deadline - System.currentTimeMillis();
        if (remainingMillis <= 0) {
          break;
        }
        try {
          FIRST_BUILD_NOTIFIER.wait(remainingMillis);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    if (!isIndexReady(configIds)) {
      LOG.warn("Doc search index was not built within " + waitSeconds
          + " seconds, doc search is not ready yet");
    }
  }

  /**
   * let any threads waiting on the first build know that a build finished, whether it
   * succeeded or not, so they do not wait out the full timeout after a failed build
   */
  private static void notifyFirstBuildDone() {
    synchronized (FIRST_BUILD_NOTIFIER) {
      FIRST_BUILD_NOTIFIER.notifyAll();
    }
  }

  /**
   * start a background rebuild of each source which is stale or not yet built.  sources are
   * independent, so one source rebuilding does not hold up another, and a source which is
   * already built stays searchable while another one builds.
   */
  private static void startRebuildIfNeeded(Set<String> configIds,
      Set<String> filesystemConfigIds) {

    pruneRemovedSources(configIds);

    for (String configId : configIds) {

      SourceIndex sourceIndex = sourceIndexes.get(configId);
      if (sourceIndex == null) {
        synchronized (INDEX_LOCK) {
          sourceIndex = sourceIndexes.get(configId);
          if (sourceIndex == null) {
            sourceIndex = new SourceIndex();
            sourceIndexes.put(configId, sourceIndex);
          }
        }
      }

      if (!isSourceStale(configId, sourceIndex, filesystemConfigIds)) {
        continue;
      }

      synchronized (INDEX_LOCK) {
        if (sourceIndex.buildInProgress) {
          continue;
        }
        if (!isSourceStale(configId, sourceIndex, filesystemConfigIds)) {
          continue;
        }
        sourceIndex.buildInProgress = true;
      }

      startSourceBuildThread(configId, sourceIndex);
    }
  }

  /**
   * check if a source has never been built or is due to be rebuilt
   * @param configId the source config id
   * @param sourceIndex the source index
   * @param filesystemConfigIds the config ids of the sources which read from the filesystem
   * @return true if the source should be built now
   */
  private static boolean isSourceStale(String configId, SourceIndex sourceIndex,
      Set<String> filesystemConfigIds) {

    if (sourceIndex.reader == null) {
      return true;
    }

    int intervalSeconds = getReindexIntervalSeconds(configId, filesystemConfigIds);

    // a source which failed is retried sooner than its normal interval, otherwise a source
    // which failed once would serve nothing until its next scheduled rebuild, which for the
    // shipped documentation is a day away
    if (sourceIndex.lastBuildFailed && intervalSeconds > FAILED_BUILD_RETRY_SECONDS) {
      intervalSeconds = FAILED_BUILD_RETRY_SECONDS;
    }

    long elapsed = System.currentTimeMillis() - sourceIndex.lastBuildMillis;
    return elapsed >= intervalSeconds * 1000L;
  }

  /**
   * build one source on a background thread
   * @param configId the source config id
   * @param sourceIndex the source index
   */
  private static void startSourceBuildThread(final String configId,
      final SourceIndex sourceIndex) {

    Thread buildThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          buildSource(configId, sourceIndex);
        } finally {
          sourceIndex.buildInProgress = false;
          notifyFirstBuildDone();
        }
      }

    }, "GrouperMcpDocSearchIndexBuilder-" + configId);

    buildThread.setDaemon(true);
    buildThread.start();
  }

  /**
   * drop the indexes of sources which are no longer configured
   * @param configIds the currently configured source config ids
   */
  private static void pruneRemovedSources(Set<String> configIds) {

    List<String> removedConfigIds = new ArrayList<String>();
    for (String configId : sourceIndexes.keySet()) {
      if (!configIds.contains(configId)) {
        removedConfigIds.add(configId);
      }
    }

    if (removedConfigIds.isEmpty()) {
      return;
    }

    synchronized (INDEX_LOCK) {
      for (String configId : removedConfigIds) {
        SourceIndex sourceIndex = sourceIndexes.remove(configId);
        if (sourceIndex != null) {
          releaseReader(sourceIndex.reader);
          LOG.info("Doc search source no longer configured, dropped from index: " + configId);
        }
      }
      refreshComposite();
    }
  }

  /**
   * how long to wait for a background build of a source to finish before forcing a rebuild of
   * that source anyway
   */
  private static final int CLAIM_BUILD_SLOT_WAIT_SECONDS = 60;

  /**
   * mark a source as being built by this thread, waiting for any build already running to
   * finish first.  gives up waiting after a while so a build which is stuck cannot block a
   * forced rebuild forever.
   * @param configId the source config id
   * @param sourceIndex the source index
   */
  private static void claimBuildSlot(String configId, SourceIndex sourceIndex) {

    long deadline = System.currentTimeMillis() + (CLAIM_BUILD_SLOT_WAIT_SECONDS * 1000L);

    while (true) {

      synchronized (INDEX_LOCK) {
        if (!sourceIndex.buildInProgress) {
          sourceIndex.buildInProgress = true;
          return;
        }
      }

      if (System.currentTimeMillis() >= deadline) {
        LOG.warn("Waited " + CLAIM_BUILD_SLOT_WAIT_SECONDS + " seconds for a build of doc "
            + "search source '" + configId + "' to finish, rebuilding it anyway");
        synchronized (INDEX_LOCK) {
          sourceIndex.buildInProgress = true;
        }
        return;
      }

      GrouperUtil.sleep(20);
    }
  }

  /**
   * force rebuild every source now (synchronously, blocks the calling thread)
   */
  public static void forceRebuild() {

    Set<String> configIds = getConfigIds();

    pruneRemovedSources(configIds);

    try {
      for (String configId : configIds) {

        SourceIndex sourceIndex = null;
        synchronized (INDEX_LOCK) {
          sourceIndex = sourceIndexes.get(configId);
          if (sourceIndex == null) {
            sourceIndex = new SourceIndex();
            sourceIndexes.put(configId, sourceIndex);
          }
        }

        // wait for any background build of this source to finish before claiming it.  setting
        // the flag without checking would let this build run at the same time as a background
        // build of the same source, and would clear a flag this thread did not set
        claimBuildSlot(configId, sourceIndex);

        try {
          buildSource(configId, sourceIndex);
        } finally {
          sourceIndex.buildInProgress = false;
        }
      }
    } finally {
      notifyFirstBuildDone();
    }
  }

  /**
   * get how often a source should be reindexed
   * @param configId the source config id
   * @param filesystemConfigIds the config ids of the sources which read from the filesystem
   * @return seconds
   */
  private static int getReindexIntervalSeconds(String configId,
      Set<String> filesystemConfigIds) {

    int defaultSeconds = DEFAULT_REINDEX_INTERVAL_SECONDS;
    if (filesystemConfigIds.contains(configId)) {
      defaultSeconds = DEFAULT_FILESYSTEM_REINDEX_INTERVAL_SECONDS;
    }

    return GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.mcp.docSearch." + configId + ".reindexIntervalSeconds", defaultSeconds);
  }

  /**
   * build one doc search source into its own in-memory index, then swap it in.  the other
   * sources are untouched and stay searchable throughout.
   * @param configId the source config id
   * @param sourceIndex the source index to swap the result into
   */
  private static void buildSource(String configId, SourceIndex sourceIndex) {

    ByteBuffersDirectory newDirectory = new ByteBuffersDirectory();

    try {
      IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
      IndexWriter writer = new IndexWriter(newDirectory, writerConfig);

      int[] counts = null;

      if (DATA_DICTIONARY_SOURCE_CONFIG_ID.equals(configId)) {
        counts = indexDataDictionary(writer);
      } else if (getFilesystemConfigIds().contains(configId)) {
        counts = indexFilesystemSource(writer, configId);
      } else {
        counts = indexSqlSource(writer, configId);
      }

      writer.close();

      DirectoryReader newReader = DirectoryReader.open(newDirectory);

      DirectoryReader oldReader = null;
      boolean orphaned = false;

      synchronized (INDEX_LOCK) {

        // the source could have been removed from configuration while this build was running.
        // if so this result has nowhere to go, and must be released rather than left dangling
        if (sourceIndexes.get(configId) != sourceIndex) {
          orphaned = true;
        } else {
          oldReader = sourceIndex.reader;
          sourceIndex.reader = newReader;
          sourceIndex.lastBuildMillis = System.currentTimeMillis();
          sourceIndex.lastBuildFailed = false;
          refreshComposite();
        }
      }

      if (orphaned) {
        releaseReader(newReader);
        LOG.info("Doc search source '" + configId
            + "' was removed from configuration while it was being built, discarding the build");
        return;
      }

      // drop this source index's own reference to the previous reader.  a composite which is
      // still being searched holds its own reference, so the reader is not actually closed
      // until those searches finish
      releaseReader(oldReader);

      LOG.info("Doc search source '" + configId + "' indexed: " + counts[0]
          + " documents, " + counts[1] + " chunks");

    } catch (Exception e) {
      LOG.error("Error building doc search source: " + configId, e);
      try {
        newDirectory.close();
      } catch (IOException e2) {
        // ignore
      }
      handleFailedBuild(configId, sourceIndex);
    }
  }

  /**
   * handle a source which failed to build.  a source which cannot be built must not take doc
   * search down for the sources which are fine, so a source which has never built successfully
   * is given an empty index and counts as built.  it contributes no results until a later build
   * succeeds, which is retried sooner than the source's normal reindex interval.
   * @param configId the source config id
   * @param sourceIndex the source index
   */
  private static void handleFailedBuild(String configId, SourceIndex sourceIndex) {

    synchronized (INDEX_LOCK) {

      // the source could have been removed from configuration while this build was running
      if (sourceIndexes.get(configId) != sourceIndex) {
        return;
      }

      sourceIndex.lastBuildFailed = true;

      // record the attempt, otherwise this source would be stale on every search and would be
      // rebuilt continuously, e.g. hammering a database which is down
      sourceIndex.lastBuildMillis = System.currentTimeMillis();

      if (sourceIndex.reader != null) {
        // there is an index from an earlier successful build, keep serving it.  content which
        // is out of date is better than no content
        return;
      }

      try {
        ByteBuffersDirectory emptyDirectory = new ByteBuffersDirectory();
        IndexWriter writer = new IndexWriter(emptyDirectory, new IndexWriterConfig(analyzer));
        writer.close();
        sourceIndex.reader = DirectoryReader.open(emptyDirectory);
        refreshComposite();

        LOG.error("Doc search source '" + configId + "' has never built successfully, it will "
            + "return no results and be retried in " + FAILED_BUILD_RETRY_SECONDS + " seconds. "
            + "The other doc search sources are unaffected.");

      } catch (Exception e) {
        LOG.error("Error creating empty index for doc search source: " + configId, e);
      }
    }
  }

  /**
   * rebuild the composite reader from every source which has been built.  callers must hold
   * INDEX_LOCK.
   */
  private static void refreshComposite() {

    List<DirectoryReader> subReaders = new ArrayList<DirectoryReader>();
    for (SourceIndex sourceIndex : sourceIndexes.values()) {
      if (sourceIndex.reader != null) {
        subReaders.add(sourceIndex.reader);
      }
    }

    MultiReader oldComposite = currentComposite;

    try {
      // closeSubReaders false means the composite incRefs each sub reader now and decRefs it
      // when the composite is released, which is what keeps a sub reader alive for searches
      // still running against an older composite
      currentComposite = new MultiReader(
          subReaders.toArray(new DirectoryReader[subReaders.size()]), false);
    } catch (IOException e) {
      LOG.error("Error building composite doc search reader", e);
      return;
    }

    releaseReader(oldComposite);
  }

  /**
   * drop a reference to a reader.  it is closed once nothing else holds a reference to it.
   * @param reader the reader, may be null
   */
  private static void releaseReader(IndexReader reader) {
    if (reader == null) {
      return;
    }
    try {
      reader.decRef();
    } catch (IOException e) {
      LOG.debug("Error releasing doc search reader", e);
    }
  }

  /**
   * get the composite reader for searching, with a reference held so it is not closed while in
   * use.  the caller must call {@link #releaseReader(IndexReader)} when done.
   * @return the composite reader, or null if there is nothing to search
   */
  private static MultiReader acquireComposite() {

    while (true) {
      MultiReader composite = currentComposite;
      if (composite == null) {
        return null;
      }
      // the composite could be swapped out and released between reading the field and using
      // it, so only use it if a reference can still be taken
      if (composite.tryIncRef()) {
        return composite;
      }
    }
  }

  /**
   * index one SQL based doc search source
   * @param writer the index writer
   * @param configId the source config id
   * @return int array of [totalDocs, totalChunks]
   */
  private static int[] indexSqlSource(IndexWriter writer, String configId) {

    int totalDocs = 0;
    int totalChunks = 0;

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
          addChunkDocument(writer, chunks.get(i), name, name, url, configId,
              i, totalChunksForDoc, null, null);
          totalChunks++;
        }
      }

    } catch (Exception e) {
      LOG.error("Error loading doc search source: " + configId, e);
    }

    return new int[] {totalDocs, totalChunks};
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
          addChunkDocument(writer, chunks.get(i), configId, configId, null,
              DATA_DICTIONARY_SOURCE_CONFIG_ID, i, totalChunksForDoc, privacyRealmConfigId, null);
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
          addChunkDocument(writer, chunks.get(i), configId, configId, null,
              DATA_DICTIONARY_SOURCE_CONFIG_ID, i, totalChunksForDoc, privacyRealmConfigId, null);
          totalChunks++;
        }
      }

    } catch (Exception e) {
      LOG.error("Error indexing data dictionary", e);
    }

    return new int[] {totalDocs, totalChunks};
  }

  /**
   * add one chunk of a document to the index
   * @param writer the index writer
   * @param chunk the chunk text, stored and indexed
   * @param titleText the title/breadcrumb of the document, indexed but not stored, and boosted
   * relative to the body at query time.  may be null
   * @param name the document name, this is the key callers use to retrieve chunks
   * @param url the source url, may be null
   * @param sourceConfigId the doc search source this document came from
   * @param chunkIndex the index of this chunk in the document
   * @param totalChunksForDocument how many chunks the document has
   * @param privacyRealmConfigId privacy realm which controls access to this document, may be null
   * @param lastUpdated when the source document was last updated, may be null
   * @throws IOException if the document cannot be written
   */
  private static void addChunkDocument(IndexWriter writer, String chunk, String titleText,
      String name, String url, String sourceConfigId, int chunkIndex, int totalChunksForDocument,
      String privacyRealmConfigId, String lastUpdated) throws IOException {

    Document doc = new Document();
    doc.add(new TextField("content", chunk, Field.Store.YES));
    if (StringUtils.isNotBlank(titleText)) {
      doc.add(new TextField("title", titleText, Field.Store.NO));
    }
    if (StringUtils.isNotBlank(url)) {
      doc.add(new StringField("url", url, Field.Store.YES));
    }
    doc.add(new StringField("name", name, Field.Store.YES));
    doc.add(new StringField("sourceConfigId", sourceConfigId, Field.Store.YES));
    doc.add(new StringField("chunkIndex", String.valueOf(chunkIndex), Field.Store.YES));
    doc.add(new StoredField("totalChunksForDocument", String.valueOf(totalChunksForDocument)));
    if (StringUtils.isNotBlank(privacyRealmConfigId)) {
      doc.add(new StringField("privacyRealmConfigId", privacyRealmConfigId, Field.Store.YES));
    }
    if (StringUtils.isNotBlank(lastUpdated)) {
      doc.add(new StoredField("lastUpdated", lastUpdated));
    }
    writer.addDocument(doc);
  }

  /**
   * index a source which reads markdown files from a directory on the filesystem.  this is how
   * the Grouper wiki documentation shipped in the container is indexed.  each file is one
   * document, YAML frontmatter at the top of the file supplies the title and url, and the
   * directory structure supplies a breadcrumb which is prepended to each chunk so a chunk from
   * the middle of a long page still says what page it came from.
   * @param writer the index writer
   * @param configId the doc search source config id
   * @return int array of [totalDocs, totalChunks]
   */
  private static int[] indexFilesystemSource(IndexWriter writer, String configId) {
    int totalDocs = 0;
    int totalChunks = 0;

    String directoryName = filesystemDirectory(configId);

    if (StringUtils.isBlank(directoryName)) {
      LOG.error("Doc search source '" + configId + "' has no directory configured");
      return new int[] {totalDocs, totalChunks};
    }

    File directory = new File(directoryName);

    if (!directory.exists() || !directory.isDirectory()) {
      // this is normal outside the container, e.g. a WAR deployment without the shipped docs
      LOG.info("Doc search source '" + configId + "' directory does not exist, skipping: "
          + directoryName);
      return new int[] {totalDocs, totalChunks};
    }

    try {
      Set<String> extensions = filesystemFileExtensions(configId);

      List<File> files = new ArrayList<File>();
      listFilesRecursive(directory, extensions, files, 0);

      if (files.size() >= MAX_FILES_PER_SOURCE) {
        LOG.error("Doc search source '" + configId + "' has at least " + MAX_FILES_PER_SOURCE
            + " files, only indexing the first " + MAX_FILES_PER_SOURCE);
      }

      // names are the key callers use to retrieve chunks, so they must be unique in a source
      Set<String> namesUsed = new HashSet<String>();

      // resolve the directory once, not once per file
      String directoryCanonicalPath = GrouperUtil.fileCanonicalPath(directory);

      for (File file : files) {

        String relativePath = relativePath(directoryCanonicalPath, file);

        String fileContents = null;
        try {
          fileContents = GrouperUtil.readFileIntoString(file);
        } catch (Exception e) {
          LOG.error("Error reading doc search file: " + relativePath, e);
          continue;
        }

        MarkdownDocument markdownDocument = parseMarkdown(fileContents);

        String body = markdownDocument.getBody();
        if (StringUtils.isBlank(body)) {
          continue;
        }

        String title = markdownDocument.getTitle();
        if (StringUtils.isBlank(title)) {
          title = titleFromPath(relativePath);
        }

        String name = title;
        if (StringUtils.isBlank(name) || namesUsed.contains(name)) {
          // duplicate titles across a directory tree, fall back to the path which is unique
          name = StringUtils.removeEnd(relativePath, "." + extensionOfFile(file));
          if (namesUsed.contains(name)) {
            LOG.error("Skipping doc search document with duplicate name '" + name
                + "' in source '" + configId + "'");
            continue;
          }
        }
        namesUsed.add(name);

        String breadcrumb = buildBreadcrumb(relativePath, title);

        List<String> chunks = chunkContent(body);
        int totalChunksForDoc = chunks.size();
        totalDocs++;

        for (int i = 0; i < chunks.size(); i++) {
          String chunkWithBreadcrumb = breadcrumb + "\n\n" + chunks.get(i);
          addChunkDocument(writer, chunkWithBreadcrumb, breadcrumb, name,
              markdownDocument.getUrl(), configId, i, totalChunksForDoc, null,
              markdownDocument.getLastUpdated());
          totalChunks++;
        }
      }

      LOG.debug("Doc search source '" + configId + "' indexed " + totalDocs
          + " documents from " + directoryName);

    } catch (Exception e) {
      LOG.error("Error indexing doc search source: " + configId, e);
    }

    return new int[] {totalDocs, totalChunks};
  }

  /**
   * list files under a directory recursively, filtered by extension, skipping hidden files,
   * symlinks, and files which are too large.  results are sorted so the index is built in a
   * stable order.
   * @param directory the directory to list
   * @param extensions lowercase extensions to include, empty for all
   * @param files the list to add to
   * @param depth the current recursion depth
   */
  private static void listFilesRecursive(File directory, Set<String> extensions,
      List<File> files, int depth) {

    if (depth > MAX_DIRECTORY_DEPTH) {
      LOG.error("Doc search directory is nested deeper than " + MAX_DIRECTORY_DEPTH
          + ", not recursing: " + GrouperUtil.fileCanonicalPath(directory));
      return;
    }

    File[] children = directory.listFiles();
    if (children == null) {
      return;
    }

    Arrays.sort(children, new Comparator<File>() {
      @Override
      public int compare(File file1, File file2) {
        return file1.getName().compareTo(file2.getName());
      }
    });

    for (File child : children) {

      if (files.size() >= MAX_FILES_PER_SOURCE) {
        return;
      }

      if (child.getName().startsWith(".")) {
        continue;
      }

      // dont follow symlinks, the docs directory should be self contained
      if (Files.isSymbolicLink(child.toPath())) {
        continue;
      }

      if (child.isDirectory()) {
        listFilesRecursive(child, extensions, files, depth + 1);
        continue;
      }

      if (!child.isFile()) {
        continue;
      }

      if (!extensions.isEmpty() && !extensions.contains(extensionOfFile(child))) {
        continue;
      }

      if (child.length() > MAX_FILE_SIZE_BYTES) {
        LOG.error("Skipping doc search file larger than " + MAX_FILE_SIZE_BYTES + " bytes: "
            + GrouperUtil.fileCanonicalPath(child));
        continue;
      }

      files.add(child);
    }
  }

  /**
   * get the lowercase extension of a file, without the dot
   * @param file the file
   * @return the extension, empty string if none
   */
  private static String extensionOfFile(File file) {
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(dotIndex + 1).toLowerCase();
  }

  /**
   * get the path of a file relative to the source directory, with forward slashes
   * @param directoryCanonicalPath the canonical path of the source directory
   * @param file the file
   * @return the relative path
   */
  private static String relativePath(String directoryCanonicalPath, File file) {
    String filePath = GrouperUtil.fileCanonicalPath(file);
    String relativePath = filePath;
    if (filePath.startsWith(directoryCanonicalPath)) {
      relativePath = filePath.substring(directoryCanonicalPath.length());
    }
    relativePath = relativePath.replace('\\', '/');
    return StringUtils.removeStart(relativePath, "/");
  }

  /**
   * derive a title from a file path when the file has no title in its frontmatter
   * @param relativePath the path relative to the source directory
   * @return the title
   */
  private static String titleFromPath(String relativePath) {
    String fileName = relativePath;
    int slashIndex = fileName.lastIndexOf('/');
    if (slashIndex != -1) {
      fileName = fileName.substring(slashIndex + 1);
    }
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      fileName = fileName.substring(0, dotIndex);
    }
    return fileName.replace('_', ' ').trim();
  }

  /**
   * build a breadcrumb from the directory structure, e.g.
   * "Grouper Wiki Home &gt; Grouper Administration Guides &gt; Provisioning and Integration".
   * the wiki mirror nests child pages in a directory named after the parent page, so the
   * directories are the page hierarchy.
   * @param relativePath the path relative to the source directory
   * @param title the document title
   * @return the breadcrumb
   */
  private static String buildBreadcrumb(String relativePath, String title) {

    StringBuilder breadcrumb = new StringBuilder();

    String[] pathParts = GrouperUtil.splitTrim(relativePath, "/");
    // the last part is the file itself, the title covers that
    for (int i = 0; i < GrouperUtil.length(pathParts) - 1; i++) {
      String pathPart = pathParts[i].replace('_', ' ').trim();
      if (StringUtils.isBlank(pathPart)) {
        continue;
      }
      breadcrumb.append(pathPart).append(" > ");
    }

    breadcrumb.append(StringUtils.defaultIfBlank(title, ""));

    return breadcrumb.toString();
  }

  /**
   * a markdown file split into its YAML frontmatter and its body
   */
  public static class MarkdownDocument {

    private String title;
    private String url;
    private String lastUpdated;
    private String body;

    /**
     * the title from frontmatter, null if none
     * @return the title
     */
    public String getTitle() {
      return this.title;
    }

    /**
     * the url of the source document from frontmatter, null if none
     * @return the url
     */
    public String getUrl() {
      return this.url;
    }

    /**
     * when the source document was last updated, from frontmatter, null if none
     * @return the last updated
     */
    public String getLastUpdated() {
      return this.lastUpdated;
    }

    /**
     * the file contents with the frontmatter removed
     * @return the body
     */
    public String getBody() {
      return this.body;
    }
  }

  /**
   * split a markdown file into its YAML frontmatter and its body.  the frontmatter is the block
   * between a line of three dashes at the very start of the file and the next line of three
   * dashes.  if there is no frontmatter the whole file is the body.
   * @param fileContents the contents of the markdown file
   * @return the parsed document, never null
   */
  public static MarkdownDocument parseMarkdown(String fileContents) {

    MarkdownDocument markdownDocument = new MarkdownDocument();

    if (fileContents == null) {
      markdownDocument.body = "";
      return markdownDocument;
    }

    String contents = StringUtils.removeStart(fileContents, "\uFEFF");

    int firstLineEnd = contents.indexOf('\n');
    if (firstLineEnd == -1 || !"---".equals(contents.substring(0, firstLineEnd).trim())) {
      markdownDocument.body = contents;
      return markdownDocument;
    }

    int bodyStart = -1;
    int lineStart = firstLineEnd + 1;

    while (lineStart < contents.length()) {

      int lineEnd = contents.indexOf('\n', lineStart);
      String line = lineEnd == -1 ? contents.substring(lineStart)
          : contents.substring(lineStart, lineEnd);

      if ("---".equals(line.trim())) {
        bodyStart = lineEnd == -1 ? contents.length() : lineEnd + 1;
        break;
      }

      int colonIndex = line.indexOf(':');
      if (colonIndex > 0) {
        String key = line.substring(0, colonIndex).trim();
        String value = unquote(line.substring(colonIndex + 1).trim());
        if ("title".equals(key)) {
          markdownDocument.title = value;
        } else if ("url".equals(key)) {
          markdownDocument.url = value;
        } else if ("lastUpdated".equals(key)) {
          markdownDocument.lastUpdated = value;
        }
      }

      if (lineEnd == -1) {
        break;
      }
      lineStart = lineEnd + 1;
    }

    if (bodyStart == -1) {
      // no closing dashes, this was not frontmatter after all
      markdownDocument.title = null;
      markdownDocument.url = null;
      markdownDocument.lastUpdated = null;
      markdownDocument.body = contents;
      return markdownDocument;
    }

    markdownDocument.body = contents.substring(bodyStart);

    return markdownDocument;
  }

  /**
   * strip matching single or double quotes from around a frontmatter value, and unescape the
   * quotes and backslashes inside it.  wiki page titles really do contain quotes, e.g.
   * {@code title: "Grouper daemon \"other job\" to run a script"}, and leaving the escaping in
   * would put backslashes in the document name.
   * @param value the value
   * @return the unquoted value
   */
  private static String unquote(String value) {

    if (value == null || value.length() < 2) {
      return value;
    }

    if (value.startsWith("'") && value.endsWith("'")) {
      // single quoted values are not escaped
      return value.substring(1, value.length() - 1);
    }

    if (!value.startsWith("\"") || !value.endsWith("\"")) {
      return value;
    }

    String quoted = value.substring(1, value.length() - 1);

    StringBuilder result = new StringBuilder(quoted.length());

    for (int i = 0; i < quoted.length(); i++) {

      char currentChar = quoted.charAt(i);

      // a backslash escapes the character after it, and is dropped
      if (currentChar == '\\' && i < quoted.length() - 1) {
        i++;
        result.append(quoted.charAt(i));
        continue;
      }

      result.append(currentChar);
    }

    return result.toString();
  }

  /**
   * get the directory a filesystem doc search source reads from
   * @param configId the source config id
   * @return the directory name, null if not configured
   */
  private static String filesystemDirectory(String configId) {
    if (GROUPER_WIKI_SOURCE_CONFIG_ID.equals(configId)) {
      if (grouperWikiDirectoryOverrideForTesting != null) {
        return grouperWikiDirectoryOverrideForTesting;
      }
      return GrouperConfig.retrieveConfig().propertyValueString(
          "grouper.mcp.docSearch." + configId + ".directory", DEFAULT_GROUPER_WIKI_DIRECTORY);
    }
    return GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.docSearch." + configId + ".directory");
  }

  /**
   * get the lowercase file extensions a filesystem doc search source indexes
   * @param configId the source config id
   * @return set of extensions without dots, empty set means all files
   */
  private static Set<String> filesystemFileExtensions(String configId) {

    String extensionsString = GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.docSearch." + configId + ".fileExtensions", DEFAULT_FILE_EXTENSIONS);

    Set<String> extensions = new HashSet<String>();
    for (String extension : GrouperUtil.nonNull(GrouperUtil.splitTrim(extensionsString, ","), String.class)) {
      extensions.add(StringUtils.removeStart(extension.toLowerCase(), "."));
    }

    return extensions;
  }

  /**
   * check if the built-in Grouper wiki documentation source is enabled and its directory is
   * present.  the directory ships in the container, so it is normally absent in other
   * deployments and in development.
   * @return true if enabled
   */
  public static boolean isGrouperWikiEnabled() {

    boolean enabled = GrouperConfig.retrieveConfig().propertyValueBoolean(
        "grouper.mcp.docSearch.grouperWiki.enable", true);
    if (!enabled) {
      return false;
    }

    String directoryName = filesystemDirectory(GROUPER_WIKI_SOURCE_CONFIG_ID);
    if (StringUtils.isBlank(directoryName)) {
      return false;
    }

    File directory = new File(directoryName);
    return directory.exists() && directory.isDirectory();
  }

  /**
   * get the doc search source config ids which read documents from SQL
   * @return set of config ids
   */
  public static Set<String> getSqlConfigIds() {
    return new LinkedHashSet<String>(
        GrouperConfig.retrieveConfig().propertyConfigIds(SQL_CONFIG_ID_PATTERN));
  }

  /**
   * get the doc search source config ids which read markdown files from the filesystem,
   * including the built-in Grouper wiki source if it is enabled
   * @return set of config ids
   */
  public static Set<String> getFilesystemConfigIds() {

    Set<String> configIds = new LinkedHashSet<String>(
        GrouperConfig.retrieveConfig().propertyConfigIds(FILESYSTEM_CONFIG_ID_PATTERN));

    if (isGrouperWikiEnabled()) {
      configIds.add(GROUPER_WIKI_SOURCE_CONFIG_ID);
    } else {
      // the directory can be configured but the source turned off, or the directory missing
      configIds.remove(GROUPER_WIKI_SOURCE_CONFIG_ID);
    }

    return configIds;
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
          } else {
            // no line break either, which happens in long markdown tables and code blocks.
            // back up to a word boundary so the chunk does not end mid word
            int wordEnd = lastWordBoundary(content, end, start + chunkSizeChars / 2);
            if (wordEnd > start + chunkSizeChars / 2) {
              end = wordEnd;
            }
            // otherwise it is one very long token, just cut at chunkSizeChars
          }
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

      // subtracting the overlap lands on an arbitrary character, so back up to the start of
      // the word it landed in.  otherwise the next chunk begins with a word fragment, e.g.
      // "incipal = ..." instead of "principal = ...", which reads badly in search results
      // and loses a term which would otherwise have matched
      if (nextStart > start && nextStart < content.length()
          && !Character.isWhitespace(content.charAt(nextStart - 1))) {
        int wordStart = lastWordBoundary(content, nextStart, start);
        if (wordStart > start) {
          nextStart = wordStart;
        }
      }

      start = nextStart;
    }

    return chunks;
  }

  /**
   * find the boundary at or before an index where a word starts, that is the index just after
   * the closest whitespace character.  used so chunks do not begin or end mid word.
   * @param content the full content text
   * @param index the index to search back from
   * @param floor do not search back past this index
   * @return the index just after the closest whitespace before index, or floor if there is no
   * whitespace to be found, meaning the whole span is one long token
   */
  private static int lastWordBoundary(String content, int index, int floor) {

    int boundary = index;

    while (boundary > floor && !Character.isWhitespace(content.charAt(boundary - 1))) {
      boundary--;
    }

    return boundary;
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

    MultiReader reader = acquireComposite();
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
    } finally {
      releaseReader(reader);
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
   * considers SQL sources, filesystem sources, and data dictionary access.
   * @param subject the subject to check, or null to skip privacy checks
   * @return true if any sources are available
   */
  public static boolean hasAnySourcesForSubject(Subject subject) {
    // check SQL-based sources (not including data dictionary)
    Set<String> sqlConfigIds = getSqlConfigIds();
    if (!sqlConfigIds.isEmpty()) {
      return true;
    }

    // check filesystem sources, e.g. the Grouper wiki, which are not privacy filtered
    if (!getFilesystemConfigIds().isEmpty()) {
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

    MultiReader reader = acquireComposite();
    if (reader == null) {
      return listNamesResult;
    }

    GrouperDataEngine dataEngine = null;
    if (subject != null) {
      dataEngine = new GrouperDataEngine();
      dataEngine.loadConfigPrivacyRealms(null);
    }

    try {
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
    } finally {
      releaseReader(reader);
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
    return getConfigIds(getFilesystemConfigIds());
  }

  /**
   * get the config ids of every doc search source, given the filesystem sources which the
   * caller has already worked out
   * @param filesystemConfigIds the config ids of the sources which read from the filesystem
   * @return set of config ids
   */
  private static Set<String> getConfigIds(Set<String> filesystemConfigIds) {
    Set<String> configIds = new LinkedHashSet<String>(getSqlConfigIds());
    configIds.addAll(filesystemConfigIds);
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

    String documentationForAiClient = GrouperConfig.retrieveConfig().propertyValueString(
        "grouper.mcp.docSearch." + configId + ".documentationForAiClient");

    if (StringUtils.isBlank(documentationForAiClient)
        && GROUPER_WIKI_SOURCE_CONFIG_ID.equals(configId)) {
      // this ends up in the tool description, which is in the AI client's context for the whole
      // session, so keep it short
      documentationForAiClient = "Grouper product documentation wiki for Grouper "
          + GrouperVersion.grouperVersion()
          + ": how Grouper features work and are configured. Not institution specific, and "
          + "frozen at that release, so check result urls for the current page.";
    }

    return documentationForAiClient;
  }

}
