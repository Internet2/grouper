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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.ProvisionerConfiguration.GroupDNStructure;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.ldappc.ldap.OrganizationalUnit;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * This synchronizes groups stored in the directory as entries.
 */
public class GroupEntrySynchronizer extends Synchronizer {

  private static final Logger LOG = GrouperUtil.getLogger(GroupEntrySynchronizer.class);

  /**
   * Default size of group hash tables if not specified in configuration.
   */
  private static final int DEFAULT_HASH_SIZE = 100000;

  /**
   * Set of ou DNs to be deleted.
   */
  private Set<Name> deleteOus;

  /**
   * Set of ou DNs already processed.
   */
  private Set<Name> processedOus;

  /**
   * Set of group DNs to be deleted.
   */
  private Set<Name> deleteGroups;

  /**
   * Set of group DNs already processed.
   */
  private Set<Name> processedGroups;

  /**
   * Holds the objectClass attribute modifications.
   */
  private AttributeModifier objectClassMods;

  /**
   * Holds the member DN listing attribute modifications.
   */
  private AttributeModifier memberDnMods;

  /**
   * Holds the member name listing attribute modifications.
   */
  private AttributeModifier memberNameMods;

  /**
   * Holds the RDN attribute modifications.
   */
  private AttributeModifier rdnMods;

  /**
   * The attribute names are the mapped ldap attributes, and the attribute values are the
   * AttributeModifiers associated with the attribute.
   * 
   * NOTE: Used BasicAttributes object here to take advantage of case insensitivity of
   * attribute names. This is important as the ldapAttribute names in grouper attribute to
   * ldap attribute mapping may not all be in the same case.
   */
  private BasicAttributes mappedLdapAttributes;

  /**
   * Constructs a <code>GroupEntrySynchronizer</code>.
   * 
   * @param ctx
   *          Ldap context to be used for synchronizing
   * @param root
   *          DN of the root element
   * @param configuration
   *          Grouper provisioning configuration
   * @param options
   *          Grouper provisioning options
   * @param subjectCache
   *          Subject cache to speed subject retrieval
   * 
   * @throws NamingException
   *           Thrown when a naming exception occurs.
   * @throws ConfigurationException
   *           Thrown if the configuration file is not correct.
   */
  public GroupEntrySynchronizer(Provisioner provisioner) throws NamingException,
      ConfigurationException {
    //
    // Call super constructor
    //
    // super(ctx, root, configuration, options, subjectCache);
    super(provisioner);

    int estimate = configuration.getGroupHashEstimate();
    if (estimate == 0) {
      estimate = DEFAULT_HASH_SIZE;
    }
    LOG.info("Group initial cache size = " + estimate);

    //
    // Init various objects
    //
    deleteOus = new HashSet<Name>(estimate);
    processedOus = new HashSet<Name>(estimate);
    deleteGroups = new HashSet<Name>(estimate);
    processedGroups = new HashSet<Name>(estimate);

    mappedLdapAttributes = new BasicAttributes(true);

    //
    // If provisioning with "flat" structure, verify that a group naming
    // attribute is defined for the group ldap entry
    //
    if (GroupDNStructure.flat.equals(configuration.getGroupDnStructure())) {
      if (configuration.getGroupDnGrouperAttribute() == null) {
        throw new ConfigurationException("Group DN grouper attribute is not defined.");
      }
    }

    //
    // Verify that a object class is defined for the group ldap entry
    //
    if (configuration.getGroupDnObjectClass() == null) {
      throw new ConfigurationException("Group ldap entry object class is not defined.");
    }

    //
    // If the RDN attribute name is defined and is not "ou", create the
    // attribute
    // modifier
    //
    String rdnAttrName = configuration.getGroupDnRdnAttribute();
    if (rdnAttrName == null
        || OrganizationalUnit.Attribute.OU.equalsIgnoreCase(rdnAttrName)) {
      throw new ConfigurationException("Group ldap entry RDN attribute name is invalid.");
    }
    rdnMods = new AttributeModifier(rdnAttrName);

    //
    // Add an attribute modifier for the object class attribute
    //
    objectClassMods = new AttributeModifier(LdapUtil.OBJECT_CLASS_ATTRIBUTE);

    //
    // If tracking member dn's, initialize related attributes
    //
    memberDnMods = null;
    if (configuration.isGroupMembersDnListed()) {
      //
      // Get the attribute name for storing Dns
      //
      String attrName = configuration.getGroupMembersDnListAttribute();
      if (attrName == null) {
        throw new ConfigurationException(
            "Group members DN list attribute name is not defined.");
      }

      //
      // Build the member Dn list attribute modifier
      //
      memberDnMods = new DnAttributeModifier(attrName, configuration
          .getGroupMembersDnListEmptyValue());
    }

    //
    // If tracking member names, initialize related attributes
    //
    memberNameMods = null;
    if (configuration.isGroupMembersNameListed()) {
      //
      // Get the attribute name for storing names
      //
      String attrName = configuration.getGroupMembersNameListAttribute();
      if (attrName == null) {
        throw new ConfigurationException(
            "Group members name list attribute name is not defined.");
      }

      //
      // Initialize the instance variable
      //
      memberNameMods = new AttributeModifier(attrName, configuration
          .getGroupMembersNameListEmptyValue());
    }

    //
    // Build attribute modifiers for the grouper to ldap attribute mapping
    //
    Map<String, List<String>> attributeMap = configuration.getGroupAttributeMapping();
    for (String grouperAttr : attributeMap.keySet()) {
      //
      // Get the next key (i.e., grouper attribute name) and the
      // corresponding value (i.e., ldap attribute name)
      for (String ldapAttr : attributeMap.get(grouperAttr)) {
        //
        // If the ldapAttr is not yet defined in mappedLdapAttributes
        // with a modifier, add it
        //
        if (mappedLdapAttributes.get(ldapAttr) == null) {
          String emptyValue = configuration
              .getGroupAttributeMappingLdapEmptyValue(ldapAttr);
          mappedLdapAttributes.put(ldapAttr, new AttributeModifier(ldapAttr, emptyValue));
        }
      }
    }
  }

