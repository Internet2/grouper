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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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
 */

package edu.internet2.middleware.ldappc;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.ldappc.util.LdapSearchFilter;

public interface LdappcConfig {

  /**
   * Indicates creating either a flat or bushy Group DN structure.
   */
  public static enum GroupDNStructure {
    flat, bushy
  };

  /**
   * Grouper group id attribute.
   */
  public static final String GROUPER_ID_ATTRIBUTE = "id";

  /**
   * Grouper group name attribute.
   */
  public static final String GROUPER_NAME_ATTRIBUTE = "name";

  /**
   * Get the group hash table size estimate.
   * 
   * @return Size of hash table estimate for holding cached subject data.
   */
  public int getGroupHashEstimate();

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group attribute
   * name/value pairs for creating matching queries. The key for the map is the attribute
   * name, and the value is a {@link java.util.Set} of the attribute value strings.
   * 
   * @return Map of the attribute name to the Set of values.
   */
  public Map getGroupAttrMatchingQueries();

  /**
   * This method returns a possibly empty {@link java.util.Set} of the Group stems for
   * creating subordinate stem queries.
   * 
   * @return Set of Group stem strings.
   */
  public Set getGroupSubordinateStemQueries();
  
  /**
   * This method returns a possibly empty {@link java.util.Set} of the attribute resolver
   * data connector ids for returning the groups to be provisioned.
   * 
   * @return Set of Group stem strings.
   */
  public Set<String> getResolverQueries();

  /**
   * This returns the defined Group DN structure.
   * 
   * @return Group DN structure.
   */
  public GroupDNStructure getGroupDnStructure();

  /**
   * This returns the DN of the root entry being used for Group DNs.
   * 
   * @return DN of the root entry
   */
  public String getGroupDnRoot();

  /**
   * Returns the object class for the Group entry.
   * 
   * @return Object class for a Group
   */
  public String getGroupDnObjectClass();

  /**
   * Returns the RDN attribute name for the Group entry.
   * 
   * @return RDN attribute name for the Group entry
   */
  public String getGroupDnRdnAttribute();

  /**
   * This returns the Grouper group attribute whose value is the Group RND value.
   * 
   * @return The Grouper group attribute whose value is the RND value
   */
  public String getGroupDnGrouperAttribute();

  /**
   * This returns a boolean indicating if Member Groups list is to be maintained.
   * 
   * @return true if the Groups to which a Member belongs are listed, and false otherwise
   * 
   * @see #getMemberGroupsListAttribute()
   * @see #getMemberGroupsNamingAttribute()
   */
  public boolean isMemberGroupsListed();

  /**
   * This returns the object class the Member's LDAP entry must have to support the member
   * groups list attribute. If {@link #isMemberGroupsListed()} returns false, the value
   * defined here has no meaning.
   * 
   * @return Object class name or <code>null</code> if not defined.
   */
  public String getMemberGroupsListObjectClass();

  /**
   * This gets the LDAP entry attribute name containing the list of Groups to which a
   * Member belongs.
   * 
   * @return Name of the LDAP entry attribute containing the list of Groups to which a
   *         Member belongs.
   */
  public String getMemberGroupsListAttribute();

  /**
   * This gets the value to store in the member groups list attribute if there are no
   * Groups to store there.
   * 
   * @return String to place in the member groups list attribute if no Groups are found to
   *         store there, or <code>null</code> if not defined.
   */
  // public String getMemberGroupsListEmptyValue();
  /**
   * Directory for the membership updates temporary file.
   * 
   * The default value is null, causing the files to be placed in the current directory.
   * 
   * @return the temporary directory.
   */
  public String getMemberGroupsListTemporaryDirectory();

  /**
   * This gets the Grouper Group naming attribute to be used when creating the list of
   * Groups to which Member belongs.
   * 
   * @return Grouper Group naming attribute to be used to create the list of Groups to
   *         which a Member belongs.
   */
  public String getMemberGroupsNamingAttribute();

  /**
   * This returns a boolean indicating if a Group Members LDAP entry DN list is to be
   * maintained on the Groups LDAP entry.
   * 
   * @return true if the DNs of Members which belong to the Group are listed, and false
   *         otherwise
   * 
   * @see #getGroupMembersDnListObjectClass()
   * @see #getGroupMembersDnListAttribute()
   */
  public boolean isGroupMembersDnListed();

