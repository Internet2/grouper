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
import edu.internet2.middleware.grouper.attr.AttributeDefAssignmentType;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeDefNameSetTest extends GrouperTest {

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

    //TestRunner.run(XmlExportAttributeDefNameSetTest.class);
    TestRunner.run(new XmlExportAttributeDefNameSetTest("testConvertToAttributeDefNameSet"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeDefNameSetTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeDefNameSet xmlExportAttributeDefNameSet = new XmlExportAttributeDefNameSet();
    
    xmlExportAttributeDefNameSet.setContextId("contextId");
    xmlExportAttributeDefNameSet.setCreateTime("createTime");
    xmlExportAttributeDefNameSet.setDepth(5L);
    xmlExportAttributeDefNameSet.setIfHasAttributeDefNameId("ifHasAttributeDefNameId");
    xmlExportAttributeDefNameSet.setHibernateVersionNumber(3L);
    xmlExportAttributeDefNameSet.setModifierTime("modifierTime");
    xmlExportAttributeDefNameSet.setThenHasAttributeDefNameId("thenHasAttributeDefNameSetId");
    xmlExportAttributeDefNameSet.setType("type");
    xmlExportAttributeDefNameSet.setUuid("uuid");
    
    String xml = xmlExportAttributeDefNameSet.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeDefNameSet = XmlExportAttributeDefNameSet.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportAttributeDefNameSet.getContextId());
    assertEquals("createTime", xmlExportAttributeDefNameSet.getCreateTime());
    assertEquals(5L, xmlExportAttributeDefNameSet.getDepth());
    assertEquals("ifHasAttributeDefNameId", xmlExportAttributeDefNameSet.getIfHasAttributeDefNameId());
    assertEquals(3L, xmlExportAttributeDefNameSet.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeDefNameSet.getModifierTime());
    assertEquals("thenHasAttributeDefNameSetId", xmlExportAttributeDefNameSet.getThenHasAttributeDefNameId());
    assertEquals("type", xmlExportAttributeDefNameSet.getType());
    assertEquals("uuid", xmlExportAttributeDefNameSet.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeDefNameSet() {
    AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
    attributeDefNameSet.setContextId("contextId");
    attributeDefNameSet.setCreatedOnDb(new Long(4L));
    attributeDefNameSet.setDepth(5);
    attributeDefNameSet.setIfHasAttributeDefNameId("ifHasAttributeDefNameId");
    attributeDefNameSet.setHibernateVersionNumber(3L);
    attributeDefNameSet.setLastUpdatedDb(new Long(7L));
    attributeDefNameSet.setParentAttrDefNameSetId("parentAttributeDefNameSetId");
    attributeDefNameSet.setThenHasAttributeDefNameId("thenHasAttributeDefNameSetId");
    attributeDefNameSet.setType(AttributeDefAssignmentType.effective);
    attributeDefNameSet.setId("id");
    
    
    XmlExportAttributeDefNameSet xmlExportAttributeDefNameSet = attributeDefNameSet.xmlToExportAttributeDefNameSet(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attributeDefNameSet = xmlExportAttributeDefNameSet.toAttributeDefNameSet();
    
    assertEquals("contextId", attributeDefNameSet.getContextId());
    assertEquals(new Long(4L), attributeDefNameSet.getCreatedOnDb());
    assertEquals(5, attributeDefNameSet.getDepth());
    assertEquals(new Long(3L), attributeDefNameSet.getHibernateVersionNumber());
    assertEquals("ifHasAttributeDefNameId", attributeDefNameSet.getIfHasAttributeDefNameId());
    assertEquals(new Long(7), attributeDefNameSet.getLastUpdatedDb());
    assertEquals("id", attributeDefNameSet.getId());
    //doesnt export parent, only references of depth 1
    assertEquals(null, attributeDefNameSet.getParentAttrDefNameSetId());
    assertEquals("thenHasAttributeDefNameSetId", attributeDefNameSet.getThenHasAttributeDefNameId());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSet.getType());
    
  }
}
