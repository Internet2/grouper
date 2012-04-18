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
/*
 * Copyright (c) 2007 Kathryn Huxtable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * $Id: SubjectCache.java,v 1.15 2009-11-13 13:38:32 tzeller Exp $
 */
package edu.internet2.middleware.ldappc.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter.OnNotFound;
import edu.internet2.middleware.subject.Subject;
import edu.vt.middleware.ldap.SearchFilter;

/**
 * Cache subjects retrieved from subject sources to help with performance issues.
 */
public class SubjectCache {

  private static final Logger LOG = LoggerFactory.getLogger(SubjectCache.class);

  /**
   * Hash table default estimate.
   */
  private static final int DEFAULT_HASH_ESTIMATE = 25000;

  /**
   * Nested table mapping source ID to table mapping subject ID to DN.
   */
  private Map<String, Hashtable<String, Set<Name>>> subjectIdToDnTables;

  /**
   * Count of subject ID's looked up.
   */
  private int subjectIdLookups;

  /**
   * Count of subject ID table hits.
   */
  private int subjectIdTableHits;

  private Ldappc ldappc;

  public SubjectCache(Ldappc ldappc) {

    this.ldappc = ldappc;

    init();

  }

  /**
   * Return the count of subject ID lookups.
   * 
   * @return the subjectIDLookups
   */
  public int getSubjectIdLookups() {
    return subjectIdLookups;
  }

  /**
   * Return the count of subject ID table hits.
   * 
   * @return the subjectIDTableHits
   */
  public int getSubjectIdTableHits() {
    return subjectIdTableHits;
  }

  /**
   * Returns subject data string without attributes.
   * 
   * @param subject
   *          Subject
   * @return subject data string
   */
  public String getSubjectData(Subject subject) {
    return getSubjectData(subject, false);
  }

  /**
   * Returns subject data string.
   * 
   * @param subject
   *          Subject
   * @param attributes
   *          include attribute values
   * @return subject data string
   */
  public String getSubjectData(Subject subject, boolean attributes) {
    String subjData = "null";
    if (subject != null) {
      subjData = "[ NAME = " + subject.getName() + " ][ ID = " + subject.getId() + " ]";
      if (attributes) {
        subjData += "[ ATTRIBUTES = " + subject.getAttributes() + " ]";
      }
    }
    return subjData;
  }

  /**
   * Returns the member's DN.
   * 
   * @param member
   *          the member
   * @return the member's DN
   * @throws NamingException
   *           if an ldap error occurs
   * @throws LdappcException
   *           if there is a configuration error or if exactly one DN cannot be determined
   */
  public Set<Name> findSubjectDn(Member member) throws NamingException, LdappcException {

    // source and subject identifiers
    String sourceId = member.getSubjectSourceId();

    // return null if we aren't provisioning member groups and source is g:gsa
    if (sourceId.equals(SubjectFinder.internal_getGSA().getId())
        && (!ldappc.getConfig().getProvisionMemberGroups())) {
      return null;
    }

    // use cache
    if (subjectIdToDnTables.get(sourceId) == null) {
      subjectIdToDnTables.put(sourceId, new Hashtable<String, Set<Name>>(
          DEFAULT_HASH_ESTIMATE));
    } else {
      Set<Name> subjectDns = subjectIdToDnTables.get(sourceId).get(member.getSubjectId());
      if (subjectDns != null) {
        subjectIdTableHits++;
        LOG.debug("cache found dns '{}' for sourceId '{}' subjectId '{}'", new Object[] {
            subjectDns, sourceId, member.getSubjectId() });
        return subjectDns;
      }

    }

    //
    // Subject identifier used for lookup
    //
    String subjectIdentifier = null;

    //
    // Determine filter, use built-in for g:gsa source
    //
    LdapSearchFilter filter = null;

    if (sourceId.equals(SubjectFinder.internal_getGSA().getId())) {
      Group group = member.toGroup();
      Name groupDN = ldappc.calculateGroupDn(group);
      subjectIdentifier = group.getName(); // for logging
      filter = new LdapSearchFilter(groupDN.toString(), SearchControls.OBJECT_SCOPE,
          "(objectclass=*)", OnNotFound.warn, false);

    } else {

      //
      // Get the LDAP search filter for the source
      //
      filter = ldappc.getConfig().getSourceSubjectLdapFilter(sourceId);
      if (filter == null) {
        throw new LdappcException("Ldap search filter not found using sourceId '"
            + sourceId + "'");
      }

      //
      // Get the source name attribute
      //
      String sourceNameAttr = ldappc.getConfig()
          .getSourceSubjectNamingAttribute(sourceId);
      if (sourceNameAttr == null) {
        throw new LdappcException("Subject source [ " + sourceId
            + " ] does not identify a source subject naming attribute");
      }

      //
      // Get the subject's name attribute value
      //
      LOG.debug("get source attribute '{}' for subject '{}'", sourceNameAttr, member
          .getSubjectId());
      subjectIdentifier = member.getSubject().getAttributeValue(sourceNameAttr);
      // if "name" or "id" try the accessor methods
      if (subjectIdentifier == null) {
        if (sourceNameAttr.equalsIgnoreCase("id")) {
          subjectIdentifier = member.getSubject().getId();
        }
        if (sourceNameAttr.equalsIgnoreCase("name")) {
          subjectIdentifier = member.getSubject().getName();
        }
      }
      if (subjectIdentifier == null) {
        throw new LdappcException("Subject " + subjectIdentifier
            + " ] has no value for attribute [ " + sourceNameAttr + " ]");
      }
    }

    // perform ldap search
    Set<Name> subjectDns = findSubjectDn(filter, subjectIdentifier);
    if (subjectDns != null) {
      // add to cache
      if (subjectIdToDnTables.get(sourceId).get(member.getSubjectId()) == null) {
        subjectIdToDnTables.get(sourceId).put(member.getSubjectId(),
            new LinkedHashSet<Name>());
      }
      subjectIdToDnTables.get(sourceId).get(member.getSubjectId()).addAll(subjectDns);
      LOG.debug("search found dn '{}' for sourceId '{}' subjectId '{}'", new Object[] {
          subjectDns, sourceId, member.getSubjectId() });
    }

    return subjectDns;
  }

