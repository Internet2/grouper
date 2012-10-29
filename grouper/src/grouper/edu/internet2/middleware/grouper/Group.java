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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignEffMshipDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignGroupDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignMembershipDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtilsMapping;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.permissions.role.RoleInheritanceDelegate;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.rules.beans.RulesPrivilegeBean;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddAlternateGroupNameValidator;
import edu.internet2.middleware.grouper.validator.AddCompositeMemberValidator;
import edu.internet2.middleware.grouper.validator.CanOptinValidator;
import edu.internet2.middleware.grouper.validator.CanOptoutValidator;
import edu.internet2.middleware.grouper.validator.CompositeValidator;
import edu.internet2.middleware.grouper.validator.FieldTypeValidator;
import edu.internet2.middleware.grouper.validator.GetGroupAttributeValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NamingValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroup;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/** 
 * A group within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.269 2009-12-15 06:47:06 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class Group extends GrouperAPI implements Role, GrouperHasContext, Owner, 
    Hib3GrouperVersioned, Comparable, XmlImportable<Group>, AttributeAssignable, Entity {

  /** name of the groups table in the db */
  public static final String TABLE_GROUPER_GROUPS = "grouper_groups";
  
  /** uuid col in db (not used anymore) */
  public static final String COLUMN_UUID = "uuid";
  
  /** id col in db */
  public static final String COLUMN_ID = "id";
  
  /** col in db */
  public static final String COLUMN_PARENT_STEM = "parent_stem";
  
  /** col in db */
  public static final String COLUMN_CREATOR_ID = "creator_id";

  /** col in db */
  public static final String COLUMN_CREATE_TIME = "create_time";

  /** col in db */
  public static final String COLUMN_MODIFIER_ID = "modifier_id";

  /** col in db */
  public static final String COLUMN_MODIFY_TIME = "modify_time";
  
  /** col in db */
  public static final String COLUMN_NAME = "name";
  
  /** col in db */
  public static final String COLUMN_DISPLAY_NAME = "display_name";
  
  /** col in db */
  public static final String COLUMN_EXTENSION = "extension";

  /** col in db */
  public static final String COLUMN_DISPLAY_EXTENSION = "display_extension";

  /** col in db */
  public static final String COLUMN_DESCRIPTION = "description";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_UUID = "old_uuid";
  
  /** timestamp of the last membership change for this group */
  public static final String COLUMN_LAST_MEMBERSHIP_CHANGE = "last_membership_change";
  
  /** timestamp of the last immediate membership change for this group */
  public static final String COLUMN_LAST_IMMEDIATE_MEMBERSHIP_CHANGE = "last_imm_membership_change";
  
  /** an alternate name for this group */
  public static final String COLUMN_ALTERNATE_NAME = "alternate_name";
  
  /** if this is a group or role */
  public static final String COLUMN_TYPE_OF_GROUP = "type_of_group";
  
  /** unique number for this group */
  public static final String COLUMN_ID_INDEX = "id_index";
  
  /**
   * if this is a composite group, get the composite object for this group
   * @return the composite group
   * @throws CompositeNotFoundException if composite not found
   * @deprecated use the overload with boolean instead
   */
  @Deprecated
  public Composite getComposite() throws CompositeNotFoundException {
    return this.getComposite(true);
  }
  
  /**
   * init group attributes in one query (old style attributes)
   * @param objects could be groups
   */
  public static void initGroupObjectAttributes(Collection<Object> objects) {
    
    Set<Group> groups = new HashSet<Group>();
    
    for (Object object : objects) {
      
      if (object instanceof Group) {
        groups.add((Group)object);
      }
      
    }
    
    initGroupAttributes(groups);
  }
  
  /**
   * init group attributes in one query (old style attributes)
   * @param groups
   */
  public static void initGroupAttributes(Collection<Group> groups) {

    //if there are none, or if this isnt the group source
    if (GrouperUtil.length(groups) == 0) {
      return;
    }
    Set<String> groupIds = new HashSet<String>();
    
    Map<String, Group> grouperGroupMap = new HashMap<String, Group>();
    
    //get the grouper subjects
    for (Group group : groups) {
      
      if (group.attributes == null) {
      
        groupIds.add(group.getId());
        grouperGroupMap.put(group.getId(), group);
      }
    }
    if (GrouperUtil.length(groupIds) == 0) {
      return;
    }
    
    Map<String, Map<String, Attribute>> groupIdToAttributeMap = GrouperDAOFactory.getFactory()
      .getAttribute().findAllAttributesByGroups(groupIds);
    
    //go through the groups in results
    for (String groupId : GrouperUtil.nonNull(groupIdToAttributeMap).keySet()) {
      Map<String, Attribute> attributeMap = groupIdToAttributeMap.get(groupId);
      
      //if there are attributes, add them to the group so they dont have to be fetched later.
      if (attributeMap != null) {
        Group group = grouperGroupMap.get(groupId);
        group.internal_setAttributes(attributeMap);
      }
    }
    
  }
  
  /**
   * if this is a composite group, get the composite object for this group
   * @param throwExceptionIfNotFound 
   * @return the composite group or null if none
   * @throws CompositeNotFoundException if not found and throwExceptionIfNotFound is true
   */
  public Composite getComposite(boolean throwExceptionIfNotFound) {
    try {
      return CompositeFinder.findAsOwner(this, true);
    } catch (CompositeNotFoundException cnfe) {
      if (throwExceptionIfNotFound) {
        throw cnfe;
      }
      return null;
    }
  }
  
  /**
   * <pre>
   * create or update a group.  Note this will not rename a group at this time (might in future)
   * 
   * This is a static method since setters to Group objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the group by groupNameToEdit
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the group (insert or update) if needed
   * 4. Return the group object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @param GROUPER_SESSION to act as
   * @param groupNameToEdit is the name of the group to edit (or null if insert)
   * @param description new description for group
   * @param displayExtension display friendly name for this group only
   * (parent stems are not specified)
   * @param name this is required, and is the full name of the group
   * including the names of parent stems.  e.g. stem1:stem2:stem3
   * the parent stem must exist unless createParentStemsIfNotExist.  
   * Can rename a stem extension, but not the parent stem name (move)
   * @param uuid of the group.  If a group exists with this uuid, then it will
   * be updated, if not, then it will be created if createIfNotExist is true
   * @param saveMode to constrain if insert only or update only, if null defaults to INSERT_OR_UPDATE
   * @param createParentStemsIfNotExist true if the stems should be created if they dont exist, false
   * for StemNotFoundException if not exist.  Note, the display extension on created stems
   * will equal the extension
   * @return the stem that was updated or created
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException 
   * @throws StemAddException 
   * @throws GroupModifyException 
   * @throws GroupNotFoundException
   * @throws GroupAddException
   */
  public static Group saveGroup(final GrouperSession GROUPER_SESSION, final String groupNameToEdit,
      final String uuid, final String name, final String displayExtension, final String description, 
      SaveMode saveMode, final boolean createParentStemsIfNotExist) 
        throws StemNotFoundException, InsufficientPrivilegeException, StemAddException, 
        GroupModifyException, GroupNotFoundException, GroupAddException {

    GroupSave groupSave = new GroupSave(GROUPER_SESSION);

    groupSave.assignGroupNameToEdit(groupNameToEdit).assignUuid(uuid);
    groupSave.assignName(name).assignDisplayExtension(displayExtension);
    groupSave.assignDescription(description).assignSaveMode(saveMode);
    groupSave.assignCreateParentStemsIfNotExist(createParentStemsIfNotExist);
    Group group = groupSave.save();

    return group;

  }
  
  /** */
  private static final  EventLog                  EVENT_LOG            = new EventLog();
  /** */
  private static final  String                    KEY_CREATOR   = "creator";  // for state caching 
  /** */
  private static final  String                    KEY_MODIFIER  = "modifier"; // for state caching
  /** */
  private static final  String                    KEY_SUBJECT   = "subject";  // for state caching
  
  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member cachedMember  = null;

  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private               HashMap<String, Subject>  subjectCache  = new HashMap<String, Subject>();
  // TODO 20070531 review lazy-loading to improve consistency + performance

  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Map<String, Attribute>       attributes;
  /** */
  private long      createTime      = 0; // default to the epoch
  /** */
  private String    creatorUUID;
  
  /** */
  private String    modifierUUID;
  /** */
  private long      modifyTime      = 0; // default to the epoch
  /** */
  private String    parentUuid;

  /** default to group type, as opposed to role */
  private TypeOfGroup typeOfGroup = TypeOfGroup.group;
  
  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Set<GroupType>       types;
  
  /** */
  private String    uuid;
  
  /** name of group, e.g. school:community:students */
  private String name;
  
  /** alternate name of group */
  private String alternateNameDb;
  
  /** displayName of group, e.g. My School:Community Groups:All Students */
  private String displayName;
  
  /** extension of group, e.g. students */
  private String extension;
  
  /** id of the group as a unique integer */
  private Long idIndex;
  
  /** displayExtension of group, e.g. All Students */
  private String displayExtension;
  
  /** description of group, friendly description, e.g. in sentence form, about what the group is about */
  private String description;
  
  /** context id of the transaction */
  private String contextId;

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }


  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: alternateNameDb */
  public static final String FIELD_ALTERNATE_NAME_DB = "alternateNameDb";
  
  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: displayExtension */
  public static final String FIELD_DISPLAY_EXTENSION = "displayExtension";

  /** constant for field name for: displayName */
  public static final String FIELD_DISPLAY_NAME = "displayName";

  /** constant for field name for: extension */
  public static final String FIELD_EXTENSION = "extension";
  
  /** constant for field name for: idIndex */
  public static final String FIELD_ID_INDEX = "idIndex";
  
  /** constant for field name for: lastMembershipChangeDb */
  public static final String FIELD_LAST_MEMBERSHIP_CHANGE_DB = "lastMembershipChangeDb";

  /** constant for field name for: lastImmediateMembershipChangeDb */
  public static final String FIELD_LAST_IMMEDIATE_MEMBERSHIP_CHANGE_DB = "lastImmediateMembershipChangeDb";
  
  /** constant for field name for: modifierUUID */
  public static final String FIELD_MODIFIER_UUID = "modifierUUID";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: parentUuid */
  public static final String FIELD_PARENT_UUID = "parentUuid";

  /** constant for field name for: typeOfGroup */
  public static final String FIELD_TYPE_OF_GROUP = "typeOfGroup";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_MODIFIER_UUID, 
      FIELD_MODIFY_TIME, FIELD_NAME, FIELD_PARENT_UUID, FIELD_TYPE_OF_GROUP, FIELD_UUID, 
      FIELD_ID_INDEX,
      FIELD_ALTERNATE_NAME_DB, FIELD_LAST_MEMBERSHIP_CHANGE_DB, FIELD_LAST_IMMEDIATE_MEMBERSHIP_CHANGE_DB);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DB_VERSION, 
      FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID_INDEX, FIELD_MODIFIER_UUID, FIELD_MODIFY_TIME, FIELD_NAME, 
      FIELD_PARENT_UUID, FIELD_TYPE_OF_GROUP, FIELD_UUID, FIELD_LAST_MEMBERSHIP_CHANGE_DB, 
      FIELD_ALTERNATE_NAME_DB, FIELD_LAST_IMMEDIATE_MEMBERSHIP_CHANGE_DB);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignGroupDelegate attributeAssignGroupDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignGroupDelegate getAttributeDelegate() {
    if (this.attributeAssignGroupDelegate == null) {
      this.attributeAssignGroupDelegate = new AttributeAssignGroupDelegate(this);
    }
    return this.attributeAssignGroupDelegate;
  }
  
  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeValueDelegate attributeValueDelegate;
  
  /**
   * this delegate works on attributes and values at the same time
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegate() {
    if (this.attributeValueDelegate == null) {
      this.attributeValueDelegate = new AttributeValueDelegate(this.getAttributeDelegate());
    }
    return this.attributeValueDelegate;
  }
  
  /**
   * delegate for effective memberships
   * @param member 
   * @return the delegate
   */
  public AttributeAssignEffMshipDelegate getAttributeDelegateEffMship(Member member) {
    return new AttributeAssignEffMshipDelegate(this, member);
  }
  
  /**
   * this delegate works on attributes and values at the same time
   * @param member 
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegateEffMship(Member member) {
    return new AttributeValueDelegate(this.getAttributeDelegateEffMship(member));
  }
  

  /**
   * delegate for effective memberships
   * @param member 
   * @return the delegate
   */
  public AttributeAssignMembershipDelegate getAttributeDelegateMembership(Member member) {
    return getAttributeDelegateMembership(member, Group.getDefaultList());
  }
  
  /**
   * delegate for effective memberships
   * @param member 
   * @param field 
   * @return the delegate
   */
  public AttributeAssignMembershipDelegate getAttributeDelegateMembership(Member member, Field field) {
    Membership membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), 
        this, member.getSubject(), field, false);
    if (membership == null) {
      throw new RuntimeException("Cannot get the immediate membership attribute delegate if not an immediate member: " 
          + this + ", " + GrouperUtil.subjectToString(member.getSubject()) + ", " + field);
    }
    return new AttributeAssignMembershipDelegate(membership);
  }
  
  /**
   * this delegate works on attributes and values at the same time
   * @param member 
   * @param field 
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegateMembership(Member member, Field field) {
    return new AttributeValueDelegate(this.getAttributeDelegateMembership(member, field));
  }

  /**
   * this delegate works on attributes and values at the same time
   * @param member 
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegateMembership(Member member) {
    return new AttributeValueDelegate(this.getAttributeDelegateMembership(member));
  }


  /**
   * Retrieve default members {@link Field}.
   * <pre class="eg">
   * Field members = Group.getDefaultList();
   * </pre>
   * @return  The "members" {@link Field}
   * @throws  GrouperException
   */
  public static Field getDefaultList() throws  GrouperException {
    return FieldFinder.find(GrouperConfig.LIST, true);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Group.class);

  /**
   * Add ore replace a composite membership to this group.
   * <pre class="eg">
   * try {
   *   g.assignCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add composite membership
   * } 
   * </pre>
   * @param   type  Add membership of this {@link CompositeType}.
   * @param   left  {@link Group} that is left factor of of composite membership.
   * @param   right {@link Group} that is right factor of composite membership.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws MemberDeleteException 
   * @since   1.0
   */
  public void assignCompositeMember(final CompositeType type, final Group left, final Group right)
    throws  InsufficientPrivilegeException, MemberAddException, MemberDeleteException {

    final String errorMessageSuffix = ", group name: " + this.name + ", compositeType: " + type
      + ", left group name: " + (left == null ? "null" : left.getName()) 
      + ", right group name: " + (right == null ? "null" : right.getName());

    Composite composite = null;
    try {
      composite = this.getComposite();
    } catch (CompositeNotFoundException cnfe) {
      
    }
    if (composite != null) {
      
      //if not equal, replace, otherwise leave alone
      if (!composite.getTypeDb().equals(type.getName())
          || !composite.getLeftFactorUuid().equals(left.getUuid())
          || !composite.getRightFactorUuid().equals(right.getUuid())) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Deleting and adding composite member for group: " + this.getExtension() + ": " 
              + type.getName() + ": " + left.getExtension() + " - " + right.getExtension());
        }
        
        final StringBuilder differences = new StringBuilder();
        if (!composite.getTypeDb().equals(type.getName())) {
          differences.append("type from: " + composite.getTypeDb() + " to: " + type.getName());
        }
        if (!composite.getLeftFactorUuid().equals(left.getUuid())) {
          try {
            differences.append("left group from: " + composite.getLeftGroup().getName() + ", to: " + left.getName());
          } catch (GroupNotFoundException gnfe) {
            differences.append("left group from: " + composite.getLeftFactorUuid() + ", to: " + left.getName());
          }
        }
        if (!composite.getRightFactorUuid().equals(right.getUuid())) {
          try {
            differences.append("right group from: " + composite.getRightGroup().getName() + ", to: " + right.getName());
          } catch (GroupNotFoundException gnfe) {
            differences.append("right group from: " + composite.getRightFactorUuid() + ", to: " + right.getName());
          }
        }
        final Composite COMPOSITE = composite;

        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
            new HibernateHandler() {

              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                
                try {

                  hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

                  Group.this.deleteCompositeMember();
                  Group.this.addCompositeMember(type, left, right);
                  
                  if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                    
                    AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_COMPOSITE_UPDATE, "id", 
                        COMPOSITE.getUuid(), "ownerId", Group.this.getUuid(), "ownerName", Group.this.getName(), "leftFactorId", 
                        left.getUuid(), "leftFactorName", left.getName(), "rightFactorId", right.getUuid(), 
                        "rightFactorName", right.getName(), "type", type.toString());
                    auditEntry.setDescription("Updated composite: " + Group.this.getName() + ", " + differences.toString());
                    auditEntry.saveOrUpdate(true);
                  }

                  return null;
                } catch (MemberAddException mae) {
                  GrouperUtil.injectInException(mae, errorMessageSuffix);
                  throw mae;
                } catch (RuntimeException re) {
                  GrouperUtil.injectInException(re, errorMessageSuffix);
                  throw re;
                }
              }
            });
      }
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Adding composite member for group: " + this.getExtension() + ": " 
            + type.getName() + ": " + left.getExtension() + " - " + right.getExtension());
      }
      this.addCompositeMember(type, left, right);
    }
    
  }
  
  /**
   * Add a composite membership to this group.
   * <pre class="eg">
   * try {
   *   g.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add composite membership
   * } 
   * </pre>
   * @param   type  Add membership of this {@link CompositeType}.
   * @param   left  {@link Group} that is left factor of of composite membership.
   * @param   right {@link Group} that is right factor of composite membership.
   * @return composite
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @since   1.0
   */
  public Composite addCompositeMember(CompositeType type, Group left, Group right)
    throws  InsufficientPrivilegeException,
            MemberAddException {
    return internal_addCompositeMember(GrouperSession.staticGrouperSession(), type, left, right, null);
  } 

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Subject subj) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    this.addMember(subj, true);
  } // public void addMember(subj)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @return false if it already existed, true if it didnt already exist
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public boolean addMember(Subject subj, boolean exceptionIfAlreadyMember) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      Field defaultList = getDefaultList();
      return this.addMember(subj, defaultList, exceptionIfAlreadyMember);
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage(), eS);
    }
  } // public void addMember(subj)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   */
  public void addMember(Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    this.addMember(subj, f, true);
  } // public void addMember(subj, f)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean addMember(final Subject subj, final Field f, final boolean exceptionIfAlreadyMember)
    throws  InsufficientPrivilegeException,
            MemberAddException, SchemaException {
    return this.internal_addMember(subj, f, exceptionIfAlreadyMember, null);
  }

  /**
   * replace the member list with new list.  Note this is not done in a tx so it can make as much progress as possible
   * though feel free to put a tx outside of the call if you need it
   * @param newSubjectList
   * @return the number of members changed
   */
  public int replaceMembers(Collection<Subject> newSubjectList) {
    return replaceMembers(newSubjectList, Group.getDefaultList());
  }

  /**
   * replace the member list with new list.  Note this is not done in a tx so it can make as much progress as possible
   * though feel free to put a tx outside of the call if you need it
   * @param newSubjectList
   * @param field
   * @return the number of members changed
   */
  public int replaceMembers(Collection<Subject> newSubjectList, Field field) {
    
    Map<String, Object> infoMap = new HashMap<String, Object>();
    infoMap.put("operation", "replaceMembers");
    infoMap.put("groupName", this.getName());
    
    try {
      Set<Member> existingMembers = GrouperUtil.nonNull(this.getMembers(field));
      
      infoMap.put("existingMemberListSize", GrouperUtil.length(existingMembers));
      infoMap.put("newMemberListSize", GrouperUtil.length(newSubjectList));
      
      //multikey is the sourceId to the subjectId
      Map<MultiKey, Member> existingMemberMap = new HashMap<MultiKey, Member>();
      
      int changedRecords = 0;
      
      //register all the existing subjects
      for (Member member : existingMembers) {
        
        MultiKey multiKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
        
        existingMemberMap.put(multiKey, member);
        
      }
  
      //lets clone this since we will tear it down
      Map<MultiKey, Subject> newMemberMap = new HashMap<MultiKey, Subject>();
      
      //lets see which ones are there already
      for (Subject subject : GrouperUtil.nonNull(newSubjectList)) {
        
        MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
        
        newMemberMap.put(multiKey, subject);
        
      }
      
      //lets remove from both the ones from both lists that already exist
      Iterator<MultiKey> newMemberListIterator = newMemberMap.keySet().iterator();
      
      while (newMemberListIterator.hasNext()) {
        
        MultiKey newMemberMultiKey = newMemberListIterator.next();
        
        if (existingMemberMap.containsKey(newMemberMultiKey)) {
          newMemberListIterator.remove();
          existingMemberMap.remove(newMemberMultiKey);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Replace members on " + this.getName() + ": Subject " 
                + newMemberMultiKey.getKey(0) + " - " + newMemberMultiKey.getKey(1) + " already is in the list");
          }
        }
      }
  
      int addedMemberCount = 0;
      
      //now the new list is the adds, and the existing list is the removes...
      //lets do that adds
      for (MultiKey newMemberMultiKey : newMemberMap.keySet()) {
        
        Subject newSubject = newMemberMap.get(newMemberMultiKey);
        
        this.addMember(newSubject, field, true);
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Replace members on " + this.getName() + ": Subject " 
              + newMemberMultiKey.getKey(0) + " - " + newMemberMultiKey.getKey(1) + " is added");
        }
  
        changedRecords++;
        addedMemberCount++;
      }

      infoMap.put("addedMemberCount", addedMemberCount);
      
      int deletedMemberCount = 0;
      //now lets do the removes
      for (MultiKey removeMultiKey : existingMemberMap.keySet()) {
        
        Member removeMember = existingMemberMap.get(removeMultiKey);
        
        this.deleteMember(removeMember, field, true);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Replace members on " + this.getName() + ": Subject " 
              + removeMultiKey.getKey(0) + " - " + removeMultiKey.getKey(1) + " already is removed");
        }

        changedRecords++;
        deletedMemberCount++;
      }
      
      infoMap.put("deletedMemberCount", deletedMemberCount);
      infoMap.put("changedRecords", changedRecords);
      
      if (LOG.isInfoEnabled()) {
        LOG.info(GrouperUtil.mapToString(infoMap));
      }
      
      return changedRecords;
    } catch (RuntimeException re) {
      
      LOG.error(GrouperUtil.mapToString(infoMap));
      throw re;
      
    }
  }
  
  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @param uuid is uuid or null for generated
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean internal_addMember(final Subject subj, final Field f, final boolean exceptionIfAlreadyMember, final String uuid)
    throws  InsufficientPrivilegeException,
            MemberAddException, SchemaException {

    final StopWatch sw = new StopWatch();
    sw.start();
    
    final String errorMessageSuffix = ", group name: " + this.name 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", field: " + (f == null ? null : f.getName());
  
    return (Boolean)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              if ( !FieldType.LIST.equals( f.getType() ) ) {
                throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
              }
              if ( !Group.this.canWriteField(f) ) { 
                GrouperValidator v = CanOptinValidator.validate(Group.this, subj, f);
                if (v.isInvalid()) {
                  throw new InsufficientPrivilegeException();
                }
              }
              //MCH 20090301: I would think this should be any member list (non privilege... not just default)
              if ( ( Group.getDefaultList().equals(f) ) && ( Group.this.hasComposite() ) ) {
                throw new MemberAddException(E.GROUP_AMTC + ", " + Group.this.getName());
              }
              boolean doesntExist = true;
              Membership membership = null;
              
              try {
                membership = Membership.internal_addImmediateMembership( GrouperSession.staticGrouperSession(), Group.this, subj, f , uuid);
                
              } catch (MemberAddAlreadyExistsException memberAddAlreadyExistsException) {
                if (exceptionIfAlreadyMember) {
                  throw memberAddAlreadyExistsException;
                }
                doesntExist = false;
              } catch (MembershipAlreadyExistsException membershipAlreadyExistsException) {
                if (exceptionIfAlreadyMember) {
                  //convert to member add
                  throw new MemberAddAlreadyExistsException(membershipAlreadyExistsException);
                }
                doesntExist = false;
              } catch (HookVeto hookVeto) {
                //pass this through
                throw hookVeto;
              } catch (Exception exception) {
                throw new RuntimeException(exception);
              }
              if (doesntExist) {
                
                //this might be a wheel or a member of wheel...  maybe there is a more efficient way to do this...
                PrivilegeHelper.flushCache();
                
                EVENT_LOG.groupAddMember(GrouperSession.staticGrouperSession(), Group.this.getName(), subj, f, sw);
                
                RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(membership, Group.this, subj);

                //if we are in the default list, then fire a rule
                if (StringUtils.equals(f.getUuid(), Group.getDefaultList().getUuid())) {
                  
                  //fire rules directly connected to this membership add
                  RuleEngine.fireRule(RuleCheckType.membershipAdd, rulesMembershipBean);
                  //fire rules related to add in stem
                  RuleEngine.fireRule(RuleCheckType.membershipAddInFolder, rulesMembershipBean);

                }

                //fire rules related to subject assign in folder
                RuleEngine.fireRule(RuleCheckType.subjectAssignInStem, rulesMembershipBean);

                if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                  
                  AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_ADD, "id", 
                      membership == null ? null : membership.getUuid(), "fieldId", f.getUuid(),
                          "fieldName", f.getName(), "memberId",  membership.getMemberUuid(),
                          "membershipType", membership.getType(), 
                          "groupId", Group.this.getUuid(), "groupName", Group.this.getName());
                          
                  auditEntry.setDescription("Added membership: group: " + Group.this.getName()
                      + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", field: "
                      + f.getName());
                  auditEntry.saveOrUpdate(true);
                }

                // just make sure again that the owner group isn't a composite,
                // it could have been made into one while adding this membership..
                // note that since the commit isn't done yet, this issue can still happen,
                // but it would hopefully be much less likely..
                if ((Group.getDefaultList().equals(f)) && (Group.this.hasComposite())) {
                  throw new IllegalStateException("Group (name=" + Group.this.getName() + ") turned into a composite while adding an immediate membership.");
                }
              }
              sw.stop();
              return doesntExist;
            } catch (RuntimeException re) {
              GrouperUtil.injectInException(re, errorMessageSuffix);
              throw re;
            }
          }
        });
    
  } // public void addMember(subj, f)

  /**
   * Add an additional group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.addType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to add type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot add system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to add type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to add a system group type.
   */
  public void addType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException {
    addType(type, true);
  }
  
  /**
   * Add an additional group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.addType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to add type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot add system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @param exceptionIfAlreadyHasType 
   * @throws  GroupModifyException if unable to add type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to add a system group type.
   * @return if it was added or not
   */
  public boolean addType(final GroupType type, final boolean exceptionIfAlreadyHasType) 
    throws  GroupModifyException, InsufficientPrivilegeException, SchemaException {

    return (Boolean)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

        StopWatch sw = new StopWatch();
        sw.start();
        if ( Group.this.hasType(type ) ) {
          if (exceptionIfAlreadyHasType) {
            throw new GroupModifyException(E.GROUP_HAS_TYPE + ", " + type);
          }
          return false;
        }
        if ( type.isSystemType() ) {
          throw new SchemaException("cannot edit system group types");
        }
        if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), Group.this, 
            GrouperSession.staticGrouperSession().getSubject() ) ) {
          throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
        }
        Set types = Group.this.getTypesDb();
        types.add( type );

        if (GrouperLoader.isDryRun()) {
          GrouperLoader.dryRunWriteLine("Group add type: " + type.getName());
        } else {
          
          GroupTypeTuple groupTypeTuple = null;
          try {
            groupTypeTuple = GrouperDAOFactory.getFactory().getGroup().addType( Group.this, type);
          } catch (RuntimeException re) {
            types.remove(type);
            throw re;
          }
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_TYPE_ASSIGN, "id", 
                groupTypeTuple.getId(), "groupId", Group.this.getUuid(), 
                "groupName", Group.this.getName(), "typeId", type.getUuid(), "typeName", type.getName());
            auditEntry.setDescription("Assigned group type: " + name + ", typeId: " + type.getUuid() 
                + ", to group: " + Group.this.getName() + ", groupId: " + Group.this.getUuid());
            auditEntry.saveOrUpdate(true);
          }
        }

        
        sw.stop();
        EventLog.info(
            GrouperSession.staticGrouperSession(),
          M.GROUP_ADDTYPE + Quote.single(Group.this.getName()) + " type=" + Quote.single( type.getName() ),
          sw
        );
        return true;
      }
    });
  } 

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canReadField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canReadField(GrouperSession.staticGrouperSession().getSubject(), f);
  } // public boolean canReadField(f)

  /**
   * Check whether the specified {@link Subject} can read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field} or {@link Subject}.
   * @since   1.2.0
   */
  public boolean canReadField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    }
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.INVALID_GROUP_TYPE + f.getGroupType().toString());
    }
    try {
      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, subj, f.getReadPriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  } // public boolean canReadField(subj, f)

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canWriteField(GrouperSession.staticGrouperSession().getSubject(), f);
  } // public boolean canWriteField(f)

  /**
   * Check whether the specified {@link Subject} can write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.internal_canWriteField(subj, f);
  } // public boolean canWriteField(subj, f)

  /**
   * Delete this group from the Groups Registry.
   * <pre class="eg">
   * try {
   *   g.delete();
   * }
   * catch (GroupDeleteException e0) {
   *   // Unable to delete group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this group
   * }
   * </pre>
   * @throws  GroupDeleteException
   * @throws  InsufficientPrivilegeException
   */
  public void delete() throws GroupDeleteException, InsufficientPrivilegeException {
    final String errorMessageSuffix = ", stem name: " + this.name + ", group extension: " + this.extension
      + ", group dExtension: " + this.displayExtension + ", uuid: " + this.uuid + ", ";

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            StopWatch sw = new StopWatch();
            sw.start();
            GrouperSession.validate( GrouperSession.staticGrouperSession() );
            if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), Group.this, 
                GrouperSession.staticGrouperSession().getSubject() ) ) {
              throw new InsufficientPrivilegeException(
                  E.CANNOT_ADMIN + errorMessageSuffix);
            }
            try {
              
              // Revoke all access privs
              Group.this._revokeAllAccessPrivs();
              
              //delete any attributes on this group
              Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByOwnerGroupId(Group.this.getId());
              
              for (AttributeAssign attributeAssign : attributeAssigns) {
                attributeAssign.delete();
              }

              // ... And delete composite mship if it exists
              if (Group.this.hasComposite()) {
                Group.this.deleteCompositeMember();
              }
              
              // ... And delete all memberships - as root
              // Deletes (and saves) now happen within internal_deleteAllFieldType().  See GRP-254.
                Membership.internal_deleteAllFieldType( 
                  GrouperSession.staticGrouperSession().internal_getRootSession(), Group.this, FieldType.LIST );
                
              // Delete privileges where this group is the member.  This is done as root..
              Subject groupSubject = Group.this.toSubject();
              GrouperSession.staticGrouperSession().internal_getRootSession()
                  .getAccessResolver().revokeAllPrivilegesForSubject(groupSubject);
              GrouperSession.staticGrouperSession().internal_getRootSession()
                  .getNamingResolver().revokeAllPrivilegesForSubject(groupSubject);
              GrouperSession.staticGrouperSession().internal_getRootSession()
                .getAttributeDefResolver().revokeAllPrivilegesForSubject(groupSubject);
              
              //deletes.add(this);            // ... And add the group last for good luck    
              String name = Group.this.getName(); // Preserve name for logging
              GrouperDAOFactory.getFactory().getGroup().delete(Group.this);

              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = null;
                if (Group.this.typeOfGroup == TypeOfGroup.entity) {
                  
                  auditEntry = new AuditEntry(AuditTypeBuiltin.ENTITY_DELETE, "id", 
                      Group.this.getUuid(), "name", Group.this.getName(), "parentStemId", Group.this.getParentUuid(), 
                      "displayName", Group.this.getDisplayName(), "description", Group.this.getDescription());
                  auditEntry.setDescription("Deleted entity: " + Group.this.getName());

                } else {
                  auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_DELETE, "id", 
                      Group.this.getUuid(), "name", Group.this.getName(), "parentStemId", Group.this.getParentUuid(), 
                      "displayName", Group.this.getDisplayName(), "description", Group.this.getDescription());
                  auditEntry.setDescription("Deleted group: " + Group.this.getName());
                  
                }
                auditEntry.saveOrUpdate(true);
              }
              
              sw.stop();
              EventLog.info(GrouperSession.staticGrouperSession(), M.GROUP_DEL + Quote.single(name), sw);
          }
          catch (InsufficientPrivilegeException eDAO) {
            throw new GrouperException( eDAO );
            }
            catch (GrouperDAOException eDAO) {
              throw new GroupDeleteException( eDAO.getMessage() + errorMessageSuffix, eDAO );
            }
            catch (RevokePrivilegeException eRP) {
              throw new GroupDeleteException(eRP.getMessage() + errorMessageSuffix, eRP);
            }
            catch (SchemaException eS) {
              throw new GroupDeleteException(eS.getMessage() + errorMessageSuffix, eS);
            }
            return null;
          }
        });
  }

  /**
   * Delete a group attribute.
   * <pre class="eg">
   * try {
   *   g.deleteAttribute(attribute);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this attribute
   * }
   * </pre>
   * @param   attrName  Delete this attribute.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(final String attrName) 
    throws  AttributeNotFoundException,
            GroupModifyException, 
            InsufficientPrivilegeException {
    this.deleteAttribute(attrName, true);
  }
  
  /**
   * Delete a group attribute.
   * <pre class="eg">
   * try {
   *   g.deleteAttribute(attribute);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this attribute
   * }
   * </pre>
   * @param   attrName  Delete this attribute.
   * @param failOnRequiredAttribute true if exception when attribute is required
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(final String attrName, final boolean failOnRequiredAttribute) 
    throws  AttributeNotFoundException,
            GroupModifyException, 
            InsufficientPrivilegeException {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              StopWatch sw = new StopWatch();
              sw.start();
              NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(attrName);
              if (v.isInvalid()) {
                throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attrName);
              }
              Field f = FieldFinder.find(attrName, true);
              if (failOnRequiredAttribute && f.getRequired()) {
                throw new GroupModifyException( E.GROUP_DRA + f.getName() );
              }
              if ( !Group.this.canWriteField(f) ) {
                throw new InsufficientPrivilegeException();
              }
              //
              Group.this.getAttributesMap(false);
              if (Group.this.attributes.containsKey(attrName)) {
                Attribute attribute = Group.this.attributes.get(attrName);
                String val = attribute.getValue(); // for logging
                Group.this.attributes.remove(attrName);
                attribute.assignGroupUuid(Group.this.getUuid(), Group.this);
                GrouperDAOFactory.getFactory().getAttribute().delete( attribute );
                sw.stop();
                EVENT_LOG.groupDelAttr(GrouperSession.staticGrouperSession(), Group.this.getName(), attrName, val, sw);
                
                if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                  
                  AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTRIBUTE_DELETE, "id", 
                      Group.this.getUuid(), "groupId", Group.this.getUuid(), 
                      "groupName", Group.this.getName(), "fieldId", Group.this.getDescription(),
                      "fieldName", attrName,  "value", attribute.getValue());
                  
                  auditEntry.setDescription("Deleted group attribute: " + attrName + " on group: " 
                      + Group.this.getName() + " value: " + attribute.getValue());
                  auditEntry.saveOrUpdate(true);
                }

                
              }
              else {
                throw new AttributeNotFoundException("Attribute not exist: " + attrName);
              }
            }
            catch (GrouperDAOException eDAO) {
              throw new GroupModifyException( eDAO.getMessage(), eDAO );
            }
            catch (InsufficientPrivilegeException eIP) {
              throw eIP;
            }
            catch (SchemaException eS) {
              throw new AttributeNotFoundException(eS.getMessage(), eS);
            }
            return null;
          }
        });
    
  } // public void deleteAttribute(attr)
  
  /**
   * Delete a {@link Composite} membership from this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * try {
   *   g.deleteCompositeMember();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete members
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete composite membership
   * } 
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @since   1.0
   */
  public void deleteCompositeMember()
    throws  InsufficientPrivilegeException,
            MemberDeleteException  {
    
    
    final StringBuilder errorMessageSuffix = new StringBuilder("group name: " + Group.this.name);
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
            try {
    
              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              StopWatch sw  = new StopWatch();
              sw.start();

              String leftGroupName = null;
              Composite composite = null;
              try {
                composite = Group.this.getComposite(true);
              } catch (CompositeNotFoundException cnfe) {
                GrouperUtil.injectInException(cnfe, errorMessageSuffix.toString());
                throw new MemberDeleteException(E.GROUP_DCFC + ", " + cnfe.getMessage(), cnfe);
              }
              
              try { 
                leftGroupName = composite.getLeftGroup().getName();
              } catch (GroupNotFoundException gnfe) {
                leftGroupName = composite.getLeftFactorUuid();
              }
              
              String rightGroupName = null;
              
              try { 
                rightGroupName = composite.getRightGroup().getName();
              } catch (GroupNotFoundException gnfe) {
                rightGroupName = composite.getRightFactorUuid();
              }
              errorMessageSuffix.append(", compositeType: " + composite.getTypeDb()
              + ", left group name: " + leftGroupName
              + ", right group name: " + rightGroupName);
              
              if ( !Group.this.canWriteField( GrouperSession.staticGrouperSession().getSubject(), Group.getDefaultList() ) ) {
                throw new InsufficientPrivilegeException();
              }

              GrouperDAOFactory.getFactory().getComposite().delete(composite);

              EVENT_LOG.groupDelComposite( GrouperSession.staticGrouperSession(), composite, sw );
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_COMPOSITE_DELETE, "id", 
                    composite.getUuid(), "ownerId", Group.this.getUuid(), "ownerName", Group.this.getName(), "leftFactorId", 
                    composite.getLeftFactorUuid(), "leftFactorName", leftGroupName, "rightFactorId", composite.getRightFactorUuid(), 
                    "rightFactorName", rightGroupName, "type", composite.getTypeDb());
                auditEntry.setDescription("Deleted composite: " + Group.this.getName() + " was " 
                    + leftGroupName + " " + composite.getTypeDb() + " " + rightGroupName);
                auditEntry.saveOrUpdate(true);
              }
              sw.stop();
              return null;
            } catch (GrouperDAOException eDAO) {
              GrouperUtil.injectInException(eDAO, errorMessageSuffix.toString());
              throw new MemberDeleteException(eDAO.getMessage(), eDAO);
            } catch (RuntimeException re) {
              GrouperUtil.injectInException(re, errorMessageSuffix.toString());
              throw re;
            }
          }
        });
  }
  
  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(member);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}
   * @param exceptionIfAlreadyDeleted throw exception if already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @return false if it was already deleted, true if it wasnt already deleted
   */
  public boolean deleteMember(Member member, boolean exceptionIfAlreadyDeleted)
    throws  InsufficientPrivilegeException, MemberDeleteException {
    try {
      return this.deleteMember(member, getDefaultList(), exceptionIfAlreadyDeleted);
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }

  }
  
  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(member);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteMember(Member member)
    throws  InsufficientPrivilegeException, MemberDeleteException {
    deleteMember(member, true);
  }

  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}.
   * @param   f     Delete subject from this {@link Field}.
   * @param exceptionIfAlreadyDeleted 
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public boolean  deleteMember(Member member, Field f, boolean exceptionIfAlreadyDeleted) 
      throws  InsufficientPrivilegeException, MemberDeleteException, SchemaException {
    
    Subject lazySubject = new LazySubject(member);
    
    return deleteMember(lazySubject, f, exceptionIfAlreadyDeleted);
    
  }

  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Member member, Field f) 
      throws  InsufficientPrivilegeException, MemberDeleteException, SchemaException {
    deleteMember(member, f, true);
  }

  
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteMember(Subject subj)
    throws  InsufficientPrivilegeException,
            MemberDeleteException {
    deleteMember(subj, true);
  }
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @param exceptionIfAlreadyDeleted 
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public boolean deleteMember(Subject subj, boolean exceptionIfAlreadyDeleted)
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      return this.deleteMember(subj, getDefaultList(), exceptionIfAlreadyDeleted);
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }
  }

  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj, Field f) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException {
    deleteMember(subj, f, true);
  }
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @param exceptionIfAlreadyDeleted true if an exception should be thrown
   * if the member is already deleted
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public boolean  deleteMember(final Subject subj, final Field f, final boolean exceptionIfAlreadyDeleted) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException { 
    final StopWatch sw  = new StopWatch();
    sw.start();
    
    final String errorMessageSuffix = ", group name: " + this.name 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", field: " + (f == null ? null : f.getName());

    return (Boolean)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {

        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          boolean notAlreadyDeleted = true;
          try {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            if ( !FieldType.LIST.equals( f.getType() ) ) {
              throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
            }
            if ( !Group.this.canWriteField(f) ) {
              GrouperValidator v = CanOptoutValidator.validate(Group.this, subj, f);
              if (v.isInvalid()) {
                throw new InsufficientPrivilegeException(errorMessageSuffix);
              }
            }
            if ( (f.equals( Group.getDefaultList() ) ) && ( Group.this.hasComposite() ) ) {
              throw new MemberDeleteException(E.GROUP_DMFC);
            }

            Membership membership = Membership.internal_delImmediateMembership( 
                GrouperSession.staticGrouperSession(), Group.this, subj, f );

            sw.stop();
            if (notAlreadyDeleted) {
              
              //this might be a wheel or a member of wheel...  maybe there is a more efficient way to do this...
              PrivilegeHelper.flushCache();

              EVENT_LOG.groupDelMember(GrouperSession.staticGrouperSession(), 
                  Group.this.getName(), subj, f, sw);

              //if we are in the default list, then fire a rule
              if (StringUtils.equals(f.getUuid(), Group.getDefaultList().getUuid())) {
                RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(membership, Group.this, subj);
                //fire rules directly connected to this membership remove
                RuleEngine.fireRule(RuleCheckType.membershipRemove, rulesMembershipBean);
                //fire rules related to remove in stem
                RuleEngine.fireRule(RuleCheckType.membershipRemoveInFolder, rulesMembershipBean);

              }
              
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, "id", 
                    membership == null ? null : membership.getUuid(), "fieldId", f.getUuid(),
                        "fieldName", f.getName(), "memberId",  membership.getMemberUuid(),
                        "membershipType", membership.getType(), 
                        "groupId", Group.this.getUuid(), "groupName", Group.this.getName());
                        
                auditEntry.setDescription("Deleted membership: group: " + Group.this.getName()
                    + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", field: "
                    + f.getName());
                auditEntry.saveOrUpdate(true);
              }

              
            }
          } catch (GrouperDAOException eDAO) {
            throw new MemberDeleteException( eDAO.getMessage() + ", " + errorMessageSuffix, eDAO );
          } catch (MemberDeleteAlreadyDeletedException mdade) {
            if (exceptionIfAlreadyDeleted) {
              GrouperUtil.injectInException(mdade, errorMessageSuffix);
              throw mdade;
            }
            notAlreadyDeleted = false;
          }
          return notAlreadyDeleted;
        }
        });
  } // public void deleteMember(subj, f)

  /**
   * Delete a group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.deleteType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to delete type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot delete system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to delete type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to delete a system group type.
   */
  public void deleteType(final GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException {
    
    final String typeString = type == null ? null : type.getName();
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        try {

          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

          StopWatch sw = new StopWatch();
          sw.start();
          if ( !Group.this.hasType(type) ) {
            throw new GroupModifyException("does not have type: " + typeString);
          }
          if ( type.isSystemType() ) {
            throw new SchemaException("cannot edit system group types: " + typeString);
          }
          if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), Group.this, GrouperSession.staticGrouperSession().getSubject() ) ) {
            throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
          }
  
          GroupTypeTuple groupTypeTuple = GrouperDAOFactory.getFactory().getGroup().deleteType( Group.this, type );
          
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_TYPE_UNASSIGN, "id", 
                groupTypeTuple.getId(), "groupId", Group.this.getUuid(), 
                "groupName", Group.this.getName(), "typeId", type.getUuid(), "typeName", type.getName());
            auditEntry.setDescription("Unasssigned group type: " + name + ", typeId: " + type.getUuid() 
                + ", to group: " + Group.this.getName() + ", groupId: " + Group.this.getUuid());
            auditEntry.saveOrUpdate(true);
          }
          
          sw.stop();
          EventLog.info(
            GrouperSession.staticGrouperSession(),
            M.GROUP_DELTYPE + Quote.single(Group.this.getName()) + " type=" + Quote.single( type.getName() ),
            sw
          );
          return null;
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUP_TYPEDEL + type + ": "; 
          msg += eDAO.getMessage();
          LOG.error(msg);
          throw new GroupModifyException(msg, eDAO);
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, "Problem with type: " + typeString);
          throw re;
        }
      }
    });
  } 

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   * @throws  GrouperException
   */
  public Set<Subject> getAdmins() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.ADMIN);
  }

  /**
   * get the value of an attribute, if not there return null
   * @param attributeName
   * @return the attribute value
   * @deprecated use getAttributeValue()
   */
  @Deprecated
  public String getAttributeOrNull(String attributeName) {
    try {
      return this.getAttribute(attributeName);
    } catch (AttributeNotFoundException anfe) {
      return null;
    }
  }
  
  /**
   * if attribute or field name is a field name, call that getter with reflection.
   * If not, then call getAttributeValue()
   * @param attributeOrFieldName
   * @param checkSecurity
   * @param exceptionIfAttributeNotFound
   * @return the value
   */
  public String getAttributeOrFieldValue(String attributeOrFieldName, boolean checkSecurity, boolean exceptionIfAttributeNotFound) {
    
    if (INTERNAL_FIELD_ATTRIBUTES.contains(attributeOrFieldName)) {
      return (String)GrouperUtil.fieldValue(this, attributeOrFieldName);
    }
    return this.getAttributeValue(attributeOrFieldName, checkSecurity, exceptionIfAttributeNotFound);
    
  }
  
  /**
   * get the value of an attribute, if not there return the empty string.
   * or exception if expected to be there.
   * 
   * @param attributeName
   * @param exceptionIfNotFound
   * @param checkSecurity
   * @return the attribute value or null if not there and not expecting exception.
   */
  public String getAttributeValue(String attributeName, 
      boolean checkSecurity, boolean exceptionIfNotFound) {

    //init
    this.getAttributesMap(false);
    
    Attribute attribute = this.attributes.get(attributeName);

    if (attribute == null) {

      // Group does not have attribute.  If attribute is not valid for Group,
      // throw AttributeNotFoundException.  Otherwise, return an empty string.
      
      Field f = FieldFinder.find(attributeName, false); 
      if (f == null) {
        throw new AttributeNotFoundException("Cant find attribute: " + attributeName);
      }
      if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
        throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() + ", " + attributeName );
      }
      GrouperValidator v = GetGroupAttributeValidator.validate(this, f);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException( v.getErrorMessage() );
      }
      
      if (exceptionIfNotFound) {
        throw new AttributeNotFoundException("Cant find attribute value: " + attributeName);
      }
      return "";
    }
    
    if (checkSecurity) {
      if (! this._canReadField( GrouperSession.staticGrouperSession(), attributeName ) ) {
        if (exceptionIfNotFound) {
          throw new AttributeNotFoundException("Cant read attribute: " + attributeName);
        }
        return "";
      }
    }
    return StringUtils.defaultString(attribute.getValue());
  }
  
  /**
   * Get attribute value.
   * <pre class="eg">
   * try {
   *   String value = g.getAttribute(attribute);
   * }
   * catch (AttributeNotFoundException e) {
   *   // Group doesn't have attribute
   * }
   * </pre>
   * @param   attr  Get value of this attribute.
   * @return  Attribute value.  or throw AttributeNotFoundException if not there.
   * The value will be the emprty string if it is null
   * @throws  AttributeNotFoundException
   * @Deprecated use getAttributeValue
   */
  @Deprecated
  public String getAttribute(String attr) 
    throws  AttributeNotFoundException {

    //if (_internal_fieldAttributes.contains(attr)) {
    //  return _internal_getAttributeBuiltIn(attr);
    //}
    
    // check to see if attribute exists in Map returned by getAttributes()
    Map attrs = this.getAttributesDb();
    if (attrs.containsKey(attr)) {
      String val = (String) attrs.get(attr);
      if (val == null) {
        return "";
      } 
      return val;
    }

    // Group does not have attribute.  If attribute is not valid for Group,
    // throw AttributeNotFoundException.  Otherwise, return an empty string.
    
    Field f = FieldFinder.find(attr, false); 
    if (f == null) {
      throw new AttributeNotFoundException("Cant find attribute: " + attr);
    }
    if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
      throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    GrouperValidator v = GetGroupAttributeValidator.validate(this, f);
    if (v.isInvalid()) {
      throw new AttributeNotFoundException( v.getErrorMessage() );
    }
    return "";
  }

  /**
   * Get {@link Composite} {@link Member}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * Set members = g.getCompositeMembers();
   * </pre>
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @since   1.0
   */
  public Set<Member> getCompositeMembers(QueryOptions queryOptions) {
    return getCompositeMembers(Group.getDefaultList(), null, queryOptions);
  } // public Set getCompositeMembers()

  /**
   * @param attr
   * @return the value
   */
  @SuppressWarnings("unused")
  private String _internal_getAttributeBuiltIn(String attr) {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("groups.allow.attribute.access.1.4", false)) {
      
      if (StringUtils.equals(FIELD_NAME, attr)) {
        return this.getName();
      }
      if (StringUtils.equals(FIELD_EXTENSION, attr)) {
        return this.getExtension();
      }
      if (StringUtils.equals(FIELD_DISPLAY_NAME, attr)) {
        return this.getDisplayName();
      }
      if (StringUtils.equals(FIELD_DISPLAY_EXTENSION, attr)) {
        return this.getDisplayExtension();
      }
      if (StringUtils.equals(FIELD_DESCRIPTION, attr)) {
        return this.getDescription();
      }
      throw new RuntimeException("Not expecting attribute: " + attr);
      
    }
    throw new RuntimeException("Cannot access built in attribute: " + attr + " from getAttributes anymore, " +
    		"use getter directly (e.g. getName(), getDisplayName()).  Or you can enable this (deprecated) with " +
    		"grouper.properties setting groups.allow.attribute.access.1.4=true");
  }

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   * @deprecated use getAttributesMap
   */
  @Deprecated
  public Map<String, String> getAttributes() {
    Map       filtered  = new HashMap();
    Map.Entry kv;
    Iterator  it        = this.getAttributesDb().entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      if ( this._canReadField( GrouperSession.staticGrouperSession(), (String) kv.getKey() ) ) {
        filtered.put( (String) kv.getKey(), (String) kv.getValue() );
      }
    }
    //Subject currentSubject = GrouperSession.staticGrouperSession().getSubject();
    
    //if (GrouperConfig.retrieveConfig().propertyValueBoolean("groups.allow.attribute.access.1.4", false)) {
    //
    //  boolean canReadGroup = this.hasRead(currentSubject);
    //  boolean canViewGroup = this.hasView(currentSubject);
    //  
    //  if (canViewGroup) {
    //    {
    //      String theName = this.getName();
    //      if (!StringUtils.isBlank(theName)) {
    //        filtered.put("name", theName);
    //      }
    //    }
    //    {
    //      String theDisplayName = this.getDisplayName();
    //      if (!StringUtils.isBlank(theDisplayName)) {
    //        filtered.put("displayName", theDisplayName);
    //      }
    //    }
    //    {
    //      String theExtension = this.getExtension();
    //      if (!StringUtils.isBlank(theExtension)) {
    //        filtered.put("extension", theExtension);
    //     }
    //    }
    //    {
    //      String theDisplayExtension = this.getDisplayExtension();
    //      if (!StringUtils.isBlank(theDisplayExtension)) {
    //        filtered.put("displayExtension", theDisplayExtension);
    //      }
    //    }
    //  }
    //  if (canReadGroup) {
    //    {
    //      String theDescription = this.getDescription();
    //      if (!StringUtils.isBlank(theDescription)) {
    //        filtered.put("description", theDescription);
    //      }
    //    }
    //  }
    //}
    return filtered;
  } // public Map getAttributes()

  /**
   * Get {@link Composite} {@link Member}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * Set members = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @since   1.0
   */
  public Set<Member> getCompositeMembers() {
    return this.getCompositeMembers(null);
  } // public Set getCompositeMembers()

  /**
   * Get {@link Composite} {@link Membership}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set mships = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @since   1.0
   */
  public Set<Membership> getCompositeMemberships() {
    return MembershipFinder.internal_findAllByGroupOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, Group.getDefaultList(), MembershipType.COMPOSITE.getTypeString()
    );
  } // public Set getCompositeMemberships()

  /**
   * Get subject that created this group.
   * <pre class="eg">
   * // Get creator of this group.
   * try {
   *   Subject creator = g.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws SubjectNotFoundException
  {
    if ( this.subjectCache.containsKey(KEY_CREATOR) ) {
      return this.subjectCache.get(KEY_CREATOR);
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      Member _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getCreatorUuid(), true );
      this.subjectCache.put( 
        KEY_CREATOR, SubjectFinder.findByIdAndSource( _m.getSubjectId(), _m.getSubjectSourceId() , true) 
      );
      return this.subjectCache.get(KEY_CREATOR);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }
  } // public Subject getCreateSubject() 
  
  /**
   * Get creation time for this group.
   * <pre class="eg">
   * // Get create time.
   * Date created = g.getCreateTime();
   * </pre>
   * @return  {@link Date} that this group was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreateTimeLong());
  } // public Date getCreateTime()

  /**
   * Get group description.
   * <pre class="eg">
   * String description = g.getDescription();
   * </pre>
   * @return  Group's <i>description</i> or an empty string if no value set.
   */
  public String getDescription() {
    return GrouperUtil.defaultString(this.description);
  }

  /**
   * Get group description for hibernate.
   * @return  Group's <i>description</i> or an empty string if no value set.
   */
  public String getDescriptionDb() {
    return this.description;
  } 

  /**
   * list of internal field attributes, access with method so it can lazy load
   */
  public static final Set<String> INTERNAL_FIELD_ATTRIBUTES = Collections.unmodifiableSet(
      GrouperUtil.toSet(FIELD_DESCRIPTION, FIELD_NAME, FIELD_EXTENSION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME));
  
  /**
   * see if field attribute (name, description, extension, displayName, displayExtension)
   * @param attributeName
   * @return true if so
   */
  public static boolean _internal_fieldAttribute(String attributeName) {
    return INTERNAL_FIELD_ATTRIBUTES.contains(attributeName);
  }
  
  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   * @throws  GrouperException
   */
  public String getDisplayExtension() {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = this.displayExtension;
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.fatal(E.GROUP_NODE);
      throw new GrouperException(E.GROUP_NODE);
    }
    return val;
  } // public String getDisplayExtension()

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   * @throws  GrouperException
   */
  public String getDisplayName() 
    throws  GrouperException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = this.displayName;
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.fatal(E.GROUP_NODN);
      throw new GrouperException(E.GROUP_NODN);
    }
    return val;
  } // public String getDisplayName()

  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperException
   */
  public Set<Member> getEffectiveMembers() 
    throws  GrouperException
  {
    try {
      return this.getEffectiveMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getEffectiveMembership()


  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getEffectiveMembers(Field f) 
    throws  SchemaException
  {
    return this.getEffectiveMembers(f, null);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getEffectiveMembers(Field f, QueryOptions queryOptions) 
    throws  SchemaException
  {
    return getEffectiveMembers(f, null, queryOptions);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param sources sources to filter by, or null for all
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getEffectiveMembers(Field f, Set<Source> sources, QueryOptions queryOptions) 
    throws  SchemaException
  {
    return MemberFinder.internal_findMembersByType(
        GrouperSession.staticGrouperSession(), this, f, MembershipType.EFFECTIVE.getTypeString(), sources, queryOptions,
        null, null, null);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective memberships of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperException
   */
  public Set<Membership> getEffectiveMemberships() 
    throws  GrouperException
  {
    try {
      return this.getEffectiveMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getEffectiveMembership()

  /**
   * Get effective memberships of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set memberships = g.getEffectiveMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findAllByGroupOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.EFFECTIVE.getTypeString()
    );
  } // public Set getEffectiveMemberships(f)

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   * @throws  GrouperException
   */
  public String getExtension() {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = this.extension;
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.error( E.GROUP_NOE);
      throw new GrouperException(E.GROUP_NOE);
    }
    return val;
  } // public String getExtension()
 
  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperException
   */
  public Set<Member> getImmediateMembers() 
    throws  GrouperException
  {
    try {
      return this.getImmediateMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getImmediateMembers()

  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getImmediateMembers(Field f) 
    throws  SchemaException {
    return getImmediateMembers(f, null);
  }

  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getImmediateMembers(Field f, QueryOptions queryOptions) 
    throws  SchemaException {
    return getImmediateMembers(f, null, queryOptions);
  }

    
    
  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param sources to search in or null if all
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getImmediateMembers(Field f, Set<Source> sources, QueryOptions queryOptions) 
    throws  SchemaException {
    return MemberFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.IMMEDIATE.getTypeString(), sources, queryOptions,
      null, null, null
    );
  }
  
  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param sources to search in or null if all
   * @param queryOptions 
   * @param memberSortStringEnum How to sort results or null for no sorting unless specified by queryOptions
   * @param memberSearchStringEnum Specify search string if searching for members in the group
   * @param memberSearchStringValue Search string value.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getImmediateMembers(Field f, Set<Source> sources, QueryOptions queryOptions, 
      SortStringEnum memberSortStringEnum, SearchStringEnum memberSearchStringEnum, 
      String memberSearchStringValue) 
    throws  SchemaException {
    return MemberFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.IMMEDIATE.getTypeString(), sources, queryOptions,
      memberSortStringEnum, memberSearchStringEnum, memberSearchStringValue
    );
  }

  /**
   * Get immediate memberships of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperException
   */
  public Set<Membership> getImmediateMemberships() 
    throws  GrouperException
  {
    try {
      return this.getImmediateMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    return MembershipFinder.internal_findAllByGroupOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.IMMEDIATE.getTypeString()
    );
  } // public Set getImmediateMemberships(f)

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperException
   */
  public Set<Member> getMembers() 
    throws  GrouperException
  {
    try {
      return this.getMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getMembers()

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getMembers(Field f) 
    throws  SchemaException {
    return this.getMembers(f, null);
  } 

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param queryOptions paging, sorting, count, etc
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getMembers(Field f, QueryOptions queryOptions)
    throws  SchemaException {
    return getMembers(f, null, queryOptions);
  } 

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.to get members from, or null for all
   * @param sources to get members from, or null for all
   * @param queryOptions paging, sorting, count, etc
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getMembers(Field f, Set<Source> sources, QueryOptions queryOptions)
    throws  SchemaException {
    return MembershipFinder.findMembers(this, f, sources, queryOptions);
  } 

  /**
   * Get memberships of this group.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperException
   */
  public Set<Membership> getMemberships() 
    throws  GrouperException
  {
    try {
      return this.getMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getMemberships()

  /**
   * Get memberships of this group.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getMemberships(Field f) 
    throws  SchemaException
  {
    return new LinkedHashSet<Membership>( 
      PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory()
          .getMembership().findAllByGroupOwnerAndField(this.getUuid(), f, true)
      )
    );
  } // public Set getMemberships(f)

  /**
   * Get memberships of this group, for a certain collection of members
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getMemberships(Field f, Collection<Member> members) 
    throws  SchemaException {
    return PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMembers(this.getUuid(), f, members, true)
      );
  }

  /**
   * Get memberships of this group, for a certain collection of members, must be enabled
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getImmediateMemberships(Field f, Collection<Member> members) 
    throws  SchemaException {
    return getImmediateMemberships(f, members, true);
  }

  /**
   * Get memberships of this group, for a certain collection of members
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @param enabledOnly
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getImmediateMemberships(Field f, Collection<Member> members, boolean enabledOnly) 
    throws  SchemaException {
    return PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMembersAndType(this.getUuid(), f, members, 
              MembershipType.IMMEDIATE.getTypeString(), enabledOnly)
      );
  }

  /**
   * Get membership of this group, for a certain member
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Membership membership = g.getMembership(f, m, true);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param subject
   * @param enabledOnly 
   * @param exceptionIfNotFound true if MembershipNotFoundException should be thrown if not found, otherwise null
   * @return A set of {@link Membership} objects.
   * @throws SchemaException
   * @throws MembershipNotFoundException if none found and exceptionIfNotFound
   */
  public Membership getImmediateMembership(Field f, Subject subject, boolean enabledOnly, boolean exceptionIfNotFound) 
      throws  SchemaException, MembershipNotFoundException {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return getImmediateMembership(f, member, enabledOnly, exceptionIfNotFound);
  }

  /**
   * Get membership of this group, for a certain member
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Membership membership = g.getMembership(f, m, true);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param member
   * @param enabledOnly 
   * @param exceptionIfNotFound true if MembershipNotFoundException should be thrown if not found, otherwise null
   * @return A set of {@link Membership} objects.
   * @throws SchemaException
   * @throws MembershipNotFoundException if none found and exceptionIfNotFound
   */
  public Membership getImmediateMembership(Field f, Member member, boolean enabledOnly, boolean exceptionIfNotFound) 
      throws  SchemaException, MembershipNotFoundException {
    
    if (member == null) {
      throw new RuntimeException("You need to pass a member here");
    }
    
    Set<Membership> memberships = this.getImmediateMemberships(f, GrouperUtil.toSet(member), enabledOnly);
    if (memberships.size() == 0) {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException("Cant find memberships for group: " + this + ", and member: " + member
            + ", and field: " + f);
      }
      return null;
    }
    if (memberships.size() > 1) {
      throw new RuntimeException("There are more than one memberships: " + memberships.size()
          + " for group: " + this + ", and member: " + member
            + ", and field: " + f);
    }
    return memberships.iterator().next();
  }

  /**
   * Get memberships of this group, for a certain collection of members
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getEffectiveMemberships(Field f, Collection<Member> members) 
    throws  SchemaException {
    return PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMembersAndType(this.getUuid(), f, members, "effective", true)
      );
  }

  /**
   * Get memberships of this group, for a certain collection of members
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param members 
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getCompositeMemberships(Collection<Member> members) 
    throws  SchemaException {
    return PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndCompositeAndMembers(this.getUuid(), members, true)
      );
  }

  /**
   * Get subject that last modified this group.
   * <pre class="eg">
   * try {
   *   Subject modifier = g.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws SubjectNotFoundException
  {
    if ( this.subjectCache.containsKey(KEY_MODIFIER) ) {
      return this.subjectCache.get(KEY_MODIFIER);
    }
    if ( this.getModifierUuid() == null ) {
      throw new SubjectNotFoundException("group has not been modified");
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      Member _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getModifierUuid(), true );
      this.subjectCache.put(
        KEY_MODIFIER, SubjectFinder.findByIdAndSource( _m.getSubjectId(), _m.getSubjectSourceId(), true )
      );
      return this.subjectCache.get(KEY_MODIFIER);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }  
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    return new Date( this.getModifyTimeLong() );
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   * @throws  GrouperException
   */
  public String getName() 
    throws  GrouperException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = this.name;
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.error( E.GROUP_NON);
      throw new GrouperException(E.GROUP_NON);
    }
    return val;
  } // public String getName()

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   * @throws  GrouperException
   */
  public Set<Subject> getOptins() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTIN);
  } 

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   * @throws  GrouperException
   */
  public Set<Subject> getOptouts() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTOUT);
  } 

  /**
   * get the name of the parent stem
   * @return the name of the parent stem
   */
  public String getParentStemName() {
    return GrouperUtil.parentStemNameFromName(this.getName(), false);
  } 

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   * @throws IllegalStateException 
   */
  public Stem getParentStem() 
    throws  IllegalStateException
  {
    String uuid = this.getParentUuid();
    if (uuid == null) {
      throw new IllegalStateException("group has no parent stem");
    }
    try {
      Stem parent = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid, true) ;
      return parent;
    }
    catch (StemNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: group has no parent stem: " + eShouldNeverHappen.getMessage(), 
        eShouldNeverHappen 
      );
    }
  } // public Stem getParentStem()

  /**
   * Get privileges that the specified subject has on this group.
   * <pre class="eg">
   * Set privs = g.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link AccessPrivilege} objects.
   */
  public Set<AccessPrivilege> getPrivs(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().getPrivileges(this, subj);
  } 


  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   * @throws  GrouperException
   */
  public Set<Subject> getReaders() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.READ);
  } 

  /**
   * Get removable group types for this group.
   * <pre class="eg">
   * Set types = g.getRemovableTypes();
   * </pre>
   * @return  Set of removable group types.
   * @since   1.0
   */
  public Set<GroupType> getRemovableTypes() {
    Set<GroupType> types = new LinkedHashSet<GroupType>();
    // Must have ADMIN to remove types.
    if (PrivilegeHelper.canAdmin(GrouperSession.staticGrouperSession(), this, GrouperSession.staticGrouperSession().getSubject())) {
      GroupType t;
      Iterator  iter  = this.getTypes().iterator();
      while (iter.hasNext()) {
        t = (GroupType) iter.next();
        if ( t.getIsAssignable() ) {
          types.add(t);
        }
      }
    }
    return types;
  } // public Set getRemovableTypes()

  /**
   * Get group types for this group (only non internal ones).
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set<GroupType> getTypes() {

    Set<GroupType>       newTypes = new LinkedHashSet<GroupType>();
    Iterator  it    = this.getTypesDb().iterator();
    while (it.hasNext()) {
      GroupType groupType = (GroupType) it.next();
      if ( !groupType.getIsInternal() ) {
        newTypes.add(groupType);
      }
    }
    return newTypes;
  } // public Set getTypes()

  /**
   * get types in db
   * @return types
   */
  public Set<GroupType> getTypesDb() {
    if (this.types == null) {
      this.types = GrouperDAOFactory.getFactory().getGroup()._findAllTypesByGroup( this.getUuid() );
    }
    return this.types;
  }
  
  /**
   * Get subjects with the UPDATE privilege on this group.
   * <pre class="eg">
   * Set updaters = g.getUpdaters();
   * </pre>
   * @return  Set of subjects with UPDATE
   * @throws  GrouperException
   */
  public Set<Subject> getUpdaters() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.UPDATE);
  } 

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   * @throws  GrouperException
   */
  public Set<Subject> getViewers() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.VIEW);
  } 

  /**
   * Grant privilege to a subject on this group.  This
   * will throw an exception if the privilege already exists
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    
    grantPriv(subj, priv, true);
    
  }
  
  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean grantPriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyMember)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    return internal_grantPriv(subj, priv, exceptionIfAlreadyMember, null);
  }
  
  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @param uuid
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean internal_grantPriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyMember, final String uuid)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    final StopWatch sw = new StopWatch();
    sw.start();

    final String errorMessageSuffix = ", group name: " + this.name 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());

    return (Boolean)HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {

        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          boolean assignedPrivilege = false;
          try {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperSession.staticGrouperSession().getAccessResolver().grantPrivilege(Group.this, subj, priv, uuid);
            assignedPrivilege = true;

            RulesPrivilegeBean rulesPrivilegeBean = new RulesPrivilegeBean(Group.this, subj, priv);
            
            //fire rules related to subject assign in folder
            RuleEngine.fireRule(RuleCheckType.subjectAssignInStem, rulesPrivilegeBean);

            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              
              Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false);
              
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_GROUP_ADD, "privilegeName", 
                  priv.getName(),  "memberId",  member.getUuid(),
                      "privilegeType", "access", 
                      "groupId", Group.this.getUuid(), "groupName", Group.this.getName());
                      
              auditEntry.setDescription("Added privilege: group: " + Group.this.getName()
                  + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", privilege: "
                  + priv.getName());
              auditEntry.saveOrUpdate(true);
            }

          } catch (UnableToPerformAlreadyExistsException eUTP) {
            if (exceptionIfAlreadyMember) {
              throw new GrantPrivilegeAlreadyExistsException(eUTP.getMessage() + errorMessageSuffix, eUTP);
            }
          } catch (UnableToPerformException eUTP) {
            throw new GrantPrivilegeException( eUTP.getMessage() + errorMessageSuffix, eUTP );
          }
          sw.stop();
          if (assignedPrivilege) {
            EVENT_LOG.groupGrantPriv(GrouperSession.staticGrouperSession(), Group.this.getName(), subj, priv, sw);
          }
          return assignedPrivilege;
        }
      });
  } 
  
  /**
   * Check whether the subject has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(subj)) {
   *   // Has ADMIN
   * }
   * else {
   *   // Does not have ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ADMIN.
   */
  public boolean hasAdmin(Subject subj) {
    AccessResolver accessResolver = GrouperSession.staticGrouperSession().getAccessResolver();
    return accessResolver.hasPrivilege(this, subj, AccessPrivilege.ADMIN);
  } 

  /**
   * Does this {@link Group} have a {@link Composite} membership.
   * <pre class="eg">
   * if (g.hasComposite()) {
   *   // this group has a composite membership
   * }
   * </pre>
   * @return  Boolean true if group has a composite membership.
   */
  public boolean hasComposite() {
    return null !=  GrouperDAOFactory.getFactory().getComposite().findAsOwner( this , false);
  } // public boolean hasComposite()

  /**
   * Check whether the subject is an effective member of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperException
   */
  public boolean hasEffectiveMember(Subject subj) 
    throws  GrouperException
  {
    try {
      return this.hasEffectiveMember(subj, getDefaultList());
    } 
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public boolean hasEffectiveMember(Subject subj)

  /**
   * Check whether the subject is an effective member of this group.  
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj, f)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group. 
   * @throws  SchemaException
   */
  public boolean hasEffectiveMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, true);
    rv = m.isEffectiveMember(this, f);
    return rv;
  } // public boolean hasEffectiveMember(subj, f)

  /**
   * Check whether the subject is an immediate member of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperException
   */
  public boolean hasImmediateMember(Subject subj) 
    throws  GrouperException
  {
    try {
      return this.hasImmediateMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public boolean hasImmediateMember(subj)

  /**
   * Check whether the subject is an immediate member of this group.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj, f)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasImmediateMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, true);
    rv = m.isImmediateMember(this, f);
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  /**
   * Check whether the subject is a member of this group.
   * 
   * All immediate subjects, and effective members are members.  
   * No duplicates will be returned (e.g. if immediate and effective).
   * 
   * <pre class="eg">
   * if (g.hasMember(subj)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperException
   */
  public boolean hasMember(Subject subj) 
    throws  GrouperException
  {
    try {
      return this.hasMember(subj, getDefaultList());
    }
    catch (SchemaException eShouldNeverHappen) {
      // If we don't have "members" we have serious issues
      String msg = "this should never happen: default group list not found: " + eShouldNeverHappen.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eShouldNeverHappen);
    }
  } // public boolean hasMember(subj)

  /**
   * Check whether the subject is a member of this list on this group.
   * 
   * All immediate subjects, and effective members are members.  
   * No duplicates will be returned (e.g. if immediate and effective).
   * 
   * <pre class="eg">
   * if (g.hasMember(subj, f)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Is subject a member of this list {@link Field}.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, true);
    rv = m.isMember(this, f);
    return rv;
  } // public boolean hasMember(subj, f)

  /**
   * Check whether the subject has OPTIN on this group.
   * <pre class="eg">
   * if (g.hasOptin(subj)) {
   *   // Has OPTIN
   * }
   * else {
   *   // Does not have OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTIN.
   */
  public boolean hasOptin(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTIN);
  }

  /**
   * Check whether the subject has OPTOUT on this group.
   * <pre class="eg">
   * if (g.hasOptout(subj)) {
   *   // has OPTOUT
   * }
   * else {
   *   // Does not have OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTOUT.
   */
  public boolean hasOptout(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTOUT);
  } 

  /**
   * Check whether the subject has READ on this group.
   * <pre class="eg">
   * if (g.hasRead(subj)) {
   *   // Has READ
   * }
   * else {
   *   // Does not have READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has READ.
   */
  public boolean hasRead(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.READ);
  } 

  /**
   * Check whether group has the specified type.
   * <pre class="eg">
   * GroupType custom = GroupTypeFinder.find("custom type");
   * if (g.hasType(custom)) {
   *   // Group has type
   * }
   * </pre>
   * @param   type  The {@link GroupType} to check.
   * @return if has type
   */
  public boolean hasType(GroupType type) {
    return this.getTypesDb().contains( type );
  } // public boolean hasType(type)

  /**
   * Check whether the subject has UPDATE on this group.
   * <pre class="eg">
   * if (g.hasUpdate(subj)) {
   *   // Has UPDATE
   * }
   * else {
   *   // Does not have UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has UPDATE.
   */
  public boolean hasUpdate(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.UPDATE);
  } 

  /**
   * Check whether the subject has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(subj)) {
   *   // Has VIEW
   * }
   * else {
   *   // Does not have VIEW
   * }
   * </pre>
   * @param   subj  Check this member.
   * @return  Boolean true if subject has VIEW.
   */
  public boolean hasView(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.VIEW);
  } 

  /**
   * Is this {@link Group} a factor in a {@link Composite} membership.
   * <pre class="eg">
   * if (g.isComposite()) {
   *   // this group is a factor in one-or-more composite memberships.
   * }
   * </pre>
   * @return  Boolean true if group is a factor in a composite membership.
   */
  public boolean isComposite() {
    if ( GrouperDAOFactory.getFactory().getComposite().findAsFactor( this ).size() > 0 ) {
      return true;
    }
    return false;
  } // public boolean isComposite()

  /**
   * Revoke all privileges of the specified type on this group.
   * <pre class="eg">
   * try {
   *   g.revokePriv(AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to modify group
   * }
   * </pre>
   * @param   priv  Revoke all instances of this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !Privilege.isAccess(priv) ) {
      throw new SchemaException("attempt to use not access privilege: " + priv);
    }
    try {
      GrouperSession.staticGrouperSession().getAccessResolver().revokePrivilege(this, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new RevokePrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EVENT_LOG.groupRevokePriv(GrouperSession.staticGrouperSession(), this.getName(), priv, sw);
  } 

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Subject subj, Privilege priv) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    revokePriv(subj, priv, true);
  }
  
  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @param exceptionIfAlreadyRevoked if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @return false if it was already revoked, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public boolean revokePriv(final Subject subj, final Privilege priv, 
      final boolean exceptionIfAlreadyRevoked) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, SchemaException {

    final StopWatch sw = new StopWatch();
    sw.start();

    final String errorMessageSuffix = ", group name: " + this.name 
    + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());

    return (Boolean)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
    
        hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

        boolean wasntAlreadyRevoked = true;
        try {
          if ( !Privilege.isAccess(priv) ) {
            throw new SchemaException("attempt to use not access privilege: " + priv);
          }
          GrouperSession.staticGrouperSession().getAccessResolver().revokePrivilege(Group.this, subj, priv);

          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            
            Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false);
            
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_GROUP_DELETE, "privilegeName", 
                priv.getName(),  "memberId",  member.getUuid(),
                    "privilegeType", "access", 
                    "groupId", Group.this.getUuid(), "groupName", Group.this.getName());
                    
            auditEntry.setDescription("Deleted privilege: group: " + Group.this.getName()
                + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", privilege: "
                + priv.getName());
            auditEntry.saveOrUpdate(true);
          }

        } catch (UnableToPerformAlreadyExistsException eUTP) {
          if (exceptionIfAlreadyRevoked) {
            throw new RevokePrivilegeAlreadyRevokedException( eUTP.getMessage() + errorMessageSuffix, eUTP );
          }
          wasntAlreadyRevoked = false;
        } catch (UnableToPerformException eUTP) {
          throw new RevokePrivilegeException( eUTP.getMessage() + errorMessageSuffix, eUTP );
        }
        sw.stop();
        if (wasntAlreadyRevoked) {
          EVENT_LOG.groupRevokePriv(GrouperSession.staticGrouperSession(), Group.this.getName(), subj, priv, sw);
        }
        return wasntAlreadyRevoked;
      }
    });
  } 

  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attributeName  Set this attribute.
   * @param   value Set to this value.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setAttribute(final String attributeName, final String value) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException {
    setAttribute(attributeName, value, true);
  }

  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attributeName  Set this attribute.
   * @param   value Set to this value.
   * @param   checkPrivileges 
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setAttribute(final String attributeName, final String value, final boolean checkPrivileges) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException {
    this.internal_setAttribute(attributeName, value, checkPrivileges, null);
  }

  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attributeName  Set this attribute.
   * @param   value Set to this value.
   * @param   checkPrivileges 
   * @param uuid
   * @return the attribute
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public Attribute internal_setAttribute(final String attributeName, final String value, final boolean checkPrivileges, final String uuid) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException {
    
    return (Attribute)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              StopWatch sw = new StopWatch();
              sw.start();
              Field f = FieldFinder.find(attributeName, false);
              if (f == null) {
                throw new AttributeNotFoundException("Cant find attribute: " + attributeName);
              }
              if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
                throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
              }
        
              // TODO 20070531 split and test
              GrouperValidator v = NotNullOrEmptyValidator.validate(attributeName);
              if (v.isInvalid()) {
                throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attributeName);
              }
              v = NotNullOrEmptyValidator.validate(value);
              if (v.isInvalid()) {
                throw new GroupModifyException(E.INVALID_ATTR_VALUE + value);
              }
              if (checkPrivileges) {
                try { 
                  if ( !Group.this.canWriteField( FieldFinder.find(attributeName, true) ) ) {
                    throw new InsufficientPrivilegeException("Cant write field: " + attributeName);
                  }
                } catch (SchemaException se) {
                  throw new AttributeNotFoundException(se.getMessage() + ", " + attributeName, se);
                }
              }              
        //      if (_internal_fieldAttribute(attr)) {
        //        if (GrouperConfig.retrieveConfig().propertyValueBoolean("groups.allow.attribute.access.1.4", false)) {
        //          
        //          if (StringUtils.equals(FIELD_NAME, attr)) {
        //            this.setName(value);
        //          }
        //          if (StringUtils.equals(FIELD_EXTENSION, attr)) {
        //            this.setExtension(value);
        //          }
        //          if (StringUtils.equals(FIELD_DISPLAY_NAME, attr)) {
        //            this.setDisplayName(value);
        //          }
        //          if (StringUtils.equals(FIELD_DISPLAY_EXTENSION, attr)) {
        //            this.setDisplayExtension(value);
        //          }
        //          if (StringUtils.equals(FIELD_DESCRIPTION, attr)) {
        //            this.setDescription(value);
        //          }
        //          throw new RuntimeException("Not expecting attribute: " + attr);
        //          
        //        }
        //        throw new RuntimeException("Cannot access built in attribute: " + attr + " from setAttributes anymore, " +
        //            "use setter directly (e.g. setName(), setDisplayName()).  Or you can enable this (deprecated) with " +
        //            "grouper.properties setting groups.allow.attribute.access.1.4=true");
        //      }      
              
              //if this is not saved, then save it
              if (HibUtilsMapping.isInsert(Group.this)) {
                Group.this.store();
              }
        
              //init attributes
              Group.this.getAttributesMap(false);
        
              Attribute attribute = Group.this.attributes.get(attributeName);
              if (attribute != null && StringUtils.equals(attribute.getValue(), value)) {
                return null;
              }
              
              AuditTypeBuiltin auditTypeBuiltin = AuditTypeBuiltin.GROUP_ATTRIBUTE_UPDATE;
              String oldValue = null;
              String oldValueName = null;
              String verb = "Updated";
              if (attribute == null) {
                attribute = new Attribute();
                attribute.setId(uuid == null ? GrouperUuid.getUuid() : uuid);
                attribute.setFieldId( FieldFinder.findFieldIdForAttribute(attributeName, true ));
                auditTypeBuiltin = AuditTypeBuiltin.GROUP_ATTRIBUTE_ADD;
                verb = "inserted";
              } else {
                //if update, this is the old value
                oldValue = attribute.getValue();
                oldValueName = "oldValue";
              }
              
              attribute.assignGroupUuid(Group.this.getUuid(), Group.this);
              attribute.setValue( value );
              
              Group.this.attributes.put(attributeName, attribute);
              
              GrouperDAOFactory.getFactory().getAttribute().createOrUpdate(attribute);
              
              sw.stop();
              EVENT_LOG.groupSetAttr(GrouperSession.staticGrouperSession(), 
                  Group.this.getName(), attributeName, value, sw);

              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                
                AuditEntry auditEntry = new AuditEntry(auditTypeBuiltin, "id", 
                    attribute.getId(), "groupId", Group.this.getUuid(), 
                    "groupName", Group.this.getName(), "fieldId", f.getUuid(),
                    "fieldName", attributeName,  "value", attribute.getValue(), oldValueName, oldValue);
                
                auditEntry.setDescription(verb + " group attribute: " + attributeName + " on group: " 
                    + Group.this.getName() + " value: " + attribute.getValue() 
                    + (auditTypeBuiltin == AuditTypeBuiltin.GROUP_ATTRIBUTE_UPDATE ? (", oldValue: " + oldValue) : ""));
                auditEntry.saveOrUpdate(true);
              }
              
              return attribute;
            }
            catch (GrouperDAOException eDAO) {
              throw new GroupModifyException( eDAO.getMessage(), eDAO );
            }
            catch (InsufficientPrivilegeException eIP) {
              throw eIP;
            }
          }
        });
  }
  
  /**
   * store this object to the DB.
   */
  public void store() {    
    
    if (GrouperLoader.isDryRun()) {
      return;
    }
    
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
    
              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              Subject subject = GrouperSession.staticGrouperSession().getSubject();
              if (!Group.this.hasAdmin(subject)) {
                throw new InsufficientPrivilegeException(GrouperUtil
                    .subjectToString(subject)
                    + " is not admin on group: " + Group.this.getName());
              }
                
              if (Group.this.dbVersionDifferentFields().contains(FIELD_ALTERNATE_NAME_DB) &&
                  Group.this.getAlternateNameDb() != null) {
                  
                // We're only checking create privilege on the stem if the alternate name
                // is for another stem since prior to this, users could rename a group
                // without having create privileges.
                String parentStemName = GrouperUtil.parentStemNameFromName(Group.this.getAlternateNameDb());
                if (parentStemName == null || !parentStemName.equals(Group.this.getParentStem().getName())) {
                  Stem stem = GrouperUtil.getFirstParentStemOfName(Group.this.getAlternateNameDb());

                  if (!stem.hasCreate(subject)) {
                    throw new InsufficientPrivilegeException(GrouperUtil.subjectToString(subject)
                        + " cannot create in stem: " + stem.getName());
                  }
                }
              }
              
              if (Group.this.dbVersionDifferentFields().contains(FIELD_TYPE_OF_GROUP)) {
                TypeOfGroup oldTypeOfGroup = Group.this.dbVersion().getTypeOfGroup();
                TypeOfGroup newTypeOfGroup = Group.this.getTypeOfGroup();
                
                if (oldTypeOfGroup.equals(TypeOfGroup.group) && newTypeOfGroup.equals(TypeOfGroup.role)) {
                  // converting group to role
                  RoleSet existingRoleSet = GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(Group.this.getUuid(), false);
                  if (existingRoleSet == null) {
                    RoleSet roleSet = new RoleSet();
                    roleSet.setId(GrouperUuid.getUuid());
                    roleSet.setDepth(0);
                    roleSet.setIfHasRoleId(Group.this.getId());
                    roleSet.setThenHasRoleId(Group.this.getId());
                    roleSet.setType(RoleHierarchyType.self);
                    roleSet.setParentRoleSetId(roleSet.getId());
                    roleSet.saveOrUpdate();
                  }
                } else if (oldTypeOfGroup.equals(TypeOfGroup.role) && newTypeOfGroup.equals(TypeOfGroup.group)) {
                  // converting role to group
                  Set<RoleSet> roleSets = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleIdImmediate(Group.this.getUuid());
                  Iterator<RoleSet> iter = roleSets.iterator();
                  while (iter.hasNext()) {
                    RoleSet roleSet = iter.next();
                    roleSet.getIfHasRole().getRoleInheritanceDelegate().removeRoleFromInheritFromThis(roleSet.getThenHasRole());
                  }
                  
                  roleSets = GrouperDAOFactory.getFactory().getRoleSet().findByThenHasRoleIdImmediate(Group.this.getUuid());
                  iter = roleSets.iterator();
                  while (iter.hasNext()) {
                    RoleSet roleSet = iter.next();
                    roleSet.getIfHasRole().getRoleInheritanceDelegate().removeRoleFromInheritFromThis(roleSet.getThenHasRole());
                  }
                  
                  RoleSet selfRoleSet = GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(Group.this.getUuid(), false);
                  if (selfRoleSet != null) {
                    selfRoleSet.delete();
                  }

                  Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByOwnerGroupId(Group.this.getUuid());
                  Iterator<AttributeAssign> attributeAssignsIter = attributeAssigns.iterator();
                  while (attributeAssignsIter.hasNext()) {
                    AttributeAssign attributeAssign = attributeAssignsIter.next();
                    if (attributeAssign.getAttributeDef().getAttributeDefType().equals(AttributeDefType.perm)) {
                      attributeAssign.delete();
                    }
                  }
                }

              }

              
              String differences = GrouperUtil.dbVersionDescribeDifferences(Group.this.dbVersion(), 
                  Group.this, Group.this.dbVersion() != null ? Group.this.dbVersionDifferentFields() : Group.CLONE_FIELDS);

              GrouperDAOFactory.getFactory().getGroup().update( Group.this );
              
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = null;
                
                if (Group.this.typeOfGroup == TypeOfGroup.entity) {
                  
                  auditEntry = new AuditEntry(AuditTypeBuiltin.ENTITY_UPDATE, "id", 
                      Group.this.getUuid(), "name", Group.this.getName(), "parentStemId", Group.this.getParentUuid(), 
                      "displayName", Group.this.getDisplayName(), "description", Group.this.getDescription());
                  auditEntry.setDescription("Updated entity: " + Group.this.getName() + ", " + differences);

                } else {
                  auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_UPDATE, "id", 
                      Group.this.getUuid(), "name", Group.this.getName(), "parentStemId", Group.this.getParentUuid(), 
                      "displayName", Group.this.getDisplayName(), "description", Group.this.getDescription());
                  auditEntry.setDescription("Updated group: " + Group.this.getName() + ", " + differences);

                }
                
                auditEntry.saveOrUpdate(true);
              }
              
              return null;
            }
          });
      
  }

  /**
   * Set group description.  
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   * try {
   *   g.setDescription(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify description
   * }
   * </pre>
   * @param   value   Set description to this value.
   */
  public void setDescription(String value) {
    this.description = value;
  }

  /**
   * Set group description (hibernate method).  
   * @param   value   Set description to this value.
   */
  public void setDescriptionDb(String value) {
    this.description = value;
  }
  
  /**
   * Set group <i>extension</i>.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   *   g.setExtension(value);
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   */
  public void setExtension(String value) {
    setExtension(value, true);
  }
 
  /**
   * Set group <i>extension</i>.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   *   g.setExtension(value, true);
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @param assignAlternateName Whether to add the old group name as an 
   *                            alternate name for the renamed group.
   */
  public void setExtension(String value, boolean assignAlternateName) {
    NamingValidator v = NamingValidator.validate(value);
    if (v.isInvalid()) {
      throw new GroupModifyException( v.getErrorMessage() );
    }

    String oldExtension = null;
    if (this.dbVersion() != null) {
      oldExtension = this.dbVersion().getExtensionDb();
    }
    
    if (assignAlternateName && oldExtension != null && !oldExtension.equals(value)) {
      internal_addAlternateName(this.dbVersion().getNameDb(), false);
    }
    this.extension = value;
    this.setNameDb(U.constructName( this.getParentStem().getName(), value ) );

  }

  /**
   * Set group <i>name</i>.  This should not be called
   * @param   value   Set <i>extension</i> to this value.
   */
  public void setName(String value) {
    throw new InsufficientPrivilegeException("group name is system maintained: " + this.name + ", " + value);
  }

  /**
   * Set group displayExtension.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   *   g.setDisplayExtension(value);
   * </pre>
   * @param   value   Set displayExtension to this value.
   */
  public void setDisplayExtension(String value) {
    NamingValidator v = NamingValidator.validate(value);
    if (v.isInvalid()) {
      throw new GroupModifyException( v.getErrorMessage() );
    }

    this.displayExtension = value;
    this.setDisplayNameDb(U.constructName( this.getParentStem().getDisplayName(), value ) );

  }

  /**
   * hibernate method
   * @param value
   */
  public void setDisplayExtensionDb(String value) {
    this.displayExtension = value;
  }
  
  /**
   * hibernate method
   * @param value
   */
  public void setExtensionDb(String value) {
    this.extension = value;
  }
  
  /**
   * hibernate method
   * @return display extension
   */
  public String getDisplayExtensionDb() {
    return this.displayExtension;
  }
  
  /**
   * hibernate method
   * @return extension
   */
  public String getExtensionDb() {
    return this.extension;
  }
  
  /**
   * This is really only for hibernate
   * @param value new display name
   */
  public void setDisplayName(String value) {
    throw new InsufficientPrivilegeException("group display name is system maintained: " + this.name + ", " + value);
  }

  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   * @throws  GrouperException
   */
  public Member toMember() 
    throws  GrouperException
  {
    if ( this.cachedMember != null ) {
      return this.cachedMember;
    }
    try {
      GrouperSession.validate( GrouperSession.staticGrouperSession() );
      Member m = GrouperDAOFactory.getFactory().getMember().findBySubject( this.toSubject(), true );
      GrouperSession.staticGrouperSession();
      this.cachedMember = m;
      return this.cachedMember;
    }  
    catch (MemberNotFoundException eMNF) {
      // If we can't convert a group to a member we have major issues
      // and should probably just give up
      String msg = E.GROUP_G2M + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    }
  } // public Member toMember()

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   * @throws  GrouperException
   */
  public Subject toSubject() 
    throws  GrouperException
  {
    if ( this.subjectCache.containsKey(KEY_SUBJECT) ) {
      return this.subjectCache.get(KEY_SUBJECT);
    }
    try {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
         if (Group.this.getTypeOfGroup() == TypeOfGroup.entity) {
           Group.this.subjectCache.put(
               KEY_SUBJECT, SubjectFinder.findByIdAndSource( Group.this.getUuid(), SubjectFinder.internal_getEntitySourceAdapter(true).getId(), true )
             );
        
          } else {
           Group.this.subjectCache.put(
             KEY_SUBJECT, SubjectFinder.findByIdAndSource( Group.this.getUuid(), SubjectFinder.internal_getGSA().getId(), true )
           );
          }
        return null;
        }
      });
        
      return this.subjectCache.get(KEY_SUBJECT);
    }
    catch (SourceUnavailableException eShouldNeverHappen0)  {
      String msg = E.GROUP_G2S + eShouldNeverHappen0.getMessage();
      LOG.fatal(msg, eShouldNeverHappen0);
      throw new GrouperException(msg, eShouldNeverHappen0);
    }
    catch (SubjectNotFoundException eShouldNeverHappen1)    {
      String msg = E.GROUP_G2S + eShouldNeverHappen1.getMessage();
      LOG.fatal(msg, eShouldNeverHappen1);
      throw new GrouperException(msg, eShouldNeverHappen1);
    }
    catch (SubjectNotUniqueException eShouldNeverHappen2)   {
      String msg = E.GROUP_G2S + eShouldNeverHappen2.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eShouldNeverHappen2);
    }
  } // public Subject toSubject()

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "name", this.name)
      .append( "uuid", this.getUuid() )
      .toString();
  }


  /**
   * TODO 20070531 make into some flavor of validator
   * @param subj 
   * @param f 
   * @return  boolean
   * @throws IllegalArgumentException 
   * @throws SchemaException 
   * @since   1.2.1
   */
  public boolean internal_canWriteField(Subject subj, Field f)
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    } 
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException( E.INVALID_GROUP_TYPE + " for group name: " + this.getName() + ", " + f.getGroupType().toString() + ":" + f.getName() );
    }
    try {
      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, subj, f.getWritePriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  }

  /** if true, then dont set the modified stuff on save */
  @GrouperIgnoreClone @GrouperIgnoreFieldConstant @GrouperIgnoreDbVersion
  private boolean dontSetModified = false;

  /**
   * if we should not set the modified attributes on group save
   * @param theDontSetModified
   */
  public void setDontSetModified(boolean theDontSetModified) {
    this.dontSetModified = theDontSetModified;
  }
  
  /**
   * 
   */
  private void internal_setModifiedIfNeeded() {
    if (!dontSetModified) {
      this.setModifierUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
      this.setModifyTimeLong( System.currentTimeMillis() );
    }
  }

  /**
   * 
   * @param session
   * @param type
   * @param left
   * @param right
   * @param uuid 
   * @return composite
   * @throws InsufficientPrivilegeException
   * @throws MemberAddException
   */
  public Composite internal_addCompositeMember(final GrouperSession session, final CompositeType type,
      final Group left, final Group right, final String uuid) throws InsufficientPrivilegeException, MemberAddException {

    //dont allow anything to be an entity... can only be a group or role
    if (this.getTypeOfGroup() == TypeOfGroup.entity || left.getTypeOfGroup() == TypeOfGroup.entity 
        || right.getTypeOfGroup() == TypeOfGroup.entity) {
      throw new RuntimeException("Cannot add composite to an entity");
    }
    
    final String errorMessageSuffix = ", group name: " + this.name + ", compositeType: " + type
      + ", left group name: " + (left == null ? "null" : left.getName()) 
      + ", right group name: " + (right == null ? "null" : right.getName());

  return (Composite)HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {

        public Object callback(HibernateHandlerBean hibernateHandlerBean)
        throws GrouperDAOException {
          try {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            StopWatch sw = new StopWatch();
            sw.start();
      
            PrivilegeHelper.dispatch(session, Group.this, session.getSubject(), Group
                .getDefaultList().getWritePriv());
            PrivilegeHelper.dispatch(session, left, session.getSubject(), Group
                .getDefaultList().getReadPriv());
            PrivilegeHelper.dispatch(session, right, session.getSubject(), Group
                .getDefaultList().getReadPriv());
      
            Composite c = new Composite();
            c.setCreateTime(new Date().getTime());
            c.setCreatorUuid(session.getMember().getUuid());
            c.setFactorOwnerUuid(Group.this.getUuid());
            c.setLeftFactorUuid(left.getUuid());
            c.setRightFactorUuid(right.getUuid());
            c.setTypeDb(type.toString());
            c.setUuid(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
            CompositeValidator vComp = CompositeValidator.validate(c);
            if (vComp.isInvalid()) {
              throw new MemberAddException(vComp.getErrorMessage() + ", " + errorMessageSuffix);
            }
      
            AddCompositeMemberValidator vAdd = AddCompositeMemberValidator.validate(Group.this);
            if (vAdd.isInvalid()) {
              throw new MemberAddException(vAdd.getErrorMessage() + ", " + errorMessageSuffix);
            }

            GrouperDAOFactory.getFactory().getComposite().save(c);
            EVENT_LOG.groupAddComposite(session, c, sw);
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_COMPOSITE_ADD, "id", 
                  c.getUuid(), "ownerId", Group.this.getUuid(), "ownerName", Group.this.getName(), "leftFactorId", 
                  left.getUuid(), "leftFactorName", left.getName(), "rightFactorId", right.getUuid(), 
                  "rightFactorName", right.getName(), "type", type.toString());
              auditEntry.setDescription("Added composite: " + Group.this.getName() + " is " 
                  + left.getName() + " " + type.toString() + " " + right.getName());
              auditEntry.saveOrUpdate(true);
            }

            // since the composite add could have taken a while if there were many memberships to add,
            // make sure there weren't any immediate memberships added in the meantime..
            // note that since the commit isn't done yet, this issue can still happen,
            // but it would hopefully be much less likely..
            Set<Membership> immediateMemberships = GrouperDAOFactory.getFactory().getMembership().findAllMembershipEntriesByGroupOwnerAndFieldAndType(Group.this.getUuid(), Group.getDefaultList(), "immediate", false);
            if (immediateMemberships.size() > 0) {
              throw new IllegalStateException("Immediate memberships were added to this group (name=" + Group.this.getName() + ") while making it a composite group");
            }
            
            sw.stop();
            return c;
          } catch (SchemaException eS) {
            GrouperUtil.injectInException(eS, errorMessageSuffix);
            throw new MemberAddException(eS);
          } catch (RuntimeException re) {
            GrouperUtil.injectInException(re, errorMessageSuffix);
            throw re;
          }
        }
      });
  } 

  // @since   1.2.0
  /**
   * @param session
   * @param name 
   * @return if can read field
   */
  private boolean _canReadField(GrouperSession session, String name) {
    boolean rv = false;
    try {
      PrivilegeHelper.dispatch(session, this, session.getSubject(), FieldFinder.find(name, true).getReadPriv());
      rv = true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false ;
    }
    catch (SchemaException eS) {
      return false;
    }
    return rv;
  } // private boolean _canReadField(name)

  /**
   * 
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   */
  private void _revokeAllAccessPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException {

    try {
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            Group.this.revokePriv(AccessPrivilege.ADMIN);
            Group.this.revokePriv(AccessPrivilege.OPTIN);
            Group.this.revokePriv(AccessPrivilege.OPTOUT);
            Group.this.revokePriv(AccessPrivilege.READ);
            Group.this.revokePriv(AccessPrivilege.UPDATE);
            Group.this.revokePriv(AccessPrivilege.VIEW);
          } catch (InsufficientPrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (SchemaException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (RevokePrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      if (gse.getCause() instanceof RevokePrivilegeException) {
        throw (RevokePrivilegeException)gse.getCause();
      }
      throw gse;
    }


  } // private void _revokeAllAccessPrivs()

  
  /**
   * @return the set of different fields
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  public Set<String> dbVersionDifferentFields() {
    return dbVersionDifferentFields(true);
    
  }
  

  
  /**
   * @param failIfNull 
   * @return the set of different fields
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  public Set<String> dbVersionDifferentFields(boolean failIfNull) {
    if (this.dbVersion == null) {
      if (failIfNull) {
        throw new RuntimeException("State was never stored from db");
      }
      return null;
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);

    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public Group clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getName(), ( (Group) other ).getName() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * This is a direct access to the attributes (going around security checking etc).
   * You probably shouldnt be calling this if not on the grouper team.
   * @param checkSecurity if we should check if the current user can see each attribute
   * 
   * @return attributes, will never be null
   * @since   1.2.0
   */
  public Map<String, Attribute> getAttributesMap(boolean checkSecurity) {
  
    if (this.attributes == null) {
      this.attributes = GrouperDAOFactory.getFactory().getAttribute().findAllAttributesByGroup( this.getUuid() );
    }
    
    if (!checkSecurity) {
      return Collections.unmodifiableMap(this.attributes);
    }
    
    Map<String, Attribute> result = new HashMap<String, Attribute>();
    
    for (String key : this.attributes.keySet()) {
      if ( this._canReadField( GrouperSession.staticGrouperSession(), key ) ) {
        result.put(key, this.attributes.get(key));
      }
    }
    return result;
  }

  /**
   * This is a direct access to the attributes (going around security checking etc).
   * Use the other get attributes map method
   * 
   * @return attributes
   * @since   1.2.0
   */
  @Deprecated
  public Map<String, String> getAttributesDb() {

//      if (GrouperConfig.retrieveConfig().propertyValueBoolean("groups.allow.attribute.access.1.4", false)) {
//        
//        String theName = this.getName();
//        if (!StringUtils.isBlank(theName)) {
//          this.attributes.put("name", theName);
//        }
//        String theDisplayName = this.getDisplayName();
//        if (!StringUtils.isBlank(theDisplayName)) {
//          this.attributes.put("displayName", theDisplayName);
//        }
//        String theExtension = this.getExtension();
//        if (!StringUtils.isBlank(theExtension)) {
//          this.attributes.put("extension", theExtension);
//        }
//        String theDisplayExtension = this.getDisplayExtension();
//        if (!StringUtils.isBlank(theDisplayExtension)) {
//          this.attributes.put("displayExtension", theDisplayExtension);
//        }
//        String theDescription = this.getDescription();
//        if (!StringUtils.isBlank(theDescription)) {
//          this.attributes.put("description", theDescription);
//        }
//      }

    //init
    this.getAttributesMap(false);
    Map<String, String> map = new HashMap<String, String>();
    for (String key : this.attributes.keySet()) {
      map.put(key, this.attributes.get(key).getValue());
    }
    return map;
  }

  /**
   * @return create time
   * @since   1.2.0
   */
  public long getCreateTimeLong() {
    return this.createTime;
  }

  /**
   * @return creator
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersion()
   */
  @Override
  public Group dbVersion() {
    return (Group)this.dbVersion;
  }

  /**
   * 
   * @return uuid
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @return  modify time
   * @since   1.2.0
   */
  public long getModifyTimeLong() {
    return this.modifyTime;
  }

  /**
   * @return parent uuid
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUuid;
  }

  /**
   * @return uuid
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @return hashcode
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getName() )
      .toHashCode();
  } // public int hashCode()

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onDelete(org.hibernate.Session)
   */
  @Override
  public boolean onDelete(Session hs) 
    throws  CallbackException {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), false );
    return Lifecycle.NO_VETO;
  }

