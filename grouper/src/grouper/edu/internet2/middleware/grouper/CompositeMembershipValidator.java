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

/** 
 * @author  blair christensen.
 * @version $Id: CompositeMembershipValidator.java,v 1.1 2007-02-27 18:08:09 blair Exp $
 * @since   1.2.0
 */
class CompositeMembershipValidator extends MembershipValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static CompositeMembershipValidator validate(MembershipDTO _ms) {
    CompositeMembershipValidator  v   = new CompositeMembershipValidator();
    // Perform generic Membership validation
    MembershipValidator           vMS = MembershipValidator.validate(_ms);
    if ( !vMS.getIsValid() ) {
      v.setErrorMessage( vMS.getErrorMessage() );  
      return v;
    }
    // Perform composite Membership validation
    if      ( _ms.getDepth() != 0 )         { // depth must be 0
      v.setErrorMessage( E.ERR_D + _ms.getDepth() );
    }
    else if ( _ms.getViaUuid() == null )    { // must have a via
      v.setErrorMessage(E.ERR_VC + "null");
    }
    else if ( _ms.getParentUuid() != null ) { // must not have a parent
      v.setErrorMessage(E.ERR_PMS);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static CompositeMembershipValidator validate(_ms)

} // class CompositeMembershipValidator extends MembershipValidator

