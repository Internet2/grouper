package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public abstract class GrouperDataProviderChangeLogQuery {
  
  private GrouperDataProviderChangeLogQueryConfig grouperDataProviderChangeLogQueryConfig;
  
  private GrouperDataProviderQueryTargetDao grouperDataProviderQueryTargetDao;
  
  private GrouperDataProviderSync grouperDataProviderSync;
  
  /**
   * return the class of the DAO
   */
  protected abstract Class<? extends GrouperDataProviderQueryTargetDao> grouperDataProviderQueryTargetDaoClass();
  
  
  /**
   * returns the subclass of Data Access Object
   * @return the DAO
   */
  public GrouperDataProviderQueryTargetDao retrieveGrouperDataProviderQueryTargetDao() {
    if (this.grouperDataProviderQueryTargetDao == null) {
      Class<? extends GrouperDataProviderQueryTargetDao> theClass = this.grouperDataProviderQueryTargetDaoClass();
      this.grouperDataProviderQueryTargetDao = GrouperUtil.newInstance(theClass);
      grouperDataProviderQueryTargetDao.setGrouperDataProviderChangeLogQuery(this);
    }
    
    return this.grouperDataProviderQueryTargetDao;
  }
  
  /**
   * return the class of the config
   */
  protected abstract Class<? extends GrouperDataProviderChangeLogQueryConfig> grouperDataProviderChangeLogQueryConfigClass();


  /**
   * returns the subclass of Data Access Object
   * @return the config
   */
  public GrouperDataProviderChangeLogQueryConfig retrieveGrouperDataProviderChangeLogQueryConfig() {
    if (this.grouperDataProviderChangeLogQueryConfig == null) {
      Class<? extends GrouperDataProviderChangeLogQueryConfig> theClass = this.grouperDataProviderChangeLogQueryConfigClass();
      this.grouperDataProviderChangeLogQueryConfig = GrouperUtil.newInstance(theClass);
      grouperDataProviderChangeLogQueryConfig.setGrouperDataProviderChangeLogQuery(this);
    }

    return this.grouperDataProviderChangeLogQueryConfig;
  }
  
  public GrouperDataProviderSync getGrouperDataProviderSync() {
    return grouperDataProviderSync;
  }

  public void setGrouperDataProviderSync(GrouperDataProviderSync grouperDataProviderSync) {
    this.grouperDataProviderSync = grouperDataProviderSync;
  }
}