  /**
   * This gets the object class to be added to the Group LDAP entry so support the Group
   * members Dn list attribute. If {@link #isGroupMembersDnListed()} returns false, the
   * value defined here has no meaning.
   * 
   * @return Group members Dn list object class, or <code>null</code> if not defined.
   */
  public String getGroupMembersDnListObjectClass();

  /**
   * This gets the LDAP entry attribute containing the list of Member DNs which belong to
   * the Group. If {@link #isGroupMembersDnListed()} returns false, the value defined here
   * has no meaning.
   * 
   * @return LDAP entry attribute containing the list of Members DNs which belong to the
   *         Group.
   */
  public String getGroupMembersDnListAttribute();

  /**
   * This gets the value to store in the group member DN list attribute if there are no
   * member DNs to store there.
   * 
   * @return String to place in the group members DN list attribute if no DNs are found to
   *         store there, or <code>null</code> if not defined.
   */
  public String getGroupMembersDnListEmptyValue();

  /**
   * This returns a boolean indicating if a Group Members name list is to be maintained on
   * the Groups LDAP entry.
   * 
   * @return true if the names of Members which belong to the Group are listed, and false
   *         otherwise
   * 
   * @see #getGroupMembersNameListAttribute()
   * @see #getGroupMembersNameListNamingAttribute(String)
   */
  public boolean isGroupMembersNameListed();

  /**
   * This gets the object class to be added to the Group LDAP entry so support the Group
   * members name list attribute.
   * 
   * @return Group members name list LDAP object class, or <code>null</code> if not
   *         defined.
   */
  public String getGroupMembersNameListObjectClass();

  /**
   * This gets the LDAP entry attribute containing the list of Member names which belong
   * to the Group.
   * 
   * @return LDAP entry attribute containing the list of Member names which belong to the
   *         Group.
   */
  public String getGroupMembersNameListAttribute();

  /**
   * This gets the value to store in the group member name list attribute if there are no
   * member names to store there.
   * 
   * @return String to place in the group members name list attribute if no names are
   *         found to store there, or <code>null</code> if not defined.
   */
  public String getGroupMembersNameListEmptyValue();

  /**
   * This method returns the Subject attribute name for creating the Member's name for the
   * given source name.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject attribute name for the source, or <code>null</code> if the source was
   *         not found.
   */
  public String getGroupMembersNameListNamingAttribute(String source);

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group members name
   * list source to subject attribute mapping used to determine a members name.
   * 
   * @return Map of Source names to Subject attribute names.
   */
  public Map getGroupMembersNameListNamingAttributes();

  /**
   * This gets the LDAP objectclass the Group entry must have to support the Grouper
   * attribute to LDAP attribute mapping.
   * 
   * @return LDAP object class or <code>null</code> if not defined.
   */
  public Set<String> getGroupAttributeMappingObjectClass();

  /**
   * This method returns a possibly empty {@link java.util.Map} of the Group attribute
   * name to LDAP attribute name mapping.
   * 
   * @return Map of Group attribute names to LDAP attribute names.
   */
  public Map<String, List<String>> getGroupAttributeMapping();

  /**
   * This gets the value to store in the ldap attribute if there are no grouper attribute
   * values to store there.
   * 
   * @param ldapAttribute
   *          Name of the Ldap Attribute
   * @return String to place in the ldap attribute if no Grouper attribute values are
   *         found to store there, or <code>null</code> if not defined.
   */
  public String getGroupAttributeMappingLdapEmptyValue(String ldapAttribute);

  /**
   * Returns true if the group should be created without members followed by a
   * modification which adds member attributes, defaults to false.
   * 
   * @return true if the group should be created without members followed by a
   *         modification which adds member attributes, defaults to false
   * 
   */
  public boolean getCreateGroupThenModifyMembers();

  /**
   * This returns a {@link java.util.Map} of the Source to Subject naming attribute for
   * the Source Subject identifiers.
   * 
   * @return Map of Source Subject naming attribute name/value pairs.
   */
  public Map getSourceSubjectNamingAttributes();

  /**
   * This returns the Subject naming attribute for the given Source for the Source Subject
   * identifiers.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject naming attribute name or <code>null</code> if the Source is not found
   */
  public String getSourceSubjectNamingAttribute(String source);

