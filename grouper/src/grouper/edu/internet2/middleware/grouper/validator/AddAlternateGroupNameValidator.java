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
 * Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.validator;

import edu.internet2.middleware.grouper.Stem;

/**
 * @author shilen
 * @version $Id: AddAlternateGroupNameValidator.java,v 1.4 2009-04-12 18:16:34 shilen Exp $
 */
public class AddAlternateGroupNameValidator extends GrouperValidator {

  /**
   * Checks to see if a name can be used as a new alternate name of a group.
   * @param name
   * @return AddAlternateGroupNameValidator
   */
  public static AddAlternateGroupNameValidator validate(String name) {
    AddAlternateGroupNameValidator v = new AddAlternateGroupNameValidator();

    NotNullOrEmptyValidator nnev = NotNullOrEmptyValidator.validate(name);
    if (nnev.isInvalid()) {
      v.setErrorMessage("invalid group name");
      return v;
    }

    String[] parts = name.split(Stem.DELIM, -1);
    if (parts.length < 2) {
      v.setErrorMessage("cannot create groups at root stem level");
      return v;
    }
    for (int i = 0; i < parts.length; i++) {
      NamingValidator nv = NamingValidator.validate(parts[i]);
      if (nv.isInvalid()) {
        v.setErrorMessage("invalid group name");
        return v;
      }
    }

    v.setIsValid(true);

    return v;
  }
}
