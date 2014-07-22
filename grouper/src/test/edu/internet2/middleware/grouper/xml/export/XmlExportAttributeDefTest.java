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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefTest;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeDefTest extends GrouperTest {

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

    TestRunner.run(XmlExportAttributeDefTest.class);
    //TestRunner.run(new XmlExportAttributeDefTest("testConvertToString"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeDefTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeDef xmlExportAttributeDef = new XmlExportAttributeDef();

    xmlExportAttributeDef.setAssignToAttributeDef("assignToAttributeDef");
    xmlExportAttributeDef.setAssignToAttributeDefAssn("assignToAttributeDefAssn");
    xmlExportAttributeDef.setAssignToEffMembership("assignToEffMembership");
    xmlExportAttributeDef.setAssignToEffMembershipAssn("assignToEffMembershipAssn");
    xmlExportAttributeDef.setAssignToGroup("assignToGroup");
    xmlExportAttributeDef.setAssignToGroupAssn("assignToGroupAssn");
    xmlExportAttributeDef.setAssignToImmMembership("assignToImmMembership");
    xmlExportAttributeDef.setAssignToImmMembershipAssn("assignToImmMembershipAssn");
    xmlExportAttributeDef.setAssignToMember("assignToMember");
    xmlExportAttributeDef.setAssignToMemberAssn("assignToMemberAssn");
    xmlExportAttributeDef.setAssignToStem("assignToStem");
    xmlExportAttributeDef.setAssignToStemAssn("assignToStemAssn");
    xmlExportAttributeDef.setAttributeDefPublic("attributeDefPublic");
    xmlExportAttributeDef.setAttributeDefType("attributeDefType");
    xmlExportAttributeDef.setContextId("contextId");
    xmlExportAttributeDef.setCreateTime("createTime");
    xmlExportAttributeDef.setCreatorId("creatorId");
    xmlExportAttributeDef.setDescription("description");
    xmlExportAttributeDef.setExtension("extension");
    xmlExportAttributeDef.setHibernateVersionNumber(3L);
    xmlExportAttributeDef.setIdIndex(12345L);
    xmlExportAttributeDef.setModifierTime("modifierTime");
    xmlExportAttributeDef.setMultiAssignable("multiAssignable");
    xmlExportAttributeDef.setMultiValued("multiValued");
    xmlExportAttributeDef.setName("name");
    xmlExportAttributeDef.setParentStem("parentStem");
    xmlExportAttributeDef.setUuid("uuid");
    xmlExportAttributeDef.setValueType("valueType");
    
    String xml = xmlExportAttributeDef.toXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION));
    
    xmlExportAttributeDef = XmlExportAttributeDef.fromXml(new GrouperVersion(GrouperVersion.GROUPER_VERSION), xml);

    assertEquals("assignToAttributeDef", xmlExportAttributeDef.getAssignToAttributeDef());
    assertEquals("assignToAttributeDefAssn", xmlExportAttributeDef.getAssignToAttributeDefAssn());
    assertEquals("assignToEffMembership", xmlExportAttributeDef.getAssignToEffMembership());
    assertEquals("assignToEffMembershipAssn", xmlExportAttributeDef.getAssignToEffMembershipAssn());
    assertEquals("assignToGroup", xmlExportAttributeDef.getAssignToGroup());
    assertEquals("assignToGroupAssn", xmlExportAttributeDef.getAssignToGroupAssn());
    assertEquals("assignToImmMembership", xmlExportAttributeDef.getAssignToImmMembership());
    assertEquals("assignToImmMembershipAssn", xmlExportAttributeDef.getAssignToImmMembershipAssn());
    assertEquals("assignToMember", xmlExportAttributeDef.getAssignToMember());
    assertEquals("assignToMemberAssn", xmlExportAttributeDef.getAssignToMemberAssn());
    assertEquals("assignToStem", xmlExportAttributeDef.getAssignToStem());
    assertEquals("assignToStemAssn", xmlExportAttributeDef.getAssignToStemAssn());
    assertEquals("attributeDefPublic", xmlExportAttributeDef.getAttributeDefPublic());
    assertEquals("attributeDefType", xmlExportAttributeDef.getAttributeDefType());
    assertEquals("contextId", xmlExportAttributeDef.getContextId());
    assertEquals("createTime", xmlExportAttributeDef.getCreateTime());
    assertEquals("creatorId", xmlExportAttributeDef.getCreatorId());
    assertEquals("description", xmlExportAttributeDef.getDescription());
    assertEquals("extension", xmlExportAttributeDef.getExtension());
    assertEquals(3L, xmlExportAttributeDef.getHibernateVersionNumber());
    assertEquals(12345L, xmlExportAttributeDef.getIdIndex().longValue());
    assertEquals("modifierTime", xmlExportAttributeDef.getModifierTime());
    assertEquals("multiAssignable", xmlExportAttributeDef.getMultiAssignable());
    assertEquals("multiValued", xmlExportAttributeDef.getMultiValued());
    assertEquals("name", xmlExportAttributeDef.getName());
    assertEquals("parentStem", xmlExportAttributeDef.getParentStem());
    assertEquals("uuid", xmlExportAttributeDef.getUuid());
    assertEquals("valueType", xmlExportAttributeDef.getValueType());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeDef() {
    AttributeDef attributeDef = AttributeDefTest.exampleAttributeDef();
    XmlExportAttributeDef xmlExportAttributeDef = attributeDef.xmlToExportAttributeDef(new GrouperVersion(GrouperVersion.GROUPER_VERSION));

    //now go back
    attributeDef = xmlExportAttributeDef.toAttributeDef();
    
    assertEquals(true, attributeDef.isAssignToAttributeDef());
    assertEquals(true, attributeDef.isAssignToAttributeDefAssn());
    assertEquals(true, attributeDef.isAssignToEffMembership());
    assertEquals(true, attributeDef.isAssignToEffMembershipAssn());
    assertEquals(true, attributeDef.isAssignToGroup());
    assertEquals(true, attributeDef.isAssignToGroupAssn());
    assertEquals(true, attributeDef.isAssignToImmMembership());
    assertEquals(true, attributeDef.isAssignToImmMembershipAssn());
    assertEquals(true, attributeDef.isAssignToMember());
    assertEquals(true, attributeDef.isAssignToMemberAssn());
    assertEquals(true, attributeDef.isAssignToStem());
    assertEquals(true, attributeDef.isAssignToStemAssn());
    assertEquals(true, attributeDef.isAttributeDefPublic());
    assertEquals(AttributeDefType.attr, attributeDef.getAttributeDefType());
    assertEquals("contextId", attributeDef.getContextId());
    assertEquals(new Long(4L), attributeDef.getCreatedOnDb());
    assertEquals("creatorId", attributeDef.getCreatorId());
    assertEquals("description", attributeDef.getDescription());
    assertEquals("extension", attributeDef.getExtension());
    assertEquals(new Long(5L), attributeDef.getHibernateVersionNumber());
    assertEquals(new Long(12345L), attributeDef.getIdIndex());
    assertEquals(new Long(3L), attributeDef.getLastUpdatedDb());
    assertEquals(true, attributeDef.isMultiAssignable());
    assertEquals(true, attributeDef.isMultiValued());
    assertEquals("name", attributeDef.getName());
    assertEquals("stemId", attributeDef.getStemId());
    assertEquals("id", attributeDef.getUuid());
    assertEquals(AttributeDefValueType.floating, attributeDef.getValueType());
    
  }
}
