/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.GrouperProvisioner;
import edu.internet2.middleware.ldappc.GrouperProvisionerConfiguration;
import edu.internet2.middleware.ldappc.GrouperProvisionerOptions;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.LdappcException;
import edu.internet2.middleware.ldappc.MultiErrorException;
import edu.internet2.middleware.ldappc.ldap.OrganizationalUnit;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * This synchronizes groups stored in the directory as entries.
 */
public class GroupEntrySynchronizer extends GroupSynchronizer
{
    private static final Log LOG = GrouperUtil.getLog(GroupEntrySynchronizer.class);
    
    /**
     * Default size of group hash tables if not specified in configuration.
     */
    private static final int                   DEFAULT_HASH_SIZE = 100000;

    /**
     * Set of ou DNs to be deleted.
     */
    private Set<Name>                          deleteOus;

    /**
     * Set of ou DNs already processed.
     */
    private Set<Name>                          processedOus;

    /**
     * Set of group DNs to be deleted.
     */
    private Set<Name>                          deleteGroups;

    /**
     * Set of group DNs already processed.
     */
    private Set<Name>                          processedGroups;

    /**
     * Holds the objectClass attribute modifications.
     */
    private AttributeModifier                  objectClassMods;

    /**
     * Holds the member DN listing attribute modifications.
     */
    private AttributeModifier                  memberDnMods;

    /**
     * Holds the member name listing attribute modifications.
     */
    private AttributeModifier                  memberNameMods;

    /**
     * Holds the RDN attribute modifications.
     */
    private AttributeModifier                  rdnMods;

    /**
     * The keys are the mapped grouper attributes and the values are the
     * AttributeModifier for the associated Ldap attribute.
     * 
     * IMPORTANT NOTE: An AttributeModifier object maybe mapped for multiple
     * grouper attributes, and the AttributeModifier objects are the same as the
     * attribute values in mappedLdapAttributes. This allows multiple grouper
     * attributes to contribute values to the ldap attribute.
     */
    private HashMap<String, AttributeModifier> mappedGrouperAttributes;

    /**
     * The attribute names are the mapped ldap attributes, and the attribute
     * values are the AttributeModifiers associated with the attribute.
     * 
     * IMPORTANT NOTE: The AttributeModifier objects here are the same as the
     * values in the mappedGrouperAttributes.
     * 
     * NOTE: Used BasicAttributes object here to take advantage of case
     * insensitivity of attribute names. This is important as the ldapAttribute
     * names in grouper attribute to ldap attribute mapping may not all be in
     * the same case.
     */
    private BasicAttributes                    mappedLdapAttributes;