//  /** instance var of attributes to compare, so if a hook does this we are all set */
//  private Map _internateAttributesToCompare = null;
//
//  /**
//   * update the attributes for a group
//   * @param hibernateSession 
//   * @param checkExisting true if an update, false if insert
//   */
//  public void _updateAttributes(HibernateSession hibernateSession, boolean checkExisting) {
//    ByObject byObject = hibernateSession.byObject();
//
//    Map                   attrs = new HashMap(this.getAttributesDb());
//    String                k;
//asdf
//    List<Attribute> attributes = checkExisting ? GrouperDAOFactory.getFactory().getGroup()._getAttributes(hibernateSession, this) : null;
//    for (Attribute attribute : GrouperUtil.nonNull(attributes)) {
//      k = attribute.getAttrName();
//      if ( attrs.containsKey(k) ) {
//        // attr both in db and in memory.  compare.
//        if ( !attribute.getValue().equals( (String) attrs.get(k) ) ) {
//          attribute.setValue( (String) attrs.get(k) );
//          byObject.update(attribute);
//        }
//        attrs.remove(k);
//      }
//      else {
//        // attr only in db.
//        byObject.delete(attribute);
//        attrs.remove(k);
//      }
//    }
//    // now handle entries that were only in memory
//    Map.Entry kv;
//    Iterator it = attrs.entrySet().iterator();
//    while (it.hasNext()) {
//      kv = (Map.Entry) it.next();
//      Attribute attribute = new Attribute(); 
//      attribute.setFieldId( FieldFinder.findFieldIdForAttribute((String) kv.getKey(), true ));
//      attribute.assignGroupUuid(this.getUuid(), this);
//      attribute.setValue( (String) kv.getValue() );
//      byObject.save(attribute);
//    }
//  } // private void _updateAttributes(hs)


  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_INSERT, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_INSERT, true, false);
    
    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_INSERT, HooksGroupBean.class, 
        this, Group.class);

  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    if (this.dbVersionDifferentFields().contains(FIELD_NAME)) {
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
        /**
         * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
         */
        public Object callback(GrouperSession rootSession) throws GrouperSessionException {

          // if this is an entity, need to potentially update the subject identifier.
          if (Group.this.getTypeOfGroup() == TypeOfGroup.entity) {
            String oldPrefix = GrouperUtil.parentStemNameFromName(Group.this.dbVersion().getName()) + ":";
            String newPrefix = GrouperUtil.parentStemNameFromName(Group.this.getName()) + ":";
            
            if (!oldPrefix.equals(newPrefix)) {
              String subjectIdentifier = Group.this.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName());
              if (subjectIdentifier != null && subjectIdentifier.startsWith(oldPrefix)) {
                subjectIdentifier = newPrefix + subjectIdentifier.substring(oldPrefix.length());
                Group.this.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), subjectIdentifier);
              }
            }
          }
          
          // need to potentially update group name in rules
          Set<RuleDefinition> definitions = RuleEngine.ruleEngine().getRuleDefinitions();
          for (RuleDefinition definition : definitions) {
            if (definition.getCheck() != null && definition.getCheck().checkTypeEnum() != null && 
                definition.getCheck().checkTypeEnum().isCheckOwnerTypeGroup(definition) && Group.this.dbVersion().getName().equals(definition.getCheck().getCheckOwnerName())) {
              definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), Group.this.getName());
            }
            
            if (definition.getIfCondition() != null && definition.getIfCondition().ifConditionEnum() != null &&
                definition.getIfCondition().ifConditionEnum().isIfOwnerTypeGroup(definition) && Group.this.dbVersion().getName().equals(definition.getIfCondition().getIfOwnerName())) {
              definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleIfOwnerNameName(), Group.this.getName());
            }
            
            // thenEnumArg0 can be a packed subject string so it may need to be updated...
            RuleThenEnum ruleThenEnum = definition.getThen().thenEnum();
            if ((ruleThenEnum == RuleThenEnum.assignGroupPrivilegeToGroupId ||
                ruleThenEnum == RuleThenEnum.assignStemPrivilegeToStemId ||
                ruleThenEnum == RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) &&
                definition.getThen().getThenEnumArg0().endsWith(Group.this.dbVersion().getName())) {
              
              String prefix = definition.getThen().getThenEnumArg0().substring(0, definition.getThen().getThenEnumArg0().length() - Group.this.dbVersion().getName().length());
              if (prefix.trim().isEmpty() || prefix.trim().endsWith("::")) {
                definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg0Name(), prefix + Group.this.getName());
              }
            }
          }
          
          return null;
        }
      });
    }
    
    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_UPDATE, HooksGroupBean.class, 
        this, Group.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_UPDATE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_UPDATE, true, false);

    // update member table
    this.toMember().updateMemberAttributes(new GrouperSubject(this), true);
  }

  /**
   * when the last member has changed
   */
  private Long lastMembershipChangeDb;
  
  /**
   * when the last member has changed
   * @return when
   */
  public Long getLastMembershipChangeDb() {
    return this.lastMembershipChangeDb;
  }
  
  /**
   * when the last member has changed
   * @param theMembershipLastChange
   */
  public void setLastMembershipChangeDb(Long theMembershipLastChange) {
    this.lastMembershipChangeDb = theMembershipLastChange;
  }
  
  /**
   * when the last member has changed
   * @return the membership last change timestamp
   */
  public Timestamp getLastMembershipChange() {
    return this.lastMembershipChangeDb == null ? null : new Timestamp(this.lastMembershipChangeDb);
  }
  
  /**
   * when the last immediate member has changed
   */
  private Long lastImmediateMembershipChangeDb;
  
  /**
   * when the last immediate member has changed
   * @return when
   */
  public Long getLastImmediateMembershipChangeDb() {
    return this.lastImmediateMembershipChangeDb;
  }
  
  /**
   * when the last immediate member has changed
   * @param theImmediateMembershipLastChange
   */
  public void setLastImmediateMembershipChangeDb(Long theImmediateMembershipLastChange) {
    this.lastImmediateMembershipChangeDb = theImmediateMembershipLastChange;
  }
  
  /**
   * when the last immediate member has changed
   * @return the immediate membership last change timestamp
   */
  public Timestamp getLastImmediateMembershipChange() {
    return this.lastImmediateMembershipChangeDb == null ? null : new Timestamp(this.lastImmediateMembershipChangeDb);
  }
  
  
  /**
   * Returns the alternate name for the group.  Used by hibernate.
   * @return the alternate name
   */
  public String getAlternateNameDb() {
    return this.alternateNameDb;
  }
  
  /**
   * Set the group's alternate name  Used by hibernate.
   * @param alternateName
   */
  public void setAlternateNameDb(String alternateName) {
    this.alternateNameDb = alternateName;
    this.alternateNames = null;
  }
  
  /** alternate names */
  private Set<String> alternateNames = null;
  
  /**
   * Returns the alternate names for the group.  Only one alternate name is supported
   * currently, so a Set of size 0 or 1 will be returned.
   * @return Set of alternate names.
   */
  public Set<String> getAlternateNames() {
    
    //lazy load this set
    if (this.alternateNames == null) {
      this.alternateNames = new LinkedHashSet<String>();
      if (!StringUtils.isBlank(this.alternateNameDb)) {
        this.alternateNames.add(this.alternateNameDb);
      }
      this.alternateNames = Collections.unmodifiableSet(this.alternateNames);
    }
    return this.alternateNames;
  }

  /**
   * Add an alternate name for this group.  Only one alternate name is supported
   * currently, so this will replace any existing alternate name.
   * This won't get saved until you call store().
   * @param alternateName
   */
  public void addAlternateName(String alternateName) {
    internal_addAlternateName(alternateName, true);
  }
  
  /**
   * Add an alternate name for this group.  Only one alternate name is supported
   * currently, so this will replace any existing alternate name.
   * This won't get saved until you call store().
   * @param alternateName
   * @param validateName
   */
  protected void internal_addAlternateName(String alternateName, boolean validateName) {
    
    if (validateName) {
      GrouperValidator v = AddAlternateGroupNameValidator.validate(alternateName);
    
      if (v.isInvalid()) {
        throw new GroupModifyException(v.getErrorMessage() + ": " + alternateName);
      }
    }

    this.alternateNameDb = alternateName;
  }
  
  /**
   * Delete the specified alternate name.  This won't get saved until you call store().
   * @param alternateName
   * @return false if the group does not have the specified alternate name
   */
  public boolean deleteAlternateName(String alternateName) {
    if (alternateName.equals(this.alternateNameDb)) {
      this.alternateNameDb = null;
      return true;
    }
    
    return false;
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_DELETE, HooksGroupBean.class, 
        this, Group.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_DELETE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_DELETE, false, true);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    this.internal_setModifiedIfNeeded();
    
    if (this.idIndex == null) {
      this.idIndex = TableIndex.reserveId(TableIndexType.group);
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_INSERT, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_INSERT, false, false);
    
    if (this.getTypeOfGroup() == TypeOfGroup.entity) {
      
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ENTITY_ADD, 
          ChangeLogLabels.ENTITY_ADD.id.name(), 
          this.getUuid(), ChangeLogLabels.ENTITY_ADD.name.name(), 
          this.getName(), ChangeLogLabels.ENTITY_ADD.parentStemId.name(), this.getParentUuid(),
          ChangeLogLabels.ENTITY_ADD.displayName.name(), this.getDisplayName(),
          ChangeLogLabels.ENTITY_ADD.description.name(), this.getDescription()).save();

    } else {
      
      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_ADD, 
          ChangeLogLabels.GROUP_ADD.id.name(), 
          this.getUuid(), ChangeLogLabels.GROUP_ADD.name.name(), 
          this.getName(), ChangeLogLabels.GROUP_ADD.parentStemId.name(), this.getParentUuid(),
          ChangeLogLabels.GROUP_ADD.displayName.name(), this.getDisplayName(),
          ChangeLogLabels.GROUP_ADD.description.name(), this.getDescription()).save();
      
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session hs) throws  CallbackException {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  }

  /**
   * set the attributes if computed in one query
   * @param theAttributes
   */
  public void internal_setAttributes(Map<String, Attribute> theAttributes) {
    this.attributes = theAttributes;
  }
  
  /**
   * 
   * @param attributes
   */
  public void setAttributes(Map<String, String> attributes) {
    
    attributes = GrouperUtil.nonNull(attributes);
    
    //make a copy of existing attributes
    Set<String> existingAttributeNames = 
      new HashSet<String>(this.getAttributesMap(false).keySet());
    
    //add or change
    for (String key : attributes.keySet()) {
      this.setAttribute(key, attributes.get(key));
      existingAttributeNames.remove(key);
    }
    
    //remove ones not in there
    for (String key : existingAttributeNames) {
      this.deleteAttribute(key);
    }
  }

  /**
   * @param createTime 
   * @since   1.2.0
   */
  public void setCreateTimeLong(long createTime) {
    this.createTime = createTime;

  }

  /**
   * @param creatorUUID 
   * @since   1.2.0
   */
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  
  }

  /**
   * @param modifierUUID 
   * @since   1.2.0
   */
  public void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;

  }

  /**
   * @param modifyTime 
   * @since   1.2.0
   */
  public void setModifyTimeLong(long modifyTime) {
    this.modifyTime = modifyTime;

  }

  /**
   * @param parentUUID 
   * @since   1.2.0
   */
  public void setParentUuid(String parentUUID) {
    this.parentUuid = parentUUID;

  }

  /**
   * @param types 
   * @since   1.2.0
   */
  public void setTypes(Set types) {
    this.types = types;

  }

  /**
   * @param uuid 
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;

  }

  /**
   * @return string
   * @since   1.2.0
   */
  public String toStringDb() {
    return new ToStringBuilder(this)
      .append( "attributes",   this.getAttributesDb()   )
      .append( "createTime",   this.getCreateTimeLong()   )
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "modifierUuid", this.getModifierUuid() )
      .append( "modifyTime",   this.getModifyTime()   )
      .append( "uuid",    this.getUuid()         )
      .append( "parentUuid",   this.getParentUuid()   )
      .append( "types",        this.getTypesDb()        )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_DELETE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_DELETE, false, false);

    if (this.getTypeOfGroup() == TypeOfGroup.entity) {

      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.ENTITY_DELETE, 
          ChangeLogLabels.ENTITY_DELETE.id.name(), 
          this.getUuid(), ChangeLogLabels.ENTITY_DELETE.name.name(), 
          this.getName(), ChangeLogLabels.ENTITY_DELETE.parentStemId.name(), this.getParentUuid(),
          ChangeLogLabels.ENTITY_DELETE.displayName.name(), this.getDisplayName(),
          ChangeLogLabels.ENTITY_DELETE.description.name(), this.getDescription()).save();

    } else {

      //change log into temp table
      new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_DELETE, 
          ChangeLogLabels.GROUP_DELETE.id.name(), 
          this.getUuid(), ChangeLogLabels.GROUP_DELETE.name.name(), 
          this.getName(), ChangeLogLabels.GROUP_DELETE.parentStemId.name(), this.getParentUuid(),
          ChangeLogLabels.GROUP_DELETE.displayName.name(), this.getDisplayName(),
          ChangeLogLabels.GROUP_DELETE.description.name(), this.getDescription()).save();

    }

  }
  
  /**
   * @param fieldName 
   * @return  the value
   * @see edu.internet2.middleware.grouper.GrouperAPI#fieldValue(java.lang.String)
   */
  public Object fieldValue(String fieldName) {
    
    return super.fieldValue(fieldName);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    
    super.onPreUpdate(hibernateSession);
    
    //if the type of group was changed, and it went to or from entity, that is bad
    if (this.dbVersionDifferentFields().contains(FIELD_TYPE_OF_GROUP) 
        && this.dbVersion != null && (this.typeOfGroup == TypeOfGroup.entity || ((Group)this.dbVersion).getTypeOfGroup() == TypeOfGroup.entity)) {
      throw new RuntimeException("Cannot change typeOfGroup, you need to delete and create the object");
    }
    
    // If the group name is changing, verify that the new name is not in use.
    // (The new name could be an alternate name).
    if (this.dbVersionDifferentFields().contains(FIELD_NAME)) {
      Group check = GrouperDAOFactory.getFactory().getGroup().findByName(this.getNameDb(), false);
      if (check != null && 
          (!check.getUuid().equals(this.getUuid()) || 
              (this.getAlternateNameDb() != null && 
                  this.getAlternateNameDb().equals(this.getNameDb())))) {
        throw new GroupModifyAlreadyExistsException("Group with name " + this.getNameDb() + " already exists.");
      }
    }
    
    // If the alternate name is changing, do the following check...
    // If the group name is not changing OR
    // if the group name is changing and the alternate name is not the old group name, THEN
    // we need to verify the alternate name isn't already taken.
    if (this.dbVersionDifferentFields().contains(FIELD_ALTERNATE_NAME_DB) &&
        this.getAlternateNameDb() != null) {
      
      String oldName = this.dbVersion().getNameDb();
      if (!this.dbVersionDifferentFields().contains(FIELD_NAME) || 
          (this.dbVersionDifferentFields().contains(FIELD_NAME) && 
              !oldName.equals(this.getAlternateNameDb()))) {
        Group check = GrouperDAOFactory.getFactory().getGroup().findByName(this.getAlternateNameDb(), false);
        if (check != null) {
          throw new GroupModifyAlreadyExistsException("Group with name " + this.getAlternateNameDb() + " already exists.");
        }
      }
    }
    
    this.internal_setModifiedIfNeeded();

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_UPDATE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_UPDATE, false, false);
    
    if (this.getTypeOfGroup() == TypeOfGroup.entity) {
      //change log into temp table
      ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.ENTITY_UPDATE, 
          this, this.dbVersion(),
          GrouperUtil.toList(ChangeLogLabels.ENTITY_UPDATE.id.name(),this.getUuid(), 
              ChangeLogLabels.ENTITY_UPDATE.name.name(), this.getName(),
              ChangeLogLabels.ENTITY_UPDATE.parentStemId.name(), this.getParentUuid(),
              ChangeLogLabels.ENTITY_UPDATE.displayName.name(), this.getDisplayName(),
              ChangeLogLabels.ENTITY_UPDATE.description.name(), this.getDescription()),
          GrouperUtil.toList(FIELD_NAME, FIELD_PARENT_UUID, FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION),
          GrouperUtil.toList(ChangeLogLabels.GROUP_UPDATE.name.name(),
              ChangeLogLabels.ENTITY_UPDATE.parentStemId.name(), 
              ChangeLogLabels.ENTITY_UPDATE.description.name(), 
              ChangeLogLabels.ENTITY_UPDATE.displayExtension.name()));    
    } else {
      //change log into temp table
      ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.GROUP_UPDATE, 
          this, this.dbVersion(),
          GrouperUtil.toList(ChangeLogLabels.GROUP_UPDATE.id.name(),this.getUuid(), 
              ChangeLogLabels.GROUP_UPDATE.name.name(), this.getName(),
              ChangeLogLabels.GROUP_UPDATE.parentStemId.name(), this.getParentUuid(),
              ChangeLogLabels.GROUP_UPDATE.displayName.name(), this.getDisplayName(),
              ChangeLogLabels.GROUP_UPDATE.description.name(), this.getDescription()),
          GrouperUtil.toList(FIELD_NAME, FIELD_PARENT_UUID, FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION),
          GrouperUtil.toList(ChangeLogLabels.GROUP_UPDATE.name.name(),
              ChangeLogLabels.GROUP_UPDATE.parentStemId.name(), 
              ChangeLogLabels.GROUP_UPDATE.description.name(), 
              ChangeLogLabels.GROUP_UPDATE.displayExtension.name()));    
      
    }
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object that) {
    if (that==null) {
      return 1;
    }
    if (!(that instanceof Group)) {
      return 1;
    }
    String thisName = StringUtils.defaultString(this.getName());
    String thatName = StringUtils.defaultString(((Group)that).getName());
    return thisName.compareTo(thatName);
  }

  /**
   * add or correct the includes/excludes group math structure for this group (as overall or system
   * or record if named correctly)
   * @param grouperSession
   * @param isIncludeExcludes 
   */
  public void manageIncludesExcludesRequiredGroups(GrouperSession grouperSession, boolean isIncludeExcludes) {
    this.manageIncludesExcludesRequiredGroups(grouperSession, isIncludeExcludes, new LinkedHashSet<Group>());
  }

  /**
   * add or correct the includes/excludes group math structure for this group (as overall or system
   * or record if named correctly)
   * @param grouperSession
   * @param isIncludeExcludes 
   * @param andGroup groups (like activeEmployee) which the user must also be in
   */
  public void manageIncludesExcludesRequiredGroups(GrouperSession grouperSession, boolean isIncludeExcludes, Group andGroup) {
    
    this.manageIncludesExcludesRequiredGroups(grouperSession, isIncludeExcludes, 
        andGroup == null ? new HashSet<Group>() : GrouperUtil.toSet(andGroup));
  }

  /**
   * add or correct the includes/excludes group math structure for this group (as overall or system
   * or record if named correctly)
   * @param grouperSession
   * @param isIncludeExcludes 
   * @param andGroups groups (like activeEmployee) which the user must also be in
   */
  public void manageIncludesExcludesRequiredGroups(GrouperSession grouperSession, boolean isIncludeExcludes, Set<Group> andGroups) {
    
    GroupTypeTupleIncludeExcludeHook.manageIncludesExcludesAndGroups(this, isIncludeExcludes, andGroups,
        "from manageIncludesExclude() method in Group class: " + this.getExtension());
  }

  /**
   * Get group displayName for hibernate.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   * @throws  GrouperException
   */
  public String getDisplayNameDb() 
    throws  GrouperException
  {
    return this.displayName;
  } // public String getDisplayName()

  /**
   * Get group name for hibernate.
   * @return  Group name db.
   * @throws  GrouperException
   */
  public String getNameDb() {
    return this.name;
  }

  /**
   * This is really only for hibernate
   * @param value new display name
   */
  public void setDisplayNameDb(String value) {
    this.displayName = value;
  }

  /**
   * Set group <i>name</i>.  This should not be called
   * @param   value   Set <i>extension</i> to this value.
   */
  public void setNameDb(String value) {
    this.name = value;
  }

  /**
   * Move this group to another Stem.  If you would like to specify options for the move, 
   * use GroupMove instead.  This will use the default options.
   * @param stem 
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void move(Stem stem) throws GroupModifyException,
      InsufficientPrivilegeException {
    
    new GroupMove(this, stem).save();
  }
  
  /**
   * 
   * @param stem
   * @param assignAlternateName
   */
  protected void internal_move(final Stem stem, final boolean assignAlternateName) {

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperSession.validate(GrouperSession.staticGrouperSession());
            
            // cannot move to the root stem
            if (stem.isRootStem()) {
              throw new GroupModifyException("Cannot move group to the root stem.");
            }
            
            // verify that the subject has admin privileges to the group
            if (!PrivilegeHelper.canAdmin(GrouperSession.staticGrouperSession(), Group.this,
                GrouperSession.staticGrouperSession().getSubject())) {
              throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
            }
            
            // verify that the subject can create groups in the stem where the group will be moved to.
            if (!PrivilegeHelper.canCreate(GrouperSession.staticGrouperSession(), stem,
                GrouperSession.staticGrouperSession().getSubject())) {
              throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
            }
            
            // if moving to the same stem, just return.
            if (stem.getUuid().equals(Group.this.getParentUuid())) {
              return null;
            }
            
            String oldName = Group.this.dbVersion().getNameDb();
            
            Group.this.setParentUuid(stem.getUuid());
            Group.this.setNameDb(stem.getName() + Stem.DELIM + Group.this.getExtension());
            Group.this.setDisplayNameDb(stem.getDisplayName() + Stem.DELIM 
                + Group.this.getDisplayExtension());
            
            if (assignAlternateName) {
              Group.this.internal_addAlternateName(oldName, false);
            }
            
            GrouperDAOFactory.getFactory().getGroup().update(Group.this);
            
            //if not a smaller operation of a larger auditable call
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_MOVE,
                  "groupId", Group.this.getUuid(), "oldGroupName", 
                  oldName, "newGroupName", Group.this.getName(), "newStemId",
                  stem.getUuid(), 
                  "assignAlternateName", assignAlternateName ? "T" : "F");
              auditEntry.setDescription("Move group " + oldName + " to name: " + Group.this.getName()
                  + ", assignAlternateName? " + (assignAlternateName ? "T" : "F")); 
              auditEntry.saveOrUpdate(true);
            }

            
            return null;
          }
        });
  }
  
  /**
   * 
   * @param stem
   * @param privilegesOfGroup
   * @param groupAsPrivilege
   * @param listMembersOfGroup
   * @param listGroupAsMember
   * @param attributes
   * @param composite
   * @param addDefaultGroupPrivileges
   * @param checkSecurity true to check that user can do this operation, false to ignore
   * @return group
   * @throws GroupAddException
   * @throws InsufficientPrivilegeException
   */
  protected Group internal_copy(final Stem stem, final boolean privilegesOfGroup,
      final boolean groupAsPrivilege, final boolean listMembersOfGroup,
      final boolean listGroupAsMember, final boolean attributes, final boolean composite,
      final boolean addDefaultGroupPrivileges, final boolean checkSecurity)
      throws GroupAddException, InsufficientPrivilegeException {
    
    return (Group)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            if (checkSecurity) {
              // verify that the subject has read privileges to the group
              if (!PrivilegeHelper.canRead(GrouperSession.staticGrouperSession(), Group.this,
                  GrouperSession.staticGrouperSession().getSubject())) {
                throw new InsufficientPrivilegeException(E.CANNOT_READ);
              }
            }
            
            GrouperSession actAs = null;
            if (addDefaultGroupPrivileges == true) {
              actAs = GrouperSession.staticGrouperSession();
            } else {
              actAs = GrouperSession.staticGrouperSession().internal_getRootSession();
            }
            
            Map<String, String> attributesMap = new HashMap<String, String>();
            if (attributes == true) {                  
              Map filtered = new HashMap();
              Map.Entry<String, Attribute> kv;
              Iterator<Map.Entry<String, Attribute>> it = Group.this.getAttributesMap(false).entrySet().iterator();
              while (it.hasNext()) {
                kv = it.next();
                if (Group.this._canReadField(actAs, (String) kv.getKey())) {
                  filtered.put((String)kv.getKey(), (String) kv.getValue().getValue());
                } else {
                  throw new InsufficientPrivilegeException("cannot read attribute on " + Group.this.getName());
                }
              }
              attributesMap = filtered;
            }

                Group newGroup = null;
                
                try {
                  newGroup = stem.internal_addChildGroup(actAs, Group.this.getExtension(),
                      Group.this.getDisplayExtensionDb(), null, Group.this
                          .description, Group.this.getTypesDb(), attributesMap,
                      addDefaultGroupPrivileges, Group.this.typeOfGroup);
                } catch (GroupAddException e) {
                  Group test = GroupFinder.findByName(GrouperSession
                      .staticGrouperSession().internal_getRootSession(), stem.getName()
                      + Stem.DELIM + Group.this.getExtension(), false);
                  if (test == null) {
                    throw e;
                  }
                  
                  // if the group already exists in the new stem, lets append ".#" to the extension.
                  String newGroupExtension = Group.this.getExtensionDb() + ".2";
                  int extensionCount = 2;
                  boolean notFound = false;
                  while (notFound == false) {
                    Group foundGroup = GroupFinder.findByName(GrouperSession
                        .staticGrouperSession().internal_getRootSession(), stem.getName()
                        + Stem.DELIM + newGroupExtension, false);
                    if (foundGroup != null) {
                      extensionCount++;
                      newGroupExtension = Group.this.getExtensionDb() + "."
                          + extensionCount;
                    } else {
                      notFound = true;
                    }
                  }
                  
                  newGroup = stem.internal_addChildGroup(actAs, newGroupExtension,
                      Group.this.getDisplayExtensionDb(), null, Group.this
                          .description, Group.this.getTypesDb(), attributesMap,
                addDefaultGroupPrivileges, Group.this.typeOfGroup);
                }
            
            if (composite) {
              Composite oldComposite = GrouperDAOFactory.getFactory()
                  .getComposite().findAsOwner(Group.this, false);
              if (oldComposite != null) {
                String leftFactorUuid = oldComposite.getLeftFactorUuid();
                String rightFactorUuid = oldComposite.getRightFactorUuid();
  
                Group leftFactorGroup = GroupFinder
                    .findByUuid(GrouperSession.staticGrouperSession()
                        .internal_getRootSession(), leftFactorUuid, true);
                Group rightFactorGroup = GroupFinder.findByUuid(GrouperSession
                    .staticGrouperSession().internal_getRootSession(),
                    rightFactorUuid, true);
  
                newGroup.internal_addCompositeMember(actAs, oldComposite.getType(),
                    leftFactorGroup, rightFactorGroup, null);
              }
            }

            if (privilegesOfGroup == true) {
              newGroup.internal_copyPrivilegesOfGroup(actAs, Group.this);
            }

            if (groupAsPrivilege == true) {
              newGroup.internal_copyGroupAsPrivilege(GrouperSession.staticGrouperSession(), Group.this);
            }

            if (listMembersOfGroup == true) {
              newGroup.internal_copyListMembersOfGroup(actAs, Group.this);
            }

            if (listGroupAsMember == true) {
              newGroup.internal_copyGroupAsMember(GrouperSession.staticGrouperSession(), Group.this);
            }
            
            final Group NEW_GROUP = newGroup;
            
            GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              /**
               * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
               */
              public Object callback(GrouperSession rootSession) throws GrouperSessionException {

                // may need to copy over the subject identifier if this is an entity
                if (TypeOfGroup.entity == NEW_GROUP.getTypeOfGroup()) {
                  String oldPrefix = GrouperUtil.parentStemNameFromName(Group.this.getName()) + ":";
                  String newPrefix = GrouperUtil.parentStemNameFromName(NEW_GROUP.getName()) + ":";
                  
                  if (!oldPrefix.equals(newPrefix)) {
                    String subjectIdentifier = Group.this.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName());
                    if (subjectIdentifier != null && subjectIdentifier.startsWith(oldPrefix)) {
                      subjectIdentifier = newPrefix + subjectIdentifier.substring(oldPrefix.length());
                      NEW_GROUP.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), subjectIdentifier);
                    }
                  }
                }
                
                return null;
              }
            });
            
            //if not a smaller operation of a larger auditable call
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_COPY,
                  "oldGroupId", Group.this.getUuid(), "oldGroupName", 
                  Group.this.getName(), "newGroupId", newGroup.getUuid(), "newGroupName",
                  newGroup.getName(), 
                  "privilegesOfGroup", privilegesOfGroup ? "T" : "F", "groupAsPrivilege",
                  groupAsPrivilege ? "T" : "F", "listMembersOfGroup",
                  listMembersOfGroup ? "T" : "F", "listGroupAsMember",
                  listGroupAsMember ? "T" : "F");
              auditEntry.setInt01(attributes ? 1L : 0L);
              auditEntry.setDescription("Copy group " + Group.this.getName() + " to name: " + newGroup.getName()
                  + ", privilegesOfGroup? " + (privilegesOfGroup ? "T" : "F")
                  + ", groupAsPrivilege? " + (groupAsPrivilege ? "T" : "F") 
                  + ", listMembersOfGroup? " + (listMembersOfGroup ? "T" : "F") 
                  + ", listGroupAsMember? " + (listGroupAsMember ? "T" : "F") 
                  + ", attributes? " + (attributes ? "T" : "F")); 
              auditEntry.saveOrUpdate(true);
            }
            
            return newGroup;
          }
        });
  }

  /**
   * 
   * @param session
   * @param group
   * @throws SchemaException
   * @throws MemberAddException
   * @throws GrouperException
   * @throws InsufficientPrivilegeException
   */
  private void internal_copyGroupAsMember(GrouperSession session, Group group)
      throws SchemaException, MemberAddException, GrouperException,
      InsufficientPrivilegeException {

    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndFieldType(group.toMember().getUuid(),
            FieldType.LIST.toString(), false);
    
    if (memberships.size() == 0) {
      return;
    }
    
    Member member = MemberFinder.findBySubject(session, this.toSubject(), true);

    Iterator<Membership> membershipsIter = memberships.iterator();
    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Field f = FieldFinder.findById(existingMembership.getFieldId(), true);
      Group g = existingMembership.getOwnerGroup();
      PrivilegeHelper.dispatch(session, g, session.getSubject(), f.getWritePriv());
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setMemberUuid(member.getUuid());
      copiedMembership.setMember(member);
      copiedMembership.setCreatorUuid(session.getMemberUuid());
      copiedMembership.setCreateTimeLong(new Date().getTime());
      copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
      copiedMembership.setHibernateVersionNumber(-1L);
      GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
    }
  }

  /**
   * 
   * @param session
   * @param group
   * @throws SchemaException
   * @throws MemberAddException
   * @throws InsufficientPrivilegeException
   */
  private void internal_copyListMembersOfGroup(GrouperSession session, Group group)
      throws SchemaException, MemberAddException, InsufficientPrivilegeException {
    Set<Field> fields = FieldFinder.findAllByType(FieldType.LIST);
    Iterator<Field> iter = fields.iterator();
    while (iter.hasNext()) {
      Field f = iter.next();
      
      if (group.hasType(f.getGroupType())) {
        PrivilegeHelper.dispatch(session, group, session.getSubject(), f.getReadPriv());
        Iterator<Membership> membershipsIter = GrouperDAOFactory.getFactory().getMembership()
            .findAllByGroupOwnerAndFieldAndType(group.getUuid(), f,
                MembershipType.IMMEDIATE.getTypeString(), false).iterator();

        while (membershipsIter.hasNext()) {
          Membership existingMembership = membershipsIter.next();
          Membership copiedMembership = existingMembership.clone();
          copiedMembership.setOwnerGroupId(this.getUuid());
          copiedMembership.setCreatorUuid(session.getMemberUuid());
          copiedMembership.setCreateTimeLong(new Date().getTime());
          copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
          copiedMembership.setHibernateVersionNumber(-1L);
          GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
        }
      }
    }
  }

  /**
   * 
   * @param session
   * @param group
   * @throws UnableToPerformException
   */
  private void internal_copyGroupAsPrivilege(GrouperSession session, Group group) 
    throws UnableToPerformException {
    Set<Privilege> privileges = Privilege.getAccessPrivs();

    Iterator<Privilege> iter = privileges.iterator();
    while (iter.hasNext()) {
      Privilege priv = iter.next();
      session.getAccessResolver().privilegeCopy(group.toSubject(), this.toSubject(), priv);
    }
    
    privileges = Privilege.getNamingPrivs();
    iter = privileges.iterator();
    while (iter.hasNext()) {
      Privilege priv = iter.next();
      session.getNamingResolver().privilegeCopy(group.toSubject(), this.toSubject(), priv);
    } 
  }

  /**
   * 
   * @param session
   * @param group
   * @throws UnableToPerformException
   */
  private void internal_copyPrivilegesOfGroup(GrouperSession session, Group group)
    throws UnableToPerformException {
    Set<Privilege> privileges = Privilege.getAccessPrivs();

    Iterator<Privilege> iter = privileges.iterator();
    while (iter.hasNext()) {
      Privilege priv = iter.next();
      session.getAccessResolver().privilegeCopy(group, this, priv);      
    }  
  }

  /**
   * Copy this group to another Stem.  If you want to specify options
   * for the copy, use GroupCopy.  This will use the default options.
   * @param stem
   * @return the new group
   * @throws InsufficientPrivilegeException 
   * @throws GroupAddException 
   */
  public Group copy(Stem stem) throws GroupAddException, InsufficientPrivilegeException {
    GroupCopy groupCopy = new GroupCopy(this, stem);
    return groupCopy.save();
  }

  /**
   * type of group, group or role 
   * @return group or role
   */
  public TypeOfGroup getTypeOfGroup() {
    return this.typeOfGroup;
  }

  /**
   * type of group, group or role
   * @param typeOfGroup1
   */
  public void setTypeOfGroup(TypeOfGroup typeOfGroup1) {
    this.typeOfGroup = typeOfGroup1;
  } 

  /**
   * type of group, group or role 
   * @return group or role
   */
  public String getTypeOfGroupDb() {
    return this.typeOfGroup == null ? TypeOfGroup.group.name() : this.typeOfGroup.name();
  }

  /**
   * type of group, group or role
   * @param typeOfGroup1
   */
  public void setTypeOfGroupDb(String typeOfGroup1) {
    this.typeOfGroup = TypeOfGroup.valueOfIgnoreCase(typeOfGroup1, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetElement#__getId()
   */
  public String __getId() {
    return this.getUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetElement#__getName()
   */
  public String __getName() {
    return this.getName();
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.role.Role#getId()
   */
  public String getId() {
    return this.getUuid();
  }

  /**
   * id of the group as a unique integer
   * @return id
   */
  public Long getIdIndex() {
    return this.idIndex;
  }

  /**
   * id of the group as a unique integer
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.role.Role#getStemId()
   */
  public String getStemId() {
    return this.getParentUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.role.Role#setId(java.lang.String)
   */
  public void setId(String id1) {
    this.setUuid(id1);
  }

  /**
   * @see edu.internet2.middleware.grouper.permissions.role.Role#setStemId(java.lang.String)
   */
  public void setStemId(String stemId1) {
    this.setParentUuid(stemId1);
  }

  /**
   * cache this for performance.  delegate calls to this class for role hierarchy stuff
   */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private RoleInheritanceDelegate roleInheritanceDelegate = null;
  
  /**
   * delegate calls to this class for role hierarchy stuff
   * @return the role inheritance hierarchy
   */
  public RoleInheritanceDelegate getRoleInheritanceDelegate() {
    if (this.roleInheritanceDelegate == null) {
      this.roleInheritanceDelegate = new RoleInheritanceDelegate(this);
    }
    return this.roleInheritanceDelegate;
  }
  
  /**
   * cache this for performance.  delegate calls to this class for role hierarchy stuff
   */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private PermissionRoleDelegate permissionRoleDelegate = null;

  /**
   * @see edu.internet2.middleware.grouper.permissions.role.Role#getPermissionRoleDelegate()
   */
  public PermissionRoleDelegate getPermissionRoleDelegate() {
    if (this.permissionRoleDelegate == null) {
      this.permissionRoleDelegate = new PermissionRoleDelegate(this);
    }
    return this.permissionRoleDelegate;
  }

  /**
   * Get memberships of this group, for a certain collection of members, must be enabled
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getNonImmediateMemberships(Field f, Collection<Member> members) 
    throws  SchemaException {
    return getNonImmediateMemberships(f, members, true);
  }

  /**
   * Get memberships of this group, for a certain collection of members
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @param members 
   * @param enabledOnly
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getNonImmediateMemberships(Field f, Collection<Member> members, boolean enabledOnly) 
    throws  SchemaException {
    return PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMembersAndType(this.getUuid(), f, members, 
              MembershipType.NONIMMEDIATE.getTypeString(), enabledOnly)
      );
  }

  /**
   * Get non immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set nonImmediates = g.getNonImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperException
   */
  public Set<Member> getNonImmediateMembers() 
    throws  GrouperException
  {
    try {
      return this.getNonImmediateMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getImmediateMembers()

  /**
   * Get non-immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set nonImmediates = g.getNonImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getNonImmediateMembers(Field f) 
    throws  SchemaException {
    return getNonImmediateMembers(f, null);
  }

  /**
   * Get non-immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set nonImmediates = g.getNonImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getNonImmediateMembers(Field f, QueryOptions queryOptions) 
    throws  SchemaException {
    return getNonImmediateMembers(f, null, queryOptions);
  } // public Set getImmediateMembers(f)

  /**
   * Get non-immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set nonImmediates = g.getNonImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @param sources 
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getNonImmediateMembers(Field f, Set<Source> sources, QueryOptions queryOptions) 
    throws  SchemaException {
    return MemberFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.NONIMMEDIATE.getTypeString(), sources, queryOptions,
      null, null, null
    );
  } // public Set getImmediateMembers(f)

  /**
   * Get nonimmediate memberships of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperException
   */
  public Set<Membership> getNonImmediateMemberships() 
    throws  GrouperException
  {
    try {
      return this.getNonImmediateMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getNonImmediateMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getNonImmediateMemberships(Field f) 
    throws  SchemaException
  {
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    return MembershipFinder.internal_findAllByGroupOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, f, MembershipType.NONIMMEDIATE.getTypeString()
    );
  } // public Set getImmediateMemberships(f)

  /**
   * Check whether the subject is a non immediate member of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperException
   */
  public boolean hasNonImmediateMember(Subject subj) 
    throws  GrouperException
  {
    try {
      return this.hasNonImmediateMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eS);
    }
  } 

  /**
   * Check whether the subject is a non immediate member of this group.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj, f)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasNonImmediateMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, true);
    rv = m.isNonImmediateMember(this, f);
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  /**
   * Get {@link Composite} {@link Member}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * Set members = g.getCompositeMembers();
   * </pre>
   * @param field
   * @param sources 
   * @param queryOptions 
   * @return  A set of {@link Member} objects.
   * @since   1.0
   */
  public Set<Member> getCompositeMembers(Field field, Set<Source> sources, QueryOptions queryOptions) {

    return MemberFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, field, 
      MembershipType.COMPOSITE.getTypeString(), sources, queryOptions, null, null, null
    );
  } // public Set getCompositeMembers()

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Group existingRecord) {
    existingRecord.setAlternateNameDb(this.alternateNameDb);
    existingRecord.setDescriptionDb(this.getDescriptionDb());
    existingRecord.setDisplayExtensionDb(this.getDisplayExtensionDb());
    existingRecord.setDisplayNameDb(this.getDisplayNameDb());
    existingRecord.setExtensionDb(this.getExtensionDb());
    existingRecord.setNameDb(this.getNameDb());
    existingRecord.setParentUuid(this.getParentUuid());
    existingRecord.setTypeOfGroup(this.typeOfGroup);
    existingRecord.setUuid(this.getUuid());
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Group other) {
    if (!StringUtils.equals(this.alternateNameDb, other.alternateNameDb)) {
      return true;
    }
    if (!StringUtils.equals(StringUtils.trimToNull(this.description), StringUtils.trimToNull(other.description))) {
      return true;
    }
    if (!StringUtils.equals(this.displayExtension, other.displayExtension)) {
      return true;
    }
    if (!StringUtils.equals(this.displayName, other.displayName)) {
      return true;
    }
    if (!StringUtils.equals(this.extension, other.extension)) {
      return true;
    }
    if (!StringUtils.equals(this.name, other.name)) {
      return true;
    }
    if (!StringUtils.equals(this.parentUuid, other.parentUuid)) {
      return true;
    }
    if (!GrouperUtil.equals(this.typeOfGroup, other.typeOfGroup)) {
      return true;
    }
    if (!StringUtils.equals(this.uuid, other.uuid)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Group other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (this.createTime != other.createTime) {
      return true;
    }
    if (!StringUtils.equals(this.creatorUUID, other.creatorUUID)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    //dont check this since after the import it changes anyways perhaps, so just leave it
//    if (!GrouperUtil.equals(this.lastMembershipChangeDb, other.lastMembershipChangeDb)) {
//      return true;
//    }
    if (!StringUtils.equals(this.modifierUUID, other.modifierUUID)) {
      return true;
    }
    if (this.modifyTime != other.modifyTime) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public Group xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getGroup().findByUuidOrName(this.uuid, this.name, false);
  }

  /**
   * assign different id index
   * @param theIdIndex
   * @return if it was changed
   */
  public boolean assignIdIndex(final long theIdIndex) {
    
    TableIndex.assertCanAssignIdIndex();

    boolean needsSave = false;
    //ok, if the index is not in use (not, it could be reserved... hmmm)
    Group tempGroup = GrouperDAOFactory.getFactory().getGroup().findByIdIndex(theIdIndex, false);
    if (tempGroup == null) {
      
      this.setIdIndex(theIdIndex);
      needsSave = true;
      
      //do a new session so we don hold on too long
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          //now we might need to increment the index
          TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().findByType(TableIndexType.group);
          if (tableIndex != null && tableIndex.getLastIndexReserved() < theIdIndex) {
            tableIndex.setLastIndexReserved(theIdIndex);
            tableIndex.saveOrUpdate();
          }
          return null;
        }
      });
      
    }
    return needsSave;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Group xmlSaveBusinessProperties(Group existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      Stem parent = this.getParentStem();
      if (this.getTypeOfGroup() == null || this.getTypeOfGroup().equals(TypeOfGroup.group)) {
        existingRecord = parent.internal_addChildGroup(
            GrouperSession.staticGrouperSession(), this.extension, this.displayExtension, this.uuid, this.description, null, null, false, this.typeOfGroup);
      } else if (this.getTypeOfGroup().equals(TypeOfGroup.role)) {
        existingRecord = (Group)parent.internal_addChildRole(this.extension, this.displayExtension, this.uuid);
      } else {
        throw new RuntimeException("Not expecting type of group: " + this.getTypeOfGroup());
      }
      if (this.idIndex != null) {
        existingRecord.assignIdIndex(this.idIndex);
      }
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getGroup().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportGroup xmlToExportGroup(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportGroup xmlExportGroup = new XmlExportGroup();
    xmlExportGroup.setAlternateName(this.getAlternateNameDb());
    xmlExportGroup.setContextId(this.getContextId());
    xmlExportGroup.setCreateTime(GrouperUtil.dateStringValue(this.getCreateTime()));
    xmlExportGroup.setCreatorId(this.getCreatorUuid());
    xmlExportGroup.setDescription(this.getDescription());
    xmlExportGroup.setDisplayExtension(this.getDisplayExtension());
    xmlExportGroup.setDisplayName(this.getDisplayName());
    xmlExportGroup.setExtension(this.getExtension());
    xmlExportGroup.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportGroup.setIdIndex(this.getIdIndex());
    //TODO make string
    xmlExportGroup.setModifierId(this.getModifierUuid());
    xmlExportGroup.setModifierTime(GrouperUtil.dateStringValue(this.getModifyTime()));
    xmlExportGroup.setName(this.getName());
    xmlExportGroup.setParentStem(this.getParentUuid());
    xmlExportGroup.setTypeOfGroup(this.getTypeOfGroupDb());
    xmlExportGroup.setUuid(this.getUuid());
    
     
    return xmlExportGroup;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    return "Group: " + this.uuid + ", " + this.name;
  }
  
 

}
