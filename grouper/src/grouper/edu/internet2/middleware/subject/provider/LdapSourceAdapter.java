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
/* ========================================================================
 * Copyright (c) 2009-2011 The University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

/*
 * Ldap subject adapter
 * @author fox
 */

package edu.internet2.middleware.subject.provider;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * Ldap source adapter.  
 */

public class LdapSourceAdapter extends BaseSourceAdapter {

  private static Log log = LogFactory.getLog(LdapSourceAdapter.class);

  private Properties props;
  private String nameAttributeName = null;
  private String subjectIDAttributeName = null;
  private boolean subjectIDFormatToLowerCase = false;
  private String descriptionAttributeName = null;
  private String ldapServerId = null;

  private boolean multipleResults = false;

  private String[] allAttributeNames;

  private boolean throwErrorOnFindAllFailure;

  /** if there is a limit to the number of results */
  private Integer maxPage;

  /**
   * 
   */
  public LdapSourceAdapter() {
    super();
  }

  /**
   * @param id
   * @param name
   */
  public LdapSourceAdapter(String id, String name) {
    super(id, name);
  }

  /**
   * {@inheritDoc}
   */
  public void init() {
    log.debug("ldap source init");
    props = initParams();

    ldapServerId = getNeededProperty(props, "ldapServerId");
    nameAttributeName = getNeededProperty(props,"Name_AttributeType");
    subjectIDAttributeName = getNeededProperty(props,"SubjectID_AttributeType");
    descriptionAttributeName = getNeededProperty(props,"Description_AttributeType");

    subjectIDFormatToLowerCase = SubjectUtils.booleanValue(getNeededProperty(props,"SubjectID_formatToLowerCase"), false);

    String mr = props.getProperty("Multiple_Results");
    if (mr!=null && (mr.equalsIgnoreCase("yes")||mr.equalsIgnoreCase("true"))) multipleResults = true;

    Set<?> attributeNameSet = this.getAttributes();
    allAttributeNames = new String[3+attributeNameSet.size()];
    allAttributeNames[0] = nameAttributeName;
    allAttributeNames[1] = subjectIDAttributeName;
    allAttributeNames[2] = descriptionAttributeName;
    int i = 0;
    for (Iterator<?> it = attributeNameSet.iterator(); it.hasNext(); allAttributeNames[3+i++]= (String) it.next());

    Map<String, String> virtualAttributes = SubjectUtils.nonNull(SubjectImpl.virtualAttributesForSource(this));

    // GRP-1669: grouper sends virtual attribute names to ldap
    //take out dupes and virtuals
    Set<String> attributeSet = SubjectUtils.toSet(allAttributeNames);
    attributeSet.removeAll(virtualAttributes.keySet());
    allAttributeNames = toArray(attributeSet, String.class);

    String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
    throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);