    /**
     * Constructs a <code>GroupEntrySynchronizer</code>.
     * 
     * @param ctx
     *            Ldap context to be used for synchronizing
     * @param root
     *            DN of the root element
     * @param configuration
     *            Grouper provisioning configuration
     * @param options
     *            Grouper provisioning options
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     * 
     * @throws NamingException
     *             Thrown when a naming exception occurs.
     * @throws LdappcConfigurationException
     *             Thrown if the configuration file is not correct.
     */
    public GroupEntrySynchronizer(LdapContext ctx, Name root, GrouperProvisionerConfiguration configuration,
            GrouperProvisionerOptions options, SubjectCache subjectCache)
            throws NamingException, LdappcConfigurationException
    {
        //
        // Call super constructor
        //
        super(ctx, root, configuration, options, subjectCache);

        int estimate = configuration.getGroupHashEstimate();
        if (estimate == 0)
        {
            estimate = DEFAULT_HASH_SIZE;
        }
        DebugLog.info("Group initial cache size = " + estimate);

        //
        // Init various objects
        //
        deleteOus = new HashSet<Name>(estimate);
        processedOus = new HashSet<Name>(estimate);
        deleteGroups = new HashSet<Name>(estimate);
        processedGroups = new HashSet<Name>(estimate);

        mappedGrouperAttributes = new HashMap<String, AttributeModifier>();
        mappedLdapAttributes = new BasicAttributes(true);

        //
        // If provisioning with "flat" structure, verify that a group naming
        // attribute is defined for the group ldap entry
        //
        if (GrouperProvisionerConfiguration.GROUP_DN_FLAT.equals(getConfiguration().getGroupDnStructure()))
        {
            if (configuration.getGroupDnGrouperAttribute() == null)
            {
                throw new LdappcConfigurationException("Group DN grouper attribute is not defined.");
            }
        }

        //
        // Verify that a object class is defined for the group ldap entry
        //
        if (configuration.getGroupDnObjectClass() == null)
        {
            throw new LdappcConfigurationException("Group ldap entry object class is not defined.");
        }

        //
        // If the RDN attribute name is defined and is not "ou", create the
        // attribute
        // modifier
        //
        String rdnAttrName = configuration.getGroupDnRdnAttribute();
        if (rdnAttrName == null || OrganizationalUnit.Attribute.OU.equalsIgnoreCase(rdnAttrName))
        {
            throw new LdappcConfigurationException("Group ldap entry RDN attribute name is invalid.");
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
        if (configuration.isGroupMembersDnListed())
        {
            //
            // Get the attribute name for storing Dns
            //
            String attrName = configuration.getGroupMembersDnListAttribute();
            if (attrName == null)
            {
                throw new LdappcConfigurationException("Group members DN list attribute name is not defined.");
            }

            //
            // Build the member Dn list attribute modifier
            //
            memberDnMods = new DnAttributeModifier(ctx.getNameParser(""), attrName, configuration
                    .getGroupMembersDnListEmptyValue());
        }

        //
        // If tracking member names, initialize related attributes
        //
        memberNameMods = null;
        if (configuration.isGroupMembersNameListed())
        {
            //
            // Get the attribute name for storing names
            //
            String attrName = configuration.getGroupMembersNameListAttribute();
            if (attrName == null)
            {
                throw new LdappcConfigurationException("Group members name list attribute name is not defined.");
            }

            //
            // Initialize the instance variable
            //
            memberNameMods = new AttributeModifier(attrName, configuration.getGroupMembersNameListEmptyValue());
        }

        //
        // Build attribute modifiers for the grouper to ldap attribute mapping
        //
        Map<String, String> attributeMap = configuration.getGroupAttributeMapping();
        for (String grouperAttr : attributeMap.keySet())
        {
            //
            // Get the next key (i.e., grouper attribute name) and the
            // corresponding value (i.e., ldap attribute name)
            String ldapAttr = (String) attributeMap.get(grouperAttr);

            //
            // If the ldapAttr is not yet defined in mappedLdapAttributes
            // with a modifier, add it
            //
            if (mappedLdapAttributes.get(ldapAttr) == null)
            {
                String emptyValue = configuration.getGroupAttributeMappingLdapEmptyValue(ldapAttr);
                mappedLdapAttributes.put(ldapAttr, new AttributeModifier(ldapAttr, emptyValue));
            }

            //
            // Get the AttributeModifier associated with the ldapAttr
            //
            AttributeModifier modifier = (AttributeModifier) mappedLdapAttributes.get(ldapAttr).get();

            //
            // Add the modifier to the mappedGrouperAttributes.
            // NOTE: this is the same modifier as was included for
            // the ldap attribute. This allows multiple grouper attributes
            // to contribute values to the ldap attribute.
            //
            mappedGrouperAttributes.put(grouperAttr, modifier);
        }
    }

    /**
     * This identifies the underlying group as one that must remain or, if need
     * be, must be added to the subject's LDAP entry. If the group has already
     * been provisioned to the entry, it will remain within the subject's LDAP
     * entry.
     * 
     * @param group
     *            Group to be included
     * @param status
     *            Either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
     *            {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void performInclude(Group group, int status) throws NamingException, LdappcException
    {
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
        if (!processedGroups.contains(groupDn))
        {
            //
            // Act based on whether or not the group DN exists already.
            //
            if (deleteGroups.remove(groupDn))
            {
                //
                // Update the group as needed
                //
                if (status == STATUS_NEW || status == STATUS_MODIFIED || status == STATUS_UNKNOWN)
                {
                    updateGroupEntry(groupDn, group);
                }
            }
            else
            {
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
        }
        else
        {
            //
            // groupDn has already been processed so log an error
            // (This should never happen).
            //
            ErrorLog.error(getClass(), "Group entry identified by " + groupDn + " has already been encountered,"
                    + " and will not be processed another time.");
        }
    }

    /**
     * This updates the group's ldap entry with the current data.
     * 
     * @param groupDn
     *            DN of the group entry
     * @param group
     *            Group associated with the group entry
     * @throws NamingException
     *             thrown if a Naming exception occurs
     */
    protected void updateGroupEntry(Name groupDn, Group group) throws NamingException
    {
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

        if (memberDnMods != null)
        {
            modifiers.add(memberDnMods);
        }

        if (memberNameMods != null)
        {
            modifiers.add(memberNameMods);
        }

        NamingEnumeration ldapAttrEnum = mappedLdapAttributes.getAll();
        while (ldapAttrEnum.hasMore())
        {
            Attribute attribute = (Attribute) ldapAttrEnum.next();
            modifiers.add((AttributeModifier) attribute.get());
        }

        //
        // Get all of the modifications
        //
        Vector<ModificationItem> modifications = new Vector<ModificationItem>();
        for (AttributeModifier modifier : modifiers)
        {
            ModificationItem[] items = modifier.getModifications();
            for (int i = 0; i < items.length; i++)
            {
                modifications.add(items[i]);
            }
        }

        //
        // Build the modification item array
        //
        ModificationItem[] modificationItems = new ModificationItem[modifications.size()];
        for (int i = 0; i < modificationItems.length; i++)
        {
            modificationItems[i] = (ModificationItem) modifications.get(i);
        }

        //
        // Modify the entry
        //
        if (modificationItems.length > 0)
        {
            LOG.debug("modify group '" + groupDn + "' " + Arrays.asList(modificationItems));
            getContext().modifyAttributes(groupDn, modificationItems);
            // DebugLog.info("Modified " + group.getName());
        }
    }

    /**
     * This populates the instance variables with the current values from the
     * group's LDAP entry.
     * 
     * @param groupDn
     *            DN of the group entry
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     */
    protected void initGroupData(Name groupDn) throws NamingException
    {
        //
        // Build the list of attributes needed from the entry
        //
        Vector<String> wantedAttr = new Vector<String>();

        wantedAttr.add(rdnMods.getAttributeName());

        if (memberDnMods != null)
        {
            wantedAttr.add(memberDnMods.getAttributeName());
        }

        if (memberNameMods != null)
        {
            wantedAttr.add(memberNameMods.getAttributeName());
        }

        NamingEnumeration mappedLdapAttrNames = mappedLdapAttributes.getIDs();
        while (mappedLdapAttrNames.hasMore())
        {
            wantedAttr.add((String) mappedLdapAttrNames.next());
        }

        wantedAttr.add(objectClassMods.getAttributeName());

        //
        // Get the existing attributes defined for the entry
        //
        Attributes attributes = getContext().getAttributes(groupDn, (String[]) wantedAttr.toArray(new String[0]));

        //
        // Populate the rdn attribute
        //
        populateAttrModifier(attributes, rdnMods);

        //
        // Populate the member dn list modifier if defined
        //
        if (memberDnMods != null)
        {
            populateAttrModifier(attributes, memberDnMods);
        }

        //
        // Populate the member name list modifier if defined
        //
        if (memberNameMods != null)
        {
            populateAttrModifier(attributes, memberNameMods);
        }

        //
        // Populate the mapped attributes modifiers
        //
        NamingEnumeration mappedLdapAttributeEnum = mappedLdapAttributes.getAll();
        while (mappedLdapAttributeEnum.hasMore())
        {
            Attribute ldapAttribute = (Attribute) mappedLdapAttributeEnum.next();
            populateAttrModifier(attributes, (AttributeModifier) ldapAttribute.get());
        }

        //
        // Populate the object class modifier and retain all of the values
        //
        populateAttrModifier(attributes, objectClassMods);
        objectClassMods.retainAll();
    }

    /**
     * This method populates an AttributeModifier with the associated attribute
     * provided in Attributes. If an associated attribute is not found in
     * <code>attributes</code>, <code>modifier</code> remains unchanged. If
     * an associated is found, <code>modifier</code> is initialized with the
     * attribute and the attribute is deleted from <code>attributes</code>.
     * 
     * @param attributes
     *            Attributes
     * @param modifier
     *            AttributeModifier to be populated
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    private void populateAttrModifier(Attributes attributes, AttributeModifier modifier) throws NamingException
    {
        Attribute attribute = attributes.get(modifier.getAttributeName());
        if (attribute != null)
        {
            modifier.init(attribute);
            attributes.remove(modifier.getAttributeName());
        }
    }

    /**
     * This stores the given Group's data in the AttributeModifiers. This stores
     * both the object class data from the configuration and the Group data in
     * the associated attribute modifiers.
     * 
     * @param group
     *            Group
     * @throws NamingException
     *             thrown if a naming error occurs
     */
    protected void storeGroupData(Group group) throws NamingException
    {
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
          for (Member member : members)
          {
              Subject subject = null;
              try
              {
                  subject = member.getSubject();
              }
              catch (SubjectNotFoundException snfe)
              {
                  //
                  // If the subject was not found, log it and continue
                  //
                  ErrorLog.warn(getClass(), getErrorData(member) + " Subject not found :: " + snfe.getMessage());
                  continue;
              }

              //
              // If maintaining member DN list, do it now
              //
              if (memberDnMods != null)
              {
                  try
                  {
                      Name subjectDn = getSubjectCache().findSubjectDn(getContext(), getConfiguration(), subject);
                      memberDnMods.store(subjectDn.toString());
                  }
                  catch (Exception e)
                  {
                      ErrorLog.warn(getClass(), getErrorData(subject) + " " + e.getMessage());
                  }
              }

              //
              // If maintaining member name list, do it now
              //
              if (memberNameMods != null)
              {
                  //
                  // Catch all of the exceptions thrown as they are "warning" and
                  // handle them in a common manner.
                  //
                  try
                  {
                      //
                      // Get the subject source
                      //
                      Source source = subject.getSource();
                      if (source == null)
                      {
                          throw new LdappcException("Source is null");
                      }

                      //
                      // Get the naming attribute for this source
                      //
                      String nameAttribute = getConfiguration().getGroupMembersNameListNamingAttribute(source.getId());
                      if (nameAttribute != null)
                      {
                          //
                          // Get the subject attribute value
                          //
                          String nameValue = subject.getAttributeValue(nameAttribute);
                          if (nameValue != null)
                          {
                              this.memberNameMods.store(nameValue);
                          }
                          else
                          {
                              throw new LdappcException("Naming attribute [" + nameAttribute + "] is not defined.");
                          }
                      }
                      else
                      {
                          throw new LdappcException("No group members name list naming attribute defined for source id ["
                                  + source.getId() + "]");
                      }
                  }
                  catch (Exception e)
                  {
                      //
                      // All of the exceptions thrown in this try are "warning"
                      // related so simply log them and continue on with
                      // processing.
                      //
                      ErrorLog.warn(getClass(), getErrorData(subject) + " " + e.getMessage());
                  }
              }
          }
        }

        //
        // Populate mapped attributes from the group
        //
        for (String groupAttribute : mappedGrouperAttributes.keySet())
        {
            //
            // If the group has this attribute populated, store it
            //
           
            //
            // Get the attribute value from the group
            //
            String groupAttributeValue = group.getAttributeOrNull(groupAttribute);                

            //
            // Only storing non-empty string attributes (i.e., length > 0)
            //
            if (groupAttributeValue != null && groupAttributeValue.length() > 0)
            {
                mappedGrouperAttributes.get(groupAttribute).store(groupAttributeValue);
            }
            //
            // Store noValue value if there are no values and noValue is defined
            //
            else if (mappedGrouperAttributes.get(groupAttribute).getNoValue() != null)
            {
                mappedGrouperAttributes.get(groupAttribute).store(mappedGrouperAttributes.get(groupAttribute).getNoValue());
            }
            else
            {
                ErrorLog.warn(getClass(), getErrorData(group) + " " + "The value for group attribute \""
                        + groupAttribute + "\" will not be stored as it is either an empty string or null.");
            }
        }
    }

