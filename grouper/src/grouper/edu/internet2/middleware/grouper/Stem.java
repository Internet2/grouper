/**
 * Copyright 2014 Internet2
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.hibernate.type.LongType;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignStemDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.AttributeDefAddException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataBuiltinTypes;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationThread;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeAssignActionDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.pit.PITUtils;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleIfConditionEnum;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.rules.beans.RulesAttributeDefBean;
import edu.internet2.middleware.grouper.rules.beans.RulesGroupBean;
import edu.internet2.middleware.grouper.rules.beans.RulesPrivilegeBean;
import edu.internet2.middleware.grouper.rules.beans.RulesStemBean;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddAlternateStemNameValidator;
import edu.internet2.middleware.grouper.validator.AddAttributeDefNameValidator;
import edu.internet2.middleware.grouper.validator.AddAttributeDefValidator;
import edu.internet2.middleware.grouper.validator.AddGroupValidator;
import edu.internet2.middleware.grouper.validator.AddStemValidator;
import edu.internet2.middleware.grouper.validator.DeleteStemValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NamingValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;
import edu.internet2.middleware.grouper.xml.export.XmlExportStem;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/** 
 * A namespace within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.209 2009-12-15 06:47:06 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class Stem extends GrouperAPI implements GrouperHasContext, Owner, 
    Hib3GrouperVersioned, Comparable<GrouperObject>, XmlImportable<Stem>, AttributeAssignable, GrouperSetElement,
    GrouperObject {

  /**
   * 
   */
  public static final String VALIDATION_STEM_NAME_TOO_LONG_KEY = "stemNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_STEM_DISPLAY_NAME_TOO_LONG_KEY = "stemDisplayNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_STEM_EXTENSION_TOO_LONG_KEY = "stemExtensionTooLong";

  /**
   * 
   */
  public static final String VALIDATION_STEM_DISPLAY_EXTENSION_TOO_LONG_KEY = "stemDisplayExtensionTooLong";

  /**
   * 
   */
  public static final String VALIDATION_STEM_DESCRIPTION_TOO_LONG_KEY = "stemDescriptionTooLong";

  /**
   * get the name of the parent stem
   * @return the name of the parent stem
   */
  public String getParentStemName() {
    return GrouperUtil.parentStemNameFromName(this.getName(), false);
  } 

  /**
   * id is same as uuid
   * @return id
   */
  public String getId() {
    return this.getUuid();
  }
  
  /** table for stems table in the db */
  public static final String TABLE_GROUPER_STEMS = "grouper_stems";

  /** uuid col in db (not used anymore) */
  public static final String COLUMN_UUID = "uuid";
  
  /** col */
  public static final String COLUMN_PARENT_STEM = "parent_stem";

  /** col */
  public static final String COLUMN_NAME = "name";
  
  /** col */
  public static final String COLUMN_DISPLAY_NAME = "display_name";

  /** col */
  public static final String COLUMN_CREATOR_ID = "creator_id";

  /** id col in db */
  public static final String COLUMN_ID = "id";

  /** unique number for this stem */
  public static final String COLUMN_ID_INDEX = "id_index";

  /** id col in db */
  public static final String COLUMN_CREATE_TIME = "create_time";

  /** id col in db */
  public static final String COLUMN_MODIFIER_ID = "modifier_id";

  /** id col in db */
  public static final String COLUMN_MODIFY_TIME = "modify_time";

  /** id col in db */
  public static final String COLUMN_DISPLAY_EXTENSION = "display_extension";

  /** id col in db */
  public static final String COLUMN_EXTENSION = "extension";

  /** id col in db */
  public static final String COLUMN_DESCRIPTION = "description";

  /** an alternate name for this stem */
  public static final String COLUMN_ALTERNATE_NAME = "alternate_name";

  /** column for hibernate version number */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  /**
   * context id column name
   */
  public static final String COLUMN_CONTEXT_ID = "context_id";


  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_UUID = "old_uuid";
 
  /** timestamp of the last membership change for this group */
  public static final String COLUMN_LAST_MEMBERSHIP_CHANGE = "last_membership_change";

  /** param helper */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private ParameterHelper param = new ParameterHelper();


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

  /** constant for field name for: modifierUUID */
  public static final String FIELD_MODIFIER_UUID = "modifierUUID";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: parentUuid */
  public static final String FIELD_PARENT_UUID = "parentUuid";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";
  
  /** constant for field name for: lastMembershipChangeDb */
  public static final String FIELD_LAST_MEMBERSHIP_CHANGE_DB = "lastMembershipChangeDb";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_ID_INDEX, 
      FIELD_MODIFIER_UUID, 
      FIELD_MODIFY_TIME, FIELD_NAME, FIELD_PARENT_UUID, 
      FIELD_UUID, FIELD_LAST_MEMBERSHIP_CHANGE_DB, FIELD_ALTERNATE_NAME_DB);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DB_VERSION, 
      FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID_INDEX, FIELD_MODIFIER_UUID, FIELD_MODIFY_TIME, 
      FIELD_NAME, FIELD_PARENT_UUID, FIELD_UUID, FIELD_LAST_MEMBERSHIP_CHANGE_DB,
      FIELD_ALTERNATE_NAME_DB);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(GrouperObject that) {
    if (that==null) {
      return 1;
    }
    String thisName = StringUtils.defaultString(this.getName());
    String thatName = StringUtils.defaultString(that.getName());
    return thisName.compareTo(thatName);
  }


  /**
   * Search scope: one-level or subtree.
   * @since   1.2.1
   */
  public enum Scope { 
    /** one level (direct children) */
    ONE, 
    
    /** all decendents */
    SUB;
    
    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static Scope valueOfIgnoreCase(String string, boolean exceptionOnNull) {
      return GrouperUtil.enumValueOfIgnoreCase(Scope.class, 
          string, exceptionOnNull);

    }

    
  }; // TODO 20070802 is this the right location?

  /**
   * Hierarchy delimiter.
   */
  public static final String DELIM      = ":";
  /**
   * Default name of root stem.
   */
  public static final String ROOT_NAME  = GrouperConfig.EMPTY_STRING;
  
  
  // PROTECTED CLASS CONSTANTS //
  // TODO 20070419 how can i get rid of this?
  /** root int */
  public static final String ROOT_INT = ":"; // Appease Oracle, et. al.


  // PRIVATE CLASS CONSTANTS //
  /** event log */
  private static final EventLog EL = new EventLog();

  // PRIVATE INSTANCE VARIABLES //
  /** creator of stem */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Subject creator;
  
  /** modifier of stem */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Subject modifier;

  /** */
  private long    createTime;
  /** */
  private String  creatorUUID;

  /** */
  private String  description;
  /** */
  private String  displayExtension;
  /** */
  private String  displayName;
  /** */
  private String  extension;
  /** */
  private String  modifierUUID;
  /** */
  private long    modifyTime;
  /** */
  private String  name;
  /** */
  private String  parentUuid;
  /** */
  private String  uuid;
  
  /** alternate name of stem */
  private String alternateNameDb;

  /** context id of the transaction */
  private String contextId;

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignStemDelegate attributeAssignStemDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignStemDelegate getAttributeDelegate() {
    if (this.attributeAssignStemDelegate == null) {
      this.attributeAssignStemDelegate = new AttributeAssignStemDelegate(this);
    }
    return this.attributeAssignStemDelegate;
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


  // PUBLIC INSTANCE METHODS //

  // PUBLIC INSTANCE METHODS //
  
  /**
   * Add a new group to the registry.
   * <pre class="eg">
   * // Add a group with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildGroup("edu", "edu domain");
   * }
   * catch (GroupAddException eGA) {
   *   // Group not added
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add group
   * }
   * </pre>
   * @param   extension         Group's extension
   * @param   displayExtension  Groups' displayExtension
   * @return  The added {@link Group}
   * @throws  GroupAddException 
   * @throws  InsufficientPrivilegeException
   */
  public Group addChildGroup(String extension, String displayExtension) 
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    return this.internal_addChildGroup(extension, displayExtension, null);
  } // public Group addChildGroup(extension, displayExtension)

  /**
   * Add a new attribute def to the registry.
   * @param   extension attributeDef's extension
   * @param attributeDefType 
   * @return  The added {@link AttributeDef}
   * @throws  InsufficientPrivilegeException
   */
  public AttributeDef addChildAttributeDef(String extension, 
      AttributeDefType attributeDefType) 
    throws InsufficientPrivilegeException {
    return this.internal_addChildAttributeDef(GrouperSession.staticGrouperSession(true), 
        extension, null, attributeDefType, null);
  }

  /**
   * Add a new attribute def to the registry.
   * @param attributeDef is the definition of this attribute
   * @param   extension attributeDef's extension
   * @param displayExtension 
   * @return  The added {@link AttributeDef}
   * @throws  InsufficientPrivilegeException
   */
  public AttributeDefName addChildAttributeDefName(AttributeDef attributeDef, String extension, String displayExtension) 
    throws InsufficientPrivilegeException {
    return this.internal_addChildAttributeDefName(GrouperSession.staticGrouperSession(true), attributeDef,
        extension, displayExtension, null, null);
  }

  /**
   * Add a new attribute def to the registry.
   * @param attributeDef is the definition of this attribute
   * @param   extension attributeDef's extension
   * @param displayExtension 
   * @param uuid 
   * @return  The added {@link AttributeDef}
   * @throws  InsufficientPrivilegeException
   */
  public AttributeDefName addChildAttributeDefName(AttributeDef attributeDef, String extension, String displayExtension, String uuid) 
    throws InsufficientPrivilegeException {
    return this.internal_addChildAttributeDefName(GrouperSession.staticGrouperSession(true), attributeDef,
        extension, displayExtension, StringUtils.trimToNull(uuid), null);
  }

  /**
   * Add a new stem to the registry.
   * <pre class="eg">
   * // Add a stem with the extension "edu" beneath this stem.
   * try {
   *   Stem edu = ns.addChildStem("edu", "edu domain");
   * }
   * catch (StemAddException e) {
   *   // Stem not added
   * }
   * </pre>
   * @param   extension         Stem's extension
   * @param   displayExtension  Stem' displayExtension
   * @param uuid if creating this is the uuid
   * @param failIfExists throws StemAddException if exists, else just return the existing stem
   * @return  The added {@link Stem}
   * @throws  InsufficientPrivilegeException
   * @throws  StemAddException 
   */
  public Stem addChildStem(String extension, String displayExtension, String uuid, boolean failIfExists) 
    throws  InsufficientPrivilegeException,
            StemAddException {
    return internal_addChildStem(GrouperSession.staticGrouperSession(), extension, displayExtension, uuid, true, failIfExists);
  } 
  
  /**
   * Add a new stem to the registry.
   * <pre class="eg">
   * // Add a stem with the extension "edu" beneath this stem.
   * try {
   *   Stem edu = ns.addChildStem("edu", "edu domain");
   * }
   * catch (StemAddException e) {
   *   // Stem not added
   * }
   * </pre>
   * @param   extension         Stem's extension
   * @param   displayExtension  Stem' displayExtension
   * @return  The added {@link Stem}
   * @throws  InsufficientPrivilegeException
   * @throws  StemAddException 
   */
  public Stem addChildStem(String extension, String displayExtension) 
    throws  InsufficientPrivilegeException,
            StemAddException {
    return internal_addChildStem(extension, displayExtension, null);
  } 

  /**
   * keep track of if we are in a delete so hooks can 
   */
  private static ThreadLocal<Boolean> threadLocalInStemDelete = new InheritableThreadLocal<Boolean>();
  
  /**
   * see if we are in the middle of a delete (e.g. for hook)
   * @return true if delete is occurring
   */
  public static boolean deleteOccuring() {
    Boolean deleteOccuring = threadLocalInStemDelete.get();
    if (deleteOccuring != null) {
      return deleteOccuring;
    }
    return false;
  }

  /**
   * Delete this stem from the Groups Registry.
   * <pre class="eg">
   * try {
   *   ns.delete();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete stem
   * }
   * catch (StemDeleteException eSD) {
   *   // unable to delete stem
   * }
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  StemDeleteException
   */
  public void delete() throws InsufficientPrivilegeException, StemDeleteException {
    
    //this is no longer recently created
    stemCreatedCache().remove(this.name);
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            StopWatch sw = new StopWatch();
            sw.start();
            GrouperSession.validate(GrouperSession.staticGrouperSession());
            if ( !PrivilegeHelper.canStemAdmin( Stem.this, GrouperSession.staticGrouperSession().getSubject() ) ) {
              throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN + ", " + Stem.this.getName());
            }
            DeleteStemValidator v = DeleteStemValidator.validate(Stem.this);
            if (v.isInvalid()) {
              throw new StemDeleteException( v.getErrorMessage() );
            }
            try {
              threadLocalInStemDelete.set(true);
              String name = Stem.this.getName(); // Preserve name for logging
              Stem.this._revokeAllNamingPrivs();
              
              //delete any attributes on this stem, this is done as root
              GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
                
                public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                  //delete any attributes on this stem
                  Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByOwnerStemId(Stem.this.getUuid());
                  
                  for (AttributeAssign attributeAssign : attributeAssigns) {
                    attributeAssign.delete();
                  }
                  return null;
                }
              });

              GrouperDAOFactory.getFactory().getStem().delete( Stem.this );
              
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_DELETE, "id", 
                    Stem.this.getUuid(), "name", Stem.this.getName(), "parentStemId", Stem.this.getUuid(), "displayName", 
                    Stem.this.getDisplayName(), "description", Stem.this.getDescription());
                auditEntry.setDescription("Deleted stem: " +Stem.this.getName());
                auditEntry.saveOrUpdate(true);
              }
              
              sw.stop();
              EventLog.info(GrouperSession.staticGrouperSession(), M.STEM_DEL + Quote.single(name), sw);
            }
            catch (GrouperDAOException eDAO)      {
              throw new StemDeleteException( eDAO.getMessage() + ", " + Stem.this.getName(), eDAO );
            }
            catch (RevokePrivilegeException eRP)  {
              throw new StemDeleteException(eRP.getMessage() + ", " + Stem.this.getName(), eRP);
            }
            catch (SchemaException eS)            {
              throw new StemDeleteException(eS.getMessage() + ", " + Stem.this.getName(), eS);
            } finally {
              threadLocalInStemDelete.remove();
            }
            return null;
         }
    });

    
  }

  /**
   * Get groups that are immediate children of this stem.
   * @return  Set of {@link Group} objects.
   * @see     Stem#getChildGroups(Scope)
   */
  public Set getChildGroups() {
    return this.getChildGroups(Scope.ONE);
  }

  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Scope scope) 
    throws  IllegalArgumentException {
    return getChildGroups(scope, AccessPrivilege.VIEW_PRIVILEGES, null);
  }

  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param inPrivSet set of privileges that the grouper session needs one of for the row to be returned.
   * AccessPrivilege has some pre-baked constant sets for use here
   * @param queryOptions 
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Scope scope, Set<Privilege> inPrivSet, QueryOptions queryOptions) {
    return this.getChildGroups(scope, inPrivSet, queryOptions, null);
  }

  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param inPrivSet set of privileges that the grouper session needs one of for the row to be returned.
   * AccessPrivilege has some pre-baked constant sets for use here
   * @param typeOfGroups is the type of groups to get, or null for all
   * @param queryOptions 
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Scope scope, Set<Privilege> inPrivSet, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) 
    throws  IllegalArgumentException {
    return this.getChildGroups(scope, inPrivSet, queryOptions, typeOfGroups, true);
  }
  
  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param inPrivSet set of privileges that the grouper session needs one of for the row to be returned.
   * AccessPrivilege has some pre-baked constant sets for use here
   * @param typeOfGroups is the type of groups to get, or null for all
   * @param queryOptions 
   * @param enabled true if enabled only, false if disabled only, null if everything
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Scope scope, Set<Privilege> inPrivSet, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups, Boolean enabled) 
    throws  IllegalArgumentException {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    this.param.notNullPrivilegeSet(inPrivSet);

    inPrivSet = AccessPrivilege.filter(inPrivSet);
    
    if (inPrivSet.size() == 0) {
      return new LinkedHashSet<Group>();
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject     subj    = grouperSession.getSubject();
    Set<Group> findAllChildGroups = 
      GrouperDAOFactory.getFactory().getStem().findAllChildGroupsSecure( this, scope, 
          grouperSession, subj, inPrivSet, queryOptions, typeOfGroups, enabled );
    return findAllChildGroups;
  }

  /**
   * Get groups that are children of this stem and there is a list membership.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param inPrivSet set of privileges that the grouper session needs one of for the row to be returned.
   * AccessPrivilege has some pre-baked constant sets for use here
   * @param queryOptions 
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildMembershipGroups(Scope scope, Set<Privilege> inPrivSet, QueryOptions queryOptions) 
    throws  IllegalArgumentException {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    this.param.notNullPrivilegeSet(inPrivSet);
      
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject     subj    = grouperSession.getSubject();
    Set<Group> findAllChildGroups = 
      GrouperDAOFactory.getFactory().getStem().findAllChildMembershipGroupsSecure( this, scope, 
          grouperSession, subj, inPrivSet, queryOptions );
    return findAllChildGroups;
      }

  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param inPrivSet set of privileges that the grouper session needs one of for the row to be returned.
   * AccessPrivilege has some pre-baked constant sets for use here
   * @param queryOptions 
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Scope scope, Set<Privilege> inPrivSet, QueryOptions queryOptions) 
    throws  IllegalArgumentException {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    this.param.notNullPrivilegeSet(inPrivSet);

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject     subj    = grouperSession.getSubject();
    Set<Stem> findAllChildStems = 
      GrouperDAOFactory.getFactory().getStem().findAllChildStemsSecure( this, scope, 
          grouperSession, subj, inPrivSet, queryOptions );
    return findAllChildStems;
  }

  /**
   * get child groups
   * @param privileges privs 
   * @param scope all or direct
   * @return  Child groups where current subject has any of the specified <i>privileges</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   * @deprecated use the overload
   */
  @Deprecated
  public Set<Group> getChildGroups(Privilege[] privileges, Scope scope)
      throws  IllegalArgumentException {

    return getChildGroups(scope, GrouperUtil.toSet(privileges), null);
    }

  /**
   * Get stems that are immediate children of this stem.
   * @return  Set of {@link Stem} objects.
   * @see     Stem#getChildStems(Scope)
   */
  public Set<Stem> getChildStems() {
    return this.getChildStems(Scope.ONE);
  }

  /**
   * Get stems that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @return  Child stems.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Scope scope) {
    return getChildStems(scope, null);
  }

  /**
   * Get stems that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @param queryOptions 
   * @return  Child stems.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Scope scope, QueryOptions queryOptions) 
    throws  IllegalArgumentException
  {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    Set<Stem> stems = new LinkedHashSet();
    for ( Stem child : GrouperDAOFactory.getFactory().getStem().findAllChildStems( this, scope, queryOptions ) ) {
      stems.add(child);
    }
    return stems;
  }

  /**
   * get child stems
   * @param privileges privs
   * @param scope all or direct
   * @return  Child (or deeper) stems where current subject has any of the specified <i>privileges</i>.  Parent stems of grandchild (or deeper) groups where the current subject has any of the specified <i>privileges</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Privilege[] privileges, Scope scope)
    throws  IllegalArgumentException 
  {
    this.param.notNullPrivilegeArray(privileges); 

    Set<Stem> stems = new LinkedHashSet();
    // TODO 20070824 this could be a lot prettier
    for ( Stem stem : this.getChildStems(scope) ) {

      for ( Privilege priv : PrivilegeHelper.getNamingPrivileges(privileges) ) {
        try {
          PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), stem, 
              GrouperSession.staticGrouperSession().getSubject(), priv );
          stems.add(stem);
          break; // we only care that one privilege matches
        }
        catch (InsufficientPrivilegeException eIP) {
          // ignore
        }
        catch (SchemaException eSchema) {
          // ignore
        }
      }

      if ( !stems.contains(stem) ) { // no matching naming privileges so checking access privilegees
        // filtering out naming privileges will happen in "#getChildGroups(Privilege[], Scope)"
        for ( Group group : stem.getChildGroups(privileges, scope) ) {
          stems.add( group.getParentStem() );
        }
      }

    }
    return stems;
  }

  /**
   * Get subject that created this stem.
   * <pre class="eg">
   * // Get creator of this stem.
   * try {
   *   Subject creator = ns.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this stem.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws  SubjectNotFoundException
  {
    if (this.creator == null) {
      String subjectId = null;
      try {
        Member member = MemberFinder.findByUuid( GrouperSession.staticGrouperSession(), 
            this.getCreatorUuid(), true );
        subjectId = member.getSubjectId();
        this.creator = member.getSubject();
      }
      catch (MemberNotFoundException eMNF) {
        throw new SubjectNotFoundException( subjectId, eMNF.getMessage(), eMNF );
      }
    }
    return this.creator; 
  } // public Subject getCreateSubject()
  
  /**
   * Get creation time for this stem.
   * <pre class="eg">
   * // Get create time.
   * Date created = ns.getCreateTime();
   * </pre>
   * @return  {@link Date} that this stem was created.
   */
  public Date getCreateTime() {
    return new Date( this.getCreateTimeLong() );
  } // public Date getCreateTime()

  /**
   * Get subjects with CREATE privilege on this stem.
   * <pre class="eg">
   * Set creators = ns.getCreators();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperException
   */
  public Set getCreators() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.CREATE);
  }

  /**
   * Get stem description.
   * <pre class="eg">
   * // Get description
   * String description = ns.getDescription();
   * </pre>
   * @return  Stem description.
   */
  public String getDescription() {
    String desc = this.description;
    if (desc == null) {
      desc = GrouperConfig.EMPTY_STRING;
    }
    return desc;
  } 
 
  /**
   * Get stem displayExtension.
   * <pre class="eg">
   * // Get displayExtension
   * String displayExtn = ns.getDisplayExtension();
   * </pre>
   * @return  Stem displayExtension.
   */
  public String getDisplayExtension() {
    String val = this.getDisplayExtensionDb();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
    }
    return val;
  }
 
  /**
   * Get stem displayName.
   * <pre class="eg">
   * // Get displayName
   * String displayName = ns.getDisplayName();
   * </pre>
   * @return  Stem displayName.
   */
  public String getDisplayName() {
    String val = this.getDisplayNameDb();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
    }
    return val;
  }
 
  /**
   * Get stem extension.
   * <pre class="eg">
   * // Get extension
   * String extension = ns.getExtension();
   * </pre>
   * @return  Stem extension.
   */
  public String getExtension() {
    String val = this.getExtensionDb();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
    }
    return val;
  }
 
  /**
   * Get subject that last modified this stem.
   * <pre class="eg">
   * // Get last modifier of this stem.
   * try {
   *   Subject modifier = ns.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this stem.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws  SubjectNotFoundException
  {
    if (this.modifier == null) {
      if ( this.getModifierUuid() == null) {
        throw new SubjectNotFoundException("stem has not been modified");
      }
      try {
        this.modifier = MemberFinder.findByUuid( GrouperSession.staticGrouperSession(), 
            this.getModifierUuid() , true).getSubject();
      }
      catch (MemberNotFoundException eMNF) {
        throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
      }
    }
    return this.modifier; 
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this stem.
   * <pre class="eg">
   * // Get last modified time.
   * Date modified = ns.getModifyTime();
   * </pre>
   * @return  {@link Date} that this stem was last modified.
   */
  public Date getModifyTime() {
    return new Date( this.getModifyTimeLong() );
  } // public Date getModifyTime()

  /**
   * Get stem name.
   * <pre class="eg">
   * // Get name
   * String name = ns.getName();
   * </pre>
   * @return  Stem name.
   */ 
  public String getName() {
    String val = this.getNameDb();
    if (StringUtils.equals(ROOT_INT, val)) {
      return ROOT_NAME;
    }
    return val;
  }

  /**
   * Get parent stem.
   * <pre class="eg">
   * // Get parent
   * Stem parent = ns.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   * @throws StemNotFoundException if stem not found
   */
  public Stem getParentStem() 
    throws StemNotFoundException
  {
    String uuid = this.getParentUuid();
    if (uuid == null) {
      throw new StemNotFoundException();
    }
    Stem parent = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid, true);
    return parent;
  } // public Stem getParentStem()
  
  /**
   * Get parent stem.
   * <pre class="eg">
   * // Get parent
   * Stem parent = ns.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   * @throws StemNotFoundException if stem not found
   */
  public Stem getParentStemOrNull() 
  {
    String theParentUuid = this.getParentUuid();
    if (theParentUuid == null) {
      return null;
    }
    Stem parent = GrouperDAOFactory.getFactory().getStem().findByUuid(theParentUuid, true);
    return parent;
  }
  
  /**
   * Returns the alternate name for the stem.  Used by hibernate.
   * @return the alternate name
   */
  public String getAlternateNameDb() {
    return this.alternateNameDb;
  }
  
  /**
   * Returns the alternate name for the stem.  If multiple, returns the first one
   * @return the alternate name
   */
  public String getAlternateName() {
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
   * Returns the alternate names for the stem.  Only one alternate name is supported
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
   * Add an alternate name for this stem.  Only one alternate name is supported
   * currently, so this will replace any existing alternate name.
   * This won't get saved until you call store().
   * @param alternateName
   */
  public void addAlternateName(String alternateName) {
    
    if (!PrivilegeHelper.canStemAdmin(this, GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN);
    }
    
    // verify that if the property security.stem.groupAllowedToRenameStem is set,
    // then the user is a member of that group.
    if (!PrivilegeHelper.canRenameStems(GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException("User cannot rename stems.");      
    }
    
    // verify name
    GrouperValidator v = AddAlternateStemNameValidator.validate(alternateName);
    if (v.isInvalid()) {
      throw new StemModifyException(v.getErrorMessage() + ": " + alternateName);
    }
    
    // Checking stem privilege on the parent stem if the alternate name is for another stem
    String parentStemName = GrouperUtil.parentStemNameFromName(alternateName);
    if (GrouperUtil.isEmpty(parentStemName)) {
      parentStemName = Stem.ROOT_NAME;
    }
    
    if (this.isRootStem() || !parentStemName.equals(this.getParentStem().getName())) {
      Stem stem = GrouperUtil.getFirstParentStemOfName(alternateName);

      if (!stem.hasStem(GrouperSession.staticGrouperSession().getSubject())) {
        throw new InsufficientPrivilegeException(E.CANNOT_STEM);
      }
    }
    
    internal_addAlternateName(alternateName);
  }
  
  /**
   * Add an alternate name for this stem.  Only one alternate name is supported
   * currently, so this will replace any existing alternate name.
   * This won't get saved until you call store().
   * @param alternateName
   */
  protected void internal_addAlternateName(String alternateName) {

    this.alternateNameDb = alternateName;
  }
  
  /**
   * Delete the specified alternate name.  This won't get saved until you call store().
   * @param alternateName
   * @return false if the stem does not have the specified alternate name
   */
  public boolean deleteAlternateName(String alternateName) {
    
    if (!PrivilegeHelper.canStemAdmin(this, GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN);
    }
    
    // verify that if the property security.stem.groupAllowedToRenameStem is set,
    // then the user is a member of that group.
    if (!PrivilegeHelper.canRenameStems(GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException("User cannot rename stems.");      
    }
    
    if (alternateName.equals(this.alternateNameDb)) {
      this.alternateNameDb = null;
      return true;
    }
    
    return false;
  }

  /**
   * Get privileges that the specified subject has on this stem.
   * <pre class="eg">
   * Set privs = ns.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link NamingPrivilege} objects.
   */
  public Set<NamingPrivilege> getPrivs(Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().getPrivileges(this, subj);
  } 

  /**
   * Get subjects with STEM privilege on this stem.
   * <pre class="eg">
   * Set stemmers = ns.getStemmers();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperException
   */
  public Set getStemmers() 
    throws  GrouperException
  {
    return getStemAdmins();
  } 
  
  /**
   * Get subjects with STEM_ADMIN privilege on this stem.
   * <pre class="eg">
   * Set stemAdmins = ns.getStemAdmins();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperException
   */
  public Set getStemAdmins() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.STEM_ADMIN);
  } 
  
  /**
   * Get subjects with STEM_ATTR_READ privilege on this stem.
   * <pre class="eg">
   * Set subjects = ns.getStemAttrReaders();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperException
   */
  public Set getStemAttrReaders() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.STEM_ATTR_READ);
  } 
  
  /**
   * Get subjects with STEM_ATTR_UPDATE privilege on this stem.
   * <pre class="eg">
   * Set subjects = ns.getStemAttrUpdaters();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperException
   */
  public Set getStemAttrUpdaters() 
    throws  GrouperException
  {
    return GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.STEM_ATTR_UPDATE);
  } 

  /**
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  } // public String getUuid()

  /**
   * Grant a privilege on this stem.
   * <pre class="eg">
   * try {
   *   ns.grantPriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (GrantPrivilegeException e) {
   *   // Error granting privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,        // TODO 20070820 stop throwing
            InsufficientPrivilegeException, // TODO 20070820 stop throwing
            SchemaException                 // TODO 20070820 stop throwing
  {
    grantPriv(subj, priv, true);
    
  }
  
  /**
   * Grant a privilege on this stem.
   * <pre class="eg">
   * try {
   *   ns.grantPriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (GrantPrivilegeException e) {
   *   // Error granting privilege
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
   * Grant a privilege on this stem.
   * <pre class="eg">
   * try {
   *   ns.grantPriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (GrantPrivilegeException e) {
   *   // Error granting privilege
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
    
    final String errorMessageSuffix = ", stem name: " + this.name 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());

    return (Boolean)HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

    
          boolean didNotExist = true;
          try {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperSession.staticGrouperSession().getNamingResolver().grantPrivilege(Stem.this, subj, priv, uuid);
            
            RulesPrivilegeBean rulesPrivilegeBean = new RulesPrivilegeBean(Stem.this, subj, priv);
            
            //fire rules related to subject assign in folder
            RuleEngine.fireRule(RuleCheckType.subjectAssignInStem, rulesPrivilegeBean);
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              
              Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false);
              
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_STEM_ADD, "privilegeName", 
                  priv.getName(),  "memberId",  member.getUuid(),
                      "privilegeType", "naming", 
                      "stemId", Stem.this.getUuid(), "stemName", Stem.this.getName());
                      
              auditEntry.setDescription("Added privilege: stem: " + Stem.this.getName()
                  + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", privilege: "
                  + priv.getName());
              auditEntry.saveOrUpdate(true);
            }

            
          } catch (UnableToPerformAlreadyExistsException eUTP) {
            if (exceptionIfAlreadyMember) {
              throw new GrantPrivilegeAlreadyExistsException( eUTP.getMessage() + errorMessageSuffix, eUTP );
            }
            didNotExist = false;
          }
          catch (UnableToPerformException eUTP) {
            throw new GrantPrivilegeException( eUTP.getMessage() + errorMessageSuffix, eUTP );
          }
          sw.stop();
          if (didNotExist) {
            EL.stemGrantPriv(GrouperSession.staticGrouperSession(), Stem.this.getName(), subj, priv, sw);
          }
          return didNotExist;
        }
      });
  } 

  /**
   * Check whether a subject has the CREATE privilege on this stem.
   * <pre class="eg">
   * if (ns.hasCreate(subj)) {
   *   // Has CREATE
   * }
   *   // Does not have CREATE
   * } 
   * </pre>
   * @param   subj  Check whether this subject has CREATE.
   * @return  Boolean true if the subject has CREATE.
   */
  public boolean hasCreate(Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.CREATE);
  } 
  
  /**
   * Check whether a subject has the STEM_ATTR_READ privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStemAttrRead(subj)) {
   *   // Has STEM_ATTR_READ
   * }
   *   // Does not have STEM_ATTR_READ
   * } 
   * </pre>
   * @param   subj  Check whether this subject has STEM_ATTR_READ.
   * @return  Boolean true if the subject has STEM_ATTR_READ.
   */
  public boolean hasStemAttrRead(Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.STEM_ATTR_READ);
  }
  
  /**
   * Check whether a subject has the STEM_ATTR_UPDATE privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStemAttrUpdate(subj)) {
   *   // Has STEM_ATTR_UPDATE
   * }
   *   // Does not have STEM_ATTR_UPDATE
   * } 
   * </pre>
   * @param   subj  Check whether this subject has STEM_ATTR_UPDATE.
   * @return  Boolean true if the subject has STEM_ATTR_UPDATE.
   */
  public boolean hasStemAttrUpdate(Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.STEM_ATTR_UPDATE);
  }
 
  /**
   * Check whether a member has the STEM privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStem(subj)) {
   *   // Has STEM
   * }
   *   // Does not have STEM
   * } 
   * </pre>
   * @param   subj  check whether this subject has STEM.
   * @return  Boolean true if the subject has STEM.
   */
  public boolean hasStem(Subject subj) {
    return hasStemAdmin(subj);
  } 
  
  /**
   * Check whether a member has the STEM_ADMIN privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStemAdmin(subj)) {
   *   // Has STEM_ADMIN
   * }
   *   // Does not have STEM_ADMIN
   * } 
   * </pre>
   * @param   subj  check whether this subject has STEM_ADMIN.
   * @return  Boolean true if the subject has STEM_ADMIN.
   */
  public boolean hasStemAdmin(Subject subj) {
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.STEM_ADMIN);
  } 
 
  /**
   * see if the subject has a privilege
   * @param subject
   * @param privilegeOrListName
   * @return true if has privilege
   */
  public boolean hasPrivilege(Subject subject, String privilegeOrListName) {
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM.getListName())
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ADMIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ADMIN.getListName())) {
      return this.hasStemAdmin(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.CREATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.CREATE.getListName())) {
      return this.hasCreate(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_READ.getListName())) {
      return this.hasStemAttrRead(subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_UPDATE.getListName())) {
      return this.hasStemAttrUpdate(subject);
    }
    throw new RuntimeException("Cant find privilege: '" + privilegeOrListName + "'");

  }

  /**
   * TODO 20070813 make public?
   * @param group group
   * @return  True if <i>group</i> is child, at any depth, of this stem.
   * @throws  IllegalArgumentException if <i>group</i> is null.
   * @since   1.2.1
   */
  public boolean isChildGroup(Group group)
    throws  IllegalArgumentException
  {
    if (group == null) { // TODO 20070813 ParameterHelper
      throw new IllegalArgumentException("null Group");
    }

    if (this.isRootStem()) {
      return true;
    } 

    String stemName = this.getName();
    String groupName = group.getName();

    if (groupName.length() <= (stemName.length() + DELIM.length())) {
      return false;
    }
    
    if ((stemName + DELIM).equals(groupName.substring(0, stemName.length() + DELIM.length()))) {
      return true;
    }

    return false;
  }

  /**
   * TODO 20070813 make public?
   * @param stem stem
   * @return  True if <i>stem</i> is child, at any depth, of this stem.
   * @throws  IllegalArgumentException if <i>stem</i> is null.
   * @since   1.2.1
   */
  public boolean isChildStem(Stem stem) 
    throws  IllegalArgumentException
  {
    if (stem == null) { // TODO 20070813 ParameterHelper
      throw new IllegalArgumentException("null Stem");
    }

    String thisName = this.getName();
    String stemName = stem.getName();

    if (
         ( thisName.equals( stemName ) )  // can't be child of self
         ||
         stem.isRootStem()                            // root stem can't be child
       )
    {
      return false;
    }
    if ( this.isRootStem() ) {
      return true; // all stems are children
    }

    if (stemName.length() <= (thisName.length() + DELIM.length())) {
      return false;
    }
    
    if ((thisName + DELIM).equals(stemName.substring(0, thisName.length() + DELIM.length()))) {
      return true;
    }

    return false;
  }

  /**
   * @return  Boolean true if this is the root stem of the Groups Registry.
   * @since   1.2.0
   */
  public boolean isRootStem() {
    return ROOT_INT.equals( this.getNameDb() );
  } 

  /**
   * Revoke all privileges of the specified type on this stem.
   * <pre class="eg">
   * try {
   *   ns.revokePriv(NamingPrivilege.CREATE);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   priv  Revoke this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Privilege priv) 
    throws  InsufficientPrivilegeException, // TODO 20070820 stop throwing this
            RevokePrivilegeException,
            SchemaException                 // TODO 20070820 stop throwing this
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !Privilege.isNaming(priv) ) {
      throw new SchemaException("attempt to use not naming privilege");
    }
    try {
      GrouperSession.staticGrouperSession().getNamingResolver().revokePrivilege(this, priv);
    }
    catch (UnableToPerformException e) {
      throw new RevokePrivilegeException( e.getMessage(), e );
    }
    sw.stop();
    EL.stemRevokePriv(GrouperSession.staticGrouperSession(), this.getName(), priv, sw);
  }
 
  /**
   * Revoke a privilege on this stem.
   * <pre class="eg">
   * try {
   *   ns.revokePriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (InsufficientPrivilegeException eIP) {
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
    throws  InsufficientPrivilegeException, // TODO 20070820 stop throwing this
            RevokePrivilegeException,
            SchemaException                 // TODO 20070820 stop throwing this
  {
    revokePriv(subj, priv, true);
  }

  /**
   * Revoke a privilege on this stem.
   * <pre class="eg">
   * try {
   *   ns.revokePriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (InsufficientPrivilegeException eIP) {
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
  public boolean revokePriv(final Subject subj, final Privilege priv, final boolean exceptionIfAlreadyRevoked)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException {

    final String errorMessageSuffix = ", stem name: " + this.name 
      + ", subject: " + GrouperUtil.subjectToString(subj) + ", privilege: " + (priv == null ? null : priv.getName());

    return (Boolean)HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

          boolean wasntAlreadyRevoked = true;
          StopWatch sw = new StopWatch();
          sw.start();
          if (!Privilege.isNaming(priv) ) {
            throw new SchemaException("attempt to use non-naming privilege: " + errorMessageSuffix);
          }
          try {
            GrouperSession.staticGrouperSession().getNamingResolver().revokePrivilege(Stem.this, subj, priv);
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              
              Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false);
              
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_STEM_DELETE, "privilegeName", 
                  priv.getName(),  "memberId",  member.getUuid(),
                      "privilegeType", "naming", 
                      "stemId", Stem.this.getUuid(), "stemName", Stem.this.getName());
                      
              auditEntry.setDescription("Deleted privilege: stem: " + Stem.this.getName()
                  + ", subject: " + subj.getSource().getId() + "." + subj.getId() + ", privilege: "
                  + priv.getName());
              auditEntry.saveOrUpdate(true);
            }

            
          } catch (UnableToPerformAlreadyExistsException eUTP) {
            if (exceptionIfAlreadyRevoked) {
              throw new RevokePrivilegeAlreadyRevokedException( eUTP.getMessage() + errorMessageSuffix, eUTP );
            }
            wasntAlreadyRevoked = false;
          } catch (UnableToPerformException e) {
            throw new RevokePrivilegeException( e.getMessage() + errorMessageSuffix, e );
          }
          sw.stop();
          if (wasntAlreadyRevoked) {
            EL.stemRevokePriv(GrouperSession.staticGrouperSession(), Stem.this.getName(), subj, priv, sw);
          }
          return wasntAlreadyRevoked;
        }
      });
  } 

  /**
   * Set stem description.
   * <pre class="eg">
   * // Set description
   * try {
   *  ns.setDescription(value);
   * }
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to set description
   * catch (StemModifyException e1) {
   *   // Error setting description
   * }
   * </pre>
   * @param   value   Set description to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setDescription(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !PrivilegeHelper.canStemAdmin( this, GrouperSession.staticGrouperSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN);
    }
    try {
      this.setDescriptionDb(value);
      this.internal_setModified();
      sw.stop();
      EL.stemSetAttr(GrouperSession.staticGrouperSession(), this.getName(), "description", value, sw);
      
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set description: " + eDAO.getMessage(), eDAO );
    }
  }

  /**
   * will be implemented soon
   */
  public void store() {

    validate();

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            String differences = GrouperUtil.dbVersionDescribeDifferences(Stem.this.dbVersion(), 
                Stem.this, Stem.this.dbVersion() != null ? Stem.this.dbVersionDifferentFields() : Stem.CLONE_FIELDS);

            try {
              GrouperDAOFactory.getFactory().getStem().update( Stem.this );
            }
            catch (GrouperDAOException e) {
              String error = "Problem with hib update: " + GrouperUtil.toStringSafe(Stem.this)
               + ",\n" + e.getMessage();
              GrouperUtil.injectInException(e, error);
              throw e;
            }
          
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_UPDATE, "id", 
                  Stem.this.getUuid(), "name", Stem.this.getName(), "parentStemId", Stem.this.getParentUuid(), "displayName", 
                  Stem.this.getDisplayName(), "description", Stem.this.getDescription());
              auditEntry.setDescription("Updated stem: " + Stem.this.getName() + ", " + differences);
              auditEntry.saveOrUpdate(true);
            }
            return null;
          }
        });
          

    
  }

  /**
   * 
   */
  public void validate() {
    //lets validate
    
    int maxNameLength = 255;
    maxNameLength = GrouperConfig.retrieveConfig().propertyValueInt("grouper.stemName.maxSize", maxNameLength);

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "extension", 
    //        Types.VARCHAR, "255", false, true);
    if (GrouperUtil.lengthAscii(this.getExtension()) > 255 ) {
      throw new GrouperValidationException("Stem extension too long: " + GrouperUtil.lengthAscii(this.getExtension()), 
          VALIDATION_STEM_EXTENSION_TOO_LONG_KEY, 255, GrouperUtil.lengthAscii(this.getExtension()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_extension", 
    //        Types.VARCHAR, "255", false, true);
    if (GrouperUtil.lengthAscii(this.getDisplayExtension()) > 255 ) {
      throw new GrouperValidationException("Stem display extension too long: " + GrouperUtil.lengthAscii(this.getDisplayExtension()), 
          VALIDATION_STEM_DISPLAY_EXTENSION_TOO_LONG_KEY, 255, GrouperUtil.lengthAscii(this.getDisplayName()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "name", 
    //        Types.VARCHAR, "255", false, true);
    if (GrouperUtil.lengthAscii(this.getName()) > maxNameLength) {
      throw new GrouperValidationException("Stem name too long: " + GrouperUtil.lengthAscii(this.getName()), 
          VALIDATION_STEM_NAME_TOO_LONG_KEY, 255, GrouperUtil.lengthAscii(this.getName()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "display_name", 
    //        Types.VARCHAR, "255", false, true);
    if (GrouperUtil.lengthAscii(this.getDisplayName()) > maxNameLength) {
      throw new GrouperValidationException("Stem display name too long: " + GrouperUtil.lengthAscii(this.getDisplayName()), 
          VALIDATION_STEM_DISPLAY_NAME_TOO_LONG_KEY, 255, GrouperUtil.lengthAscii(this.getDisplayName()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(stemsTable, "description", 
    //        Types.VARCHAR, "1024", false, false);
    if (GrouperUtil.lengthAscii(this.getDescription()) > 1024 ) {
      throw new GrouperValidationException("Stem description too long: " + GrouperUtil.lengthAscii(this.getDescription()), 
          VALIDATION_STEM_DESCRIPTION_TOO_LONG_KEY, 1024, GrouperUtil.lengthAscii(this.getDescription()));
    }
  }
  
  /**
   * Set <i>displayExtension</i>.
   * <p>This will also update the <i>displayName</i> of all child stems and groups.</p>
   * <pre class="eg">
   * try {
   *  ns.setDisplayExtension(value);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to set displayExtension
   * catch (StemModifyException eNSM) {
   *   // Error setting displayExtension
   * }
   * </pre>
   * @param   value   Set displayExtension to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setDisplayExtension(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    // If root stem, give exception.  I'm leaving the root stem specific logic below
    // in case we want to remove this later.  But if we remove this, the onPreUpdate logic 
    // for name changes would need to be adjusted.
    if (this.isRootStem()) {
      throw new StemModifyException("cannot set display extension on root stem.");
    }
    
    StopWatch sw = new StopWatch();
    sw.start();
    NamingValidator nv = NamingValidator.validate(value);
    if (nv.isInvalid()) {
      if ( this.isRootStem() && value.equals(ROOT_NAME) ) {
        // Appease Oracle
        value = ROOT_INT;   
      }
      else {
        throw new StemModifyException( nv.getErrorMessage() );
      }
    }
    if ( !PrivilegeHelper.canStemAdmin( this, GrouperSession.staticGrouperSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN);
    }
    try {
      this.setDisplayExtensionDb(value);
      this.internal_setModified();
      if (this.isRootStem()) {
        this.setDisplayNameDb(value);
      }
      else {
        try {
          this.setDisplayNameDb( U.constructName( this.getParentStem().getDisplayName(), value ) );
        }
        catch (StemNotFoundException eShouldNeverHappen) {
          throw new IllegalStateException( 
            "this should never happen: non-root stem without parent: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen 
          );
        }
      }
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set displayExtension: " + eDAO.getMessage(), eDAO );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_NAME;
    }
    EL.stemSetAttr(GrouperSession.staticGrouperSession(), this.getName(), "displayExtension", value, sw);
  } // public void setDisplayExtension(value)

  
  /**
   * Set <i>extension</i>.
   * <p>This will also update the <i>name</i> of all child stems and groups.</p>
   * <pre class="eg">
   * try {
   *  ns.setExtension(value);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to set "extension"
   * catch (StemModifyException eNSM) {
   *   // Error setting "extension"
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setExtension(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException {
     setExtension(value, true);
  }
  
  /**
   * Set <i>extension</i>.
   * <p>This will also update the <i>name</i> of all child stems and groups.</p>
   * <pre class="eg">
   * try {
   *  ns.setExtension(value, true);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to set "extension"
   * catch (StemModifyException eNSM) {
   *   // Error setting "extension"
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @param   assignAlternateName   Whether to add the old group and stem names as 
   *                                alternate names for any renamed groups and stems.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setExtension(String value, boolean assignAlternateName) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    // If root stem, give exception.  I'm leaving the root stem specific logic below
    // in case we want to remove this later.  But if we remove this, we'll have to deal
    // with parts of the code that identify the root stem as "" or ":".
    // Also, the onPreUpdate logic for name changes would need to be adjusted.
    if (this.isRootStem()) {
      throw new StemModifyException("cannot set extension on root stem.");
    }
    
    // TODO 20070531 DRY w/ "setDisplayExtension"
    StopWatch sw = new StopWatch();
    sw.start();
    NamingValidator nv = NamingValidator.validate(value);
    if (nv.isInvalid()) {
      if ( this.isRootStem() && value.equals(ROOT_NAME) ) {
        // Appease Oracle
        value = ROOT_INT;   
      }
      else {
        throw new StemModifyException( nv.getErrorMessage() );
      }
    }
    if ( !PrivilegeHelper.canStemAdmin( this, GrouperSession.staticGrouperSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN);
    }
    
    // verify that if the property security.stem.groupAllowedToRenameStem is set,
    // then the user is a member of that group.
    if (!PrivilegeHelper.canRenameStems(GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException("User cannot rename stems.");      
    }
    
    String oldExtension = null;
    if (this.dbVersion() != null) {
      oldExtension = this.dbVersion().getExtensionDb();
    }
    
    if (assignAlternateName && oldExtension != null && !oldExtension.equals(value)) {
      internal_addAlternateName(this.dbVersion().getNameDb());
    }
    
    try {
      this.setExtensionDb(value);
      this.internal_setModified();
      if (this.isRootStem()) {
        this.setNameDb(value);
      }
      else {
        try {
          this.setNameDb( U.constructName( this.getParentStem().getName(), value ) );
        }
        catch (StemNotFoundException eShouldNeverHappen) {
          throw new IllegalStateException( 
            "this should never happen: non-root stem without parent: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen 
          );
        }
      }
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set extension: " + eDAO.getMessage(), eDAO );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_NAME;
    }
    EL.stemSetAttr( GrouperSession.staticGrouperSession(), this.getName(), "extension", value, sw );
    
    this.setAlternateNameOnMovesAndRenames = assignAlternateName;
  } // public void setExtension(value)

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "displayName", this.getDisplayName()  )
      .append( "name",  this.getName()         )
      .append( "uuid",                this.getUuid()         )
      .append( "creator",             this.getCreatorUuid()  )
      .append( "modifier",            this.getModifierUuid() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  /**
   * add root stem
   * @param s session
   * @param changed if you want to know if it was added, pass in array of size one, else null
   * @since   1.2.0
   * @return stem
   * @throws GrouperException is problem
   */
  public static Stem internal_addRootStem(GrouperSession s, boolean[] changed) 
    throws  GrouperException {
    Stem root = null;
    try {
      root = StemFinder.findByName(s, ROOT_INT, true);
    } catch (StemNotFoundException snfe) {
    
    }
    
    //dont add twice!
    if (root != null) {
      
      if (!StringUtils.equals(root.getDisplayExtensionDb(), ROOT_INT)) {
        throw new RuntimeException("Root display extension should be '" 
            + ROOT_INT + "' but is: '" + root.getDisplayExtensionDb() + "'" );
      }
      if (!StringUtils.equals(root.getDisplayNameDb(), ROOT_INT)) {
        throw new RuntimeException("Root display name should be '" 
            + ROOT_INT + "' but is: '" + root.getDisplayNameDb() + "'" );
      }
      if (!StringUtils.equals(root.getExtensionDb(), ROOT_INT)) {
        throw new RuntimeException("Root extension should be '" 
            + ROOT_INT + "' but is: '" + root.getExtensionDb() + "'" );
      }
      if (!StringUtils.equals(root.getNameDb(), ROOT_INT)) {
        throw new RuntimeException("Root name should be '" 
            + ROOT_INT + "' but is: '" + root.getNameDb() + "'" );
      }
      if (GrouperUtil.length(changed) > 0) {
        changed[0] = false;
      }
      return root;
    }
    if (GrouperUtil.length(changed) > 0) {
      changed[0] = false;
    }
    
    //note, no need for GrouperSession inverse of control
    try {
      root = new Stem();
      root.setCreatorUuid( s.getMember().getUuid() );
      root.setCreateTimeLong( new Date().getTime() );
      root.setDisplayExtensionDb(ROOT_INT);
      root.setDisplayNameDb(ROOT_INT);
      root.setExtensionDb(ROOT_INT);
      root.setNameDb(ROOT_INT);
      root.setUuid( GrouperUuid.getUuid() );
      GrouperDAOFactory.getFactory().getStem().createRootStem(root) ;
      return root;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.STEM_ROOTINSTALL + eDAO.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eDAO);
    }
  } // protected static Stem internal_addRootStem(GrouperSession s)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Stem.class);

  /**
   * set modified
   * @since   1.2.0
   */
  public void internal_setModified() {
    this.setModifierUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeLong(  new Date().getTime()    );
  } // protected void internal_setModified()


  // PROTECTED INSTANCE METHODS //

  /**
   * add child group with uuid
   * @param extn extension
   * @param dExtn display extension
   * @param uuid uuid
   * @return group 
   * @throws GroupAddException if problem 
   * @throws InsufficientPrivilegeException if problem 
   * @since   1.2.0
   */
  public Group internal_addChildGroup(final String extn, final String dExtn, final String uuid) 
    throws GroupAddException, InsufficientPrivilegeException {
    
    return internal_addChildGroup(extn, dExtn, uuid, null);
  }
  
  /**
   * add child group with uuid
   * @param extn extension
   * @param dExtn display extension
   * @param uuid uuid
   * @param typeOfGroup
   * @return group 
   * @throws GroupAddException if problem 
   * @throws InsufficientPrivilegeException if problem 
   */
  public Group internal_addChildGroup(final String extn, final String dExtn, final String uuid, final TypeOfGroup typeOfGroup) 
    throws GroupAddException, InsufficientPrivilegeException {

    Set types = new LinkedHashSet<GroupType>();
    
    return internal_addChildGroup(extn, dExtn, uuid, 
        null, types, new HashMap<String, String>(), true, typeOfGroup, true);    
  }
  
  /**
   * 
   * @param extn
   * @param dExtn
   * @param uuid
   * @param description
   * @param types
   * @param attributes
   * @param addDefaultGroupPrivileges
   * @param typeOfGroup or null for default
   * @param checkSecurity 
   * @return group
   * @throws GroupAddException
   * @throws InsufficientPrivilegeException
   */
  public Group internal_addChildGroup(final String extn, final String dExtn,
      final String uuid, final String description, final Set<GroupType> types,
      final Map<String, String> attributes, final boolean addDefaultGroupPrivileges, final TypeOfGroup typeOfGroup,
      final boolean checkSecurity)
      throws GroupAddException, InsufficientPrivilegeException {
    
    final String errorMessageSuffix = ", stem name: " + this.name + ", group extension: " + extn
      + ", group dExtension: " + dExtn + ", uuid: " + uuid + ", typeOfGroup: " + typeOfGroup;
    
    Group group = (Group)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {
  
          @SuppressWarnings("deprecation")
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
              
              StopWatch sw = new StopWatch();
              sw.start();
              if (checkSecurity) {
                if (!PrivilegeHelper.canCreate(GrouperSession.staticGrouperSession(), 
                    Stem.this, GrouperSession.staticGrouperSession().getSubject())) {
                  throw new InsufficientPrivilegeException(E.CANNOT_CREATE + errorMessageSuffix + ", " + GrouperSession.staticGrouperSession());
                } 
              }
              GrouperValidator v = AddGroupValidator.validate(Stem.this, extn, dExtn);
              if (v.isInvalid()) {
                if (v.getErrorMessage().startsWith(AddGroupValidator.GROUP_ALREADY_EXISTS_WITH_NAME_PREFIX)) {
                  throw new GroupAddAlreadyExistsException(v.getErrorMessage() + errorMessageSuffix);
                }
                throw new GroupAddException( v.getErrorMessage() + errorMessageSuffix );
              }
        
              Group _g = new Group();
              _g.setParentUuid(Stem.this.getUuid());
              if (GrouperLoader.isDryRun()) {
                _g.setDisplayExtensionDb(dExtn);
                _g.setExtensionDb(extn);
                
              } else {
                _g.setDisplayExtension(dExtn);
                _g.setExtension(extn);
                
              }
              _g.setDescription(description);
              _g.setCreateTimeLong(new Date().getTime());
              _g.setCreatorUuid(GrouperSession.staticGrouperSession().getMember().getUuid());
              _g.setTypes(types);
              
              if (typeOfGroup != null) {
                _g.setTypeOfGroup(typeOfGroup);
              }
  
              v = NotNullOrEmptyValidator.validate(uuid);
              if (v.isInvalid()) {
                _g.setUuid( GrouperUuid.getUuid() );
              }
              else {
                _g.setUuid(uuid);
              }
        
              //CH 20080220: this will start saving the group
              //if loader dry run dont bother
              if (GrouperLoader.isDryRun()) {
                GrouperLoader.dryRunWriteLine("Creating group: " + name);
                
              } else {
                GrouperSubject  subj  = new GrouperSubject(_g);
                Member _m = new Member();
                _m.setSubjectIdDb( subj.getId() );
                _m.setSubjectSourceIdDb( subj.getSource().getId() );
                _m.setSubjectTypeId( subj.getType().getName() );
                _m.updateMemberAttributes(subj, false);
                // TODO 20070328 this is incredibly ugly.  making it even worse is that i am also checking
                //               for existence in the dao as well.
                if (uuid == null) {
                  _m.setUuid( GrouperUuid.getUuid() ); // assign a new uuid
                }
                else {
                  try {
                    // member already exists.  use existing uuid.
                    _m.setUuid( GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true).getUuid() );
                  }
                  catch (MemberNotFoundException eMNF) {
                    // couldn't find member.  assign new uuid.
                    _m.setUuid( GrouperUuid.getUuid() ); 
                  }
                }
        

                GrouperDAOFactory.getFactory().getStem().createChildGroup(Stem.this, _g, _m);
                hibernateHandlerBean.getHibernateSession().misc().flush();
                
                if (addDefaultGroupPrivileges) {
                  _grantDefaultPrivsUponCreate(_g);
                }
                
                // take care of attributes now that default privs have been added
                for (GroupType groupType : _g.getTypesDb()) {    
                  _g.getAttributeDelegate().internal_assignAttributeHelper(null, groupType.getAttributeDefName(), checkSecurity, null, null);
                }
                
                //loop through in case an attribute is set in hook
                if (attributes != null) {
                  for (String key : attributes.keySet()) {
                    _g.setAttribute(key, attributes.get(key), checkSecurity);
                  }
                }
                
                //fire a rule
                RulesGroupBean rulesGroupBean = new RulesGroupBean(_g);
                //fire rules directly connected to this membership remove
                RuleEngine.fireRule(RuleCheckType.groupCreate, rulesGroupBean);

              }
              
              
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = null;
                
                if (typeOfGroup == TypeOfGroup.entity) {
                  auditEntry = new AuditEntry(AuditTypeBuiltin.ENTITY_ADD, "id", 
                      _g.getUuid(), "name", _g.getName(), "parentStemId", Stem.this.getUuid(), "displayName", 
                      _g.getDisplayName(), "description", _g.getDescription());
                  auditEntry.setDescription("Added entity: " + _g.getName());
                  
                } else {
                  auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ADD, "id", 
                      _g.getUuid(), "name", _g.getName(), "parentStemId", Stem.this.getUuid(), "displayName", 
                      _g.getDisplayName(), "description", _g.getDescription());
                  auditEntry.setDescription("Added group: " + _g.getName());
                  
                }
                auditEntry.saveOrUpdate(true);
              }
              
              sw.stop();
              EventLog.info(GrouperSession.staticGrouperSession(), M.GROUP_ADD + Quote.single(_g.getName()), sw);
              
              return _g;
            } catch (GrouperDAOException eDAO) {
              throw new GroupAddException( E.CANNOT_CREATE_GROUP + errorMessageSuffix + eDAO.getMessage(), eDAO );
            } catch (SourceUnavailableException eSU)  {
              throw new GroupAddException(E.CANNOT_CREATE_GROUP + errorMessageSuffix + eSU.getMessage(), eSU);
            }
          }
        });
    
    return group;
  }

  /**
   * @param attributeDef
   * @param session
   * @param extension
   * @param displayExtension
   * @param id
   * @param description
   * @return group
   * @throws AttributeDefNameAddException
   * @throws InsufficientPrivilegeException
   */
  public AttributeDefName internal_addChildAttributeDefName(final GrouperSession session, final AttributeDef attributeDef,
      final String extension, final String displayExtension,
      final String id, final String description)
      throws InsufficientPrivilegeException {

    final String errorMessageSuffix = ", stem name: " + this.name + ", attrDefName extension: " + extension
      + ", uuid: " + id + ", ";

    return (AttributeDefName)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              StopWatch sw = new StopWatch();
              sw.start();
              if (!PrivilegeHelper.canCreate(session,
                  Stem.this, session.getSubject())) {
                throw new InsufficientPrivilegeException(E.CANNOT_CREATE + errorMessageSuffix);
              }
              GrouperValidator v = AddAttributeDefNameValidator.validate(Stem.this, extension);
              if (v.isInvalid()) {
                throw new AttributeDefNameAddException( v.getErrorMessage() + errorMessageSuffix );
              }
        
              AttributeDefName attributeDefName = new AttributeDefName();
              attributeDefName.setAttributeDefId(attributeDef.getId());
              attributeDefName.setStemId(Stem.this.getUuid());
              attributeDefName.setExtensionDb(extension);
              attributeDefName.setDisplayExtensionDb(displayExtension);
              attributeDefName.setDescription(description);
              attributeDefName.setNameDb(Stem.this.getName() + ":" + extension);
              attributeDefName.setDisplayNameDb(Stem.this.getDisplayName() + ":" + displayExtension);

              String theId = id;
              if (StringUtils.isBlank(theId)) {
                theId = GrouperUuid.getUuid();
              }
              
              attributeDefName.setId(theId);
                              
              //CH 20080220: this will start saving the attributeDefName
              GrouperDAOFactory.getFactory().getStem().createChildAttributeDefName( Stem.this, attributeDefName );
                
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD, "id", 
                    attributeDefName.getId(), "name", attributeDefName.getName(), 
                    "displayName", attributeDefName.getDisplayName(),
                    "description", attributeDefName.getDescription(),
                    "parentStemId", Stem.this.getUuid(), 
                    "parentAttributeDefId", attributeDef.getId(),
                    "parentAttributeDefName", attributeDef.getName()
                );
                auditEntry.setDescription("Added attributeDefName: " + attributeDefName.getName());
                auditEntry.saveOrUpdate(true);
              }
              
              sw.stop();
              
              return attributeDefName;
            } catch (HookVeto hv) {
              throw hv;
            } catch (AttributeDefNameAddException adnae) {
              throw adnae;
            } catch (Exception e) {
              throw new AttributeDefNameAddException( "Cannot create attribute def name: " + errorMessageSuffix + e.getMessage(), e );
            }
          }
        });
  } 

  
  /**
   * 
   * @param session
   * @param extn
   * @param id
   * @param attributeDefType 
   * @param description
   * @return group
   * @throws AttributeDefAddException
   * @throws InsufficientPrivilegeException
   */
  public AttributeDef internal_addChildAttributeDef(final GrouperSession session, final String extn,
      final String id, final AttributeDefType attributeDefType, final String description)
      throws InsufficientPrivilegeException {
    
    final String errorMessageSuffix = ", stem name: " + this.name + ", attrDef extension: " + extn
      + ", uuid: " + id + ", ";
    
    AttributeDef attributeDef = (AttributeDef)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try {

              hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

              StopWatch sw = new StopWatch();
              sw.start();
              if (!PrivilegeHelper.canCreate(session, 
                  Stem.this, session.getSubject())) {
                throw new InsufficientPrivilegeException(E.CANNOT_CREATE + errorMessageSuffix);
              } 
              GrouperValidator v = AddAttributeDefValidator.validate(Stem.this, extn);
              if (v.isInvalid()) {
                throw new AttributeDefAddException( v.getErrorMessage() + errorMessageSuffix );
              }
        
              AttributeDef attributeDef = new AttributeDef();
              attributeDef.setStemId(Stem.this.getUuid());
              attributeDef.setExtensionDb(extn);
              attributeDef.setDescription(description);
              attributeDef.setNameDb(Stem.this.getName() + ":" + extn);
              attributeDef.setAttributeDefType(attributeDefType);

              String theId = id;
              if (StringUtils.isBlank(theId)) {
                theId = GrouperUuid.getUuid();
              }
              
              attributeDef.setId(theId);
                              
              //CH 20080220: this will start saving the attributeDef
              GrouperDAOFactory.getFactory().getStem().createChildAttributeDef( Stem.this, attributeDef );
                
              //default action
              attributeDef.getAttributeDefActionDelegate().addAction("assign");
              
              //grant privs
              _grantDefaultPrivsUponCreate(attributeDef);
              
              //fire a rule
              RulesAttributeDefBean rulesAttributeDefBean = new RulesAttributeDefBean(attributeDef);
              //fire rules directly connected to this membership remove
              RuleEngine.fireRule(RuleCheckType.attributeDefCreate, rulesAttributeDefBean);

              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_DEF_ADD, "id", 
                    attributeDef.getId(), "name", attributeDef.getName(), "parentStemId", Stem.this.getUuid(), 
                    "description", attributeDef.getDescription());
                auditEntry.setDescription("Added attributeDef: " + attributeDef.getName());
                auditEntry.saveOrUpdate(true);
              }
              
              sw.stop();
              
              return attributeDef;
            } catch (HookVeto hv) {
              GrouperUtil.injectInException(hv, "Cannot create attribute def: " + errorMessageSuffix);
              throw hv;
            } catch (Exception e) {
              throw new AttributeDefAddException( "Cannot create attribute def: " + errorMessageSuffix + ", " + e.getMessage(), e );
            }
          }
        });
    
    return attributeDef;
    
  } 

  /**
   * add child stem with uuid
   * @since   1.2.0
   * @param extn extension
   * @param dExtn display extension
   * @param uuid uuid
   * @return the new stem
   * @throws StemAddException if problem
   * @throws InsufficientPrivilegeException if problem
   */
  public Stem internal_addChildStem(final String extn, final String dExtn,
      final String uuid) throws StemAddException, InsufficientPrivilegeException {
    return internal_addChildStem(GrouperSession.staticGrouperSession(), extn, dExtn,
        uuid, true, true);
  }

  
  /**
   * keep a map of strings to synchronize on
   */
  private static GrouperCache<String, String> stemLocksCache = null;
  
  /**
   * stem locks cache
   * @return the cache
   */
  private static GrouperCache<String, String> stemLocksCache() {
    if (stemLocksCache == null) {
      synchronized(Stem.class) {
        if (stemLocksCache == null) {
          stemLocksCache = new GrouperCache<String, String>(Stem.class.getName() + ".stemLocksCache");
        }
      }
    }
    return stemLocksCache;
  }
  
  /**
   * keep track of when stem is created
   */
  private static GrouperCache<String, Boolean> stemCreatedCache = null;
  
  /**
   * stem created cache
   * @return the cache
   */
  private static GrouperCache<String, Boolean> stemCreatedCache() {
    if (stemCreatedCache == null) {
      synchronized(Stem.class) {
        if (stemCreatedCache == null) {
          stemCreatedCache = new GrouperCache<String, Boolean>(Stem.class.getName() + ".stemCreatedCache");
        }
      }
    }
    return stemCreatedCache;
  }
  
  /**
   * 
   * @param session
   * @param extn
   * @param dExtn
   * @param uuid
   * @param addDefaultStemPrivileges
   * @param failIfExists 
   * @return stem
   * @throws StemAddException
   * @throws InsufficientPrivilegeException
   */
  protected Stem internal_addChildStem(final GrouperSession session, final String extn, 
      final String dExtn, final String uuid, final boolean addDefaultStemPrivileges, final boolean failIfExists) 
    throws  StemAddException,
            InsufficientPrivilegeException {
    
    if (StringUtils.isBlank(extn) || extn.contains(":")) {
      throw new StemAddException("extension cant be blank or contain colon: '" + extn + "'");
    }
    
    String stemName = U.constructName( Stem.this.getName(), extn );
    
    Stem stem = StemFinder.findByName(session.internal_getRootSession(), stemName, false, new QueryOptions().secondLevelCache(false));

    if (stem != null && failIfExists) {
      throw new StemAddAlreadyExistsException("Stem exists: " + stemName);
    }
    
    if (stem == null) {
      
      synchronized (Stem.class) {
        //get the same key for name
        if (!stemLocksCache().containsKey(stemName)) {
          stemLocksCache().put(stemName, stemName);
        }
        stemName = stemLocksCache().get(stemName);
      }
      final String STEM_NAME = stemName;

      synchronized(stemName) {
        
        //something created this recently
        if (stemCreatedCache().get(stemName) != null) {

          //wait for the transaction to finish...
          for (int i=0;i<20;i++) {

            //in transaction
            stem = StemFinder.findByName(session.internal_getRootSession(), STEM_NAME, false, new QueryOptions().secondLevelCache(false));
            
            if (stem != null) {

              if (failIfExists) {
                throw new StemAddAlreadyExistsException("Stem exists: " + stemName);
              }
              
              break;
            }

            //out of transaction
            stem = (Stem)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.NONE, new GrouperTransactionHandler() {
              
              public Object callback(GrouperTransaction grouperTransaction)
                  throws GrouperDAOException {
                return StemFinder.findByName(session.internal_getRootSession(), STEM_NAME, false, new QueryOptions().secondLevelCache(false));
              }
            });
            
            if (stem != null) {
              
              if (failIfExists) {
                throw new StemAddAlreadyExistsException("Stem exists: " + stemName);
              }
              
              // if its in another transaction... wait a couple seconds for it to finish
              GrouperUtil.sleep(2000);

              break;
            }

            GrouperUtil.sleep(1000);
          }
          
        }
        
        
        if (stem == null) {

          //race conditions
          stemCreatedCache().put(stemName, Boolean.TRUE);
          
          stem = (Stem)HibernateSession.callbackHibernateSession(
              GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
              new HibernateHandler() {

                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                    throws GrouperDAOException {

                  hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

                  StopWatch sw = new StopWatch();
                  sw.start();
                  if ( !GrouperLoader.isDryRun() && !PrivilegeHelper.canCreate(GrouperSession.staticGrouperSession(), Stem.this, session.getSubject())) {
                    throw new InsufficientPrivilegeException(E.CANNOT_CREATE + ", "
                        + GrouperUtil.toStringSafe(Stem.this) + ", extn: " + extn + ", dExtn: " 
                        + dExtn + ", uuid: " + uuid + ", subject: " + session.getSubject());
                  } 
                  GrouperValidator v = AddStemValidator.validate(Stem.this, extn, dExtn);
                  if (v.isInvalid()) {
                    String errorMessage = StringUtils.defaultString(v.getErrorMessage());
                    if (errorMessage.startsWith(AddStemValidator.STEM_ALREADY_EXISTS_ERROR_MESSAGE)) {
                      throw new StemAddAlreadyExistsException(errorMessage);
                    }
                    throw new StemAddException( errorMessage );
                  }
                  try {
                    Stem _ns = new Stem();
                    _ns.setCreatorUuid( session.getMember().getUuid() );
                    _ns.setCreateTimeLong( new Date().getTime() );
                    _ns.setDisplayExtensionDb(dExtn);
                    _ns.setDisplayNameDb( U.constructName( Stem.this.getDisplayName(), dExtn ) );
                    _ns.setExtensionDb(extn);
                    _ns.setNameDb(STEM_NAME );
                    _ns.setParentUuid( Stem.this.getUuid() );
                    
                    v = NotNullOrEmptyValidator.validate(uuid);
                    if (v.isInvalid()) {
                      _ns.setUuid( GrouperUuid.getUuid() );
                    }
                    else {
                      _ns.setUuid(uuid);
                    }
                    
                    if (!GrouperLoader.isDryRun()) {
                      GrouperDAOFactory.getFactory().getStem().createChildStem( _ns ) ;
        
        
                      if (addDefaultStemPrivileges) {
                        _grantDefaultPrivsUponCreate(_ns);
                      }
        
                      //fire a rule
                      RulesStemBean rulesStemBean = new RulesStemBean(_ns);
                      //fire rules directly connected to this membership remove
                      RuleEngine.fireRule(RuleCheckType.stemCreate, rulesStemBean);
        
                      
                      if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ADD, "id", 
                            _ns.getUuid(), "name", _ns.getName(), "parentStemId", Stem.this.getUuid(), "displayName", 
                            _ns.getDisplayName(), "description", _ns.getDescription());
                        auditEntry.setDescription("Added stem: " + _ns.getName());
                        auditEntry.saveOrUpdate(true);
                      }
                    } 
                    
                    sw.stop();
                    EventLog.info(session, M.STEM_ADD + Quote.single( _ns.getName() ), sw);

                    return _ns;
                  } catch (StemAddException e) {
                    String error = "Problem creating child stem: " + GrouperUtil.toStringSafe(Stem.this)
                      + ", extn: " + extn + ", dExtn: " + dExtn + ", uuid: " + uuid + ", " + e.getMessage();
                    GrouperUtil.injectInException(e, error);
                    throw e;
                  } catch (GrouperDAOException e) {
                    String error = "Problem creating child stem: " + GrouperUtil.toStringSafe(Stem.this)
                      + ", extn: " + extn + ", dExtn: " + dExtn + ", uuid: " + uuid + ", " + e.getMessage();
                    GrouperUtil.injectInException(e, error);
                    throw new StemAddException(E.CANNOT_CREATE_STEM + e.getMessage(), e);
                  }
                }
          });
        }
      }
    }
    
    return stem;
  }

  /**
   * <pre>
   * Now grant ADMIN (as root) to the creator of the child group.
   *
   * Ideally this would be wrapped up in the broader transaction
   * of adding the child stem but as the interfaces may be
   * outside of our control, I don't think we can do that.  
   *
   * Possibly a bug. The modify* attrs get set when granting ADMIN at creation.
   * </pre>
   * @param g group
   * @throws GroupAddException if problem
   */
  private void _grantDefaultPrivsUponCreate(Group g)
    throws  GroupAddException
  {
    try {
      boolean assignAdminToWheelOrRootOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToWheelOrRootOnCreate", false);
      
      if (assignAdminToWheelOrRootOnCreate || !PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
        
        boolean assignAdminToInheritedAdminsOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToInheritedAdminsOnCreate", false);
        if (assignAdminToInheritedAdminsOnCreate || !RuleApi.hasInheritedPrivilege(g, GrouperSession.staticGrouperSession().getSubject(), AccessPrivilege.ADMIN, true)) {
        
          GrouperSession.staticGrouperSession().internal_getRootSession().getAccessResolver().grantPrivilege(
            g, GrouperSession.staticGrouperSession().getSubject(), AccessPrivilege.ADMIN, null   
          );
        }
      }
      
      // Now optionally grant other privs
      if (g.getTypeOfGroup() != TypeOfGroup.entity) {
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.VIEW, GrouperConfig.GCGAV );
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.OPTIN, GrouperConfig.GCGAOI );
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.OPTOUT, GrouperConfig.GCGAOO );
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.READ, GrouperConfig.GCGAR );
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.GROUP_ATTR_READ, GrouperConfig.GCGAGAR );
      }
      if (g.getTypeOfGroup() == TypeOfGroup.entity) {
        this._grantOptionalPrivUponCreate( g, AccessPrivilege.VIEW, "entities.create.grant.all.view" );
      }
    }
    catch (GrantPrivilegeException eGP)         {
      throw new GroupAddException(eGP.getMessage(), eGP);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new GroupAddException(eIP.getMessage(), eIP);
    }
    catch (SchemaException eS)                  {
      throw new GroupAddException(eS.getMessage(), eS);
    }
    catch (UnableToPerformException eUTP) {
      throw new GroupAddException( eUTP.getMessage(), eUTP );
    }
  } 
  
  /**
   * Now grant ADMIN (as root) to the creator of the attributeDef.
   *
   * @param attributeDef stem
   * @throws AttributeDefAddException if problem
   */
  private void _grantDefaultPrivsUponCreate(AttributeDef attributeDef) throws  AttributeDefAddException {
    try {
      boolean assignAdminToWheelOrRootOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToWheelOrRootOnCreate", false);
      
      if (assignAdminToWheelOrRootOnCreate || !PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
        boolean assignAdminToInheritedAdminsOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToInheritedAdminsOnCreate", false);
        if (assignAdminToInheritedAdminsOnCreate || !RuleApi.hasInheritedPrivilege(attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN, true)) {
          //whoever created this is an admin
          GrouperSession.staticGrouperSession().internal_getRootSession().getAttributeDefResolver().grantPrivilege(
            attributeDef, GrouperSession.staticGrouperSession().getSubject(), AttributeDefPrivilege.ATTR_ADMIN, null);
        }
      }
      
      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        attributeDef, AttributeDefPrivilege.ATTR_ADMIN, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_ADMIN
      );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_OPTIN, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_OPTIN
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_OPTOUT, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_OPTOUT
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_READ, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_READ
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_UPDATE, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_UPDATE
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_VIEW, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_VIEW
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_DEF_ATTR_READ
        );
      this._grantOptionalPrivUponCreate(
          attributeDef, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, GrouperConfig.ATTRIBUTE_DEFS_CREATE_GRANT_ALL_ATTR_DEF_ATTR_UPDATE
        );
    }
    catch (GrantPrivilegeException eGP)         {
      throw new AttributeDefAddException(eGP.getMessage(), eGP);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new AttributeDefAddException(eIP.getMessage(), eIP);
    }
    catch (SchemaException eS)                  {
      throw new AttributeDefAddException(eS.getMessage(), eS);
    }
    catch (UnableToPerformException eUTP) {
      throw new AttributeDefAddException( eUTP.getMessage(), eUTP );
    }
  } 

  /**
   * Now grant STEM (as root) to the creator of the child stem.
   *
   * Ideally this would be wrapped up in the broader transaction
   * of adding the child stem but as the interfaces may be
   * outside of our control, I don't think we can do that.  
   *
   * Possibly a bug. The modify* attrs get set when granting privs at creation.
   * 
   * @param ns stem
   * @throws StemAddException if problem
   */
  private void _grantDefaultPrivsUponCreate(Stem ns)
    throws  StemAddException
  {
    try {
      boolean assignAdminToWheelOrRootOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToWheelOrRootOnCreate", false);
      
      if (assignAdminToWheelOrRootOnCreate || !PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
        
        boolean assignAdminToInheritedAdminsOnCreate = GrouperConfig.retrieveConfig().propertyValueBoolean("privileges.assignAdminToInheritedAdminsOnCreate", false);
        if (assignAdminToInheritedAdminsOnCreate || !RuleApi.hasInheritedPrivilege(ns, GrouperSession.staticGrouperSession().getSubject(), NamingPrivilege.STEM_ADMIN, true)) {

          GrouperSession.staticGrouperSession().internal_getRootSession().getNamingResolver().grantPrivilege(
            ns, GrouperSession.staticGrouperSession().getSubject(), NamingPrivilege.STEM_ADMIN, null
          );
        }
      }
      
      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        ns, NamingPrivilege.CREATE, GrouperConfig.SCGAC
      );
      this._grantOptionalPrivUponCreate(
        ns, NamingPrivilege.STEM_ADMIN, GrouperConfig.SCGASA
      );
      this._grantOptionalPrivUponCreate(
          ns, NamingPrivilege.STEM_ATTR_READ, GrouperConfig.SCGASAR
        );
      this._grantOptionalPrivUponCreate(
          ns, NamingPrivilege.STEM_ATTR_UPDATE, GrouperConfig.SCGASAU
        );
    }
    catch (GrantPrivilegeException eGP)         {
      throw new StemAddException(eGP.getMessage(), eGP);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new StemAddException(eIP.getMessage(), eIP);
    }
    catch (SchemaException eS)                  {
      throw new StemAddException(eS.getMessage(), eS);
    }
    catch (UnableToPerformException eUTP) {
      throw new StemAddException( eUTP.getMessage(), eUTP );
    }
  } 

  /**
   * grant optional priv upon create
   * @param o object
   * @param p prov
   * @param opt opt
   * @throws GrantPrivilegeException if problem 
   * @throws  IllegalStateException if <i>o</i> is neither group nor stem.
   * @throws InsufficientPrivilegeException if not privs
   * @throws SchemaException if problem
   * @throws UnableToPerformException if problem
   * @since   1.2.1
   */
  private void _grantOptionalPrivUponCreate(Object o, Privilege p, String opt) 
    throws  GrantPrivilegeException,
            IllegalStateException,
            InsufficientPrivilegeException,
            SchemaException,
            UnableToPerformException
  {
    Subject       all = SubjectFinder.findAllSubject();
    if (StringUtils.equals(GrouperConfig.retrieveConfig().propertyValueString(opt), GrouperConfig.BT)) {
      StopWatch sw = new StopWatch();
      sw.start();
      if      (o instanceof Group) {
        Group g = (Group) o;
        GrouperSession.staticGrouperSession().getAccessResolver().grantPrivilege(g, all, p, null);
        sw.stop();
        EL.groupGrantPriv(GrouperSession.staticGrouperSession(), g.getName(), all, p, sw);
      }
      else if (o instanceof Stem) {
        Stem ns = (Stem) o;
        GrouperSession.staticGrouperSession().getNamingResolver().grantPrivilege(ns, all, p, null);
        sw.stop();
        EL.stemGrantPriv(GrouperSession.staticGrouperSession(), ns.getName(), all, p, sw);
      }
      else if (o instanceof AttributeDef) {
        AttributeDef attributeDef = (AttributeDef) o;
        GrouperSession.staticGrouperSession().getAttributeDefResolver().grantPrivilege(attributeDef, all, p, null);
        sw.stop();
      }
      else {
        throw new IllegalStateException("unexpected condition: object is not group or stem: " + o);
      }
    }
  } 

  /**
   * rename child groups
   * @since   1.2.0
   * @param nameChange
   * @param displayNameChange
   * @param modifier
   * @param modifyTime
   * @param setAlternateName
   * @return the set of Group's
   */
  private Set _renameChildGroups(boolean nameChange, boolean displayNameChange, String modifier, long modifyTime, 
      boolean setAlternateName) {
    
    Set                 groups  = new LinkedHashSet();
    Iterator<Group> it = GrouperDAOFactory.getFactory().getStem().findAllChildGroups(this, Stem.Scope.ONE).iterator();
    while (it.hasNext()) {
      Group _g = (Group) it.next();
      
      if (displayNameChange) {
        _g.setDisplayNameDb(U.constructName(this.getDisplayName(), _g.getDisplayExtension()));
      }

      if (nameChange) {
        if (setAlternateName) {
          _g.internal_addAlternateName(_g.dbVersion().getNameDb(), false);
        }
        
        _g.setNameDb(U.constructName(this.getName(), _g.getExtension()));
      }
      
      _g.setModifierUuid(modifier);
      _g.setModifyTimeLong(modifyTime);
      _g.setDontSetModified(true);
      groups.add(_g);
    }
    return groups;
  } 

  /**
   * rename children.
   * @since   1.2.0
   * @param nameChange
   * @param displayNameChange
   * @param setAlternateName
   * @return set of stems and groups
   * @throws StemModifyException if problem
   */
  private Set _renameChildren(boolean nameChange, boolean displayNameChange, boolean setAlternateName)
    throws  StemModifyException {
    
    // rename child groups, stems, attributeDefs, and attributeDefNames
    Set     children    = new LinkedHashSet();
    String  modifier    = GrouperSession.staticGrouperSession().getMember().getUuid();
    long    modifyTime  = new Date().getTime();
    children.addAll(this._renameAttr(nameChange, displayNameChange));
    children.addAll(this._renameChildGroups(nameChange, displayNameChange, modifier, modifyTime, setAlternateName));
    children.addAll(this._renameChildStemsAndGroups(nameChange, displayNameChange, modifier, modifyTime, setAlternateName));
    return children;
  } 

  /**
   * rename child stems and groups.
   * @param nameChange
   * @param displayNameChange
   * @param modifier modifier
   * @param modifyTime modify time
   * @param setAlternateName
   * @return the set of stems and groups
   * @throws IllegalStateException if problem
   */
  private Set _renameChildStemsAndGroups(boolean nameChange, boolean displayNameChange, String modifier, 
      long modifyTime, boolean setAlternateName) throws IllegalStateException {
    
    Set       children  = new LinkedHashSet();
    Iterator<Stem> it = GrouperDAOFactory.getFactory().getStem().findAllChildStems(this, Scope.ONE, null, false).iterator();
    
    while (it.hasNext()) {
      Stem child = it.next();
      
      if (displayNameChange) {
        child.setDisplayNameDb(U.constructName(this.getDisplayNameDb(), child.getDisplayExtensionDb()));
      }
    
      if (nameChange) {
        if (setAlternateName) {
          child.internal_addAlternateName(child.dbVersion().getNameDb());
        }
        
        child.setNameDb(U.constructName(this.getNameDb(), child.getExtensionDb()));
      }
      
      // rename child stem
      child.setModifierUuid(modifier);
      child.setModifyTimeLong(modifyTime);
      children.add(child);
      
      // rename attributeDef and attributeDefName
      children.addAll(child._renameAttr(nameChange, displayNameChange));

      children.addAll(child._renameChildGroups(nameChange, displayNameChange, modifier, modifyTime, setAlternateName));
      children.addAll(child._renameChildStemsAndGroups(nameChange, displayNameChange, modifier, modifyTime, setAlternateName));
    }
    return children;
  } 

  /**
   * Rename attributeDef and attributeDefName due to a name and/or displayName change
   * @param nameChange
   * @param displayNameChange
   * @return the children
   */
  private Set<GrouperAPI> _renameAttr(boolean nameChange, boolean displayNameChange) {
    Set<GrouperAPI> children  = new LinkedHashSet();

    if (!nameChange && !displayNameChange) {
      return children;
    }
    
    Set<AttributeDefName> attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findByStem(this.getUuid());
    Iterator<AttributeDefName> attributeDefNameIter = attributeDefNames.iterator();
    while (attributeDefNameIter.hasNext()) {
      AttributeDefName attributeDefName = attributeDefNameIter.next();
      
      if (nameChange) {
        attributeDefName.setNameDb(this.getName() + ":" + attributeDefName.getExtensionDb());
      }
      
      if (displayNameChange) {
        attributeDefName.setDisplayNameDb(this.getDisplayName() + ":" + attributeDefName.getDisplayExtensionDb());
      }
      
      children.add(attributeDefName);
    }
    
    if (nameChange) {
      Set<AttributeDef> attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().findByStem(this.getUuid());
      Iterator<AttributeDef> attributeDefIter = attributeDefs.iterator();
      while (attributeDefIter.hasNext()) {
        AttributeDef attributeDef = attributeDefIter.next();
        attributeDef.setNameDb(this.getName() + ":" + attributeDef.getExtensionDb());
        children.add(attributeDef);
      }
    }
    
    return children;
  }


  /**
   * revoke naming privs
   * @throws InsufficientPrivilegeException if problem
   * @throws RevokePrivilegeException if problem
   * @throws SchemaException if problem
   */
  private void _revokeAllNamingPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException {

    try {
      GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), 
          new GrouperSessionHandler() {
  
            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {

              try {
                Stem.this.revokePriv(NamingPrivilege.CREATE);
                Stem.this.revokePriv(NamingPrivilege.STEM_ADMIN);
                Stem.this.revokePriv(NamingPrivilege.STEM_ATTR_READ);
                Stem.this.revokePriv(NamingPrivilege.STEM_ATTR_UPDATE);
                return null;
              } catch (InsufficientPrivilegeException ipe) {
                throw new GrouperSessionException(ipe);
              } catch (RevokePrivilegeException rpe) {
                throw new GrouperSessionException(rpe);
              } catch (SchemaException se) {
                throw new GrouperSessionException(se);
              }
            }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException) gse.getCause();
      }
      if (gse.getCause() instanceof RevokePrivilegeException) {
        throw (RevokePrivilegeException) gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException) gse.getCause();
      }
      throw gse;
    }
  } // private void _revokeAllNamingPrivs()

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Stem)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.name, ( (Stem) other ).name )
      .isEquals();
  } // public boolean equals(other)

  /**
   * 
   * @return create time
   */
  public long getCreateTimeLong() {
    return this.createTime;
  }

  /**
   * 
   * @return create time
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @return description
   * @since   1.2.0
   */
  public String getDescriptionDb() {
    return this.description;
  }

  /**
   * @return displayExtension
   * @since   1.2.0
   */
  public String getDisplayExtensionDb() {
    return this.displayExtension;
  }

  /**
   * @return displayName
   * @since   1.2.0
   */
  public String getDisplayNameDb() {
    return this.displayName;
  }

  /**
   * @return extension
   * @since   1.2.0
   */
  public String getExtensionDb() {
    return this.extension;
  }

  /**
   * @return modifier uuid
   * @since   1.2.0
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @return modify time long
   * @since   1.2.0
   */
  public long getModifyTimeLong() {
    return this.modifyTime;
  }

  /**
   * @return name
   * @since   1.2.0
   */
  public String getNameDb() {
    return this.name;
  }

  /**
   * @return parent uuid
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUuid;
  }

  /**
   * @return hash code
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.name )
      .toHashCode();
  } // public int hashCode()

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
   * @param description 
   * @since   1.2.0
   */
  public void setDescriptionDb(String description) {
    this.description = description;
  
  }

  /**
   * @param displayExtension 
   * @since   1.2.0
   */
  public void setDisplayExtensionDb(String displayExtension) {
    this.displayExtension = displayExtension;
  
  }

  /**
   * @param displayName 
   * @since   1.2.0
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  
  }

  /**
   * @param displayName 
   * @since   1.2.0
   */
  public void setDisplayNameDb(String displayName) {
    this.displayName = displayName;
  
  }

  /**
   * @param extension 
   * @since   1.2.0
   */
  public void setExtensionDb(String extension) {
    this.extension = extension;
  
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
   * @param name 
   * @since   1.2.0
   */
  public void setName(String name) {
    this.name = name;
  
  }

  /**
   * @param name 
   * @since   1.2.0
   */
  public void setNameDb(String name) {
    this.name = name;
  
  }

  /**
   * @param parentUUID 
   * @since   1.2.0
   */
  public void setParentUuid(String parentUUID) {
    this.parentUuid = parentUUID;
  
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
      .append( "createTime",       this.getCreateTime()       )
      .append( "creatorUuid",      this.getCreatorUuid()      )
      .append( "description",      this.getDescription()      )
      .append( "displayExtension", this.getDisplayExtension() )
      .append( "displayName",      this.getDisplayName()      )
      .append( "extension",        this.getExtension()        )
      .append( "modifierUuid",     this.getModifierUuid()     )
      .append( "modifyTime",       this.getModifyTime()       )
      .append( "name",             this.getName()             )
      .append( "ownerUuid",        this.getUuid()             )
      .append( "parentUuid",       this.getParentUuid()       )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_COMMIT_DELETE, HooksStemBean.class, 
        this, Stem.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_DELETE, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_POST_DELETE, false, true);
    
    InstrumentationThread.addCount(InstrumentationDataBuiltinTypes.API_STEM_DELETE.name());
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_INSERT, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_COMMIT_INSERT, HooksStemBean.class, 
        this, Stem.class);

    InstrumentationThread.addCount(InstrumentationDataBuiltinTypes.API_STEM_ADD.name());
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    if (this.dbVersionDifferentFields().contains(FIELD_NAME)) {
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
        /**
         * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
         */
        public Object callback(GrouperSession rootSession) throws GrouperSessionException {

          // need to potentially update stem name in rules
          Set<RuleDefinition> definitions = RuleEngine.ruleEngine().getRuleDefinitions();
          for (RuleDefinition definition : definitions) {
            if (definition.getCheck() != null && definition.getCheck().checkTypeEnum() != null && 
                definition.getCheck().checkTypeEnum().isCheckOwnerTypeStem(definition) && Stem.this.dbVersion().getName().equals(definition.getCheck().getCheckOwnerName())) {
              definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), Stem.this.getName());
            }
            
            if (definition.getIfCondition() != null && definition.getIfCondition().ifConditionEnum() != null &&
                definition.getIfCondition().ifConditionEnum().isIfOwnerTypeStem(definition) && Stem.this.dbVersion().getName().equals(definition.getIfCondition().getIfOwnerName())) {
              definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleIfOwnerNameName(), Stem.this.getName());
            }
            
            // take care of nameMatchesSqlLikeString
            // if sql like string is a:b:%someGroup and a is changing to x:a, update sql like string to x:a:b:%someGroup
            if (definition.getIfCondition() != null && definition.getIfCondition().ifConditionEnum() == RuleIfConditionEnum.nameMatchesSqlLikeString) {
              if (definition.getIfCondition().getIfConditionEnumArg0().startsWith(Stem.this.dbVersion().getName() + ":")) {
                String newString = Stem.this.getName() + definition.getIfCondition().getIfConditionEnumArg0().substring(Stem.this.dbVersion().getName().length());
                definition.getAttributeAssignType().getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionEnumArg0Name(), newString);
              }
            }
          }
          
          return null;
        }
      });
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_PARENT_UUID)) {
      // stem is being moved.  take care of stem sets..
      Set<StemSet> ifHasStemSetsOfParentStem = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(this.parentUuid);
      Set<StemSet> oldStemSets = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(this.uuid);
      GrouperDAOFactory.getFactory().getStem().moveStemSets(new LinkedList<StemSet>(ifHasStemSetsOfParentStem), new LinkedList<StemSet>(oldStemSets), this.uuid, 0);
    }
    
    super.onPostUpdate(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_COMMIT_UPDATE, HooksStemBean.class, 
        this, Stem.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_POST_UPDATE, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    //delete some caches
    Hib3MemberDAO.membersCacheClear();
    Hib3AttributeDefDAO.attributeDefCacheClear();
    Hib3AttributeDefNameDAO.attributeDefNameCacheClear();
    StemFinder.stemCacheClear();
    GroupFinder.groupCacheClear();
    Hib3AttributeAssignActionDAO.attributeAssignActionCacheClear();
    Hib3AttributeAssignDAO.attributeAssignCacheClear();
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_PRE_DELETE, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_PRE_DELETE, false, false);
  
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.STEM_DELETE, 
        ChangeLogLabels.STEM_DELETE.id.name(), 
        this.getUuid(), ChangeLogLabels.STEM_DELETE.name.name(), 
        this.getName(), ChangeLogLabels.STEM_DELETE.parentStemId.name(), this.getParentUuid(),
        ChangeLogLabels.STEM_DELETE.displayName.name(), this.getDisplayName(),
        ChangeLogLabels.STEM_DELETE.description.name(), this.getDescription()).save();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    if (this.idIndex == null) {
      this.idIndex = TableIndex.reserveId(TableIndexType.stem);
    }

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_PRE_INSERT, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_PRE_INSERT, false, false);
  
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.STEM_ADD, 
        ChangeLogLabels.STEM_ADD.id.name(), 
        this.getUuid(), ChangeLogLabels.STEM_ADD.name.name(), 
        this.getName(), ChangeLogLabels.STEM_ADD.parentStemId.name(), this.getParentUuid(),
        ChangeLogLabels.STEM_ADD.displayName.name(), this.getDisplayName(),
        ChangeLogLabels.STEM_ADD.description.name(), this.getDescription()).save();
  }

  /** see if already in onPreUpdate, dont go in again */
  private static ThreadLocal<Boolean> inOnPreUpdate = new ThreadLocal<Boolean>();
  
  /** whether we should be setting alternate names for groups during moves and renames */
  private boolean setAlternateNameOnMovesAndRenames;
    
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    //delete some caches
    Hib3MemberDAO.membersCacheClear();
    Hib3AttributeDefDAO.attributeDefCacheClear();
    Hib3AttributeDefNameDAO.attributeDefNameCacheClear();
    StemFinder.stemCacheClear();
    GroupFinder.groupCacheClear();
    Hib3AttributeAssignActionDAO.attributeAssignActionCacheClear();
    Hib3AttributeAssignDAO.attributeAssignCacheClear();

    // If the stem name is changing, verify that the new name is not in use.
    // (The new name could be an alternate name).
    if (this.dbVersionDifferentFields().contains(FIELD_NAME)) {
      Stem check = GrouperDAOFactory.getFactory().getStem().findByName(this.getNameDb(), false);
      if (check != null && 
          (!check.getUuid().equals(this.getUuid()) || 
              (this.getAlternateNameDb() != null && 
                  this.getAlternateNameDb().equals(this.getNameDb())))) {
        throw new StemModifyException("Stem with name " + this.getNameDb() + " already exists.");
      }
    }
    
    // If the alternate name is changing, do the following check...
    // If the stem name is not changing OR
    // if the stem name is changing and the alternate name is not the old group name, THEN
    // we need to verify the alternate name isn't already taken.
    if (this.dbVersionDifferentFields().contains(FIELD_ALTERNATE_NAME_DB) &&
        this.getAlternateNameDb() != null) {
      
      String oldName = this.dbVersion().getNameDb();
      if (!this.dbVersionDifferentFields().contains(FIELD_NAME) || 
          (this.dbVersionDifferentFields().contains(FIELD_NAME) && 
              !oldName.equals(this.getAlternateNameDb()))) {
        Stem check = GrouperDAOFactory.getFactory().getStem().findByName(this.getAlternateNameDb(), false);
        if (check != null) {
          throw new StemModifyException("Stem with name " + this.getAlternateNameDb() + " already exists.");
        }
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.STEM, 
        StemHooks.METHOD_STEM_PRE_UPDATE, HooksStemBean.class, 
        this, Stem.class, VetoTypeGrouper.STEM_PRE_UPDATE, false, false);
    
    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.STEM_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(ChangeLogLabels.STEM_UPDATE.id.name(),this.getUuid(), 
            ChangeLogLabels.STEM_UPDATE.name.name(), this.getName(),
            ChangeLogLabels.STEM_UPDATE.parentStemId.name(), this.getParentUuid(),
            ChangeLogLabels.STEM_UPDATE.displayName.name(), this.getDisplayName(),
            ChangeLogLabels.STEM_UPDATE.description.name(), this.getDescription()),
        GrouperUtil.toList(FIELD_NAME, FIELD_PARENT_UUID, FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION),
        GrouperUtil.toList(ChangeLogLabels.STEM_UPDATE.name.name(),
            ChangeLogLabels.STEM_UPDATE.parentStemId.name(), 
            ChangeLogLabels.STEM_UPDATE.description.name(), 
            ChangeLogLabels.STEM_UPDATE.displayExtension.name()));
    
    //if supposed to not have setters do queries
    Boolean inOnPreUpdateBoolean = inOnPreUpdate.get();
    try {
      
      if (inOnPreUpdateBoolean == null || !inOnPreUpdateBoolean) {
        inOnPreUpdate.set(true);
        //check and see what needs to be updated
        boolean nameChange = false;
        boolean displayNameChange = false;
        
        Set<String> dbVersionDifferentFields = Stem.this.dbVersionDifferentFields();
        if (dbVersionDifferentFields.contains(FIELD_EXTENSION) || 
            dbVersionDifferentFields.contains(FIELD_NAME)) {
          nameChange = true;
        }
        if (dbVersionDifferentFields.contains(FIELD_DISPLAY_EXTENSION) ||
            dbVersionDifferentFields.contains(FIELD_DISPLAY_NAME)) {
          displayNameChange = true;
        }
        
        if (nameChange || displayNameChange) {
          // Now iterate through all child groups and stems, renaming each.
          GrouperDAOFactory.getFactory().getStem().renameStemAndChildren(
              Stem.this._renameChildren(nameChange, displayNameChange, Stem.this.setAlternateNameOnMovesAndRenames));
        }
        
        //if its description, just store, we are all good
      }
    } catch (StemModifyException ste) {
      //tunnel checked exceptions
      throw new RuntimeException(ste);
    } finally {
      //if we changed it
      if (inOnPreUpdateBoolean== null || !inOnPreUpdateBoolean) {
        //change it back
        inOnPreUpdate.remove();
      }
    }
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Stem dbVersion() {
    return (Stem)this.dbVersion;
  }

  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
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
  public Stem clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * create stems and parents if not exist.
   * @param stemName
   * @param grouperSession 
   * @param stemDisplayNameForInserts optional, will use this for display name, and not just default to the name.  Note this is
   * only used if creating something, it will not update existing stems
   * @return the resulting stem
   * @throws InsufficientPrivilegeException 
   * @throws StemNotFoundException 
   * @throws StemAddException 
   */
  public static Stem _createStemAndParentStemsIfNotExist(final GrouperSession grouperSession, final String stemName, String stemDisplayNameForInserts)
     throws InsufficientPrivilegeException, StemNotFoundException, StemAddException {
    //note, no need for GrouperSession inverse of control
    String[] stems = StringUtils.split(stemName, ':');
    String[] displayStems = StringUtils.isBlank(stemDisplayNameForInserts) ? null : StringUtils.split(stemDisplayNameForInserts, ':');

    boolean hasDisplayStems = displayStems != null;
    
    if (hasDisplayStems) {
      if (stems.length != displayStems.length) {
        throw new RuntimeException("The length of stems in stem name: " + stems.length + ", " + stemName
            + ", should be the same as the display stems: " + displayStems.length + ", " + stemDisplayNameForInserts);
      }
    }
    
    Stem currentStem = StemFinder.findRootStem(grouperSession);
    String currentName = stems[0];
    for (int i=0;i<stems.length;i++) {
      try {
        currentStem = StemFinder.findByName(grouperSession, currentName, true, new QueryOptions().secondLevelCache(false));
      } catch (StemNotFoundException snfe1) {
        try {
          //this isnt ideal, but just use the extension as the display extension
          currentStem = currentStem.addChildStem(stems[i], hasDisplayStems ? displayStems[i] : stems[i], null, false);
        } catch (RuntimeException e) {
          for (int j=0; j<4; j++) {
            // could a tx need to finish?
            GrouperUtil.sleep(500);
            // see if another thread created it
            currentStem = StemFinder.findByName(grouperSession, currentName, false, new QueryOptions().secondLevelCache(false));
            if (currentName != null) {
              break;
            }
          }
          if (currentStem == null) {
            throw e;
          }
        }
      }
      //increment the name, dont worry if on the last one, we are done
      if (i < stems.length-1) {
        currentName += ":" + stems[i+1];
      }
    }
    Stem parentStem = null;
    //at this point the stem should be there (and is equal to currentStem), just to be sure, query again, do it in a loop
    //in case there are race conditions
    for (int i=0;i<20;i++) {
      
      //try with tx
      parentStem = StemFinder.findByName(grouperSession, stemName, false, new QueryOptions().secondLevelCache(false));
      
      if (parentStem != null) {

        return parentStem;
      }

      parentStem = (Stem)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.NONE, new GrouperTransactionHandler() {
        
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          return StemFinder.findByName(grouperSession, stemName, false, new QueryOptions().secondLevelCache(false));
        }
      });
      
      if (parentStem != null) {
        // if its in another transaction... wait a couple seconds for it to finish
        GrouperUtil.sleep(2000);
        
        return parentStem;
      }

      GrouperUtil.sleep(1000);
    }

    throw new RuntimeException("Cant find stem after creating??? " + stemName);

  }
  
  /**
   * <pre>
   * create or update a stem.  Note this will not move a stem at this time (might in future)
   * 
   * This is a static method since setters to Stem objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the stem by stemNameToEdit (if not there then its an insert)
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the stem (insert or update) if needed
   * 4. Return the stem object
   * 
   * This occurs in a transaction, so if a part of it fails, it rolls back, and potentially
   * rolls back outer transactions too
   * </pre>
   * @param grouperSession to act as
   * @param stemNameToEdit is the name of the stem to edit (or null if insert)
   * @param description new description for stem
   * @param displayExtension display friendly name for this stem only
   * (parent stems are not specified)
   * @param name this is required, and is the full name of the stem
   * including the names of parent stems.  e.g. stem1:stem2:stem3
   * the parent stem must exist unless createParentStemsIfNotExist.  
   * Can rename a stem extension, but not the parent stem name (move)
   * @param uuid of the stem.  uuid for an inserted stem
   * @param saveMode to constrain if insert only or update only, if null defaults to INSERT_OR_UPDATE
   * @param createParentStemsIfNotExist true if the stems should be created if they dont exist, false
   * for StemNotFoundException if not exist.  Note, the display extension on created stems
   * will equal the extension.  This could be dangerous and should probably only be used for testing
   * @return the stem that was updated or created
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException 
   * @throws StemAddException 
   * @throws StemModifyException 
   */
  public static Stem saveStem(final GrouperSession grouperSession, final String stemNameToEdit,
      final String uuid, final String name, final String displayExtension, final String description, 
      SaveMode saveMode, final boolean createParentStemsIfNotExist) 
        throws StemNotFoundException,
      InsufficientPrivilegeException,
      StemAddException, StemModifyException {
  
    StemSave stemSave = new StemSave(grouperSession);

    stemSave.assignStemNameToEdit(stemNameToEdit).assignUuid(uuid);
    stemSave.assignName(name).assignDisplayExtension(displayExtension);
    stemSave.assignDescription(description).assignSaveMode(saveMode);
    stemSave.assignCreateParentStemsIfNotExist(createParentStemsIfNotExist);
    Stem stem = stemSave.save();

    return stem;

  }

  /**
   * Move this stem to another Stem.  If you would like to specify options for the move, 
   * use StemMove instead.  This will use the default options.
   * @param stem 
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void move(Stem stem) throws StemModifyException,
      InsufficientPrivilegeException {
    
    new StemMove(this, stem).save();
  }
  
  /**
   * 
   * @param stem
   * @param assignAlternateName
   * @throws StemModifyException
   * @throws InsufficientPrivilegeException
   */
  protected void internal_move(final Stem stem, final boolean assignAlternateName) throws StemModifyException,
    InsufficientPrivilegeException {

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperSession.validate(GrouperSession.staticGrouperSession());
            
            String oldName = Stem.this.getName();
            
            // cannot move the root stem
            if (Stem.this.isRootStem()) {
              throw new StemModifyException("Cannot move the root stem.");
            }
            
            // the new stem should not be a child of the current stem
            if (stem.getUuid().equals(Stem.this.getUuid()) || 
                Stem.this.isChildStem(stem)) {
              throw new StemModifyException("Cannot move stem. " + stem.getName()
                  + " is the same as or a child of " + Stem.this.getName() + ".");
            }

            // verify that the subject has stem privileges to the stem
            if (!PrivilegeHelper.canStemAdmin(Stem.this, GrouperSession.staticGrouperSession()
                .getSubject())) {
              throw new InsufficientPrivilegeException(E.CANNOT_STEM_ADMIN + ": " + Stem.this.getName());
            }

            // verify that the subject can create stems in the stem where this stem will be moved to.
            if (!PrivilegeHelper.canCreate(GrouperSession.staticGrouperSession(), stem, GrouperSession.staticGrouperSession()
                .getSubject())) {
              throw new InsufficientPrivilegeException(E.CANNOT_CREATE + ": " + stem.getName());
            }
            
            // verify that if the property security.stem.groupAllowedToMoveStem is set,
            // then the user is a member of that group.
            if (!PrivilegeHelper.canMoveStems(GrouperSession.staticGrouperSession().getSubject())) {
              throw new InsufficientPrivilegeException("User cannot move stems.");      
            }
            
            // if moving to the same stem, just return.
            if (stem.getUuid().equals(Stem.this.getParentUuid())) {
              return null;
            }

            Stem.this.setParentUuid(stem.getUuid());
            Stem.this.internal_setModified();
            
            if (stem.isRootStem()) {
              Stem.this.setNameDb(Stem.this.getExtension());
              Stem.this.setDisplayNameDb(Stem.this.getDisplayExtension());
            } else {
              Stem.this.setNameDb(stem.getName() + Stem.DELIM + Stem.this.getExtension());
              Stem.this.setDisplayNameDb(stem.getDisplayName() + Stem.DELIM
                  + Stem.this.getDisplayExtension());
            }
            
            if (assignAlternateName) {
              Stem.this.internal_addAlternateName(oldName);
            }
            
            Stem.this.setAlternateNameOnMovesAndRenames = assignAlternateName;
            
            Stem.this.store();
            
            //if not a smaller operation of a larger auditable call
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {

              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_MOVE,
                  "stemId", Stem.this.getUuid(), "oldStemName", 
                  oldName, "newStemName", Stem.this.getName(), "newParentStemId",
                  stem.getUuid(), 
                  "assignAlternateName", assignAlternateName ? "T" : "F");
              auditEntry.setDescription("Move stem " + oldName + " to name: " + Stem.this.getName()
                  + ", assignAlternateName? " + (assignAlternateName ? "T" : "F")); 
              auditEntry.saveOrUpdate(true);
            }

            
            return null;
          }
        });

    
  }
  
  
  /**
   * Copy this stem to another Stem.
   * @param stem 
   * @param privilegesOfStem Whether to copy privileges of stems
   * @param privilegesOfGroup Whether to copy privileges of groups
   * @param groupAsPrivilege Whether to copy privileges where groups are a member
   * @param listMembersOfGroup Whether to copy the list memberships of groups
   * @param listGroupAsMember Whether to copy list memberships where groups are a member
   * @param attributes Whether to copy attributes
   * @return the new stem
   * @throws StemAddException 
   * @throws InsufficientPrivilegeException 
   *
   */
  protected Stem internal_copy(final Stem stem, final boolean privilegesOfStem,
      final boolean privilegesOfGroup, final boolean groupAsPrivilege,
      final boolean listMembersOfGroup, final boolean listGroupAsMember,
      final boolean attributes) throws StemAddException, InsufficientPrivilegeException {

    return (Stem) HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            
            // cannot copy the root stem
            if (Stem.this.isRootStem()) {
              throw new StemAddException("Cannot copy the root stem.");
            }
            
            // the new stem should not be a child of the current stem
            if (stem.getUuid().equals(Stem.this.getUuid()) || 
                Stem.this.isChildStem(stem)) {
              throw new StemAddException("Cannot copy stem. " + stem.getName()
                  + " is the same as or a child of " + Stem.this.getName() + ".");
            }

            // verify that if the property security.stem.groupAllowedToCopyStem is set,
            // then the user is a member of that group.
            if (!PrivilegeHelper.canCopyStems(GrouperSession.staticGrouperSession().getSubject())) {
              throw new InsufficientPrivilegeException("User cannot copy stems.");      
            }
            
            if (!PrivilegeHelper.canStem(Stem.this, GrouperSession.staticGrouperSession().getSubject())) {
              throw new InsufficientPrivilegeException("Cannot copy stem: " + Stem.this.getName());      
            }
            
            Map<String, Stem> oldStemUuidToNewStem = new HashMap<String, Stem>();
            Map<String, Group> oldGroupUuidToNewGroup = new HashMap<String, Group>();
            Set<Composite> oldComposites = new LinkedHashSet<Composite>();
            
            // now lets copy over the stems
            Stem newStem = stem.internal_addChildStem(GrouperSession
                .staticGrouperSession(), Stem.this.getExtension(),
                Stem.this.getDisplayExtension(), null, false, true);
            
            if (privilegesOfStem) {
              newStem.internal_copyPrivilegesOfStem(GrouperSession
                  .staticGrouperSession().internal_getRootSession(), Stem.this);
            }
            
            oldStemUuidToNewStem.put(Stem.this.getUuid(), newStem);
            
            for (Stem childStem : GrouperDAOFactory.getFactory().getStem()
                    .findAllChildStems(Stem.this, Stem.Scope.SUB, new QueryOptions().sortAsc("name"), false)) {
              Stem newChildStem = oldStemUuidToNewStem.get(childStem.getParentUuid())
                  .internal_addChildStem(GrouperSession
                      .staticGrouperSession().internal_getRootSession(), childStem.getExtension(),
                      childStem.getDisplayExtension(), null, false, true);
              
              if (privilegesOfStem) {
                newChildStem.internal_copyPrivilegesOfStem(GrouperSession
                    .staticGrouperSession().internal_getRootSession(), childStem);
              }
              
              oldStemUuidToNewStem.put(childStem.getUuid(), newChildStem);
            }
            
            // now lets copy over the groups
            for (Group child : GrouperDAOFactory.getFactory().getStem()
                .findAllChildGroups(Stem.this, Stem.Scope.SUB)) {
              Group newChild = child.internal_copy(oldStemUuidToNewStem.get(child
                  .getParentUuid()), privilegesOfGroup, groupAsPrivilege,
                  listMembersOfGroup, listGroupAsMember, attributes, false, false, false, null, null);
              oldGroupUuidToNewGroup.put(child.getUuid(), newChild);
              
              Composite oldComposite = GrouperDAOFactory.getFactory().getComposite()
                  .findAsOwner(child, false);
              if (oldComposite != null) {
                
                oldComposites.add(oldComposite);
              }
            }
      
            
            // need to take care of composites....
            Iterator<Composite> oldCompositesIter = oldComposites.iterator();
            while (oldCompositesIter.hasNext()) {
              Composite oldComposite = oldCompositesIter.next();
              String oldOwnerUuid = oldComposite.getFactorOwnerUuid();
              String oldLeftUuid = oldComposite.getLeftFactorUuid();
              String oldRightUuid = oldComposite.getRightFactorUuid();

              Group newCompositeOwnerGroup = oldGroupUuidToNewGroup.get(oldOwnerUuid);
              Group newCompositeLeftGroup = oldGroupUuidToNewGroup.get(oldLeftUuid);
              Group newCompositeRightGroup = oldGroupUuidToNewGroup.get(oldRightUuid);

              // if the factors aren't part of the stem being moved, 
              // we'll create the composite using the old factors...
              // maybe the way this works should be configurable? 
              if (newCompositeLeftGroup == null || newCompositeRightGroup == null) {
                try {
                  newCompositeLeftGroup = oldComposite.getLeftGroup();
                  newCompositeRightGroup = oldComposite.getRightGroup();
                } catch (GroupNotFoundException gnfe) {
                  if (LOG.isDebugEnabled()) {
                    LOG.debug("Not allowed to see composite factor of owner in stem copy: " + newCompositeOwnerGroup.getName() );
                  }
                  //not allowed to view left or right?  skip it
                  continue;
                }
              }

              newCompositeOwnerGroup.internal_addCompositeMember(GrouperSession
                  .staticGrouperSession().internal_getRootSession(), oldComposite
                  .getType(), newCompositeLeftGroup, newCompositeRightGroup, null);
            }

            //if not a smaller operation of a larger auditable call
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_COPY,
                  "oldStemId", Stem.this.getUuid(), "oldStemName", 
                  Stem.this.getName(), "newStemName", newStem.getName(), "newStemId",
                  newStem.getUuid(), 
                  "privilegesOfStem", privilegesOfStem ? "T" : "F", "privilegesOfGroup",
                      privilegesOfGroup ? "T" : "F", "listMembersOfGroup",
                  listMembersOfGroup ? "T" : "F", "listGroupAsMember",
                  listGroupAsMember ? "T" : "F");
              auditEntry.setInt01(attributes ? 1L : 0L);
              auditEntry.setDescription("Copy stem " 
                  + Stem.this.getName() + " to name: " + newStem.getName()
                  + ", privilegesOfStem? " + (privilegesOfStem ? "T" : "F")
                  + ", privilegesOfGroup? " + (privilegesOfGroup ? "T" : "F")
                  + ", groupAsPrivilege? " + (groupAsPrivilege ? "T" : "F") 
                  + ", listMembersOfGroup? " + (listMembersOfGroup ? "T" : "F") 
                  + ", listGroupAsMember? " + (listGroupAsMember ? "T" : "F") 
                  + ", attributes? " + (attributes ? "T" : "F")); 
              auditEntry.saveOrUpdate(true);
            }

            
            return newStem;
          }
        });
  }


  /**
   * Copy this stem to another Stem.  If you want to specify options
   * for the copy, use StemCopy.  This will use the default options.
   * @param stem
   * @return the new stem
   * @throws StemAddException 
   * @throws InsufficientPrivilegeException 
   */
  public Stem copy(Stem stem) throws StemAddException, InsufficientPrivilegeException {
    StemCopy stemCopy = new StemCopy(this, stem);
    return stemCopy.save();  
  } 
  
  /**
   * 
   * @param session
   * @param stem
   * @throws UnableToPerformException
   */
  private void internal_copyPrivilegesOfStem(GrouperSession session, Stem stem)
      throws UnableToPerformException {
    Set<Privilege> privileges = Privilege.getNamingPrivs();
  
    Iterator<Privilege> iter = privileges.iterator();
    while (iter.hasNext()) {
      Privilege priv = iter.next();
      session.getNamingResolver().privilegeCopy(stem, this, priv);      
    }  
  }
  
  /**
   * when the last member has changed, used by hibernate
   */
  private Long lastMembershipChangeDb;

  /** id of the group as a unique integer */
  private Long idIndex;
  
  /**
   * when the last member has changed, used by hibernate
   * @return when
   */
  public Long getLastMembershipChangeDb() {
    return this.lastMembershipChangeDb;
  }
  
  /**
   * when the last member has changed, used by hibernate
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

  // PUBLIC INSTANCE METHODS //
  
  // PUBLIC INSTANCE METHODS //
  
  /**
   * Add a new role to the registry.
   * <pre class="eg">
   * // Add a role with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildRole("edu", "edu domain");
   * }
   * catch (GroupAddException eGA) {
   *   // Group not added
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add group
   * }
   * </pre>
   * @param   extension         Role extension
   * @param   displayExtension  Role displayExtension
   * @return  The added {@link Role}
   * @throws  GroupAddException 
   * @throws  InsufficientPrivilegeException
   */
  public Role addChildRole(final String extension, final String displayExtension) 
    throws  GroupAddException,
            InsufficientPrivilegeException  {
    return internal_addChildRole(extension, displayExtension, null);
  }

  /**
   * Add a new role to the registry.
   * <pre class="eg">
   * // Add a role with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildRole("edu", "edu domain");
   * }
   * catch (GroupAddException eGA) {
   *   // Group not added
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add group
   * }
   * </pre>
   * @param   extension         Role extension
   * @param   displayExtension  Role displayExtension
   * @param uuid is uuid or null if generated
   * @return  The added {@link Role}
   * @throws  GroupAddException 
   * @throws  InsufficientPrivilegeException
   */
  public Role internal_addChildRole(final String extension, final String displayExtension, final String uuid) 
    throws  GroupAddException,
            InsufficientPrivilegeException  {
    
    return (Group)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

        //note, roles are modeled as groups
        Group group = Stem.this.internal_addChildGroup(extension, displayExtension, uuid, TypeOfGroup.role);
        
        RoleSet roleSet = new RoleSet();
        roleSet.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
        roleSet.setDepth(0);
        roleSet.setIfHasRoleId(group.getId());
        roleSet.setThenHasRoleId(group.getId());
        roleSet.setType(RoleHierarchyType.self);
        roleSet.setParentRoleSetId(roleSet.getId());
        roleSet.saveOrUpdate();
        
        return group;
      }
      
    });
      
  }

  /**
   * Add a new role to the registry.
   * <pre class="eg">
   * // Add a role with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildEntity("edu", "edu domain");
   * }
   * catch (GroupAddException eGA) {
   *   // Group not added
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add group
   * }
   * </pre>
   * @param   extension         Entity extension
   * @param   displayExtension  Entity displayExtension
   * @param uuid is uuid or null if generated
   * @return  The added {@link Role}
   * @throws  GroupAddException 
   * @throws  InsufficientPrivilegeException
   */
  public Role internal_addChildEntity(final String extension, final String displayExtension, final String uuid) 
    throws  GroupAddException,
            InsufficientPrivilegeException  {
    
    return (Group)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

        //note, roles are modeled as groups
        Group group = Stem.this.internal_addChildGroup(extension, displayExtension, uuid, TypeOfGroup.entity);
        
        return group;
      }
      
    });
      
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Stem other) {
    
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
    if (!StringUtils.equals(this.uuid, other.uuid)) {
      return true;
    }

    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Stem other) {
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
    if (!GrouperUtil.equals(this.lastMembershipChangeDb, other.lastMembershipChangeDb)) {
      return true;
    }
    if (!StringUtils.equals(this.modifierUUID, other.modifierUUID)) {
      return true;
    }
    if (this.modifyTime != other.modifyTime) {
      return true;
    }
    return false;

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Stem existingRecord) {
    existingRecord.setAlternateNameDb(this.alternateNameDb);
    existingRecord.setDescriptionDb(this.getDescriptionDb());
    existingRecord.setDisplayExtensionDb(this.getDisplayExtensionDb());
    existingRecord.setDisplayNameDb(this.getDisplayNameDb());
    existingRecord.setExtensionDb(this.getExtensionDb());
    existingRecord.setNameDb(this.getNameDb());
    existingRecord.setParentUuid(this.getParentUuid());
    existingRecord.setUuid(this.getUuid());
  }



  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public Stem xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getStem().findByUuidOrName(this.uuid, this.name, false,
        new QueryOptions().secondLevelCache(false));
  }

  /**
   * assign different id index
   * @param theIdIndex
   * @return if it was changed
   */
  public boolean assignIdIndex(final long theIdIndex) {
    
    TableIndex.assertCanAssignIdIndex();

    boolean needsSave = false;
    
    synchronized (TableIndexType.stem) {

      //ok, if the index is not in use (not, it could be reserved... hmmm)
      Stem tempStem = GrouperDAOFactory.getFactory().getStem().findByIdIndex(theIdIndex, false, null);
      if (tempStem == null) {
        
        this.setIdIndex(theIdIndex);
        TableIndex.clearReservedId(TableIndexType.stem, theIdIndex);
        needsSave = true;
        
        //do a new session so we don hold on too long
        HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //now we might need to increment the index
            TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().findByType(TableIndexType.stem);
            if (tableIndex != null && tableIndex.getLastIndexReserved() < theIdIndex) {
              tableIndex.setLastIndexReserved(theIdIndex);
              tableIndex.saveOrUpdate();
            }
            return null;
          }
        });
      }      
    }
    return needsSave;
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Stem xmlSaveBusinessProperties(Stem existingRecord) {

    //if its an insert, call the business method
    if (existingRecord == null) {
      if (this.isRootStem()) {
        throw new RuntimeException("Why is there no root stem???");
      }
      Stem parent = this.getParentStem();
      existingRecord = parent.internal_addChildStem(GrouperSession.staticGrouperSession(), this.extension, this.displayExtension, this.uuid, false, true);

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
    
    GrouperDAOFactory.getFactory().getStem().saveUpdateProperties(this);
    
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportStem xmlToExportStem(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }

    XmlExportStem xmlExportStem = new XmlExportStem();
    
    xmlExportStem.setAlternateName(this.getAlternateNameDb());
    xmlExportStem.setContextId(this.getContextId());
    xmlExportStem.setCreateTime(GrouperUtil.dateStringValue(this.getCreateTime()));
    xmlExportStem.setCreatorId(this.getCreatorUuid());
    xmlExportStem.setDescription(this.getDescriptionDb());
    xmlExportStem.setDisplayExtension(this.getDisplayExtensionDb());
    xmlExportStem.setDisplayName(this.getDisplayNameDb());
    xmlExportStem.setExtension(this.getExtensionDb());
    xmlExportStem.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportStem.setIdIndex(this.getIdIndex());
    //TODO make string
    xmlExportStem.setLastMembershipChange(this.getLastMembershipChangeDb());
    xmlExportStem.setModifierId(this.getModifierUuid());
    xmlExportStem.setModifierTime(GrouperUtil.dateStringValue(this.getModifyTime()));
    xmlExportStem.setName(this.getNameDb());
    xmlExportStem.setParentStem(this.getParentUuid());
    xmlExportStem.setUuid(this.getUuid());
    return xmlExportStem;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setUuid(theId);
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    return "Stem: " + this.uuid + ", " + this.name;
  }

  /**
   * obliterate this stem name (which might not exist) from point in time
   * @param stemName
   * @param printOutput
   */
  public static void obliterateFromPointInTime(String stemName, final boolean printOutput) {
    Stem ns = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, false);
    obliterateFromPointInTimeHelper(stemName, ns, printOutput);
  }

  /**
   * obliterate this stem name (which might not exist) from point in time
   * @param stemName
   * @param printOutput
   * @param testOnly 
   * @param deletePointInTime 
   */
  public static void obliterate(String stemName,final boolean printOutput, boolean testOnly, boolean deletePointInTime) {
    Stem ns = StemFinder.findByName(GrouperSession.staticGrouperSession(), stemName, false);
    if (ns != null) {
      ns.obliterate(printOutput, testOnly);
    }
    if (!testOnly && deletePointInTime) {
      obliterateFromPointInTimeHelper(stemName, ns, printOutput);
    }
  }

  /**
   * obliterate this stem name (which might not exist) from point in time
   * @param stemName
   * @param printOutput
   * @param ns
   */
  private static void obliterateFromPointInTimeHelper(String stemName,Stem ns,final boolean printOutput) {

    //make sure something in changelog temp
    if (HibernateSession.bySqlStatic().select(int.class, "SELECT count(1) FROM grouper_change_log_entry_temp") > 0) {
      
      //see if we are making progress
      long maxCreatedOn = HibernateSession.bySqlStatic().select(long.class, "SELECT max(created_on) FROM grouper_change_log_entry_temp");
      int changeLogEntryCount = HibernateSession.bySqlStatic().select(int.class, "SELECT count(1) FROM grouper_change_log_entry_temp where created_on <= ?", 
          GrouperUtil.toListObject(maxCreatedOn), HibUtils.listType(LongType.INSTANCE));

      int loops = 0;
      
      OUTER:
      while (true) {
        if (ns != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(ns.getUuid(), false);
          if (pitStem != null && !pitStem.isActive()) {
            break;
          }
        } else {
          Set<PITStem> pitStems = GrouperDAOFactory.getFactory().getPITStem().findByName(stemName, false);
          if (pitStems.size() > 0 && !pitStems.iterator().next().isActive()) {
            break;
          } 
        }
        
        if (printOutput) {
          System.out.println("Waiting for Grouper Daemon to process before obliterating from point in time data. "
              + " This is expected to take a few minutes.  Be sure the Grouper Daemon is running.");
        }
        GrouperUtil.sleep(15000);

        int hasCreatedOn = HibernateSession.bySqlStatic().select(int.class, 
            "SELECT count(1) FROM grouper_change_log_entry_temp where created_on = ?", 
            GrouperUtil.toListObject(maxCreatedOn), HibUtils.listType(LongType.INSTANCE));
        
        //nothing to do
        if (hasCreatedOn == 0) {
          break;
        }
        
        int currentChangeLogEntryCount = HibernateSession.bySqlStatic().select(int.class, 
            "SELECT count(1) FROM grouper_change_log_entry_temp where created_on <= ?", 
            GrouperUtil.toListObject(maxCreatedOn), HibUtils.listType(LongType.INSTANCE));
          
        int loopsLimit = testingRunChangeLogSooner ? 2 : 100;
        
        if (loops > loopsLimit && changeLogEntryCount == currentChangeLogEntryCount) {
          //  daemon is not running, run the temp to change log
          int recordsChanged = ChangeLogTempToEntity.convertRecords();
          while (recordsChanged > 0) {
            hasCreatedOn = HibernateSession.bySqlStatic().select(int.class, 
                "SELECT count(1) FROM grouper_change_log_entry_temp where created_on = ?", 
                GrouperUtil.toListObject(maxCreatedOn), HibUtils.listType(LongType.INSTANCE));
            if (hasCreatedOn == 0) {
              break OUTER;
            }
            recordsChanged = ChangeLogTempToEntity.convertRecords();
          }
          break;
        }
        loops++;
      }
    }      
    PITUtils.deleteInactiveStem(stemName, printOutput);

  }
  
  /**
   * run the change log sooner for test
   */
  public static boolean testingRunChangeLogSooner = false;
  
  /**
   * Delete this stem from the Groups Registry including all sub objects.
   * @param printOutput
   * @param testOnly
   * @param deleteFromPointInTime needs to wait for change log entries to be processed, then delete those too
   */
  public void obliterate(final boolean printOutput, final boolean testOnly, final boolean deleteFromPointInTime) {

    this.obliterate(printOutput, testOnly);
    
    if (!testOnly && deleteFromPointInTime) {
      obliterateFromPointInTimeHelper(this.getName(), this, printOutput);
    }

  }

  /**
   * Delete this stem from the Groups Registry including all sub objects.
   * @param printOutput 
   * @param testOnly 
   * @throws  InsufficientPrivilegeException
   * @throws  StemDeleteException
   */
  public void obliterate(final boolean printOutput, final boolean testOnly) throws InsufficientPrivilegeException, StemDeleteException {
    
    if (printOutput) {
      if (testOnly) {
        System.out.println("Would obliterate stem: " + this.getName());
      } else {
        System.out.println("Obliterating stem: " + this.getName());
      }
    }

    StemObliterateResults stemObliterateResults = new StemObliterateResults();
    stemObliterateResultsThreadLocal.set(stemObliterateResults);

    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.obliterate.stem.in.transaction", false)) {
      
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
    
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
    
              obliterateHelper(printOutput, testOnly, hibernateHandlerBean);
              
              return null;
           }
      });
    } else {
      obliterateHelper(printOutput, testOnly, null);
    }
  
    if (printOutput) {
      if (testOnly) {
        System.out.println("Would be done obliterating stem: " + this.getName());
      } else {
        System.out.println("Done obliterating stem: " + this.getName());
      }
    }    
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
   * @see GrouperObject#matchesLowerSearchStrings(Set)
   */
  @Override
  public boolean matchesLowerSearchStrings(Set<String> filterStrings) {

    if (GrouperUtil.length(filterStrings) == 0) {
      return true;
    }

    String lowerId = this.getId().toLowerCase();
    String lowerName = StringUtils.defaultString(this.getName()).toLowerCase();
    String lowerDisplayName = StringUtils.defaultString(this.getDisplayName()).toLowerCase();
    String lowerDescription = StringUtils.defaultString(this.getDescription()).toLowerCase();
    String lowerAlternateName = StringUtils.defaultString(this.getAlternateName()).toLowerCase();
    
    for (String filterString : GrouperUtil.nonNull(filterStrings)) {
      
      //if all dont match, return false
      if (!lowerId.contains(filterString)
          && !lowerName.contains(filterString)
          && !lowerDisplayName.contains(filterString)
          && !lowerDescription.contains(filterString)
          && !lowerAlternateName.contains(filterString)) {
        return false;
      }
      
    }
    return true;
  }

  /**
   * see if the subject has a privilege or another privilege that implies this privilege.
   * @param subject
   * @param privilegeOrListName
   * @param secure if the user must be an admin to check
   * @return true if has privilege
   */
  public boolean canHavePrivilege(Subject subject, String privilegeOrListName, boolean secure) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    if (secure) {
      PrivilegeHelper.dispatch(grouperSession, this, grouperSession.getSubject(), NamingPrivilege.STEM_ADMIN);
    }
    
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM.getListName())
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ADMIN.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ADMIN.getListName())) {
      return PrivilegeHelper.canStemAdmin(grouperSession, this, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.CREATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.CREATE.getListName())) {
      return PrivilegeHelper.canCreate(grouperSession, this, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_READ.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_READ.getListName())) {
      return PrivilegeHelper.canStemAttrRead(grouperSession, this, subject);
    }
    if (StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_UPDATE.getName()) 
        || StringUtils.equalsIgnoreCase(privilegeOrListName, NamingPrivilege.STEM_ATTR_UPDATE.getListName())) {
      return PrivilegeHelper.canStemAttrUpdate(grouperSession, this, subject);
    }
    throw new RuntimeException("Cant find privilege: '" + privilegeOrListName + "'");
  
  }

  /**
   * grant privs to stem
   * @param subject to add
   * @param createChecked
   * @param stemAdminChecked
   * @param attrReadChecked
   * @param attrUpdateChecked
   * @param revokeIfUnchecked
   * @return if something was changed
   */
  public boolean grantPrivs(final Subject subject,
      final boolean stemAdminChecked,
      final boolean createChecked, final boolean attrReadChecked,
      final boolean attrUpdateChecked, final boolean revokeIfUnchecked) {
    
    return (Boolean)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
  
        boolean hadChange = false;
        
        //see if add or remove
        if (stemAdminChecked) {
          hadChange = hadChange | Stem.this.grantPriv(subject, NamingPrivilege.STEM_ADMIN, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | Stem.this.revokePriv(subject, NamingPrivilege.STEM_ADMIN, false);
          }
        }

        //see if add or remove
        if (createChecked) {
          hadChange = hadChange | Stem.this.grantPriv(subject, NamingPrivilege.CREATE, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | Stem.this.revokePriv(subject, NamingPrivilege.CREATE, false);
          }
        }

        //see if add or remove
        if (attrReadChecked) {
          hadChange = hadChange | Stem.this.grantPriv(subject, NamingPrivilege.STEM_ATTR_READ, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | Stem.this.revokePriv(subject, NamingPrivilege.STEM_ATTR_READ, false);
          }
        }

        //see if add or remove
        if (attrUpdateChecked) {
          hadChange = hadChange | Stem.this.grantPriv(subject, NamingPrivilege.STEM_ATTR_UPDATE, false);
        } else {
          if (revokeIfUnchecked) {
            hadChange = hadChange | Stem.this.revokePriv(subject, NamingPrivilege.STEM_ATTR_UPDATE, false);
          }
        }

        return hadChange;
      }
    });
  
    
  }

  /**
   * counts when obliterating or seeing if can obliterate
   */
  public static class StemObliterateResults {

    /**
     * stem count total (even ones not allowed to delete)
     */
    private int stemCountTotal;
    
    /**
     * @return the stemCountTotal
     */
    public int getStemCountTotal() {
      return this.stemCountTotal;
    }
    
    /**
     * @param stemCountTotal1 the stemCountTotal to set
     */
    public void setStemCountTotal(int stemCountTotal1) {
      this.stemCountTotal = stemCountTotal1;
    }

    /**
     * group count total (even ones not allowed to delete)
     */
    private int groupCountTotal;
    
    /**
     * group count total (even ones not allowed to delete)
     * @return the groupCountTotal
     */
    public int getGroupCountTotal() {
      return this.groupCountTotal;
    }

    /**
     * group count total (even ones not allowed to delete)
     * @param groupCountTotal1 the groupCountTotal to set
     */
    public void setGroupCountTotal(int groupCountTotal1) {
      this.groupCountTotal = groupCountTotal1;
    }

    /**
     * attribute def count total (even ones arent allowed to delete)
     */
    private int attributeDefCountTotal;
    
    /**
     * attribute def count total (even ones arent allowed to delete)
     * @return the attributeDefCountTotal
     */
    public int getAttributeDefCountTotal() {
      return this.attributeDefCountTotal;
    }
    
    /**
     * attribute def count total (even ones arent allowed to delete)
     * @param attributeDefCountTotal the attributeDefCountTotal to set
     */
    public void setAttributeDefCountTotal(int attributeDefCountTotal) {
      this.attributeDefCountTotal = attributeDefCountTotal;
    }

    /**
     * attribute def name count total objects (even ones you arent allowed to delete)
     */
    private int attributeDefNameCountTotal;
    
    
    /**
     * attribute def name count total objects (even ones you arent allowed to delete)
     * @return the attributeDefNameCountTotal
     */
    public int getAttributeDefNameCountTotal() {
      return this.attributeDefNameCountTotal;
    }
    
    /**
     * attribute def name count total objects (even ones you arent allowed to delete)
     * @param attributeDefNameCountTotal1 the attributeDefNameCountTotal to set
     */
    public void setAttributeDefNameCountTotal(int attributeDefNameCountTotal1) {
      this.attributeDefNameCountTotal = attributeDefNameCountTotal1;
    }

    /**
     * stem count
     */
    private int stemCount;
    
    /**
     * stem count
     * @return the stemCount
     */
    public int getStemCount() {
      return this.stemCount;
    }
    
    /**
     * stem count
     * @param stemCount1 the stemCount to set
     */
    public void setStemCount(int stemCount1) {
      this.stemCount = stemCount1;
    }
    
    /**
     * group count
     */
    private int groupCount;
    
    /**
     * group count
     * @return the groupCount
     */
    public int getGroupCount() {
      return this.groupCount;
    }
    
    /**
     * group count
     * @param groupCount1 the groupCount to set
     */
    public void setGroupCount(int groupCount1) {
      this.groupCount = groupCount1;
    }
    
    /** attribute def count */
    private int attributeDefCount;


    
    /**
     * attribute def count
     * @return the attributeDefCount
     */
    public int getAttributeDefCount() {
      return this.attributeDefCount;
    }


    
    /**
     * attribute def count
     * @param attributeDefCount the attributeDefCount to set
     */
    public void setAttributeDefCount(int attributeDefCount) {
      this.attributeDefCount = attributeDefCount;
    }
    
    /**
     * attribute def name count
     */
    private int attributeDefNameCount;


    
    /**
     * attribute def name count
     * @return the attributeDefNameCount
     */
    public int getAttributeDefNameCount() {
      return this.attributeDefNameCount;
    }


    
    /**
     * attribute def name count
     * @param attributeDefNameCount1 the attributeDefNameCount to set
     */
    public void setAttributeDefNameCount(int attributeDefNameCount1) {
      this.attributeDefNameCount = attributeDefNameCount1;
    }

  }

  /**
   * keep results for obliterate
   */
  private static ThreadLocal<StemObliterateResults> stemObliterateResultsThreadLocal = new InheritableThreadLocal<StemObliterateResults>();
  
  /**
   * 
   * @return obliterate results
   */
  public static StemObliterateResults retrieveObliterateResults() {
    return stemObliterateResultsThreadLocal.get();
  }
  
  /**
   * @param printOutput
   * @param testOnly
   * @param hibernateHandlerBean
   */
  private void obliterateHelper(final boolean printOutput, final boolean testOnly,
      HibernateHandlerBean hibernateHandlerBean) {
    
    if (hibernateHandlerBean != null) {
      hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
    }
    
    StemObliterateResults stemObliterateResults = retrieveObliterateResults();

    {
      //obliterate all child stems
      Set<Stem> stems = GrouperDAOFactory.getFactory().getStem().findAllChildStems(Stem.this, Scope.ONE);
  
      // done below
      //stemObliterateResults.setStemCount(stemObliterateResults.getStemCount() + GrouperUtil.length(stems));
      
      for (Stem stem : GrouperUtil.nonNull(stems)) {
        stem.obliterateHelper(printOutput, testOnly, hibernateHandlerBean);
      }
    }
    
    {
      Set<Group> groups = deleteGroups(printOutput, testOnly, Scope.ONE);
  
      stemObliterateResults.setGroupCount(stemObliterateResults.getGroupCount() + GrouperUtil.length(groups));
    }
    
    {
      Set<AttributeDefName> attributeDefNames = deleteAttributeDefNames(printOutput, testOnly, Scope.ONE);
  
      stemObliterateResults.setAttributeDefNameCount(stemObliterateResults.getAttributeDefNameCount() + GrouperUtil.length(attributeDefNames));
    }
    
    {
      Set<AttributeDef> attributeDefs = deleteAttributeDefs(printOutput, testOnly, Scope.ONE);
  
      stemObliterateResults.setAttributeDefCount(stemObliterateResults.getAttributeDefCount() + GrouperUtil.length(attributeDefs));
    }
    
    //delete stem
    if (!testOnly) {
      Stem.this.delete();
    }
    stemObliterateResults.setStemCount(stemObliterateResults.getStemCount() + 1);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperSession.startRootSession();
    GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findById("test.subject.0", true));
    
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignParentStemId(stem.getUuid()).assignStemScope(Scope.SUB)
    .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES)
    .assignQueryOptions(queryOptions).findAttributeNames();
    System.out.println(queryOptions.getCount());
    System.out.println(GrouperUtil.length(attributeDefNames));
  }
  
  /**
   * 
   * @return if this stem is empty i.e. can be deleted
   */
  public boolean isEmpty() {
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Set<Stem> stems = GrouperDAOFactory.getFactory().getStem().findAllChildStems(Stem.this, Scope.ONE);
        if (GrouperUtil.length(stems) > 0) {
          return false;
        }
        Set<Group> groups = GrouperDAOFactory.getFactory().getStem().findAllChildGroups(Stem.this, Scope.ONE);
        if (GrouperUtil.length(groups) > 0) {
          return false;
        }
        Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignParentStemId(Stem.this.uuid).assignStemScope(Scope.ONE)
              .findAttributeNames();
        if (GrouperUtil.length(attributeDefNames) > 0) {
          return false;
        }
        Set<AttributeDef> attributeDefs = new AttributeDefFinder().assignParentStemId(Stem.this.uuid).assignStemScope(Scope.ONE)
              .findAttributes();
        if (GrouperUtil.length(attributeDefs) > 0) {
          return false;
        }
        return true;
      }
    });
  }
  
  /**
   * see if the current session can obliterate.  also setup counts of object types
   * @return true if can obliterate
   */
  public boolean isCanObliterate() {

    final StemObliterateResults stemObliterateResults = new StemObliterateResults();
    stemObliterateResultsThreadLocal.set(stemObliterateResults);

    Subject subject = GrouperSession.staticGrouperSession().getSubject();
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    new AttributeDefFinder().assignParentStemId(this.uuid).assignStemScope(Scope.SUB)
        .assignSubject(subject)
        .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES)
        .assignQueryOptions(queryOptions).findAttributes();
    stemObliterateResults.setAttributeDefCount(GrouperUtil.intValue(queryOptions.getCount(), -1));
    
    queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    new AttributeDefNameFinder().assignParentStemId(this.uuid).assignStemScope(Scope.SUB)
        .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES)
        .assignSubject(subject)
        .assignQueryOptions(queryOptions).findAttributeNames();
    stemObliterateResults.setAttributeDefNameCount(GrouperUtil.intValue(queryOptions.getCount(), -1));
    
    queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    new GroupFinder().assignStemScope(Scope.SUB).assignParentStemId(this.uuid)
        .assignPrivileges(AccessPrivilege.ADMIN_PRIVILEGES)
        .assignQueryOptions(queryOptions).findGroups();
    stemObliterateResults.setGroupCount(GrouperUtil.intValue(queryOptions.getCount(), -1));

    queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    new StemFinder().assignStemScope(Scope.SUB).assignParentStemId(this.uuid)
        .assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
        .assignQueryOptions(queryOptions).findStems();
    stemObliterateResults.setStemCount(GrouperUtil.intValue(queryOptions.getCount(), -1));
    if (stemObliterateResults.getStemCount() >= 0 && this.canHavePrivilege(subject, NamingPrivilege.STEM_ADMIN.toString(), false)) {
      // add one for parent folder
      stemObliterateResults.setStemCount(stemObliterateResults.getStemCount()+1);
    }
    
    boolean isWheelOrRoot = PrivilegeHelper.isWheelOrRoot(subject);
    //TODO: if an inheritable admin then yes

    if (isWheelOrRoot) {
      stemObliterateResults.setAttributeDefCountTotal(stemObliterateResults.getAttributeDefCount());
      stemObliterateResults.setAttributeDefNameCountTotal(stemObliterateResults.getAttributeDefNameCount());
      stemObliterateResults.setStemCountTotal(stemObliterateResults.getStemCount());
      stemObliterateResults.setGroupCountTotal(stemObliterateResults.getGroupCount());
      return true;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        Subject subject = SubjectFinder.findRootSubject();
        QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
        new AttributeDefFinder().assignParentStemId(Stem.this.uuid).assignStemScope(Scope.SUB)
            .assignQueryOptions(queryOptions).findAttributes();
        stemObliterateResults.setAttributeDefCountTotal(GrouperUtil.intValue(queryOptions.getCount(), -1));
        
        queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
        new AttributeDefNameFinder().assignParentStemId(Stem.this.uuid).assignStemScope(Scope.SUB)
            .assignQueryOptions(queryOptions).findAttributeNames();
        stemObliterateResults.setAttributeDefNameCountTotal(GrouperUtil.intValue(queryOptions.getCount(), -1));
        
        queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
        new GroupFinder().assignStemScope(Scope.SUB).assignParentStemId(Stem.this.uuid)
            .assignQueryOptions(queryOptions).findGroups();
        stemObliterateResults.setGroupCountTotal(GrouperUtil.intValue(queryOptions.getCount(), -1));

        queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
        new StemFinder().assignStemScope(Scope.SUB).assignParentStemId(Stem.this.uuid)
            .assignQueryOptions(queryOptions).findStems();
        stemObliterateResults.setStemCountTotal(GrouperUtil.intValue(queryOptions.getCount(), -1));

        if (stemObliterateResults.getStemCount() >= 0) {
          // add one for parent folder
          stemObliterateResults.setStemCountTotal(stemObliterateResults.getStemCountTotal()+1);
        }

        return null;
      }
    });

    //see if counts match
    if (stemObliterateResults.getAttributeDefCount() != stemObliterateResults.getAttributeDefCountTotal()) {
      return false;
    }
    if (stemObliterateResults.getAttributeDefNameCount() != stemObliterateResults.getAttributeDefNameCountTotal()) {
      return false;
    }
    if (stemObliterateResults.getStemCount() != stemObliterateResults.getStemCountTotal()) {
      return false;
    }
    if (stemObliterateResults.getGroupCount() != stemObliterateResults.getGroupCountTotal()) {
      return false;
    }
    return true;
    
    
  }
  
  /**
   * @param printOutput
   * @param testOnly
   * @param scope
   * @return attribute defs to be deleted or would be deleted
   */
  public Set<AttributeDef> deleteAttributeDefs(final boolean printOutput, final boolean testOnly, Scope scope) {

    Set<AttributeDef> attributeDefs = new AttributeDefFinder().assignParentStemId(this.uuid).assignStemScope(scope)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES).findAttributes();
    
    Set<AttributeDef> deletedObjects = new HashSet<AttributeDef>();

    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      
      deletedObjects.add(attributeDef);

      if (!testOnly) {
        attributeDef.delete();
      }
      if (printOutput) {
        if (testOnly) {
          System.out.println("Would be done deleting attributeDef: " + attributeDef.getName());
        } else {
          System.out.println("Done deleting attributeDef: " + attributeDef.getName());
        }
      }              
    }
    return deletedObjects;

  }

  /**
   * @param printOutput
   * @param testOnly
   * @param scope
   * @return attribute def names deleted or to be deleted
   */
  public Set<AttributeDefName> deleteAttributeDefNames(final boolean printOutput, final boolean testOnly, Scope scope) {

    Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignParentStemId(this.uuid).assignStemScope(scope)
      .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES).assignSubject(GrouperSession.staticGrouperSession().getSubject()).findAttributeNames();
    
    Set<AttributeDefName> deletedObjects = new HashSet<AttributeDefName>();

    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      deletedObjects.add(attributeDefName);
      if (!testOnly) {
        attributeDefName.delete();
      }
      if (printOutput) {
        if (testOnly) {
          System.out.println("Would be done deleting attributeDefName: " + attributeDefName.getName());
        } else {
          System.out.println("Done deleting attributeDefName: " + attributeDefName.getName());
        }
      }              
    }
    return deletedObjects;
  }

  /**
   * @param printOutput
   * @param testOnly
   * @param scope 
   * @return groups to be deleted or would be deleted
   */
  public Set<Group> deleteGroups(final boolean printOutput, final boolean testOnly, Scope scope) {
    //delete all objects
    //groups
    Set<Group> groups = new GroupFinder().assignStemScope(scope).assignParentStemId(this.uuid)
        .assignPrivileges(AccessPrivilege.ADMIN_PRIVILEGES).findGroups();
    
    Set<Group> deletedObjects = new HashSet<Group>();
    
    //pass one for composites since if you delete a factor first it bombs...
    for (int i : new int[]{0,1}) {
      Iterator<Group> groupIterator = groups.iterator();
      while (groupIterator.hasNext()) {
        Group group = groupIterator.next();
        
        if (i==0 && !group.isHasComposite()) {
          continue;
        }
        
        deletedObjects.add(group);
        
        if (!testOnly) {
          group.delete();
        }
        if (printOutput) {
          if (testOnly) {
            System.out.println("Would be done deleting " + group.getTypeOfGroup() + ": " + group.getName());
          } else {
            System.out.println("Done deleting " + group.getTypeOfGroup() + ": " + group.getName());
          }
        }
        groupIterator.remove();
      }
    }
    
    return deletedObjects;
    
  }

  /**
   * @param printOutput
   * @param testOnly
   * @param scope 
   * @return groups have memberships deleted or would be deleted
   */
  public Set<Group> deleteGroupMemberships(boolean printOutput, final boolean testOnly, Scope scope) {
    //delete all objects
    //groups
    Set<Group> groups = new GroupFinder().assignStemScope(scope).assignParentStemId(this.uuid)
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES).addTypeOfGroup(TypeOfGroup.group).addTypeOfGroup(TypeOfGroup.role).findGroups();
    
    Set<Group> deletedObjects = new HashSet<Group>();
    
    //pass one for composites since if you delete a factor first it bombs...
    Iterator<Group> groupIterator = groups.iterator();
    while (groupIterator.hasNext()) {
      Group group = groupIterator.next();
      
      deletedObjects.add(group);
      
      if (!testOnly) {
        group.deleteAllMemberships();
      }
      if (printOutput) {
        if (testOnly) {
          System.out.println("Would be done deleting memberships from " + group.getTypeOfGroup() + ": " + group.getName());
        } else {
          System.out.println("Done deleting memberships from " + group.getTypeOfGroup() + ": " + group.getName());
        }
      }
      groupIterator.remove();
    }
    
    return deletedObjects;
    
  }

  /**
   * @param printOutput
   * @param testOnly
   * @param scope 
   * @return stems to be deleted or would be deleted
   */
  public Set<Stem> deleteEmptyStems(final boolean printOutput, final boolean testOnly, Scope scope) {
    //delete all non empty stems
    //stems sorted by name desc so if stems contain empty stems they will be deleted
    Set<Stem> stems = new StemFinder().assignStemScope(scope).assignParentStemId(this.uuid)
        .assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES).assignQueryOptions(new QueryOptions().sortDesc("name")).findStems();

    Set<Stem> deletedObjects = new HashSet<Stem>();

    Iterator<Stem> stemIterator = stems.iterator();
    while (stemIterator.hasNext()) {
      Stem stem = stemIterator.next();
      if (!stem.isEmpty()) {
        continue;
      }
      deletedObjects.add(stem);
      if (!testOnly) {
        stem.delete();
      }
      if (printOutput) {
        if (testOnly) {
          System.out.println("Would be done deleting stem: " + stem.getName());
        } else {
          System.out.println("Done deleting stem: " + stem.getName());
        }
      }
    }
    return deletedObjects;
  }

}

