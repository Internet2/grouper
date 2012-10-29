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
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddFieldToGroupTypeValidator;
import edu.internet2.middleware.grouper.validator.DeleteFieldFromGroupTypeValidator;
import edu.internet2.middleware.grouper.validator.ModifyGroupTypeValidator;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupType;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.89 2009-06-10 05:31:35 mchyzer Exp $
 */
public class GroupType extends GrouperAPI implements GrouperHasContext, Serializable, Hib3GrouperVersioned, Comparable, XmlImportable<GroupType> {

  /** name of table for grouper_types */
  public static final String TABLE_GROUPER_TYPES = "grouper_types";
  
  /** uuid col in db */
  public static final String COLUMN_TYPE_UUID = "type_uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_TYPE_UUID = "old_type_uuid";
 
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: isAssignable */
  public static final String FIELD_IS_ASSIGNABLE = "isAssignable";

  /** constant for field name for: isInternal */
  public static final String FIELD_IS_INTERNAL = "isInternal";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_IS_ASSIGNABLE, FIELD_IS_INTERNAL, 
      FIELD_NAME, FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_IS_ASSIGNABLE, FIELD_IS_INTERNAL, FIELD_NAME, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  public static final long serialVersionUID = 8214760621248803096L;

  /**
   * Create a new {@link GroupType}.  
   * <p/>
   * Create a new custom group type that can be assigned to existing or
   * new groups.  If the type already exists, a {@link SchemaException}
   * will be thrown.  If the subject is not root-like, an 
   * {@link InsufficientPrivilegeException} will be thrown.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupType.createType(s, "my custom type");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to add group types.
   * }
   * catch (SchemaException eS) {
   *   // Type not created
   * }
   * </pre>
   * @param   s     Create type within this session context.
   * @param   name  Create type with this name.
   * @return  New {@link GroupType}.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public static GroupType createType(GrouperSession s, String name) 
    throws  InsufficientPrivilegeException, SchemaException  {
    return createTypeHelper(s, name, true);
  }

  /**
   * Create a new {@link GroupType}.  
   * <p/>
   * Create a new custom group type that can be assigned to existing or
   * new groups.  If the type already exists, a {@link SchemaException}
   * will be thrown.  If the subject is not root-like, an 
   * {@link InsufficientPrivilegeException} will be thrown.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupType.createType(s, "my custom type");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to add group types.
   * }
   * catch (SchemaException eS) {
   *   // Type not created
   * }
   * </pre>
   * @param   s     Create type within this session context.
   * @param   name  Create type with this name.
   * @param exceptionIfExists 
   * @return  New {@link GroupType}.
   * @throws  InsufficientPrivilegeException
   * @throws SchemaException 
   */
  public static GroupType createType(GrouperSession s, String name, 
      boolean exceptionIfExists) 
    throws  InsufficientPrivilegeException, SchemaException  {
    return createTypeHelper(s, name, exceptionIfExists);
  }

  /**
   * @param s
   * @param name
   * @param exceptionIfExists 
   * @return the type
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  private static GroupType createTypeHelper(GrouperSession s, String name, boolean exceptionIfExists)
      throws InsufficientPrivilegeException, SchemaException {
    //note, no need for GrouperSession inverse of control
    StopWatch sw = new StopWatch();
    sw.start();
    boolean[] existedAlready = new boolean[1];
    GroupType type = internal_createType(s, name, true, false, exceptionIfExists, existedAlready, null);
    sw.stop();
    if (!existedAlready[0]) {
      EventLog.info(s, M.GROUPTYPE_ADD + Quote.single( type.getName() ), sw);
    }
    return type;
  }

  /** */
  private long    createTime;
  /** */
  private String  creatorUUID;
  
  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Set     fields;
  
  /** */
  private boolean isAssignable  = true;
  /** */
  private boolean isInternal    = false;
  /** */
  private String  name;
  /** */
  private String  uuid; 

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

