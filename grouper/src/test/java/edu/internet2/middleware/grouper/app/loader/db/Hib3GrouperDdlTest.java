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
 * $Id: Hib3GrouperDdlTest.java,v 1.2 2008-07-23 06:41:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;


/**
 *
 */
public class Hib3GrouperDdlTest extends GrouperTest {

  /**
   * Constructor for Hib3GrouperDdlTest.
   * 
   * @param arg0
   */
  public Hib3GrouperDdlTest(String arg0) {
    super(arg0);
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(Hib3GrouperDdlTest.class);
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
    Hib3GrouperDdl hib3GrouperDdl = HibernateSession.byHqlStatic().createQuery("from Hib3GrouperDdl where objectName = '" + testObjectName + "'").uniqueResult(Hib3GrouperDdl.class); 

    if (hib3GrouperDdl != null) {
      HibernateSession.byObjectStatic().delete(hib3GrouperDdl);
      hib3GrouperDdl = null;
    }
    
    hib3GrouperDdl = new Hib3GrouperDdl();
    hib3GrouperDdl.setDbVersion(-5);
    hib3GrouperDdl.setObjectName(testObjectName);

    assertNull("Not stored, no id", hib3GrouperDdl.getId());
    HibernateSession.byObjectStatic().saveOrUpdate(hib3GrouperDdl);
    assertNotNull("Stored, should have id", hib3GrouperDdl.getId());
    
    //try an update
    hib3GrouperDdl.setDbVersion(-8);
    HibernateSession.byObjectStatic().saveOrUpdate(hib3GrouperDdl);
    
    //now clean up, just delete
    HibernateSession.byObjectStatic().delete(hib3GrouperDdl);
  }
  
}