    /**
     * This stores the object class data from the configuration in the
     * associated AttributeModifier.
     * 
     * @throws NamingException
     *             thrown if a naming exception occurs
     */
    protected void storeObjectClassData() throws NamingException
    {
        //
        // Store the group entry object class
        //
        objectClassMods.store(getConfiguration().getGroupDnObjectClass());

        //
        // If needed and defined, store the member dn list object class
        //
        if (memberDnMods != null)
        {
            String objectClass = getConfiguration().getGroupMembersDnListObjectClass();
            if (objectClass != null)
            {
                objectClassMods.store(objectClass);
            }
        }

        //
        // If needed and defined, store the member name list object class
        //
        if (memberNameMods != null)
        {
            String objectClass = getConfiguration().getGroupMembersNameListObjectClass();
            if (objectClass != null)
            {
                objectClassMods.store(objectClass);
            }
        }

        //
        // If defined, store the grouper attribute object class
        //
        String attrMapObjClass = getConfiguration().getGroupAttributeMappingObjectClass();
        if (attrMapObjClass != null)
        {
            objectClassMods.store(attrMapObjClass);
        }
    }

    /**
     * This creates a new Group ldap entry. The new entry is identified by
     * <code>groupDn</code> and is populated from <code>group</code>.
     * 
     * @param groupDn
     *            DN of the new entry
     * @param group
     *            Group holding the data for the new entry
     * 
     * @throws NamingException
     *             Thrown if a naming exception occurs.
     */
    protected void addGroupEntry(Name groupDn, Group group) throws NamingException
    {
        //
        // Get the group data
        //
        storeGroupData(group);

        //
        // Build list of all attribute modifiers possibly holding data for the
        // entry
        //
        Vector<AttributeModifier> modifiers = new Vector<AttributeModifier>();

        modifiers.add(objectClassMods);

        modifiers.add(rdnMods);

        if (memberDnMods != null)
        {
            modifiers.add(memberDnMods);
        }

        if (memberNameMods != null)
        {
            modifiers.add(memberNameMods);
        }

        NamingEnumeration ldapAttrEnum = mappedLdapAttributes.getAll();
        while (ldapAttrEnum.hasMore())
        {
            Attribute attribute = (Attribute) ldapAttrEnum.next();
            modifiers.add((AttributeModifier) attribute.get());
        }

        //
        // Get the attributes for building the new entry
        //
        BasicAttributes attributes = new BasicAttributes(true);

        for (AttributeModifier modifier : modifiers)
        {
            Attribute attribute = modifier.getAdditions();
            if (attribute.size() > 0)
            {
                attributes.put(attribute);
            }
        }

        //
        // Build the subject context
        //
        LOG.debug("create group '" + groupDn + "' attrs '" + attributes + "'");
        getContext().createSubcontext(groupDn, attributes);
        // DebugLog.info("Added " + group.getName());
    }

