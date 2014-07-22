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
 * @author mchyzer
 * $Id: AttributeAssignDelegatable.java,v 1.1 2009-10-10 18:02:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * if whoever has an assignment can grant to someone else
 */
public enum AttributeAssignDelegatable {

  /** true that whoever has this assignment can delegate to someone else */
  TRUE {

    @Override
    public boolean delegatable() {
      return true;
    }
  },
  
  /** false, whoever has this assignment cannot delegate to someone else */
  FALSE {

    @Override
    public boolean delegatable() {
      return false;
    }
  },
  
  /** true, whoever has this assignment can delegate to someone else, and can make that assignment delegatable or grant */
  GRANT {

    @Override
    public boolean delegatable() {
      return true;
    }
  };

  /**
   * if this enum is delegatable
   * @return if delegatable
   */
  public abstract boolean delegatable();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignDelegatable valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignDelegatable.class, 
        string, exceptionOnNull);
  }

}