  /**
   * Synchronizes the group set with that in the directory.
   * 
   * @param groups
   *          Set of Groups
   * @throws javax.naming.NamingException
   *           thrown if a Naming error occurs
   * @throws MultiErrorException
   *           thrown if one or more exceptions occurred that did not need to stop all
   *           processing
   * @throws LdappcException
   *           thrown if an error occurs
   */
  public void synchronize(Set<Group> groups) throws NamingException, LdappcException {

    initialize();

    for (Group group : groups) {
      performInclude(group, provisioner.determineStatus(group));
    }

    commit();
  }

  /**
   * This identifies the underlying group as one that must remain or, if need be, must be
   * added to the subject's LDAP entry. If the group has already been provisioned to the
   * entry, it will remain within the subject's LDAP entry.
   * 
   * @param group
   *          Group to be included
   * @param status
   *          Either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
   *          {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
   * @throws NamingException
   *           thrown if a Naming error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   */
  protected void performInclude(Group group, int status) throws NamingException,
      LdappcException {
    // DebugLog.info("Starting include of group " + group.getName());

    //
    // Initialize for the perform include
    //
    initializeInclude(group);

    //
    // Build the group's DN
    //
    Name groupDn = buildGroupDn(group);

    //
    // Act on whether or not the group has already been processed
    //
    if (!processedGroups.contains(groupDn)) {
      //
      // Act based on whether or not the group DN exists already.
      //
      if (deleteGroups.remove(groupDn)) {
        //
        // Update the group as needed
        //
        if (status == Provisioner.STATUS_NEW || status == Provisioner.STATUS_MODIFIED
            || status == Provisioner.STATUS_UNKNOWN) {
          updateGroupEntry(groupDn, group);
        }
      } else {
        //
        // Add the group entry
        //
        addGroupEntry(groupDn, group);

      }

      //
      // Mark the stem OUs as processed.
      // NOTE: If naming structure isn't "bushy" this method does
      // nothing
      //
      updateProcessedOus(groupDn);

      //
      // Identify the groupDn as processed so it isn't attempted again
      //
      processedGroups.add(groupDn);
    } else {
      //
      // groupDn has already been processed so log an error
      // (This should never happen).
      //
      LOG.error("Group entry identified by {} has already been encountered,"
          + " and will not be processed another time.", groupDn);
    }
  }

