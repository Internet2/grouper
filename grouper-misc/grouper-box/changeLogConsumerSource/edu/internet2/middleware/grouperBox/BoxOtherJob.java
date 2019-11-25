/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouperBox.GrouperBoxFullRefresh.GrouperBoxFullRefreshResults;


/**
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class BoxOtherJob extends OtherJobBase {

  /**
   * 
   */
  public BoxOtherJob() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    GrouperBoxFullRefreshResults grouperBoxFullRefreshResults = GrouperBoxFullRefresh.fullRefreshLogicWithResult();

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    hib3GrouperLoaderLog.setDeleteCount(grouperBoxFullRefreshResults.getDeleteCount());
    hib3GrouperLoaderLog.setInsertCount(grouperBoxFullRefreshResults.getInsertCount());
    hib3GrouperLoaderLog.setMillis(grouperBoxFullRefreshResults.getMillis());
    hib3GrouperLoaderLog.setMillisGetData(grouperBoxFullRefreshResults.getMillisGetData());
    hib3GrouperLoaderLog.setTotalCount(grouperBoxFullRefreshResults.getTotalCount());
    hib3GrouperLoaderLog.setUnresolvableSubjectCount(grouperBoxFullRefreshResults.getUnresolvableCount());
    return null;
  }

}
