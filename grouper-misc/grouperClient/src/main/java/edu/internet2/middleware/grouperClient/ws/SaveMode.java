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
  Copyright (C) 2004-2007 The University Of Pennsylvania

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
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * save mode for static saves.  insert only (exception if update),
 * update only (exception on insert), and insert or update doesnt matter.
 * @author mchyzer
 *
 */
public enum SaveMode {
  
  /** insert only, if exists then exception */
  INSERT, 
  
  /** update only, if not exist, then exception */
  UPDATE,
  
  /** it will insert or update depending if exsits or not */
  INSERT_OR_UPDATE;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum of null or exception if not found
   */
  public static SaveMode valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(SaveMode.class, string, false);
  }

}
