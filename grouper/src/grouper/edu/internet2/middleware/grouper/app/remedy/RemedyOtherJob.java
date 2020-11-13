/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.remedy;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
    hib3GrouperLoaderLog.setJobMessage(GrouperUtil.mapToString(grouperRemedyFullRefresh.getDebugMap()));
    hib3GrouperLoaderLog.store();
    return null;
  }

}
