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

/** 
 * @author  blair christensen.
 * @version $Id: GroupValidator.java,v 1.9 2006-06-16 15:01:46 blair Exp $
 * @since   1.0
 */
class GroupValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void canAddCompositeMember(Group g, Composite c)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    Field f = Group.getDefaultList();
    isTypeEqual(f, FieldType.LIST);
    canWriteField(g.getSession(), g, g.getSession().getSubject(), f);
    if (g.hasComposite()) {
      throw new ModelException(E.GROUP_ACTC); // TODO TEST!
    }
    if (g.getMembers().size() > 0) {
      throw new ModelException(E.GROUP_ACTM); // TODO ModelException
    }
  } // protected static void canAddCompositeMember(g, c)

  // @since 1.0
  protected static void canAddMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    try {
      isTypeEqual(f, FieldType.LIST);
      canWriteField(g.getSession(), g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP0) {
      try {
        canOptin(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP1) {
        // Throw with original message
        throw new InsufficientPrivilegeException(eIP0.getMessage(), eIP0);
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberAddException(E.GROUP_AMTC); // TODO ModelException
    }
  } // protected static void canAddMember(g, subj, f)

  // @since 1.0
  protected static void canDelAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (f.getRequired()) {
      throw new ModelException(E.GROUP_DRA + f.getName());
    }
    canModAttribute(g, f);
  } // protected static void canDelAttribute(g, f, value)

  // @since 1.0
  protected static void canDelCompositeMember(Group g)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    Field f = Group.getDefaultList();
    isTypeEqual(f, FieldType.LIST); // TODO Why do I bother?
    canWriteField(g.getSession(), g, g.getSession().getSubject(), f);
    if (!g.hasComposite()) {
      throw new ModelException(E.GROUP_DCFC); 
    }
  } // protected static void canDelCompositeMember(g)

  // @since 1.0
  protected static void canDelMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            SchemaException
  {
    try {
      isTypeEqual(f, FieldType.LIST);
      canWriteField(g.getSession(), g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP0) {
      try {
        canOptout(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP1) {
        // Throw with original message
        throw new InsufficientPrivilegeException(eIP0.getMessage(), eIP0);
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberDeleteException(E.GROUP_DMFC); // TODO ModelException
    }
  } // protected static void canDelMember(g, subj, f)

  // @since 1.0
  protected static void canDelGroupType(GrouperSession s, Group g, GroupType type) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (!g.hasType(type)) {
      throw new ModelException("does not have type");
    }
    canModGroupType(s, g, type);
  } // protected static void canDelGroupType(s, g, type)

  // @since 1.0
  protected static void canModAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    isTypeEqual(f, FieldType.ATTRIBUTE);
    canWriteField(g.getSession(), g, g.getSession().getSubject(), f);
  } // protected static void canModAttribute(g, f)

  // @since 1.0
  protected static void canModGroupType(GrouperSession s, Group g, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (GroupType.isSystemType(type)) {
      throw new SchemaException("cannot edit system group types");
    }
    PrivilegeResolver.getInstance().canADMIN(s, g, s.getSubject());
  } // protected static void canModGroupType(s, g, type)

  // @since 1.0
  protected static void canOptin(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(E.GROUP_COI);
    } 
    PrivilegeResolver.getInstance().canOPTIN(g.getSession(), g, subj);
  } // protected static void canOptin(g, subj, f)

  // @since 1.0
  protected static void canOptout(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(E.GROUP_COO);
    } 
    PrivilegeResolver.getInstance().canOPTOUT(g.getSession(), g, subj);
  } // protected static void canOptin(g, subj, f)

  // @since 1.0
  protected static void canReadField(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // FIXME Can I remove s?
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.GROUP_GT + f.getGroupType().toString());
    }
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, g, subj, f.getReadPriv()
    );
  } // protected static void canReadField(s, g, subj, f)

  // @since 1.0
  protected static void canSetAttribute(Group g, Field f, String value) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if ( (value == null) || (value.equals(GrouperConfig.EMPTY_STRING)) ) {
      throw new ModelException(E.GROUP_AV);
    }
    if (f.getName().equals("displayExtension")) {
      AttributeValidator.namingValue(value);
    }
    if (f.getName().equals("displayName")) {
      AttributeValidator.namingValue(value);
    }
    canModAttribute(g, f);
  } // protected static void canSetAttribute(g, f, value)

  // @since 1.0
  protected static void canWriteField(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // FIXME Can I remove s?
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.GROUP_GT + f.getGroupType().toString());
    }
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, g, subj, f.getWritePriv()
    );
  } // protected static void canWriteField(s, g, subj, f)

  // is this field of the appropriate type
  // @since 1.0
  protected static void isTypeEqual(Field f, FieldType type)
    throws  SchemaException
  {
    isTypeValid(f);
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_TYPE + f.getType());
    }
  } // protected static void isTypeEqual(f, type)

  // is this f valid for groups
  // @since 1.0
  protected static void isTypeValid(Field f)
    throws  IllegalArgumentException,
            SchemaException
  {
    Validator.argNotNull(f, E.FIELD_NULL);
    if (
      !
      (
            f.getType().equals( FieldType.ACCESS    )
        ||  f.getType().equals( FieldType.ATTRIBUTE )
        ||  f.getType().equals( FieldType.LIST      )
      )
    )
    {
      throw new SchemaException(E.FIELD_TYPE + f.getType());
    }
  } // protected static void isTypeValid(f)

}

