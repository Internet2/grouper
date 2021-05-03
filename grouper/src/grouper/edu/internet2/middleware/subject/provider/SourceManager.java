/**
 * Copyright 2014 Internet2
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
/*--
$Id: SourceManager.java,v 1.12 2009-08-13 06:26:30 mchyzer Exp $
$Date: 2009-08-13 06:26:30 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */

package edu.internet2.middleware.subject.provider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.internet2.middleware.grouper.cache.GrouperCacheDatabase;
import edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseClear;
import edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseClearInput;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAutoSourceAdapter;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;

/**
 * Factory to load and get Sources.  Sources are defined
 * in a configuration file named, subject.properties, and must
 * be placed in the classpath.<p>
 *
 */
public class SourceManager {

  //  <init-param>
  //  <param-name>statusLabel<param-name>
  //  <param-value>status</param-value>
  //</init-param>
  //<!-- available statuses from screen (if not specified, any will be allowed). comma separated list - - >
  //<init-param>
  //  <param-name>statusesFromUser<param-name>
  //  <param-value>Active, Inactive, Pending, All</param-value>
  //</init-param>
  //<!-- all label from the user - - >
  //<init-param>
  //  <param-name>statusAllFromUser<param-name>
  //  <param-value>All</param-value>
  //</init-param>
  
  /**
   * bean to hold the status stuff across all sources
   */
  public static class SourceManagerStatusBean {

    /**
     * map of source id to status
     * @return the source id
     */
    public Map<String, SubjectStatusConfig> getSourceIdToStatusConfigs() {
      return this.sourceIdToStatusConfigs;
    }

    /**
     * map of source id to status
     */
    private Map<String, SubjectStatusConfig> sourceIdToStatusConfigs = new HashMap<String, SubjectStatusConfig>();
    
    /**
     * search string from user which represents the status.  e.g. status=active
     */
    private Set<String> statusLabels = new HashSet<String>();
    
    /**
     * available statuses from screen (if not specified, any will be allowed). comma separated list
     */
    private Set<String> statusesFromUser = new HashSet<String>();
    
    /**
     * all label from the user
     */
    private Set<String> statusAllFromUser = new HashSet<String>();

    /**
     * search string from user which represents the status.  e.g. status=active
     * @return search string
     */
    public Set<String> getStatusLabels() {
      return this.statusLabels;
    }

    /**
     * available statuses from screen (if not specified, any will be allowed). comma separated list
     * @return available statuses
     */
    public Set<String> getStatusesFromUser() {
      return this.statusesFromUser;
    }

    /**
     * all label from the user
     * @return status from user
     */
    public Set<String> getStatusAllFromUser() {
      return this.statusAllFromUser;
    }
    
    /**
     * loop through config beans from subject.properties 
     */
    public void processConfigBeans() {
      
      for (SubjectStatusConfig subjectStatusConfig : this.sourceIdToStatusConfigs.values()) {

        {
          String statusLabel = subjectStatusConfig.getStatusLabel();
          if (!StringUtils.isBlank(statusLabel)) {
            this.statusLabels.add(statusLabel);
          }
        }
        
        {
          Set<String> statusesFromUser = subjectStatusConfig.getStatusesFromUser();
          this.statusesFromUser.addAll(statusesFromUser);
        }

        {
          String statusAllFromUser = subjectStatusConfig.getStatusAllFromUser();
          if (!StringUtils.isBlank(statusAllFromUser)) {
            this.statusAllFromUser.add(statusAllFromUser);
          }
        }
      }
    }
  }
  
  public static void clearAllSources() {
    manager = null;
  }
  
  /**
   * search string from user which represents the status.  e.g. status=active
   */
  private static ExpirableCache<Boolean, SourceManagerStatusBean> sourceManagerStatusBeanCache = 
      new ExpirableCache<Boolean, SourceManagerStatusBean>();

  /**
   * get status information across all sources
   * @return the status rollup bean
   */
  public SourceManagerStatusBean getSourceManagerStatusBean() {
    SourceManagerStatusBean sourceManagerStatusBean = sourceManagerStatusBeanCache.get(true);
    if (sourceManagerStatusBean == null) {
      synchronized (sourceManagerStatusBeanCache) {
        sourceManagerStatusBean = sourceManagerStatusBeanCache.get(true);
        if (sourceManagerStatusBean == null) {
          
          sourceManagerStatusBean = new SourceManagerStatusBean();
          
          for (Source source : getInstance().getSources()) {
            
            SubjectStatusConfig subjectStatusConfig = new SubjectStatusConfig(source);
            sourceManagerStatusBean.getSourceIdToStatusConfigs().put(source.getId(), subjectStatusConfig);
            
          }
          
          sourceManagerStatusBean.processConfigBeans();
          
          sourceManagerStatusBeanCache.put(Boolean.TRUE, sourceManagerStatusBean);
          
        }
      }
    }
    return sourceManagerStatusBean;
  }
  
