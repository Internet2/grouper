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
 * @version $Id: GroupValidator.java,v 1.33 2007-03-05 20:23:22 blair Exp $
 * @since   1.0
 */
class GroupValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_canAddMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    if ( !g.canWriteField(f) ) { 
      try {
        internal_canOptin(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP1) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberAddException(E.GROUP_AMTC);
    }
  } // protected static void internal_canAddMember(g, subj, f)

  // @since   1.2.0
  protected static void internal_canDelAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (f.getRequired()) {
      throw new ModelException(E.GROUP_DRA + f.getName());
    }
    if ( !g.canWriteField(f) ) {
      throw new InsufficientPrivilegeException();
    }
  } // protected static void internal_canDelAttribute(g, f, value)

  // @since   1.2.0
  protected static void internal_canDelCompositeMember(Group g)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if ( !g.canWriteField( g.getSession().getSubject(), Group.getDefaultList() ) ) {
      throw new InsufficientPrivilegeException();
    }
    if (!g.hasComposite()) {
      throw new ModelException(E.GROUP_DCFC); 
    }
  } // protected static void internal_canDelCompositeMember(g)

  // @since   1.2.0
  protected static void internal_canDelMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            SchemaException
  {
    if ( !g.canWriteField(f) ) {
      try {
        internal_canOptout(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberDeleteException(E.GROUP_DMFC);
    }
  } // protected static void internal_canDelMember(g, subj, f)

  // @since   1.2.0
  protected static void internal_canOptin(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.internal_eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(E.GROUP_COI);
    } 
    if (!PrivilegeResolver.internal_canOPTIN(g.getSession(), g, subj)) {
      throw new InsufficientPrivilegeException(E.CANNOT_OPTIN);
    }
  } // protected static void internal_canOptin(g, subj, f)

  // @since   1.2.0
  protected static void internal_canOptout(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.internal_eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(E.GROUP_COO);
    } 
    if (!PrivilegeResolver.internal_canOPTOUT(g.getSession(), g, subj)) {
      throw new InsufficientPrivilegeException(E.CANNOT_OPTOUT);
    }
  } // protected static void internal_canOptout(g, subj, f)

  // @return  Attribute as {@link Field}
  // @since   1.2.0
  protected static Field internal_canSetAttribute(Group g, String attr, String value) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(attr);
    if ( !v.getIsValid() ) {
      throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
    }
    v = NotNullOrEmptyValidator.validate(value);
    if ( !v.getIsValid() ) {
      throw new GroupModifyException(E.INVALID_ATTR_VALUE + value);
    }
    if (
          attr.equals(GrouperConfig.ATTR_DE)
      ||  attr.equals(GrouperConfig.ATTR_DN)
      ||  attr.equals(GrouperConfig.ATTR_E)
    )
    {
      NamingValidator nv = NamingValidator.validate(value);
      if ( !nv.getIsValid() ) {
        throw new ModelException( nv.getErrorMessage() );
      }
    }
    Field f = FieldFinder.find(attr);
    if ( !g.canWriteField(f) ) {
      throw new InsufficientPrivilegeException();
    }
    return f;
  } // protected static Field internal_canSetAttribute(g, attr, value)

}

