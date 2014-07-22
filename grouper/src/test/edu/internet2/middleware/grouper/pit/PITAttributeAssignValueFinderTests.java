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
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeAssignValueFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignValueFinderTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITAttributeAssignValueFinderTests(String name) {
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
  public void testFindByAttributeAssign() {

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.limit);
    attributeDef.setAssignToStem(true);
    attributeDef.setMultiValued(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attrDefName = edu.addChildAttributeDefName(attributeDef, "attrDefName", "attrDefName");
    AttributeDefName attrDefName2 = edu.addChildAttributeDefName(attributeDef, "attrDefName2", "attrDefName2");
    
    
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.attrDefName.limitName", attrDefName.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.attrDefName.logicClass", PITPermissionLimit.class.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.attrDefName2.limitName", attrDefName2.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.attrDefName2.logicClass", PITPermissionLimit.class.getName());
    
    //clear the cache
    PermissionLimitUtils.clearLimitLogicMap();

    
    
    AttributeAssign assign = edu.getAttributeDelegate().assignAttribute(attrDefName).getAttributeAssign();
    edu.getAttributeDelegate().assignAttribute(attrDefName2).getAttributeAssign();

    edu.getAttributeValueDelegate().addValue(attrDefName2.getName(), "bogus");
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeAll = getTimestampWithSleep();
    
    edu.getAttributeValueDelegate().addValue(attrDefName.getName(), "value1");
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp after1 = getTimestampWithSleep();
    
    edu.getAttributeValueDelegate().addValue(attrDefName.getName(), "value2");
    ChangeLogTempToEntity.convertRecords();    

    Timestamp after2 = getTimestampWithSleep();

    edu.getAttributeValueDelegate().deleteValue(attrDefName.getName(), "value1");
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp afterDelete = getTimestampWithSleep();
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign.getId(), false);
    
    Set<PITAttributeAssignValue> values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, null, null);
    assertEquals(2, values.size());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, null, beforeAll);
    assertEquals(0, values.size());

    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, null, after1);
    assertEquals(1, values.size());
    assertEquals("value1", values.iterator().next().getValueString());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, null, after2);
    assertEquals(2, values.size());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, null, afterDelete);
    assertEquals(2, values.size());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, afterDelete, null);
    assertEquals(1, values.size());
    assertEquals("value2", values.iterator().next().getValueString());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, beforeAll, beforeAll);
    assertEquals(0, values.size());
    
    values = PITAttributeAssignValueFinder.findByPITAttributeAssign(pitAttributeAssign, beforeAll, afterDelete);
    assertEquals(2, values.size());
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new PITAttributeAssignValueFinderTests("testFindPermissionsWithDisallowAndRoleInheritance"));
    TestRunner.run(PITAttributeAssignValueFinderTests.class);
  }
}
