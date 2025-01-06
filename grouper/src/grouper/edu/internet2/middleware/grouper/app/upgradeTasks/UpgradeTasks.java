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
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public enum UpgradeTasks {
  

  /**
   * add groupAttrRead/groupAttrUpdate group sets for entities
   */
  V1 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV1();
    }
    
  },
  
  /**
   * move subject resolution status attributes to member table
   */
  V2 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV2();
    }
    
  },
  V3{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV3();
    }
    
  },
  V4{

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV4();
    }
    
  },
  V5 {

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV5();
    }
    
  },
  V6 {

    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV6();
    }
    
  },
  V7 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV7();
    }

  },
  V8 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV8();
    }

  },
  V9{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV9();
    }

  }, 
  V14{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV14();
    }

  }, 
  
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V10 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV10();
    }

  }      
  ,
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V11 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV11();
    }

  }
  , 
  /**
   * make sure internal_id is populated in grouper_members and make column not null
   */
  V12 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV12();
    }    
    
  }
  ,
  /**
   * make sure source_internal_id is populated in pit tables (fields/members/groups)
   */
  V13 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV13();
    }

  },  
  V29{
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV29();
    }
    
  },
  V21 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV21();
    }

  }
  ,
  V22 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV22();
    }

  },
  V23{
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV23();
    }
  }
  ,
  V24 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV24();
    }

  }
  ,
  V25 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV25();
    }
    
  },
  V26 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTasksInterface() {
        
        @Override
        public GrouperVersion versionIntroduced() {
          return GrouperVersion.valueOfIgnoreCase("5.14.0");
        }
        
        @Override
        public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
          
        }
      };
    }
  },
  V27 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV27();
    }
    
  },
  V28 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV28();
    }
    
  },
  V16 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV16();
    }

  }, 
  
  V19 {
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV19();
    }
  }, 
  V20{
    
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV20();
    }
  },
  
  /**
   * remove old maintenance jobs
   */
  V15 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV15();
    }

  }
  ,
  /**
   * remove old maintenance jobs
   */
  V17 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV17();
    }

    
  }
  ,
  /**
   * remove old maintenance jobs
   */
  V18 {
    @Override
    public UpgradeTasksInterface upgradeTask() {
      return new UpgradeTaskV18();
    }
  };
  
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
  
  public abstract UpgradeTasksInterface upgradeTask();
  

}
