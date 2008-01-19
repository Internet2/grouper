/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;

/** 
 * An effective member has an indirect membership to a group
 * (e.g. in a group within a group).  All subjects in a
 * composite group are effective members (since the composite
 * group has two groups and a set operator and no other immediate
 * members).  Note that a member can have 0 to 1 immediate memberships
 * to a single group, and 0 to many effective memberships to a group.
 * 'group within a group' can be nested to any level so long as it does 
 * not become circular.  A group can have potentially unlimited effective 
 * memberships
 * 
 * indirect membership to a group (e.g. in a group within a group).
 * @author  blair christensen.
 * @version $Id: EffectiveMembershipValidator.java,v 1.8 2008-01-19 05:41:00 mchyzer Exp $
 * @since   1.2.0
 */
class EffectiveMembershipValidator extends MembershipValidator {

  // PROTECTED CLASS CONSTANTS // 
  protected static final String INVALID_DEPTH       = "membership depth < 1";
  protected static final String INVALID_PARENTUUID  = "membership has invalid parentUuid";
  protected static final String INVALID_TYPE        = "membership type is not EFFECTIVE";
  protected static final String INVALID_VIAUUID     = "membership has invalid viaUuid";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static MembershipValidator validate(MembershipDTO _ms) {
    EffectiveMembershipValidator  v     = new EffectiveMembershipValidator();
    NotNullValidator              vNull = NotNullValidator.validate(_ms);
    if (vNull.isInvalid()) {
      v.setErrorMessage( vNull.getErrorMessage() );
    }
    // validate effective membership attributes
    else if ( !Membership.EFFECTIVE.equals( _ms.getType() ) ) { // type must be effective
      v.setErrorMessage(INVALID_TYPE);
    }
    else if ( _ms.getDepth() < 1 )                            { // must have depth > 0
      v.setErrorMessage(INVALID_DEPTH);
    }
    else if ( _ms.getViaUuid() == null )                      { // must have a via
      v.setErrorMessage(INVALID_VIAUUID);
    }
    else if ( _ms.getParentUuid() == null )                   { // must have a parent
      v.setErrorMessage(INVALID_PARENTUUID);
    }
    else {
      // Perform generic Membership validation
      MembershipValidator vMS = MembershipValidator.validate(_ms);
      if (vMS.isInvalid()) {
        v.setErrorMessage( vMS.getErrorMessage() );
      }
      else {
        v.setIsValid(true);
      }
    }
    return v;
  }

}

