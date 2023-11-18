package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;


public class GrouperLdapDataProviderQuery extends GrouperDataProviderQuery {

  @Override
  protected Class<? extends GrouperDataProviderQueryTargetDao> grouperDataProviderQueryTargetDaoClass() {
    return GrouperLdapDataProviderQueryTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperDataProviderQueryConfig> grouperDataProviderQueryConfigClass() {
    return GrouperLdapDataProviderQueryConfig.class;
  }
}
