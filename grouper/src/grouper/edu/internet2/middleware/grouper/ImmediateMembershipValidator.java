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
 * An immediate member is directly assigned to a group.
 * A composite group has no immediate members.  Note that a 
 * member can have 0 to 1 immediate memberships
 * to a single group, and 0 to many effective memberships to a group.
 * A group can have potentially unlimited effective 
 * memberships
 * 
 * @author  blair christensen.
 * @version $Id: ImmediateMembershipValidator.java,v 1.11 2008-06-25 05:46:05 mchyzer Exp $
 * @since   1.2.0
 */
class ImmediateMembershipValidator extends MembershipValidator {

  // PROTECTED CLASS CONSTANTS // 
  protected static final String INVALID_CIRCULAR    = "membership cannot be circular";
  protected static final String INVALID_DEPTH       = "membership depth != 0";
  protected static final String INVALID_EXISTS      = "membership already exists";
  protected static final String INVALID_PARENTUUID  = "membership cannot have parentUuid";
  protected static final String INVALID_TYPE        = "membership type is not IMMEDIATE";
  protected static final String INVALID_VIAUUID     = "membership cannot have viaUuid";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static MembershipValidator validate(Membership _ms) {
    ImmediateMembershipValidator  v     = new ImmediateMembershipValidator();
    NotNullValidator              vNull = NotNullValidator.validate(_ms);
    if (vNull.isInvalid()) {
      v.setErrorMessage( vNull.getErrorMessage() );
    }
    // validate immediate membership attributes
    else if ( !Membership.IMMEDIATE.equals( _ms.getType() ) ) { // type must be immediate
      v.setErrorMessage(INVALID_TYPE);
    }
    else if ( _ms.getDepth() != 0 )                           { // must have depth == 0
      v.setErrorMessage(INVALID_DEPTH);
    }
    else if ( _ms.getViaUuid() != null )                      { // must not have a via
      v.setErrorMessage(INVALID_VIAUUID);
    }
    else if ( _ms.getParentUuid() != null )                   { // must not have a parent
      v.setErrorMessage(INVALID_PARENTUUID);
    }
    else if ( v._isCircular(_ms) )                            { // cannot be a direct member of oneself
      v.setErrorMessage(INVALID_CIRCULAR);
    }
    else if ( 
      GrouperDAOFactory.getFactory().getMembership().exists(    // cannot already exist
        _ms.getOwnerUuid(), _ms.getMemberUuid(), _ms.getListName(), Membership.IMMEDIATE 
      )
    )
    {
      v.setErrorMessage(INVALID_EXISTS);
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


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  // TODO 20070531 it would be nice if i could avoid exceptions here
  private boolean _isCircular(Membership _ms) 
    throws  IllegalStateException
  {
    if ( GrouperConfig.LIST.equals( _ms.getListName() ) ) {
      try {
        Group  _g  = GrouperDAOFactory.getFactory().getGroup().findByUuid( _ms.getOwnerUuid() );
        Member _m  = GrouperDAOFactory.getFactory().getMember().findByUuid( _ms.getMemberUuid() );
        if ( _g.getUuid().equals( _m.getSubjectId() ) ) {
          return true;
        }
      }
      catch (GroupNotFoundException eGNF)   {
        throw new IllegalStateException( 
          "error verifying membership is not circular: " + eGNF.getMessage(), eGNF
        );
      }
      catch (MemberNotFoundException eMNF)  {
        throw new IllegalStateException( 
          "error verifying membership is not circular: " + eMNF.getMessage(), eMNF
        );
      }
    }
    return false;
  }

}

