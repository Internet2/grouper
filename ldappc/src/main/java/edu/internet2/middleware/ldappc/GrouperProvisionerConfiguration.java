/*
 Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
 Copyright 2004-2006 The University Of Chicago
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import java.util.Map;
import java.util.Set;

/**
 * This interface defines the common configuration functionality required by the
 * Grouper provisioner.
 */
public interface GrouperProvisionerConfiguration extends
        ProvisionerConfiguration
{

    /**
     * Indicates creating a flat Group DN structure
     */
    public static final String GROUP_DN_FLAT = "flat";

    /**
     * Indicates creating a bushy Group DN structure
     */
    public static final String GROUP_DN_BUSHY = "bushy";

    /**
     * Grouper group id attribute
     */
    public static final String GROUPER_ID_ATTRIBUTE = "id";

    /**
     * Grouper group name attribute
     */
    public static final String GROUPER_NAME_ATTRIBUTE = "name";

    /**
     * This method returns a possibly empty {@link java.util.Map} of the Group
     * attribute name/value pairs for creating matching queries. The key for the
     * map is the attribute name, and the value is a {@link java.util.Set} of
     * the attribute value strings.
     * 
     * @return Map of the attribute name to the Set of values.
     */
    public Map getGroupAttrMatchingQueries();

    /**
     * This method returns a possibly empty {@link java.util.Set} of the Group
     * stems for creating subordinate stem queries.
     * 
     * @return Set of Group stem strings.
     */
    public Set getGroupSubordinateStemQueries();

    /**
     * This returns the defined Group DN structure.
     * 
     * @return Group DN structure, either {@link #GROUP_DN_FLAT} or
     *         {@link #GROUP_DN_BUSHY}
     */
    public String getGroupDnStructure();

    /**
     * This returns the DN of the root entry being used for Group DNs.
     * 
     * @return DN of the root entry
     */
    public String getGroupDnRoot();

    /**
     * Returns the object class for the Group entry
     * 
     * @return Object class for a Group
     */
    public String getGroupDnObjectClass();

    /**
     * Returns the RDN attribute name for the Group entry
     * 
     * @return RDN attribute name for the Group entry
     */
    public String getGroupDnRdnAttribute();

    /**
     * This returns the Grouper group attribute whose value is the Group RND
     * value.
     * 
     * @return The Grouper group attribute whose value is the RND value
     */
    public String getGroupDnGrouperAttribute();

    /**
     * This returns a boolean indicating if Member Groups list is to be
     * maintained
     * 
     * @return true if the Groups to which a Member belongs are listed, and
     *         false otherwise
     * 
     * @see #getMemberGroupsListAttribute()
     * @see #getMemberGroupsNamingAttribute()
     */
    public boolean isMemberGroupsListed();

    /**
     * This returns the object class the Member's LDAP entry must have to
     * support the member groups list attribute. If
     * {@link #isMemberGroupsListed()} returns false, the value defined here has
     * no meaning.
     * 
     * @return Object class name or <code>null</code> if not defined.
     */
    public String getMemberGroupsListObjectClass();

    /**
     * This gets the LDAP entry attribute name containing the list of Groups to
     * which a Member belongs.
     * 
     * @return Name of the LDAP entry attribute containing the list of Groups to
     *         which a Member belongs.
     */
    public String getMemberGroupsListAttribute();

    /**
     * This gets the value to store in the member groups list attribute if there
     * are no Groups to store there.
     * 
     * @return String to place in the member groups list attribute if no Groups
     *         are found to store there, or <code>null</code> if not defined.
     */
    public String getMemberGroupsListEmptyValue();

    /**
     * This gets the Grouper Group naming attribute to be used when creating the
     * list of Groups to which Member belongs.
     * 
     * @return Grouper Group naming attribute to be used to create the list of
     *         Groups to which a Member belongs.
     */
    public String getMemberGroupsNamingAttribute();

    /**
     * This returns a boolean indicating if a Group Members LDAP entry DN list
     * is to be maintained on the Groups LDAP entry.
     * 
     * @return true if the DNs of Members which belong to the Group are listed,
     *         and false otherwise
     * 
     * @see #getGroupMembersDnListObjectClass()
     * @see #getGroupMembersDnListAttribute()
     */
    public boolean isGroupMembersDnListed();

    /**
     * This gets the object class to be added to the Group LDAP entry so support
     * the Group members Dn list attribute. If {@link #isGroupMembersDnListed()}
     * returns false, the value defined here has no meaning.
     * 
     * @return Group members Dn list object class, or <code>null</code> if not
     *         defined.
     */
    public String getGroupMembersDnListObjectClass();

    /**
     * This gets the LDAP entry attribute containing the list of Member DNs
     * which belong to the Group. If {@link #isGroupMembersDnListed()} returns
     * false, the value defined here has no meaning.
     * 
     * @return LDAP entry attribute containing the list of Members DNs which
     *         belong to the Group.
     */
    public String getGroupMembersDnListAttribute();

    /**
     * This gets the value to store in the group member DN list attribute if
     * there are no member DNs to store there.
     * 
     * @return String to place in the group members DN list attribute if no DNs
     *         are found to store there, or <code>null</code> if not defined.
     */
    public String getGroupMembersDnListEmptyValue();

    /**
     * This returns a boolean indicating if a Group Members name list is to be
     * maintained on the Groups LDAP entry.
     * 
     * @return true if the names of Members which belong to the Group are
     *         listed, and false otherwise
     * 
     * @see #getGroupMembersNameListAttribute()
     * @see #getGroupMembersNameListNamingAttribute(String)
     */
    public boolean isGroupMembersNameListed();

    /**
     * This gets the object class to be added to the Group LDAP entry so support
     * the Group members name list attribute.
     * 
     * @return Group members name list LDAP object class, or <code>null</code>
     *         if not defined.
     */
    public String getGroupMembersNameListObjectClass();

    /**
     * This gets the LDAP entry attribute containing the list of Member names
     * which belong to the Group.
     * 
     * @return LDAP entry attribute containing the list of Member names which
     *         belong to the Group.
     */
    public String getGroupMembersNameListAttribute();

    /**
     * This gets the value to store in the group member name list attribute if
     * there are no member names to store there.
     * 
     * @return String to place in the group members name list attribute if no
     *         names are found to store there, or <code>null</code> if not
     *         defined.
     */
    public String getGroupMembersNameListEmptyValue();

    /**
     * This method returns the Subject attribute name for creating the Member's
     * name for the given source name.
     * 
     * @param source
     *            Source name
     * 
     * @return Subject attribute name for the source, or <code>null</code> if
     *         the source was not found.
     */
    public String getGroupMembersNameListNamingAttribute(String source);

    /**
     * This method returns a possibly empty {@link java.util.Map} of the Group
     * members name list source to subject attribute mapping used to determine a
     * members name.
     * 
     * @return Map of Source names to Subject attribute names.
     */
    public Map getGroupMembersNameListNamingAttributes();

    /**
     * This gets the LDAP objectclass the Group entry must have to support the
     * Grouper attribute to LDAP attribute mapping.
     * 
     * @return LDAP object class or <code>null</code> if not defined.
     */
    public String getGroupAttributeMappingObjectClass();

    /**
     * This method returns a possibly empty {@link java.util.Map} of the Group
     * attribute name to LDAP attribute name mapping.
     * 
     * @return Map of Group attribute names to LDAP attribute names.
     */
    public Map getGroupAttributeMapping();

    /**
     * This gets the value to store in the ldap attribute if there are no
     * grouper attribute values to store there.
     * 
     * @param ldapAttribute
     *            Name of the Ldap Attribute
     * @return String to place in the ldap attribute if no Grouper attribute
     *         values are found to store there, or <code>null</code> if not
     *         defined.
     */
    public String getGroupAttributeMappingLdapEmptyValue(String ldapAttribute);
}