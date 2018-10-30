/**
 * Copyright 2018 Internet2
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
package edu.internet2.middleware.grouper.app.loader.ldap;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.ldap.LdapEntry;

/**
 * @author shilen
 */
public class LdapResultsTransformationInput {
  
  private Map<String, List<String>> membershipResults;
  
  private Map<String, String> groupNameToDisplayName;
  
  private Map<String, String> groupNameToDescription;
  
  private List<LdapEntry> ldapSearchResults;
  
  /**
   * These are the original group name to subject ids results that would be returned if this transformation is not done.
   * This takes into account all other jexl changes.
   * @return the membershipResults
   */
  public Map<String, List<String>> getMembershipResults() {
    return membershipResults;
  }


  /**
   * @param membershipResults the membershipResults to set
   * @return this
   */
  public LdapResultsTransformationInput setMembershipResults(Map<String, List<String>> membershipResults) {
    this.membershipResults = membershipResults;
    return this;
  }

  /**
   * These are the original group name to display name mappings
   * @return the groupNameToDisplayName
   */
  public Map<String, String> getGroupNameToDisplayName() {
    return groupNameToDisplayName;
  }


  /**
   * @param groupNameToDisplayName the groupNameToDisplayName to set
   * @return this
   */
  public LdapResultsTransformationInput setGroupNameToDisplayName(Map<String, String> groupNameToDisplayName) {
    this.groupNameToDisplayName = groupNameToDisplayName;
    return this;
  }

  /**
   * These are the original group name to description mappings
   * @return the groupNameToDescription
   */
  public Map<String, String> getGroupNameToDescription() {
    return groupNameToDescription;
  }


  /**
   * @param groupNameToDescription the groupNameToDescription to set
   * @return this
   */
  public LdapResultsTransformationInput setGroupNameToDescription(Map<String, String> groupNameToDescription) {
    this.groupNameToDescription = groupNameToDescription;
    return this;
  }

  /**
   * Contains the data directly out of LDAP
   * @return the ldapSearchResults
   */
  public List<LdapEntry> getLdapSearchResults() {
    return ldapSearchResults;
  }
  
  /**
   * @param ldapSearchResults the ldapSearchResults to set
   * @return this
   */
  public LdapResultsTransformationInput setLdapSearchResults(List<LdapEntry> ldapSearchResults) {
    this.ldapSearchResults = ldapSearchResults;
    return this;
  }
}