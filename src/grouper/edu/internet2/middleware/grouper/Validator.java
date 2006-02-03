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
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;


/** 
 * Validator Utility Class.
 * <p />
 * @author  blair christensen.
 * @version $Id: Validator.java,v 1.2 2006-02-03 20:20:15 blair Exp $
 */
class Validator implements Serializable {

  // Protected Class Methods

  protected static void canAddFieldToType(
    GrouperSession s, GroupType gt, String name, FieldType ft, Privilege read, Privilege write
  ) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    _canModifyField(s, gt);
    Field f = null;
    try {
      f = FieldFinder.find(name);  
    }
    catch (SchemaException eS) {
      // Ignore
    } 
    if (f != null) {
      throw new SchemaException("field already exists");
    }
    if 
    (
      !(
        (ft.toString().equals(FieldType.ATTRIBUTE.toString()) ) 
        || 
        (ft.toString().equals(FieldType.LIST.toString())      ) 
      )
    )
    {
      throw new SchemaException("invalid field type");
    }
    if (!Privilege.isAccess(read)) {
      throw new SchemaException("read privilege not access privilege");
    }
    if (!Privilege.isAccess(write)) {
      throw new SchemaException("write privilege not access privilege");
    }
  } // protected static void canAddFieldToType(s, gt, name, ft, read, write)

  protected static void canAddGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (g.hasType(type)) {
      throw new GroupModifyException("already has type");
    }
    _canModifyGroupType(s, g, type);
  } // protected static void canAddGroupType(s, g, type)

  protected static void canDeleteFieldFromType(
    GrouperSession s, GroupType type, Field f
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    _canModifyField(s, type);
    if (!f.getGroupType().equals(type)) {
      throw new SchemaException("field does not belong to this group type");
    }
  } // protected static void canDeleteFieldFromType(s, type, f)

  protected static void canDeleteGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (!g.hasType(type)) {
      throw new GroupModifyException("does not have type");
    }
    _canModifyGroupType(s, g, type);
  } // protected static void canAddGroupType(s, g, type)


  // Private Class Methods

  private static void _canModifyField(GrouperSession s, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (!PrivilegeResolver.getInstance().isRoot(s.getSubject())) {
      throw new InsufficientPrivilegeException("not privileged to modify fields");
    }
    if (GroupType.isSystemType(type)) {
      throw new SchemaException("cannot modify fields on system-maintained groups");
    }
  } // private static void _canModifyField(s, type)

  private static void _canModifyGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (GroupType.isSystemType(type)) {
      throw new SchemaException("cannot edit system group types");
    }
    PrivilegeResolver.getInstance().canADMIN(s, g, s.getSubject());
  } // private static void _canModifyGroupType(s, g, type)

}

