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
 * @version $Id: ImmediateMembershipValidator.java,v 1.1 2007-02-20 20:29:20 blair Exp $
 * @since   1.2.0
 */
class ImmediateMembershipValidator extends MembershipValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static ImmediateMembershipValidator validate(MembershipDTO _ms) {
    ImmediateMembershipValidator  v   = new ImmediateMembershipValidator();
    // Perform generic Membership validation
    MembershipValidator           vMS = MembershipValidator.validate(_ms);
    if ( !vMS.getIsValid() ) {
      v.setErrorMessage( vMS.getErrorMessage() );  
      return v;
    }
    // Perform immediate Membership validation
    if      ( !Membership.IMMEDIATE.equals( _ms.getType() ) ) { // type must be immediate
      v.setErrorMessage( E.MSV_TYPE + _ms.getType() );
    }
    else if ( _ms.getDepth() != 0 )                           { // must have depth == 0
      v.setErrorMessage( E.ERR_D + _ms.getDepth() );
    }
    else if ( _ms.getViaUuid() != null )                      { // must not have a via
      v.setErrorMessage(E.ERR_IV);
    }
    else if ( _ms.getParentUuid() != null )                   { // must not have a parent
      v.setErrorMessage(E.ERR_PMS);
    }
    else if ( v._isCircular(_ms) )                            { // cannot be a direct member of oneself
      v.setErrorMessage(E.MSV_CIRCULAR);
    }
    else if ( v._exists(_ms) )                                { // cannot already exist
      v.setErrorMessage(E.ERR_MAE);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static ImmediateMembershipValidator validate(_ms)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  // TODO 20070220 this is still fuck
  private boolean _exists(MembershipDTO _ms) 
    throws  IllegalStateException
  {
    try {
      HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType(
        _ms.getOwnerUuid(), _ms.getMemberUuid(), FieldFinder.find( _ms.getListName() ), Membership.IMMEDIATE
      );
      return true;
    }
    catch (MembershipNotFoundException eMNF) {
      return false; 
      // Ignore - this is what we want. 
    }
    catch (SchemaException eS) {
      throw new IllegalStateException( eS.getMessage(), eS );
    }
  } // private boolean _exists(_ms)

  // @since   1.2.0
  // TODO 20070220 this is still fuck
  private boolean _isCircular(MembershipDTO _ms) 
    throws  IllegalStateException
  {
    if ( GrouperConfig.LIST.equals( _ms.getListName() ) ) {
      try {
        GroupDTO  _g  = HibernateGroupDAO.findByUuid( _ms.getOwnerUuid() );
        MemberDTO _m  = HibernateMemberDAO.findByUuid( _ms.getMemberUuid() );
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
  } // private boolean _isCircular(_ms)

} // class ImmediateMembershipValidator extends MembershipValidator

