package edu.internet2.middleware.grouper.changeLog;

import java.util.Collection;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * dao for change log entry temp
 * @author shilen
 */
public class ChangeLogEntryTempDao {


  public ChangeLogEntryTempDao() {
  }

  /**
   * 
   * @param changLogEntries
   * @return number of changes
   */
  public static int store(Collection<ChangeLogEntryTemp> changLogEntries) {
    if (GrouperUtil.length(changLogEntries) == 0) {
      return 0;
    }
    for (ChangeLogEntryTemp changeLogEntry : changLogEntries) {
      changeLogEntry.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().storeBatchToDatabase(changLogEntries, batchSize);
  }
}
