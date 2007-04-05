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
 * @version $Id: GrouperDAOFactory.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
abstract class GrouperDAOFactory {

  // CONSTRUCTORS //

  // @since   1.2.0
  protected GrouperDAOFactory() {
    super();
  } // protected GrouperDAOFactory()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static GrouperDAOFactory getFactory() {
    // TODO 20070403 cache?  singleton?
    return new HibernateDAOFactory();
  } // protected static GrouperDAOFactory getFactory()


  // PROTECTED ABSTRACT INSTANCE METHODS //

  // TODO 20070403 add static class methods that call these?

  // @since   1.2.0
  protected abstract CompositeDAO getComposite();

  // @since   1.2.0
  protected abstract FieldDAO getField();

  // @since   1.2.0
  protected abstract GroupDAO getGroup();

  // @since   1.2.0
  protected abstract GrouperSessionDAO getGrouperSession();

  // @since   1.2.0
  protected abstract GroupTypeDAO getGroupType();

  // @since   1.2.0
  protected abstract MemberDAO getMember();

  // @since   1.2.0
  protected abstract MembershipDAO getMembership();

  // @since   1.2.0
  protected abstract RegistryDAO getRegistry();

  // @since   1.2.0
  protected abstract RegistrySubjectDAO getRegistrySubject();

  // @since   1.2.0
  protected abstract StemDAO getStem();

} // abstract class GrouperDAOFactory

