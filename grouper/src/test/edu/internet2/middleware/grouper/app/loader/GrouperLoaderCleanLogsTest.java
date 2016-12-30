/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Date;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperLoaderCleanLogsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderCleanLogsTest("testCleanLogs"));
  }
  
  /**
   * 
   */
  public GrouperLoaderCleanLogsTest() {
  }

  /**
   * @param name
   */
  public GrouperLoaderCleanLogsTest(String name) {
    super(name);

  }

  /**
   * 
   */
  public void testCleanLogs() {
    //lets make a bunch of loader logs and change log records
    for (int i=0;i<723;i++) {
      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      hib3GrouploaderLog.setHost("abc");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
      hib3GrouploaderLog.store();

      ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_FIELD_ADD, 
          null, 
          null, null, 
          null, null, 
          null,
          null, 
          null,
          null, null
      );
      
      changeLogEntry.setTempObject(false);
      changeLogEntry.save();
    }
    
    //change the dates
    HibernateSession.bySqlStatic().executeSql("update grouper_loader_log set last_updated = ? where host = 'abc'", GrouperUtil.toListObject(new Date(0L)));
    HibernateSession.bySqlStatic().executeSql("update grouper_change_log_entry set created_on = 1");

    int grouperLoaderLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log");
    int grouperChangeLogEntryCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertTrue(grouperLoaderLogCount >= 723);
    assertTrue(grouperChangeLogEntryCount >= 723);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperBatchDeleteSelectSize", "237");
    try {
  
      GrouperLoader.runOnceByJobName(GrouperSession.startRootSession(), GrouperLoaderType.MAINTENANCE_CLEAN_LOGS);
      
      grouperLoaderLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_loader_log");
      grouperChangeLogEntryCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");
  
      assertTrue(grouperLoaderLogCount < 100);
      assertTrue(grouperChangeLogEntryCount < 100);
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperBatchDeleteSelectSize");
    }
    
  }
  
}