  /**
   * This updates the group's ldap entry with the current data.
   * 
   * @param groupDn
   *          DN of the group entry
   * @param group
   *          Group associated with the group entry
   * @throws NamingException
   *           thrown if a Naming exception occurs
   */
  protected void updateGroupEntry(Name groupDn, Group group) throws NamingException {
    //
    // Store the values from the current entry
    //
    initGroupData(groupDn);

    //
    // Store the values from the current entry in order to update
    //
    storeGroupData(group);

    //
    // Build a vector to hold all of the modifiers
    //
    Vector<AttributeModifier> modifiers = new Vector<AttributeModifier>();

    modifiers.add(objectClassMods);

    if (memberDnMods != null) {
      //
      // If no members could be resolved, then provision the noValue value if appropriate
      //
      if (memberDnMods.getRetainedValues().size() == 0
          && memberDnMods.getNoValue() != null) {
        memberDnMods.store(memberDnMods.getNoValue());
      }
      modifiers.add(memberDnMods);
    }

    if (memberNameMods != null) {
      //
      // If no members could be resolved, then provision the noValue value if appropriate
      //
      if (memberNameMods.getRetainedValues().size() == 0
          && memberNameMods.getNoValue() != null) {
        memberNameMods.store(memberNameMods.getNoValue());
      }
      modifiers.add(memberNameMods);
    }

    NamingEnumeration<Attribute> ldapAttrEnum = mappedLdapAttributes.getAll();
    while (ldapAttrEnum.hasMore()) {
      Attribute attribute = ldapAttrEnum.next();
      modifiers.add((AttributeModifier) attribute.get());
    }

    //
    // Get all of the modifications
    //
    Vector<ModificationItem> modifications = new Vector<ModificationItem>();
    for (AttributeModifier modifier : modifiers) {
      ModificationItem[] items = modifier.getModifications();
      for (int i = 0; i < items.length; i++) {
        modifications.add(items[i]);
      }
    }

    //
    // Build the modification item array
    //
    ModificationItem[] modificationItems = new ModificationItem[modifications.size()];
    for (int i = 0; i < modificationItems.length; i++) {
      modificationItems[i] = (ModificationItem) modifications.get(i);
    }

    //
    // Modify the entry
    //
    if (modificationItems.length > 0) {
      LOG.info("Modify '" + groupDn + "' " + Arrays.asList(modificationItems));
      ldapCtx.modifyAttributes(groupDn, modificationItems);
    }
  }

  /**
   * This populates the instance variables with the current values from the group's LDAP
   * entry.
   * 
   * @param groupDn
   *          DN of the group entry
   * 
   * @throws javax.naming.NamingException
   *           thrown if a Naming error occurs
   */
  protected void initGroupData(Name groupDn) throws NamingException {
    //
    // Build the list of attributes needed from the entry
    //
    Vector<String> wantedAttr = new Vector<String>();

    wantedAttr.add(rdnMods.getAttributeName());

    if (memberDnMods != null) {
      wantedAttr.add(memberDnMods.getAttributeName());
    }

    if (memberNameMods != null) {
      wantedAttr.add(memberNameMods.getAttributeName());
    }

    NamingEnumeration<String> mappedLdapAttrNames = mappedLdapAttributes.getIDs();
    while (mappedLdapAttrNames.hasMore()) {
      wantedAttr.add(mappedLdapAttrNames.next());
    }

    wantedAttr.add(objectClassMods.getAttributeName());

    //
    // Get the existing attributes defined for the entry
    //
    LOG.debug("get group attributes '" + groupDn + "' attrs " + wantedAttr);
    Attributes attributes = ldapCtx.getAttributes(groupDn, (String[]) wantedAttr
        .toArray(new String[0]));

    //
    // Populate the rdn attribute
    //
    populateAttrModifier(attributes, rdnMods);

    //
    // Populate the member dn list modifier if defined
    //
    if (memberDnMods != null) {
      populateAttrModifier(attributes, memberDnMods);
    }

    //
    // Populate the member name list modifier if defined
    //
    if (memberNameMods != null) {
      populateAttrModifier(attributes, memberNameMods);
    }

    //
    // Populate the mapped attributes modifiers
    //
    NamingEnumeration<Attribute> mappedLdapAttributeEnum = mappedLdapAttributes.getAll();
    while (mappedLdapAttributeEnum.hasMore()) {
      Attribute ldapAttribute = mappedLdapAttributeEnum.next();
      populateAttrModifier(attributes, (AttributeModifier) ldapAttribute.get());
    }

    //
    // Populate the object class modifier and retain all of the values
    //
    populateAttrModifier(attributes, objectClassMods);
    objectClassMods.retainAll();
  }

