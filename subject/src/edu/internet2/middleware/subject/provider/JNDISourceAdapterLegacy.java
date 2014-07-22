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
$Id: JNDISourceAdapter.java,v 1.16 2009-10-23 04:04:22 mchyzer Exp $
$Date: 2009-10-23 04:04:22 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
/*
 * JNDISourceAdapter.java
 * 
 * Created on March 6, 2006
 * 
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

/**
 *
 * @author esluss
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.SearchPageResult;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectTooManyResults;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * JNDI Source 
 *
 */
public class JNDISourceAdapterLegacy extends BaseSourceAdapter {

  /** */
  private static Log log = LogFactory.getLog(JNDISourceAdapterLegacy.class);

  /** */
  Hashtable<String, String> environment = new Hashtable<String, String>(11);

  /** */
  String nameAttributeName = null;

  /** */
  String subjectIDAttributeName = null;

  /** */
  String descriptionAttributeName = null;

  /** */
  String subjectTypeString = null;

  /** if there is a limit to the number of results */
  private Integer maxPage;

  /** if there is a limit to the number of results */
  protected Integer maxResults;

  /** Return scope for searching as a int - associate the string with the int */
  protected static HashMap<String, Integer> scopeStrings = new HashMap<String, Integer>();

  static {
    // Use constructor instead of `Integer.valueOf()` to preserve 1.4.2 compatability. [blair]
    scopeStrings.put("OBJECT_SCOPE", new Integer(SearchControls.OBJECT_SCOPE));
    scopeStrings.put("ONELEVEL_SCOPE", new Integer(SearchControls.ONELEVEL_SCOPE));
    scopeStrings.put("SUBTREE_SCOPE", new Integer(SearchControls.SUBTREE_SCOPE));

  }

  /**
   * 
   * @param scope
   * @return scope
   */
  protected static int getScope(String scope) {
    Integer s = scopeStrings.get(scope.toUpperCase());
    if (s == null) {
      return -1;
    }
    return s;
  }

  /**
   * Allocates new JNDISourceAdapter;
   */
  public JNDISourceAdapterLegacy() {
    super();
  }

  /**
   * Allocates new JNDISourceAdapter;
   * @param id1
   * @param name1
   */
  public JNDISourceAdapterLegacy(String id1, String name1) {
    super(id1, name1);
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String, boolean)
   */
  @Override
  public Subject getSubject(String id1, boolean exceptionIfNull) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    Subject subject = null;
    Search search = getSearch("searchSubject");
    if (search == null) {
      log.error("searchType: \"searchSubject\" not defined.");
      return subject;
    }
    String[] attributeNames = { this.nameAttributeName, this.descriptionAttributeName,
        this.subjectIDAttributeName };
    try {
      Attributes attributes1 = getLdapUnique(search, id1, attributeNames);
      subject = createSubject(attributes1);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
    }
    if (subject == null && exceptionIfNull) {
      throw new SubjectNotFoundException("Subject " + id1 + " not found.");
    }

