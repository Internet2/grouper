package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfig;


public class GrouperSqlDataProviderChangeLogQuery extends GrouperDataProviderChangeLogQuery {

  @Override
  protected Class<? extends GrouperDataProviderQueryTargetDao> grouperDataProviderQueryTargetDaoClass() {
    return GrouperSqlDataProviderQueryTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperDataProviderChangeLogQueryConfig> grouperDataProviderChangeLogQueryConfigClass() {
    return GrouperSqlDataProviderChangeLogQueryConfig.class;
  }

}
