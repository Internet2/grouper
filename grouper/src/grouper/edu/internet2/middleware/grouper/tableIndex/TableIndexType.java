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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ddl.GrouperDdl2_6_16;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupUser;

/**
 * 
 * @author mchyzer
 */
public enum TableIndexType {
  
  /** index assigned to a group */
  group {

    @Override
    public String tableName() {
      return Group.TABLE_GROUPER_GROUPS;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }

    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** groupInternalId assigned to a group */
  groupInternalId {

    @Override
    public String tableName() {
      return Group.TABLE_GROUPER_GROUPS;
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }

    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** index assigned to a field */
  field {

    @Override
    public String tableName() {
      return Field.TABLE_GROUPER_FIELDS;
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }

    @Override
    public boolean isHasIdColumn() {
      return true;
    }

    
  },
  
  /** index assigned to a dictionary item */
  dictionary {

    @Override
    public String tableName() {
      return "grouper_dictionary";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data load config */
  dataLoaderConfig {

    @Override
    public String tableName() {
      return "grouper_data_provider";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data field */
  dataField {

    @Override
    public String tableName() {
      return "grouper_data_field";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data row */
  dataRow {

    @Override
    public String tableName() {
      return "grouper_data_row";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data field alias */
  dataAlias {

    @Override
    public String tableName() {
      return "grouper_data_alias";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data field assign */
  dataFieldAssign {

    @Override
    public String tableName() {
      return "grouper_data_field_assign";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data global assign */
  dataGlobalAssign {

    @Override
    public String tableName() {
      return "grouper_data_global_assign";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data row field assign */
  dataRowFieldAssign {

    @Override
    public String tableName() {
      return "grouper_data_row_field_assign";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a data row assign */
  dataRowAssign {

    @Override
    public String tableName() {
      return "grouper_data_row_assign";
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  },
  
  /** index assigned to a provisioning group group */
  syncDepGroup {

    @Override
    public String tableName() {
      return GcGrouperSyncDependencyGroupGroup.TABLE_GROUPER_SYNC_DEP_GROUP_GROUP;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }

  },
  
  /** index assigned to a provisioning group group */
  syncDepUser {

    @Override
    public String tableName() {
      return GcGrouperSyncDependencyGroupUser.TABLE_GROUPER_SYNC_DEP_GROUP_USER;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }

  },
  

  /** index assigned to a member */
  member {

    @Override
    public String tableName() {
      return Member.TABLE_GROUPER_MEMBERS;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** index assigned to a member */
  memberInternalId {

    @Override
    public String tableName() {
      return Member.TABLE_GROUPER_MEMBERS;
    }

    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** index assigned to a stem */
  stem {

    @Override
    public String tableName() {
      return Stem.TABLE_GROUPER_STEMS;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** index assigned to an attribute def */
  attributeDef {

    @Override
    public String tableName() {
      return AttributeDef.TABLE_GROUPER_ATTRIBUTE_DEF;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return true;
    }
  },
  
  /** index assigned to an attribute name */
  attributeDefName {

    @Override
    public String tableName() {
      return AttributeDefName.TABLE_GROUPER_ATTRIBUTE_DEF_NAME;
    }

    @Override
    public String getIncrementingColumn() {
      return "id_index";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return true;
    }

  },
  
  /** index assigned to a membership require change */
  membershipRequire {

    @Override
    public String tableName() {
      return GrouperDdl2_6_16.TABLE_GROUPER_MSHIP_REQ_CHANGE;
    }

    @Override
    public String getIncrementingColumn() {
      return "id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  }, 
  
  /** index assigned to a sql group cache entry */
  sqlGroupCache{
  
    @Override
    public String tableName() {
      return "grouper_sql_cache_group";
    }
  
    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  }, 
  /** SQL cache dependency type */
  sqlCacheDependencyType{
  
    @Override
    public String tableName() {
      return "grouper_sql_cache_depend_type";
    }
  
    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  }, 
  /** index assigned to a sql membership cache entry */
  sqlMembershipCache{
  
    @Override
    public String tableName() {
      return "grouper_sql_cache_mship";
    }
  
    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
    }
  }, 
  /** SQL cache dependency type */
  sqlCacheDependency{
  
    @Override
    public String tableName() {
      return "grouper_sql_cache_dependency";
    }
  
    @Override
    public String getIncrementingColumn() {
      return "internal_id";
    }
    
    @Override
    public boolean isHasIdColumn() {
      return false;
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

  /**
   * column of table which is incrementing
   * @return column name
   */
  public abstract String getIncrementingColumn();
  
  /**
   * does this index type table has the id column
   * @return column name
   */
  public abstract boolean isHasIdColumn();

  /**
   * does this index type table has the id column
   * @return column name
   */
  public String getIdColumnName() {
    return "id";
  };

}
