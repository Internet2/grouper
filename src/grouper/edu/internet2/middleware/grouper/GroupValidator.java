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
 * @version $Id: GroupValidator.java,v 1.28 2007-02-28 17:40:44 blair Exp $
 * @since   1.0
 */
class GroupValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_canAddCompositeMember(Group g)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    Field f = Group.getDefaultList();
    internal_isTypeEqual(f, FieldType.LIST);
    internal_canWriteField(g, g.getSession().getSubject(), f);
    if ( g.hasComposite() ) {
      throw new ModelException(E.GROUP_ACTC);
    }
    if (g.getMembers().size() > 0) {
      throw new ModelException(E.GROUP_ACTM);
    }
  } // protected static void internal_canAddCompositeMember(g)

  // @since   1.2.0
  protected static void internal_canAddMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    try {
      internal_isTypeEqual(f, FieldType.LIST);
      internal_canWriteField(g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP0) {
      try {
        internal_canOptin(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP1) {
        // Throw with original message
        throw new InsufficientPrivilegeException(eIP0.getMessage(), eIP0);
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberAddException(E.GROUP_AMTC);
    }
  } // protected static void internal_canAddMember(g, subj, f)

  // @since   1.2.0
  protected static void internal_canAddType(GrouperSession s, Group g, GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (g.hasType(type)) {
      throw new GroupModifyException(E.GROUP_HAS_TYPE);
    }
    GroupValidator.internal_canModGroupType(s, g, type);
  } // protected static void internal_canAddType(s, g, type)

  // @since   1.2.0
  protected static void internal_canDelAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (f.getRequired()) {
      throw new ModelException(E.GROUP_DRA + f.getName());
    }
    internal_canModAttribute(g, f);
  } // protected static void internal_canDelAttribute(g, f, value)

  // @since   1.2.0
  protected static void internal_canDelCompositeMember(Group g)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    Field f = Group.getDefaultList();
    internal_canWriteField(g, g.getSession().getSubject(), f);
    if (!g.hasComposite()) {
      throw new ModelException(E.GROUP_DCFC); 
    }
  } // protected static void internal_canDelCompositeMember(g)

  // @since   1.2.0
  protected static void internal_canDeleteGroup(Group g)
    throws  GrouperRuntimeException,
            InsufficientPrivilegeException
  {
    GrouperSession.validate( g.getSession() );
    if (
      !PrivilegeResolver.internal_canADMIN( g.getSession(), g, g.getSession().getSubject() )
    )
    {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
  } // protected static void internal_canDeleteGroup(g)

  // @since   1.2.0
  protected static void internal_canDelMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            SchemaException
  {
    try {
      internal_isTypeEqual(f, FieldType.LIST);
      internal_canWriteField(g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP0) {
      try {
        internal_canOptout(g, subj, f);
      }
      catch (InsufficientPrivilegeException eIP1) {
        // Throw with original message
        throw new InsufficientPrivilegeException(eIP0.getMessage(), eIP0);
      }
    }
    if ( (f.equals(Group.getDefaultList())) && (g.hasComposite()) ) {
      throw new MemberDeleteException(E.GROUP_DMFC);
    }
  } // protected static void internal_canDelMember(g, subj, f)

  // @since   1.2.0
  protected static void internal_canDeleteType(GrouperSession s, Group g, GroupType type) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (!g.hasType(type)) {
      throw new ModelException("does not have type");
    }
    internal_canModGroupType(s, g, type);
  } // protected static void internal_canDeleteGroupType(s, g, type)

  // @since   1.2.0
  protected static void internal_canGetAttribute(Group g, String attr) 
    throws  AttributeNotFoundException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(attr);
    if ( !v.getIsValid() ) {
      throw new AttributeNotFoundException(E.INVALID_ATTR_NAME);
    }
    try {
      Field f = FieldFinder.find(attr);
      internal_isTypeEqual(f, FieldType.ATTRIBUTE);
      if (!g.hasType( f.getGroupType() ) ) {
        throw new SchemaException(E.GROUP_DOES_NOT_HAVE_TYPE + f.getGroupType().toString());
      }
      internal_canReadField(g, g.getSession().getSubject(), f);
    }
    catch (InsufficientPrivilegeException eIP) {
      throw new AttributeNotFoundException(eIP);
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS);
    }
  } // protected static void internal_canGetAttribute(g, attr)

  // @since   1.2.0
  protected static void internal_canModAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    internal_isTypeEqual(f, FieldType.ATTRIBUTE);
    internal_canWriteField(g, g.getSession().getSubject(), f);
  } // protected static void internal_canModAttribute(g, f)

  // @since   1.2.0
  protected static void internal_canModGroupType(GrouperSession s, Group g, GroupType type) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if ( GroupType.internal_isSystemType(type) ) {
      throw new SchemaException("cannot edit system group types");
    }
    if (!PrivilegeResolver.internal_canADMIN(s, g, s.getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
  } // protected static void internal_canModGroupType(s, g, type)

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
  } // protected static void internal_canOptin(g, subj, f)

  // @since   1.2.0
  protected static void internal_canReadField(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.INVALID_GROUP_TYPE + f.getGroupType().toString());
    }
    PrivilegeResolver.internal_canPrivDispatch(g.getSession(), g, subj, f.getReadPriv());
  } // protected static void internal_canReadField(g, subj, f)

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
      NamingValidator nv = NamingValidator.validateName(value);
      if ( !nv.getIsValid() ) {
        throw new ModelException( nv.getErrorMessage() );
      }
    }
    Field f = FieldFinder.find(attr);
    internal_canModAttribute(g, f);
    return f;
  } // protected static Field internal_canSetAttribute(g, attr, value)

  // @since   1.2.0
  protected static void internal_canWriteField(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            SchemaException
  {  
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.INVALID_GROUP_TYPE + f.getGroupType().toString());
    }
    PrivilegeResolver.internal_canPrivDispatch(g.getSession(), g, subj, f.getWritePriv());
  } // protected static void internal_canWriteField(s, g, subj, f)

  // @since   1.2.0
  protected static void internal_isTypeEqual(Field f, FieldType type)
    throws  SchemaException
  {
    internal_isTypeValid(f);
    if (!f.getType().equals(type)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }
  } // protected static void internal_isTypeEqual(f, type)

  // @since   1.2.0
  protected static void internal_isTypeValid(Field f)
    throws  IllegalArgumentException,
            SchemaException
  {
    Validator.internal_argNotNull(f, E.FIELD_NULL);
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
  } // protected static void internal_isTypeValid(f)

}

