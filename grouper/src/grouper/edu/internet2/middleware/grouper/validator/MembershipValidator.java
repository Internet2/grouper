/**
 * Copyright 2014 Internet2
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

package edu.internet2.middleware.grouper.validator;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * @author  blair christensen.
 * @version $Id: MembershipValidator.java,v 1.4 2009-09-21 06:14:27 mchyzer Exp $
 * @since   1.0
 */
public class MembershipValidator extends GrouperValidator {

  /**
   * 
   * @param _ms
   * @return validator
   */
  public static MembershipValidator validate(Membership _ms) {
    MembershipValidator v = new MembershipValidator();
    if ( _ms.getCreateTimeLong() == GrouperConfig.EPOCH ) {
      v.setErrorMessage("creation time is set to epoch");
    }
    else if ( _ms.getCreatorUuid() == null ) {
      v.setErrorMessage("null creator");
    }
    else if ( !v._doesOwnerExist( _ms.getOwnerGroupId(), _ms.getOwnerStemId(), _ms.getOwnerAttrDefId() ) ) {
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


  /**
   * TODO 20070531 should i go straight to the dao?  or would i be burned by
   * bypassing any caching done in "FieldFinder"?
   * @param name
   * @return if exist
   */
  private boolean _doesFieldExist(String name) {
    return null != FieldFinder.find(name, false);
  }

  /**
   * @param ownerAttributeDefId
   * @param ownerGroupId 
   * @param ownerStemId 
   * @return if owner id exists
   */
  private boolean _doesOwnerExist(String ownerGroupId, String ownerStemId, String ownerAttributeDefId) {
    if ( !StringUtils.isBlank(ownerGroupId) 
        && GrouperDAOFactory.getFactory().getGroup().exists(ownerGroupId) ) {
      return true;
    }
    if ( !StringUtils.isBlank(ownerStemId) 
      && GrouperDAOFactory.getFactory().getStem().exists(ownerStemId)) {
      return true;
    }
    return !StringUtils.isBlank(ownerAttributeDefId);
  } 

  /**
   * 
   * @param type
   * @return if is valid type
   */
  private boolean _isFieldValidType(String type) {
    if (
          FieldType.ACCESS.toString().equals(type)
      ||  FieldType.LIST.toString().equals(type)
      ||  FieldType.ATTRIBUTE_DEF.toString().equals(type)
      ||  FieldType.NAMING.toString().equals(type)
    )
    {
      return true;
    }
    return false;
  } 
} 