  /**
   * This method populates an AttributeModifier with the associated attribute provided in
   * Attributes. If an associated attribute is not found in <code>attributes</code>,
   * <code>modifier</code> remains unchanged. If an associated is found,
   * <code>modifier</code> is initialized with the attribute and the attribute is deleted
   * from <code>attributes</code>.
   * 
   * @param attributes
   *          Attributes
   * @param modifier
   *          AttributeModifier to be populated
   * @throws NamingException
   *           thrown if a Naming error occurs.
   */
  private void populateAttrModifier(Attributes attributes, AttributeModifier modifier)
      throws NamingException {
    Attribute attribute = attributes.get(modifier.getAttributeName());
    if (attribute != null) {
      modifier.init(attribute);
      attributes.remove(modifier.getAttributeName());
    }
  }

  /**
   * This stores the given Group's data in the AttributeModifiers. This stores both the
   * object class data from the configuration and the Group data in the associated
   * attribute modifiers.
   * 
   * @param group
   *          Group
   * @throws NamingException
   *           thrown if a naming error occurs
   */
  protected void storeGroupData(Group group) throws NamingException {
    //
    // Store the object class data for the group entry.
    //
    storeObjectClassData();

    Set<Member> members = group.getMembers();

    //
    // If there are no members, then provision the noValue value if appropriate
    //
    if (members.isEmpty()) {
      if (memberDnMods != null && memberDnMods.getNoValue() != null) {
        memberDnMods.store(memberDnMods.getNoValue());
      }
      if (memberNameMods != null && memberNameMods.getNoValue() != null) {
        memberNameMods.store(memberNameMods.getNoValue());
      }
    } else {

      //
      // Get membership process it
      //
      for (Member member : members) {
        Subject subject = null;
        try {
          subject = member.getSubject();
        } catch (SubjectNotFoundException snfe) {
          //
          // If the subject was not found, log it and continue
          //
          LOG.warn("Subject not found", snfe);
          continue;
        }

        //
        // If maintaining member DN list, do it now
        //
        if (memberDnMods != null) {
          try {
            Name subjectDn = subjectCache.findSubjectDn(member);
            if (subjectDn != null) {
              memberDnMods.store(subjectDn.toString());
            }
          } catch (Exception e) {
            LOG.warn(getErrorData(subject), e);
          }
        }

        //
        // If maintaining member name list, do it now
        //
        if (memberNameMods != null) {
          //
          // Catch all of the exceptions thrown as they are "warning" and
          // handle them in a common manner.
          //
          try {
            //
            // Get the subject source
            //
            Source source = subject.getSource();
            if (source == null) {
              throw new LdappcException("Source is null");
            }

            //
            // Get the naming attribute for this source
            //
            String nameAttribute = configuration
                .getGroupMembersNameListNamingAttribute(source.getId());
            if (nameAttribute != null) {
              //
              // Get the subject attribute value
              //
              String nameValue = subject.getAttributeValue(nameAttribute);
              if (nameValue != null) {
                this.memberNameMods.store(nameValue);
              } else {
                throw new LdappcException("Naming attribute [" + nameAttribute
                    + "] is not defined.");
              }
            } else {
              throw new LdappcException(
                  "No group members name list naming attribute defined for source id ["
                      + source.getId() + "]");
            }
          } catch (Exception e) {
            //
            // All of the exceptions thrown in this try are "warning"
            // related so simply log them and continue on with
            // processing.
            //
            LOG.warn(getErrorData(subject), e);
          }
        }
      }
    }

    //
    // Populate mapped attributes from the group
    //
    Map<String, List<String>> attributeMap = configuration.getGroupAttributeMapping();
    for (String groupAttribute : attributeMap.keySet()) {
      //
      // Get the attribute value from the group
      //
      String groupAttributeValue = group.getAttributeOrFieldValue(groupAttribute, false,
          false);

      //
      // Get the next key (i.e., grouper attribute name) and the
      // corresponding value (i.e., ldap attribute name)
      for (String ldapAttr : attributeMap.get(groupAttribute)) {
        //
        // If the group has this attribute populated, store it
        //
        Attribute attribute = mappedLdapAttributes.get(ldapAttr);
        AttributeModifier attributeModifier = (AttributeModifier) attribute.get();
        //
        // Only storing non-empty string attributes (i.e., length > 0)
        //
        if (groupAttributeValue != null && groupAttributeValue.length() > 0) {
          attributeModifier.store(groupAttributeValue);
        }
        //
        // Store noValue value if there are no values and noValue is defined
        //
        else if (attributeModifier.getNoValue() != null) {
          attributeModifier.store(attributeModifier.getNoValue());
        }
      }
    }
  }

