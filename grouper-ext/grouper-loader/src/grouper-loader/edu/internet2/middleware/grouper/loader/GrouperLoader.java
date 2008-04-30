/*
 * @author mchyzer
 * $Id: GrouperLoader.java,v 1.3 2008-04-30 08:03:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.loader.util.GrouperDdlUtils;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;



/**
 * main class to start the grouper loader
 */
public class GrouperLoader {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //this will find all schedulable groups, and schedule them
    //GrouperLoaderType.scheduleLoads();
    //printAllSupportDdlUtilsPlatforms();
    GrouperDdlUtils.bootstrap();
  }

  /**
   * print out all ddlutils platforms
   */
  public static void printAllSupportDdlUtilsPlatforms() {
    String[] platforms = PlatformFactory.getSupportedPlatforms();
    Arrays.sort(platforms);
    for (String platform : platforms) {
      System.out.print(platform + ", ");
    }
  }
  
  
  /**
   * group attribute name of type of the loader, must match one of the enums in GrouperLoaderType
   */
  public static final String GROUPER_LOADER_TYPE = "grouperLoaderType";

  /**
   * job param of group name of the loader
   */
  public static final String GROUPER_LOADER_GROUP_NAME = "grouperLoaderGroupName";

  /**
   * group attribute name of type of schedule, must match one of the enums in GrouperLoaderScheduleType
   */
  public static final String GROUPER_LOADER_SCHEDULE_TYPE = "grouperLoaderScheduleType";

  /**
   * group attribute name of query, must have the required columns for the grouperLoaderType
   */
  public static final String GROUPER_LOADER_QUERY = "grouperLoaderQuery";

  /**
   * group attribute name of quartz cron-like string to describe when the job should run
   */
  public static final String GROUPER_LOADER_QUARTZ_CRON = "grouperLoaderQuartzCron";

  /**
   * group attribute name of the interval in seconds for a schedule type like START_TO_START_INTERVAL
   */
  public static final String GROUPER_LOADER_INTERVAL_SECONDS = "grouperLoaderIntervalSeconds";

  /**
   * group attribute name of priority of job, optional, if not there, will be 5.  More is better.
   * if the threadpool is full, then this priority will help the schedule pick which job should go next
   */
  public static final String GROUPER_LOADER_PRIORITY = "grouperLoaderPriority";

  /**
   * group attribute name of the db connection where this query comes from.
   * if the name is "grouper", then it will be the group db name
   */
  public static final String GROUPER_LOADER_DB_NAME = "grouperLoaderDbName";
  
  /**
   * scheduler factory singleton
   */
  private static SchedulerFactory schedulerFactory = null;

  /**
   * lazy load (and start the scheduler) the scheduler factory
   * @return the scheduler factory
   */
  public static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }
}
