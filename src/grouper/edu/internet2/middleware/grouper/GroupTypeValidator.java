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
 * @version $Id: GroupTypeValidator.java,v 1.9 2007-02-08 16:25:25 blair Exp $
 * @since   1.1.0
 */
class GroupTypeValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_canAddFieldToType(
    GrouperSession s, GroupType gt, String name, FieldType ft, Privilege read, Privilege write
  ) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (!RootPrivilegeResolver.internal_isRoot(s)) {
      throw new InsufficientPrivilegeException(E.GROUPTYPE_CANNOT_MODIFY_TYPE);
    }
    // 20070206 moved system-type validation to public methods for adding fields
    boolean exists = false;
    try {
      FieldFinder.find(name);
      exists = true;
    }
    catch (SchemaException eS) {
      // The field doesn't exist.  Now see if it can be created.
      // 20070206 eliminated FieldType validation check
      // 20070206 moved priv-type validationt to public methods for adding fields
    } 
    if (exists) {
      throw new SchemaException(E.FIELD_ALREADY_EXISTS + name);
    }
  } // protected static void internal-canAddFieldToType(s, gt, name, ft, read, write)

  // @since   1.2.0 
  protected static void internal_canDeleteFieldFromType(
    GrouperSession s, GroupType type, Field f
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    internal_canModifyField(s, type);
    if (!f.getGroupType().equals(type)) {
      throw new SchemaException(E.FIELD_DOES_NOT_BELONG_TO_TYPE + f.getGroupType());
    }
  } // protected static void internal_canDeleteFieldFromType(s, type, f)

  // @since   1.2.0
  protected static void internal_canModifyField(GrouperSession s, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (!RootPrivilegeResolver.internal_isRoot(s)) {
      throw new InsufficientPrivilegeException(E.GROUPTYPE_CANNOT_MODIFY_TYPE);
    }
    if ( GroupType.internal_isSystemType(type) ) {
      throw new SchemaException(E.GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES);
    }
  } // protected static void internal_canModifyField(s, type)

} // class GroupTypeValidator