  /**
   * This stores the object class data from the configuration in the associated
   * AttributeModifier.
   * 
   * @throws NamingException
   *           thrown if a naming exception occurs
   */
  protected void storeObjectClassData() throws NamingException {
    //
    // Store the group entry object class
    //
    objectClassMods.store(configuration.getGroupDnObjectClass());

    //
    // If needed and defined, store the member dn list object class
    //
    if (memberDnMods != null) {
      String objectClass = configuration.getGroupMembersDnListObjectClass();
      if (objectClass != null) {
        objectClassMods.store(objectClass);
      }
    }

    //
    // If needed and defined, store the member name list object class
    //
    if (memberNameMods != null) {
      String objectClass = configuration.getGroupMembersNameListObjectClass();
      if (objectClass != null) {
        objectClassMods.store(objectClass);
      }
    }

    //
    // If defined, store the grouper attribute object class
    //
    String attrMapObjClass = configuration.getGroupAttributeMappingObjectClass();
    if (attrMapObjClass != null) {
      objectClassMods.store(attrMapObjClass);
    }
  }

  /**
   * This creates a new Group ldap entry. The new entry is identified by
   * <code>groupDn</code> and is populated from <code>group</code>.
   * 
   * @param groupDn
   *          DN of the new entry
   * @param group
   *          Group holding the data for the new entry
   * 
   * @throws NamingException
   *           Thrown if a naming exception occurs.
   */
  protected void addGroupEntry(Name groupDn, Group group) throws NamingException {
    //
    // Get the group data
    //
    storeGroupData(group);

    //
    // Build list of attribute modifiers possibly holding data
    //
    Vector<AttributeModifier> modifiers = new Vector<AttributeModifier>();

    modifiers.add(objectClassMods);

    modifiers.add(rdnMods);

    NamingEnumeration<Attribute> ldapAttrEnum = mappedLdapAttributes.getAll();
    while (ldapAttrEnum.hasMore()) {
      Attribute attribute = ldapAttrEnum.next();
      modifiers.add((AttributeModifier) attribute.get());
    }

    //
    // Build list of member attribute modifiers possibly holding data
    //
    Vector<AttributeModifier> memberModifiers = new Vector<AttributeModifier>();

    if (memberDnMods != null) {
      //
      // If no members could be resolved, then provision the noValue value if appropriate
      //
      if (memberDnMods.getAdditions().size() == 0 && memberDnMods.getNoValue() != null) {
        memberDnMods.store(memberDnMods.getNoValue());
      }
      memberModifiers.add(memberDnMods);
    }

    if (memberNameMods != null) {
      //
      // If no members could be resolved, then provision the noValue value if appropriate
      //
      if (memberNameMods.getAdditions().size() == 0
          && memberNameMods.getNoValue() != null) {
        memberNameMods.store(memberNameMods.getNoValue());
      }
      memberModifiers.add(memberNameMods);
    }

    //
    // Get the attributes for building the new entry
    //
    BasicAttributes attributes = new BasicAttributes(true);

    for (AttributeModifier modifier : modifiers) {
      Attribute attribute = modifier.getAdditions();
      if (attribute.size() > 0) {
        attributes.put(attribute);
      }
    }

    //
    // If not creating the group then modifying members,
    // include member attributes when creating the group
    //
    if (!configuration.getCreateGroupThenModifyMembers()) {
      for (AttributeModifier modifier : memberModifiers) {
        Attribute attribute = modifier.getAdditions();
        if (attribute.size() > 0) {
          attributes.put(attribute);
        }
      }
    }

    //
    // If not creating the group then modifying members,
    // include member attributes when creating the group
    //
    if (!configuration.getCreateGroupThenModifyMembers()) {
      for (AttributeModifier modifier : memberModifiers) {
        Attribute attribute = modifier.getAdditions();
        if (attribute.size() > 0) {
          attributes.put(attribute);
        }
      }
    }

    //
    // Build the subject context
    //
    LOG.info("Creating '" + groupDn + "' attrs " + attributes);
    ldapCtx.createSubcontext(groupDn, attributes);

    //
    // If creating the group then modifying members,
    // modify the member attributes
    //
    if (configuration.getCreateGroupThenModifyMembers()) {
      //
      // Member modifications
      //
      Vector<ModificationItem> modifications = new Vector<ModificationItem>();
      for (AttributeModifier modifier : memberModifiers) {
        for (ModificationItem modItem : modifier.getModifications()) {
          modifications.add(modItem);
        }
      }

      //
      // Modify the entry
      //
      if (!modifications.isEmpty()) {
        LOG.info("Modify '" + groupDn + "' " + modifications);
        ldapCtx.modifyAttributes(groupDn, modifications
            .toArray(new ModificationItem[] {}));
      }
    }
  }

