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

import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import  edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import  edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperSessionDAO;
import  edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import  edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import  edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import  edu.internet2.middleware.grouper.internal.dao.RegistryDAO;
import  edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import  edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dao.TransactionDAO;

/** 
 * Basic Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAOFactory.java,v 1.3.4.2 2008-06-18 09:22:21 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3DAOFactory extends GrouperDAOFactory {

  // PROTECTED INSTANCE METHODS //

  // @since   @HEAD@
  public CompositeDAO getComposite() {
    return new Hib3CompositeDAO();
  } 

  // @since   @HEAD@
  public FieldDAO getField() {
    return new Hib3FieldDAO();
  }

  // @since   @HEAD@
  public GroupDAO getGroup() {
    return new Hib3GroupDAO();
  }

  // @since   @HEAD@
  public GrouperSessionDAO getGrouperSession() {
    return new Hib3GrouperSessionDAO();
  } 

  // @since   @HEAD@
  public GroupTypeDAO getGroupType() {
    return new Hib3GroupTypeDAO();
  } 

  // @since   @HEAD@
  public MemberDAO getMember() {
    return new Hib3MemberDAO();
  } 

  // @since   @HEAD@
  public MembershipDAO getMembership() {
    return new Hib3MembershipDAO();
  } 

  // @since   @HEAD@
  public RegistryDAO getRegistry() {
    return new Hib3RegistryDAO();
  }

  // @since   @HEAD@
  public RegistrySubjectDAO getRegistrySubject() {
    return new Hib3RegistrySubjectDAO();
  } 

  // @since   @HEAD@
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
   * @see edu.internet2.middleware.grouper.GrouperDAOFactory#getConfiguration()
   */
  @Override
  public Configuration getConfiguration() {
    return Hib3DAO.getConfiguration();
  }

} 

