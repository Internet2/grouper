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
 * @version $Id: HibernateDAOFactory.java,v 1.2 2007-04-12 15:40:41 blair Exp $
 * @since   1.2.0
 */
class HibernateDAOFactory extends GrouperDAOFactory {

  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected CompositeDAO getComposite() {
    return new HibernateCompositeDAO();
  } 

  // @since   1.2.0
  protected FieldDAO getField() {
    return new HibernateFieldDAO();
  }

  // @since   1.2.0
  protected GroupDAO getGroup() {
    return new HibernateGroupDAO();
  }

  // @since   1.2.0
  protected GrouperSessionDAO getGrouperSession() {
    return new HibernateGrouperSessionDAO();
  } 

  // @since   1.2.0
  protected GroupTypeDAO getGroupType() {
    return new HibernateGroupTypeDAO();
  } 

  // @since   1.2.0
  protected MemberDAO getMember() {
    return new HibernateMemberDAO();
  } 

  // @since   1.2.0
  protected MembershipDAO getMembership() {
    return new HibernateMembershipDAO();
  } 

  // @since   1.2.0
  protected RegistryDAO getRegistry() {
    return new HibernateRegistryDAO();
  }

  // @since   1.2.0
  protected RegistrySubjectDAO getRegistrySubject() {
    return new HibernateRegistrySubjectDAO();
  } 

  // @since   1.2.0
  protected StemDAO getStem() {
    return new HibernateStemDAO();
  }

} 

