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
import  edu.internet2.middleware.grouper.internal.util.U;

/** 
 * @author  blair christensen.
 * @version $Id: AddGroupValidator.java,v 1.3 2007-04-17 17:13:26 blair Exp $
 * @since   1.2.0
 */
class AddGroupValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static AddGroupValidator validate(Stem parent, String extn, String dExtn) {
    AddGroupValidator  v   = new AddGroupValidator();
    NamingValidator   nv  = NamingValidator.validate(extn);
    if (nv.isInvalid()) {
      v.setErrorMessage( nv.getErrorMessage() );
      return v;
    }
    nv = NamingValidator.validate(dExtn);
    if (nv.isInvalid()) {
      v.setErrorMessage( nv.getErrorMessage() );
      return v;
    }
    if ( parent.isRootStem() ) {
      v.setErrorMessage("cannot create groups at root stem level");
    }
    else {
      try {
        // TODO 20070301 add "existsByName()" method to DAO?
        GrouperDAOFactory.getFactory().getGroup().findByName( U.constructName( parent.getName(), extn ) );
        v.setErrorMessage("group already exists");
      }
      catch (GroupNotFoundException eGNF) {
        v.setIsValid(true); // group does not exist, which is what we want
      }
    }
    return v;
  } // protected static AddGroupValidator validate(parent, extn, dExtn)

} // class AddGroupValidator extends GrouperValidator

