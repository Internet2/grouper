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
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.misc;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetViewDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetViewDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import edu.internet2.middleware.grouper.internal.dao.EntityDAO;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectAttributeDAO;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectDAO;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupSetDAO;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.internal.dao.PITFieldDAO;
import edu.internet2.middleware.grouper.internal.dao.PITGroupDAO;
import edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO;
import edu.internet2.middleware.grouper.internal.dao.PITMemberDAO;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO;
import edu.internet2.middleware.grouper.internal.dao.PITPermissionAllViewDAO;
import edu.internet2.middleware.grouper.internal.dao.PITRoleSetDAO;
import edu.internet2.middleware.grouper.internal.dao.PITStemDAO;
import edu.internet2.middleware.grouper.internal.dao.PermissionEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.RegistryDAO;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.internal.dao.RoleDAO;
import edu.internet2.middleware.grouper.internal.dao.RoleSetDAO;
import edu.internet2.middleware.grouper.internal.dao.RoleSetViewDAO;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dao.StemSetDAO;
import edu.internet2.middleware.grouper.internal.dao.TableIndexDAO;
import edu.internet2.middleware.grouper.internal.dao.TransactionDAO;
import edu.internet2.middleware.grouper.internal.util.Realize;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;

/** 
 * Factory for returning <code>GrouperDAO</code> objects.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperDAOFactory.java,v 1.15 2009-10-26 02:26:07 mchyzer Exp $
 * @since   1.2.0
 */
public abstract class GrouperDAOFactory {

  /**
   * 
   */
  private static GrouperDAOFactory gdf;


  /**
   * Return singleton {@link GrouperDAOFactory} implementation.
   * <p/>
   * @return factory
   * @since   1.2.0
   */
  public static GrouperDAOFactory getFactory() {
    if (gdf == null) {
      gdf = getFactoryHelper( );
    }
    return gdf;
  } 

  /**
   * Return singleton {@link GrouperDAOFactory} implementation using the specified
   * configuration.
   * <p/>
   * @param cfg 
   * @return factory
   * @throws  IllegalArgumentException if <i>cfg</i> is null.
   * @since   1.2.1
   */
  private static GrouperDAOFactory getFactoryHelper() 
    throws  IllegalArgumentException
  {
    String            klass = GrouperConfig.retrieveConfig().propertyValueString(GrouperConfig.PROP_DAO_FACTORY);
    GrouperValidator  v     = NotNullOrEmptyValidator.validate(klass);
    if ( v.isInvalid() ) {
      klass = GrouperConfig.DEFAULT_DAO_FACTORY;
    }
    return (GrouperDAOFactory) Realize.instantiate(klass);
  }

  /**
   * @return attributeDef
   */
  public abstract AttributeDefDAO getAttributeDef();

  /**
   * @return attributeAssign
   */
  public abstract AttributeAssignDAO getAttributeAssign();

  /**
   * @return attributeAssign
   */
  public abstract AttributeAssignActionDAO getAttributeAssignAction();

  /**   
   * @return attributeAssign
   */
  public abstract AttributeAssignActionSetDAO getAttributeAssignActionSet();

  /**
   * @return attributeAssign
   */
  public abstract AttributeAssignValueDAO getAttributeAssignValue();

  /**
   * @return attributeDefScope
   */
  public abstract AttributeDefScopeDAO getAttributeDefScope();

  /**
   * @return attributeDefNameSet
   */
  public abstract AttributeDefNameSetDAO getAttributeDefNameSet();

  /**
   * @return attributeDefNameSetView
   */
  public abstract AttributeDefNameSetViewDAO getAttributeDefNameSetView();

  /**
   * @return attributeAssignActionSetView
   */
  public abstract AttributeAssignActionSetViewDAO getAttributeAssignActionSetView();

  /**
   * @return attributeDefName
   */
  public abstract AttributeDefNameDAO getAttributeDefName();

  /**
   * 
   * @return role dao
   */
  public abstract RoleDAO getRole();
  
  /**
   * 
   * @return permission entry dao
   */
  public abstract PermissionEntryDAO getPermissionEntry();
  
  /**
   * @return composite
   * @since   1.2.0
   */
  public abstract CompositeDAO getComposite();

  /**
   * @return audit entry dao
   * @since   1.2.0
   */
  public abstract AuditEntryDAO getAuditEntry();

  /**
   * @return audit type
   * @since   1.2.0
   */
  public abstract AuditTypeDAO getAuditType();

  /**
   * @return role set
   * @since   1.2.0
   */
  public abstract RoleSetDAO getRoleSet();

