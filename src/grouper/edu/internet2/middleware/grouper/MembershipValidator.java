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
 * @version $Id: MembershipValidator.java,v 1.11 2006-09-11 16:58:02 blair Exp $
 * @since   1.0
 */
class MembershipValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void validateComposite(Membership ms)
    throws  ModelException
  {
    _validate(ms, MembershipType.C); 
    // Verify Depth
    if (ms.getDepth() != 0) {
      throw new ModelException(E.ERR_D + ms.getDepth());
    }
    // Verify Via
    Owner via = ms.getVia_id();
    if (via == null) {
      throw new ModelException(E.ERR_V);
    }
    if (!(via instanceof Composite)) {
      throw new ModelException(E.ERR_VC + via.getClass().getName());
    }
    // Verify Parent Membership
    if (ms.getParent_membership() != null) {
      throw new ModelException(E.ERR_PMS);
    }
  } // protected static void validateComposite(ms)

  // @since 1.0
  protected static void validateEffective(Membership ms)
    throws  ModelException
  {
    _validate(ms, MembershipType.E); 
    // Verify Depth
    if (!(ms.getDepth() > 0)) {
      throw new ModelException(E.ERR_D + ms.getDepth());
    }
    // Verify Via
    Owner via = ms.getVia_id();
    if (via == null) {
      throw new ModelException(E.ERR_EV);
    }
    if (!(via instanceof Group)) {
      throw new ModelException(E.ERR_VC + via.getClass().getName());
    }
    // Verify Parent Membership
    if (ms.getParent_membership() == null) {
      throw new ModelException(E.MSV_NO_PARENT);
    }
  } // protected static void validateEffective(ms)

  // @since 1.0
  protected static void validateImmediate(Membership ms)
    throws  ModelException
  {
    _validate(ms, MembershipType.I); 
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
  } // protected static void validateImmediate(ms)


  // PRIVATE CLASS METHODS //

  // @since 1.0
  private static void _notCircular(Membership ms) 
    throws  ModelException
  {
    Owner           o = ms.getOwner_id();
    Member          m = ms.getMember_id();
    Field           f = ms.getField();

    if ( (o instanceof Group) && (f.getName().equals(GrouperConfig.LIST)) ) {
      Group g = (Group) o;
      try {
        if (SubjectHelper.eq(g.toSubject(), m.getSubject())) {
          throw new ModelException(E.MSV_CIRCULAR);
        }
      }
      catch (SubjectNotFoundException eSNF) {
        throw new ModelException(eSNF);
      }
    }
  } // private static void _notCircular(ms)

  // @since 1.0
  private static void _validate(Membership ms, MembershipType type) 
    throws  ModelException
  {
    GrouperSessionValidator.validate(ms.getSession());
    MembershipType  t = ms.getMship_type();
    Owner           o = ms.getOwner_id();
    Member          m = ms.getMember_id();
    Field           f = ms.getField();
    Validator.notNullPerModel(ms.getCreateTime(), "null creation time");
    Validator.notNullPerModel(ms.getCreator()   , "null creator"      );
    // Verify type
    if (!t.equals(type)) {
      throw new ModelException(E.MSV_TYPE + t);
    }
    // Verify Owner
    if (o == null) {
      throw new ModelException(E.ERR_O);
    }
    if (! ( (o instanceof Group) || (o instanceof Stem) ) ) {
      throw new ModelException(E.ERR_OC + o.getClass().getName());
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
      MembershipFinder.findMembershipByTypeNoPrivNoSession(
        ms.getOwner_id(), ms.getMember_id(), ms.getField(), MembershipType.I
      );
      throw new ModelException(E.ERR_MAE);
    }
    catch (MembershipNotFoundException eMNF) {
      // Ignore - this is what we want. 
    }
  } // private static void _validateDoesNotExist(ms)

} // class MembershipValidator

