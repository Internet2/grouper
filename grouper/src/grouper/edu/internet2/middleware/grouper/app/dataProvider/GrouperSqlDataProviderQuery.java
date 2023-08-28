package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;


public class GrouperSqlDataProviderQuery extends GrouperDataProviderQuery {

  @Override
  protected Class<? extends GrouperDataProviderQueryTargetDao> grouperDataProviderQueryTargetDaoClass() {
    return GrouperSqlDataProviderQueryTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperDataProviderQueryConfig> grouperDataProviderQueryConfigClass() {
    return GrouperSqlDataProviderQueryConfig.class;
  }

}
