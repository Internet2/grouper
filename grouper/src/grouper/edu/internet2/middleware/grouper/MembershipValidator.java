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
 * @version $Id: MembershipValidator.java,v 1.1.2.1 2006-04-19 22:55:14 blair Exp $
 */
class MembershipValidator implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_CM  = "cannot create a circular membership";
  protected static final String ERR_D   = "membership has invalid depth: ";
  protected static final String ERR_FT  = "membership has invalid field type: ";
  protected static final String ERR_M   = "membership has null member";
  protected static final String ERR_MAE = "membership already exists";
  protected static final String ERR_O   = "membership has null owner";
  protected static final String ERR_OC  = "membership has invalid owner class: ";
  protected static final String ERR_PMS = "immediate membership has parent membership";
  protected static final String ERR_V   = "immediate membership has via";

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(MembershipValidator.class);

  // Protected Class Methods //
  protected static void validate(Membership ms) 
    throws  ModelException
  {
    GrouperSessionValidator.validate(ms.getSession());
    Owner   o = ms.getOwner_id();
    Member  m = ms.getMember_id();
    Field   f = ms.getField();
    // Verify Owner
    if (o == null) {
      throw new ModelException(ERR_O);
    }
    if (! ( (o instanceof Group) || (o instanceof Stem) ) ) {
      throw new ModelException(ERR_OC + o.getClass().getName());
    }
    // Verify Member
    if (m == null) {
      throw new ModelException(ERR_M);
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
      throw new ModelException(ERR_FT + f.getType());
    }
    // Verify Depth
    if (ms.getDepth() != 0) {
      throw new ModelException(ERR_D + ms.getDepth());
    }
    // Verify Via
    if (ms.getVia_id() != null) {
      throw new ModelException(ERR_V);
    }
    // Verify Parent Membership
    if (ms.getParent_membership() != null) {
      throw new ModelException(ERR_PMS);
    }
    // Verify that it is not a circular membership
    if ( (o instanceof Group) && (f.getName().equals(GrouperConfig.LIST)) ) {
      Group g = (Group) o;
      try {
        if (SubjectHelper.eq(g.toSubject(), m.getSubject())) {
          throw new ModelException(ERR_CM);
        }
      }
      catch (SubjectNotFoundException eSNF) {
        throw new ModelException(eSNF.getMessage()); // FIXME
      }
    }
    // Verify that membership doesn't already exist
    try {
      MembershipFinder.findImmediateMembership(o, m, f);
      throw new ModelException(ERR_MAE);
    }
    catch (MembershipNotFoundException eMNF) {
      // Ignore - this is what we want. 
    }
  } // protected static void validate(ms)

}

