/*--
$Id: BaseSourceAdapter.java,v 1.8 2009-10-30 20:41:41 mchyzer Exp $
$Date: 2009-10-30 20:41:41 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;

/**
 * <pre>
 * Base Source adapter.
 * 
 * Developers note: you should implement the getSubject and getSubjectByIdentifier
 * methods (that take boolean) since the base class method will soon become abstract, and the
 * method overloads which are deprecated and dont take booleans will go away.
 * 
 * </pre>
 */
public abstract class BaseSourceAdapter implements Source {

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers) {
    Map<String, Subject> result = new LinkedHashMap<String, Subject>();
    
    Subject subject = null;
    for (String theIdentifier : identifiers) {
      try {
        subject = getSubjectByIdentifier(theIdentifier, true);
        result.put(theIdentifier, subject);
      } catch (SubjectNotFoundException snfe) {
        //ignore
      } catch (SubjectNotUniqueException snue) {
        //ignore
      }
    }
    return result;
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIds(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids) {
    Map<String, Subject> result = new LinkedHashMap<String, Subject>();
    
    Subject subject = null;
    for (String theId : ids) {
      try {
        subject = getSubject(theId, true);
        result.put(theId, subject);
      } catch (SubjectNotFoundException snfe) {
        //ignore
      } catch (SubjectNotUniqueException snue) {
        //ignore
      }
    }
    return result;
    
  }
  
  /**
   * find by id or identifier
   * @param idOrIdentifier
   * @param exceptionIfNull if SubjectNotFoundException or null
   * @return the subject
   * @throws SubjectNotFoundException 
   * @throws SubjectNotUniqueException 
   */
  public Subject getSubjectByIdOrIdentifier(String idOrIdentifier, boolean exceptionIfNull) 
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Subject subject = null;

    //try by id first
    subject = this.getSubject(idOrIdentifier, false);

    //try by identifier if not by id
    if (subject == null) {
      subject = this.getSubjectByIdentifier(idOrIdentifier, false);
    }

    //if null at this point, and exception, then throw it
    if (subject == null && exceptionIfNull) {
      throw new SubjectNotFoundException("Cant find subject by id or identifier: '" + idOrIdentifier + "'"); 
    }

    return subject;
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdsOrIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(
      Collection<String> idsOrIdentifiers) {
    Map<String, Subject> result = new LinkedHashMap<String, Subject>();
    
    Subject subject = null;
    for (String theIdOrIdentifier : idsOrIdentifiers) {
      try {
        subject = getSubjectByIdOrIdentifier(theIdOrIdentifier, true);
        result.put(theIdOrIdentifier, subject);
      } catch (SubjectNotFoundException snfe) {
        //ignore
      } catch (SubjectNotUniqueException snue) {
        //ignore
      }
    }
    return result;
  }

  /**
   * 
   */
  private static Log log = LogFactory.getLog(BaseSourceAdapter.class);

  /** */
  protected String id = null;

  /** */
  protected String name = null;

  /** */
  protected Set<SubjectType> types = new HashSet<SubjectType>();

  /** */
  protected SubjectType type = null;

  /** */
  protected Properties params = new Properties();

  /** The three different kinds of searches:  */
  protected HashMap<String, Search> searches = new HashMap<String, Search>();

  /** */
  protected Set<String> attributes = new HashSet<String>();

  /** internal attributes. */
  protected Set<String> internalAttributes = new HashSet<String>();
  
  protected Map<Integer, String> sortAttributes = null;
  
  protected Map<Integer, String> searchAttributes = null;
  /**
   * Default constructor.
   */
  public BaseSourceAdapter() {
  }

  /**
   * Allocates adapter with ID and name.
   * @param id1
   * @param name1
   */
  public BaseSourceAdapter(String id1, String name1) {
    this.id = id1;
    this.name = name1;
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
  public void setId(String id1) {
    this.id = id1;
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
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * {@inheritDoc}
   */
  public Set<SubjectType> getSubjectTypes() {
    return this.types;
  }

  /**
   * 
   * @return subject type
   */
  public SubjectType getSubjectType() {
    return this.type;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String)
   * @deprecated use the overload instead
   */
  @Deprecated
  public abstract Subject getSubject(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException;

  /**
   * 
   * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String, boolean)
   */
  public Subject getSubject(String id1, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    //NOTE this implementation is here temporarily for backwards compatibility... it will go away soon
    //and this method will become abstract
    try {
      return this.getSubject(id1);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }

  }

  /**
   * 
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String)
   * @deprecated use the overload instead
   */
  @Deprecated
  public abstract Subject getSubjectByIdentifier(String id1)
      throws SubjectNotFoundException, SubjectNotUniqueException;

  /**
   * note, you should implement this method since this implementation will become abstract at some point
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String, boolean)
   */
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    //NOTE this implementation is here temporarily for backwards compatibility... it will go away soon
    //and this method will become abstract
    try {
      return this.getSubjectByIdentifier(id1);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public abstract Set<Subject> search(String searchValue);

  /**
   * {@inheritDoc}
   */
  public abstract void init() throws SourceUnavailableException;

  /**
   * Compares this source against the specified source.
   * Returns true if the IDs of both sources are equal.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof BaseSourceAdapter) {
      return this.getId().equals(((BaseSourceAdapter) other).getId());
    }
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return "BaseSourceAdapter".hashCode() + this.getId().hashCode();
  }

  /**
   * (non-javadoc)
   * @param type1
   */
  public void addSubjectType(String type1) {
    this.types.add(SubjectTypeEnum.valueOf(type1));
    this.type = SubjectTypeEnum.valueOf(type1);
  }

  /**
   * (non-javadoc)
   * @param name1
   * @param value
   */
  public void addInitParam(String name1, String value) {
    this.params.setProperty(name1, value);
  }
  
  /**
   * @param name1
   */
  public void removeInitParam(String name1) {
    this.params.remove(name1);
  }

  /**
   * (non-javadoc)
   * @param name1
   * @return param
   */
  public String getInitParam(String name1) {
    return this.params.getProperty(name1);
  }

  /**
   * (non-javadoc)
   * @return params
   */
  public Properties getInitParams() {
    return this.params;
  }

  /**
   * 
   * @param attributeName
   */
  public void addAttribute(String attributeName) {
    this.attributes.add(attributeName);
  }
  
  /**
   * @param attributeName
   */
  public void addInternalAttribute(String attributeName) {
    this.internalAttributes.add(attributeName);
  }

  /**
   * 
   * @return set
   */
  protected Set getAttributes() {
    return this.attributes;
  }
  
  /**
   * @return set
   */
  public Set<String> getInternalAttributes() {
    return this.internalAttributes;
  }

  /**
   * 
   * @param searches1
   */
  protected void setSearches(HashMap<String, Search> searches1) {
    this.searches = searches1;
  }

  /**
   * 
   * @return map
   */
  protected HashMap<String, Search> getSearches() {
    return this.searches;
  }

  /**
   * 
   * @param searchType
   * @return search
   */
  protected Search getSearch(String searchType) {
    HashMap searches1 = getSearches();
    return (Search) searches1.get(searchType);
  }

  /**
   * 
   * @param search
   */
  public void loadSearch(Search search) {
    log.debug("Loading search: " + search.getSearchType());
    this.searches.put(search.getSearchType(), search);
  }
  
  /**
   * @param sortAttributes
   */
  public void setSortAttributes(Map<Integer, String> sortAttributes) {
    this.sortAttributes = sortAttributes;
  }
  
  /**
   * @param searchAttributes
   */
  public void setSearchAttributes(Map<Integer, String> searchAttributes) {
    this.searchAttributes = searchAttributes;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSortAttributes()
   */
  public Map<Integer, String> getSortAttributes() {
    
    if (this.sortAttributes == null) {
      this.sortAttributes = new LinkedHashMap<Integer, String>();
      
      for (int i = 0; i < 5; i++) {
        String value = getInitParam("sortAttribute" + i);
        if (value != null) {
          this.sortAttributes.put(i, value);
        }        
      }
    }
    
    return this.sortAttributes;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSearchAttributes()
   */
  public Map<Integer, String> getSearchAttributes() {
    
    if (this.searchAttributes == null) {
      this.searchAttributes = new LinkedHashMap<Integer, String>();
      
      for (int i = 0; i < 5; i++) {
        String value = getInitParam("searchAttribute" + i);
        if (value != null) {
          this.searchAttributes.put(i, value);
        }        
      }
    }
    
    return this.searchAttributes;
  }
}
