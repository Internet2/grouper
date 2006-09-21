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

/** 
 * @author  blair christensen.
 * @version $Id: GroupValidator.java,v 1.17 2006-09-21 16:10:23 blair Exp $
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
    canWriteField(g, g.getSession().getSubject(), f);
    if (g.hasComposite()) {
      throw new ModelException(E.GROUP_ACTC); // TODO TEST!
    }
    if (g.getMembers().size() > 0) {
      throw new ModelException(E.GROUP_ACTM);
    }
  } // protected static void canAddCompositeMember(g, c)

  // @since   1.1.0
  protected static void canAddGroupType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (g.hasType(type)) {
      throw new GroupModifyException(E.GROUP_HAS_TYPE);
    }
    GroupValidator.canModGroupType(s, g, type);
  } // protected static void canAddGroupType(s, g, type)

  // @since 1.0
  protected static void canAddMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    try {
      isTypeEqual(f, FieldType.LIST);
      canWriteField(g, g.getSession().getSubject(), f);
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
      throw new MemberAddException(E.GROUP_AMTC);
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
    canWriteField(g, g.getSession().getSubject(), f);
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
      canWriteField(g, g.getSession().getSubject(), f);
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
      throw new MemberDeleteException(E.GROUP_DMFC);
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

  // @throws  AttributeNotFoundException
  // @since   1.1.0
  protected static void canGetAttribute(Group g, String attr) 
    throws  AttributeNotFoundException
  {
    if (!AttributeValidator.isPermittedName(attr)) {
      throw new AttributeNotFoundException(E.INVALID_ATTR_NAME);
    }
    try {
      Field f = FieldFinder.find(attr);
      isTypeEqual(f, FieldType.ATTRIBUTE);
      if (!g.hasType( f.getGroupType() ) ) {
        throw new SchemaException(E.GROUP_DOES_NOT_HAVE_TYPE + f.getGroupType().toString());
      }
      canReadField(g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP) {
      throw new AttributeNotFoundException(eIP);
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS);
    }
  } // protected static void canGetAttribute(g, attr)

  // @since 1.0
  protected static void canModAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    isTypeEqual(f, FieldType.ATTRIBUTE);
    canWriteField(g, g.getSession().getSubject(), f);
  } // protected static void canModAttribute(g, f)

  // @since 1.0
  protected static void canModGroupType(GrouperSession s, Group g, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (GroupType.isSystemType(type)) {
      throw new SchemaException("cannot edit system group types");
    }
    if (!PrivilegeResolver.canADMIN(s, g, s.getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
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
    if (!PrivilegeResolver.canOPTIN(g.getSession(), g, subj)) {
      throw new InsufficientPrivilegeException(E.CANNOT_OPTIN);
    }
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
    if (!PrivilegeResolver.canOPTOUT(g.getSession(), g, subj)) {
      throw new InsufficientPrivilegeException(E.CANNOT_OPTOUT);
    }
  } // protected static void canOptin(g, subj, f)

  // @since 1.1
  protected static void canReadField(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.GROUP_GT + f.getGroupType().toString());
    }
    PrivilegeResolver.canPrivDispatch(g.getSession(), g, subj, f.getReadPriv());
  } // protected static void canReadField(g, subj, f)

  // @return  Attribute as {@link Field}
  // @throws  AttributeNotFoundException
  // @throws  GroupModifyException
  // @throws  InsufficientPrivilegeException
  // @throws  ModelException
  // @throws  SchemaException
  // @since   1.1.0
  protected static Field canSetAttribute(Group g, String attr, String value) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (!AttributeValidator.isPermittedName(attr)) {
      throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
    }
    if (!AttributeValidator.isPermittedValue(value)) {
      throw new GroupModifyException(E.INVALID_ATTR_VALUE + value);
    }
    Field f = FieldFinder.find(attr);
    if (f.getName().equals(GrouperConfig.ATTR_DE)) {
      AttributeValidator.namingValue(value);
    }
    if (f.getName().equals(GrouperConfig.ATTR_DN)) {
      AttributeValidator.namingValue(value);
    }
    canModAttribute(g, f);
    return f;
  } // protected static Field canSetAttribute(g, attr, value)

  // @since 1.1
  protected static void canWriteField(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            SchemaException
  {  
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.GROUP_GT + f.getGroupType().toString());
    }
    PrivilegeResolver.canPrivDispatch(g.getSession(), g, subj, f.getWritePriv());
  } // protected static void canWriteField(s, g, subj, f)

  // is this field of the appropriate type
  // @since 1.0
  protected static void isTypeEqual(Field f, FieldType type)
    throws  SchemaException
  {
    isTypeValid(f);
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
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
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }
  } // protected static void isTypeValid(f)

}