    /**
     * This builds the DN of the given group. Also this populates the
     * AttributeModifier with the Group's RDN value. In the event that the Group
     * naming structure is bushy, this calls {@link #buildStemOuEntries(Group)}
     * to build the necessary organizationalUnit entries.
     * 
     * @param group
     *            Group
     * @return DN for the associated LDAP entry
     * @throws NamingException
     *             thrown if a Naming error occurs.
     * @throws LdappcException
     *             thrown if the RDN attribute is not defined for the group.
     */
    protected Name buildGroupDn(Group group) throws NamingException, LdappcException
    {
        //
        // Initialize return value
        //
        Name groupDn = null;

        //
        // If DN structure is bushy, build stem Ou's and initialize the group DN
        // with the parent OU DN. Else, initialize the group DN with the root
        // DN.
        //
        if (GrouperProvisionerConfiguration.GROUP_DN_BUSHY.equals(getConfiguration().getGroupDnStructure()))
        {
            groupDn = buildStemOuEntries(group);
        }
        else
        {
            groupDn = (Name) getRoot().clone();
        }

        //
        // Get the group's rdn value
        //
        String rdnString = null;
        if (GrouperProvisionerConfiguration.GROUP_DN_FLAT.equals(getConfiguration().getGroupDnStructure()))
        {
            if (GrouperProvisionerConfiguration.GROUPER_NAME_ATTRIBUTE.equals(getConfiguration()
                    .getGroupDnGrouperAttribute()))
            {
                rdnString = group.getName();
            }
            else
            {
                rdnString = group.getUuid();
            }
        }
        else
        {
            //
            // Structure must be bushy so use the extension
            //
            rdnString = group.getExtension();
        }

        //
        // Add the rdn to the group Dn and the rdnMods
        //
        rdnMods.store(rdnString);
        groupDn = groupDn.add(rdnMods.getAttributeName() + "=" + LdapUtil.makeLdapNameSafe(rdnString));

        return groupDn;
    }

