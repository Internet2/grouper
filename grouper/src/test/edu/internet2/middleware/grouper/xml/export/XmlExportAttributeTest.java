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
import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeTest.class);
    //TestRunner.run(new XmlExportGroupTypeTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttribute xmlExportAttribute = new XmlExportAttribute();
    
    xmlExportAttribute.setContextId("contextId");
    xmlExportAttribute.setFieldId("fieldId");
    xmlExportAttribute.setGroupId("groupId");
    xmlExportAttribute.setHibernateVersionNumber(3L);
    xmlExportAttribute.setUuid("uuid");
    xmlExportAttribute.setValue("value");
    
    String xml = xmlExportAttribute.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttribute = XmlExportAttribute.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("contextId", xmlExportAttribute.getContextId());
    assertEquals("fieldId", xmlExportAttribute.getFieldId());
    assertEquals("groupId", xmlExportAttribute.getGroupId());
    assertEquals(3L, xmlExportAttribute.getHibernateVersionNumber());
    assertEquals("uuid", xmlExportAttribute.getUuid());
    assertEquals("value", xmlExportAttribute.getValue());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttribute() {
    Attribute attribute = new Attribute();
    attribute.setContextId("contextId");
    attribute.setFieldId("fieldId");
    attribute.setGroupUuid("groupId");
    attribute.setHibernateVersionNumber(3L);
    attribute.setId("uuid");
    attribute.setValue("value");
    
    XmlExportAttribute xmlExportAttribute = attribute.xmlToExportAttribute(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attribute = xmlExportAttribute.toAttribute();
    
    assertEquals("contextId", attribute.getContextId());
    assertEquals("fieldId", attribute.getFieldId());
    assertEquals("groupId", attribute.getGroupUuid());
    assertEquals(new Long(3L), attribute.getHibernateVersionNumber());
    assertEquals("uuid", attribute.getId());
    assertEquals("value", attribute.getValue());
    
  }
}
