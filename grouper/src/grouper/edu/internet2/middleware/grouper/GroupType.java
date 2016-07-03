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
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.DeleteFieldFromGroupTypeValidator;
import edu.internet2.middleware.grouper.validator.ModifyGroupTypeValidator;

/** 
 * Schema specification for a Group type.
 * 
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.89 2009-06-10 05:31:35 mchyzer Exp $
 */
public class GroupType extends GrouperAPI implements Serializable, Comparable {
  
  /** name of table for grouper_types */
  public static final String TABLE_OLD_GROUPER_TYPES = "grouper_types";
  
  /** uuid col in db */
  public static final String COLUMN_TYPE_UUID = "type_uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_TYPE_UUID = "old_type_uuid";
 
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_NAME, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  public static final long serialVersionUID = 8214760621248803096L;

  /**
   * Create a new {@link GroupType}.  
   * 
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
   * @deprecated
   */
  public static GroupType createType(GrouperSession s, String name) 
    throws  InsufficientPrivilegeException, SchemaException  {
    return createTypeHelper(s, name, true, null);
  }

  /**
   * Create a new {@link GroupType}.  
   * 
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
   * @deprecated
   */
  public static GroupType createType(GrouperSession s, String name, 
      boolean exceptionIfExists) 
    throws  InsufficientPrivilegeException, SchemaException  {
    return createTypeHelper(s, name, exceptionIfExists, null);
  }

  /**
   * Create a new {@link GroupType}.  
   * 
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
   * @param uuid 
   * @return  New {@link GroupType}.
   * @throws  InsufficientPrivilegeException
   * @throws SchemaException 
   * @deprecated
   */
  public static GroupType createType(GrouperSession s, String name, 
      boolean exceptionIfExists, String uuid) 
    throws  InsufficientPrivilegeException, SchemaException  {
    return createTypeHelper(s, name, exceptionIfExists, uuid);
  }

  /**
   * @param s
   * @param name
   * @param exceptionIfExists 
   * @param uuid 
   * @return the type
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   * @deprecated
   */
  private static GroupType createTypeHelper(GrouperSession s, String name, boolean exceptionIfExists, String uuid)
      throws InsufficientPrivilegeException, SchemaException {
    //note, no need for GrouperSession inverse of control
    StopWatch sw = new StopWatch();
    sw.start();
    boolean[] existedAlready = new boolean[1];
    GroupType type = internal_createType(s, name, exceptionIfExists, existedAlready, StringUtils.trimToNull(uuid));
    sw.stop();
    if (!existedAlready[0]) {
      EventLog.info(s, M.GROUPTYPE_ADD + Quote.single( type.getName() ), sw);
    }
    return type;
  }

  /** */
  private long    createTime;
  
  /** */
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Set     fields;
  
  /** */
  private String  name;
  /** */
  private String  uuid; 

  /** context id of the transaction */
  private String contextId;
  
  /**
   * cache of group type id to attribute def name
   */
  private static GrouperCache<String, AttributeDefName> attributeDefNameFromTypeIdCache = null;
  
  /**
   * cache of group type id to attribute def name
   * @return cache
   */
  private static GrouperCache<String, AttributeDefName> attributeDefNameFromTypeIdCache() {
    if (attributeDefNameFromTypeIdCache == null) {
      synchronized(GroupType.class) {
        if (attributeDefNameFromTypeIdCache == null) {
          attributeDefNameFromTypeIdCache = new GrouperCache<String, AttributeDefName>(
              GroupType.class.getName() + ".attributeDefNameFromTypeIdCache", 200, false, 
              30, 30, false);
        }
      }
    }
    return attributeDefNameFromTypeIdCache;
  }  

  /**
   * cache of group type id to legacy attributes
   */
  private static GrouperCache<String, Set<AttributeDefName>> legacyAttributesFromTypeIdCache = null;
  
