/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package edu.internet2.middleware.grouper.validator;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.membership.MembershipType;

/** 
 * A member of a group (aka composite member) has either or both of
 * an immediate (direct) membership, or an effective (indirect) membership
 * @author  blair christensen.
 * @version $Id: CompositeMembershipValidator.java,v 1.4 2009-12-07 07:31:08 mchyzer Exp $
 * @since   1.2.0
 */
public class CompositeMembershipValidator extends MembershipValidator {

  // PROTECTED CLASS CONSTANTS // 
  public static final String INVALID_DEPTH       = "membership depth is != 0";
  public static final String INVALID_PARENTUUID  = "membership cannot have parentUuid";
  public static final String INVALID_TYPE        = "membership type is not COMPOSITE";
  public static final String INVALID_VIAUUID     = "membership has invalid viaUuid";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static MembershipValidator validate(Membership _ms) {
    CompositeMembershipValidator  v     = new CompositeMembershipValidator();
    NotNullValidator              vNull = NotNullValidator.validate(_ms);
    if (vNull.isInvalid()) {
      v.setErrorMessage( vNull.getErrorMessage() );
    }
    // validate composite membership attributes
    else if ( !MembershipType.COMPOSITE.getTypeString().equals( _ms.getType() ) ) { // type must be composite
      v.setErrorMessage(INVALID_TYPE);
    }
    else if ( _ms.getDepth() != 0 )                           { // depth must be 0
      v.setErrorMessage(INVALID_DEPTH);
    }
    else if ( _ms.getViaCompositeId() == null )                      { // must have a via
      v.setErrorMessage(INVALID_VIAUUID);
    }
    else if ( _ms.getViaGroupId() != null )                      { // must have a via
      v.setErrorMessage(INVALID_VIAUUID);
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

