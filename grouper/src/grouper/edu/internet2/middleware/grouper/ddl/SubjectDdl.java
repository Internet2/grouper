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
 * @author mchyzer
 * $Id: SubjectDdl.java,v 1.12 2009-01-31 16:46:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;



/**
 * ddl versions and stuff for grouper.  All ddl classes must have a currentVersion method that
 * returns the current version
 */
public enum SubjectDdl implements DdlVersionable {

  /** first version of grouper, make sure the ddl table is there */
  V1 {
    /**
     * add the subject table
     * @see SubjectDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database, 
      DdlVersionBean)
     */
    @Override
    public void updateVersionFromPrevious(Database database, DdlVersionBean ddlVersionBean) {

      {
        Table subjectTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "subject");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectId", 
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectTypeId", 
            Types.VARCHAR, "32", false, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "name", 
            Types.VARCHAR, "255", false, false);
  
      }
      {
        Table subjectTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
            "subjectattribute");
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "subjectId", 
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "name", 
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "value", 
            Types.VARCHAR, "255", true, true);
  
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(subjectTable, "searchValue", 
            Types.VARCHAR, "255", false, false);
  
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, subjectTable.getName(), 
            "searchattribute_value_idx", false, "value");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, subjectTable.getName(), 
            "searchattribute_id_name_idx", true, "subjectId", "name");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, subjectTable.getName(), 
            "searchattribute_name_idx", false, "name");

      }
      
    }
  };

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }

  /**
   * cache this
   */
  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (SubjectDdl grouperDdl : SubjectDdl.values()) {
        String number = grouperDdl.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getObjectName()
   */
  public String getObjectName() {
    return GrouperDdlUtils.objectName(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getDefaultTablePattern()
   */
  public String getDefaultTablePattern() {
    return "SUBJECT%";
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database, DdlVersionBean)
   */
  public abstract void updateVersionFromPrevious(Database database, 
      DdlVersionBean ddlVersionBean);  
  
  /**
   * drop all views
   * @param ddlVersionBean 
   */
  public void dropAllViews(DdlVersionBean ddlVersionBean) {
    
  }
  
  /**
   * add all foreign keys
   * @param ddlVersionBean 
   */
  public void addAllForeignKeysViewsEtc(DdlVersionBean ddlVersionBean) {
    Database database = ddlVersionBean.getDatabase();

    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "subjectattribute", 
        "fk_subjectattr_subjectid", "subject", "subjectId", "subjectId");

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        "subject", "sample subject table for grouper unit tests");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "subject", "subjectId", 
        "subject id of row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "subject", "subjectTypeId", 
        "subject type e.g. person");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "subject", "name", 
        "name of this subject");

    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        "subjectattribute", "attribute data for each subject");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        "subjectattribute",  "subjectId", 
        "subject id of row");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        "subjectattribute",  "name", 
        "name of attribute");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        "subjectattribute",  "value", 
        "value of attribute");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        "subjectattribute",  "searchValue", 
        "search value (e.g. all lower)");

  }
  
  /**
   * an example table name so we can hone in on the exact metadata
   * @return the table name
   */
  public String[] getSampleTablenames() {
    return new String[]{"subject","subjectattribute"};
  }


}
