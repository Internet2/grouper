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
 * @version $Id: CompositeValidator.java,v 1.11 2007-02-28 19:55:26 blair Exp $
 * @since   1.0
 */
class CompositeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  //  @since 1.2.0
  protected static CompositeValidator validate(CompositeDTO _c) {
    CompositeValidator v = new CompositeValidator();
    // validate basic attributes
    if      ( _c.getCreateTime() <= GrouperConfig.EPOCH )  {
      v.setErrorMessage("composite has invalid createTime");
    }
    else if ( _c.getCreatorUuid() == null )                {
      v.setErrorMessage("composite has null creator");
    }
    else if ( _c.getUuid() == null )                       {
      v.setErrorMessage("composite has null uuid");
    }
    else if ( _c.getType() == null )                       {
      v.setErrorMessage(E.COMP_T);
    }
    else {  
      // validate that owner and factors are existing groups
      try {
        HibernateGroupDAO.findByUuid( _c.getFactorOwnerUuid() );
      }
      catch (GroupNotFoundException eGNF) {
        v.setErrorMessage("invalid owner class");
        return v;
      }
      try {
        HibernateGroupDAO.findByUuid( _c.getLeftFactorUuid() );
      }
      catch (GroupNotFoundException eGNF) {
        v.setErrorMessage("invalid left factor class");
        return v;
      }
      try {
        HibernateGroupDAO.findByUuid( _c.getRightFactorUuid() );
      }
      catch (GroupNotFoundException eGNF) {
        v.setErrorMessage("invalid right factor class");
        return v;
      }

      // validate that this is not a cyclical composite
      if      ( _c.getLeftFactorUuid().equals( _c.getRightFactorUuid() ) )  {
        v.setErrorMessage(E.COMP_LR);
      }
      else if ( _c.getFactorOwnerUuid().equals( _c.getLeftFactorUuid() ) )  {
        v.setErrorMessage(E.COMP_CL);
      }
      else if ( _c.getFactorOwnerUuid().equals( _c.getRightFactorUuid() ) ) {
        v.setErrorMessage(E.COMP_CR);
      }
      else {
        v.setIsValid(true);
      }
    }
    return v;
  } // protected static CompositeValidator validate(_c)

} // class CompositeValidator extends GrouperValidator 

