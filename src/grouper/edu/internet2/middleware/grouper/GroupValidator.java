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
import  org.apache.commons.logging.*;


/** 
 * @author  blair christensen.
 * @version $Id: GroupValidator.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
class GroupValidator implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_ACTC  = "cannot add composite membership to group with composite membership";
  protected static final String ERR_ACTM  = "cannot add composite membership to group with members";
  protected static final String ERR_AMTC  = "cannot add member to composite membership";
  protected static final String ERR_COI   = "cannot OPTIN";
  protected static final String ERR_COO   = "cannot OPTOUT";
  protected static final String ERR_DCFC  = "cannot delete non-existent composite membership";
  protected static final String ERR_DCFM  = "cannot delete composite membership from group with members";
  protected static final String ERR_DMFC  = "cannot delete member from composite membership";
  protected static final String ERR_DRA   = "cannot delete required attribute: ";
  protected static final String ERR_FT    = "invalid field type: ";
  protected static final String ERR_GT    = "invalid group type: ";
  protected static final String ERR_AV    = "invalid attribute value";

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(GroupValidator.class);

  // Protected Class Methods //
  protected static void canAddCompositeMember(Group g, Composite c)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    canWriteField(
      g.getSession(), g, g.getSession().getSubject(), Group.getDefaultList(), FieldType.LIST
    );
    if (g.hasComposite()) {
      throw new ModelException(ERR_ACTC); // TODO TEST!
    }
    if (g.getMembers().size() > 0) {
      throw new ModelException(ERR_ACTM); // TODO ModelException
    }
  } // protected static void canAddCompositeMember(g, c)

  protected static void canAddMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    try {
      canWriteField(g.getSession(), g, g.getSession().getSubject(), f, FieldType.LIST);
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
      throw new MemberAddException(ERR_AMTC); // TODO ModelException
    }
  } // protected static void canAddMember(g, subj, f)

  protected static void canDelAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if (f.getRequired()) {
      throw new ModelException(ERR_DRA + f.getName());
    }
    canModAttribute(g, f);
  } // protected static void canDelAttribute(g, f, value)

  protected static void canDelCompositeMember(Group g)
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    canWriteField(
      g.getSession(), g, g.getSession().getSubject(), Group.getDefaultList(), FieldType.LIST
    );
    if (!g.hasComposite()) {
      throw new ModelException(ERR_DCFC); 
    }
  } // protected static void canDelCompositeMember(g)

  protected static void canDelMember(Group g, Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberDeleteException,
            SchemaException
  {
    try {
      canWriteField(g.getSession(), g, g.getSession().getSubject(), f, FieldType.LIST);
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
      throw new MemberDeleteException(ERR_DMFC); // TODO ModelException
    }
  } // protected static void canDelMember(g, subj, f)

  protected static void canModAttribute(Group g, Field f) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    canWriteField(
      g.getSession(), g, g.getSession().getSubject(), f, FieldType.ATTRIBUTE
    );
  } // protected static void canModAttribute(g, f)

  protected static void canOptin(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(ERR_COI);
    } 
    // FIXME Refactor
    PrivilegeResolver.getInstance().canOPTIN(g.getSession(), g, subj);
  } // protected static void canOptin(g, subj, f)

  protected static void canOptout(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException
  {
    if (!
        (SubjectHelper.eq(g.getSession().getSubject(), subj)) 
      && f.equals(Group.getDefaultList()) 
    )
    {
      throw new InsufficientPrivilegeException(ERR_COO);
    } 
    // FIXME Refactor
    PrivilegeResolver.getInstance().canOPTOUT(g.getSession(), g, subj);
  } // protected static void canOptin(g, subj, f)

  protected static void canReadField(
    GrouperSession s, Group g, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // FIXME Can I remove s?
    // Validate the field type
    if (!f.getType().equals(type)) {
      throw new SchemaException(ERR_FT + f.getType());
    }  
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(ERR_GT + f.getGroupType().toString());
    }
    // FIXME Should this be internalized?
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, g, subj, f.getReadPriv()
    );
  } // protected static void canReadField(s, g, subj, f, type)

  protected static void canSetAttribute(Group g, Field f, String value) 
    throws  InsufficientPrivilegeException,
            ModelException,
            SchemaException
  {
    if ( (value == null) || (value.equals("")) ) {
      throw new ModelException(ERR_AV);
    }
    if (f.getName().equals("displayExtension")) {
      AttributeValidator.namingValue(value);
    }
    if (f.getName().equals("displayName")) {
      AttributeValidator.namingValue(value);
    }
    canModAttribute(g, f);
  } // protected static void canSetAttribute(g, f, value)

  protected static void canWriteField(
    GrouperSession s, Group g, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // FIXME Can I remove s?
    // Validate the field type
    if (!f.getType().equals(type)) {
      throw new SchemaException(ERR_FT + f.getType());
    }  
    // Validate that this group has the proper group type for this field
    if (!g.hasType( f.getGroupType() ) ) {
      throw new SchemaException(ERR_GT + f.getGroupType().toString());
    }
    // FIXME Should this be internalized?
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, g, subj, f.getWritePriv()
    );
  } // protected static void canWriteField(s, g, subj, f, type)

}

