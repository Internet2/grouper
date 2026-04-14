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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.ConfigFile;

/**
 * Manages an in-memory Lucene index for searching Grouper configuration properties.
 * Indexes all properties from all ConfigPropertiesCascadeBase subclasses
 * (via ConfigFileName enum). Sensitive values (passwords, secrets) are masked.
 * Includes metadata per property: where configured (base/override/database),
 * default value, EL expression, comment, value type, and required flag.
 *
 * <p>The index is built lazily on first search and rebuilt periodically
 * (default every hour).</p>
 *
 * <p>Thread-safe: builds a new index then swaps the reference atomically.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpConfigSearchIndex {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpConfigSearchIndex.class);

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

  /** lock for index building */
  private static final Object INDEX_LOCK = new Object();

  /** masked value for sensitive configs */
  public static final String MASKED_VALUE = "*****";

  /**
   * result object for a config search hit
   */
  public static class ConfigSearchResult {

    private String key;
    private String value;
    private String configFile;
    private boolean sensitive;
    private float score;
    private String configuredIn;
    private String defaultValue;
    private String elScript;
    private String comment;
    private String valueType;
    private boolean required;

    public String getKey() {
      return this.key;
    }

    public String getValue() {
      return this.value;
    }

    public String getConfigFile() {
      return this.configFile;
    }

    public boolean isSensitive() {
      return this.sensitive;
    }

    public float getScore() {
      return this.score;
    }

    public String getConfiguredIn() {
      return this.configuredIn;
    }

    public String getDefaultValue() {
      return this.defaultValue;
    }

    public String getElScript() {
      return this.elScript;
    }

    public String getComment() {
      return this.comment;
    }

    public String getValueType() {
      return this.valueType;
    }

    public boolean isRequired() {
      return this.required;
    }
  }

  /**
   * search the config index
   * @param queryString the Lucene query string
   * @param filterConfigFile optional config file name to filter by, or null for all
   * @param maxResults max results to return
   * @return list of search results ordered by relevance
   */
  public static List<ConfigSearchResult> search(String queryString,
      String filterConfigFile, int maxResults) {

    List<ConfigSearchResult> results = new ArrayList<>();

    rebuildIfNeeded();

    DirectoryReader reader = currentReader;
    if (reader == null) {
      return results;
    }

    try {
      IndexSearcher searcher = new IndexSearcher(reader);
      QueryParser parser = new QueryParser("content", analyzer);
      parser.setAllowLeadingWildcard(true);

      Query contentQuery = parser.parse(queryString);

      Query finalQuery;
      if (StringUtils.isNotBlank(filterConfigFile)) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(contentQuery, BooleanClause.Occur.MUST);
        builder.add(new TermQuery(new Term("configFile", filterConfigFile)),
            BooleanClause.Occur.MUST);
        finalQuery = builder.build();
      } else {
        finalQuery = contentQuery;
      }

      TopDocs topDocs = searcher.search(finalQuery, maxResults);

      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        Document doc = searcher.storedFields().document(scoreDoc.doc);

        ConfigSearchResult result = new ConfigSearchResult();
        result.key = doc.get("key");
        result.value = doc.get("value");
        result.configFile = doc.get("configFile");
        result.sensitive = "true".equals(doc.get("sensitive"));
        result.score = scoreDoc.score;
        result.configuredIn = doc.get("configuredIn");
        result.defaultValue = doc.get("defaultValue");
        result.elScript = doc.get("elScript");
        result.comment = doc.get("comment");
        result.valueType = doc.get("valueType");
        result.required = "true".equals(doc.get("required"));
        results.add(result);
      }

    } catch (Exception e) {
      LOG.error("Error searching config index", e);
    }

    return results;
  }

  /**
   * rebuild the index if it is stale or not yet built
   */
  public static void rebuildIfNeeded() {
    if (currentReader != null) {
      long now = System.currentTimeMillis();
      long elapsed = now - lastIndexBuildMillis;
      if (elapsed < DEFAULT_REINDEX_INTERVAL_SECONDS * 1000L) {
        return;
      }
    }

    synchronized (INDEX_LOCK) {
      // double-check after acquiring lock
      if (currentReader != null) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastIndexBuildMillis;
        if (elapsed < DEFAULT_REINDEX_INTERVAL_SECONDS * 1000L) {
          return;
        }
      }

      buildIndex();
    }
  }

  /**
   * force rebuild the index now
   */
  public static void forceRebuild() {
    synchronized (INDEX_LOCK) {
      buildIndex();
    }
  }

  /**
   * determine where a config property value is set from (base, override file, database)
   * @param config the config cascade
   * @param key the property key
   * @return the source location string (e.g. "grouper.base.properties", "grouper.properties", "database")
   */
  private static String determineConfiguredIn(ConfigPropertiesCascadeBase config, String key) {
    try {
      List<ConfigFile> configFiles = new ArrayList<>(config.internalRetrieveConfigFiles());
      Collections.reverse(configFiles);

      String elKey = key + ".elConfig";

      for (ConfigFile configFile : configFiles) {
        Properties properties = configFile.getProperties();
        if (properties.containsKey(elKey) || properties.containsKey(key)) {
          String fromWhere = configFile.getOriginalConfig();
          if (fromWhere != null) {
            fromWhere = fromWhere.replace("classpath:", "");
            fromWhere = fromWhere.replace("database:grouper", "database");
          }
          return fromWhere;
        }
      }
    } catch (Exception e) {
      LOG.debug("Error determining configuredIn for key: " + key, e);
    }
    return null;
  }

  /**
   * get the EL expression for a config property if it has one
   * @param config the config cascade
   * @param key the property key
   * @return the EL expression string, or null if not an EL property
   */
  private static String getElScript(ConfigPropertiesCascadeBase config, String key) {
    try {
      List<ConfigFile> configFiles = new ArrayList<>(config.internalRetrieveConfigFiles());
      Collections.reverse(configFiles);

      String elKey = key + ".elConfig";

      for (ConfigFile configFile : configFiles) {
        Properties properties = configFile.getProperties();
        if (properties.containsKey(elKey)) {
          return properties.getProperty(elKey);
        }
        if (properties.containsKey(key)) {
          return null;
        }
      }
    } catch (Exception e) {
      LOG.debug("Error getting EL script for key: " + key, e);
    }
    return null;
  }

  /**
   * get the default/base value for a config property
   * @param config the config cascade
   * @param key the property key
   * @param metadata the config item metadata (may be null)
   * @return the default value, or null
   */
  private static String getDefaultValue(ConfigPropertiesCascadeBase config, String key,
      ConfigItemMetadata metadata) {
    try {
      List<ConfigFile> configFiles = config.internalRetrieveConfigFiles();
      if (configFiles != null && !configFiles.isEmpty()) {
        // base config is first in the list
        ConfigFile baseConfigFile = configFiles.get(0);
        Properties properties = baseConfigFile.getProperties();
        String elKey = key + ".elConfig";
        if (properties.containsKey(elKey)) {
          return properties.getProperty(elKey);
        }
        if (properties.containsKey(key)) {
          return properties.getProperty(key);
        }
      }
    } catch (Exception e) {
      LOG.debug("Error getting default value for key: " + key, e);
    }

    // fall back to metadata default
    if (metadata != null && StringUtils.isNotBlank(metadata.getDefaultValue())) {
      return metadata.getDefaultValue();
    }
    return null;
  }

  /**
   * build the index from all ConfigFileName sources.
   * creates a new in-memory directory, indexes all config properties, then swaps
   * the reference so searches use the new index.
   */
  private static void buildIndex() {

    ByteBuffersDirectory newDirectory = new ByteBuffersDirectory();

    try {
      IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
      IndexWriter writer = new IndexWriter(newDirectory, writerConfig);

      int totalConfigs = 0;

      for (ConfigFileName fileName : ConfigFileName.values()) {
        try {
          ConfigPropertiesCascadeBase config = fileName.getConfig();
          if (config == null) {
            continue;
          }

          // pre-load configFileMetadata once per config file to avoid repeated database lookups
          ConfigFileMetadata configFileMetadata = null;
          try {
            configFileMetadata = fileName.configFileMetadata();
          } catch (Exception e) {
            LOG.debug("Error loading config file metadata for " + fileName.getConfigFileName(), e);
          }

          Set<String> propertyNames = config.propertyNames();
          for (String key : propertyNames) {

            String value = config.propertyValueString(key);

            // look up metadata once, then pass to isPasswordHelper so it doesn't re-fetch
            ConfigItemMetadata metadata = null;
            try {
              if (configFileMetadata != null) {
                metadata = configFileMetadata.findConfigItemMetdataFromConfig(key);
              }
            } catch (Exception e) {
              LOG.debug("Error finding metadata for key: " + key, e);
            }

            // pass null for configFileName so isPasswordHelper doesn't re-fetch metadata
            // from the database when metadata is null (we already looked it up)
            boolean isSensitive = GrouperConfigHibernate.isPasswordHelper(
                null, metadata, key, value,
                StringUtils.isNotBlank(value), null);

            Document doc = new Document();
            doc.add(new StringField("key", key, Field.Store.YES));
            doc.add(new StringField("configFile", fileName.getConfigFileName(), Field.Store.YES));

            // build content field: key with dots as spaces + original key + value (if not sensitive)
            String searchableKey = key.replace('.', ' ');
            StringBuilder contentForSearch = new StringBuilder();
            contentForSearch.append(searchableKey).append(" ").append(key);

            if (isSensitive) {
              doc.add(new StoredField("value", MASKED_VALUE));
              doc.add(new StringField("sensitive", "true", Field.Store.YES));
            } else if (StringUtils.isNotBlank(value)) {
              contentForSearch.append(" ").append(value);
              doc.add(new StoredField("value", value));
            } else {
              doc.add(new StoredField("value", ""));
            }

            doc.add(new TextField("content", contentForSearch.toString(), Field.Store.NO));

            // add metadata: configuredIn
            String configuredIn = determineConfiguredIn(config, key);
            if (StringUtils.isNotBlank(configuredIn)) {
              doc.add(new StoredField("configuredIn", configuredIn));
            }

            // add metadata: elScript
            String elScript = getElScript(config, key);
            if (StringUtils.isNotBlank(elScript)) {
              doc.add(new StoredField("elScript", elScript));
            }

            // add metadata: defaultValue
            String defaultValue = getDefaultValue(config, key, metadata);
            if (StringUtils.isNotBlank(defaultValue)) {
              if (isSensitive) {
                doc.add(new StoredField("defaultValue", MASKED_VALUE));
              } else {
                doc.add(new StoredField("defaultValue", defaultValue));
              }
            }

            if (metadata != null) {
              // add metadata: comment
              if (StringUtils.isNotBlank(metadata.getComment())) {
                doc.add(new StoredField("comment", metadata.getComment()));
              }

              // add metadata: valueType
              if (metadata.getValueType() != null) {
                doc.add(new StoredField("valueType", metadata.getValueType().getStringForUi()));
              }

              // add metadata: required
              if (metadata.isRequired()) {
                doc.add(new StringField("required", "true", Field.Store.YES));
              }
            }

            writer.addDocument(doc);
            totalConfigs++;
          }
        } catch (Exception e) {
          LOG.warn("Error reading config file " + fileName.getConfigFileName()
              + " for Lucene indexing: " + e.getMessage(), e);
        }
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
          LOG.debug("Error closing old config index reader", e);
        }
      }
      if (oldDirectory != null) {
        try {
          oldDirectory.close();
        } catch (IOException e) {
          LOG.debug("Error closing old config index directory", e);
        }
      }

      LOG.info("Config search index built: " + totalConfigs + " properties from "
          + ConfigFileName.values().length + " config files");

    } catch (Exception e) {
      LOG.error("Error building config search index", e);
      try {
        newDirectory.close();
      } catch (IOException e2) {
        // ignore
      }
    }
  }

}
