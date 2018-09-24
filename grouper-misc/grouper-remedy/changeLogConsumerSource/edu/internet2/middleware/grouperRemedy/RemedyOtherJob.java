/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;


/**
 *
 */
public class RemedyOtherJob extends OtherJobBase {

  /**
   * 
   */
  public RemedyOtherJob() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    GrouperRemedyFullRefresh grouperRemedyFullRefresh = new GrouperRemedyFullRefresh();
    grouperRemedyFullRefresh.fullRefreshLogicHelper();

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    hib3GrouperLoaderLog.setDeleteCount(grouperRemedyFullRefresh.getDeleteCount());
    hib3GrouperLoaderLog.setInsertCount(grouperRemedyFullRefresh.getInsertCount());
    hib3GrouperLoaderLog.setTotalCount(grouperRemedyFullRefresh.getTotalCount());
    hib3GrouperLoaderLog.setMillisGetData(grouperRemedyFullRefresh.getMillisGetData());
    hib3GrouperLoaderLog.setMillisLoadData(grouperRemedyFullRefresh.getMillisLoadData());
    return null;
  }

}