  /**
   * @return role set
   * @since   1.2.0
   */
  public abstract RoleSetViewDAO getRoleSetView();

  /**
   * @return change log dao
   * @since   1.2.0
   */
  public abstract ChangeLogEntryDAO getChangeLogEntry();

  /**
   * @return change log type
   * @since   1.2.0
   */
  public abstract ChangeLogTypeDAO getChangeLogType();

  /**
   * change log consumer dao
   * @return the change log consumer dao
   */
  public abstract ChangeLogConsumerDAO getChangeLogConsumer();
  
  /**
   * @return field
   * @since   1.2.0
   */
  public abstract FieldDAO getField();

  /**
   * @return group dao
   * @since   1.2.0
   */
  public abstract GroupDAO getGroup();

  /**
   * @return entity dao
   * @since   2.1.0
   */
  public abstract EntityDAO getEntity();

  /**
   * @return member dao
   * @since   1.2.0
   */
  public abstract MemberDAO getMember();

  /**
   * @return membership dao
   * @since   1.2.0
   */
  public abstract MembershipDAO getMembership();

  /**
   * @return registry dao
   * @since   1.2.0
   */
  public abstract RegistryDAO getRegistry();

  /**
   * @return registry subject dao
   * @since   1.2.0
   */
  public abstract RegistrySubjectDAO getRegistrySubject();

  /**
   * @return stem dao
   * @since   1.2.0
   */
  public abstract StemDAO getStem();


  /**
   * 
   */
  public static void internal_resetFactory() {
    gdf = null;
  }

  /**
   * get a hibernate session (note, this is a framework method
   * that should not be called outside of grouper hibernate framework methods
   * @return the session
   */
  public abstract Session getSession();
  
  /**
   * get a hibernate configuration (this is internal for grouper team only)
   * @return the configuration
   */
  public abstract Configuration getConfiguration();
  
  /**
   * return the transaction implementation
   * @since   1.3
   * @return the transaction implementation
   */
  public abstract TransactionDAO getTransaction();


  /**
   * @since 1.5.0
   * @return group set dao
   */
  public abstract GroupSetDAO getGroupSet();
  
  /**
   * @return pit attribute def dao
   */
  public abstract PITAttributeDefDAO getPITAttributeDef();
  
  /**
   * @return pit group dao
   */
  public abstract PITGroupDAO getPITGroup();
  
  /**
   * @return pit stem dao
   */
  public abstract PITStemDAO getPITStem();
  
  /**
   * @return pit field dao
   */
  public abstract PITFieldDAO getPITField();
  
  /**
   * @return pit member dao
   */
  public abstract PITMemberDAO getPITMember();
  
  /**
   * @return pit membership dao
   */
  public abstract PITMembershipDAO getPITMembership();
  
  /**
   * @return pit membership view dao
   */
  public abstract PITMembershipViewDAO getPITMembershipView();
  
  /**
   * @return pit group set dao
   */
  public abstract PITGroupSetDAO getPITGroupSet();

  /**
   * @return pit attribute assign dao
   */
  public abstract PITAttributeAssignDAO getPITAttributeAssign();
  
  /**
   * @return pit attribute assign value dao
   */
  public abstract PITAttributeAssignValueDAO getPITAttributeAssignValue();
  
  /**
   * @return pit attribute assign action dao
   */
  public abstract PITAttributeAssignActionDAO getPITAttributeAssignAction();
  
  /**
   * @return pit attribute assign action set dao
   */
  public abstract PITAttributeAssignActionSetDAO getPITAttributeAssignActionSet();
  
  /**
   * @return pit role set dao
   */
  public abstract PITRoleSetDAO getPITRoleSet();
  
  /**
   * @return pit attribute def name dao
   */
  public abstract PITAttributeDefNameDAO getPITAttributeDefName();
  
  /**
   * @return pit attribute def name set dao
   */
  public abstract PITAttributeDefNameSetDAO getPITAttributeDefNameSet();
  
  /**
   * @return pit permission all view dao
   */
  public abstract PITPermissionAllViewDAO getPITPermissionAllView();

  /**
   * @return external subject 
   */
  public abstract ExternalSubjectDAO getExternalSubject();

  /**
   * @return external subject attribute
   */
  public abstract ExternalSubjectAttributeDAO getExternalSubjectAttribute();

  /**
   * @return stem set
   */
  public abstract StemSetDAO getStemSet();

  /**
   * @return table index
   */
  public abstract TableIndexDAO getTableIndex();
} 

