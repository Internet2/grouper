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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import org.hibernate.Session;

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

/** 
 * Basic Hibernate DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateDAOFactory.java,v 1.4 2008-02-19 07:50:47 mchyzer Exp $
 * @since   1.2.0
 */
public class HibernateDAOFactory extends GrouperDAOFactory {

  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  public CompositeDAO getComposite() {
    return new HibernateCompositeDAO();
  } 

  // @since   1.2.0
  public FieldDAO getField() {
    return new HibernateFieldDAO();
  }

  // @since   1.2.0
  public GroupDAO getGroup() {
    return new HibernateGroupDAO();
  }

  // @since   1.2.0
  public GrouperSessionDAO getGrouperSession() {
    return new HibernateGrouperSessionDAO();
  } 

  // @since   1.2.0
  public GroupTypeDAO getGroupType() {
    return new HibernateGroupTypeDAO();
  } 

  // @since   1.2.0
  public MemberDAO getMember() {
    return new HibernateMemberDAO();
  } 

  // @since   1.2.0
  public MembershipDAO getMembership() {
    return new HibernateMembershipDAO();
  } 

  // @since   1.2.0
  public RegistryDAO getRegistry() {
    return new HibernateRegistryDAO();
  }

  // @since   1.2.0
  public RegistrySubjectDAO getRegistrySubject() {
    return new HibernateRegistrySubjectDAO();
  } 

  // @since   1.2.0
  public StemDAO getStem() {
    return new HibernateStemDAO();
  }

  /**
   * get a hibernate session (note, this is a framework method
   * that should not be called outside of grouper hibernate framework methods
   * @return the session
   */
  @Override
  public Session getSession() {
    throw new RuntimeException("Cant instantiate hib3 from hib2");
  }

} 