  /**
   * cache of group type id to legacy attributes
   * @return cache
   */
  private static GrouperCache<String, Set<AttributeDefName>> legacyAttributesFromTypeIdCache() {
    if (legacyAttributesFromTypeIdCache == null) {
      synchronized(GroupType.class) {
        if (legacyAttributesFromTypeIdCache == null) {
          legacyAttributesFromTypeIdCache = new GrouperCache<String, Set<AttributeDefName>>(
              GroupType.class.getName() + ".legacyAttributesFromTypeIdCache", 200, false, 
              30, 30, false);
        }
      }
    }
    return legacyAttributesFromTypeIdCache;
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
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @deprecated
   */
  public AttributeDefName addAttribute(
    GrouperSession s, String name
  )
    throws  InsufficientPrivilegeException,
            SchemaException {
    return addAttribute(s, name, true);
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
   * @param exceptionIfExists 
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @deprecated
   */
  public AttributeDefName addAttribute(
    final GrouperSession s, final String name, final boolean exceptionIfExists
  )
    throws  InsufficientPrivilegeException,
            SchemaException {
    return addAttribute(s, name, exceptionIfExists, null);
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
   * @param exceptionIfExists 
   * @param uuid 
   * @return  field
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @deprecated
   */
  public AttributeDefName addAttribute(
    final GrouperSession s, final String name, final boolean exceptionIfExists, final String uuid
  )
    throws  InsufficientPrivilegeException,
            SchemaException {

    return (AttributeDefName)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        if (!PrivilegeHelper.isRoot(s)) {
          String msg = "Cannot add legacy attributes.";
          LOG.error(msg);
          throw new InsufficientPrivilegeException(msg);
        }
        
        //these are reserved words:
        if (Group.INTERNAL_FIELD_ATTRIBUTES.contains(name)) {
          throw new RuntimeException("You cannot add a field which is a reserved word '" 
              + name + "', reserved words are : " + GrouperUtil.toStringForLog(Group.INTERNAL_FIELD_ATTRIBUTES));
        }
      
        // see if the attribute def exists first.
        String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
        Stem stem = GrouperDAOFactory.getFactory().getStem().findByName(stemName, true);
        String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
        String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
        AttributeDef attributeDef = AttributeDefFinder.findByName(stemName + ":" + attributeDefPrefix + GroupType.this.name, false);
        if (attributeDef == null) {
          // get the attribute definition used for the group type
          String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
          AttributeDefName groupType = AttributeDefNameFinder.findByName(stemName + ":" + groupTypePrefix + GroupType.this.name, true);
          
          // create it..
          attributeDef = stem.addChildAttributeDef(attributeDefPrefix + GroupType.this.name, AttributeDefType.attr);
          attributeDef.setAssignToGroupAssn(true);
          attributeDef.setValueType(AttributeDefValueType.string);
          attributeDef.store();
          
          // add scope
          attributeDef.getAttributeDefScopeDelegate().assignScope(AttributeDefScopeType.idEquals, groupType.getId(), null);
        } else {
          // see if the attribute already exists.
          AttributeDefName attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + name, false);
          if (attribute != null) {
            if (exceptionIfExists) {
              throw new SchemaException(name + " already exists.");
            }
            
            return attribute;
          }
        }
        
        return stem.addChildAttributeDefName(attributeDef, attributePrefix + name, attributePrefix + name, uuid);
      }    
    });          
  }
      
      

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
   * @deprecated
   */
  public Field addList(
    final GrouperSession s, final String name, final Privilege read, final Privilege write
  )
    throws  InsufficientPrivilegeException,
            SchemaException {
    return internal_addList(s, name, read, write, null, true);
  }
  
  /**
   * @param s
   * @param name
   * @param read
   * @param write
   * @param fieldId
   * @param exceptionIfExists
   * @return field
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public Field internal_addList(
    final GrouperSession s, final String name, final Privilege read, final Privilege write,
    final String fieldId, final boolean exceptionIfExists
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    final String UUID = StringUtils.isBlank(fieldId) ? GrouperUuid.getUuid() : fieldId;

    return (Field)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
      
          if (!Privilege.isAccess(read)) {
            throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
          }
          if (!Privilege.isAccess(write)) {
            throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
          }
        
          if (!PrivilegeHelper.isRoot(s)) {
            String msg = "Cannot add lists.";
            LOG.error(msg);
            throw new InsufficientPrivilegeException(msg);
          }
                    
          // see if the attribute def and attribute exist first.
          String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
          Stem stem = GrouperDAOFactory.getFactory().getStem().findByName(stemName, true);
          String customListDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customListDef.prefix");
          String customListPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customList.prefix");
          
          // get the attribute definition used for the group type
          String groupTypeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupTypeDef.prefix");
          AttributeDef groupTypeDef = AttributeDefFinder.findByName(stemName + ":" + groupTypeDefPrefix + GroupType.this.name, true);
          
          AttributeDef customListDef = AttributeDefFinder.findByName(stemName + ":" + customListDefPrefix + GroupType.this.name, false);
          AttributeDefName customList = AttributeDefNameFinder.findByName(stemName + ":" + customListPrefix + GroupType.this.name, false);
          if (customListDef == null) {
            // create it..
            customListDef = stem.addChildAttributeDef(customListDefPrefix + GroupType.this.name, AttributeDefType.attr);
            customListDef.setAssignToAttributeDef(true);
            customListDef.setValueType(AttributeDefValueType.string);
            customListDef.setMultiValued(true);
            customListDef.store();
            
            // add scope
            customListDef.getAttributeDefScopeDelegate().assignScope(AttributeDefScopeType.idEquals, groupTypeDef.getId(), null);
          }
          
          if (customList == null) {
            customList = stem.addChildAttributeDefName(customListDef, customListPrefix + GroupType.this.name, customListPrefix + GroupType.this.name);
          }
          
          AttributeAssignValue attributeAssignValue = groupTypeDef.getAttributeValueDelegate().findValue(customList.getName(), UUID);
          
          if (attributeAssignValue == null) {
            groupTypeDef.getAttributeValueDelegate().addValue(customList.getName(), UUID);
          } else {
            if (exceptionIfExists) {
              throw new RuntimeException(attributeAssignValue.toString() + " already exists.");
            }
          }
          
          FieldFinder.clearCache();
          GroupTypeFinder.clearCache();
          
          Field field = Field.internal_addField(s, name, FieldType.LIST, read, write, exceptionIfExists, false, null, UUID);
          
          Set fields = GroupType.this.getFields();
          fields.add(field);
          
          return field;
      }
    });
  }

  /**
   * Delete a custom {@link GroupType} definition.
   * 
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
   * @deprecated
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
        if (!PrivilegeHelper.isRoot(s)) {
          String msg = E.GROUPTYPE_NODEL;
          LOG.error( msg);
          throw new InsufficientPrivilegeException(msg);
        }
        try {
          Set<Field> fields = GroupType.this.getFields();
          
          // verify fields aren't in use
          for (Field field : fields) {
            if (GrouperDAOFactory.getFactory().getField().isInUse(field)) {
              String msg = E.GROUPTYPE_DELINUSE;
              LOG.error(msg);
              throw new SchemaException(msg);
            }
          }
          
          // verify attributes aren't in use
          String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
          String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");          
          AttributeDefName groupType = AttributeDefNameFinder.findByName(stemName + ":" + groupTypePrefix + GroupType.this.name, false);
          if (groupType != null) {
            if (GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameId(groupType.getId()).size() > 0) {
              String msg = E.GROUPTYPE_DELINUSE;
              LOG.error(msg);
              throw new SchemaException(msg);
            }
          }
          
          String typeName = GroupType.this.getName(); // For logging purposes
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting type: " + GroupType.this.getName() + " and fields: " 
              + Field.fieldNames(fields));
          }
          
          // Now delete
          GrouperDAOFactory.getFactory().getField().delete(fields);
          String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
          String groupTypeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupTypeDef.prefix");          
          String customListDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customListDef.prefix");          
          AttributeDef attributeDef = AttributeDefFinder.findByName(stemName + ":" + attributeDefPrefix + GroupType.this.name, false);
          AttributeDef groupTypeDef = AttributeDefFinder.findByName(stemName + ":" + groupTypeDefPrefix + GroupType.this.name, false);
          AttributeDef customListDefDef = AttributeDefFinder.findByName(stemName + ":" + customListDefPrefix + GroupType.this.name, false);

          if (groupType != null) {
            groupType.delete();
            GroupType.this.setContextId(groupType.getContextId());
          }
          
          if (attributeDef != null) {
            attributeDef.delete();
          }
          if (customListDefDef != null) {
            customListDefDef.delete();
          }
          if (groupTypeDef != null) {
            groupTypeDef.delete();
          }
          
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
   * 
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
   * @deprecated
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
            GrouperDAOFactory.getFactory().getField().delete(field);
            
            String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
            String groupTypeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupTypeDef.prefix");
            AttributeDef groupTypeDef = AttributeDefFinder.findByName(stemName + ":" + groupTypeDefPrefix + GroupType.this.name, true);
            String customListPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customList.prefix");
            AttributeDefName customList = AttributeDefNameFinder.findByName(stemName + ":" + customListPrefix + GroupType.this.name, false);
            groupTypeDef.getAttributeValueDelegate().deleteValue(customList.getName(), field.getUuid());
            
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
          
          FieldFinder.clearCache();
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
    return false;
  } 
  

  /**
   * 
   * @param s
   * @param name
   * @param exceptionIfExists
   * @param changed boolean array, the fisrt index will be in it existed already
   * @param uuid to use or null for one to be assigned
   * @return the type
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public static GroupType internal_createType(
    final GrouperSession s, final String name, final boolean exceptionIfExists, final boolean[] changed, String uuid)
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
          
          @SuppressWarnings("deprecation")
          GroupType existing = GroupTypeFinder.find(name, false);

          if (GrouperUtil.length(changed) >= 1) {
            changed[0] = true;
          }
          if (existing != null) {
            if (GrouperUtil.length(changed) >= 1) {
              changed[0] = false;
            }
            if (exceptionIfExists) {
              String msg = E.GROUPTYPE_EXISTS + name;
              LOG.error( msg);
              throw new SchemaException(msg);
            }

            return existing;
          }
          
          String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
          String groupTypeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupTypeDef.prefix");
          String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
          
          Stem stem = GrouperDAOFactory.getFactory().getStem().findByName(stemName, true);
          AttributeDef groupTypeDef = stem.addChildAttributeDef(groupTypeDefPrefix + name, AttributeDefType.attr);
          groupTypeDef.setAssignToGroup(true);
          groupTypeDef.store();
          
          AttributeDefName groupType = stem.internal_addChildAttributeDefName(s, groupTypeDef, groupTypePrefix + name, groupTypePrefix + name, UUID, null);
                      
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            //only audit if actually changed the type
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_TYPE_ADD, "id", 
                groupType.getId(), "name", groupType.getName());
            auditEntry.setDescription("Added group type: " + groupType.getName());
            auditEntry.saveOrUpdate(true);
          }
          
          return GroupType.internal_getGroupType(groupType, true);
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_ADD + name + ": " + eDAO.getMessage();
          LOG.error( msg);
          throw new SchemaException(msg, eDAO);
        }
      }
      
    });

  }


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
   * @param fields 
   * @since   1.2.0
   */ 
  public void setFields(Set fields) {
    this.fields = fields;
  
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
      .append( "createTime",   this.getCreateTime()   )
      .append( "fields",       GrouperUtil.length(this.getFields()))
      .append( "name",         this.getName()         )
      .append( "uuid",         this.getUuid()         )
      .toString();
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
   * @return attributeDefName that corresponds to this group type
   */
  public AttributeDefName getAttributeDefName() {
    
    AttributeDefName attributeDefName = attributeDefNameFromTypeIdCache().get(this.uuid);
    
    if (attributeDefName == null) {
      attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findById(this.uuid, true);
      attributeDefNameFromTypeIdCache().put(this.uuid, attributeDefName);
    }
    
    return attributeDefName;
  }
  
  /**
   * @param attributeDefName1
   */
  public void internalSetAttributeDefName(AttributeDefName attributeDefName1) {
    
    if (attributeDefName1 != null) {
      if (!StringUtils.equals(this.uuid, attributeDefName1.getId())) {
        throw new RuntimeException("Why does the groupType id " 
            + this.uuid + " not equal the param id: " + attributeDefName1.getId());
      }
    }
    attributeDefNameFromTypeIdCache().put(this.uuid, attributeDefName1);
    
  }

  /**
   * @param attribute
   * @param exceptionIfNotLegacyGroupType 
   * @return groupType
   */
  public static GroupType internal_getGroupType(AttributeDefName attribute, boolean exceptionIfNotLegacyGroupType) {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
    
    if (!attribute.getName().startsWith(stemName + ":" + groupTypePrefix)) {
      if (exceptionIfNotLegacyGroupType) {
        throw new RuntimeException("AttributeDefName " + attribute.getName() + " is not a legacy group type.");
      }
      
      return null;
    }
    
    GroupType gt = new GroupType();
    gt.setContextId(attribute.getContextId());
    gt.setCreateTime(attribute.getCreatedOnDb());
    gt.setHibernateVersionNumber(attribute.getHibernateVersionNumber());
    gt.setName(attribute.getExtension().substring(groupTypePrefix.length()));
    gt.setUuid(attribute.getId());
    gt.internalSetAttributeDefName(attribute);
    
    return gt;
  }
  
  /**
   * @return the attributeDef for attribute assignments
   */
  public AttributeDef internal_getAttributeDefForAttributes() {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String attributeDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attributeDef.prefix");
    AttributeDef attributeDef = AttributeDefFinder.findByName(stemName + ":" + attributeDefPrefix + GroupType.this.name, false);
    
    return attributeDef;
  }
  
  /**
   * @return the attributeDef for custom lists
   */
  public AttributeDef internal_getAttributeDefForCustomLists() {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");    
    String customListDefPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customListDef.prefix");
    AttributeDef customListDef = AttributeDefFinder.findByName(stemName + ":" + customListDefPrefix + GroupType.this.name, false);    
    
    return customListDef;
  }
  
  /**
   * @return the attributeDefName for custom lists
   */
  public AttributeDefName internal_getAttributeDefNameForCustomLists() {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");    
    String customListPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customList.prefix");
    AttributeDefName customList = AttributeDefNameFinder.findByName(stemName + ":" + customListPrefix + GroupType.this.name, false);

    return customList;
  }
  
  /**
   * @return legacy attributes
   */
  public Set<AttributeDefName> getLegacyAttributes() {
    
    Set<AttributeDefName> legacyAttributes = legacyAttributesFromTypeIdCache().get(this.uuid);
    
    if (legacyAttributes == null) {
      AttributeDef attributeDefForAttributes = this.internal_getAttributeDefForAttributes();
      
      if (attributeDefForAttributes != null) {
        legacyAttributes = GrouperDAOFactory.getFactory().getAttributeDefName().findByAttributeDef(attributeDefForAttributes.getId());
      } else {
        legacyAttributes = new LinkedHashSet<AttributeDefName>();
      }
      
      legacyAttributesFromTypeIdCache().put(this.uuid, legacyAttributes);
    }
    return legacyAttributes;
  }
}
