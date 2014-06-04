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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.permissions.RoleSetTest;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;


/**
 *
 */
public class XmlExportRoleSetTest extends GrouperTest {

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

    //TestRunner.run(XmlExportRoleSetTest.class);
    TestRunner.run(new XmlExportRoleSetTest("testConvertToRoleSet"));

  }
  
  /**
   * @param name
   */
  public XmlExportRoleSetTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportRoleSet xmlExportRoleSet = new XmlExportRoleSet();
    
    xmlExportRoleSet.setContextId("contextId");
    xmlExportRoleSet.setCreateTime("createTime");
    xmlExportRoleSet.setDepth(5L);
    xmlExportRoleSet.setIfHasRoleId("ifHasRoleId");
    xmlExportRoleSet.setHibernateVersionNumber(3L);
    xmlExportRoleSet.setModifierTime("modifierTime");
    xmlExportRoleSet.setThenHasRoleId("thenHasRoleSetId");
    xmlExportRoleSet.setType("type");
    xmlExportRoleSet.setUuid("uuid");
    
    String xml = xmlExportRoleSet.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportRoleSet = XmlExportRoleSet.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportRoleSet.getContextId());
    assertEquals("createTime", xmlExportRoleSet.getCreateTime());
    assertEquals(5L, xmlExportRoleSet.getDepth());
    assertEquals("ifHasRoleId", xmlExportRoleSet.getIfHasRoleId());
    assertEquals(3L, xmlExportRoleSet.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportRoleSet.getModifierTime());
    assertEquals("thenHasRoleSetId", xmlExportRoleSet.getThenHasRoleId());
    assertEquals("type", xmlExportRoleSet.getType());
    assertEquals("uuid", xmlExportRoleSet.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToRoleSet() {
    RoleSet roleSet = RoleSetTest.exampleRoleSet();
    
    
    XmlExportRoleSet xmlExportRoleSet = roleSet.xmlToExportRoleSet(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    roleSet = xmlExportRoleSet.toRoleSet();
    
    assertEquals("contextId", roleSet.getContextId());
    assertEquals(new Long(4L), roleSet.getCreatedOnDb());
    assertEquals(5, roleSet.getDepth());
    assertEquals(new Long(3L), roleSet.getHibernateVersionNumber());
    assertEquals("ifHasRoleId", roleSet.getIfHasRoleId());
    assertEquals(new Long(7), roleSet.getLastUpdatedDb());
    assertEquals("id", roleSet.getId());
    //the parent isnt exported, only references of depth 1
    assertNull(roleSet.getParentRoleSetId());
    assertEquals("thenHasRoleSetId", roleSet.getThenHasRoleId());
    assertEquals(RoleHierarchyType.effective, roleSet.getType());
    
  }
}
