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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

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
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * Basic Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAOFactory.java,v 1.19 2009-10-26 02:26:07 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3DAOFactory extends GrouperDAOFactory {


  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAuditEntry()
   */
  public AuditEntryDAO getAuditEntry() {
    return new Hib3AuditEntryDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAuditType()
   */
  public AuditTypeDAO getAuditType() {
    return new Hib3AuditTypeDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getComposite()
   */
  public CompositeDAO getComposite() {
    return new Hib3CompositeDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getField()
   */
  public FieldDAO getField() {
    return new Hib3FieldDAO();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getGroup()
   */
  public GroupDAO getGroup() {
    return new Hib3GroupDAO();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getMember()
   */
  public MemberDAO getMember() {
    return new Hib3MemberDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getMembership()
   */
  public MembershipDAO getMembership() {
    return new Hib3MembershipDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getRegistry()
   */
  public RegistryDAO getRegistry() {
    return new Hib3RegistryDAO();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getRegistrySubject()
   */
  public RegistrySubjectDAO getRegistrySubject() {
    return new Hib3RegistrySubjectDAO();
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getStem()
   */
  public StemDAO getStem() {
    return new Hib3StemDAO();
  }

  /**
   * get a hibernate session (note, this is a framework method
   * that should not be called outside of grouper hibernate framework methods
   * @return the session
   */
  @Override
  public Session getSession() {
    return Hib3DAO.session();
  }

  /**
   * return the transaction implementation
   * @since   1.3
   * @return the transaction implementation
   */
  @Override
  public TransactionDAO getTransaction() {
    return new Hib3TransactionDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getConfiguration()
   */
  @Override
  public Configuration getConfiguration() {
    return Hib3DAO.getConfiguration();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getChangeLogEntry()
   */
  @Override
  public ChangeLogEntryDAO getChangeLogEntry() {
    return new Hib3ChangeLogEntryDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getChangeLogType()
   */
  @Override
  public ChangeLogTypeDAO getChangeLogType() {
    return new Hib3ChangeLogTypeDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getGroupSet()
   */
  @Override
  public GroupSetDAO getGroupSet() {
    return new Hib3GroupSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getChangeLogConsumer()
   */
  @Override
  public ChangeLogConsumerDAO getChangeLogConsumer() {
    return new Hib3ChangeLogConsumerDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeDef()
   */
  @Override
  public AttributeDefDAO getAttributeDef() {
    return new Hib3AttributeDefDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeDefName()
   */
  @Override
  public AttributeDefNameDAO getAttributeDefName() {
    return new Hib3AttributeDefNameDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeAssign()
   */
  @Override
  public AttributeAssignDAO getAttributeAssign() {
    return new Hib3AttributeAssignDAO();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPermissionEntry()
   */
  @Override
  public PermissionEntryDAO getPermissionEntry() {
    return new Hib3PermissionEntryDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeAssignValue()
   */
  @Override
  public AttributeAssignValueDAO getAttributeAssignValue() {
    return new Hib3AttributeAssignValueDAO();
  }

  /**
   * 
   */
  @Override
  public AttributeDefScopeDAO getAttributeDefScope() {
    return new Hib3AttributeDefScopeDAO();
  }

  /**
   * @see GrouperDAOFactory#getAttributeDefNameSet()
   */
  @Override
  public AttributeDefNameSetDAO getAttributeDefNameSet() {
    return new Hib3AttributeDefNameSetDAO();
  }

  /**
   * @see GrouperDAOFactory#getAttributeDefNameSetView()
   */
  @Override
  public AttributeDefNameSetViewDAO getAttributeDefNameSetView() {
    return new Hib3AttributeDefNameSetViewDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getRoleSet()
   */
  @Override
  public RoleSetDAO getRoleSet() {
    return new Hib3RoleSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getRole()
   */
  @Override
  public RoleDAO getRole() {
    return new Hib3RoleDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getRoleSetView()
   */
  @Override
  public RoleSetViewDAO getRoleSetView() {
    return new Hib3RoleSetViewDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeAssignAction()
   */
  @Override
  public AttributeAssignActionDAO getAttributeAssignAction() {
    return new Hib3AttributeAssignActionDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeAssignActionSet()
   */
  @Override
  public AttributeAssignActionSetDAO getAttributeAssignActionSet() {
    return new Hib3AttributeAssignActionSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttributeAssignActionSetView()
   */
  @Override
  public AttributeAssignActionSetViewDAO getAttributeAssignActionSetView() {
    return new Hib3AttributeAssignActionSetViewDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeDef()
   */
  @Override
  public PITAttributeDefDAO getPITAttributeDef() {
    return new Hib3PITAttributeDefDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITField()
   */
  @Override
  public PITFieldDAO getPITField() {
    return new Hib3PITFieldDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITGroup()
   */
  @Override
  public PITGroupDAO getPITGroup() {
    return new Hib3PITGroupDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITGroupSet()
   */
  @Override
  public PITGroupSetDAO getPITGroupSet() {
    return new Hib3PITGroupSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITMember()
   */
  @Override
  public PITMemberDAO getPITMember() {
    return new Hib3PITMemberDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITMembership()
   */
  @Override
  public PITMembershipDAO getPITMembership() {
    return new Hib3PITMembershipDAO();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITMembershipView()
   */
  @Override
  public PITMembershipViewDAO getPITMembershipView() {
    return new Hib3PITMembershipViewDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITStem()
   */
  @Override
  public PITStemDAO getPITStem() {
    return new Hib3PITStemDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeAssign()
   */
  @Override
  public PITAttributeAssignDAO getPITAttributeAssign() {
    return new Hib3PITAttributeAssignDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeAssignValue()
   */
  @Override
  public PITAttributeAssignValueDAO getPITAttributeAssignValue() {
    return new Hib3PITAttributeAssignValueDAO();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeAssignAction()
   */
  @Override
  public PITAttributeAssignActionDAO getPITAttributeAssignAction() {
    return new Hib3PITAttributeAssignActionDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeAssignActionSet()
   */
  @Override
  public PITAttributeAssignActionSetDAO getPITAttributeAssignActionSet() {
    return new Hib3PITAttributeAssignActionSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeDefName()
   */
  @Override
  public PITAttributeDefNameDAO getPITAttributeDefName() {
    return new Hib3PITAttributeDefNameDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITAttributeDefNameSet()
   */
  @Override
  public PITAttributeDefNameSetDAO getPITAttributeDefNameSet() {
    return new Hib3PITAttributeDefNameSetDAO();
  }

  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITRoleSet()
   */
  @Override
  public PITRoleSetDAO getPITRoleSet() {
    return new Hib3PITRoleSetDAO();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getPITPermissionAllView()
   */
  @Override
  public PITPermissionAllViewDAO getPITPermissionAllView() {
    return new Hib3PITPermissionAllViewDAO();
  }

  /**
   * @see GrouperDAOFactory#getExternalSubject()
   */
  @Override
  public ExternalSubjectDAO getExternalSubject() {
    return new Hib3ExternalSubjectDAO();
  }

  /**
   * @see GrouperDAOFactory#getExternalSubjectAttribute()
   */
  @Override
  public ExternalSubjectAttributeDAO getExternalSubjectAttribute() {
    return new Hib3ExternalSubjectAttributeDAO();
  }

  /**
   * @see GrouperDAOFactory#getEntity()
   */
  @Override
  public EntityDAO getEntity() {
    return new Hib3EntityDAO();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getStemSet()
   */
  @Override
  public StemSetDAO getStemSet() {
    return new Hib3StemSetDAO();
  }

  /**
   * @see GrouperDAOFactory#getTableIndex()
   */
  @Override
  public TableIndexDAO getTableIndex() {
    return new Hib3TableIndexDAO();
  }
} 