  /**
   * print out the config for the subject API
   * @return the config
   */
  public String printConfig() {
    try {
      StringBuilder result = new StringBuilder();

      File subjectPropertiesFile = SubjectUtils.fileFromResourceName("subject.properties");
      String subjectPropertiesFileLocation = subjectPropertiesFile == null ? " [cant find subject.properties]"
          : SubjectUtils.fileCanonicalPath(subjectPropertiesFile);
      result.append("subject.properties read from: " + subjectPropertiesFileLocation + "\n");

      result.append("sources configured in:        subject.properties\n");
      File sourcesXmlFile = SubjectUtils.fileFromResourceName("sources.xml");
      if (sourcesXmlFile != null && sourcesXmlFile.exists() && sourcesXmlFile.isFile()) {
        String sourcesError = "NON-FATAL ERROR:              subject sources are read from subject.properties but you "
            + "still have a sources.xml on the classpath which is confusing, please backup and remove this file: " + sourcesXmlFile.getAbsolutePath();
        result.append(sourcesError + "\n");
        log.error(sourcesError);
      }
      
      //at this point, we have a subject.properties...  now check it out
      Collection<Source> sources = SourceManager.getInstance().getSources();
      for (Source source : sources) {
        result.append(source.printConfig()).append("\n");
      }
      //dont end in newline
      if (result.toString().endsWith("\n")) {
        result.deleteCharAt(result.length() - 1);
      }
      return result.toString();
    } catch (Exception e) {
      log.error("Cant print subject API configs", e);
    }
    return "Cant print subject API configs";

  }

  /** */
  private static Log log = LogFactory.getLog(SourceManager.class);

  /** */
  private static SourceManager manager;

  /** */
  private Map<SubjectType, Set<Source>> source2TypeMap = new HashMap<SubjectType, Set<Source>>();

  /** */
  Map<String, Source> sourceMap = new HashMap<String, Source>();
  
  private static Set<String> registeredDatabaseCacheNames = Collections.synchronizedSet(new HashSet<String>());

  private static boolean grouperCacheClearDatabaseInitted;
  /**
   * Default constructor.
   */
  private SourceManager() {
    init();
    
    if (!grouperCacheClearDatabaseInitted) {
      String cacheName = "edu.internet2.middleware.subject.provider.SourceManager.reloadSource";
      GrouperCacheDatabase.customRegisterDatabaseClearable(cacheName, new GrouperCacheDatabaseClear() {         
              
        @Override
        public void clear(GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput) {
          String cacheName = grouperCacheDatabaseClearInput.getCacheName();
          String sourceId = StringUtils.substringAfterLast(cacheName, "____");
          GrouperConfigHibernate.clearConfigsInMemory();
          SourceManager.getInstance().reloadSource(sourceId);
        }
      });
      grouperCacheClearDatabaseInitted = true;
    }
    
    
  }

  /**
   * Returns the singleton instance of SourceManager.
   * @return source manager
   *
   */
  public static SourceManager getInstance() {
    if (manager == null) {
      synchronized(SourceManager.class) {
        if (manager == null) {
          manager = new SourceManager();
        }
      }
    }
    return manager;
  }

  /**
   * Gets Source for the argument source ID.
   * @param sourceId
   * @return Source
   * @throws SourceUnavailableException
   */
  public Source getSource(String sourceId) throws SourceUnavailableException {
    Source source = this.sourceMap.get(sourceId);
    if (source == null) {
      
      StringBuilder allSources = new StringBuilder();
      int i=0;
      for (String theSourceId : this.sourceMap.keySet()) {
        allSources.append(theSourceId);
        if (i != this.sourceMap.size() - 1) {
          allSources.append(", ");
        }
        i++;
      }
      
      throw new SourceUnavailableException("Source not found: '" + sourceId + "', available sources are: " + allSources);
    }
    return source;
  }

  /**
   * Returns a Collection of Sources.
   * @return Collection
   */
  public Collection<Source> getSources() {
    return new LinkedHashSet<Source>(this.sourceMap.values());
  }

  /**
   * Returns a Collection of Sources that
   * supports the argument SubjectType.
   * @param type 
   * @return Collection
   */
  public Collection<Source> getSources(SubjectType type) {
    if (this.source2TypeMap.containsKey(type)) {
      return this.source2TypeMap.get(type);
    }
    return new HashSet<Source>();
  }

