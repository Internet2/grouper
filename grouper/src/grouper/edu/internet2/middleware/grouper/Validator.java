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
 * @version $Id: Validator.java,v 1.1 2006-02-03 19:39:52 blair Exp $
 */
class Validator implements Serializable {

  // Protected Class Methods

  protected static void canAddGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (g.hasType(type)) {
      throw new GroupModifyException("already has type");
    }
    _canEditGroupType(s, g, type);
  } // protected static void canAddGroupType(s, g, type)

  protected static void canDeleteGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (!g.hasType(type)) {
      throw new GroupModifyException("does not have type");
    }
    _canEditGroupType(s, g, type);
  } // protected static void canAddGroupType(s, g, type)


  // Private Class Methods

  private static void _canEditGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (GroupType.isSystemType(type)) {
      throw new SchemaException("cannot edit system group types");
    }
    PrivilegeResolver.getInstance().canADMIN(s, g, s.getSubject());
  } // private static void _canEditGroupType(s, g, type)

}

