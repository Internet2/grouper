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
$Id: BaseSourceAdapter.java,v 1.8 2009-10-30 20:41:41 mchyzer Exp $
$Date: 2009-10-30 20:41:41 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2_5;
import edu.internet2.middleware.grouper.subj.GrouperLdapSourceAdapter2_5;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveSetImpl;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager.SourceManagerStatusBean;

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
  
  private Set<String> sourceAttributesToLowerCase = null;
  
  protected String nameAttributeName;
  protected String descriptionAttributeName;
  
  
  protected Subject createSubject(Map<String, Object> sourceAttributesToValues, String subjectID) {
    
    Map<String, Object> translationMap = new CaseInsensitiveMap();
    
    for (String sourceAttribute: sourceAttributesToValues.keySet()) {
      translationMap.put("source_attribute__"+sourceAttribute, sourceAttributesToValues.get(sourceAttribute));
    }
    
    Map<String, Object> subjectAttributesToValues = new CaseInsensitiveMap();
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    
    if (StringUtils.isNotBlank(numberOfAttributes)) {
      
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
        
        String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
        
        boolean isSourceAttribute = StringUtils.equals(translationType, "sourceAttribute");
        boolean isSourceAttributeSameAsSubjectAttribute = StringUtils.equals(translationType, "sourceAttributeSameAsSubjectAttribute");
        
        if (isSourceAttributeSameAsSubjectAttribute) {
          Object value = sourceAttributesToValues.get(subjectAttributeName);
          subjectAttributesToValues.put(subjectAttributeName, value);
          translationMap.put("subject_attribute__"+subjectAttributeName.toLowerCase(), value);
        } else if (isSourceAttribute) {
          
          String sourceAttribute = StringUtils.trimToEmpty(SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute")).toLowerCase();

          Object value = sourceAttributesToValues.get(sourceAttribute);
          subjectAttributesToValues.put(subjectAttributeName, value);
          translationMap.put("subject_attribute__"+subjectAttributeName.toLowerCase(), value);
        }
      }
      
    }
          
     
    SubjectImpl subject = new SubjectImpl(subjectID, null, null, this.getSubjectType().getName(), this.getId(), nameAttributeName, descriptionAttributeName);
    subject.setTranslationMap(translationMap);
    
    // add the attributes
    Map<String, Set<String>> myAttributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();

    for (String subjectAttributeName: subjectAttributesToValues.keySet()) {
      Object value = subjectAttributesToValues.get(subjectAttributeName);
      if (value instanceof Set) {
        myAttributes.put(subjectAttributeName, (Set<String>)value);
      } else {
        myAttributes.put(subjectAttributeName, GrouperUtil.toSetObject((String)value));
      }
    }

    subject.setAttributes(myAttributes);
    return subject;
  }
  
  public Set<String> getSourceAttributesToLowerCase() {
    
    if (sourceAttributesToLowerCase == null) {
      
      Set<String> temp = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      
      String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
            
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
                    
          boolean formatToLowerCase = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".formatToLowerCase", false);
          
          String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
          
          boolean isSourceAttribute = StringUtils.equals(translationType, "sourceAttribute");
          boolean isSourceAttributeSameAsSubjectAttribute = StringUtils.equals(translationType, "sourceAttributeSameAsSubjectAttribute");
          
          if (formatToLowerCase) {
            if (isSourceAttributeSameAsSubjectAttribute) {
              String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
              temp.add(subjectAttributeName);
            } else if (isSourceAttribute) {
              String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
              temp.add(sourceAttribute);
            }
            
          } 
       
        }
        
      }
            
      sourceAttributesToLowerCase = temp;
      
    }
    
    return sourceAttributesToLowerCase;
  }


  public String convertSubjectAttributeToSourceAttribute(String nameOfSubjectAttribute) {
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    
    if (this.isEditable()) {
          
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
          
          String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
          
          boolean isSourceAttribute = StringUtils.equals(translationType, "sourceAttribute");
          
          String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
          if (StringUtils.equals(nameOfSubjectAttribute, subjectAttributeName)) {
            if (isSourceAttribute) {
              return sourceAttribute;
            }
          }
        }
        
      }
          
    }
    
    return nameOfSubjectAttribute;
    
  } 
  
  private Map<String, String> exportLabelToAttributeName = null;

  private Map<String, String> attributeNameToViewerGroupName = null;
  
  
  /**
   * return export label to attribute name (if there are overrides)
   * @return empty if no overrides, otherwise the attribute name to export label
   */
  public Map<String, String> exportLabelToAttributeName() {
    
    if (this.exportLabelToAttributeName == null) {
      Map<String, String> tempExportLabelToAttributeName = new HashMap<String, String>();
      
      String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
      
      if (this.isEditable()) {
            
        if (StringUtils.isNotBlank(numberOfAttributes)) {
          
          int numberOfAttrs = Integer.parseInt(numberOfAttributes);
          for (int i=0; i<numberOfAttrs; i++) {
            
            String exportHeader = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".exportHeader");
            if (!StringUtils.isBlank(exportHeader)) {
              String attributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
              tempExportLabelToAttributeName.put(exportHeader, attributeName);
            }
          }
        }
      }
      this.exportLabelToAttributeName = tempExportLabelToAttributeName;
    }
    return this.exportLabelToAttributeName;
  }
  
  /**
   * return the attribute name
   * @return empty if no overrides, otherwise the attribute name to export label
   */
  public Map<String, String> attributeNameToViewerGroupName() {
    
    if (this.attributeNameToViewerGroupName == null) {
      Map<String, String> tempAttributeNameToViewerGroupName = new HashMap<String, String>();
      
      String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
      
      if (this.isEditable()) {
            
        if (StringUtils.isNotBlank(numberOfAttributes)) {
          
          int numberOfAttrs = Integer.parseInt(numberOfAttributes);
          for (int i=0; i<numberOfAttrs; i++) {
            
            String requireGroupNameForView = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".requireGroupNameForView");
            if (!StringUtils.isBlank(requireGroupNameForView)) {
              String attributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
              tempAttributeNameToViewerGroupName.put(attributeName, requireGroupNameForView);
            }
          }
        }
      }
      this.attributeNameToViewerGroupName = tempAttributeNameToViewerGroupName;
    }
    return this.attributeNameToViewerGroupName;
  }
  
  public String convertSourceAttributeToSubjectAttribute(String nameOfSourceAttribute) {
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    
    if (this.isEditable()) {
          
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
          if (StringUtils.equals(nameOfSourceAttribute, sourceAttribute)) {
            
            String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
            
            boolean isSourceAttribute = StringUtils.equals(translationType, "sourceAttribute");
            
            if (isSourceAttribute) {
              return SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
            }
          }
        }
        
      }
          
    }
    
    return nameOfSourceAttribute;
    
  } 
  
  
  /**
   * @see edu.internet2.middleware.subject.Source#retrieveAllSubjectIds()
   */
  @Override
  public Set<String> retrieveAllSubjectIds() {
    throw new UnsupportedOperationException();
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String, boolean, java.lang.String)
   */
  @Override
  public Subject getSubject(String id1, boolean exceptionIfNull, String realm)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSubject(id1, exceptionIfNull);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String, boolean, java.lang.String)
   */
  @Override
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull, String realm)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSubjectByIdentifier(id1, exceptionIfNull);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectByIdOrIdentifier(java.lang.String, boolean, java.lang.String)
   */
  @Override
  public Subject getSubjectByIdOrIdentifier(String idOrIdentifier,
      boolean exceptionIfNull, String realm) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubjectByIdOrIdentifier(idOrIdentifier, exceptionIfNull);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdentifiers(java.util.Collection, java.lang.String)
   */
  @Override
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers,
      String realm) {
    return this.getSubjectsByIdentifiers(identifiers);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIds(java.util.Collection, java.lang.String)
   */
  @Override
  public Map<String, Subject> getSubjectsByIds(Collection<String> ids, String realm) {
    return this.getSubjectsByIds(ids);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdsOrIdentifiers(java.util.Collection, java.lang.String)
   */
  @Override
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(
      Collection<String> idsOrIdentifiers, String realm) {
    return null;
  }


  /**
   * @see edu.internet2.middleware.subject.Source#search(java.lang.String, java.lang.String)
   */
  @Override
  public Set<Subject> search(String searchValue, String realm) {
    return this.search(searchValue);
  }


  /**
   * @see edu.internet2.middleware.subject.Source#searchPage(java.lang.String, java.lang.String)
   */
  @Override
  public SearchPageResult searchPage(String searchValue, String realm) {
    return this.searchPage(searchValue);
  }


  /**
   * @see Source#getSubjectStatusConfig()
   */
  @Override
  public SubjectStatusConfig getSubjectStatusConfig() {

    //get the cached config for this source
    SourceManager sourceManager = SourceManager.getInstance();
    SourceManagerStatusBean sourceManagerStatusBean = sourceManager.getSourceManagerStatusBean();
    Map<String, SubjectStatusConfig> sourceIdToStatusConfigs = sourceManagerStatusBean.getSourceIdToStatusConfigs();

//    System.out.println(sourceManager.hashCode() + ", " 
//        + sourceManagerStatusBean.hashCode() + ", " 
//        + sourceIdToStatusConfigs.hashCode() + ", " 
//        + sourceIdToStatusConfigs.size() + ", " 
//        + sourceIdToStatusConfigs.get("g:gsa") + ", " + this.getId());
    
    return sourceIdToStatusConfigs.get(this.getId());
    
  }


  /**
   * see what the result set limit should be (dont add one yet)
   * @param firstPageOnly
   * @param pageSize
   * @param theMaxResults
   * @return the limit or null if none
   */
  public static Integer resultSetLimit(boolean firstPageOnly, Integer pageSize, Integer theMaxResults) {
    Integer result = null;
    if ((firstPageOnly && pageSize != null) || theMaxResults != null) {
      result = (firstPageOnly && pageSize != null) ? (pageSize) : null;
      if (result == null) {
        result = theMaxResults;
      } else if (theMaxResults != null){
        result = Math.min(result, theMaxResults);
      }
    }
    return result;
  }
  

  /**
   * @see edu.internet2.middleware.subject.Source#searchPage(java.lang.String)
   */
  public SearchPageResult searchPage(String searchValue) {
    Set<Subject> results = this.search(searchValue);
    return new SearchPageResult(false, results);
  }

  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdentifiers(Collection<String> identifiers) {
    Map<String, Subject> result = new LinkedHashMap<String, Subject>();
    
    Subject subject = null;
    for (String theIdentifier : identifiers) {
      if (theIdentifier == null ) {
        continue;
      }

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
    for (String theId : SubjectUtils.nonNull(ids)) {
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
    
    Map<String, Object> debugLog = null;
    try {
      if (log.isDebugEnabled()) {
        debugLog = new LinkedHashMap<String, Object>();
        debugLog.put("method", "getSubjectByIdOrIdentifier");
        debugLog.put("idOrIdentifier", idOrIdentifier);
        debugLog.put("exceptionIfNull", exceptionIfNull);
      }
      
      Subject subject = null;
  
      //try by id first
      subject = this.getSubject(idOrIdentifier, false);
  
      //try by identifier if not by id
      if (subject == null) {
        if (debugLog != null) {
          debugLog.put("subjectById", "notFound");
        }
        subject = this.getSubjectByIdentifier(idOrIdentifier, false);
        if (debugLog != null) {
          debugLog.put("subjectByIdentifier", subject == null ? "notFound" : "found");
        }
      } else {
        if (debugLog != null) {
          debugLog.put("subjectById", "found");
        }
      }
  
      //if null at this point, and exception, then throw it
      if (subject == null && exceptionIfNull) {
        throw new SubjectNotFoundException("Cant find subject by id or identifier: '" + idOrIdentifier + "'"); 
      }
  
      return subject;
    } finally {
      if (log.isDebugEnabled()) {
        log.debug(SubjectUtils.mapToString(debugLog));
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectsByIdsOrIdentifiers(java.util.Collection)
   */
  public Map<String, Subject> getSubjectsByIdsOrIdentifiers(
      Collection<String> idsOrIdentifiers) {
    Map<String, Subject> result = new LinkedHashMap<String, Subject>();

    if (SubjectUtils.length(idsOrIdentifiers) == 0) {
      return result;
    }
    //do these in batches so they have the batched performance...
    result.putAll(SubjectUtils.nonNull(this.getSubjectsByIdentifiers(idsOrIdentifiers)));
    
    //take out the ones that were found
    Set<String> identifiers = new HashSet<String>(idsOrIdentifiers);
    identifiers.removeAll(result.keySet());
    if (SubjectUtils.length(identifiers) > 0) {
      result.putAll(SubjectUtils.nonNull(this.getSubjectsByIds(identifiers)));
    }
    
    return result;
  }

  /**
   * 
   */
  private static Log log = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(BaseSourceAdapter.class);

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
  protected Set<String> attributes = new SubjectCaseInsensitiveSetImpl<String>();

  /** internal attributes. */
  protected Set<String> internalAttributes = new SubjectCaseInsensitiveSetImpl<String>();
  
  protected Map<Integer, String> subjectIdentifierAttributes = null;
  
  protected Map<Integer, String> subjectIdentifierAttributesAll = null;
  
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
    this.params.setProperty(name1, GrouperClientUtils.defaultString(value));
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
  public Properties initParams() {
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
    if (log.isDebugEnabled()) {
      log.debug("Loading search: " + (search == null ? null : search.getSearchType()));
    }
    this.searches.put(search.getSearchType(), search);
  }
  
  /**
   * @param subjectIdentifierAttributes
   */
  public void setSubjectIdentifierAttributes(Map<Integer, String> subjectIdentifierAttributes) {
    this.subjectIdentifierAttributes = subjectIdentifierAttributes;
  }
  
  /**
   * @param subjectIdentifierAttributesAll
   */
  public void setSubjectIdentifierAttributesAll(Map<Integer, String> subjectIdentifierAttributesAll) {
    this.subjectIdentifierAttributesAll = subjectIdentifierAttributesAll;
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
   * @see edu.internet2.middleware.subject.Source#getSubjectIdentifierAttributes()
   */
  public Map<Integer, String> getSubjectIdentifierAttributes() {
    
    if (this.subjectIdentifierAttributes == null) {
      synchronized(BaseSourceAdapter.class) {
        if (this.subjectIdentifierAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          for (int i = 0; i < 1; i++) {
            String value = getInitParam("subjectIdentifierAttribute" + i);
            if (value != null) {
              temp.put(i, value.toLowerCase());
            }        
          }
          
          if (temp.size() == 0) {
            String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
            if (StringUtils.isNotBlank(numberOfAttributes)) {
              
              int numberOfAttrs = Integer.parseInt(numberOfAttributes);
              for (int i=0; i<numberOfAttrs; i++) {
                
                boolean subjectIdentifier = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".subjectIdentifier", false);
                if (subjectIdentifier) {
                  String name = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
                  if (StringUtils.isNotBlank(name)) {
                    int attributeNumber = temp.size();
                    temp.put(attributeNumber, name.toLowerCase());
                  }
                }
              
              }
              
            }
          }
          
          this.subjectIdentifierAttributes = temp;
        }
      }
    }
    
    
    
    return this.subjectIdentifierAttributes;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSubjectIdentifierAttributesAll()
   */
  public Map<Integer, String> getSubjectIdentifierAttributesAll() {
    
    if (this.subjectIdentifierAttributesAll == null) {
      synchronized(BaseSourceAdapter.class) {
        if (this.subjectIdentifierAttributesAll == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          for (int i = 0; i < 20; i++) {
            String value = getInitParam("subjectIdentifierAttribute" + i);
            if (value != null) {
              temp.put(i, value.toLowerCase());
            }        
          }

          if (temp.size() == 0) {
            String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
            if (StringUtils.isNotBlank(numberOfAttributes)) {
              
              int numberOfAttrs = Integer.parseInt(numberOfAttributes);
              for (int i=0; i<numberOfAttrs; i++) {
                
                boolean subjectIdentifier = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".subjectIdentifier", false);
                if (subjectIdentifier) {
                  String name = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
                  if (StringUtils.isNotBlank(name)) {
                    int attributeNumber = temp.size();
                    temp.put(attributeNumber, name.toLowerCase());
                  }
                }
              
              }
              
            }
          }
          
          this.subjectIdentifierAttributesAll = temp;
        }
      }
    }
    
    return this.subjectIdentifierAttributesAll;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Source#getSortAttributes()
   */
  public Map<Integer, String> getSortAttributes() {
    
    if (this.sortAttributes == null) {
      synchronized(BaseSourceAdapter.class) {
        if (this.sortAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          for (int i = 0; i < 5; i++) {
            String value = getInitParam("sortAttribute" + i);
            if (value != null) {
              temp.put(i, value.toLowerCase());
            }        
          }
          
          this.sortAttributes = temp;
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
      synchronized(BaseSourceAdapter.class) {
        if (this.searchAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          for (int i = 0; i < 5; i++) {
            String value = getInitParam("searchAttribute" + i);
            if (value != null) {
              temp.put(i, value.toLowerCase());
            }        
          }
          
          this.searchAttributes = temp;
        }
      }
    }
    
    return this.searchAttributes;
  }


  @Override
  public boolean isEditable() {
    return false;
  }

  private String configId;
  
  /** expirable cache will not look at configs all the time, but will refresh */
  private static ExpirableCache<String, Map<String, String>> virtualAttributeForSource = 
    new ExpirableCache<String, Map<String, String>>(2);

  /** expirable cache will not look at configs all the time, but will refresh */
  private static ExpirableCache<String, Map<String, String>> virtualAttributeForSourceLegacy = 
    new ExpirableCache<String, Map<String, String>>(2);

  /** expirable cache will not look at configs all the time, but will refresh */
  private static ExpirableCache<String, Map<String, String>> virtualAttributeVariablesForSourceLegacy = 
    new ExpirableCache<String, Map<String, String>>(2);
  
  @Override
  public String getConfigId() {
    return this.configId;
  }
  
  @Override
  public void setConfigId(String configId) {
    this.configId = configId;
  }


  @Override
  public boolean isEnabled() {
    return true;
  }
  
  /**
   * get the ordered list of virtual attributes for a source (new style)
   * @param source
   * @return the ordered list
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> virtualAttributesForSource(Source source) {
  
    Map<String, String> virtualAttributes = virtualAttributeForSource.get(source.getId());
    if (virtualAttributes!=null) {
      return virtualAttributes;
    }
    String configId = source.getConfigId();
    virtualAttributes = new LinkedHashMap<String, String>();
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + configId + ".numberOfAttributes");
    
    if (StringUtils.isNotBlank(numberOfAttributes)) {
  
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
  
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + configId + ".attribute."+i+".name");
        
        String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + configId + ".attribute."+i+".translationType");
        
        boolean isTranslation = StringUtils.equals(translationType, "translation");
        
        if (isTranslation) {
  
          String translation = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + configId + ".attribute."+i+".translation");
          virtualAttributes.put(subjectAttributeName, translation);
        }
      }
    }
    
    virtualAttributeForSource.put(source.getId(), virtualAttributes);
    return virtualAttributes;
  }

  /**
   * get the ordered list of virtual attributes for a source
   * @param source
   * @return the ordered list
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> virtualAttributesForSourceLegacy(Source source) {
  
    Map<String, String> virtualAttributes = virtualAttributeForSourceLegacy.get(source.getId());
    if (virtualAttributes!=null) {
      return virtualAttributes;
    }
    
    virtualAttributes = new LinkedHashMap<String, String>();
    
    if (source instanceof GrouperLdapSourceAdapter2_5 || source instanceof GrouperJdbcSourceAdapter2_5) {
      
      String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".numberOfAttributes");
      
      if (StringUtils.isNotBlank(numberOfAttributes)) {
        
        int numberOfAttrs = Integer.parseInt(numberOfAttributes);
        for (int i=0; i<numberOfAttrs; i++) {
          
          String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".name");
          
          String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".translationType");
          
          boolean isTranslation = StringUtils.equals(translationType, "translation");
          
          if (isTranslation) {
            String translation = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + source.getConfigId() + ".attribute."+i+".translation");
            virtualAttributes.put(subjectAttributeName, translation);
          }
       }
      }
      
    } else {
      Properties properties = source.initParams();
      
      //no virtuals
      if (properties != null && properties.size() > 0) {
        
        //these are the virtual names:
        Set<String> virtualKeys = new HashSet<String>((Set<String>)(Object)properties.keySet());
        
  
        Iterator<String> iterator = virtualKeys.iterator();
        
        while (iterator.hasNext()) {
          String virtualKey = iterator.next();
          if (!virtualKey.startsWith("subjectVirtualAttribute_")) {
            iterator.remove();
          }
        }
        
        //look for virtuals, we need these in order since they might depend on each other
        for (int i=0;i<100;i++) {
  
          //maybe we are done
          if (virtualKeys.size() == 0) {
            break;
          }
          
          iterator = virtualKeys.iterator();
          
          Pattern pattern = Pattern.compile("^subjectVirtualAttribute_" + i + "_(.*)$");
          
          //subjectVirtualAttribute_0_someName (name alphanumeric underscore) JEXL expression
          while (iterator.hasNext()) {
            String key = iterator.next();
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              String name = matcher.group(1);
              if (!name.matches("[a-zA-Z0-9_]+")) {
                String message = "Virtual attribute name (from subject.properties?) must be alphanumeric, or underscore: '" 
                  + name + "' for source: " + source.getId();
                log.error(message);
                throw new RuntimeException(message);
              }
              virtualAttributes.put(name, properties.getProperty(key));
              iterator.remove();
            }
          }
        }
        if (virtualKeys.size() > 0) {
          log.error("Invalid virtual attribute keys: " + SubjectUtils.toStringForLog(virtualKeys) + ", for source: " + source.getId());
        }
      }
    }
    
    
    virtualAttributeForSourceLegacy.put(source.getId(), virtualAttributes);
    return virtualAttributes;
  }

  /**
   * get the ordered list of virtual attributes for a source
   * @param source
   * @return the ordered list
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> virtualAttributeVariablesForSourceLegacy(Source source) {
  
    Map<String, String> virtualAttributeVariables = virtualAttributeVariablesForSourceLegacy.get(source.getId());
    if (virtualAttributeVariables!=null) {
      return virtualAttributeVariables;
    }
    
    virtualAttributeVariables = new LinkedHashMap<String, String>();
    Properties properties = source.initParams();
    
    //no virtuals
    if (properties != null && properties.size() > 0) {
      
      //these are the virtual names:
      Set<String> propertiesSet = new HashSet<String>((Set<String>)(Object)properties.keySet());
      
      Iterator<String> iterator = propertiesSet.iterator();
      
      Pattern pattern = Pattern.compile("^subjectVirtualAttributeVariable_(.*)$");
  
      while (iterator.hasNext()) {
        String property = iterator.next();
        Matcher matcher = pattern.matcher(property);
        if (matcher.matches()) {
          String name = matcher.group(1);
          if (!name.matches("[a-zA-Z0-9_]+")) {
            String message = "Virtual attribute variable name (from subject.properties?) must be alphanumeric, or underscore: '" 
              + name + "' for source: " + source.getId();
            log.error(message);
            throw new RuntimeException(message);
          }
          virtualAttributeVariables.put(name, properties.getProperty(property));
        }
      }
    }
    
    virtualAttributeVariablesForSourceLegacy.put(source.getId(), virtualAttributeVariables);
    return virtualAttributeVariables;
  }
  
}
