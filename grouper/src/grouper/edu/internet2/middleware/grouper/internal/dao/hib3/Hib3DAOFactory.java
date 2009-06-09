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

import edu.internet2.middleware.grouper.internal.dao.AttributeDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogConsumerDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupSetDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.RegistryDAO;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dao.TransactionDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * Basic Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAOFactory.java,v 1.11 2009-06-09 22:55:39 shilen Exp $
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
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getGroupType()
   */
  public GroupTypeDAO getGroupType() {
    return new Hib3GroupTypeDAO();
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
   * @see edu.internet2.middleware.grouper.misc.GrouperDAOFactory#getAttribute()
   */
  @Override
  public AttributeDAO getAttribute() {
    return new Hib3AttributeDAO();
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
} 