    return subject;
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String, boolean)
   */
  @Override
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    Subject subject = null;
    Search search = getSearch("searchSubjectByIdentifier");
    if (search == null) {
      //MCH 20090321: is this the right thing to do?  Or throw an exception?  Or search by ID?
      log.error("searchType: \"searchSubjectByIdentifier\" not defined.");
      return subject;
    }
    String[] attributeNames = { this.nameAttributeName, this.subjectIDAttributeName,
        this.descriptionAttributeName };
    try {
      Attributes attributes1 = getLdapUnique(search, id1, attributeNames);
      subject = createSubject(attributes1);
    } catch (SubjectNotFoundException snfe) {
      if (exceptionIfNull) {
        throw snfe;
      }
    }
    if (subject == null && exceptionIfNull) {
      throw new SubjectNotFoundException("Subject " + id1 + " not found.");
    }
    return subject;
  }

  /** for testing if we should fail on testing */
  public static boolean failOnSearchForTesting = false;

  /**
   * 
   * @param searchValue 
   * @param firstPageOnly 
   * @return  the result and if too many
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#search(java.lang.String)
   */
  private SearchPageResult searchHelper(String searchValue, boolean firstPageOnly) {
    Set<Subject> result = new LinkedHashSet<Subject>();
    boolean tooManyResults = false;
    Search search = getSearch("search");
    if (search == null) {
      log.error("searchType: \"search\" not defined.");
      return new SearchPageResult(tooManyResults, result);
    }
    String throwErrorOnFindAllFailureString = this.getInitParam("throwErrorOnFindAllFailure");
    boolean throwErrorOnFindAllFailure = SubjectUtils.booleanValue(throwErrorOnFindAllFailureString, true);

    try {
      String[] attributeNames = { this.nameAttributeName, this.subjectIDAttributeName,
          this.descriptionAttributeName };
      NamingEnumeration ldapResults = getLdapResults(search, searchValue, attributeNames, firstPageOnly);
      if (ldapResults == null) {
        return new SearchPageResult(tooManyResults, result);
      }

      if (failOnSearchForTesting) {
        throw new RuntimeException("failOnSearchForTesting");
      }
      
      while (ldapResults.hasMore()) {
        SearchResult si = (SearchResult) ldapResults.next();
        Attributes attributes1 = si.getAttributes();
        Subject subject = createSubject(attributes1);
        result.add(subject);
        //if we are at the end of the page
        if (firstPageOnly && this.maxPage != null && result.size() >= this.maxPage) {
          tooManyResults = true;
          break;
        }
        if (this.maxResults != null && result.size() >= this.maxResults) {
          throw new SubjectTooManyResults(
              "More results than allowed: " + this.maxResults 
              + " for search '" + search + "'");
        }
      }
    } catch (Exception ex) {
      if (ex instanceof SubjectTooManyResults) {
        throw (SubjectTooManyResults)ex;
      }
      if (!throwErrorOnFindAllFailure) {
        log.error("LDAP Naming Except: " 
            + ex.getMessage() + ", " + this.id + ", " + searchValue, ex);
      } else {
        throw new SourceUnavailableException(ex.getMessage() + ", source: " + this.getId() + ", sql: "
            + search.getParam("sql"), ex);
      }
    }

    return new SearchPageResult(tooManyResults, result);
  }

  /**
   * 
   * @param attributes1
   * @return subject
   */
  private Subject createSubject(Attributes attributes1) {
    String name1 = "";
    String subjectID = "";
    String description = "";
    try {
      Attribute attribute = attributes1.get(this.subjectIDAttributeName);
      if (attribute == null) {
        log
            .error("The LDAP attribute \""
                + this.subjectIDAttributeName
                + "\" does not have a value. It is beging used as the Grouper special attribute \"SubjectID\".");
        return null;
      }
      subjectID = (String) attribute.get();
      attribute = attributes1.get(this.nameAttributeName);
      if (attribute == null) {
        log
            .error("The LDAP attribute \""
                + this.nameAttributeName
                + "\" does not have a value. It is being used as the Grouper special attribute \"name\".");
        return null;
      }
      name1 = (String) attribute.get();
      attribute = attributes1.get(this.descriptionAttributeName);
      if (attribute == null) {
        log
            .error("The LDAP attribute \""
                + this.descriptionAttributeName
                + "\" does not have a value. It is being used as the Grouper special attribute \"description\".");
      } else {
        description = (String) attribute.get();
      }
    } catch (NamingException ex) {
      throw new SourceUnavailableException("LDAP Naming Except: " + ex.getMessage(), ex);
    }
    
    return new JNDISubject(subjectID, name1, description, 
        this.getSubjectType().getName(), this.getId(), null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Subject> search(String searchValue) {
    return searchHelper(searchValue, false).getResults();
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#searchPage(java.lang.String)
   */
  @Override
  public SearchPageResult searchPage(String searchValue) {
    return searchHelper(searchValue, true);
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#init()
   */
  @Override
  public void init() throws SourceUnavailableException {
    try {
      Properties props = getInitParams();
      setupEnvironment(props);
      
      {
        String maxResultsString = props.getProperty("maxResults");
        if (!StringUtils.isBlank(maxResultsString)) {
          try {
            this.maxResults = Integer.parseInt(maxResultsString);
          } catch (NumberFormatException nfe) {
            throw new SourceUnavailableException("Cant parse maxResults: " + maxResultsString, nfe);
          }
        }
      }
      
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

    } catch (Exception ex) {
      throw new SourceUnavailableException("Unable to init JNDI source", ex);
    }
  }

  /**
   * Setup environment.
   * @param props 
   * @throws SourceUnavailableException
   */
  protected void setupEnvironment(Properties props) throws SourceUnavailableException {

    this.environment.put(Context.INITIAL_CONTEXT_FACTORY, props
        .getProperty("INITIAL_CONTEXT_FACTORY"));
    this.environment.put(Context.PROVIDER_URL, props.getProperty("PROVIDER_URL"));
    this.environment.put(Context.SECURITY_AUTHENTICATION, props
        .getProperty("SECURITY_AUTHENTICATION"));
    
    if (props.getProperty("SECURITY_PRINCIPAL") != null) {
    this.environment.put(Context.SECURITY_PRINCIPAL, props.getProperty("SECURITY_PRINCIPAL"));
    }

    String password = props.getProperty("SECURITY_CREDENTIALS");
    password = Morph.decryptIfFile(password);

    if (password != null) {
    this.environment.put(Context.SECURITY_CREDENTIALS, password);
    }
    
    if (props.getProperty("SECURITY_PROTOCOL") != null) {
      //this used to be hardcoded to SSL!!!
      this.environment.put(Context.SECURITY_PROTOCOL, props.getProperty("SECURITY_PROTOCOL"));
    }
    Context context = null;
    try {
      log.debug("Creating Directory Context");
      context = new InitialDirContext(this.environment);
    } catch (AuthenticationException ex) {
      log.error("Error with Authentication " + ex.getMessage(), ex);
      throw new SourceUnavailableException("Error with Authentication ", ex);
    } catch (NamingException ex) {
      log.error("Naming Error " + ex.getMessage(), ex);
      throw new SourceUnavailableException("Naming Error", ex);
    } finally {
      if (context != null) {
        try {
          context.close();
        } catch (NamingException ne) {
          // squelch, since it is already closed
        }
      }
    }
    log.info("Success in connecting to LDAP");

    this.nameAttributeName = props.getProperty("Name_AttributeType");
    if (this.nameAttributeName == null) {
      log.error("Name_AttributeType not defined");
    }
    this.subjectIDAttributeName = props.getProperty("SubjectID_AttributeType");
    if (this.subjectIDAttributeName == null) {
      log.error("SubjectID_AttributeType not defined");
    }
    this.descriptionAttributeName = props.getProperty("Description_AttributeType");
    if (this.descriptionAttributeName == null) {
      log.error("Description_AttributeType not defined");
    }

  }

  /**
   * Loads attributes for the argument subject.
   * @param subject 
   * @return the map
   */
  protected Map loadAttributes(SubjectImpl subject) {
    Map<String, Set<String>> attributes1 = new SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    Search search = getSearch("searchSubject");
    if (search == null) {
      log.error("searchType: \"search\" not defined.");
      return attributes1;
    }
    //setting attributeNames to null will cause all attributes for a subject to be returned
    String[] attributeNames = null;
    Set attributeNameSet = this.getAttributes();
    if (attributeNameSet.size() == 0) {
      attributeNames = null;
    } else {
      attributeNames = new String[attributeNameSet.size()];
      int i = 0;
      for (Iterator it = attributeNameSet.iterator(); it.hasNext(); attributeNames[i++] = (String) it
          .next())
        ;
    }
    try {
      Attributes ldapAttributes = getLdapUnique(search, subject.getId(), attributeNames);
      for (NamingEnumeration e = ldapAttributes.getAll(); e.hasMore();) {
        Attribute attr = (Attribute) e.next();
        String name1 = attr.getID();
        Set<String> values = new HashSet<String>();
        for (NamingEnumeration en = attr.getAll(); en.hasMore();) {
          Object value = en.next();
          values.add(value.toString());
        }
        attributes1.put(name1, values);
      }
      subject.setAttributes(attributes1);
    } catch (NamingException ex) {
      throw new SourceUnavailableException("LDAP Naming Except: " + ex.getMessage(), ex);
    }
    return attributes1;
  }

  /**
   * 
   * @param search
   * @param searchValue
   * @param attributeNames
   * @return naming enumeration
   */
  protected NamingEnumeration getLdapResults(Search search, String searchValue,
      String[] attributeNames) {
    return getLdapResults(search, searchValue, attributeNames, false);
  }

  /**
   * 
   * @param search
   * @param searchValue
   * @param attributeNames
   * @return naming enumeration
   */
  protected NamingEnumeration getLdapResults(Search search, String searchValue,
      String[] attributeNames, boolean firstPageOnly) {
    
    //if this is a search and not by id or identifier, strip out the status part
    boolean subjectStatusQuery = StringUtils.equals("search", search.getSearchType());
    if (subjectStatusQuery) {
      SubjectStatusResult subjectStatusResult = null;
      
      //see if we are doing status
      SubjectStatusProcessor subjectStatusProcessor = new SubjectStatusProcessor(searchValue, this.getSubjectStatusConfig());
      subjectStatusResult = subjectStatusProcessor.processSearch();

      //strip out status parts
      searchValue = subjectStatusResult.getStrippedQuery();
    }      
    
    DirContext context = null;
    NamingEnumeration results = null;
    String filter = search.getParam("filter");
    if (filter == null) {
      log.error("Search filter not found for search type:  " + search.getSearchType());
      return results;
    }
    
    filter = filter.replaceAll("%TERM%", escapeSearchFilter(searchValue));
    String base = search.getParam("base");
    if (base == null) {
      base = "";
      log.error("Search base not found for:  " + search.getSearchType()
          + ". Using base \"\" ");

    }
    int scopeNum = -1;
    String scope = search.getParam("scope");
    if (scope != null) {
      scopeNum = getScope(scope);
    }
    if (scopeNum == -1) {
      scopeNum = SearchControls.SUBTREE_SCOPE;
      log.error("Search scope not found for: " + search.getSearchType()
          + ". Using scope SUBTREE_SCOPE.");
    }
    log.debug("searchType: " + search.getSearchType() + " filter: " + filter + " base: "
        + base + " scope: " + scope);
    try {
      context = new InitialDirContext(this.environment);
      SearchControls constraints = new SearchControls();
      
      if ((firstPageOnly && this.maxPage != null) || this.maxResults != null) {
        int pagesize = (firstPageOnly && this.maxPage != null) ? (this.maxPage+1) : -1;
        if (pagesize == -1) {
          pagesize = this.maxResults + 1;
        } else if (this.maxResults != null){
          pagesize = Math.min(pagesize, this.maxResults+1);
        }
        constraints.setCountLimit(pagesize);
      }

      constraints.setSearchScope(scopeNum);
      constraints.setReturningAttributes(attributeNames);
      results = context.search(base, filter, constraints);
    } catch (AuthenticationException ex) {
      throw new SourceUnavailableException("Ldap Authentication Exception: " + ex.getMessage(), ex);
    } catch (NamingException ex) {
      throw new SourceUnavailableException("Ldap NamingException: " + ex.getMessage(), ex);

    } finally {
      if (context != null) {
        try {
          context.close();
        } catch (NamingException ne) {
          // squelch, since it is already closed
        }
      }
    }
    return results;

  }

  /**
   * 
   * @param search
   * @param searchValue
   * @param attributeNames
   * @return attributes
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  protected Attributes getLdapUnique(Search search, String searchValue,
      String[] attributeNames) throws SubjectNotFoundException, SubjectNotUniqueException {
    Attributes attributes1 = null;
    NamingEnumeration results = getLdapResults(search, searchValue, attributeNames);

    try {
      if (results == null || !results.hasMore()) {
        String errMsg = "No results: " + search.getSearchType() + " filter:"
            + search.getParam("filter") + " searchValue: " + searchValue;
        throw new SubjectNotFoundException(errMsg);
      }

      SearchResult si = (SearchResult) results.next();
      attributes1 = si.getAttributes();
      if (results.hasMore()) {
        si = (SearchResult) results.next();
        String errMsg = "Search is not unique:" + si.getName() + "\n";
        throw new SubjectNotUniqueException(errMsg);
      }
    } catch (NamingException ex) {
      throw new SourceUnavailableException("Ldap NamingException: " + ex.getMessage(), ex);
    }
    return attributes1;
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
    escapedStr = escapedStr.replaceAll("\\\\", "\\\\5c");
    // We want people to be able to use wildcards.
    // escapedStr = escapedStr.replaceAll("\\*","\\\\2a");
    escapedStr = escapedStr.replaceAll("\\(", "\\\\28");
    escapedStr = escapedStr.replaceAll("\\)", "\\\\29");
    escapedStr = escapedStr.replaceAll("\\" + Character.toString('\u0000'), "\\\\00");
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
    Properties props = this.getInitParams();
    String dbUrl = props.getProperty("PROVIDER_URL");
    String dbUser = props.getProperty("SECURITY_PRINCIPAL");
    String dbResult = dbUser + "@" + dbUrl;

    String message = "sources.xml jndi source id:   " + this.getId() + ": " + dbResult;
    return message;

  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubject(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubject(id1, true);
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubjectByIdentifier(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    return this.getSubjectByIdentifier(id1, true);
  }

  /**
   * max Page size
   * @return the maxPage
   */
  public Integer getMaxPage() {
    return this.maxPage;
  }

}
