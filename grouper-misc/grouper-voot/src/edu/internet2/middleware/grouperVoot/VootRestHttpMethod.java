/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer $Id: GrouperRestHttpMethod.java,v 1.5 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.grouperVoot;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * types of http methods accepted by grouper rest
 */
public enum VootRestHttpMethod {

  /** GET */
  GET,

  /** POST */
  POST,

  /** PUT */
  PUT,

  /** DELETE */
  DELETE;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static VootRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(VootRestHttpMethod.class, 
        string, exceptionOnNotFound);
  }
}