  /**
   * Add a custom attribute {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myAttr = type.addAttribute(
   *     "my attribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add attribute
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add attribute within this session context.
   * @param   name      Name of attribute.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this attribute required.
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addAttribute(
    GrouperSession s, String name, Privilege read, Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException {
    return addAttribute(s, name, read, write, required, true);
  }

  /**
   * Add a custom attribute {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myAttr = type.addAttribute(
   *     "my attribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add attribute
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add attribute within this session context.
   * @param   name      Name of attribute.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this attribute required.
   * @param exceptionIfExists 
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addAttribute(
    GrouperSession s, String name, Privilege read, Privilege write, boolean required, boolean exceptionIfExists
  )
    throws  InsufficientPrivilegeException,
            SchemaException {
    return internal_addField(s, name, FieldType.ATTRIBUTE, read, write, required, exceptionIfExists, false, null, null);
  } // public Field addAttribute(s, name, read, write, required)

  /**
   * Add a custom attribute {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myAttr = type.addAttribute(
   *     "my attribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add attribute
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add attribute within this session context.
   * @param   name      Name of attribute.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this attribute required.
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addOrUpdateAttribute(
    GrouperSession s, String name, Privilege read, Privilege write, boolean required
  ) throws  InsufficientPrivilegeException, SchemaException {
    return internal_addField(s, name, FieldType.ATTRIBUTE, read, write, required, false, true, null, null);
  } // public Field addAttribute(s, name, read, write, required)

  /**
   * Add a custom list {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myList = type.addList(
   *     s, "my list", AccessPrivilege.VIEW, AccessPrivilege.UPDATE
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add list
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add list within this session context.
   * @param   name      Name of list.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @return field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addList(
    GrouperSession s, String name, Privilege read, Privilege write
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    ModifyGroupTypeValidator v = ModifyGroupTypeValidator.validate(s, this);
    if (v.isInvalid()) {
      throw new InsufficientPrivilegeException( v.getErrorMessage() );
    }
    if (!Privilege.isAccess(read)) {
      throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
    }
    if (!Privilege.isAccess(write)) {
      throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
    }
    return this.internal_addField(s, name, FieldType.LIST, read, write, false, true, false, null, null);
  } // public Field addList(s, name, read, write)

  /**
   * Delete a custom {@link GroupType} definition.
   * <p/>
   * <pre class="eg">
   * try {
   *   aGroupType.delete(s);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to delete group type.
   * }
   * catch (SchemaException eS) {
   *   // Type could not be deleted
   * }
   * </pre>
   * @param   s     Delete type within this session context.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @since   1.0
   */
  public void delete(final GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException {
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        //note, no need for GrouperSession inverse of control
        StopWatch sw = new StopWatch();
        sw.start();
        if ( GroupType.this.isSystemType() ) {
          String msg = E.GROUPTYPE_NODELSYS + GroupType.this.getName();
          LOG.error( msg);
          throw new SchemaException(msg);
        } 
        if (!PrivilegeHelper.isRoot(s)) {
          String msg = E.GROUPTYPE_NODEL;
          LOG.error( msg);
          throw new InsufficientPrivilegeException(msg);
        }
        try {
          if ( GrouperDAOFactory.getFactory().getGroup().findAllByType( GroupType.this ).size() > 0 ) {
            String msg = E.GROUPTYPE_DELINUSE;
            LOG.error( msg);
            throw new SchemaException(msg);
          }
          // Now delete the type
          String typeName = GroupType.this.getName(); // For logging purposes
          Set<Field> fields2 = GroupType.this.getFields();
      
          if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting type: " + GroupType.this.getName() + " and fields: " 
              + Field.fieldNames(fields2));
          }
      
          GrouperDAOFactory.getFactory().getGroupType().delete( GroupType.this, GroupType.this.getFields() );
          sw.stop();
          EventLog.info(s, M.GROUPTYPE_DEL + Quote.single(typeName), sw);
          // Now update the cached types + fields
          GroupTypeFinder.internal_updateKnownTypes();
          FieldFinder.internal_updateKnownFields();

          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_TYPE_DELETE, "id", 
                GroupType.this.getUuid(), "name", GroupType.this.getName());
            auditEntry.setDescription("Deleted group type: " + GroupType.this.getName());
            auditEntry.saveOrUpdate(true);
          }
        }
        catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_DEL + eDAO.getMessage();
          LOG.error(msg);
          throw new SchemaException(msg, eDAO);
        }
        return null;
      }});
  } // public void delete(s)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupType.class);

  /**
   * Delete a custom {@link Field} from a custom {@link GroupType}.
   * <p/>
   * Delete a field from this group type.  If the field does not exist
   * in this type a {@link SchemaException} will be thrown.  If the
   * subject is not root-like, an {@link InsufficientPrivilegeException}
   * will be thrown.
   * <pre class="eg">
   * try {
   *   type.deleteField(s, "my field");
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete field
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Delete field within this session context.
   * @param   name      Name of field to delete.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void deleteField(final GrouperSession s, final String name)
      throws  InsufficientPrivilegeException, SchemaException {

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        try {
          //note, no need for GrouperSession inverse of control
          StopWatch sw  = new StopWatch();
          sw.start();
          Field     field   = FieldFinder.find(name, true);  
          ModifyGroupTypeValidator          vModify = ModifyGroupTypeValidator.validate(s, GroupType.this);
          if (vModify.isInvalid()) {
            throw new InsufficientPrivilegeException( vModify.getErrorMessage() );
          }
          DeleteFieldFromGroupTypeValidator vDelete = DeleteFieldFromGroupTypeValidator.validate(GroupType.this, field);
          if (vDelete.isInvalid()) {
            throw new SchemaException( vDelete.getErrorMessage() );
          }
          if ( GrouperDAOFactory.getFactory().getField().isInUse(field) ) {
            String msg = E.GROUPTYPE_FIELDNODELINUSE + name;
            LOG.error( msg);
            throw new SchemaException(msg);
          }
          Set fields = GroupType.this.getFields();
          if ( fields.remove( field ) ) {
            GroupType.this.setFields(fields);
            String typeString = field.getTypeString();
            GrouperDAOFactory.getFactory().getGroupType().deleteField( field);
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              //only audit if actually changed the type
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_FIELD_DELETE, "id", 
                  field.getUuid(), "name", field.getName(), "groupTypeId", GroupType.this.getUuid(), "groupTypeName", GroupType.this.getName(), "type", typeString);
              auditEntry.setDescription("Deleted group field: " + name + ", id: " + field.getUuid() + ", type: " + typeString + ", groupType: " + GroupType.this.getName());
              auditEntry.saveOrUpdate(true);
            }
            
            sw.stop();
            EventLog.info(
              s,
              M.GROUPTYPE_DELFIELD + Quote.single(field.getName()) + " type=" + Quote.single( GroupType.this.getName() ),
              sw
            );
          }
          else {
            String msg = E.GROUPTYPE_FIELDNODELMISS;
            LOG.error( msg);
            throw new SchemaException(msg);
          }
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_FIELDDEL + name + ": " + eDAO.getMessage();
          LOG.error( msg);
          throw new SchemaException(msg, eDAO);
        }
        return null;
      }        
    });
  }

  /**
   * 
   * @return true if system type
   */
  public boolean isSystemType() {
    if ( "base".equals( this.getName() ) || "naming".equals( this.getName() ) ) {
      return true;
    }
    return false;
  } 
  

  /**
   * 
   * @param s
   * @param name
   * @param isAssignable
   * @param isInternal
   * @param exceptionIfExists
   * @param changed boolean array, the fisrt index will be in it existed already
   * @param uuid to use or null for one to be assigned
   * @return the type
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public static GroupType internal_createType(
    final GrouperSession s, final String name, final boolean isAssignable, 
    final boolean isInternal, final boolean exceptionIfExists, final boolean[] changed, String uuid)
      throws  InsufficientPrivilegeException,
              SchemaException { 
    
    final String UUID = StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid;
    
    return (GroupType)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        try {
          //note, no need for GrouperSession inverse of control
          if (!PrivilegeHelper.isRoot(s)) {
            String msg = "subject '" + GrouperUtil.subjectToString(s.getSubject()) + "' not privileged to add group types ('" + name + "')";
            LOG.error( msg);
            throw new InsufficientPrivilegeException(msg);
          }
          GroupTypeDAO dao = GrouperDAOFactory.getFactory().getGroupType();
          if (GrouperUtil.length(changed) >= 1) {
            changed[0] = true;
          }
          if ( dao.existsByName(name) ) {
            if (GrouperUtil.length(changed) >= 1) {
              changed[0] = false;
            }
            if (exceptionIfExists) {
              String msg = E.GROUPTYPE_EXISTS + name;
              LOG.error( msg);
              throw new SchemaException(msg);
            }
            return GroupTypeFinder.find(name, true);
          }
          GroupType _gt = new GroupType();
          _gt.setCreateTime( new Date().getTime() );
            _gt.setCreatorUuid( s.getMember().getUuid() );
            _gt.setFields( new LinkedHashSet() );
            _gt.setIsAssignable(isAssignable);
            _gt.setIsInternal(isInternal);
            _gt.setName(name);
            _gt.setUuid( UUID );
            
          dao.createOrUpdate(_gt) ;
          
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            //only audit if actually changed the type
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_TYPE_ADD, "id", 
                _gt.getUuid(), "name", _gt.getName());
            auditEntry.setDescription("Added group type: " + _gt.getName());
            auditEntry.saveOrUpdate(true);
          }
          
          return _gt;
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_ADD + name + ": " + eDAO.getMessage();
          LOG.error( msg);
          throw new SchemaException(msg, eDAO);
        }
      }
      
    });

  }


  /**
   * add a field if it is not already there
   * @param s
   * @param name
   * @param type
   * @param read
   * @param write
   * @param required
   * @param exceptionIfExists
   * @param updateIfExists 
   * @param changedArray is an array of 1 if you want to know if this method changed anything, else null
   * @param uuid 
   * @return the field
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public Field internal_addField(
    final GrouperSession s, final String name, final FieldType type, final Privilege read, 
    final Privilege write, final boolean required, final boolean exceptionIfExists, final boolean updateIfExists,
    final boolean[] changedArray, String uuid) throws  InsufficientPrivilegeException, SchemaException {

    //these are reserved words:
    if (Group.INTERNAL_FIELD_ATTRIBUTES.contains(name)) {
      throw new RuntimeException("You cannot add a field which is a reserved word '" 
          + name + "', reserved words are : " + GrouperUtil.toStringForLog(Group.INTERNAL_FIELD_ATTRIBUTES));
    }
    
    final String UUID = StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid;
    
    return (Field)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        try {
          if (FieldType.ATTRIBUTE.equals(type)) {
            //note, no need for GrouperSession inverse of control
            ModifyGroupTypeValidator v = ModifyGroupTypeValidator.validate(s, GroupType.this);
            if (v.isInvalid()) {
              throw new InsufficientPrivilegeException( v.getErrorMessage() + ", attribute: '" + name + "'" );
            }
            if (!Privilege.isAccess(read)) {
              throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
            }
            if (!Privilege.isAccess(write)) {
              throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
            }
          }
          
          //note, no need for GrouperSession inverse of control
          StopWatch sw  = new StopWatch();
          sw.start();
          AddFieldToGroupTypeValidator v = AddFieldToGroupTypeValidator.validate(name, !exceptionIfExists);
          if (v.isInvalid()) {
            throw new SchemaException( v.getErrorMessage() );
          }
          Field field = FieldFinder.find(name, false);

          if (field != null) {
            boolean changed = false;
            if (!type.equals(field.getType())) {
              //dont want to change types, that could be bad!
              throw new SchemaException("field '" + name + "' does not have type: " + type + ", it has: " + field.getType());
            }
            if (field.getRequired() != required) {
              if (exceptionIfExists) {
                throw new SchemaException("field '" + name + "' does not match required flag: " + required);
              }
              if (updateIfExists) {
                changed = true;
                field.setIsNullable(!required);
              }
            }
            if (!read.equals(field.getReadPriv())) {
              if (exceptionIfExists) {
                throw new SchemaException("field '" + name + "' does not have read privilege: " + read + ", it has: " + field.getReadPrivilege());
              }
              if (updateIfExists) {
                changed = true;
                field.setReadPrivilege(read);
              }
            }
            if (!write.equals(field.getWritePriv())) {
              if (exceptionIfExists) {
                throw new SchemaException("field '" + name + "' does not have write privilege: " + write + ", it has: " + field.getWritePrivilege());
              }
              if (updateIfExists) {
                changed = true;
                field.setWritePrivilege(write);
              }
            }
            if (exceptionIfExists) {
              throw new SchemaException("field exists: '" + name + "'");
            }
            //store minor changes to db
            if (changed && updateIfExists) {
              changed = true;
              
              String differences = GrouperUtil.dbVersionDescribeDifferences(field.dbVersion(), 
                  field, field.dbVersion() != null ? field.dbVersionDifferentFields() : GroupType.CLONE_FIELDS);
              
              GrouperDAOFactory.getFactory().getField().createOrUpdate(field);
  
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                //audit the update
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_FIELD_UPDATE, "id", 
                    field.getUuid(), "name", field.getName(), "groupTypeId", GroupType.this.getUuid(), 
                    "groupTypeName", GroupType.this.getName(), "type", type.getType());
                
                String description = "Updated group field: " + name + ", id: " + field.getUuid() 
                    + ", type: " + type + ", groupType: " + GroupType.this.getName() + ".\n" + differences;
                auditEntry.setDescription(description);
                
                auditEntry.saveOrUpdate(true);
              }
              
              if (GrouperUtil.length(changedArray) > 0) {
                changedArray[0] = true;
              }
            } else {
              if (GrouperUtil.length(changedArray) > 0) {
                changedArray[0] = false;
              }
            }
            FieldFinder.internal_updateKnownFields();
            return field;
          }
          if (GrouperUtil.length(changedArray) > 0) {
            changedArray[0] = true;
          }
          try {
            boolean nullable = true;
            if (required == true) {
              nullable = false;
            }
            field = new Field();
            field.setGroupTypeUuid( GroupType.this.getUuid() );
            field.setIsNullable(nullable);
            field.setName(name);
            field.setReadPrivilege(read);
            field.setType(type);
            field.setUuid(UUID);
            field.setWritePrivilege(write);
              
            GrouperDAOFactory.getFactory().getGroupType().createField(field);
  
            Set fields = GroupType.this.getFields();
            fields.add( field );
  
            sw.stop();
            EventLog.info(
              s, 
              M.GROUPTYPE_ADDFIELD + Quote.single(field.getName()) + " ftype=" + Quote.single(type.toString()) 
              + " gtype=" + Quote.single( GroupType.this.getName() ),
              sw
            );
            FieldFinder.internal_updateKnownFields();
          }
          catch (GrouperDAOException eDAO) {
            String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
            LOG.error( msg);
            throw new SchemaException(msg, eDAO);
          }
          
          
          //only audit if actually changed the type
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_FIELD_ADD, "id", 
              field.getUuid(), "name", field.getName(), "groupTypeId", GroupType.this.getUuid(), "groupTypeName", GroupType.this.getName(), "type", type.getType());
          auditEntry.setDescription("Added group field: " + name + ", id: " + field.getUuid() + ", type: " + type + ", groupType: " + GroupType.this.getName());
          auditEntry.saveOrUpdate(true);
          
          return field;
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
          LOG.error( msg);
          throw new SchemaException(msg, eDAO);
        }
      }
      
    });
    
    
    
    
  } // protected Field internal_addField(s, name, type, read, write, required)


  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupType)) {
      return false;
    }
    GroupType that = (GroupType) other;
    return new EqualsBuilder()
      .append( this.name, that.name )
      .isEquals();
  }


  /**
   * @return create time
   * @since   1.2.0
   */ 
  public long getCreateTime() {
    return this.createTime;
  }


  /**
   * @return creator uuid
   * @since   1.2.0
   */ 
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @return fields
   * @since   1.2.0
   */ 
  public Set<Field> getFields() {
    if (this.fields == null) {
      this.fields = FieldFinder.findAllByGroupType(this);
    }
    return this.fields;
  }


  /**
   * @return if assignable
   * @since   1.2.0
   */ 
  public boolean getIsAssignable() {
    return this.isAssignable;
  }


  /**
   * @return if internal
   * @since   1.2.0
   */ 
  public boolean getIsInternal() {
    return this.isInternal;
  }


  /**
   * @return name
   * @since   1.2.0
   */ 
  public String getName() {
    return this.name;
  }


  /**
   * @return uuid
   * @since   1.2.0
   */ 
  public String getUuid() {
    return this.uuid;
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
  public void setCreateTime(long createTime) {
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
   * @param fields 
   * @since   1.2.0
   */ 
  public void setFields(Set fields) {
    this.fields = fields;
  
  }


  /**
   * @param isAssignable 
   * @since   1.2.0
   */ 
  public void setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
  
  }


  /**
   * @param isInternal 
   * @since   1.2.0
   */ 
  public void setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
  
  }


  /**
   * @param name 
   * @since   1.2.0
   */ 
  public void setName(String name) {
    this.name = name;
  
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
  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "createTime",   this.getCreateTime()   )
      .append( "fields",       GrouperUtil.length(this.getFields()))
      .append( "isAssignable", this.getIsAssignable() )
      .append( "isInternal",   this.getIsInternal()   )
      .append( "name",         this.getName()         )
      .append( "uuid",         this.getUuid()         )
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {

    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_DELETE, false, false);
  
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_DELETE, 
        ChangeLogLabels.GROUP_TYPE_DELETE.id.name(), 
        this.getUuid(), ChangeLogLabels.GROUP_TYPE_DELETE.name.name(), 
        this.getName()).save();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_INSERT, false, false);

    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_ADD, 
        ChangeLogLabels.GROUP_TYPE_ADD.id.name(), 
        this.getUuid(), ChangeLogLabels.GROUP_TYPE_ADD.name.name(), 
        this.getName()).save();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_UPDATE, false, false);
    
    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.GROUP_TYPE_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(ChangeLogLabels.GROUP_TYPE_UPDATE.id.name(),this.getUuid(), 
            ChangeLogLabels.GROUP_TYPE_UPDATE.name.name(), this.getName()),
        GrouperUtil.toList(FIELD_NAME),
        GrouperUtil.toList(ChangeLogLabels.GROUP_TYPE_UPDATE.name.name()));    
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public GroupType dbVersion() {
    return (GroupType)this.dbVersion;
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
  public GroupType clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (!(o instanceof GroupType)) {
      return -1;
    }
    String thisName = StringUtils.defaultString(this.name);
    String otherName = StringUtils.defaultString(((GroupType)o).name);
    return thisName.compareTo(otherName);
  }

  /**
   * store this object to the DB.
   */
  public void store() {    
    GrouperDAOFactory.getFactory().getGroupType().update(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(GroupType existingRecord) {
    existingRecord.isAssignable = this.isAssignable;
    existingRecord.isInternal = this.isInternal;
    existingRecord.name = this.name;
    existingRecord.setUuid(this.getUuid());

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(GroupType other) {
    if (this.isAssignable != other.isAssignable) {
      return true;
    }
    if (this.isInternal != other.isInternal) {
      return true;
    }
    if (!StringUtils.equals(this.name, other.name)) {
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
  public boolean xmlDifferentUpdateProperties(GroupType other) {
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
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public GroupType xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getGroupType().findByUuidOrName(this.uuid, this.name, false, 
        new QueryOptions().secondLevelCache(false));
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public GroupType xmlSaveBusinessProperties(GroupType existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = internal_createType(GrouperSession.staticGrouperSession(), this.name, this.isAssignable, this.isInternal, true, null, this.uuid);
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
    GrouperDAOFactory.getFactory().getGroupType().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportGroupType xmlToExportGroupType(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportGroupType xmlExportGroupType = new XmlExportGroupType();
    
    xmlExportGroupType.setAssignable(this.getIsAssignable() ? "T" : "F");
    xmlExportGroupType.setContextId(this.getContextId());
    xmlExportGroupType.setCreateTime(GrouperUtil.dateStringValue(new Date(this.getCreateTime())));
    xmlExportGroupType.setCreatorId(this.getCreatorUuid());
    xmlExportGroupType.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportGroupType.setInternal(this.getIsInternal() ? "T" : "F");
    xmlExportGroupType.setName(this.getName());
    xmlExportGroupType.setUuid(this.getUuid());
    return xmlExportGroupType;
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
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("GroupType: " + this.getUuid() + ", " + this.getName());

    return stringWriter.toString();
    
  }

}
