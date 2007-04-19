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
import  edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDAOFactory;

/** 
 * Factor for returning <code>GrouperDAO</code> objects.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperDAOFactory.java,v 1.4 2007-04-19 16:48:43 blair Exp $
 * @since   1.2.0
 */
public abstract class GrouperDAOFactory {

  // CONSTRUCTORS //

  // @since   1.2.0
  public GrouperDAOFactory() {
    super();
  } // public GrouperDAOFactory()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static GrouperDAOFactory getFactory() {
    // TODO 20070403 cache?  singleton?
    // TODO 20070416 use reflection to instantiate right class
    return new HibernateDAOFactory();
  } // public static GrouperDAOFactory getFactory()


  // PROTECTED ABSTRACT INSTANCE METHODS //

  // TODO 20070403 add static class methods that call these?

  // @since   1.2.0
  public abstract CompositeDAO getComposite();

  // @since   1.2.0
  public abstract FieldDAO getField();

  // @since   1.2.0
  public abstract GroupDAO getGroup();

  // @since   1.2.0
  public abstract GrouperSessionDAO getGrouperSession();

  // @since   1.2.0
  public abstract GroupTypeDAO getGroupType();

  // @since   1.2.0
  public abstract MemberDAO getMember();

  // @since   1.2.0
  public abstract MembershipDAO getMembership();

  // @since   1.2.0
  public abstract RegistryDAO getRegistry();

  // @since   1.2.0
  public abstract RegistrySubjectDAO getRegistrySubject();

  // @since   1.2.0
  public abstract StemDAO getStem();

} // abstract class GrouperDAOFactory