  /**
   * Return the DN for the given subject identifier using the supplied filter object.
   * Returns null if the subject cannot be found.
   * 
   * @param filter
   *          the filter appropriate for the subject's source
   * @param subjectIdentifier
   *          the identifier of the subject
   * @return the subject's DN as a Name
   * @throws NamingException
   *           thrown if an ldap error occurs
   * @throws LdappcException
   *           thrown if the search returns more than 1 object or if the object is
   *           relative
   */
  private Set<Name> findSubjectDn(LdapSearchFilter filter, String subjectIdentifier)
      throws NamingException, LdappcException {

    subjectIdLookups++;

    // base
    String baseName = filter.getBase();

    // filter
    String filterExpr = filter.getFilter();

    // filter args
    Object[] filterArgs = new Object[] { subjectIdentifier };

    // search control
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(filter.getScope());
    searchControls.setReturningAttributes(new String[] {});
    searchControls.setCountLimit(2);

    String msg = "search for subjectId '" + subjectIdentifier + "' base '" + baseName
        + "' filter '" + filterExpr + "' args " + Arrays.asList(filterArgs);

    //
    // Perform the search
    //
    LOG.debug(msg);
    Iterator<SearchResult> searchResults = null;
    try {
      SearchFilter searchFilter = new SearchFilter(filterExpr);
      searchFilter.setFilterArgs(filterArgs);
      searchResults = ldappc.getContext().search(LdapUtil.escapeForwardSlash(baseName),
          searchFilter, searchControls);
    } catch (NameNotFoundException e) {
      if (filter.getOnNotFound().equals(OnNotFound.fail)) {
        LOG.error("Subject not found using " + msg, e);
        throw new LdappcException(e);
      } else if (filter.getOnNotFound().equals(OnNotFound.warn)) {
        LOG.warn("Subject not found using " + msg);
      }
      return null;
    }

    //
    // If no entries where found, throw an exception - no return null
    //
    if (!searchResults.hasNext()) {
      if (filter.getOnNotFound().equals(OnNotFound.fail)) {
        LOG.error("Subject not found using " + msg);
        throw new LdappcException("Subject not found using " + msg);
      } else if (filter.getOnNotFound().equals(OnNotFound.warn)) {
        LOG.warn("Subject not found using " + msg);
      }
      return null;
    }

    Set<Name> subjectDns = new LinkedHashSet<Name>();
    while (searchResults.hasNext()) {
      //
      // Get the result.
      //
      SearchResult searchResult = searchResults.next();

      //
      // After getting the first result, if there are more throw an exception
      // as the search result was not unique, if we are not allowing multiple results
      //
      if (searchResults.hasNext()) {
        if (!filter.getMultipleResults()) {
          throw new LdappcException("Multiple entries found using " + msg);
        }
      }

      //
      // If name is NOT relative, throw an exception
      //
      if (!searchResult.isRelative()) {
        throw new LdappcException("Unable to resolve the reference found using " + msg);
      }

      //
      // Build the subject's DN
      //
      LOG.trace("found {}", searchResult.getName());
      Name subjectDn = new LdapName(LdapUtil.unescapeForwardSlash(searchResult.getName()));
      subjectDns.add(subjectDn);
    }

    return subjectDns;
  }

  /**
   * Initialize, or clear, the cache. Initialize the hash tables mapping between RDN and
   * subject ID. Use the estimate in the config file if present.
   */
  public void init() {

    subjectIdLookups = 0;
    subjectIdTableHits = 0;

    subjectIdToDnTables = new HashMap<String, Hashtable<String, Set<Name>>>();

    Map<String, Integer> estimates = ldappc.getConfig().getSourceSubjectHashEstimates();
    for (String source : ldappc.getConfig().getSourceSubjectLdapFilters().keySet()) {
      int estimate = DEFAULT_HASH_ESTIMATE;
      if (estimates.get(source) != null) {
        estimate = estimates.get(source);
      }

      if (subjectIdToDnTables.get(source) == null) {
        subjectIdToDnTables.put(source, new Hashtable<String, Set<Name>>(estimate));
      } else {
        subjectIdToDnTables.get(source).clear();
      }
    }
  }
}
