/*--
$Id: BaseSourceAdapter.java,v 1.3 2006-03-12 23:10:50 tbarton Exp $
$Date: 2006-03-12 23:10:50 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;


/**
 * Base Source adapter.
 */
public abstract class BaseSourceAdapter
        implements Source {
    
    private static Log log = LogFactory.getLog(BaseSourceAdapter.class);
    
    protected String id = null;
    protected String name = null;
    protected Set types = new HashSet();
    protected SubjectType type = null;
    protected Properties params = new Properties();
    //The three different kinds of searches: 
    protected HashMap searches = new HashMap();
    
    /**
     * Default constructor.
     */
    public BaseSourceAdapter() {}
    
    /**
     * Allocates adapter with ID and name.
     * @param id
     * @param name
     */
    public BaseSourceAdapter(String id, String name){
        this.id = id;
        this.name = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getSubjectTypes() {
        return types;
    }
    
    public SubjectType getSubjectType() {
        return type;
    }
    
    /**
     * {@inheritDoc}
     */
    public abstract Subject getSubject(String id)
    throws SubjectNotFoundException;
    
    /**
     * {@inheritDoc}
     */
    public abstract Subject getSubjectByIdentifier(String id)
    throws SubjectNotFoundException;
    
    /**
     * {@inheritDoc}
     */
    public abstract Set search(String searchValue);
    
    /**
     * {@inheritDoc}
     */
    public abstract void init()
    throws SourceUnavailableException;
    
    /**
     * Compares this source against the specified source.
     * Returns true if the IDs of both sources are equal.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof BaseSourceAdapter) {
            return this.getId().equals(
                    ((BaseSourceAdapter)other).getId());
        }
        return false;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return "BaseSourceAdapter".hashCode() + this.getId().hashCode();
    }
    
    /**
     * (non-javadoc)
     * @param type
     */
    public void addSubjectType(String type) {
        this.types.add(SubjectTypeEnum.valueOf(type));
        this.type = SubjectTypeEnum.valueOf(type);
    }
    
    /**
     * (non-javadoc)
     * @param name
     * @param value
     */
    public void addInitParam(String name, String value) {
        this.params.setProperty(name, value);
    }
    
    /**
     * (non-javadoc)
     * @param name
     * @return param
     */
    protected String getInitParam(String name) {
        return this.params.getProperty(name);
    }
    
    /**
     * (non-javadoc)
     * @return params
     */
    protected Properties getInitParams() {
        return this.params;
        
    }
    
    
    protected void setSearches(HashMap searches) {
        this.searches = searches;
    }
    
    protected HashMap getSearches() {
        return this.searches;
    }
    
    protected Search getSearch(String searchType) {
        HashMap searches = getSearches();
        return (Search) searches.get(searchType);
    }
    
    public void loadSearch(Search search) {
        log.debug("Loading search: " + search.getSearchType());
        this.searches.put(search.getSearchType(), search );
        
    }
}