    /**
     * This builds the group's parent OU DN. Also, if necessary, this builds any
     * missing OU entries in the directory for the group's stem. The DNs of any
     * newly created OUs are placed into the list of OUs to be deleted. They
     * should be removed from the delete list and placed into the list of
     * processed OUs when the group entry is successfully created.
     * 
     * @param group
     *            Group
     * @return OU DN under which the group entry must be created.
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occured.
     * @see #updateProcessedOus(Name)
     */
    protected Name buildStemOuEntries(Group group) throws NamingException
    {
        //
        // Build an attribute list once for creating new ou entries
        // below. Note, the "ou" attribute is added below.
        //
        Attributes attributes = new BasicAttributes(true);
        attributes.put(new BasicAttribute(LdapUtil.OBJECT_CLASS_ATTRIBUTE, OrganizationalUnit.OBJECT_CLASS));

        //
        // Initialize the stemDn to be the root DN. This stemDn
        // is updated for each element of the group's stem below
        //
        Name stemDn = (Name) getRoot().clone();

        //
        // Get the group's parent stem, and tokenize it's name to build
        // the ou's for the group.
        //
        Stem stem = group.getParentStem();
        StringTokenizer stemTokens = new StringTokenizer(stem.getName(), STEM_DELIMITER);
        while (stemTokens.hasMoreTokens())
        {
            //
            // Get next stem token for the rdn value making sure it is Ldap name
            // safe
            //
            String rdnString = stemTokens.nextToken();
            String rdnValue = LdapUtil.makeLdapNameSafe(rdnString);

            //
            // Build the new name (keep adding on to previous)
            //
            stemDn = stemDn.add(OrganizationalUnit.Attribute.OU + "=" + rdnValue);

            //
            // If stemDn hasn't been processed, process it based on whether it
            // already exists
            //
            if (!processedOus.contains(stemDn))
            {
                //
                // If it isn't deleted from deleteOus, create it
                //
                if (!deleteOus.contains(stemDn))
                {
                    //
                    // Build the new OU
                    //
                    attributes.put(OrganizationalUnit.Attribute.OU, rdnString);
                    getContext().createSubcontext(stemDn, attributes);

                    //
                    // Add it to deleteOus so if the group isn't processed it
                    // will be deleted. If the group is processed correctly,
                    // these will need to be moved to processedOus
                    //
                    deleteOus.add((Name) stemDn.clone());
                }
            }
        }

        return stemDn;
    }

