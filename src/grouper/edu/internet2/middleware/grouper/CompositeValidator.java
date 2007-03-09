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

/** 
 * @author  blair christensen.
 * @version $Id: CompositeValidator.java,v 1.12 2007-03-09 19:28:21 blair Exp $
 * @since   1.0
 */
class CompositeValidator extends GrouperValidator {

  // PROTECTED CLASS CONSTANTS //
  protected static final String INVALID_CREATETIME      = "composite has invalid createTime";
  protected static final String INVALID_CREATORUUID     = "composite has invalid creatorUuid";
  protected static final String INVALID_FACTOROWNER     = "composite has invalid factorOwner";
  protected static final String INVALID_FACTOROWNERUUID = "composite has invalid factorOwnerUuid";
  protected static final String INVALID_LEFTFACTOR      = "composite has invalid leftFactor";
  protected static final String INVALID_LEFTFACTORUUID  = "composite has invalid leftFactorUuid";
  protected static final String INVALID_LEFTRIGHTCYCLE  = "composite has same left and right factors";
  protected static final String INVALID_OWNERLEFTCYCLE  = "composite has same owner and left factor";
  protected static final String INVALID_OWNERRIGHTCYCLE = "composite has same owner and right factor";
  protected static final String INVALID_RIGHTFACTOR     = "composite has invalid rightFactor";
  protected static final String INVALID_RIGHTFACTORUUID = "composite has invalid rightFactorUUID";
  protected static final String INVALID_TYPE            = "composite has invalid type";
  protected static final String INVALID_UUID            = "composite has invalid uuid";

  // PROTECTED CLASS METHODS //

  //  @since 1.2.0
  protected static CompositeValidator validate(CompositeDTO _c) {
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
    else if ( _c.getType() == null )                      {
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
      try {
        if      ( !HibernateGroupDAO.exists( _c.getFactorOwnerUuid() ) )  {
          v.setErrorMessage(INVALID_FACTOROWNER);
        }
        else if ( !HibernateGroupDAO.exists( _c.getLeftFactorUuid() ) )   {
          v.setErrorMessage(INVALID_LEFTFACTOR);
        }
        else if ( !HibernateGroupDAO.exists( _c.getRightFactorUuid() ) )  {
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

