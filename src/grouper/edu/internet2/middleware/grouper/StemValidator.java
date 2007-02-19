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
import  edu.internet2.middleware.subject.*;

/** 
 * @author  blair christensen.
 * @version $Id: StemValidator.java,v 1.21 2007-02-19 17:53:48 blair Exp $
 * @since   1.0
 */
class StemValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static boolean internal_canAddChildGroup(Stem ns, String extension, String displayExtension)
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    boolean rv = false;
    NamingValidator nv = NamingValidator.validateName(extension);
    if ( !nv.getIsValid() ) {
      throw new GroupAddException( nv.getErrorMessage() );
    }
    nv = NamingValidator.validateName(displayExtension);
    if ( !nv.getIsValid() ) {
      throw new GroupAddException( nv.getErrorMessage() );
    }
    if (!PrivilegeResolver.internal_canCREATE( ns.getSession(), ns, ns.getSession().getSubject() )) {
      throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
    }
    if (ns.isRootStem()) {
      throw new GroupAddException("cannot create groups at root stem level");
    }
    try {
      HibernateGroupDAO.findByName( U.internal_constructName(ns.getName(), extension) );
      throw new GroupAddException("group already exists");
    }
    catch (GroupNotFoundException eGNF) {
      rv = true; // Group does not exist.  This is what we want.
    }
    return rv;
  } // protected static boolean internal_canAddChildGroup(ns, extension, displayExtension)

  // @since   1.2.0
  protected static boolean internal_canAddChildStem(Stem ns, String extension, String displayExtension)
    throws  InsufficientPrivilegeException,
            StemAddException
  {
    boolean rv = false;
    NamingValidator nv = NamingValidator.validateName(extension);
    if ( !nv.getIsValid() ) {
      throw new StemAddException( nv.getErrorMessage() );
    }
    nv = NamingValidator.validateName(displayExtension);
    if ( !nv.getIsValid() ) {
      throw new StemAddException( nv.getErrorMessage() );
    }
    if (!RootPrivilegeResolver.internal_canSTEM( ns, ns.getSession().getSubject()) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    } 
    try {
      StemFinder.internal_findByName( U.internal_constructName(ns.getName(), extension) );
      throw new StemAddException("stem already exists");
    }
    catch (StemNotFoundException eSNF) {
      rv = true; // Stem does not exist.  This is what we want.
    }
    return rv;
  } // protected static boolean internal_canAddChildStem(ns, extension, displayExtension)

  // @since   1.2.0
  protected static void internal_canDeleteStem(Stem ns) 
    throws  InsufficientPrivilegeException,
            StemDeleteException
  {
    if ( ns.getName().equals(Stem.ROOT_EXT) ) {
      throw new StemDeleteException("cannot delete root stem");
    }
    if ( !PrivilegeResolver.internal_canSTEM( ns, ns.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    if ( HibernateStemDAO.findChildStems(ns).size() > 0 ) {
      throw new StemDeleteException("cannot delete stem with child stems");
    }
    if ( HibernateStemDAO.findChildGroups(ns).size() > 0 ) {
      throw new StemDeleteException("cannot delete stem with child groups");
    }
  } // protected static void internal_canDeleteStem(ns)

  // @since   1.2.0
  protected static void internal_canWriteField(
    Stem ns, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // Validate the field type
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    PrivilegeResolver.internal_canPrivDispatch(ns.getSession(), ns, subj, f.getWritePriv());
  } // protected static void internal_canWriteField(ns, subj, f, type)

}

