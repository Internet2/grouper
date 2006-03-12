/*--
$Id: SourceManager.java,v 1.3 2006-03-12 23:10:50 tbarton Exp $
$Date: 2006-03-12 23:10:50 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.io.InputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.xml.sax.SAXException;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;


/**
 * Factory to load and get Sources.  Sources are defined
 * in a configuration file named, sources.xml, and must
 * be placed in the classpath.<p>
 *
 */
public class SourceManager {
    
    private static final String CONFIG_FILE = "/sources.xml";
    
    private static Log log = LogFactory.getLog(SourceManager.class);
    
    private static SourceManager manager;
    private Map source2TypeMap = new HashMap();
    private Map sourceMap       = new HashMap();
    
    /**
     * Default constructor.
     * @throws Exception
     */
    private SourceManager()
    throws Exception {
        init();
    }
    
    /**
     * Returns the singleton instance of SourceManager.
     *
     */
    public static SourceManager getInstance()
    throws Exception {
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
    public Source getSource(String sourceId)
    throws SourceUnavailableException {
        Source source = (Source)this.sourceMap.get(sourceId);
        if (source == null) {
            throw new SourceUnavailableException("Source not found.");
        }
        return source;
    }
    
    /**
     * Returns a Collection of Sources.
     * @return Collection
     */
    public Collection getSources() {
        return this.sourceMap.values();
    }
    
    /**
     * Returns a Collection of Sources that
     * supports the argument SubjectType.
     * @return Collection
     */
    public Collection getSources(SubjectType type) {
        return (Collection)this.source2TypeMap.get(type);
    }
    
    /**
     * Initialize this SourceManager.
     * @throws Exception
     */
    private void init()
    throws Exception {
        try {
            parseConfig();
        }  catch (Exception ex) {
            log.error(
                    "Error initializing SourceManager: " + ex.getMessage(), ex);
            throw new Exception(
                    "Error initializing SourceManager", ex);
        }
    }
    
    /**
     * (non-javadoc)
     * @param source
     */
    public void loadSource(BaseSourceAdapter source) {
        log.debug("Loading source: " + source.getId());
        try {
            source.init();
            this.sourceMap.put(source.getId(), source);
            for (Iterator it = source.getSubjectTypes().iterator(); it.hasNext(); ) {
                SubjectType type = (SubjectType)it.next();
                Set sources = (Set)this.source2TypeMap.get(type);
                if (sources == null) {
                    sources = new HashSet();
                    this.source2TypeMap.put(type, sources);
                }
                sources.add(source);
            }
        } catch (SourceUnavailableException ex) {
            log.error("Unable to init Source: " + source.getId(), ex);
        }
    }
    
    
    /**
     * Parses sources.xml config file using org.apache.commons.digester.Digester.
     */
    private void parseConfig()
    throws IOException, SAXException {
        log.debug("Instantiating new Digester.");
        Digester digester = new Digester();
        digester.push(this);
        digester.addObjectCreate("sources/source",
                "edu.internet2.middleware.subject.BaseSourceAdapter",
                "adapterClass");
        digester.addCallMethod("sources/source/id",    "setId", 0);
        digester.addCallMethod("sources/source/name",  "setName", 0);
        digester.addCallMethod("sources/source/type",  "addSubjectType", 0);
        digester.addCallMethod("sources/source/init-param","addInitParam", 2);
        digester.addCallParam("sources/source/init-param/param-name", 0);
        digester.addCallParam("sources/source/init-param/param-value", 1);
        
        digester.addObjectCreate("sources/source/search","edu.internet2.middleware.subject.provider.Search" );
        digester.addCallMethod("sources/source/search/searchType","setSearchType",0);
        digester.addCallMethod("sources/source/search/param", "addParam",2);
        digester.addCallParam("sources/source/search/param/param-name",0);
        digester.addCallParam("sources/source/search/param/param-value",1);
        digester.addSetNext("sources/source/search","loadSearch");
        
        digester.addSetNext("sources/source", "loadSource");
        InputStream is = this.getClass().getResourceAsStream(CONFIG_FILE);
        log.debug("Parsing config input stream: " + is);
        digester.parse(is);
        is.close();
    }
    
    /**
     * Validates sources.xml config file.
     */
    public static void main(String[] args) {
        try {
            SourceManager mgr = SourceManager.getInstance();
            for (Iterator iter = mgr.getSources().iterator(); iter.hasNext();) {
                BaseSourceAdapter source = (BaseSourceAdapter)iter.next();
                log.debug("Source init params: "
                        + "id = " + source.getId()
                        + ", params = " + source.getInitParams());
                source.init();
                if (source.getId().equals("example")) {
                    
                    Subject subject = source.getSubject("70061854");
                    //Subject subject = source.getSubject("xxxxx");
                    log.debug("getSubject id: " + subject.getId() + " name: " + subject.getName() + " description:" + subject.getDescription());
                    subject = source.getSubjectByIdentifier("esluss");
                    log.debug("getSubjectByIdentifier id: " + subject.getId() + " name: " + subject.getName() + " description:" + subject.getDescription());
                    log.debug("Starting barton search");
                    Set subjectSet = source.search("barton");
                    log.debug("num elements found: " + subjectSet.size());
                    for (Iterator it = subjectSet.iterator(); it.hasNext();) {
                        subject = (Subject) it.next();
                        log.debug("id: " + subject.getId() + " name: " + subject.getName() + " description:" + subject.getDescription());
                        Map attrs = subject.getAttributes();
                    }
                    
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred: " + ex.getMessage(), ex);
        }
    }
}
