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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeDefNameTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeDefNameTest.class);
    //TestRunner.run(new XmlExportAttributeDefNameTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeDefNameTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeDefName xmlExportAttributeDefName = new XmlExportAttributeDefName();
    
    xmlExportAttributeDefName.setAttributeDefId("attributeDefId");
    xmlExportAttributeDefName.setContextId("contextId");
    xmlExportAttributeDefName.setCreateTime("createTime");
    xmlExportAttributeDefName.setDescription("description");
    xmlExportAttributeDefName.setDisplayExtension("displayExtension");
    xmlExportAttributeDefName.setDisplayName("displayName");
    xmlExportAttributeDefName.setExtension("extension");
    xmlExportAttributeDefName.setHibernateVersionNumber(3L);
    xmlExportAttributeDefName.setIdIndex(12345L);
    xmlExportAttributeDefName.setModifierTime("modifierTime");
    xmlExportAttributeDefName.setName("name");
    xmlExportAttributeDefName.setParentStem("parentStem");
    xmlExportAttributeDefName.setUuid("uuid");
    
    String xml = xmlExportAttributeDefName.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeDefName = XmlExportAttributeDefName.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("attributeDefId", xmlExportAttributeDefName.getAttributeDefId());
    assertEquals("contextId", xmlExportAttributeDefName.getContextId());
    assertEquals("createTime", xmlExportAttributeDefName.getCreateTime());
    assertEquals("description", xmlExportAttributeDefName.getDescription());
    assertEquals("displayExtension", xmlExportAttributeDefName.getDisplayExtension());
    assertEquals("displayName", xmlExportAttributeDefName.getDisplayName());
    assertEquals("extension", xmlExportAttributeDefName.getExtension());
    assertEquals(3L, xmlExportAttributeDefName.getHibernateVersionNumber());
    assertEquals(12345L, xmlExportAttributeDefName.getIdIndex().longValue());
    assertEquals("modifierTime", xmlExportAttributeDefName.getModifierTime());
    assertEquals("name", xmlExportAttributeDefName.getName());
    assertEquals("parentStem", xmlExportAttributeDefName.getParentStem());
    assertEquals("uuid", xmlExportAttributeDefName.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeDefName() {
    AttributeDefName attributeDefName = new AttributeDefName();
    attributeDefName.setAttributeDefId("attributeDefId");
    attributeDefName.setContextId("contextId");
    attributeDefName.setCreatedOnDb(5L);
    attributeDefName.setDescription("description");
    attributeDefName.setDisplayExtensionDb("displayExtension");
    attributeDefName.setDisplayNameDb("displayName");
    attributeDefName.setExtensionDb("extension");
    attributeDefName.setHibernateVersionNumber(3L);
    attributeDefName.setIdIndex(12345L);
    attributeDefName.setLastUpdatedDb(6L);
    attributeDefName.setNameDb("name");
    attributeDefName.setStemId("parentUuid");
    attributeDefName.setId("uuid");
    
    XmlExportAttributeDefName xmlExportAttributeDefName = attributeDefName.xmlToExportAttributeDefName(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attributeDefName = xmlExportAttributeDefName.toAttributeDefName();
    
    assertEquals("attributeDefId", attributeDefName.getAttributeDefId());
    assertEquals("contextId", attributeDefName.getContextId());
    assertEquals(new Long(5L), attributeDefName.getCreatedOnDb());
    assertEquals("description", attributeDefName.getDescription());
    assertEquals("displayExtension", attributeDefName.getDisplayExtension());
    assertEquals("displayName", attributeDefName.getDisplayName());
    assertEquals("extension", attributeDefName.getExtension());
    assertEquals(new Long(3L), attributeDefName.getHibernateVersionNumber());
    assertEquals(new Long(12345L), attributeDefName.getIdIndex());
    assertEquals(new Long(6L), attributeDefName.getLastUpdatedDb());
    assertEquals("name", attributeDefName.getName());
    assertEquals("parentUuid", attributeDefName.getStemId());
    assertEquals("uuid", attributeDefName.getId());
    
  }
}
