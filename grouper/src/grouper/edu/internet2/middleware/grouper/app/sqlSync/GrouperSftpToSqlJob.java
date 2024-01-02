package edu.internet2.middleware.grouper.app.sqlSync;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.app.file.GrouperSftp;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;

/**
 * 
 * @author mchyzer
 *
 */
@DisallowConcurrentExecution
public class GrouperSftpToSqlJob extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSftpToSqlJob.class);

  /**
   * 
   */
  public GrouperSftpToSqlJob() {
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
  }

  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_sftpTpSql
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.nanoTime();
    
    try {
      debugMap.put("job", "sftpTpSql");

      String sftpConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.sftp.configId");

      debugMap.put("sftpConfigId", sftpConfigId);
      
      String fileNameRemote = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.sftp.fileNameRemote");

      debugMap.put("fileNameRemote", fileNameRemote);

      boolean fileExists = GrouperSftp.existsFile(sftpConfigId, fileNameRemote);

      debugMap.put("fileExists", fileExists);

      boolean errorIfRemoteFileDoesNotExist = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sftpToSql.errorIfRemoteFileDoesNotExist", false);

      debugMap.put("errorIfRemoteFileDoesNotExist", errorIfRemoteFileDoesNotExist);

      if (!fileExists) {
        if (errorIfRemoteFileDoesNotExist) {
          throw new RuntimeException("Remote file '" + fileNameRemote + "' doesnt exist");
        }
        return null;
      }
      
      String database = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".sftpToSql.database", "grouper");
      
      debugMap.put("database", database);

      String table = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.table");

      debugMap.put("table", table);
      
      String columnsString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.columns");

      debugMap.put("columns", columnsString);
      
      List<String> columns = GrouperUtil.splitTrimToList(columnsString, ",");

      String columnsPrimaryKeyString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.columnsPrimaryKey");

      debugMap.put("columnsPrimaryKey", columnsPrimaryKeyString);
      
      List<String> columnsPrimaryKey = GrouperUtil.splitTrimToList(columnsPrimaryKeyString, ",");
      
      boolean hasHeaderRow = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sftpToSql.hasHeaderRow", false);

      String separator = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sftpToSql.separator");

      debugMap.put("separator", separator);

      String escapedSeparator = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".sftpToSql.escapedSeparator");

      debugMap.put("escapedSeparator", escapedSeparator);
      
      String sftpDir = GrouperUtil.stripLastSlashIfExists(GrouperUtil.tmpDir()) + File.separator + "grouperSftp";
      GrouperUtil.mkdirs(new File(sftpDir));

      String fileNameWithoutPath = null;
      if (fileNameRemote.contains("/")) {
        fileNameWithoutPath = GrouperUtil.prefixOrSuffix(fileNameRemote, "/", false);
      } else if (fileNameRemote.contains("\\")) {
        fileNameWithoutPath = GrouperUtil.prefixOrSuffix(fileNameRemote, "\\", false);
      } else {
        fileNameWithoutPath = fileNameRemote;
      }
      
      // add on a unique file
      String sftpReceiveFileName = sftpDir + File.separator + "sftpToSql_" 
          + GrouperUtil.timestampToFileString(new Date()) + "_" + GrouperUtil.uniqueId() + "_" + fileNameWithoutPath;
      File sftpReceiveFile = new File(sftpReceiveFileName);

      GrouperSftp.receiveFile(sftpConfigId, fileNameRemote, sftpReceiveFile);

      String fileContents = GrouperUtil.readFileIntoString(sftpReceiveFile);

      List<Object[]> rows = new ArrayList<Object[]>();
      int numberOfRows = 0;
      if (!StringUtils.isBlank(fileContents)) {
        
        List<String> fileLines = GrouperUtil.splitFileLines(fileContents);
        
        if (hasHeaderRow) {
          fileLines.remove(0);
        }
        if (GrouperUtil.length(fileLines) > 0) {
          for (String fileLine : fileLines) {
            if (StringUtils.isBlank(fileLine)) {
              continue;
            }
            String[] cols = GrouperUtil.splitTrim(fileLine, separator);
            for (int i=0;i<cols.length;i++) {
              if (!StringUtils.isBlank(escapedSeparator)) {
                cols[i] = StringUtils.replace(cols[i], escapedSeparator, separator);
              }
            }
            rows.add(cols);
          }
          numberOfRows = GrouperUtil.length(rows);
        }
      }
      
      long millisGetData = (System.nanoTime() - now) / 1000000;
      
      GcTableSyncFromData gcTableSyncFromData = new GcTableSyncFromData().assignDebugMap(debugMap).assignConnectionName(database).assignTableName(table).
            assignColumnNames(columns).assignColumnNamesPrimaryKey(columnsPrimaryKey).assignData(rows);
      gcTableSyncFromData.sync();

      TableSyncOtherJob.updateHib3LoaderLog(hib3GrouperLoaderLog, gcTableSyncFromData.getGcTableSync(), false);
      
      hib3GrouperLoaderLog.setTotalCount(numberOfRows);
      hib3GrouperLoaderLog.setMillisGetData(GrouperUtil.intObjectValue(millisGetData, false));
      
      boolean deleteFile = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sftpToSql.deleteFile", false);

      debugMap.put("deleteFile", deleteFile);
      
      if (deleteFile) {
        GrouperSftp.deleteFile(sftpConfigId, fileNameRemote);
      }
      
      GrouperUtil.deleteFile(sftpReceiveFile);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      debugMap.put("tookMillis", ((System.nanoTime()-now)/1000000));
      String debugMessage = GrouperUtil.mapToString(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMessage);
      if (LOG.isDebugEnabled()) {
        LOG.debug(debugMessage);
      }
    }

    return null;
  }

}
