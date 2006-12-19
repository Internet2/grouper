/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  net.sf.hibernate.*;

/** 
 * @author  blair christensen.
 * @version $Id: StemValidator.java,v 1.14 2006-12-19 18:56:44 blair Exp $
 * @since   1.0
 */
class StemValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static boolean canAddChildGroup(Stem ns, String extension, String displayExtension)
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    boolean rv = false;
    try {
      AttributeValidator.namingValue(extension);
      AttributeValidator.namingValue(displayExtension);
    }
    catch (ModelException eM) {
      throw new GroupAddException(eM.getMessage(), eM);
    }
    if (!PrivilegeResolver.canCREATE( ns.getSession(), ns, ns.getSession().getSubject() )) {
      throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
    }
    if (ns.isRootStem()) {
      throw new GroupAddException("cannot create groups at root stem level");
    }
    try {
      GroupFinder.findByName( U.constructName(ns.getName(), extension) );
      throw new GroupAddException("group already exists");
    }
    catch (GroupNotFoundException eGNF) {
      rv = true; // Group does not exist.  This is what we want.
    }
    return rv;
  } // protected static boolean canAddChildGroup(ns, extension, displayExtension)

  // @since   1.1.0
  protected static boolean canAddChildStem(Stem ns, String extension, String displayExtension)
    throws  InsufficientPrivilegeException,
            StemAddException
  {
    boolean rv = false;
    try {
      AttributeValidator.namingValue(extension);
      AttributeValidator.namingValue(displayExtension);
    }
    catch (ModelException eM) {
      throw new StemAddException(eM.getMessage(), eM);
    }
    if (!RootPrivilegeResolver.canSTEM( ns, ns.getSession().getSubject()) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    } 
    try {
      StemFinder.internal_findByName( U.constructName(ns.getName(), extension) );
      throw new StemAddException("stem already exists");
    }
    catch (StemNotFoundException eSNF) {
      rv = true; // Stem does not exist.  This is what we want.
    }
    return rv;
  } // protected static boolean canAddChildStem(ns, extension, displayExtension)

  // @since 1.0
  protected static void canDeleteStem(Stem ns) 
    throws  InsufficientPrivilegeException,
            StemDeleteException
  {
    if ( ns.getName().equals(Stem.ROOT_EXT) ) {
      throw new StemDeleteException("cannot delete root stem");
    }
    if ( !PrivilegeResolver.canSTEM( ns, ns.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    if ( HibernateStemDAO.findChildStems(ns).size() > 0 ) {
      throw new StemDeleteException("cannot delete stem with child stems");
    }
    if ( HibernateStemDAO.findChildGroups(ns).size() > 0 ) {
      throw new StemDeleteException("cannot delete stem with child groups");
    }
  } // protected static void canDeleteStem(ns)

  // @since 1.0
  protected static void canWriteField(
    Stem ns, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // Validate the field type
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    PrivilegeResolver.canPrivDispatch(ns.getSession(), ns, subj, f.getWritePriv());
  } // protected static void canWriteField(ns, subj, f, type)

}