  /**
   * This builds the DN of the given group. Also this populates the AttributeModifier with
   * the Group's RDN value. In the event that the Group naming structure is bushy, this
   * calls {@link #buildStemOuEntries(Group)} to build the necessary organizationalUnit
   * entries.
   * 
   * @param group
   *          Group
   * @return DN for the associated LDAP entry
   * @throws NamingException
   *           thrown if a Naming error occurs.
   * @throws LdappcException
   *           thrown if the RDN attribute is not defined for the group.
   */
  protected Name buildGroupDn(Group group) throws NamingException, LdappcException {

    //
    // If DN structure is bushy, build stem Ou's and initialize the group DN
    // with the parent OU DN. Else, initialize the group DN with the root
    // DN.
    //
    if (GroupDNStructure.bushy.equals(configuration.getGroupDnStructure())) {
      buildStemOuEntries(group);
    }

    Name groupDn = provisioner.calculateGroupDn(group);

    Rdn rdn = new Rdn(groupDn.get(groupDn.size() - 1));
    rdnMods.store(rdn.getValue().toString());

    return groupDn;
  }

  /**
   * This builds the group's parent OU DN. Also, if necessary, this builds any missing OU
   * entries in the directory for the group's stem. The DNs of any newly created OUs are
   * placed into the list of OUs to be deleted. They should be removed from the delete
   * list and placed into the list of processed OUs when the group entry is successfully
   * created.
   * 
   * @param group
   *          Group
   * @return OU DN under which the group entry must be created.
   * @throws javax.naming.NamingException
   *           thrown if a Naming exception occured.
   * @see #updateProcessedOus(Name)
   */
  protected void buildStemOuEntries(Group group) throws NamingException {
    //
    // Build an attribute list once for creating new ou entries
    // below. Note, the "ou" attribute is added below.
    //
    Attributes attributes = new BasicAttributes(true);
    attributes.put(new BasicAttribute(LdapUtil.OBJECT_CLASS_ATTRIBUTE,
        OrganizationalUnit.OBJECT_CLASS));

    List<Name> stemDns = provisioner.calculateStemDns(group);
    for (Name stemDn : stemDns) {
      //
      // If stemDn hasn't been processed, process it based on whether it
      // already exists
      //
      if (!processedOus.contains(stemDn)) {
        //
        // If it isn't deleted from deleteOus, create it
        //
        if (!deleteOus.contains(stemDn)) {
          //
          // Build the new OU
          //
          Rdn rdn = new Rdn(stemDn.get(stemDn.size() - 1));
          attributes.put(OrganizationalUnit.Attribute.OU, rdn.getValue().toString());
          LOG.info("Creating '" + stemDn + "' attrs " + attributes);
          ldapCtx.createSubcontext(stemDn, attributes);

          //
          // Add it to deleteOus so if the group isn't processed it
          // will be deleted. If the group is processed correctly,
          // these will need to be moved to processedOus
          //
          deleteOus.add((Name) stemDn.clone());
        }
      }
    }
  }

