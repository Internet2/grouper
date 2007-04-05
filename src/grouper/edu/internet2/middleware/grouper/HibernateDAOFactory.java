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

package edu.internet2.middleware.grouper;

/** 
 * TODO 20070330
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateDAOFactory.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
class HibernateDAOFactory extends GrouperDAOFactory {

  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected HibernateCompositeDAO getComposite() {
    return new HibernateCompositeDAO();
  } 

  // @since   1.2.0
  protected HibernateFieldDAO getField() {
    return new HibernateFieldDAO();
  }

  // @since   1.2.0
  protected HibernateGroupDAO getGroup() {
    return new HibernateGroupDAO();
  }

  // @since   1.2.0
  protected HibernateGrouperSessionDAO getGrouperSession() {
    return new HibernateGrouperSessionDAO();
  } 

  // @since   1.2.0
  protected HibernateGroupTypeDAO getGroupType() {
    return new HibernateGroupTypeDAO();
  } 

  // @since   1.2.0
  protected HibernateMemberDAO getMember() {
    return new HibernateMemberDAO();
  } 

  // @since   1.2.0
  protected HibernateMembershipDAO getMembership() {
    return new HibernateMembershipDAO();
  } 

  // @since   1.2.0
  protected HibernateRegistryDAO getRegistry() {
    return new HibernateRegistryDAO();
  }

  // @since   1.2.0
  protected HibernateRegistrySubjectDAO getRegistrySubject() {
    return new HibernateRegistrySubjectDAO();
  } 

  // @since   1.2.0
  protected HibernateStemDAO getStem() {
    return new HibernateStemDAO();
  }

} // class HibernateDAOFactory extends GrouperDAOFactory

