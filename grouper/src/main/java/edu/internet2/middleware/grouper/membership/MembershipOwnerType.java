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
/**
 * 
 */
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * what type of membership this is, e.g.: list, groupPrivilege, stemPrivilege, attributeDefPrivilege
 * 
 * @author mchyzer
 */
public enum MembershipOwnerType {

  /**
   * membership list (generally this is the members list
   */
  list, 

  /**
   * group access privilege, e.g. READ or ADMIN
   */
  groupPrivilege, 

  /**
   * stem naming privilege, e.g. stem, create
   */
  stemPrivilege, 

  /**
   * attribute definition privilege
   */
  attributeDefPrivilege;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static MembershipOwnerType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    return GrouperUtil.enumValueOfIgnoreCase(MembershipOwnerType.class, 
        string, exceptionOnNull);
  }

  
  
}
