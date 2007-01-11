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
 * @version $Id: MembershipValidator.java,v 1.18 2007-01-11 14:22:06 blair Exp $
 * @since   1.0
 */
class MembershipValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_validateComposite(Membership ms)
    throws  ModelException
  {
    _validate(ms, Membership.INTERNAL_TYPE_C); 
    // Verify Depth
    if (ms.getDepth() != 0) {
      throw new ModelException(E.ERR_D + ms.getDepth());
    }
    // Verify Via
    if ( ms.getVia_id() == null ) {
      throw new ModelException(E.ERR_VC + "null");
    }    
    // Verify Parent Membership
    if (ms.getParent_membership() != null) {
      throw new ModelException(E.ERR_PMS);
    }
  } // protected static void internal_validateComposite(ms)

  // @since   1.2.0
  protected static void internal_validateEffective(Membership ms)
    throws  ModelException
  {
    _validate(ms, Membership.INTERNAL_TYPE_E); 
    // Verify Depth
    if (!(ms.getDepth() > 0)) {
      throw new ModelException(E.ERR_D + ms.getDepth());
    }
    // Verify Via
    if ( ms.getVia_id() == null ) {
      throw new ModelException(E.ERR_VC + "null");
    }    
    // Verify Parent Membership
    if (ms.getParent_membership() == null) {
      throw new ModelException(E.MSV_NO_PARENT);
    }
  } // protected static void internal_validateEffective(ms)

  // @since   1.2.0
  protected static void internal_validateImmediate(Membership ms)
    throws  ModelException
  {
    _validate(ms, Membership.INTERNAL_TYPE_I); 
    _validateDoesNotExist(ms);
    _notCircular(ms);
    // Verify Depth
    if (ms.getDepth() != 0) {
      throw new ModelException(E.ERR_D + ms.getDepth());
    }
    // Verify Via
    if (ms.getVia_id() != null) {
      throw new ModelException(E.ERR_IV);
    }
    // Verify Parent Membership
    if (ms.getParent_membership() != null) {
      throw new ModelException(E.ERR_PMS);
    }
  } // protected static void internal_validateImmediate(ms)


  // PRIVATE CLASS METHODS //

  // @since 1.0
  private static void _notCircular(Membership ms) 
    throws  ModelException
  {
    try {
      Group   g = ms.getGroup();
      Member  m = ms.getMember_id();
      Field   f = ms.getField();
      if ( f.getName().equals(GrouperConfig.LIST) ) {
        try {
          if (SubjectHelper.internal_eq(g.toSubject(), m.getSubject())) {
            throw new ModelException(E.MSV_CIRCULAR);
          }
        }
        catch (SubjectNotFoundException eSNF) {
          throw new ModelException(eSNF);
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      // ignore
    } 
  } // private static void _notCircular(ms)

  // @since 1.0
  private static void _validate(Membership ms, String type) 
    throws  ModelException
  {
    GrouperSessionValidator.internal_validate(ms.internal_getSession());
    String  t = ms.getMship_type();
    Member  m = ms.getMember_id();
    Field   f = ms.getField();
    Validator.internal_notNullPerModel(ms.getCreateTime(), "null creation time");
    Validator.internal_notNullPerModel(ms.getCreator()   , "null creator"      );
    // Verify type
    if (!t.equals(type)) {
      throw new ModelException(E.MSV_TYPE + t);
    }
    // Verify Owner
    try {
      Owner o = ms.internal_getOwner();
      if (! ( (o instanceof Group) || (o instanceof Stem) ) ) {
        throw new ModelException(E.ERR_OC + o.getClass().getName());
      }
    }
    catch (OwnerNotFoundException eONF) {
      throw new ModelException( E.ERR_OC + eONF.getMessage(), eONF );
    }
    // Verify Member
    if (m == null) {
      throw new ModelException(E.ERR_M);
    }
    // Verify Field
    if (! 
      ( 
            (f.getType().equals(FieldType.ACCESS) ) 
        ||  (f.getType().equals(FieldType.LIST  ) )    
        ||  (f.getType().equals(FieldType.NAMING) )
      )
    )
    {
      throw new ModelException(E.ERR_FT + f.getType());
    }
  } // private static void _validate(ms, type)

  // Verify that membership doesn't already exist
  // @since 1.0
  private static void _validateDoesNotExist(Membership ms) 
    throws  ModelException
  {
    try {
      MembershipFinder.internal_findByOwnerAndMemberAndFieldAndType(
        ms.internal_getOwner(), ms.getMember_id(), ms.getField(), Membership.INTERNAL_TYPE_I
      );
      throw new ModelException(E.ERR_MAE);
    }
    catch (MembershipNotFoundException eMNF) {
      // Ignore - this is what we want. 
    }
    catch (OwnerNotFoundException eONF) {
      throw new ModelException( "ms owner mysteriously does not exist: " + eONF.getMessage(), eONF );
    }
  } // private static void _validateDoesNotExist(ms)

} // class MembershipValidator

