/*
 * @author mchyzer
 * $Id: GrouperLoaderJob.java,v 1.1 2008-04-28 06:40:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;


/**
 * class which will run a loader job
 */
public class GrouperLoaderJob implements Job {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperLoaderJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    try {
      
      //get all the params that define the job
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      
      String grouperLoaderGroupName = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_GROUP_NAME);
      String grouperLoaderType = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_TYPE);
      String grouperLoaderDbName = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_DB_NAME);
      String grouperLoaderQuery = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_QUERY);
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
      
      GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(grouperLoaderDbName);
      
      //based on type, run query from the db and sync members
      grouperLoaderTypeEnum.syncGroupMembership(grouperLoaderGroupName, grouperLoaderDb, grouperLoaderQuery);
      
      
//      String grouperLoaderScheduleType = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
//      String grouperLoaderQuartzCron = jobDataMap.getString(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
//      Integer grouperLoaderIntervalSeconds = jobDataMap.getIntegerFromString(GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
//      Integer grouperLoaderPriority = jobDataMap.getIntegerFromString(GrouperLoader.GROUPER_LOADER_PRIORITY);
      
      
      
      
    } catch (Throwable t) {
      LOG.error(t);
      throw new JobExecutionException(t);
    }
    
  }

}
