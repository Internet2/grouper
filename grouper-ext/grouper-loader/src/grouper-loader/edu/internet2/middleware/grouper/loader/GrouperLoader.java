/*
 * @author mchyzer
 * $Id: GrouperLoader.java,v 1.2 2008-04-29 13:54:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;



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
    updateSchema();
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
   * add tables, types, etc
   */
  public static void updateSchema() {
    
    String ddlUtilsDbnameOverride = GrouperLoaderConfig.getPropertyString("ddlutils.dbname.override");
    Platform platform = null;
    
    //convenience to get the url, user, etc of the grouper db
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");

    if (StringUtils.isBlank(ddlUtilsDbnameOverride)) {
      platform = PlatformFactory.createNewPlatformInstance(grouperDb.getDriver(), grouperDb.getUrl());
    } else {
      platform = PlatformFactory.createNewPlatformInstance(ddlUtilsDbnameOverride);
    }
    
    Connection connection = null;
    
    try {
      connection = grouperDb.connection();

      //to be safe lets only deal with grouper tables
      platform.getModelReader().setDefaultTablePattern("GROUPER%");

      Database oldDatabase = platform.readModelFromDatabase(connection, "grouper", null, grouperDb.getUser().toUpperCase(), null);
      Database newDatabase = platform.readModelFromDatabase(connection, "grouper", null, grouperDb.getUser().toUpperCase(), null);
      
      //see if the grouper_ext_loader_log table is there
      Table table = newDatabase.findTable("grouper_ext_loader_log");
      //if not there, then create
      if (table == null) {
        table = new Table();
        table.setName("grouper_ext_loader_log");
        //table comment?
        table.setDescription("Log entries for the grouper loader extension");
        
        Column idColumn = new Column();
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        idColumn.setDescription("uuid of this log record");
        idColumn.setName("id");
        idColumn.setTypeCode(Types.VARCHAR);
        idColumn.setSize("128");
        table.addColumn(idColumn);
        
//        Table table2 = database.findTable("GROUPER_GROUPS");
//        Column groupCol = table2.findColumn("uuid");
//        
//        ForeignKey foreignKey = new ForeignKey("some_foreign_key");
//        foreignKey.setForeignTable(table2);
//        Reference reference = new Reference();
//        reference.setLocalColumn(idColumn);
//        reference.setForeignColumn(groupCol);
//        
//        foreignKey.addReference(reference);
//        
//        
//        table.addForeignKey(foreignKey);
        
        newDatabase.addTable(table);
        
      }
      Column idColumn = table.findColumn("id");
      
      Column jobTypeColumn = table.findColumn("job_type");
      if (jobTypeColumn == null) {
        jobTypeColumn = new Column();
        jobTypeColumn.setName("GrouperLoaderJobType");
        jobTypeColumn.setRequired(true);
        jobTypeColumn.setDescription("GrouperLoaderJobType enum value");
        jobTypeColumn.setTypeCode(Types.VARCHAR);
        jobTypeColumn.setSize("128");
        table.addColumn(jobTypeColumn);

        //actually it should just go last, but thats ok
        AddColumnChange addColumnChange = new AddColumnChange(table, jobTypeColumn, idColumn, null);
        SqlBuilder sqlBuilder = platform.getSqlBuilder();
        String   ddl          = null;

//        StringWriter buffer = new StringWriter();
//        sqlBuilder.setWriter(buffer);

//            processTableStructureChanges(Database currentModel,
//                Database desiredModel,
//                Table    sourceTable,
//                Table    targetTable,
//                Map      parameters,
//                List     changes) throws IOException

//        GrouperUtil.callMethod(sqlBuilder.getClass(), sqlBuilder, "processTableStructureChanges",
//            new Class[]{Database.class, Database.class, Table.class, Table.class, Map.class, List.class},
//            new Object[]{oldDatabase, newDatabase, oldDatabase.findTable("grouper_ext_loader_log"), table, null, GrouperUtil.toList(addColumnChange)});

        //getSqlBuilder().alterDatabase(currentModel, desiredModel, null);
//        ddl = buffer.toString();
        //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
        //String ddl = platform.getAlterTablesSql(connection, database);
        
//        System.out.println(ddl);
      }
    //System.out.println(ddl);
      
    } finally {
      GrouperLoaderUtils.closeQuietly(connection);
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
