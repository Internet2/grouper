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
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * @author  blair christensen.
 * @version $Id: CompositeValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.0
 */
public class CompositeValidator extends GrouperValidator {

  // PROTECTED CLASS CONSTANTS //
  public static final String INVALID_CREATETIME      = "composite has invalid createTime";
  public static final String INVALID_CREATORUUID     = "composite has invalid creatorUuid";
  public static final String INVALID_FACTOROWNER     = "composite has invalid factorOwner";
  public static final String INVALID_FACTOROWNERUUID = "composite has invalid factorOwnerUuid";
  public static final String INVALID_LEFTFACTOR      = "composite has invalid leftFactor";
  public static final String INVALID_LEFTFACTORUUID  = "composite has invalid leftFactorUuid";
  public static final String INVALID_LEFTRIGHTCYCLE  = "composite has same left and right factors";
  public static final String INVALID_OWNERLEFTCYCLE  = "composite has same owner and left factor";
  public static final String INVALID_OWNERRIGHTCYCLE = "composite has same owner and right factor";
  public static final String INVALID_RIGHTFACTOR     = "composite has invalid rightFactor";
  public static final String INVALID_RIGHTFACTORUUID = "composite has invalid rightFactorUUID";
  public static final String INVALID_TYPE            = "composite has invalid type";
  public static final String INVALID_UUID            = "composite has invalid uuid";

  // PROTECTED CLASS METHODS //

  //  @since 1.2.0
  public static CompositeValidator validate(Composite _c) {
    CompositeValidator  v     = new CompositeValidator();
    NotNullValidator    vNull = NotNullValidator.validate(_c);
    if (vNull.isInvalid()) {
      v.setErrorMessage( vNull.getErrorMessage() );
    }
    // validate basic attributes
    else if ( _c.getCreateTime() <= GrouperConfig.EPOCH ) {
      v.setErrorMessage(INVALID_CREATETIME);
    }
    else if ( _c.getCreatorUuid() == null )               {
      v.setErrorMessage(INVALID_CREATORUUID);
    }
    else if ( _c.getUuid() == null )                      {
      v.setErrorMessage(INVALID_UUID);
    }
    else if ( _c.getTypeDb() == null )                      {
      v.setErrorMessage(INVALID_TYPE);
    }
    else if ( _c.getFactorOwnerUuid() == null )           {
      v.setErrorMessage(INVALID_FACTOROWNERUUID);
    }
    else if ( _c.getLeftFactorUuid() == null )            {
      v.setErrorMessage(INVALID_LEFTFACTORUUID);
    }
    else if ( _c.getRightFactorUuid() == null )           {
      v.setErrorMessage(INVALID_RIGHTFACTORUUID);
    }
    // validate that this is not a cyclical composite
    else if ( _c.getLeftFactorUuid().equals( _c.getRightFactorUuid() ) )  {
      v.setErrorMessage(INVALID_LEFTRIGHTCYCLE);
    }
    else if ( _c.getFactorOwnerUuid().equals( _c.getLeftFactorUuid() ) )  {
      v.setErrorMessage(INVALID_OWNERLEFTCYCLE);
    }
    else if ( _c.getFactorOwnerUuid().equals( _c.getRightFactorUuid() ) ) {
      v.setErrorMessage(INVALID_OWNERRIGHTCYCLE);
    }
    else {  
      // validate that owner and factors are existing groups
      GroupDAO dao = GrouperDAOFactory.getFactory().getGroup();
      try {
        if      ( !dao.exists( _c.getFactorOwnerUuid() ) ) {
          v.setErrorMessage(INVALID_FACTOROWNER);
        }
        else if ( !dao.exists( _c.getLeftFactorUuid() ) ) {
          v.setErrorMessage(INVALID_LEFTFACTOR);
        }
        else if ( !dao.exists( _c.getRightFactorUuid() ) ) {
          v.setErrorMessage(INVALID_RIGHTFACTOR);
        }
        else {
          v.setIsValid(true);
        }
      }
      catch (GrouperDAOException eDAO) {
        v.setErrorMessage( eDAO.getMessage() );
      }
    }
    return v;
  } // protected static CompositeValidator validate(_c)

} // class CompositeValidator extends GrouperValidator 

