/*******************************************************************************
 * Copyright 2012 Internet2
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
/*--
$Id: SourceManager.java,v 1.12 2009-08-13 06:26:30 mchyzer Exp $
$Date: 2009-08-13 06:26:30 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */

package edu.internet2.middleware.subject.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;

/**
 * Factory to load and get Sources.  Sources are defined
 * in a configuration file named, sources.xml, and must
 * be placed in the classpath.<p>
 *
 */
public class SourceManager {

  /**
   * print out the config for the subject API
   * @return the config
   */
  public String printConfig() {
    try {
      StringBuilder result = new StringBuilder();

      File sourcesXmlFile = SubjectUtils.fileFromResourceName("sources.xml");
      String sourcesXmlFileLocation = sourcesXmlFile == null ? " [cant find sources.xml]"
          : SubjectUtils.fileCanonicalPath(sourcesXmlFile);

      result.append("sources.xml read from:        " + sourcesXmlFileLocation + "\n");

      //at this point, we have a sources.xml...  now check it out
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
  private static final String CONFIG_FILE = "/sources.xml";

  /** */
  private static Log log = LogFactory.getLog(SourceManager.class);

  /** */
  private static SourceManager manager;

  /** */
  private Map<SubjectType, Set<Source>> source2TypeMap = new HashMap<SubjectType, Set<Source>>();

  /** */
  Map<String, Source> sourceMap = new HashMap<String, Source>();

  /**
   * Default constructor.
   */
  private SourceManager() {
    init();
  }

  /**
   * Returns the singleton instance of SourceManager.
   * @return source manager
   *
   */
  public static SourceManager getInstance() {
    if (manager == null) {
      manager = new SourceManager();
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
    } catch (Exception ex) {
      log.error("Error initializing SourceManager: " + ex.getMessage(), ex);
      throw new RuntimeException("Error initializing SourceManager", ex);
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
      
  }

  /**
   * Parses sources.xml config file using org.apache.commons.digester.Digester.
   * @throws IOException 
   * @throws SAXException 
   */
  private void parseConfig() throws IOException, SAXException {
    log.debug("Instantiating new Digester.");
    Digester digester = new Digester();
    digester.push(this);
    digester.addObjectCreate("sources/source",
        "edu.internet2.middleware.subject.BaseSourceAdapter", "adapterClass");
    digester.addCallMethod("sources/source/id", "setId", 0);
    digester.addCallMethod("sources/source/name", "setName", 0);
    digester.addCallMethod("sources/source/type", "addSubjectType", 0);
    digester.addCallMethod("sources/source/init-param", "addInitParam", 2);
    digester.addCallParam("sources/source/init-param/param-name", 0);
    digester.addCallParam("sources/source/init-param/param-value", 1);
    digester.addCallMethod("sources/source/attribute", "addAttribute", 0);
    digester.addCallMethod("sources/source/internal-attribute", "addInternalAttribute", 0);

    digester.addObjectCreate("sources/source/search",
        "edu.internet2.middleware.subject.provider.Search");
    digester.addCallMethod("sources/source/search/searchType", "setSearchType", 0);
    digester.addCallMethod("sources/source/search/param", "addParam", 2);
    digester.addCallParam("sources/source/search/param/param-name", 0);
    digester.addCallParam("sources/source/search/param/param-value", 1);
    digester.addSetNext("sources/source/search", "loadSearch");

    digester.addSetNext("sources/source", "loadSource");
    
    
    InputStream is = sourcesXmlConfig();
    log.debug("Parsing config input stream: " + is);
    try {
      digester.parse(is);
    } catch (Exception e) {
      String sourcesLocation = SubjectConfig.retrieveConfig().propertyValueString("subject.sources.xml.location");
      throw new RuntimeException("Problem reading sources xml file: " + sourcesLocation, e );
    }
    is.close();
  }

  /**
   * input stream
   * @return the input stream of the config file
   */
  public static InputStream sourcesXmlConfig() {
    String sourcesXmlLocation = SubjectConfig.retrieveConfig().propertyValueStringRequired("subject.sources.xml.location");
    return GrouperClientUtils.fileOrClasspathInputstream(sourcesXmlLocation, "In the subject.properties, the entry for subject.sources.xml.location");
  }
  
  /**
   * Validates sources.xml config file.
   * @param args 
   */
  public static void main(String[] args) {
    
    try {
      SourceManager mgr = SourceManager.getInstance();
      for (Iterator iter = mgr.getSources().iterator(); iter.hasNext();) {
        BaseSourceAdapter source = (BaseSourceAdapter) iter.next();
        log.debug("Source init params: " + "id = " + source.getId() + ", params = "
            + source.getInitParams());
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
}