    /**
     * This updates the list of processed OUs with those identified from the
     * group's DN. Any of the OU DNs found in the list of OUs to be deleted are
     * removed from deletion list as well. This assumes that any parent DN
     * between the root DN and the group DN identifies an OU associated with the
     * group's parent stem.
     * 
     * @param groupDn
     *            DN of the group entry
     */
    protected void updateProcessedOus(Name groupDn)
    {
        for (int i = getRoot().size() + 1; i < groupDn.size(); i++)
        {
            Name stemDn = groupDn.getPrefix(i);
            deleteOus.remove(stemDn);
            processedOus.add(stemDn);
        }
    }

    /**
     * Perform any initialization prior to processing the set of groups.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     */
    protected void initialize() throws NamingException, LdappcException
    {
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
     * This deletes any entries under the root entry are neither
     * organizationalUnits nor have the same object class as a group entry.
     * 
     * @throws NamingException
     *             Thrown if a naming exception occurs.
     */
    protected void clearRoot() throws NamingException
    {
        //
        // Build the query filter to find all existing
        // children under the root that are not object class type of the group
        // entries, and if needed not organizationalUnit entries
        //
        String filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + getConfiguration().getGroupDnObjectClass() + ")";

        if (GrouperProvisionerConfiguration.GROUP_DN_BUSHY.equals(getConfiguration().getGroupDnStructure()))
        {
            filter = "(|" + filter + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + OrganizationalUnit.OBJECT_CLASS
                    + "))";
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
        NamingEnumeration searchEnum = getContext().search(getRoot(), filter, searchControls);

        //
        // Delete anything found here and it's children.
        //
        NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);
        while (searchEnum.hasMore())
        {
            SearchResult searchResult = (SearchResult) searchEnum.next();
            if (searchResult.isRelative())
            {
                //
                // Get the entry's name. If it is "" then is must be the root so
                // just continue as it isn't processed
                //
                String entryName = searchResult.getName();
                if (entryName.length() == 0)
                {
                    continue;
                }

                //
                // Build the entry's DN
                //
                Name entryDn = parser.parse(entryName);
                entryDn = entryDn.addAll(0, getRoot());

                //
                // Try to find it as may already been deleted. If not found,
                // just continue
                //
                try
                {
                    getContext().lookup(entryDn);
                }
                catch (NamingException ne)
                {
                    //
                    // Assume it couldn't be found, so just continue
                    //
                    continue;
                }

                //
                // Try to delete it
                //
                try
                {
                    LdapUtil.delete(getContext(), entryDn);
                }
                catch (Exception e)
                {
                    ErrorLog.error(getClass(), "Unable to delete " + entryDn);
                }
            }
            else
            {
                //
                // Log it and continue on.
                //
                ErrorLog.error(this.getClass(), "Unable to handle LDAP URL references: " + searchResult.getName());
            }
        }
    }

    /**
     * This populates this object with the current values from the root's LDAP
     * entry that are either organizational units or have a object class type of
     * the group entry.
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     */
    protected void populate() throws NamingException
    {
        //
        // Build search controls for the searched performed below
        //
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setCountLimit(0);

        //
        // Populate the group deletes
        //
        String filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + getConfiguration().getGroupDnObjectClass() + ")";
        populateDns(deleteGroups, filter, searchControls);

        //
        // If necessary, populate the stem ou deletes
        //
        if (GrouperProvisionerConfiguration.GROUP_DN_BUSHY.equals(getConfiguration().getGroupDnStructure()))
        {
            filter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "=" + OrganizationalUnit.OBJECT_CLASS + ")";
            populateDns(deleteOus, filter, searchControls);
        }
    }

    /**
     * This populates the given Set with the DNs of any child entries of the
     * root DN matching the given filter.
     * 
     * @param dns
     *            Set to hold the DNs
     * @param filter
     *            LDAP Filter
     * @param searchControls
     *            Search controls
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    protected void populateDns(Set dns, String filter, SearchControls searchControls) throws NamingException
    {
        //
        // Perform the search of the root
        //
        NamingEnumeration searchEnum = getContext().search(getRoot(), filter, searchControls);

        //
        // Populate dns with DNs of existing objects. The root dn is excluded.
        //
        NameParser parser = getContext().getNameParser(LdapUtil.EMPTY_NAME);
        while (searchEnum.hasMore())
        {
            SearchResult searchResult = (SearchResult) searchEnum.next();
            if (searchResult.isRelative())
            {
                //
                // Get the name string. If empty it is the root so ignore
                //
                String entryName = searchResult.getName();
                if (entryName.length() == 0)
                {
                    continue;
                }

                //
                // Build the entry's DN
                //
                Name entryDn = parser.parse(entryName);
                entryDn = entryDn.addAll(0, getRoot());

                //
                // Add entryDn to the deletes list
                //
                dns.add(entryDn);
            }
            else
            {
                //
                // Log it and continue on.
                //
                ErrorLog.error(this.getClass(), "Unable to handle LDAP URL references: " + searchResult.getName());
            }
        }
    }

    /**
     * Initializes the attributes needed for holding data for the given group.
     * 
     * @param group
     *            Group
     */
    protected void initializeInclude(Group group)
    {
        //
        // Clear existing values
        //
        objectClassMods.clear();
        rdnMods.clear();
        if (memberDnMods != null)
        {
            memberDnMods.clear();
        }

        if (memberNameMods != null)
        {
            memberNameMods.clear();
        }

        for (AttributeModifier modifier : mappedGrouperAttributes.values())
        {
            modifier.clear();
        }
    }

    /**
     * This commits any changes not already committed to the directory.
     * 
     * @throws NamingException
     *             thrown if a Naming error occurs
     * @throws LdappcException
     *             thrown if an error occurs
     * 
     * @see edu.internet2.middleware.ldappc.synchronize.MembershipSynchronizer#commit()
     */
    protected void commit() throws NamingException, LdappcException
    {
        //
        // Init vars
        //
        Vector<NamingException> namingExceptions = new Vector<NamingException>();
        LdapContext context = getContext();

        //
        // Delete any group entries that are no longer valid
        //
        for (Name dn : deleteGroups)
        {
            try
            {
                //
                // Delete it
                //
                LdapUtil.delete(context, dn);
            }
            catch (NamingException ne)
            {
                ErrorLog.error(getClass(), "Failed to delete " + dn + " for " + getRoot() + " :: " + ne.getMessage());
                namingExceptions.add(ne);
            }
        }

        //
        // Delete any ou entries that are no longer valid
        //
        for (Name dn : deleteOus)
        {
            //
            // Try to find it. If not found, just continue
            //
            try
            {
                getContext().lookup(dn);
            }
            catch (NamingException ne)
            {
                //
                // Assume it couldn't be found, so just continue
                //
                continue;
            }

            //
            // Try to delete it
            //
            try
            {
                LdapUtil.delete(context, dn);
            }
            catch (NamingException ne)
            {
                ErrorLog.error(getClass(), "Failed to delete " + dn + " for " + getRoot() + " :: " + ne.getMessage());
                namingExceptions.add(ne);
            }
        }

        //
        // If necessary, thrown an exception
        //
        if (namingExceptions.size() > 0)
        {
            throw new MultiErrorException((Exception[]) namingExceptions.toArray(new Exception[0]));
        }
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param subject
     *            Subject
     * @return data string for error message
     */
    protected String getErrorData(Subject subject)
    {
        return "SUBJECT[" + getSubjectCache().getSubjectData(subject) + "]";
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param group
     *            Group
     * @return data string for error message
     */
    protected String getErrorData(Group group)
    {
        return "GROUP[" + GrouperProvisioner.getGroupData(group) + "]";
    }

    /**
     * Builds an error data string based on the objects provided.
     * 
     * @param member
     *            Member
     * @return member data string
     */
    protected String getErrorData(Member member)
    {
        return "MEMBER[" + GrouperProvisioner.getMemberData(member) + "]";
    }
}
