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
 * $Id: SubjectCache.java,v 1.5 2009-05-15 18:33:29 tzeller Exp $
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

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.subject.Source;
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
   * This returns the DN of the subject. This uses the query filter defined for the
   * <code>sourceId</code> in the configuration to search for the <code>subject</code>.
   * 
   * @param ctx
   *          Ldap context
   * @param configuration
   *          Provisioner configuration
   * @param subject
   *          Subject
   * @return Subject's DN
   * @throws javax.naming.NamingException
   *           thrown if a Naming exception occurs
   * @throws EntryNotFoundException
   *           thrown if the entry could not be located
   * @throws MultipleEntriesFoundException
   *           thrown if the search found more than one entry
   */
  public Name findSubjectDn(Subject subject) throws NamingException, LdappcException {
    //
    // Get the subject's source and source id
    //
    Source source = subject.getSource();
    if (source == null) {
      throw new LdappcException("Subject [ " + getSubjectData(subject)
          + " ] has a null source");
    }

    String sourceId = source.getId();

    if (sourceId.equals("g:gsa")) {
      LOG.debug("g:gsa shortcut");

    }

    //
    // Get the source name attribute
    //
    String sourceNameAttr = provisioner.getConfiguration()
        .getSourceSubjectNamingAttribute(sourceId);
    if (sourceNameAttr == null) {
      throw new LdappcException("Subject [ " + getSubjectData(subject) + " ] source [ "
          + sourceId + " ] does not identify a source subject naming attribute");
    }

    //
    // Get the subject's name attribute value
    //
    LOG.debug("get attribute {} for subject {}", sourceNameAttr, subject);
    String subjectIdentifier = subject.getAttributeValue(sourceNameAttr);
    if (subjectIdentifier == null) {
      throw new LdappcException("Subject [ " + getSubjectData(subject, true)
          + " ] has no value for attribute [ " + sourceNameAttr + " ]");
    }

    return findSubjectDn(sourceId, subjectIdentifier);
  }

  /**
   * This returns the DN of the identified subject. This uses the query filter defined for
   * the <code>sourceId</code> in the configuration to search for the subject identified
   * by <code>subjectIdentifier</code>. It will return <code>null</code> if the subject is
   * not found or multiple entries match the search.
   * 
   * @param ldapCtx
   *          Ldap context
   * @param configuration
   *          Provisioner configuration
   * @param sourceId
   *          Source Id of the subject
   * @param subjectIdentifier
   *          Identifier string of the subject
   * @return Subject's DN
   * @throws javax.naming.NamingException
   *           thrown if a Naming exception occurs
   * @throws EntryNotFoundException
   *           thrown if the entry could not be located
   * @throws MultipleEntriesFoundException
   *           thrown if the search found more than one entry
   */
  public Name findSubjectDn(String sourceId, String subjectIdentifier)
      throws NamingException, LdappcException {
    //
    // Initialize error message suffix
    //
    String errorSuffix = "[ subject id = " + subjectIdentifier + " ][ source = "
        + sourceId + " ]";

    //
    // Get the LDAP search filter for the source
    //
    LdapSearchFilter filter = provisioner.getConfiguration().getSourceSubjectLdapFilter(
        sourceId);
    if (filter == null) {
      throw new LdappcException("Ldap search filter not found using " + errorSuffix);
    }

    //
    // Update the error suffix
    //
    errorSuffix += "[ filter = " + filter + " ]";

    //
    // Build all of pieces needed to search
    //
    NameParser parser = provisioner.getContext().getNameParser(LdapUtil.EMPTY_NAME);
    Name baseName = parser.parse(filter.getBase());

    subjectIdLookups++;
    Name subjectDn = null;
    if (subjectIdToDnTables.get(sourceId) == null) {
      subjectIdToDnTables.put(sourceId,
          new Hashtable<String, Name>(DEFAULT_HASH_ESTIMATE));
    } else {
      subjectDn = subjectIdToDnTables.get(sourceId).get(subjectIdentifier);
      if (subjectDn != null) {
        subjectIdTableHits++;
        return subjectDn;
      }
    }
    String filterExpr = filter.getFilter();

    Object[] filterArgs = new Object[] { subjectIdentifier };

    //
    // Update the error suffix
    //
    errorSuffix += "[ filterArgs = " + Arrays.asList(filterArgs) + " ]";

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(filter.getScope());
    searchControls.setReturningAttributes(new String[] {});

    //
    // As only 1 is wanted, if two are found the subjectName value
    // wasn't unique
    //
    searchControls.setCountLimit(2);

    //
    // Perform the search
    //
    LOG
        .debug("search base '{}' filter '{}' filterArgs '{}' subjectId '{}'",
            new Object[] { baseName, filterExpr, Arrays.asList(filterArgs),
                subjectIdentifier });
    NamingEnumeration namingEnum = provisioner.getContext().search(baseName, filterExpr,
        filterArgs, searchControls);

    //
    // If no entries where found, throw an exception
    //
    if (!namingEnum.hasMore()) {
      throw new LdappcException("Subject not found using " + errorSuffix);
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
      throw new LdappcException("Multiple entries found using " + errorSuffix);
    }

    //
    // If name is NOT relative, throw an exception
    //
    if (!searchResult.isRelative()) {
      throw new LdappcException("Unable to resolve the reference found using "
          + errorSuffix);
    }

    //
    // Build the subject's DN
    //
    subjectDn = parser.parse(searchResult.getName());
    subjectDn = subjectDn.addAll(0, baseName);

    subjectIdToDnTables.get(sourceId).put(subjectIdentifier, subjectDn);

    return subjectDn;
  }
}