    {
      String maxPageString = props.getProperty("maxPageSize");
      if (!StringUtils.isBlank(maxPageString)) {
        try {
          this.maxPage = Integer.parseInt(maxPageString);
        } catch (NumberFormatException nfe) {
          throw new SourceUnavailableException("Cant parse maxPage: " + maxPageString, nfe);
        }
      }
    }

  }

  /**
   * convert a list into an array of type of theClass
   * @param <T> is the type of the array
   * @param collection list to convert
   * @param theClass type of array to return
   * @return array of type theClass[] filled with the objects from list
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Collection collection, Class<T> theClass) {
    if (collection == null || collection.size() == 0) {
      return null;
    }

    return (T[])collection.toArray((Object[]) Array.newInstance(theClass,
        collection.size()));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Subject getSubject(String id, boolean exceptionIfNull)
      throws SubjectNotFoundException,SubjectNotUniqueException {
    Subject subject = null;
    Search search = getSearch("searchSubject");
    if (search == null) {
      log.error("searchType: \"searchSubject\" not defined.");
      return subject;
    }
    try {
      LdapEntry entry = getLdapUnique( search, id, allAttributeNames   );
      subject = createSubject(entry);
      if (subject == null && exceptionIfNull) {
        throw new SubjectNotFoundException("Subject " + id + " not found.");
      }
    } catch (SubjectNotFoundException e) {
      if (exceptionIfNull) throw e;
    }
    return subject;
  }

  /**
   * {@inheritDoc}
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubject(String id) throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSubject(id, true); 
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public Subject getSubjectByIdentifier(String id, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Map<String, Object> debugLog = null;
    try {
      if (log.isDebugEnabled()) {
        debugLog = new LinkedHashMap<String, Object>();
        debugLog.put("method", "getSubjectByIdentifier");
        debugLog.put("id", id);
        debugLog.put("exceptionIfNull", exceptionIfNull);
      }
      Subject subject = null;
      Search search = getSearch("searchSubjectByIdentifier");
      if (debugLog != null) {
        debugLog.put("search", search);
      }

      if (search == null) {
        log.error("searchType: \"searchSubjectByIdentifier\" not defined.");
        return subject;
      }
      try {
        LdapEntry entry = getLdapUnique(search, id, allAttributeNames);
        subject = createSubject(entry);
        if (debugLog != null) {
          debugLog.put("foundSubject", subject != null);
        }
        if (subject == null && exceptionIfNull) {
          throw new SubjectNotFoundException("Subject " + id + " not found.");
        }
      } catch (SubjectNotFoundException e) {
        if (exceptionIfNull) {
          throw e;
        }
        return null;
      }
      search = getSearch("searchSubjectByIdentifierAttributes");
      if (debugLog != null) {
        debugLog.put("searchSubjectByIdentifierAttributesNotNull", search!=null);
      }
      if (search == null)
        ((LdapSubject) subject).setAttributesGotten(true);
      return subject;

    } finally {
      if (log.isDebugEnabled()) {
        log.debug(SubjectUtils.mapToString(debugLog));
      }
    }

  }

  /**
   * {@inheritDoc}
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubjectByIdentifier(String id) throws SubjectNotFoundException, SubjectNotUniqueException {
    return this.getSubjectByIdentifier(id, true);
  } 

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#searchPage(java.lang.String)
   */
  @Override
  public SearchPageResult searchPage(String searchValue) {
    return searchHelper(searchValue, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Subject> search(String searchValue) {
    return searchHelper(searchValue, false).getResults();
  }

  /**
   * @param searchValue 
   * @return the set
   */
  private SearchPageResult searchHelper(String searchValue, boolean firstPageOnly) {

    boolean tooManyResults = false;
    Comparator cp = new LdapComparator();
    TreeSet result = new TreeSet(cp);
    Search search = getSearch("search");
    if (search == null) {
      log.error("searchType: \"search\" not defined.");
      return new SearchPageResult(tooManyResults, result);
    }
    Search searchA = getSearch("searchAttributes");
    boolean noAttrSearch = true;
    if (searchA!=null) noAttrSearch = false;

    try {
      Iterator<LdapEntry> ldapResults = getLdapResultsHelper(search,searchValue, allAttributeNames, firstPageOnly);
      if (ldapResults == null) {
        return new SearchPageResult(tooManyResults, result);
      }
      while ( ldapResults.hasNext()) {
        //if we are at the end of the page
        if (firstPageOnly && this.maxPage != null && result.size() >= this.maxPage) {
          tooManyResults = true;
          break;
        }

        LdapEntry si = ldapResults.next();
        Subject subject = createSubject(si);

        if (subject != null) {
          if (noAttrSearch) ((LdapSubject)subject).setAttributesGotten(true);
          result.add(subject);
        } else {
          log.error("Failed to create subject with attributes: " + si.toString());  
        }


      }

      if (log.isDebugEnabled()) {
        log.debug("set has " + result.size() + " subjects");
        if (result.size()>0) log.debug("first is " + ((Subject)result.first()).getName());
      }

    } catch (Exception ex) {

      if (throwErrorOnFindAllFailure) {
        throw new SourceUnavailableException(ex.getMessage() + ", source: " + this.getId() + ", sql: " + search.getParam("sql"), ex);
      }

      log.error("LDAP Naming Except: " + ex.getMessage() + ", " + this.id + ", " + searchValue, ex);
    }

    return new SearchPageResult(tooManyResults, result);
  }

  /**
   * @param attributes
   */
  private Subject createSubject(LdapEntry entry) {
    String subjectID = "";

    if (entry==null) {
      log.error("Ldap createSubject called with null entry.");
      return (null);
    }
    LdapAttribute attribute = entry.getAttribute(subjectIDAttributeName);
    if (attribute == null || attribute.getStringValues().size() == 0) {
      log.error("No value for LDAP attribute \"" + subjectIDAttributeName + "\". It is Grouper attribute \"SubjectID\".\".  Subject's problematic attributes : " + entry.toString());
      return null;
    }
    subjectID   = attribute.getStringValues().iterator().next();
    if (this.subjectIDFormatToLowerCase) {
      subjectID = subjectID.toLowerCase();
    }
    LdapSubject subject = new LdapSubject(subjectID, null, null, this.getSubjectType().getName(), this.getId(), nameAttributeName, descriptionAttributeName);

    // add the attributes

    Map<String, Set<String>> myAttributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    for (Iterator<LdapAttribute> e = entry.getAttributes().iterator(); e.hasNext();) {
      LdapAttribute attr = e.next();
      String attrName = attr.getName();

      Set<String> values = new HashSet<String>();
      values.addAll(attr.getStringValues());
      myAttributes.put(attrName, values);
    }
    subject.setAttributes(myAttributes);

    return subject;
  }

  protected String getNeededProperty(Properties props, String prop) {
    String value = props.getProperty(prop);
    if (value==null) {
      log.error("Property '" + prop + "' is not defined!");
    }
    return (value);
  }


  /**
   * Try to get more attributes for the subject.
   * @param subject
   */
  protected Map<String, Set<String>> getAllAttributes(LdapSubject subject) {
    Map<String, Set<String>> attributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    if (log.isDebugEnabled()) {
      log.debug("getAllAttributes for " + subject.getName());
    }
    Search search = getSearch("searchSubjectAttributes");
    if (search == null) {
      log.debug("searchType: \"searchSubjectAttributes\" not defined.");
      return attributes;
    }

    try {
      LdapEntry entry = getLdapUnique(search,subject.getName(), allAttributeNames);
      for (Iterator<LdapAttribute> e = entry.getAttributes().iterator(); e.hasNext();) {
        LdapAttribute attr = e.next();
        String attrName = attr.getName();

        Set<String> values = new HashSet<String>();
        values.addAll(attr.getStringValues());
        attributes.put(attrName, values);
      }
      subject.setAttributes(attributes);
    } catch (SubjectNotFoundException ex ) {
      log.error("SubjectNotFound: "+ subject.getId() +" " + ex.getMessage(), ex);
    } catch (SubjectNotUniqueException ex ) {
      log.error("SubjectNotUnique: "+ subject.getId() +" " + ex.getMessage(), ex);
    }
    return attributes;
  }

  protected Iterator<LdapEntry> getLdapResults(Search search, String searchValue, String[] attributeNames) {
    return getLdapResultsHelper(search, searchValue, attributeNames, false); 
  }

  private Iterator<LdapEntry> getLdapResultsHelper(Search search, String searchValue, String[] attributeNames, boolean firstPageOnly ) {

    SubjectStatusResult subjectStatusResult = null;

    //if this is a search and not by id or identifier
    boolean subjectStatusQuery = StringUtils.equals("search", search.getSearchType());
    if (subjectStatusQuery) {
      //see if we are doing status
      SubjectStatusProcessor subjectStatusProcessor = new SubjectStatusProcessor(searchValue, this.getSubjectStatusConfig());
      subjectStatusResult = subjectStatusProcessor.processSearch();

      //strip out status parts
      searchValue = subjectStatusResult.getStrippedQuery();
    }      

    String filter = null;
    Iterator<LdapEntry> results = null;
    int cp;
    String aff = null;

    // build a filter
    if ((cp=searchValue.indexOf(',')) >0 ) {
      int lb, rb;
      if ( (lb=searchValue.indexOf('['))>cp && (rb=searchValue.indexOf(']'))>lb ) {
        aff = searchValue.substring(lb+1, rb);
        searchValue = searchValue.substring(0, lb);
        // log.debug("first, last [" + aff + "] search: " + searchValue);
        filter = search.getParam("affiliationfilter");
      } else {
        // log.debug("first, last search: " + searchValue);
        filter = search.getParam("firstlastfilter");
      }
      if (filter==null) filter = search.getParam("filter");  // fall back
      if (filter==null) {
        log.error("Search filter not found for search type:  " + search.getSearchType());
        return null;
      }
      String last = searchValue.substring(0, cp);
      String first = searchValue.substring(cp+1);
      if (last!=null) filter = GrouperClientUtils.replace(filter, "%LAST%", escapeSearchFilter(last));
      if (first!=null) filter = GrouperClientUtils.replace(filter, "%FIRST%", escapeSearchFilter(first));
      if (aff!=null) filter = GrouperClientUtils.replace(filter, "%AFFILIATION%", escapeSearchFilter(aff));
    } else {
      // simple search
      filter = search.getParam("filter");
      if (filter==null) {
        log.error("Search filter not found for search type:  " + search.getSearchType());
        return results;
      }
      filter = GrouperClientUtils.replace(filter, "%TERM%", escapeSearchFilter(searchValue));
    }

    String preStatusFilter = filter;
    if (subjectStatusQuery && !subjectStatusResult.isAll() && !StringUtils.isBlank(subjectStatusResult.getDatastoreFieldName())) {

      //validate the status value
      if (!subjectStatusResult.getDatastoreValue().matches("[a-zA-Z0-9_-]+")) {
        throw new RuntimeException("Invalid status value: " + subjectStatusResult.getDatastoreValue());
      }

      //wrap the query in a status part
      filter = "(&" + filter + "(" + (subjectStatusResult.isEquals()?"":" ! ( ") + subjectStatusResult.getDatastoreFieldName() + "=" 
          + subjectStatusResult.getDatastoreValue() + (subjectStatusResult.isEquals()?"":" ) ") + "))";

    }

    if (!StringUtils.equals(preStatusFilter, filter)) {
      if (log.isDebugEnabled()) {
        log.debug("searchType: " + search.getSearchType() + ", preStatusFilter: " + preStatusFilter + ", filter: " + filter);
      }

    } else {
      if (log.isDebugEnabled()) {
        log.debug("searchType: " + search.getSearchType() + ", filter: " + filter);
      }

    }

    try  {

      Long sizeLimit = null;
      //if we are at the end of the page
      if (firstPageOnly && this.maxPage != null) {
        sizeLimit = this.maxPage + 1L;
      }

      // get params
      String base = search.getParam("base");

      String searchScopeString = search.getParam("scope");   
      LdapSearchScope searchScope = null;
      
      if (searchScopeString != null) {
        searchScope = LdapSearchScope.valueOf(searchScopeString);
      }

      results = LdapSessionUtils.ldapSession().list(ldapServerId, base, searchScope, filter, attributeNames, sizeLimit).iterator();
    } catch (Exception ex) {  // don't know if there are others
      log.error("Ldap Exception: " + ex.getMessage(), ex);
      throw new SourceUnavailableException("Ldap Exception: " + ex.getMessage(), ex);
    }
    
    return results;
  }


  protected LdapEntry getLdapUnique( Search search, String searchValue, String[] attributeNames)
      throws SubjectNotFoundException,SubjectNotUniqueException, SourceUnavailableException  {


    Map<String, Object> debugLog = null;
    try {
      if (log.isDebugEnabled()) {
        debugLog = new LinkedHashMap<String, Object>();
        debugLog.put("method", "getLdapUnique");
        debugLog.put("search", search);
        debugLog.put("searchValue", searchValue);
        debugLog.put("attributeNames", SubjectUtils.toStringForLog(attributeNames, 200));
      }

      Iterator<LdapEntry> results = getLdapResults(search, searchValue, attributeNames);

      if (results == null || !results.hasNext()) {
        String errMsg = "No results: " + search.getSearchType() + " filter:" + search.getParam("filter") + " searchValue: " + searchValue;
        throw new SubjectNotFoundException( errMsg);
      }

      LdapEntry overall = results.next();

      if (debugLog!=null) {
        debugLog.put("dn", overall.getDn());
      }

      // Add the DN to the returned attributes.
      LdapAttribute dnAttribute = new LdapAttribute("dn");
      dnAttribute.addStringValue(overall.getDn());
      overall.addAttribute(dnAttribute);

      if (results.hasNext()) {
        LdapEntry si = results.next();
        if (debugLog!=null) {
          debugLog.put("dn2", si.getDn());
        }
        if (!multipleResults) {
          if (debugLog!=null) {
            debugLog.put("searchIsNotUnique", true);
          }
          String errMsg ="Search is not unique:" + si.getDn() + "\n";
          throw new SubjectNotUniqueException( errMsg );
        }
        Iterator<LdapAttribute> n = si.getAttributes().iterator();
          while (n.hasNext()) {
            LdapAttribute a = n.next();
            if (log.isDebugEnabled()) {
              log.debug("checking attribute " + a.getName());
            }
            if (overall.getAttribute(a.getName())==null || 
                (overall.getAttribute(a.getName()).getStringValues().size() == 0 && overall.getAttribute(a.getName()).getBinaryValues().size() == 0)) {
              if (log.isDebugEnabled()) {
                log.debug("adding " + a.getName());
              }
              overall.addAttribute(a);
            }
          }

        // Add the DN to the returned attributes.
        overall.getAttribute("dn").addStringValue(si.getDn());
      }
      return overall;

    } finally {
      if (log.isDebugEnabled()) {
        log.debug(SubjectUtils.mapToString(debugLog));
      }
    }


  }

  /**
   * Escape a search filter to prevent LDAP injection.
   * From http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
   * 
   * @param filter
   * @return escaped filter
   */
  protected String escapeSearchFilter(String filter) {
    //From RFC 2254
    String escapedStr = new String(filter);
    // note, these are regexes, so thats why there is an extra slash
    escapedStr = escapedStr.replaceAll("\\\\","\\\\5c");
    // We want people to be able to use wildcards.
    // escapedStr = escapedStr.replaceAll("\\*","\\\\2a");
    escapedStr = escapedStr.replaceAll("\\(","\\\\28");
    escapedStr = escapedStr.replaceAll("\\)","\\\\29");
    escapedStr = escapedStr.replaceAll("\\"+Character.toString('\u0000'), "\\\\00");
    return escapedStr;
  }
  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {
  }
  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {
    StringBuilder message = new StringBuilder((SourceManager.usingSubjectProperties() ? "subject.properties" : "sources.xml       ") + " ldap source id:   ").append(this.getId()).append(": ");
    message.append(ldapServerId);

    return message.toString();
  }

  /**
   * Set whether or not multiple results are allowed. Primarily for tests.
   * 
   * @param multipleResults
   */
  public void setMultipleResults(boolean multipleResults) {
    this.multipleResults = multipleResults;
  }

  /**
   * max Page size
   * @return the maxPage
   */
  public Integer getMaxPage() {
    return this.maxPage;
  }
}
