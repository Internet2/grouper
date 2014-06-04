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
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeAssignFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignFinderTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITAttributeAssignFinderTests(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  private Timestamp getTimestampWithSleep() {
    GrouperUtil.sleep(100);
    Date date = new Date();
    GrouperUtil.sleep(100);
    return new Timestamp(date.getTime());
  }
  
  /**
   * 
   */
  public void testFindByOwnerPITGroupAndPITAttributeDefName() {

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attrDefName = edu.addChildAttributeDefName(attributeDef, "attrDefName", "attrDefName");
    AttributeDefName attrDefName2 = edu.addChildAttributeDefName(attributeDef, "attrDefName2", "attrDefName2");
    Group group = edu.addChildGroup("testGroup", "testGroup");
    Group group2 = edu.addChildGroup("testGroup2", "testGroup2");    
    
    @SuppressWarnings("unused")
    Timestamp beforeAll = getTimestampWithSleep();
    group.getAttributeDelegate().assignAttribute(attrDefName).getAttributeAssign();
    
    @SuppressWarnings("unused")
    Timestamp after1 = getTimestampWithSleep();
    group.getAttributeDelegate().assignAttribute(attrDefName2).getAttributeAssign();

    Timestamp after2 = getTimestampWithSleep();
    AttributeAssign assignGroup2Attr1 = group2.getAttributeDelegate().assignAttribute(attrDefName).getAttributeAssign();
 
    Timestamp after3 = getTimestampWithSleep();
    group2.getAttributeDelegate().assignAttribute(attrDefName2).getAttributeAssign();

    Timestamp afterAllAdd = getTimestampWithSleep();
    
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup2 = PITGroupFinder.findMostRecentByName(group2.getName(), true);
    PITAttributeDefName pitAttrDefName = PITAttributeDefNameFinder.findByName(attrDefName.getName(), true, true).iterator().next();
    
    Set<PITAttributeAssign> assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, null, null);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, null, after2);
    assertEquals(0, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, null, after3);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, after2, after3);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterAllAdd, afterAllAdd);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterAllAdd, null);
    assertEquals(1, assignments.size());
    
    // delete assignment now
    assignGroup2Attr1.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterDelete = getTimestampWithSleep();
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterAllAdd, afterAllAdd);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterAllAdd, null);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterAllAdd, afterDelete);
    assertEquals(1, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterDelete, null);
    assertEquals(0, assignments.size());
    
    assignments = PITAttributeAssignFinder.findByOwnerPITGroupAndPITAttributeDefName(pitGroup2, pitAttrDefName, afterDelete, afterDelete);
    assertEquals(0, assignments.size());
  }
}