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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeAssignTest;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttributeAssignTest extends GrouperTest {

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

    //TestRunner.run(XmlExportAttributeAssignTest.class);
    TestRunner.run(new XmlExportAttributeAssignTest("testConvertToAttributeAssign"));

  }
  
  /**
   * @param name
   */
  public XmlExportAttributeAssignTest(String name) {
    super(name);
  }


  /**
   * 
   */
  public void testConvertToXml() {
    
    XmlExportAttributeAssign xmlExportAttributeAssign = new XmlExportAttributeAssign();
    
    xmlExportAttributeAssign.setDisallowed("disallowed");
    xmlExportAttributeAssign.setAttributeAssignActionId("attributeAssignActionId");
    xmlExportAttributeAssign.setAttributeAssignDelegatable("attributeAssignDelegatable");
    xmlExportAttributeAssign.setAttributeAssignType("attributeAssignType");
    xmlExportAttributeAssign.setAttributeDefNameId("attributeDefNameId");
    xmlExportAttributeAssign.setContextId("contextId");
    xmlExportAttributeAssign.setCreateTime("createTime");
    xmlExportAttributeAssign.setDisabledTime("disabledTime");
    xmlExportAttributeAssign.setEnabled("enabled");
    xmlExportAttributeAssign.setEnabledTime("enabledTime");
    xmlExportAttributeAssign.setHibernateVersionNumber(3L);
    xmlExportAttributeAssign.setModifierTime("modifierTime");
    xmlExportAttributeAssign.setNotes("notes");
    xmlExportAttributeAssign.setOwnerAttributeAssignId("ownerAttributeAssignId");
    xmlExportAttributeAssign.setOwnerAttributeDefId("ownerAttributeDefId");
    xmlExportAttributeAssign.setOwnerGroupId("ownerGroupId");
    xmlExportAttributeAssign.setOwnerMemberId("ownerMemberId");
    xmlExportAttributeAssign.setOwnerMembershipId("ownerMembershipId");
    xmlExportAttributeAssign.setOwnerStemId("ownerStemId");
    xmlExportAttributeAssign.setUuid("uuid");
    
    String xml = xmlExportAttributeAssign.toXml(new GrouperVersion(GrouperVersion.grouperContainerVersionOrMavenVersion()));
    
    xmlExportAttributeAssign = XmlExportAttributeAssign.fromXml(new GrouperVersion(GrouperVersion.grouperContainerVersionOrMavenVersion()), xml);
    
    assertEquals("disallowed", xmlExportAttributeAssign.getDisallowed());
    assertEquals("attributeAssignActionId", xmlExportAttributeAssign.getAttributeAssignActionId());
    assertEquals("attributeAssignDelegatable", xmlExportAttributeAssign.getAttributeAssignDelegatable());
    assertEquals("attributeAssignType", xmlExportAttributeAssign.getAttributeAssignType());
    assertEquals("attributeDefNameId", xmlExportAttributeAssign.getAttributeDefNameId());
    assertEquals("contextId", xmlExportAttributeAssign.getContextId());
    assertEquals("createTime", xmlExportAttributeAssign.getCreateTime());
    assertEquals("disabledTime", xmlExportAttributeAssign.getDisabledTime());
    assertEquals("enabled", xmlExportAttributeAssign.getEnabled());
    assertEquals("enabledTime", xmlExportAttributeAssign.getEnabledTime());
    assertEquals(3L, xmlExportAttributeAssign.getHibernateVersionNumber());
    assertEquals("modifierTime", xmlExportAttributeAssign.getModifierTime());
    assertEquals("notes", xmlExportAttributeAssign.getNotes());
    assertEquals("ownerAttributeAssignId", xmlExportAttributeAssign.getOwnerAttributeAssignId());
    assertEquals("ownerAttributeDefId", xmlExportAttributeAssign.getOwnerAttributeDefId());
    assertEquals("ownerGroupId", xmlExportAttributeAssign.getOwnerGroupId());
    assertEquals("ownerMemberId", xmlExportAttributeAssign.getOwnerMemberId());
    assertEquals("ownerMembershipId", xmlExportAttributeAssign.getOwnerMembershipId());
    assertEquals("ownerStemId", xmlExportAttributeAssign.getOwnerStemId());
    assertEquals("uuid", xmlExportAttributeAssign.getUuid());
        
  }
  
  /**
   * 
   */
  public void testConvertToAttributeAssign() {
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssign();
    
    XmlExportAttributeAssign xmlExportAttributeAssign = attributeAssign.xmlToExportAttributeAssign(new GrouperVersion(GrouperVersion.grouperContainerVersionOrMavenVersion()));

    //now go back
    attributeAssign = xmlExportAttributeAssign.toAttributeAssign();
    
    assertEquals("T", attributeAssign.getDisallowedDb());
    assertEquals("attributeAssignActionId", attributeAssign.getAttributeAssignActionId());
    assertEquals(AttributeAssignDelegatable.TRUE, attributeAssign.getAttributeAssignDelegatable());
    assertEquals(AttributeAssignType.any_mem, attributeAssign.getAttributeAssignType());
    assertEquals("attributeDefNameId", attributeAssign.getAttributeDefNameId());
    assertEquals("contextId", attributeAssign.getContextId());
    assertEquals(new Long(5L), attributeAssign.getCreatedOnDb());
    assertEquals(new Long(7L), attributeAssign.getDisabledTimeDb());
    assertEquals("F", attributeAssign.getEnabledDb());
    assertEquals(new Long(8L), attributeAssign.getEnabledTimeDb());
    assertEquals(new Long(3L), attributeAssign.getHibernateVersionNumber());
    assertEquals(new Long(6L), attributeAssign.getLastUpdatedDb());
    assertEquals("notes", attributeAssign.getNotes());
    assertEquals("ownerAttributeAssignId", attributeAssign.getOwnerAttributeAssignId());
    assertEquals("ownerAttributeDefId", attributeAssign.getOwnerAttributeDefId());
    assertEquals("ownerGroupId", attributeAssign.getOwnerGroupId());
    assertEquals("ownerMemberId", attributeAssign.getOwnerMemberId());
    assertEquals("ownerMembershipId", attributeAssign.getOwnerMembershipId());
    assertEquals("ownerStemId", attributeAssign.getOwnerStemId());
    assertEquals("uuid", attributeAssign.getId());
    
  }
}
