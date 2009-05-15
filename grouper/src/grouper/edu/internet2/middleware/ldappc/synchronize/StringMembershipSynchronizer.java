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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Arrays;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.util.LdapUtil;

/**
 * This synchronizes memberships stored in the directory as strings in an attribute.
 */
public class StringMembershipSynchronizer extends Synchronizer {

  private static final Logger LOG = GrouperUtil
      .getLogger(StringMembershipSynchronizer.class);

  /**
   * Holds the membership listing attribute modifications.
   */
  private AttributeModifier membershipMods;

  /**
   * Holds the object class attribute modifications.
   */
  private AttributeModifier objectClassMods;

  /**
   * Name of the group naming attribute.
   */
  private String groupNamingAttribute;

  /**
   * DN of the subject whose permissions are being synchronized.
   */
  private String subject;

  /**
   * Constructs a <code>StringMembershipSynchronizer</code>.
   * 
   * @param ctx
   *          Ldap context to use for provisioning
   * @param subject
   *          DN of the subject whose memberships are being provisioned
   * @param configuration
   *          Signet provisioning configuration
   * @param options
   *          Signet provisioning options
   * @param subjectCache
   *          Subject cache to speed subject retrieval
   * 
   * @throws NamingException
   *           thrown if a naming exception occurs.
   * @throws ConfigurationException
   *           thrown if the configuration isn't correct.
   */
  public StringMembershipSynchronizer(Provisioner provisioner, String subject)
      throws NamingException, ConfigurationException {
    //
    // Call super constructor
    //
    // super(ctx, subject, configuration, options, subjectCache);
    super(provisioner);

    this.subject = subject;

    //
    // Try to get the membership listing string attribute name as it is
    // needed to initialize instance variables
    //
    String listAttrName = configuration.getMemberGroupsListAttribute();
    if (listAttrName == null) {
      throw new ConfigurationException(
          "The name of the attribute to store membership group strings is null.");
    }

    //
    // Initialize the instance attributes
    //
    objectClassMods = new AttributeModifier(LdapUtil.OBJECT_CLASS_ATTRIBUTE);
    // membershipMods = new AttributeModifier(listAttrName,
    // configuration.getMemberGroupsListEmptyValue());
    membershipMods = new AttributeModifier(listAttrName);

    //
    // Get the group naming attribute
    //
    groupNamingAttribute = configuration.getMemberGroupsNamingAttribute();
    if (groupNamingAttribute == null) {
      throw new ConfigurationException(
          "The name of the group naming attribute is null.");
    }
  }

  /**
   * Synchronizes the groups with those in the directory.
   * 
   * @param groupNames
   *          Set of group names
   * @throws javax.naming.NamingException
   *           thrown if a Naming error occurs
   * @throws MultiErrorException
   *           thrown if one or more exceptions occurred that did not need to stop all
   *           processing
   * @throws LdappcException
   *           thrown if an error occurs
   */
  public void synchronize(Set<String> groupNames) throws NamingException, LdappcException {
    //
    // Initialize the process
    //
    initialize();

    //
    // Iterate over the set of membership group names.
    //
    for (String groupNameString : groupNames) {
      //
      // Process the group
      //
      performInclude(groupNameString, Provisioner.STATUS_UNKNOWN);
    }

    //
    // Commit the modifications to the directory
    //
    commit();
  }

  /**
   * This identifies the underlying group as one that must remain or, if need be, must be
   * added to the subject's LDAP entry. If the group has already been provisioned to the
   * entry, it will remain within the subject's LDAP entry.
   * 
   * @param groupNameString
   *          Group to be included
   * @param status
   *          Either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
   *          {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
   * @throws NamingException
   *           thrown if a Naming error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   * @see edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer#performInclude(String,
   *      int)
   */
  protected void performInclude(String groupNameString, int status)
      throws NamingException, LdappcException {
    membershipMods.store(groupNameString);
  }

  /**
   * Perform any initialization prior to processing the set of permissions.
   * 
   * @throws NamingException
   *           thrown if a Naming error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   * 
   * @see edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer#initialize()
   */
  protected void initialize() throws NamingException, LdappcException {
    // DebugLog.info("Updating subject " + getSubject());
    //
    // Clear any existing values
    //
    membershipMods.clear();
    objectClassMods.clear();

    //
    // Populate with current values from the subject entry
    //
    populate();
  }

  /**
   * This populates this object with the current values from the subject's LDAP entry.
   * 
   * @throws javax.naming.NamingException
   *           thrown if a Naming error occurs
   */
  protected void populate() throws NamingException {
    //
    // Get the existing values
    //
    LOG.debug("get attributes for '{}' attrs '{}' '{}'", new Object[] { getSubject(),
        membershipMods.getAttributeName(), objectClassMods.getAttributeName() });
    Attributes attributes = ldapCtx.getAttributes(getSubject(), new String[] {
        membershipMods.getAttributeName(), objectClassMods.getAttributeName() });

    //
    // Initialize the membership listing attribute modifier
    //
    Attribute attribute = attributes.get(membershipMods.getAttributeName());
    membershipMods.init(attribute);

    //
    // Populate the objectClass modifier if needed
    //
    objectClassMods.init();
    String stringObjectClass = configuration.getMemberGroupsListObjectClass();
    if (stringObjectClass != null) {
      attribute = attributes.get(objectClassMods.getAttributeName());
      objectClassMods.init(attribute);
      objectClassMods.retainAll();
      objectClassMods.store(stringObjectClass);
    }
  }

  /**
   * This commits any changes not already committed to the directory.
   * 
   * @throws NamingException
   *           thrown if a Naming error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   * 
   * @see edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer#commit()
   */
  protected void commit() throws NamingException, LdappcException {
    //
    // Determine how many modifications are to be performed
    //
    ModificationItem[] objectClassModItems = objectClassMods.getModifications();
    ModificationItem[] membershipModItems = membershipMods.getModifications();
    int modCnt = objectClassModItems.length + membershipModItems.length;

    //
    // Perform modifications if needed
    //
    if (modCnt > 0) {
      //
      // Build the array for the modification items
      //
      ModificationItem[] mods = new ModificationItem[modCnt];

      //
      // Add the modifications in the following order
      // 1. Add the object class modifications
      // 2. Add the membership listing attribute modifications
      //
      int modIndex = 0;

      for (ModificationItem modItem : objectClassModItems) {
        mods[modIndex++] = modItem;
      }

      for (ModificationItem modItem : membershipModItems) {
        mods[modIndex++] = modItem;
      }

      //
      // Perform the modifications
      //
      LOG.info("Modify subject '{}' {}", getSubject(), Arrays.asList(mods));
      ldapCtx.modifyAttributes(getSubject(), mods);
    }
  }

  /**
   * Get the DN of the subject.
   * 
   * @return DN of the subject
   */
  public String getSubject() {
    return subject;
  }
}
