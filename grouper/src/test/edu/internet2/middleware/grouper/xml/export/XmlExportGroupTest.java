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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.group.TestGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportGroupTest extends GrouperTest {

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

    //TestRunner.run(XmlExportGroupTest.class);
    TestRunner.run(new XmlExportGroupTest("testConvertToGroup"));

  }
  
  /**
   * @param name
   */
  public XmlExportGroupTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportGroup xmlExportGroup = new XmlExportGroup();
    
    xmlExportGroup.setAlternateName("alternateName");
    xmlExportGroup.setContextId("contextId");
    xmlExportGroup.setCreateTime("createTime");
    xmlExportGroup.setCreatorId("creatorId");
    xmlExportGroup.setDescription("description");
    xmlExportGroup.setDisplayExtension("displayExtension");
    xmlExportGroup.setDisplayName("displayName");
    xmlExportGroup.setExtension("extension");
    xmlExportGroup.setHibernateVersionNumber(3L);
    xmlExportGroup.setModifierId("modifierId");
    xmlExportGroup.setModifierTime("modifierTime");
    xmlExportGroup.setName("name");
    xmlExportGroup.setParentStem("parentStem");
    xmlExportGroup.setTypeOfGroup("typeOfGroup");
    xmlExportGroup.setUuid("uuid");
    
    String xml = xmlExportGroup.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportGroup = XmlExportGroup.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("alternateName", xmlExportGroup.getAlternateName());
    assertEquals("contextId", xmlExportGroup.getContextId());
    assertEquals("createTime", xmlExportGroup.getCreateTime());
    assertEquals("creatorId", xmlExportGroup.getCreatorId());
    assertEquals("description", xmlExportGroup.getDescription());
    assertEquals("displayExtension", xmlExportGroup.getDisplayExtension());
    assertEquals("displayName", xmlExportGroup.getDisplayName());
    assertEquals("extension", xmlExportGroup.getExtension());
    assertEquals(3L, xmlExportGroup.getHibernateVersionNumber());
    assertEquals("modifierId", xmlExportGroup.getModifierId());
    assertEquals("modifierTime", xmlExportGroup.getModifierTime());
    assertEquals("name", xmlExportGroup.getName());
    assertEquals("parentStem", xmlExportGroup.getParentStem());
    assertEquals("typeOfGroup", xmlExportGroup.getTypeOfGroup());
    assertEquals("uuid", xmlExportGroup.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToGroup() {
    Group group = TestGroup.exampleGroup();
    
    XmlExportGroup xmlExportGroup = group.xmlToExportGroup(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    group = xmlExportGroup.toGroup();
    
    assertEquals("alternateName", group.getAlternateNameDb());
    assertEquals("contextId", group.getContextId());
    assertEquals(5L, group.getCreateTimeLong());
    assertEquals("creatorId", group.getCreatorUuid());
    assertEquals("description", group.getDescription());
    assertEquals("displayExtension", group.getDisplayExtension());
    assertEquals("displayName", group.getDisplayName());
    assertEquals("extension", group.getExtension());
    assertEquals(new Long(3L), group.getHibernateVersionNumber());
    assertEquals(null, group.getLastMembershipChangeDb());
    assertEquals("modifierId", group.getModifierUuid());
    assertEquals(6L, group.getModifyTimeLong());
    assertEquals("name", group.getName());
    assertEquals("parentUuid", group.getParentUuid());
    assertEquals("role", group.getTypeOfGroupDb());
    assertEquals("uuid", group.getUuid());
    
  }
}
