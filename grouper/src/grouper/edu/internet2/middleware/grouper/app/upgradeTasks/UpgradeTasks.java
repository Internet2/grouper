/**
 * Copyright 2019 Internet2
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

package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public enum UpgradeTasks implements UpgradeTasksInterface {
  

  /**
   * add groupAttrRead/groupAttrUpdate group sets for entities
   */
  V1 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV1().updateVersionFromPrevious(otherJobInput);
      //new SyncPITTables().processMissingActivePITGroupSets();
    }
  },
  
  /**
   * move subject resolution status attributes to member table
   */
  V2 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV2().updateVersionFromPrevious(otherJobInput);
    }
  },
  V3{

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV3().updateVersionFromPrevious(otherJobInput);
    }
    
  },
  V4{

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV4().updateVersionFromPrevious(otherJobInput);
    }
    
  },
  V5 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV5().updateVersionFromPrevious(otherJobInput);
    }
    
  },
  V6 {

    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV6().updateVersionFromPrevious(otherJobInput);
    }
    
  },
  V7 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV7().updateVersionFromPrevious(otherJobInput);
    }

  },
 V8 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV8().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      boolean columnNullable = GrouperDdlUtils.isColumnNullable("grouper_members", "id_index", "subject_id", "GrouperSystem");
      return columnNullable;
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  V21 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV21().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV21().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  V22 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV22().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV22().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  V23{
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV23().updateVersionFromPrevious(otherJobInput);
    }
  }
  ,
  V24 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV24().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV24().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  V25 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV25().updateVersionFromPrevious(otherJobInput);
    }
    
    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV25().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  },
  V26 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
    }
  },
  V27 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV27().updateVersionFromPrevious(otherJobInput);
    }
    
    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV27().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
    @Override
    public boolean runOnNewInstall() {
      return true;
    }
  },
  V28 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV28().updateVersionFromPrevious(otherJobInput);
    }
    
    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV28().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
  },
  V9{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV9().updateVersionFromPrevious(otherJobInput);
    }
  }, 
  V14{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV14().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV14().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }, 
  
  V16 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV16().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV16().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
    
  }, 
  
  V19 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV19().updateVersionFromPrevious(otherJobInput);
    }
  }, 
  V20{
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV20().updateVersionFromPrevious(otherJobInput);
    }
  }
  ,
  
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V10 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV10().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV10().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }      
  ,
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V11 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV11().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV11().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  , 
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V12 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV12().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV12().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
    
  }
  ,
  /**
   * make sure source_internal_id is populated in pit tables (fields/members/groups)
   */
  V13 {
    
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      new UpgradeTaskV13().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV13().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  
  /**
   * remove old maintenance jobs
   */
  V15 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      new UpgradeTaskV15().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV15().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
  }
  ,
  /**
   * remove old maintenance jobs
   */
  V17 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      new UpgradeTaskV17().updateVersionFromPrevious(otherJobInput);
    }

    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV17().doesUpgradeTaskHaveDdlWorkToDo();
    }

    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
    
    
  }
  ,
  /**
   * remove old maintenance jobs
   */
  V18 {
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {      
      new UpgradeTaskV18().updateVersionFromPrevious(otherJobInput);
    }
  }, 
  V29{
    @Override
    public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      new UpgradeTaskV29().updateVersionFromPrevious(otherJobInput);
    }
    
    @Override
    public boolean doesUpgradeTaskHaveDdlWorkToDo() {
      return new UpgradeTaskV29().doesUpgradeTaskHaveDdlWorkToDo();
    }
  
    @Override
    public boolean upgradeTaskIsDdl() {
      return true;
    }
  }
  ;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTasks.class);

  private static int currentVersion = -1;
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    if (currentVersion == -1) {
      int max = -1;
      for (UpgradeTasks task : UpgradeTasks.values()) {
        String number = task.name().substring(1);
        int theInt = Integer.parseInt(number);
        max = Math.max(max, theInt);
      }
      currentVersion = max;
    }
    return currentVersion;
  }
  
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    return false;
  }
  
  public boolean upgradeTaskIsDdl() {
    return false;
  }
  

  public static final Set<String> v8_entityResolverSuffixesToRefactor = GrouperUtil.toSet("entityAttributesNotInSubjectSource",
      "resolveAttributesWithSQL",
      "useGlobalSQLResolver",
      "globalSQLResolver",
      "sqlConfigId",
      "tableOrViewName",
      "columnNames",
      "subjectSourceIdColumn",
      "subjectSearchMatchingColumn",
      "sqlMappingType",
      "sqlMappingEntityAttribute",
      "sqlMappingExpression",
      "lastUpdatedColumn",
      "lastUpdatedType",
      "selectAllSQLOnFull",
      "resolveAttributesWithLDAP",
      "useGlobalLDAPResolver",
      "globalLDAPResolver",
      "ldapConfigId",
      "baseDN",
      "subjectSourceId",
      "searchScope",
      "filterPart",
      "attributes",
      "multiValuedLdapAttributes",
      "ldapMatchingSearchAttribute",
      "ldapMappingType",
      "ldapMappingEntityAttribute",
      "ldapMatchingExpression",
      "filterAllLDAPOnFull",
      "lastUpdatedAttribute",
      "lastUpdatedFormat" );

}