  /**
   * This updates the list of processed OUs with those identified from the group's DN. Any
   * of the OU DNs found in the list of OUs to be deleted are removed from deletion list
   * as well. This assumes that any parent DN between the root DN and the group DN
   * identifies an OU associated with the group's parent stem.
   * 
   * @param groupDn
   *          DN of the group entry
   */
  protected void updateProcessedOus(Name groupDn) {
    for (int i = rootDn.size() + 1; i < groupDn.size(); i++) {
      Name stemDn = groupDn.getPrefix(i);
      deleteOus.remove(stemDn);
      processedOus.add(stemDn);
    }
  }

  /**
   * Perform any initialization prior to processing the set of groups.
   * 
   * @throws NamingException
   *           thrown if a Naming error occurs
   * @throws LdappcException
   *           thrown if an error occurs
   */
  protected void initialize() throws NamingException, LdappcException {
    //
    // Clear existing data
    //
    processedOus.clear();
    deleteOus.clear();
    processedGroups.clear();
    deleteGroups.clear();

    //
    // Clear any LDAP entries that are no longer needed
    // Must be done prior to populating below
    //
    clearRoot();

    //
    // Populate any necessary data
    //
    populate();
  }

  /**
   * This deletes any entries under the root entry are neither organizationalUnits nor
   * have the same object class as a group entry.
   * 
   * @throws NamingException
   *           Thrown if a naming exception occurs.
   */
  protected void clearRoot() throws NamingException {
    //
    // Build the query filter to find all existing
    // children under the root that are not object class type of the group
    // entries, and if needed not organizationalUnit entries
    //
    String filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
        + configuration.getGroupDnObjectClass() + ")";

    if (GroupDNStructure.bushy.equals(configuration.getGroupDnStructure())) {
      filter = "(|" + filter + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
          + OrganizationalUnit.OBJECT_CLASS + "))";
    }

    filter = "(!" + filter + ")";

    //
    // Build search controls to search entire sub-tree
    //
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setCountLimit(0);

    //
    // Perform the search
    //
    LOG.debug("search base '" + rootDn + "' filter '" + filter + "'");
    NamingEnumeration searchEnum = ldapCtx.search(rootDn, filter, searchControls);

    //
    // Delete anything found here and it's children.
    //
    NameParser parser = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME);
    while (searchEnum.hasMore()) {
      SearchResult searchResult = (SearchResult) searchEnum.next();
      if (searchResult.isRelative()) {
        //
        // Get the entry's name. If it is "" then is must be the root so
        // just continue as it isn't processed
        //
        String entryName = searchResult.getName();
        if (entryName.length() == 0) {
          continue;
        }

        //
        // Build the entry's DN
        //
        Name entryDn = parser.parse(entryName);
        entryDn = entryDn.addAll(0, rootDn);

        //
        // Try to find it as may already been deleted. If not found,
        // just continue
        //
        try {
          ldapCtx.lookup(entryDn);
        } catch (NamingException ne) {
          //
          // Assume it couldn't be found, so just continue
          //
          continue;
        }

        //
        // Try to delete it
        //
        try {
          LOG.debug("delete '" + entryDn + "'");
          LdapUtil.delete(ldapCtx, entryDn);
        } catch (Exception e) {
          LOG.warn("Unable to delete " + entryDn, e);
        }
      } else {
        //
        // Log it and continue on.
        //
        LOG.error("Unable to handle LDAP URL references: {}", searchResult.getName());
      }
    }
  }

  /**
   * This populates this object with the current values from the root's LDAP entry that
   * are either organizational units or have a object class type of the group entry.
   * 
   * @throws javax.naming.NamingException
   *           thrown if a Naming error occurs
   */
  protected void populate() throws NamingException {
    //
    // Build search controls for the searched performed below
    //
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setCountLimit(0);
    searchControls.setReturningAttributes(new String[] {});

    //
    // Populate the group deletes
    //
    String filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
        + configuration.getGroupDnObjectClass() + ")";
    populateDns(deleteGroups, filter, searchControls);
    LOG.debug("found " + deleteGroups.size() + " groups");

    //
    // If necessary, populate the stem ou deletes
    //
    if (GroupDNStructure.bushy.equals(configuration.getGroupDnStructure())) {
      filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
          + OrganizationalUnit.OBJECT_CLASS + ")";
      populateDns(deleteOus, filter, searchControls);
      LOG.debug("found " + deleteOus.size() + " ous");
    }
  }

  /**
   * This populates the given Set with the DNs of any child entries of the root DN
   * matching the given filter.
   * 
   * @param dns
   *          Set to hold the DNs
   * @param filter
   *          LDAP Filter
   * @param searchControls
   *          Search controls
   * @throws NamingException
   *           thrown if a Naming error occurs.
   */
  protected void populateDns(Set<Name> dns, String filter, SearchControls searchControls)
      throws NamingException {
    //
    // Perform the search of the root
    //
    LOG.debug("search base '{}' filter '{}'", rootDn, filter);
    NamingEnumeration searchEnum = ldapCtx.search(rootDn, filter, searchControls);

    //
    // Populate dns with DNs of existing objects. The root dn is excluded.
    //
    NameParser parser = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME);
    while (searchEnum.hasMore()) {
      SearchResult searchResult = (SearchResult) searchEnum.next();
      if (searchResult.isRelative()) {
        //
        // Get the name string. If empty it is the root so ignore
        //
        String entryName = searchResult.getName();
        if (entryName.length() == 0) {
          continue;
        }

        //
        // Build the entry's DN
        //
        Name entryDn = LdapUtil.getName(parser, searchResult);
        entryDn = entryDn.addAll(0, rootDn);

        //
        // Add entryDn to the deletes list
        //
        LOG.debug("entryDn '" + entryDn + "'");
        dns.add(entryDn);
      } else {
        //
        // Log it and continue on.
        //
        LOG.error("Unable to handle LDAP URL references: {}", searchResult.getName());
      }
    }
  }

  /**
   * Initializes the attributes needed for holding data for the given group.
   * 
   * @param group
   *          Group
   * @throws NamingException
   */
  protected void initializeInclude(Group group) throws NamingException {
    //
    // Clear existing values
    //
    objectClassMods.clear();
    rdnMods.clear();
    if (memberDnMods != null) {
      memberDnMods.clear();
    }

    if (memberNameMods != null) {
      memberNameMods.clear();
    }

    NamingEnumeration<Attribute> ldapAttrEnum = mappedLdapAttributes.getAll();
    while (ldapAttrEnum.hasMore()) {
      Attribute attribute = ldapAttrEnum.next();
      ((AttributeModifier) attribute.get()).clear();
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
    // Init vars
    //
    LdapContext context = ldapCtx;

    //
    // Delete any group entries that are no longer valid
    //
    for (Name dn : deleteGroups) {
      //
      // Delete it
      //
      LOG.info("Delete group '" + dn + "'");
      LdapUtil.delete(context, dn);
    }

    //
    // Delete any ou entries that are no longer valid
    //
    for (Name dn : deleteOus) {
      //
      // Try to find it. If not found, just continue
      //
      try {
        ldapCtx.lookup(dn);
      } catch (NamingException ne) {
        //
        // Assume it couldn't be found, so just continue
        //
        continue;
      }

      //
      // Try to delete it
      //
      LOG.info("Delete ou '" + dn + "'");
      LdapUtil.delete(context, dn);
    }

  }

  /**
   * Builds an error data string based on the objects provided.
   * 
   * @param subject
   *          Subject
   * @return data string for error message
   */
  protected String getErrorData(Subject subject) {
    return "SUBJECT[" + subjectCache.getSubjectData(subject) + "]";
  }

  /**
   * Builds an error data string based on the objects provided.
   * 
   * @param group
   *          Group
   * @return data string for error message
   */
  protected String getErrorData(Group group) {
    return "GROUP[" + Provisioner.getGroupData(group) + "]";
  }

  /**
   * Builds an error data string based on the objects provided.
   * 
   * @param member
   *          Member
   * @return member data string
   */
  protected String getErrorData(Member member) {
    return "MEMBER[" + Provisioner.getMemberData(member) + "]";
  }
}
