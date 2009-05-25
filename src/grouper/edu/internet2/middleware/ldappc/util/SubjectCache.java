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
 * $Id: SubjectCache.java,v 1.6 2009-05-25 20:40:36 tzeller Exp $
 */
package edu.internet2.middleware.ldappc.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.subject.Subject;

/**
 * Cache subjects retrieved from subject sources to help with performance issues.
 */
public class SubjectCache {

  private static final Logger LOG = GrouperUtil.getLogger(SubjectCache.class);

  /**
   * Hash table default estimate.
   */
  private static final int DEFAULT_HASH_ESTIMATE = 25000;

  /**
   * Nested table mapping source ID to table mapping subject ID to DN.
   */
  private Map<String, Hashtable<String, Name>> subjectIdToDnTables = new HashMap<String, Hashtable<String, Name>>();

  /**
   * Count of subject ID's looked up.
   */
  private int subjectIdLookups;

  /**
   * Count of subject ID table hits.
   */
  private int subjectIdTableHits;

  private Provisioner provisioner;

  public SubjectCache(Provisioner provisioner) {

    this.provisioner = provisioner;

    //
    // Initialize the hash tables mapping between RDN and subject ID.
    // Use the estimate in the config file if present
    //
    Map<String, Integer> estimates = provisioner.getConfiguration()
        .getSourceSubjectHashEstimates();
    for (String source : provisioner.getConfiguration().getSourceSubjectLdapFilters()
        .keySet()) {
      int estimate = DEFAULT_HASH_ESTIMATE;
      if (estimates.get(source) != null) {
        estimate = estimates.get(source);
      }

      if (subjectIdToDnTables.get(source) == null) {
        subjectIdToDnTables.put(source, new Hashtable<String, Name>(estimate));
      } else {
        subjectIdToDnTables.get(source).clear();
      }
    }
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
  public Name findSubjectDn(Member member) throws NamingException, LdappcException {

    // source and subject identifiers
    String sourceId = member.getSubjectSourceId();

    // return null if we aren't provisioning member groups and source is g:gsa
    if (sourceId.equals("g:gsa")
        && (!provisioner.getConfiguration().getProvisionMemberGroups())) {
      return null;
    }

    String subjectIdentifier = member.getSubjectId();

    // use cache
    if (subjectIdToDnTables.get(sourceId) == null) {
      subjectIdToDnTables.put(sourceId,
          new Hashtable<String, Name>(DEFAULT_HASH_ESTIMATE));
    } else {
      Name subjectDn = subjectIdToDnTables.get(sourceId).get(subjectIdentifier);
      if (subjectDn != null) {
        subjectIdTableHits++;
        LOG.debug("cache found dn '{}' for sourceId '{}' subjectId '{}'", new Object[] {
            subjectDn, sourceId, subjectIdentifier });
        return subjectDn;
      }
    }

    // determine filter, use built-in for g:gsa source
    LdapSearchFilter filter = null;

    if (sourceId.equals("g:gsa")) {

      Name groupDN = provisioner.calculateGroupDn(member.toGroup());
      filter = new LdapSearchFilter(groupDN.toString(), SearchControls.OBJECT_SCOPE,
          "(objectclass=*)");

    } else {

      //
      // Get the LDAP search filter for the source
      //
      filter = provisioner.getConfiguration().getSourceSubjectLdapFilter(sourceId);
      if (filter == null) {
        throw new LdappcException("Ldap search filter not found using sourceId '"
            + sourceId + "'");
      }

      //
      // Get the source name attribute
      //
      String sourceNameAttr = provisioner.getConfiguration()
          .getSourceSubjectNamingAttribute(sourceId);
      if (sourceNameAttr == null) {
        throw new LdappcException("Subject source [ " + sourceId
            + " ] does not identify a source subject naming attribute");
      }

      //
      // Get the subject's name attribute value
      //
      if (!sourceNameAttr.equals("id")) {
        LOG.debug("get source attribute '{}' for subject '{}'", sourceNameAttr,
            subjectIdentifier);
        subjectIdentifier = member.getSubject().getAttributeValue(sourceNameAttr);
        if (subjectIdentifier == null) {
          throw new LdappcException("Subject " + subjectIdentifier
              + " ] has no value for attribute [ " + sourceNameAttr + " ]");
        }
      }
    }

    // perform ldap search
    Name subjectDn = findSubjectDn(filter, subjectIdentifier);

    // add to cache
    subjectIdToDnTables.get(sourceId).put(subjectIdentifier, subjectDn);

    LOG.debug("search found dn '{}' for sourceId '{}' subjectId '{}'", new Object[] {
        subjectDn, sourceId, subjectIdentifier });

    return subjectDn;
  }

  /**
   * Return the DN for the given subject identifier using the supplied filter object.
   * 
   * @param filter
   *          the filter appropriate for the subject's source
   * @param subjectIdentifier
   *          the identifier of the subject
   * @return the subject's DN as a Name
   * @throws NamingException
   *           thrown if an ldap error occurs
   * @throws LdappcException
   *           thrown if the search does not return exactly 1 result
   */
  private Name findSubjectDn(LdapSearchFilter filter, String subjectIdentifier)
      throws NamingException, LdappcException {

    subjectIdLookups++;

    // base
    NameParser parser = provisioner.getContext().getNameParser(LdapUtil.EMPTY_NAME);
    Name baseName = parser.parse(filter.getBase());

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
    NamingEnumeration namingEnum = provisioner.getContext().search(baseName, filterExpr,
        filterArgs, searchControls);

    //
    // If no entries where found, throw an exception
    //
    if (!namingEnum.hasMore()) {
      throw new LdappcException("Subject not found using " + msg);
    }

    //
    // Get the first result.
    //
    SearchResult searchResult = (SearchResult) namingEnum.next();

    //
    // After getting the first result, if there are more throw an exception
    // as the search result was not unique
    //
    if (namingEnum.hasMore()) {
      throw new LdappcException("Multiple entries found using " + msg);
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
    Name subjectDn = parser.parse(searchResult.getName());
    subjectDn = subjectDn.addAll(0, baseName);

    return subjectDn;
  }
}
