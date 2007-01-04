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

/** 
 * Validation methods that apply to {@link GroupType}s.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeValidator.java,v 1.6 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0
 */
class GroupTypeValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static void canAddFieldToType(
    GrouperSession s, GroupType gt, String name, FieldType ft, Privilege read, Privilege write
  ) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    GroupTypeValidator.canModifyField(s, gt);
    try {
      FieldFinder.find(name);
      throw new SchemaException(E.FIELD_ALREADY_EXISTS + name);
    }
    catch (SchemaException eS) {
      // The field doesn't exist.  Now see if it can be created.
      _isRightFieldType(ft.toString());
      if (!Privilege.isAccess(read)) {
        throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
      }
      if (!Privilege.isAccess(write)) {
        throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
      }
    } 
  } // protected static void canAddFieldToType(s, gt, name, ft, read, write)

  // @since   1.1.0 
  protected static void canDeleteFieldFromType(
    GrouperSession s, GroupType type, Field f
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    canModifyField(s, type);
    if (!f.getGroupType().equals(type)) {
      throw new SchemaException(E.FIELD_DOES_NOT_BELONG_TO_TYPE + f.getGroupType());
    }
  } // protected static void canDeleteFieldFromType(s, type, f)

  // @since   1.1.0
  protected static void canModifyField(GrouperSession s, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (!RootPrivilegeResolver.isRoot(s)) {
      throw new InsufficientPrivilegeException(E.GROUPTYPE_CANNOT_MODIFY_TYPE);
    }
    if (GroupType.isSystemType(type)) {
      throw new SchemaException(E.GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES);
    }
  } // protected static void canModifyField(s, type)


  // PRIVATE CLASS METHODS // 
  
  // @throws  SchemaException
  // @since   1.1.0
  private static void _isRightFieldType(String type) 
    throws  SchemaException 
  {
    if 
    (
      !(
        (type.equals(FieldType.ATTRIBUTE.toString()) ) 
        || 
        (type.equals(FieldType.LIST.toString())      ) 
      )
    )
    {
      throw new SchemaException(E.FIELD_INVALID_TYPE + type);
    }
  } // private static void _isRightFieldType(type)

} // class GroupTypeValidator

