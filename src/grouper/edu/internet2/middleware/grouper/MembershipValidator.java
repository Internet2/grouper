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
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;

/** 
 * @author  blair christensen.
 * @version $Id: MembershipValidator.java,v 1.30 2007-04-17 14:17:29 blair Exp $
 * @since   1.0
 */
class MembershipValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static MembershipValidator validate(MembershipDTO _ms) {
    MembershipValidator v = new MembershipValidator();
    if ( _ms.getCreateTime() == GrouperConfig.EPOCH ) {
      v.setErrorMessage("creation time is set to epoch");
    }
    else if ( _ms.getCreatorUuid() == null ) {
      v.setErrorMessage("null creator");
    }
    else if ( !v._doesOwnerExist( _ms.getOwnerUuid() ) ) {
      v.setErrorMessage("unable to find membership owner");
    }
    else if ( !GrouperDAOFactory.getFactory().getMember().exists( _ms.getMemberUuid() ) ) {
      v.setErrorMessage("unable to find membership member");
    }
    else if ( !v._doesFieldExist( _ms.getListName() ) ) {
      v.setErrorMessage("unable to find membership field");
    }
    else if ( !v._isFieldValidType( _ms.getListType() ) ) {
      v.setErrorMessage( E.ERR_FT + _ms.getListType() );
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static MembershipValidator validate(_ms)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  // TODO 20070220 this is still fuck
  private boolean _doesFieldExist(String name) {
    try {
      FieldFinder.find(name);
      return true;
    }
    catch (SchemaException eS) {
      return false;
    }
  } // private boolean _doesFieldExist(name)

  // @since   1.2.0
  private boolean _doesOwnerExist(String ownerUUID) {
    if ( GrouperDAOFactory.getFactory().getGroup().exists(ownerUUID) ) {
      return true;
    }
    return GrouperDAOFactory.getFactory().getStem().exists(ownerUUID);
  } // private boolean _doesOwnerExist(ownerUUID)

  // @since   1.2.0
  private boolean _isFieldValidType(String type) {
    if (
          FieldType.ACCESS.toString().equals(type)
      ||  FieldType.LIST.toString().equals(type)
      ||  FieldType.NAMING.toString().equals(type)
    )
    {
      return true;
    }
    return false;
  } // private boolean _isFieldValidType(type)
} // class MembershipValidator extends GrouperValidator

