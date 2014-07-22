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
package edu.internet2.middleware.grouper.tableIndex;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public enum TableIndexType {
  
  /** index assigned to a group */
  group {

    @Override
    public String tableName() {
      return Group.TABLE_GROUPER_GROUPS;
    }
  },
  
  /** index assigned to a stem */
  stem {

    @Override
    public String tableName() {
      return Stem.TABLE_GROUPER_STEMS;
    }
  },
  
  /** index assigned to an attribute def */
  attributeDef {

    @Override
    public String tableName() {
      return AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF;
    }
  },
  
  /** index assigned to an attribute name */
  attributeDefName {

    @Override
    public String tableName() {
      return AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME;
    }
  };

  /**
   * table name in grouper
   * @return the name of the table
   */
  public abstract String tableName();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TableIndexType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(TableIndexType.class, 
        string, exceptionOnNull);

  }

}
