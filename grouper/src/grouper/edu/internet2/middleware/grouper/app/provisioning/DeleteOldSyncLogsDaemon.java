package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogDao;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class DeleteOldSyncLogsDaemon extends OtherJobBase {

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    try {
      String jobName = otherJobInput.getJobName();
      // jobName = OTHER_JOB_deleteOldSyncLogs
      jobName = GrouperClientUtils.stripPrefix(jobName, "OTHER_JOB_");
  
      int keepEntriesForSeconds = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob." + jobName + ".keepEntriesForSeconds", 604800);
      
      debugMap.put("keepEntriesForSeconds", keepEntriesForSeconds);
      
      if (keepEntriesForSeconds < 0) {
        return null;
      }
      long millisSince1970beforeWhichDelete = System.currentTimeMillis() - (keepEntriesForSeconds * 1000);
      
      List<String> idsToDelete = new GcDbAccess().sql("select id from grouper_sync_log where last_updated < ?").addBindVar(new Timestamp(millisSince1970beforeWhichDelete)).selectList(String.class);

      debugMap.put("idsToDeleteSize", GrouperUtil.length(idsToDelete));

      if (GrouperUtil.length(idsToDelete) > 0) {
        int deleted = GcGrouperSyncLogDao.internal_logDeleteByIds(idsToDelete);
        debugMap.put("deleted", deleted);
        otherJobInput.getHib3GrouperLoaderLog().setDeleteCount(deleted);
      }        
      
    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      otherJobInput.getHib3GrouperLoaderLog().setJobDescription(GrouperUtil.mapToString(debugMap));
    }
    return null;
  }

}
