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

/** 
 * Validation methods that apply to {@link GroupType}s.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeValidator.java,v 1.3 2006-08-22 19:48:22 blair Exp $
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
    // TODO Refactor.  There is too much going on here.
    GroupTypeValidator.canModifyField(s, gt);
    Field f = null;
    try {
      f = FieldFinder.find(name);  
    }
    catch (SchemaException eS) {
      // Ignore
    } 
    if (f != null) {
      throw new SchemaException(E.FIELD_ALREADY_EXISTS + name);
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
      throw new SchemaException(E.FIELD_INVALID_TYPE + ft);
    }
    if (!Privilege.isAccess(read)) {
      throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
    }
    if (!Privilege.isAccess(write)) {
      throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
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
    if (!PrivilegeResolver.isRoot(s.getSubject())) {
      throw new InsufficientPrivilegeException(E.GROUPTYPE_CANNOT_MODIFY_TYPE);
    }
    if (GroupType.isSystemType(type)) {
      throw new SchemaException(E.GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES);
    }
  } // protected static void canModifyField(s, type)

} // class GroupTypeValidator

