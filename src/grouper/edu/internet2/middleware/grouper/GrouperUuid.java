/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

import  org.doomdark.uuid.UUIDGenerator;

/** 
 * Generate UUIDs.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperUuid.java,v 1.2 2005-11-11 18:32:07 blair Exp $
 *     
*/
class GrouperUuid {

  // Protected Class Methods
  protected static String getUuid() {
    return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
  }

}

