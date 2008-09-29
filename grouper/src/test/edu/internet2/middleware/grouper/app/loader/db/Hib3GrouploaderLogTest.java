/*
 * @author mchyzer
 * $Id: Hib3GrouploaderLogTest.java,v 1.4 2008-09-29 03:38:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class Hib3GrouploaderLogTest extends TestCase {

  /**
   * Constructor for Hib3GrouperDdlTest.
   * 
   * @param arg0
   */
  public Hib3GrouploaderLogTest(String arg0) {
    super(arg0);
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(Hib3GrouploaderLogTest.class);
  }

  /**
   * 
   */
  public void testPersistence() {

    String testObjectName = "unitTestingOnlyIgnore";
    
    //clean up before test
    HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where job_name = ?",
        GrouperUtil.toList((Object)testObjectName));
    
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
    hib3GrouploaderLog.setJobName(testObjectName);
    hib3GrouploaderLog.setJobMessage(StringUtils.repeat("a", 4001));
    assertEquals(4001, hib3GrouploaderLog.getJobMessage().length());
    
    assertNull("Not stored, no id", hib3GrouploaderLog.getId());
    hib3GrouploaderLog.store();
    assertNotNull("Stored, should have id", hib3GrouploaderLog.getId());
    
    //the value should have truncated
    assertEquals(4000, hib3GrouploaderLog.getJobMessage().length());
    
    
    //try an update
    hib3GrouploaderLog.setJobDescription("hey");
    HibernateSession.byObjectStatic().saveOrUpdate(hib3GrouploaderLog);
    
    //now clean up, just delete
    HibernateSession.byObjectStatic().delete(hib3GrouploaderLog);
  }
  
}