  /**
   * Initialize this SourceManager.
   * @throws RuntimeException
   */
  private void init() throws RuntimeException {
    try {
      parseConfig();
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("externalSubjects.autoCreateSource", true)) {
        
        this.loadSource(ExternalSubjectAutoSourceAdapter.instance());
        
      }
      

    } catch (Exception ex) {
      log.error("Error initializing SourceManager: " + ex.getMessage(), ex);
      throw new RuntimeException("Error initializing SourceManager", ex);
    }
  }
  
  public synchronized void reloadSource(String sourceId) {
    Source source = SubjectConfig.retrieveConfig().reloadSourceConfigs(sourceId);
    if (source != null) {
      loadSource(source);
    } else {
      sourceMap.remove(sourceId);
    }
  }

  /**
   * (non-javadoc)
   * @param source
   */
  public void loadSource(Source source) {
    log.debug("Loading source: " + source.getId());
    
    //put in map before initting
    this.sourceMap.put(source.getId(), source);
    
    String cacheName = "edu.internet2.middleware.subject.provider.SourceManager.reloadSource____" + source.getId();
    if (!registeredDatabaseCacheNames.contains(cacheName)) {
      registeredDatabaseCacheNames.add(cacheName);
      GrouperCacheDatabase.customRegisterDatabaseClearable(cacheName, new GrouperCacheDatabaseClear() {         
             
             private String sourceId = source.getId();
             
             @Override
             public void clear(GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput) {
               GrouperConfigHibernate.clearConfigsInMemory();
               SourceManager.getInstance().reloadSource(sourceId);
             }
          });
    }

    for (Iterator it = source.getSubjectTypes().iterator(); it.hasNext();) {
      SubjectType type = (SubjectType) it.next();
      Set<Source> sources = this.source2TypeMap.get(type);
      if (sources == null) {
        sources = new HashSet<Source>();
        this.source2TypeMap.put(type, sources);
      }
      sources.add(source);
    }

    //do this last in case it throws exceptions...
    source.init();
      
    source.getSearchAttributes();
    source.getSortAttributes();
    source.getSubjectIdentifierAttributes();
  }

  /**
   * Parses subject.properties config file using org.apache.commons.digester.Digester.
   * @throws IOException 
   * @throws SAXException 
   */
  private void parseConfig() throws IOException, SAXException {
    for (Source source : SubjectConfig.retrieveConfig().retrieveSourceConfigs().values()) {
      loadSource(source);
    }
  }

  /**
   * 
   * @return true if using subject.properties, false if subject.properties
   */
  public static boolean usingSubjectProperties() {
    return true;
  }
  
  
  /**
   * Validates subject.properties config file.
   * @param args 
   */
  public static void main(String[] args) {
    
    try {
      SourceManager mgr = SourceManager.getInstance();
      for (Iterator iter = mgr.getSources().iterator(); iter.hasNext();) {
        BaseSourceAdapter source = (BaseSourceAdapter) iter.next();
        log.debug("Source init params: " + "id = " + source.getId() + ", params = "
            + source.initParams());
        source.init();
        if (source.getId().equals("example")) {

          Subject subject = source.getSubject("70061854", true);
          //Subject subject = source.getSubject("xxxxx");
          log.debug("getSubject id: " + subject.getId() + " name: " + subject.getName()
              + " description:" + subject.getDescription());
          subject = source.getSubjectByIdentifier("esluss", true);
          log.debug("getSubjectByIdentifier id: " + subject.getId() + " name: "
              + subject.getName() + " description:" + subject.getDescription());
          log.debug("Starting barton search");
          Set subjectSet = source.search("barton");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }

        } else if (source.getId().equals("jdbc")) {
          Subject subject = source.getSubject("37413", true);

          //Subject subject = source.getSubject("xxxxx");
          log.debug("getSubject id: " + subject.getId() + " name: " + subject.getName()
              + " description:" + subject.getDescription());
          subject = source.getSubjectByIdentifier("abean", true);
          log.debug("getSubjectByIdentifier id: " + subject.getId() + " name: "
              + subject.getName() + " description:" + subject.getDescription());
          log.debug("Starting barton search");
          Set subjectSet = source.search("smith");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }
          subjectSet = source.search("bean");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }
          subjectSet = source.search("smith");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }
          subjectSet = source.search("bean");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }
          subjectSet = source.search("smith");
          log.debug("num elements found: " + subjectSet.size());
          for (Iterator it = subjectSet.iterator(); it.hasNext();) {
            subject = (Subject) it.next();
            log.debug("id: " + subject.getId() + " name: " + subject.getName()
                + " description:" + subject.getDescription());
            Map attrs = subject.getAttributes();
            for (Iterator it2 = attrs.keySet().iterator(); it2.hasNext(); log
                .debug(it2.next()))
              ;
          }

        }
      }
    } catch (Exception ex) {
      log.error("Exception occurred: " + ex.getMessage(), ex);
    }
  }
  
  /**
   * remove source for testing
   * @param sourceId
   */
  public void internal_removeSource(String sourceId) {
    sourceMap.remove(sourceId);
  }
}
