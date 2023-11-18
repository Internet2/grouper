package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public abstract class GrouperDataProviderQuery {
  
  private GrouperDataProviderQueryConfig grouperDataProviderQueryConfig;
  
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
      grouperDataProviderQueryTargetDao.setGrouperDataProviderQuery(this);
    }
    
    return this.grouperDataProviderQueryTargetDao;
  }
  
  /**
   * return the class of the config
   */
  protected abstract Class<? extends GrouperDataProviderQueryConfig> grouperDataProviderQueryConfigClass();


  /**
   * returns the subclass of Data Access Object
   * @return the config
   */
  public GrouperDataProviderQueryConfig retrieveGrouperDataProviderQueryConfig() {
    if (this.grouperDataProviderQueryConfig == null) {
      Class<? extends GrouperDataProviderQueryConfig> theClass = this.grouperDataProviderQueryConfigClass();
      this.grouperDataProviderQueryConfig = GrouperUtil.newInstance(theClass);
      grouperDataProviderQueryConfig.setGrouperDataProviderQuery(this);
    }

    return this.grouperDataProviderQueryConfig;
  }
  
  public GrouperDataProviderSync getGrouperDataProviderSync() {
    return grouperDataProviderSync;
  }

  public void setGrouperDataProviderSync(GrouperDataProviderSync grouperDataProviderSync) {
    this.grouperDataProviderSync = grouperDataProviderSync;
  }
}
