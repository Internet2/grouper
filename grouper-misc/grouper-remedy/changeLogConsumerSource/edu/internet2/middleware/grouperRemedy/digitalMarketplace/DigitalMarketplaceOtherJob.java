/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;


/**
 *
 */
public class DigitalMarketplaceOtherJob extends OtherJobBase {

  /**
   * 
   */
  public DigitalMarketplaceOtherJob() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    GrouperDigitalMarketplaceFullRefresh grouperDigitalMarketplaceFullRefresh = new GrouperDigitalMarketplaceFullRefresh();
    grouperDigitalMarketplaceFullRefresh.fullRefreshLogicHelper();

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    hib3GrouperLoaderLog.setDeleteCount(grouperDigitalMarketplaceFullRefresh.getDeleteCount());
    hib3GrouperLoaderLog.setInsertCount(grouperDigitalMarketplaceFullRefresh.getInsertCount());
    hib3GrouperLoaderLog.setTotalCount(grouperDigitalMarketplaceFullRefresh.getTotalCount());
    hib3GrouperLoaderLog.setMillisGetData(grouperDigitalMarketplaceFullRefresh.getMillisGetData());
    hib3GrouperLoaderLog.setMillisLoadData(grouperDigitalMarketplaceFullRefresh.getMillisLoadData());
    return null;
  }

}
