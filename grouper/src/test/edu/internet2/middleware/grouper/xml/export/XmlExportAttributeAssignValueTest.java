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
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeAssignValueTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeAssignValueTest.class);
    //TestRunner.run(new XmlExportAttributeAssignValueTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeAssignValueTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeAssignValue xmlExportAttributeAssignValue = new XmlExportAttributeAssignValue();
    
    xmlExportAttributeAssignValue.setAttributeAssignId("attributeAssignId");
    xmlExportAttributeAssignValue.setContextId("contextId");
    xmlExportAttributeAssignValue.setCreateTime("createTime");
    xmlExportAttributeAssignValue.setHibernateVersionNumber(3L);
    xmlExportAttributeAssignValue.setModifierTime("modifierTime");
    xmlExportAttributeAssignValue.setUuid("uuid");
    xmlExportAttributeAssignValue.setValueInteger(7L);
    xmlExportAttributeAssignValue.setValueMemberId("valueMemberId");
    xmlExportAttributeAssignValue.setValueString("valueString");
    
    String xml = xmlExportAttributeAssignValue.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeAssignValue = XmlExportAttributeAssignValue.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);
    
    assertEquals("attributeAssignId", xmlExportAttributeAssignValue.getAttributeAssignId());
    assertEquals("contextId", xmlExportAttributeAssignValue.getContextId());
    assertEquals("createTime", xmlExportAttributeAssignValue.getCreateTime());
    assertEquals(3L, xmlExportAttributeAssignValue.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeAssignValue.getModifierTime());
    assertEquals("uuid", xmlExportAttributeAssignValue.getUuid());
    assertEquals(new Long(7L), xmlExportAttributeAssignValue.getValueInteger());
    assertEquals("valueMemberId", xmlExportAttributeAssignValue.getValueMemberId());
    assertEquals("valueString", xmlExportAttributeAssignValue.getValueString());
  }
  
  /**
   * 
   */
  public void testConvertToAttributeAssignValue() {
    AttributeAssignValue attributeAssignValue = AttributeAssignValueTest.exampleAttributeAssignValue();
    
    XmlExportAttributeAssignValue xmlExportAttributeAssignValue = attributeAssignValue.xmlToExportAttributeAssignValue(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attributeAssignValue = xmlExportAttributeAssignValue.toAttributeAssignValue();
    
    assertEquals("attributeAssignId", attributeAssignValue.getAttributeAssignId());
    assertEquals("contextId", attributeAssignValue.getContextId());
    assertEquals(new Long(5L), attributeAssignValue.getCreatedOnDb());
    assertEquals(new Long(3L), attributeAssignValue.getHibernateVersionNumber());
    assertEquals(new Long(6L), attributeAssignValue.getLastUpdatedDb());
    assertEquals("uuid", attributeAssignValue.getId());
    assertEquals(new Long(7L), attributeAssignValue.getValueInteger());
    assertEquals("valueMemberId", attributeAssignValue.getValueMemberId());
    assertEquals("valueString", attributeAssignValue.getValueString());
    
  }
}
