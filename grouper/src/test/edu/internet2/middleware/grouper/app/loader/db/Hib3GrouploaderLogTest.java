/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: Hib3GrouploaderLogTest.java,v 1.5 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Timestamp;


import org.apache.commons.lang.StringUtils;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public class Hib3GrouploaderLogTest extends GrouperTest {

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
  
  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
  }

  /**
   * 
   */
  public void testPersistence() {

    String testObjectName = "unitTestingOnlyIgnore";
    
    //clean up before test
    HibernateSession.bySqlStatic().executeSql("delete from grouper_loader_log where job_name = ?",
        HibUtils.listObject(testObjectName), HibUtils.listType(StringType.INSTANCE));
    
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
