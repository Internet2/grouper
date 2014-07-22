/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSetTest;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeAssignActionSetTest extends GrouperTest {

  /** grouperSession */
  private GrouperSession grouperSession;

  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    this.grouperSession = GrouperSession.startRootSession();
  }

 
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    super.tearDown();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    //TestRunner.run(XmlExportAttributeAssignActionSetTest.class);
    TestRunner.run(new XmlExportAttributeAssignActionSetTest("testConvertToAttributeAssignActionSet"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeAssignActionSetTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = new XmlExportAttributeAssignActionSet();
    
    xmlExportAttributeAssignActionSet.setContextId("contextId");
    xmlExportAttributeAssignActionSet.setCreateTime("createTime");
    xmlExportAttributeAssignActionSet.setDepth(5L);
    xmlExportAttributeAssignActionSet.setIfHasAttributeAssignActionId("ifHasAttributeAssignActionId");
    xmlExportAttributeAssignActionSet.setHibernateVersionNumber(3L);
    xmlExportAttributeAssignActionSet.setModifierTime("modifierTime");
    xmlExportAttributeAssignActionSet.setThenHasAttributeAssignActionId("thenHasAttributeAssignActionSetId");
    xmlExportAttributeAssignActionSet.setType("type");
    xmlExportAttributeAssignActionSet.setUuid("uuid");
    
    String xml = xmlExportAttributeAssignActionSet.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeAssignActionSet = XmlExportAttributeAssignActionSet.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportAttributeAssignActionSet.getContextId());
    assertEquals("createTime", xmlExportAttributeAssignActionSet.getCreateTime());
    assertEquals(5L, xmlExportAttributeAssignActionSet.getDepth());
    assertEquals("ifHasAttributeAssignActionId", xmlExportAttributeAssignActionSet.getIfHasAttributeAssignActionId());
    assertEquals(3L, xmlExportAttributeAssignActionSet.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeAssignActionSet.getModifierTime());
    assertEquals("thenHasAttributeAssignActionSetId", xmlExportAttributeAssignActionSet.getThenHasAttributeAssignActionId());
    assertEquals("type", xmlExportAttributeAssignActionSet.getType());
    assertEquals("uuid", xmlExportAttributeAssignActionSet.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeAssignActionSet() {
    AttributeAssignActionSet attributeAssignActionSet = AttributeAssignActionSetTest.exampleAttributeAssignActionSet();
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = attributeAssignActionSet.xmlToExportAttributeAssignActionSet(
        new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attributeAssignActionSet = xmlExportAttributeAssignActionSet.toAttributeAssignActionSet();
    
    assertEquals("contextId", attributeAssignActionSet.getContextId());
    assertEquals(new Long(4L), attributeAssignActionSet.getCreatedOnDb());
    assertEquals(5, attributeAssignActionSet.getDepth());
    assertEquals(new Long(3L), attributeAssignActionSet.getHibernateVersionNumber());
    assertEquals("ifHasAttributeAssignActionId", attributeAssignActionSet.getIfHasAttrAssignActionId());
    assertEquals(new Long(7), attributeAssignActionSet.getLastUpdatedDb());
    assertEquals("id", attributeAssignActionSet.getId());
    assertEquals(null, attributeAssignActionSet.getParentAttrAssignActionSetId());
    assertEquals("thenHasAttributeAssignActionSetId", attributeAssignActionSet.getThenHasAttrAssignActionId());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSet.getType());
    
  }
}
