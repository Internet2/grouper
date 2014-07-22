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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import  edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * @author  blair christensen.
 * @version $Id: AddGroupValidator.java,v 1.5 2009-11-17 02:52:29 mchyzer Exp $
 * @since   1.2.0
 */
public class AddGroupValidator extends GrouperValidator {

  /** */
  public static final String GROUP_ALREADY_EXISTS_WITH_NAME_PREFIX = "group already exists with name: '";

  /**
   * 
   * @param parent
   * @param extn
   * @param dExtn
   * @return self for chaining
   */
  public static AddGroupValidator validate(Stem parent, String extn, String dExtn) {
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
        String groupName = U.constructName( parent.getName(), extn );
        GrouperDAOFactory.getFactory().getGroup().findByName( groupName, true, new QueryOptions().secondLevelCache(false) );
        v.setErrorMessage(GROUP_ALREADY_EXISTS_WITH_NAME_PREFIX + groupName + "'");
      }
      catch (GroupNotFoundException eGNF) {
        v.setIsValid(true); // group does not exist, which is what we want
      }
    }
    return v;
  } // protected static AddGroupValidator validate(parent, extn, dExtn)

} // class AddGroupValidator extends GrouperValidator