  /**
   * This returns a {@link java.util.Map} of the Source to Subject LDAP filters for the
   * Source Subject identifiers.
   * 
   * @return Map of Source Subject LDAP filter name/value pairs.
   */
  public Map<String, LdapSearchFilter> getSourceSubjectLdapFilters();

  /**
   * This returns the Subject LDAP filter for the given Source for the Source Subject
   * identifiers.
   * 
   * @param source
   *          Source name
   * 
   * @return Subject LDAP filter or <code>null</code> if the Source is not found
   */
  public LdapSearchFilter getSourceSubjectLdapFilter(String source);

  /**
   * This returns a {@link java.util.Map} of size estimate for a hash table containing the
   * subjects in this source that will be provisioned.
   * 
   * @return size estimate for a hash table.
   */
  public Map<String, Integer> getSourceSubjectHashEstimates();

  /**
   * This returns the size estimate for a hash table containing the subjects in this
   * source that will be provisioned.
   * 
   * @param source
   *          Source name
   * @return size estimate for a hash table.
   */
  public int getSourceSubjectHashEstimate(String source);

  /**
   * This method returns a {@link java.util.Hashtable} of the LDAP parameters defined to
   * create the {@link javax.naming.InitialContext}. Each of the parameter names from the
   * configuration file that match, ignoring case, a constant name from
   * {@link javax.naming.ldap.LdapContext} have been converted to the actual value of the
   * <code>LdapContext</code> constant. This allows the returned <code>Hashtable</code> to
   * be used directly when creating an initial context.
   * 
   * @return Hashtable with the LDAP initial context parameters.
   */
  public Hashtable getLdapContextParameters();

  /**
   * Returns true if member groups should be provisioned as members, false otherwise. True
   * by default. A member group is a group which is a member of another group. This method
   * effectively replaces the "g:gsa" source-subject-identifier.
   * 
   * @return true if member groups should be provisioned as members.
   */
  public boolean getProvisionMemberGroups();

  /**
   * Returns true if groups should be provisioned in two steps. The first step provisions
   * all groups without any members. The second step provisions all groups with members.
   * If false, LDAPPC will log errors when provisioning groups which have other groups as
   * members which have not been provisioned yet. True by default.
   * 
   * @return true if groups should be provisioned in two steps.
   */
  public boolean getProvisionGroupsTwoStep();

  /**
   * Returns true if a group's attribute modifications should be performed in one LDAP
   * operation. If false, each group attribute modification is performed as a separate
   * LDAP operation. True by default.
   * 
   * @return true if attribute modifications should be bundled
   */
  public boolean getBundleModifications();

  /**
   * This gets the LDAP objectclass the Group entry must have to support the
   * AttributeResolver attribute to LDAP attribute mapping.
   * 
   * @return LDAP object class or <code>null</code> if not defined.
   */
  public Set<String> getAttributeResolverMappingObjectClass();

  /**
   * This method returns a possibly empty {@link java.util.Map} of the AttributeResolver
   * attribute name to LDAP attribute name mapping.
   * 
   * @return Map of AttributeResolver attribute names to LDAP attribute names.
   */
  public Map<String, List<String>> getAttributeResolverMapping();

  /**
   * This gets the value to store in the ldap attribute if there are no AttributeResolver
   * attribute values to store there.
   * 
   * @param ldapAttribute
   *          Name of the Ldap Attribute
   * @return String to place in the ldap attribute if no AttributeResolver attribute
   *         values are found to store there, or <code>null</code> if not defined.
   */
  public String getAttributeResolverMappingLdapEmptyValue(String ldapAttribute);

  /**
   * Returns true if the
   * {@link edu.internet2.middleware.ldappc.util.RangeSearchResultHandler} should be used
   * to process attributes returned from LDAP searches. See
   * http://code.google.com/p/vt-middleware/wiki/vtldapAD
   * 
   * @return true if "range" attributes should be incrementally retrieved
   */
  public boolean useRangeSearchResultHandler();

  /**
   * Returns true if member groups should be provisioned even if they are not in the set
   * of groups to be provisioned, which reproduces the behavior of LDAPPC prior to 1.5.0.
   * Defaults to false, which means that member groups are provisioned only if they are in
   * the set of groups to be provisioned.
   * 
   * Ignored unless {@link #getProvisionMemberGroups()} is true.
   * 
   * @return true if non-selected member groups should be provisioned
   */
  public boolean getProvisionMemberGroupsIgnoreQueries();
}
