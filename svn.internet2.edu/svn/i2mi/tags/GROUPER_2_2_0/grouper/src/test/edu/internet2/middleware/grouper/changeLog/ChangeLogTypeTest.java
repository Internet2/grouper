/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: ChangeLogTypeTest.java,v 1.1 2009-05-12 06:35:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogEntryDAO;
import edu.internet2.middleware.grouper.internal.dao.ChangeLogTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 *
 */
public class ChangeLogTypeTest extends GrouperTest {

  /**
   * @param name
   */
  public ChangeLogTypeTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ChangeLogTypeTest("testPersistence"));
  }

  /**
   * 
   */
  public void testPersistence() {
    
    //clear out
    ChangeLogTypeDAO changeLogTypeDao = GrouperDAOFactory.getFactory().getChangeLogType();
    changeLogTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    ChangeLogType changeLogType = new ChangeLogType("a", "b", null, "s1", "s2");
    changeLogType.setId(GrouperUuid.getUuid());
    changeLogTypeDao.saveOrUpdate(changeLogType);
    
    //update and save again
    changeLogType.setLabelString03("s3");
    changeLogTypeDao.saveOrUpdate(changeLogType);
  
    ChangeLogEntry changeLogEntry = new ChangeLogEntry();
    changeLogEntry.setChangeLogTypeId(changeLogType.getId());
    changeLogEntry.setContextId("whatever");
    changeLogEntry.setString01("something");
    changeLogEntry.setString02(GrouperUuid.getUuid());
    
    ChangeLogEntryDAO changeLogEntryDao = GrouperDAOFactory.getFactory().getChangeLogEntry();
    changeLogEntryDao.save(changeLogEntry);
    
    //clear out
    changeLogTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
  }

}
